package com.hamza.expenses.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "expense_audits")
public class ExpenseAudit {
    @Id
    @Column(name = "expense_id")
    private UUID expenseId;

    @Column(nullable = false)
    private String reason;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected ExpenseAudit() {
    }
}
