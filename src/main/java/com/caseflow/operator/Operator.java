package com.caseflow.operator;

import jakarta.persistence.*;

@Entity
@Table(name = "operators")
public class Operator {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 100)
    private String displayName;
    @Column(nullable = false)
    private boolean active;

    protected Operator() {}
    public Long getId() { return id; }
    public String getDisplayName() { return displayName; }
    public boolean isActive() { return active; }
}
