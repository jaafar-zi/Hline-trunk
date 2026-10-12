package com.example.demo.modules.customer.domain.event;

import java.util.UUID;

import lombok.Getter;

/**
 * Event published when a customer account is activated
 * 
 * Typically occurs when:
 * - Reactivating an inactive account
 * - Unsuspending an account
 * 
 * Business Impact:
 * - Customer can place orders again
 * - Notification sent to customer
 * - Access restored to customer portal
 * 
 * @author Customer Domain Team
 */
@Getter
public class CustomerActivatedEvent extends CustomerEvent {

    public CustomerActivatedEvent(UUID customerId, Long version) {
        super(customerId, version);
    }

    public CustomerActivatedEvent(UUID customerId, Long version, String triggeredBy) {
        super(customerId, version, triggeredBy);
    }

    @Override
    public String getEventType() {
        return "customer.activated";
    }

    @Override
    public String toString() {
        return String.format(
            "CustomerActivatedEvent[customerId=%d, triggeredBy=%s, occurredAt=%s]",
            getCustomerId(), getTriggeredBy(), getOccurredAt()
        );
    }
}