package com.hamza.expenses.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "expenses")
public class Expense {
    @Id
    private UUID id;

    @Column(name = "request_id", nullable = false, unique = true, updatable = false)
    private String requestId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExpenseStatus status;

    protected Expense() {
    }

    private Expense(UUID id, String requestId, BigDecimal amount, ExpenseStatus status) {
        this.id = Objects.requireNonNull(id);
        this.requestId = Objects.requireNonNull(requestId);
        this.amount = Objects.requireNonNull(amount);
        this.status = Objects.requireNonNull(status);
    }

    public static Expense pending(String requestId, BigDecimal amount) {
        if (requestId == null || requestId.isBlank()) {
            throw new IllegalArgumentException("requestId must not be blank");
        }
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("amount must be positive");
        }
        return new Expense(UUID.randomUUID(), requestId, amount, ExpenseStatus.PENDING);
    }

    public void approve() {
        if (status != ExpenseStatus.PENDING) {
            throw new IllegalStateException("Only a pending expense can be approved");
        }
        status = ExpenseStatus.APPROVED;
    }

    public void markVerifying() {
        if (status != ExpenseStatus.PENDING) {
            throw new IllegalStateException("Only a pending expense can be verified");
        }
        status = ExpenseStatus.VERIFYING;
    }

    public void approveAfterVerification() {
        if (status != ExpenseStatus.VERIFYING) {
            throw new IllegalStateException("Expense is not being verified");
        }
        status = ExpenseStatus.APPROVED;
    }

    public void markFraudCheckFailed() {
        if (status != ExpenseStatus.VERIFYING) {
            throw new IllegalStateException("Expense is not being verified");
        }
        status = ExpenseStatus.FRAUD_CHECK_FAILED;
    }

    public UUID id() { return id; }
    public String requestId() { return requestId; }
    public BigDecimal amount() { return amount; }
    public ExpenseStatus status() { return status; }
}
