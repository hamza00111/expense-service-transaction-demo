package com.hamza.expenses.repository;

import com.hamza.expenses.domain.Expense;
import com.hamza.expenses.domain.ExpenseStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ExpenseRepository extends JpaRepository<Expense, UUID> {
    Optional<Expense> findByRequestId(String requestId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update Expense expense
               set expense.status = :verifying
             where expense.id = :expenseId
               and expense.status = :pending
            """)
    int claimForVerification(
            @Param("expenseId") UUID expenseId,
            @Param("pending") ExpenseStatus pending,
            @Param("verifying") ExpenseStatus verifying
    );

    @Modifying
    @Query(value = """
            insert into expense_audits (expense_id, reason, created_at)
            values (:expenseId, :reason, current_timestamp)
            """, nativeQuery = true)
    void saveAudit(@Param("expenseId") UUID expenseId, @Param("reason") String reason);
}
