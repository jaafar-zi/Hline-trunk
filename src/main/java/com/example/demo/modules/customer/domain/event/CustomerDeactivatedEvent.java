package com.example.demo.modules.customer.domain.event;

import java.util.UUID;

import lombok.Getter;

/**
 * Event published when a customer voluntarily deactivates their account
 * 
 * Customer-initiated action (not admin)
 * 
 * Business Impact:
 * - Customer cannot place new orders
 * - Scheduled orders may be cancelled
 * - Retention campaign may be triggered
 * - Analytics tracking for churn analysis
 * 
 * @author Customer Domain Team
 */
@Getter
public class CustomerDeactivatedEvent extends CustomerEvent {

    public CustomerDeactivatedEvent(UUID customerId, Long version) {
        super(customerId, version);
    }

    public CustomerDeactivatedEvent(UUID customerId, Long version, String triggeredBy) {
        super(customerId, version, triggeredBy);
    }

    @Override
    public String getEventType() {
        return "customer.deactivated";
    }

    @Override
    public String toString() {
        return String.format(
            "CustomerDeactivatedEvent[customerId=%d, triggeredBy=%s, occurredAt=%s]",
            getCustomerId(), getTriggeredBy(), getOccurredAt()
        );
    }
}