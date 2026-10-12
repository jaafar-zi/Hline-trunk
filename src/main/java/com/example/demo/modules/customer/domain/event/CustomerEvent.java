package com.example.demo.modules.customer.domain.event;

import com.example.demo.common.event.DomainEvent;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Base class for all Customer domain events
 * 
 * Provides common functionality for customer-related events
 * 
 * @author Customer Domain Team
 */
@Getter
public abstract class CustomerEvent implements DomainEvent {
    
    private final String eventId;
    private final UUID aggregateId;
    private final Long aggregateVersion;
    private final LocalDateTime occurredAt;
    private final String triggeredBy;

    protected CustomerEvent(UUID aggregateId, Long aggregateVersion) {
        this.eventId = UUID.randomUUID().toString();
        this.aggregateId = aggregateId;
        this.aggregateVersion = aggregateVersion;
        this.occurredAt = LocalDateTime.now();
        this.triggeredBy = "system"; // Can be overridden
    }

    protected CustomerEvent(UUID aggregateId, Long aggregateVersion, String triggeredBy) {
        this.eventId = UUID.randomUUID().toString();
        this.aggregateId = aggregateId;
        this.aggregateVersion = aggregateVersion;
        this.occurredAt = LocalDateTime.now();
        this.triggeredBy = triggeredBy;
    }

    @Override
    public String getSource() {
        return "customer-domain";
    }

    @Override
    public String getVersion() {
        return "1.0";
    }

    /**
     * Get the customer ID (aggregate ID)
     */
    public UUID getCustomerId() {
        return aggregateId;
    }
}