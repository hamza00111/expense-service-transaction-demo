package com.hamza.expenses.pitfalls;

import com.hamza.expenses.domain.Expense;
import com.hamza.expenses.domain.ExpenseStatus;
import com.hamza.expenses.model.ExpenseApproved;
import com.hamza.expenses.model.SubmitExpense;
import com.hamza.expenses.port.FraudClient;
import com.hamza.expenses.repository.ExpenseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExpenseServiceTest {
    @Mock
    private ExpenseRepository repository;
    @Mock
    private FraudClient fraudClient;
    @Mock
    private KafkaTemplate<String, Object> kafka;

    private ExpenseService service;

    @BeforeEach
    void setUp() {
        service = new ExpenseService(repository, fraudClient, kafka);
        when(repository.save(any(Expense.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void approvesAndPublishesAnExpense() {
        SubmitExpense command = new SubmitExpense("req-1", new BigDecimal("42.50"));

        Expense result = service.submit(command);

        assertEquals(ExpenseStatus.APPROVED, result.status());
        verify(fraudClient).verify(result);
        verify(kafka).send(
                eq("expenses"),
                eq(result.id().toString()),
                any(ExpenseApproved.class)
        );
    }

    @Test
    void attemptsToRecordFailureAndRethrowsOriginalFailure() {
        SubmitExpense command = new SubmitExpense("req-2", new BigDecimal("99.00"));
        RuntimeException failure = new RuntimeException("fraud service unavailable");
        doThrow(failure).when(fraudClient).verify(any(Expense.class));

        RuntimeException thrown = assertThrows(
                RuntimeException.class,
                () -> service.submit(command)
        );

        assertSame(failure, thrown);
        verify(repository).saveAudit(any(), eq("fraud service unavailable"));
    }
}
