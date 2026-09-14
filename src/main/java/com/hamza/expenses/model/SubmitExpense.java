package com.hamza.expenses.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record SubmitExpense(
        @NotBlank String requestId,
        @NotNull @Positive BigDecimal amount
) {
}
