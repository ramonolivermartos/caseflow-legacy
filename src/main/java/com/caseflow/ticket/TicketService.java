package com.caseflow.ticket;

import com.caseflow.common.ApiException;
import com.caseflow.operator.*;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import static org.springframework.http.HttpStatus.*;
import static com.caseflow.ticket.TicketDtos.*;

@Service
@Transactional(readOnly = true)
public class TicketService {
    private final TicketRepository tickets;
    private final OperatorRepository operators;
    private final TicketCommentRepository comments;
    private final TicketHistoryRepository history;
    private final TicketMapper mapper;

    public TicketService(TicketRepository tickets, OperatorRepository operators, TicketCommentRepository comments,
                         TicketHistoryRepository history, TicketMapper mapper) {
        this.tickets = tickets; this.operators = operators; this.comments = comments; this.history = history; this.mapper = mapper;
    }
    public PageResult<Summary> search(TicketStatus status, Priority priority, Long assigneeId, String q, int page, int size) {
        var result = tickets.findAll(TicketSearch.matching(status, priority, assigneeId, q),
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt", "id")));
        return new PageResult<>(result.getContent().stream().map(mapper::summary).toList(), page, size, result.getTotalElements(), result.getTotalPages());
    }
    public Detail get(long id) { return detail(ticket(id)); }

    @Transactional
    public Detail create(CreateTicket request) {
        var t = tickets.save(new Ticket(request.title().trim(), request.description().trim(), request.priority()));
        record(t, "CREATED", "Ticket registered");
        return detail(t);
    }
    @Transactional
    public Detail update(long id, UpdateTicket request) {
        var t = ticket(id);
        ensureOpen(t);
        t.update(request.title(), request.description(), request.priority());
        record(t, "UPDATED", "Ticket content updated");
        tickets.flush();
        return detail(t);
    }
    @Transactional
    public Detail assign(long id, Assignment request) {
        var t = ticket(id);
        ensureOpen(t);
        var operator = activeOperator(request.operatorId());
        t.assign(operator);
        record(t, "ASSIGNED", "Assigned to operator " + operator.getId());
        tickets.flush();
        return detail(t);
    }
    @Transactional
    public Detail transition(long id, Transition request) {
        var t = ticket(id);
        var from = t.getStatus();
        var to = request.status();
        boolean allowed = switch (from) {
            case NEW -> to == TicketStatus.IN_PROGRESS;
            case IN_PROGRESS -> to == TicketStatus.WAITING || to == TicketStatus.RESOLVED;
            case WAITING -> to == TicketStatus.IN_PROGRESS;
            case RESOLVED -> to == TicketStatus.IN_PROGRESS || to == TicketStatus.CLOSED;
            case CLOSED -> false;
        };
        if (!allowed) throw new ApiException(CONFLICT, "Transition from " + from + " to " + to + " is not allowed");
        if (to == TicketStatus.IN_PROGRESS && t.getAssignee() == null)
            throw new ApiException(CONFLICT, "Assign an operator before starting work");
        t.transitionTo(to);
        record(t, "STATUS_CHANGED", from + " -> " + to);
        tickets.flush();
        return detail(t);
    }
    @Transactional
    public CommentView addComment(long id, AddComment request) {
        var t = ticket(id);
        ensureOpen(t);
        var author = activeOperator(request.authorId());
        var c = comments.save(new TicketComment(t, author, request.body().trim()));
        t.touch();
        record(t, "COMMENT_ADDED", "Comment added by operator " + author.getId());
        return mapper.comment(c);
    }
    private Ticket ticket(long id) { return tickets.findById(id).orElseThrow(() -> new ApiException(NOT_FOUND, "Ticket not found")); }
    private Operator activeOperator(long id) {
        var operator = operators.findById(id).orElseThrow(() -> new ApiException(NOT_FOUND, "Operator not found"));
        if (!operator.isActive()) throw new ApiException(CONFLICT, "Operator is inactive");
        return operator;
    }
    private void ensureOpen(Ticket t) {
        if (t.getStatus() == TicketStatus.CLOSED) throw new ApiException(CONFLICT, "Closed tickets are read-only");
    }
    private void record(Ticket t, String type, String detail) { history.save(new TicketHistory(t, type, detail)); }
    private Detail detail(Ticket t) {
        return mapper.detail(t, comments.findByTicketIdOrderByCreatedAtAscIdAsc(t.getId()), history.findByTicketIdOrderByCreatedAtAscIdAsc(t.getId()));
    }
}
