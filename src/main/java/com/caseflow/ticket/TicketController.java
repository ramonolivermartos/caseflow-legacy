package com.caseflow.ticket;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import static com.caseflow.ticket.TicketDtos.*;

@Validated
@RestController
@RequestMapping("/api/tickets")
public class TicketController {
    private final TicketService service;
    public TicketController(TicketService service) { this.service = service; }
    @GetMapping
    public PageResult<Summary> search(@RequestParam(required = false) TicketStatus status,
            @RequestParam(required = false) Priority priority, @RequestParam(required = false) @Positive Long assigneeId,
            @RequestParam(required = false) @Size(max = 140) String q,
            @RequestParam(defaultValue = "0") @Min(0) int page, @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return service.search(status, priority, assigneeId, q, page, size);
    }
    @GetMapping("/{id}")
    public Detail get(@PathVariable @Positive long id) { return service.get(id); }
    @PostMapping
    public ResponseEntity<Detail> create(@RequestBody @Valid CreateTicket request) {
        var result = service.create(request);
        return ResponseEntity.created(URI.create("/api/tickets/" + result.id())).body(result);
    }
    @PatchMapping("/{id}")
    public Detail update(@PathVariable @Positive long id, @RequestBody @Valid UpdateTicket request) { return service.update(id, request); }
    @PutMapping("/{id}/assignee")
    public Detail assign(@PathVariable @Positive long id, @RequestBody @Valid Assignment request) { return service.assign(id, request); }
    @PostMapping("/{id}/transitions")
    public Detail transition(@PathVariable @Positive long id, @RequestBody @Valid Transition request) { return service.transition(id, request); }
    @PostMapping("/{id}/comments")
    public ResponseEntity<CommentView> comment(@PathVariable @Positive long id, @RequestBody @Valid AddComment request) {
        return ResponseEntity.status(201).body(service.addComment(id, request));
    }
}
