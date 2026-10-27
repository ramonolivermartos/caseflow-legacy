package com.caseflow.ticket;

import com.caseflow.operator.Operator;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "ticket_comments")
public class TicketComment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "ticket_id") private Ticket ticket;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "author_id") private Operator author;
    @Column(nullable = false, length = 2000) private String body;
    @Column(nullable = false) private Instant createdAt;
    protected TicketComment() {}
    public TicketComment(Ticket ticket, Operator author, String body) {
        this.ticket = ticket; this.author = author; this.body = body; this.createdAt = Instant.now();
    }
    public Long getId() { return id; }
    public Operator getAuthor() { return author; }
    public String getBody() { return body; }
    public Instant getCreatedAt() { return createdAt; }
}
