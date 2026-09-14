package com.hamza.expenses.port;

import com.hamza.expenses.domain.Expense;

public interface FraudClient {
    void verify(Expense expense);
}
