package com.hamza.expenses.client;

import com.hamza.expenses.domain.Expense;
import com.hamza.expenses.port.FraudClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class HttpFraudClient implements FraudClient {
    private final RestClient restClient;

    public HttpFraudClient(RestClient.Builder builder, @Value("${fraud.base-url}") String baseUrl) {
        this.restClient = builder.baseUrl(baseUrl).build();
    }

    @Override
    public void verify(Expense expense) {
        restClient.post()
                .uri("/verifications")
                .body(new FraudVerificationRequest(expense.id(), expense.amount()))
                .retrieve()
                .toBodilessEntity();
    }

    private record FraudVerificationRequest(UUID expenseId, BigDecimal amount) {
    }
}
