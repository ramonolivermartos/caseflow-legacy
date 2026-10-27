package com.caseflow.ticket;

import java.util.List;
import org.springframework.stereotype.Component;
import static com.caseflow.ticket.TicketDtos.*;

@Component
public class TicketMapper {
    public Summary summary(Ticket t) {
        return new Summary(t.getId(), t.getTitle(), t.getStatus(), t.getPriority(), assigneeId(t), t.getCreatedAt(), t.getUpdatedAt());
    }
    public Detail detail(Ticket t, List<TicketComment> comments, List<TicketHistory> history) {
        return new Detail(t.getId(), t.getVersion(), t.getTitle(), t.getDescription(), t.getStatus(), t.getPriority(),
                assigneeId(t), t.getCreatedAt(), t.getUpdatedAt(), allowedTransitions(t),
                comments.stream().map(this::comment).toList(),
                history.stream().map(h -> new HistoryView(h.getId(), h.getEventType(), h.getDetail(), h.getCreatedAt())).toList());
    }
    public CommentView comment(TicketComment c) {
        return new CommentView(c.getId(), c.getAuthor().getId(), c.getBody(), c.getCreatedAt());
    }
    private Long assigneeId(Ticket t) { return t.getAssignee() == null ? null : t.getAssignee().getId(); }
    public List<TicketStatus> allowedTransitions(Ticket t) {
        return switch (t.getStatus()) {
            case NEW -> t.getAssignee() == null ? List.of() : List.of(TicketStatus.IN_PROGRESS);
            case IN_PROGRESS -> List.of(TicketStatus.WAITING, TicketStatus.RESOLVED);
            case WAITING -> List.of(TicketStatus.IN_PROGRESS);
            case RESOLVED -> List.of(TicketStatus.IN_PROGRESS, TicketStatus.CLOSED);
            case CLOSED -> List.of();
        };
    }
}
