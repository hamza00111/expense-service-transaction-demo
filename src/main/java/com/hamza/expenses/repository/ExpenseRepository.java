package com.hamza.expenses.repository;

import com.hamza.expenses.domain.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface ExpenseRepository extends JpaRepository<Expense, UUID> {
    @Modifying
    @Query(value = """
            insert into expense_audits (expense_id, reason, created_at)
            values (:expenseId, :reason, current_timestamp)
            """, nativeQuery = true)
    void saveAudit(@Param("expenseId") UUID expenseId, @Param("reason") String reason);
}
