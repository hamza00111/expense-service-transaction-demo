package com.hamza.expenses.solution.outbox;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OutboxDeliveryService {
    private final OutboxRepository repository;

    @Transactional
    public void markPublished(UUID eventId) {
        OutboxEvent event = repository.findById(eventId)
                .orElseThrow(() -> new IllegalStateException("Outbox event not found"));
        event.markPublished();
    }
}
