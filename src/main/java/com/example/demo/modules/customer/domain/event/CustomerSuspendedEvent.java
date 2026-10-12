package com.example.demo.modules.customer.domain.event;

import java.util.UUID;

import lombok.Getter;

/**
 * Event published when a customer account is suspended by admin
 * 
 * Temporary suspension due to:
 * - Policy violation
 * - Payment issues
 * - Fraud investigation
 * 
 * Business Impact:
 * - Customer cannot place orders
 * - Customer service notified
 * - Active orders may be held
 * - Email notification sent
 * 
 * @author Customer Domain Team
 */
@Getter
public class CustomerSuspendedEvent extends CustomerEvent {
    
    private final String reason;

    public CustomerSuspendedEvent(UUID customerId, Long version, String reason) {
        super(customerId, version);
        this.reason = reason;
    }

    public CustomerSuspendedEvent(UUID customerId, Long version, String reason, String triggeredBy) {
        super(customerId, version, triggeredBy);
        this.reason = reason;
    }

    @Override
    public String getEventType() {
        return "customer.suspended";
    }

    @Override
    public boolean isAsync() {
        return false; // Sync for immediate action
    }

    @Override
    public String toString() {
        return String.format(
            "CustomerSuspendedEvent[customerId=%d, reason=%s, triggeredBy=%s, occurredAt=%s]",
            getCustomerId(), reason, getTriggeredBy(), getOccurredAt()
        );
    }
}