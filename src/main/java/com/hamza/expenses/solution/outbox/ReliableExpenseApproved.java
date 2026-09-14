package com.hamza.expenses.solution.outbox;

import java.util.UUID;

public record ReliableExpenseApproved(UUID eventId, UUID expenseId) {
}
