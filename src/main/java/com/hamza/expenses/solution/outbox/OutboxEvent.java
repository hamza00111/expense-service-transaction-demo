package com.hamza.expenses.solution.outbox;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "outbox_events")
public class OutboxEvent {
    @Id
    private UUID id;

    @Column(name = "expense_id", nullable = false)
    private UUID expenseId;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "published_at")
    private Instant publishedAt;

    protected OutboxEvent() {
    }

    private OutboxEvent(UUID id, UUID expenseId, String eventType, Instant createdAt) {
        this.id = id;
        this.expenseId = expenseId;
        this.eventType = eventType;
        this.createdAt = createdAt;
    }

    public static OutboxEvent expenseApproved(UUID expenseId) {
        return new OutboxEvent(
                UUID.randomUUID(),
                expenseId,
                "ExpenseApproved",
                Instant.now()
        );
    }

    public void markPublished() {
        if (publishedAt == null) {
            publishedAt = Instant.now();
        }
    }

    public UUID id() { return id; }
    public UUID expenseId() { return expenseId; }
    public String eventType() { return eventType; }
}
