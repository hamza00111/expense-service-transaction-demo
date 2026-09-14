package com.hamza.expenses.solution;

import com.hamza.expenses.domain.Expense;
import com.hamza.expenses.domain.ExpenseStatus;
import com.hamza.expenses.model.SubmitExpense;
import com.hamza.expenses.repository.ExpenseRepository;
import com.hamza.expenses.solution.outbox.OutboxEvent;
import com.hamza.expenses.solution.outbox.OutboxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ExpenseTransactions {
    private final ExpenseRepository expenseRepository;
    private final OutboxRepository outboxRepository;

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Expense findOrCreate(SubmitExpense command) {
        return expenseRepository.findByRequestId(command.requestId())
                .orElseGet(() -> expenseRepository.saveAndFlush(
                        Expense.pending(command.requestId(), command.amount())
                ));
    }

    @Transactional(readOnly = true)
    public Expense findByRequestId(String requestId) {
        return expenseRepository.findByRequestId(requestId)
                .orElseThrow(() -> new IllegalStateException(
                        "Concurrent submission committed no expense"
                ));
    }

    @Transactional(readOnly = true)
    public Expense findById(UUID expenseId) {
        return expenseRepository.findById(expenseId)
                .orElseThrow(() -> new IllegalStateException("Expense not found"));
    }

    @Transactional
    public boolean claimForVerification(UUID expenseId) {
        return expenseRepository.claimForVerification(
                expenseId,
                ExpenseStatus.PENDING,
                ExpenseStatus.VERIFYING
        ) == 1;
    }

    @Transactional
    public Expense approveAndEnqueue(UUID expenseId) {
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new IllegalStateException("Expense not found"));

        if (expense.status() == ExpenseStatus.APPROVED) {
            return expense;
        }

        expense.approveAfterVerification();
        outboxRepository.save(OutboxEvent.expenseApproved(expense.id()));
        return expense;
    }
}
