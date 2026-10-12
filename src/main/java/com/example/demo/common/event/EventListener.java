package com.example.demo.common.event;

/**
 * Base interface for event listeners
 *
 * @param <E> Event type
 *
 * @author Architecture Team
 * @version 1.0
 */
public interface EventListener<E extends DomainEvent> {

    /**
     * Handle the event
     *
     * @param event Domain event
     */
    void handle(E event);

    /**
     * Get event type this listener handles
     *
     * @return Event type
     */
    default String getEventType() {
        return "unknown";
    }

    /**
     * Check if listener should handle this event
     *
     * @param event Domain event
     * @return true if should handle
     */
    default boolean shouldHandle(E event) {
        return true;
    }
}