# Expense service transaction pitfalls

A Java 21 / Spring Boot 4.1.1 Maven project built around an intentionally problematic transaction boundary.

The service under study is in:

    com.hamza.expenses.pitfalls.ExpenseService

## Run

Requirements: JDK 21, Docker, and Maven 3.9+.

    docker compose up -d
    mvn spring-boot:run

Submit an expense:

    curl -X POST http://localhost:8080/expenses \
      -H "Content-Type: application/json" \
      -d '{"requestId":"req-1001","amount":125.50}'

The fraud service URL is externalized with FRAUD_BASE_URL.

## Intentional pitfalls

ExpenseService is deliberately not production-ready:

1. recordFailure is invoked from the same bean. Self-invocation bypasses the Spring proxy, so REQUIRES_NEW is not applied.
2. The database transaction remains open during the remote HTTP call.
3. The database write and Kafka publication are not atomic.
4. KafkaTemplate.send is asynchronous; later failures are not caught by the catch block.
5. failure.getMessage may be null, unstable, or sensitive.

Supporting packages make the example runnable without correcting those issues.

## Endpoints

- POST /expenses
- GET /expenses/{id}

H2 console: /h2-console using jdbc:h2:mem:expenses.

## Test

    mvn test
