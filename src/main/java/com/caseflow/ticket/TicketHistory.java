package com.caseflow.ticket;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "ticket_history")
public class TicketHistory {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "ticket_id") private Ticket ticket;
    @Column(nullable = false, length = 40) private String eventType;
    @Column(nullable = false, length = 300) private String detail;
    @Column(nullable = false) private Instant createdAt;
    protected TicketHistory() {}
    public TicketHistory(Ticket ticket, String eventType, String detail) {
        this.ticket = ticket; this.eventType = eventType; this.detail = detail; this.createdAt = Instant.now();
    }
    public Long getId() { return id; }
    public String getEventType() { return eventType; }
    public String getDetail() { return detail; }
    public Instant getCreatedAt() { return createdAt; }
}
