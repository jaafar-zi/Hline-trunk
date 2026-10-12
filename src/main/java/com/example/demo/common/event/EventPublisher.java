package com.example.demo.common.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * Central event publisher for domain events
 *
 * Features:
 * - Synchronous local event publishing (Spring Events)
 * - Asynchronous distributed event publishing (Kafka)
 * - Automatic event routing
 * - Error handling and logging
 *
 * @author Architecture Team
 * @version 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;
    private final KafkaTemplate<String, DomainEvent> kafkaTemplate;

    private static final String TOPIC_PREFIX = "domain-events";

    /**
     * Publish event locally (synchronous)
     * Used for in-process event handling
     *
     * @param event Domain event to publish
     */
    public void publishLocal(DomainEvent event) {
        try {
            log.debug("Publishing local event: {}", event.getEventType());
            applicationEventPublisher.publishEvent(event);
            log.trace("Local event published successfully: {}", event.getEventId());
        } catch (Exception e) {
            log.error("Failed to publish local event: {}", event.getEventType(), e);
            throw new EventPublishException("Failed to publish local event", e);
        }
    }

    /**
     * Publish event to Kafka (asynchronous)
     * Used for distributed event handling
     *
     * @param event Domain event to publish
     */
    @Async("eventExecutor")
    public void publishDistributed(DomainEvent event) {
        try {
            String topic = buildTopicName(event);
            String key = event.getEventId();

            log.debug("Publishing distributed event to topic '{}': {}", topic, event.getEventType());

            kafkaTemplate.send(topic, key, event)
                    .whenComplete((result, ex) -> {
                        if (ex == null) {
                            log.trace("Distributed event published successfully: {} to topic: {}",
                                    event.getEventId(), topic);
                        } else {
                            log.error("Failed to publish distributed event: {} to topic: {}",
                                    event.getEventType(), topic, ex);
                        }
                    });

        } catch (Exception e) {
            log.error("Failed to publish distributed event: {}", event.getEventType(), e);
            // Don't throw exception to avoid breaking the application
        }
    }

    /**
     * Publish event both locally and distributed
     *
     * @param event Domain event to publish
     */
    public void publish(DomainEvent event) {
        log.debug("Publishing event: {} (async={})", event.getEventType(), event.isAsync());

        // Always publish locally first
        publishLocal(event);

        // Publish to Kafka if event is async
        if (event.isAsync()) {
            publishDistributed(event);
        }
    }

    /**
     * Publish event and return CompletableFuture for async handling
     *
     * @param event Domain event to publish
     * @return CompletableFuture that completes when event is published
     */
    @Async("eventExecutor")
    public CompletableFuture<Void> publishAsync(DomainEvent event) {
        return CompletableFuture.runAsync(() -> {
            publish(event);
        });
    }

    /**
     * Publish multiple events
     *
     * @param events Domain events to publish
     */
    public void publishAll(DomainEvent... events) {
        if (events == null || events.length == 0) {
            return;
        }

        log.debug("Publishing {} events", events.length);

        for (DomainEvent event : events) {
            try {
                publish(event);
            } catch (Exception e) {
                log.error("Failed to publish event: {}", event.getEventType(), e);
                // Continue with other events
            }
        }
    }

    /**
     * Build topic name from event type
     *
     * @param event Domain event
     * @return Kafka topic name
     */
    private String buildTopicName(DomainEvent event) {
        String eventType = event.getEventType();

        // Extract entity type from event type
        // Example: "entity.created.user" -> "user-events"
        String[] parts = eventType.split("\\.");
        if (parts.length >= 3) {
            String entityType = parts[2];
            return entityType + "-events";
        }

        // Fallback to generic topic
        return TOPIC_PREFIX;
    }

    /**
     * Publish event with custom topic
     *
     * @param event Domain event
     * @param topic Kafka topic name
     */
    public void publishToTopic(DomainEvent event, String topic) {
        try {
            log.debug("Publishing event to custom topic '{}': {}", topic, event.getEventType());

            kafkaTemplate.send(topic, event.getEventId(), event)
                    .whenComplete((result, ex) -> {
                        if (ex == null) {
                            log.trace("Event published to topic '{}': {}", topic, event.getEventId());
                        } else {
                            log.error("Failed to publish event to topic '{}': {}", topic, event.getEventType(), ex);
                        }
                    });

        } catch (Exception e) {
            log.error("Failed to publish event to topic '{}': {}", topic, event.getEventType(), e);
        }
    }

    /**
     * Custom exception for event publishing failures
     */
    public static class EventPublishException extends RuntimeException {
        public EventPublishException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}