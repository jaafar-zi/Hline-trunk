package com.example.demo.common.base.entity;

import com.example.demo.common.event.DomainEvent;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PostLoad;
import jakarta.persistence.Transient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Base Aggregate Root with ONLY event management responsibility
 *
 * Does NOT extend anything - pure event container
 * Subclasses decide if they need auditing, soft delete, etc.
 *
 * @author Architecture Team
 * @version 1.0
 */
@MappedSuperclass
public abstract class AbstractAggregateRoot extends BaseEntity implements AggregateRoot {

    @Transient
    private final List<DomainEvent> domainEvents = new ArrayList<>();

    @Transient
    private boolean eventsCleared = false;

    @Override
    public List<DomainEvent> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    @Override
    public void clearDomainEvents() {
        domainEvents.clear();
        eventsCleared = true;
    }

    @Override
    public void registerEvent(DomainEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("Domain event cannot be null");
        }

        if (eventsCleared) {
            throw new IllegalStateException(
                    "Cannot register events after clearDomainEvents() has been called. " +
                            "This aggregate's events may have already been published."
            );
        }

        domainEvents.add(event);
    }

    /**
     * Reset event state after loading from database
     * Allows new events on reloaded aggregates
     */
    @PostLoad
    protected void resetEventState() {
        domainEvents.clear(); // Clear any stale transient state
        eventsCleared = false;
    }

    /**
     * Fluent API for event registration
     *
     * Usage: return this.andRegisterEvent(new OrderCreatedEvent(...));
     */
    @SuppressWarnings("unchecked")
    protected <T extends AbstractAggregateRoot> T andRegisterEvent(DomainEvent event) {
        registerEvent(event);
        return (T) this;
    }

    /**
     * Check if aggregate has unpublished events
     */
    @Transient
    public boolean hasPendingEvents() {
        return !domainEvents.isEmpty();
    }

    /**
     * Get aggregate type name for event metadata
     */
    @Transient
    public String getAggregateType() {
        return this.getClass().getSimpleName();
    }

    /**
     * Get aggregate version for event sourcing compatibility
     * Delegates to BaseEntity version field
     */
    @Transient
    public Long getAggregateVersion() {
        return this.getVersion();
    }
}