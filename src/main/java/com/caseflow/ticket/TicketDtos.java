package com.caseflow.ticket;

import jakarta.validation.constraints.*;
import java.time.Instant;
import java.util.List;

public final class TicketDtos {
    private TicketDtos() {}
    public record CreateTicket(@NotBlank @Size(min = 3, max = 140) String title,
                               @NotBlank @Size(max = 4000) String description,
                               @NotNull Priority priority) {}
    public record UpdateTicket(@NotNull @Size(min = 3, max = 140) String title,
                               @NotBlank @Size(max = 4000) String description,
                               @NotNull Priority priority) {}
    public record Assignment(@NotNull @Positive Long operatorId) {}
    public record Transition(@NotNull TicketStatus status) {}
    public record AddComment(@NotNull @Positive Long authorId,
                             @NotBlank @Size(max = 2000) String body) {}
    public record Summary(Long id, String title, TicketStatus status, Priority priority,
                          Long assigneeId, Instant createdAt, Instant updatedAt) {}
    public record CommentView(Long id, Long authorId, String body, Instant createdAt) {}
    public record HistoryView(Long id, String eventType, String detail, Instant createdAt) {}
    public record Detail(Long id, long version, String title, String description, TicketStatus status,
                         Priority priority, Long assigneeId, Instant createdAt, Instant updatedAt,
                         List<TicketStatus> allowedTransitions, List<CommentView> comments,
                         List<HistoryView> history) {}
    public record PageResult<T>(List<T> content, int page, int size, long totalElements, int totalPages) {}
}
