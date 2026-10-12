package com.example.demo.common.event;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Base interface for all domain events
 *
 * Domain events represent something that happened in the domain
 * that domain experts care about.
 *
 * Features:
 * - Event identification
 * - Timestamp tracking
 * - Event metadata support
 * - Serializable for messaging
 *
 * @author Architecture Team
 * @version 1.0
 */
public interface DomainEvent extends Serializable {

    /**
     * Get unique event ID
     * Used for idempotency and event deduplication
     *
     * @return Event ID
     */
    String getEventId();

    /**
     * Get event type
     * Used for event routing and filtering
     *
     * @return Event type identifier
     */
    String getEventType();

    /**
     * Get timestamp when event occurred
     *
     * @return Event timestamp
     */
    LocalDateTime getOccurredAt();

    /**
     * Get event source
     * Identifies where the event originated
     *
     * @return Source identifier (e.g., service name, module name)
     */
    default String getSource() {
        return "demo-app";
    }

    /**
     * Get event version
     * Supports event schema evolution
     *
     * @return Event version
     */
    default String getVersion() {
        return "1.0";
    }

    /**
     * Get correlation ID
     * Links events that are part of the same business transaction
     *
     * @return Correlation ID
     */
    default String getCorrelationId() {
        return null;
    }

    /**
     * Get causation ID
     * Links events in a cause-effect chain
     *
     * @return Causation ID
     */
    default String getCausationId() {
        return null;
    }

    /**
     * Get user who triggered the event
     *
     * @return User identifier
     */
    default String getTriggeredBy() {
        return "system";
    }

    /**
     * Check if event should be published asynchronously
     *
     * @return true if async, false if sync
     */
    default boolean isAsync() {
        return true;
    }

    /**
     * Get event metadata
     *
     * @return Event metadata
     */
    default EventMetadata getMetadata() {
        return EventMetadata.builder()
                .eventId(getEventId())
                .eventType(getEventType())
                .source(getSource())
                .version(getVersion())
                .occurredAt(getOccurredAt())
                .correlationId(getCorrelationId())
                .causationId(getCausationId())
                .triggeredBy(getTriggeredBy())
                .build();
    }
}