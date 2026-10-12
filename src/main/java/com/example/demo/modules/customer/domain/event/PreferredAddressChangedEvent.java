package com.example.demo.modules.customer.domain.event;

import java.util.UUID;

import lombok.Getter;

/**
 * Event published when customer's preferred address changes
 * 
 * Business Impact:
 * - Default shipping address updated
 * - Order placement flow updated
 * - Customer profile cache invalidated
 * 
 * @author Customer Domain Team
 */
@Getter
public class PreferredAddressChangedEvent extends CustomerEvent {
    
    private final Long newPreferredAddressId;

    public PreferredAddressChangedEvent(
            UUID customerId,
            Long version,
            Long newPreferredAddressId) {
        super(customerId, version);
        this.newPreferredAddressId = newPreferredAddressId;
    }

    @Override
    public String getEventType() {
        return "customer.address.preferred.changed";
    }

    @Override
    public String toString() {
        return String.format(
            "PreferredAddressChangedEvent[customerId=%d, newPreferredAddressId=%d, occurredAt=%s]",
            getCustomerId(), newPreferredAddressId, getOccurredAt()
        );
    }
}