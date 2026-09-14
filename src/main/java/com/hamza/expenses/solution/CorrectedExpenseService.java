package com.hamza.expenses.solution;

import java.util.UUID;

import com.hamza.expenses.domain.Expense;
import com.hamza.expenses.domain.ExpenseStatus;
import com.hamza.expenses.model.SubmitExpense;
import com.hamza.expenses.port.FraudClient;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CorrectedExpenseService {
    private final ExpenseTransactions transactions;
    private final FailureRecorder failureRecorder;
    private final FraudClient fraudClient;

    public Expense submit(SubmitExpense command) {
        Expense expense = findOrCreateIdempotently(command);
        validateSameRequest(command, expense);

        if (expense.status() != ExpenseStatus.PENDING) {
            return expense;
        }

        if (!transactions.claimForVerification(expense.id())) {
            return transactions.findById(expense.id());
        }

        Expense claimedExpense = transactions.findById(expense.id());

        try {
            fraudClient.verify(claimedExpense);
            return transactions.approveAndEnqueue(claimedExpense.id());
        } catch (RuntimeException failure) {
            recordFailureWithoutMaskingOriginal(claimedExpense.id(), failure);
            throw failure;
        }
    }

    private Expense findOrCreateIdempotently(SubmitExpense command) {
        try {
            return transactions.findOrCreate(command);
        } catch (DataIntegrityViolationException concurrentDuplicate) {
            return transactions.findByRequestId(command.requestId());
        }
    }

    private void validateSameRequest(SubmitExpense command, Expense expense) {
        if (expense.amount().compareTo(command.amount()) != 0) {
            throw new IllegalArgumentException(
                    "requestId was already used with a different amount"
            );
        }
    }

    private void recordFailureWithoutMaskingOriginal(UUID expenseId, RuntimeException failure) {
        try {
            failureRecorder.record(expenseId, failure);
        } catch (RuntimeException auditFailure) {
            failure.addSuppressed(auditFailure);
        }
    }
}
