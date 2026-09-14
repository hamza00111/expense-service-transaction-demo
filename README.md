# Expense service transaction pitfalls

A Java 21 / Spring Boot 4.1.1 Maven project containing the original flawed implementation and a corrected implementation.

## Compare the implementations

Original code, deliberately unchanged:

    com.hamza.expenses.pitfalls.ExpenseService

Corrected orchestration:

    com.hamza.expenses.solution.CorrectedExpenseService

The REST controller uses the corrected implementation. The original service remains available for study and for its unit tests.

## Run

Requirements: JDK 21, Docker, and Maven 3.9+.

    docker compose up -d
    mvn spring-boot:run

Submit an expense:

    curl -X POST http://localhost:8080/expenses \
      -H "Content-Type: application/json" \
      -d '{"requestId":"req-1001","amount":125.50}'

The fraud service URL is externalized with FRAUD_BASE_URL.

## The five original pitfalls

1. Self-invocation prevents REQUIRES_NEW from being applied.
2. The remote HTTP call keeps the database transaction open.
3. The database write and Kafka publication are not atomic.
4. KafkaTemplate.send is asynchronous and later failures escape the catch block.
5. A retry with the same requestId is not handled as an idempotent submission.

## Corrected flow

The solution package:

- uses short transactions in ExpenseTransactions;
- performs the fraud HTTP call outside a database transaction;
- records failures through a separate proxied FailureRecorder;
- writes approval and an outbox row in the same transaction;
- publishes pending outbox events and waits for Kafka's asynchronous result;
- gives every reliable event an eventId for consumer deduplication;
- uses requestId plus a conditional state transition to prevent duplicate processing.

The outbox is intentionally at-least-once. A crash after Kafka accepts an event but before publishedAt is stored may produce a duplicate, so consumers must deduplicate by eventId.

## Endpoints

- POST /expenses
- GET /expenses/{id}

H2 console: /h2-console using jdbc:h2:mem:expenses.

## Test

    mvn test
