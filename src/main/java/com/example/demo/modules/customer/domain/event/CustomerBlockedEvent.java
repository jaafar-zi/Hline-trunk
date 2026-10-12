package com.example.demo.modules.customer.domain.event;

import java.util.UUID;

import lombok.Getter;

/**
 * Event published when a customer account is blocked by admin
 * 
 * Permanent or long-term block due to:
 * - Severe policy violation
 * - Fraud detected
 * - Legal requirements
 * 
 * Business Impact:
 * - Customer completely locked out
 * - All active orders cancelled
 * - Legal/compliance team notified
 * - Fraud detection system updated
 * 
 * @author Customer Domain Team
 */
@Getter
public class CustomerBlockedEvent extends CustomerEvent {
    
    private final String reason;

    public CustomerBlockedEvent(UUID customerId, Long version, String reason) {
        super(customerId, version);
        this.reason = reason;
    }

    public CustomerBlockedEvent(UUID customerId, Long version, String reason, String triggeredBy) {
        super(customerId, version, triggeredBy);
        this.reason = reason;
    }

    @Override
    public String getEventType() {
        return "customer.blocked";
    }

    @Override
    public boolean isAsync() {
        return false; // Sync for immediate enforcement
    }

    @Override
    public String toString() {
        return String.format(
            "CustomerBlockedEvent[customerId=%d, reason=%s, triggeredBy=%s, occurredAt=%s]",
            getCustomerId(), reason, getTriggeredBy(), getOccurredAt()
        );
    }
}