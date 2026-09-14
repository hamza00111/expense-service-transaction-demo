package com.hamza.expenses.solution.outbox;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
@RequiredArgsConstructor
public class OutboxPublisher {
    private final OutboxRepository repository;
    private final OutboxDeliveryService deliveryService;
    private final KafkaTemplate<String, Object> kafka;

    @Scheduled(fixedDelayString = "${outbox.publish-delay:1000}")
    public void publishPending() {
        repository.findTop100ByPublishedAtIsNullOrderByCreatedAtAsc()
                .forEach(this::publish);
    }

    private void publish(OutboxEvent event) {
        try {
            kafka.send(
                    "expenses",
                    event.expenseId().toString(),
                    new ReliableExpenseApproved(event.id(), event.expenseId())
            ).get(10, TimeUnit.SECONDS);

            deliveryService.markPublished(event.id());
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException | TimeoutException publicationFailure) {
            // Leave the row unpublished. The next scheduled run retries it.
        }
    }
}
