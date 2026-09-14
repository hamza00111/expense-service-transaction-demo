package com.hamza.expenses.solution;

import com.hamza.expenses.domain.Expense;
import com.hamza.expenses.domain.ExpenseStatus;
import com.hamza.expenses.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FailureRecorder {
    private final ExpenseRepository repository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(UUID expenseId, RuntimeException failure) {
        Expense expense = repository.findById(expenseId)
                .orElseThrow(() -> new IllegalStateException("Expense not found"));

        if (expense.status() == ExpenseStatus.VERIFYING) {
            expense.markFraudCheckFailed();
        }

        repository.saveAudit(expenseId, failure.getClass().getSimpleName());
    }
}
