package com.caseflow.ticket;

import com.caseflow.operator.Operator;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "tickets")
public class Ticket {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Version private long version;
    @Column(nullable = false, length = 140)
    private String title;
    @Column(nullable = false, length = 4000)
    private String description;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private TicketStatus status;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private Priority priority;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "assignee_id")
    private Operator assignee;
    @Column(nullable = false, updatable = false) private Instant createdAt;
    @Column(nullable = false) private Instant updatedAt;

    protected Ticket() {}
    public Ticket(String title, String description, Priority priority) {
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.status = TicketStatus.NEW;
        this.createdAt = Instant.now();
        this.updatedAt = createdAt;
    }
    public void update(String title, String description, Priority priority) {
        this.title = title;
        this.description = description;
        this.priority = priority;
        touch();
    }
    public void assign(Operator operator) { assignee = operator; touch(); }
    public void transitionTo(TicketStatus next) { status = next; touch(); }
    public void touch() { updatedAt = Instant.now(); }
    public Long getId() { return id; }
    public long getVersion() { return version; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public TicketStatus getStatus() { return status; }
    public Priority getPriority() { return priority; }
    public Operator getAssignee() { return assignee; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
