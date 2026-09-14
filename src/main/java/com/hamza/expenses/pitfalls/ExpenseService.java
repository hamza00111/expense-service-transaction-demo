package com.hamza.expenses.pitfalls;

import com.hamza.expenses.domain.Expense;
import com.hamza.expenses.model.ExpenseApproved;
import com.hamza.expenses.model.SubmitExpense;
import com.hamza.expenses.port.FraudClient;
import com.hamza.expenses.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ExpenseService {
    private final ExpenseRepository repository;
    private final FraudClient fraudClient;
    private final KafkaTemplate<String, Object> kafka;

    @Transactional
    public Expense submit(SubmitExpense command) {
        Expense expense = repository.save(
                Expense.pending(
                        command.requestId(),
                        command.amount()
                )
        );

        try {
            fraudClient.verify(expense); // Remote HTTP call
            expense.approve();

            kafka.send(
                    "expenses",
                    expense.id().toString(),
                    new ExpenseApproved(expense.id())
            );

            return expense;
        } catch (Exception failure) {
            recordFailure(expense.id(), failure.getMessage());
            throw failure;
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordFailure(UUID expenseId, String reason) {
        repository.saveAudit(expenseId, reason);
    }
}
