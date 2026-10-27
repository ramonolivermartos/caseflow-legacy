package com.caseflow.ticket;

import java.util.ArrayList;
import java.util.Locale;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

final class TicketSearch {
    private TicketSearch() {}
    static Specification<Ticket> matching(TicketStatus status, Priority priority, Long assigneeId, String q) {
        return (root, query, cb) -> {
            var filters = new ArrayList<Predicate>();
            if (status != null) filters.add(cb.equal(root.get("status"), status));
            if (assigneeId != null) {
                filters.add(cb.equal(root.get("assignee").get("id"), assigneeId));
            } else if (priority != null) {
                filters.add(cb.equal(root.get("priority"), priority));
            }
            if (q != null && !q.isBlank()) {
                String term = q.trim().toLowerCase(Locale.ROOT).replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
                filters.add(cb.like(cb.lower(root.get("title")), "%" + term + "%", '\\'));
            }
            return cb.and(filters.toArray(Predicate[]::new));
        };
    }
}
