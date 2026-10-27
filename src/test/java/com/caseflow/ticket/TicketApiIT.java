package com.caseflow.ticket;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import java.util.Map;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TicketApiIT {
    static final PostgreSQLContainer<?> postgres;
    static {
        if (System.getenv("IT_DB_URL") == null) {
            postgres = new PostgreSQLContainer<>("postgres:17-alpine");
            postgres.start();
        } else postgres = null;
    }
    @DynamicPropertySource
    static void database(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", () -> postgres == null ? System.getenv("IT_DB_URL") : postgres.getJdbcUrl());
        r.add("spring.datasource.username", () -> postgres == null ? System.getenv("IT_DB_USER") : postgres.getUsername());
        r.add("spring.datasource.password", () -> postgres == null ? System.getenv("IT_DB_PASSWORD") : postgres.getPassword());
    }
    @Autowired TestRestTemplate http;
    @Autowired JdbcTemplate jdbc;

    @Test void migrationsAndSyntheticWorkspaceAreAvailable() {
        assertThat(jdbc.queryForObject("select count(*) from flyway_schema_history where success and version in ('1','2')", Integer.class)).isEqualTo(2);
        var operators = http.getForEntity("/api/operators", JsonNode.class);
        assertThat(operators.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(operators.getBody().size()).isEqualTo(4);
        var seeded = http.getForObject("/api/tickets/1", JsonNode.class);
        assertThat(seeded.path("title").asText()).isEqualTo("Invoice export delayed");
        assertThat(seeded.path("comments").size()).isEqualTo(1);
        assertThat(seeded.path("history").size()).isGreaterThanOrEqualTo(4);
    }
    @Test void triageWorkflowPersistsAssignmentCommentAndHistory() {
        var created = http.postForEntity("/api/tickets", Map.of("title", "Reconciliation timeout", "description", "Regional reconciliation needs investigation", "priority", "HIGH"), JsonNode.class);
        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        var location = created.getHeaders().getLocation();
        assertThat(location).isNotNull();
        String path = location.toString();
        var assigned = http.exchange(path + "/assignee", HttpMethod.PUT, new HttpEntity<>(Map.of("operatorId", 1)), JsonNode.class);
        assertThat(assigned.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(assigned.getBody().path("assigneeId").asLong()).isEqualTo(1);
        var moved = http.postForEntity(path + "/transitions", Map.of("status", "IN_PROGRESS"), JsonNode.class);
        assertThat(moved.getStatusCode()).isEqualTo(HttpStatus.OK);
        var comment = http.postForEntity(path + "/comments", Map.of("authorId", 1, "body", "Investigating the failed batch"), JsonNode.class);
        assertThat(comment.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        var reloaded = http.getForObject(path, JsonNode.class);
        assertThat(reloaded.path("status").asText()).isEqualTo("IN_PROGRESS");
        assertThat(reloaded.path("comments").size()).isEqualTo(1);
        assertThat(reloaded.path("history").size()).isEqualTo(4);
        assertThat(jdbc.queryForObject("select count(*) from ticket_history where ticket_id = ?", Integer.class, reloaded.path("id").asLong())).isEqualTo(4);
    }
    @Test void individualFiltersAndPaginationAreApplied() {
        var result = http.getForObject("/api/tickets?priority=URGENT&size=1", JsonNode.class);
        assertThat(result.path("content").size()).isEqualTo(1);
        assertThat(result.path("content").get(0).path("priority").asText()).isEqualTo("URGENT");
        var title = http.getForObject("/api/tickets?q=INVOICE&status=NEW", JsonNode.class);
        assertThat(title.path("content").size()).isGreaterThanOrEqualTo(1);
        for (JsonNode row : title.path("content")) {
            assertThat(row.path("title").asText().toLowerCase()).contains("invoice");
            assertThat(row.path("status").asText()).isEqualTo("NEW");
        }
        var page = http.getForObject("/api/tickets?page=1&size=2", JsonNode.class);
        assertThat(page.path("content").size()).isEqualTo(2);
        assertThat(page.path("page").asInt()).isEqualTo(1);
    }
    @Test void invalidCreateAndPageAreRejectedWithoutAddingATicket() {
        int before = jdbc.queryForObject("select count(*) from tickets", Integer.class);
        assertThat(http.postForEntity("/api/tickets", Map.of("title", "   ", "description", "Details", "priority", "HIGH"), JsonNode.class).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(http.getForEntity("/api/tickets?size=101", JsonNode.class).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(jdbc.queryForObject("select count(*) from tickets", Integer.class)).isEqualTo(before);
    }
    @Test void missingTicketIsNotFoundAndRejectedTransitionLeavesNoAuditEntry() {
        assertThat(http.getForEntity("/api/tickets/999999", JsonNode.class).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        int before = jdbc.queryForObject("select count(*) from ticket_history where ticket_id = 4", Integer.class);
        assertThat(http.postForEntity("/api/tickets/4/transitions", Map.of("status", "CLOSED"), JsonNode.class).getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(jdbc.queryForObject("select count(*) from ticket_history where ticket_id = 4", Integer.class)).isEqualTo(before);
    }
}
