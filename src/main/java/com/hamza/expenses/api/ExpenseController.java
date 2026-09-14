package com.hamza.expenses.api;

import com.hamza.expenses.domain.Expense;
import com.hamza.expenses.domain.ExpenseStatus;
import com.hamza.expenses.model.SubmitExpense;
import com.hamza.expenses.repository.ExpenseRepository;
import com.hamza.expenses.solution.CorrectedExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/expenses")
@RequiredArgsConstructor
public class ExpenseController {
    private final CorrectedExpenseService service;
    private final ExpenseRepository repository;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ExpenseResponse submit(@Valid @RequestBody SubmitExpense command) {
        return ExpenseResponse.from(service.submit(command));
    }

    @GetMapping("/{id}")
    public ExpenseResponse findById(@PathVariable UUID id) {
        return repository.findById(id)
                .map(ExpenseResponse::from)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    public record ExpenseResponse(UUID id, String requestId, BigDecimal amount, ExpenseStatus status) {
        static ExpenseResponse from(Expense expense) {
            return new ExpenseResponse(
                    expense.id(), expense.requestId(), expense.amount(), expense.status()
            );
        }
    }
}
