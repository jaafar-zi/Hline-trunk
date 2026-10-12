package com.example.demo.modules.customer.domain.event;

import java.util.UUID;

import lombok.Getter;

/**
 * Event published when an existing address is updated
 * 
 * Business Impact:
 * - Active orders using this address notified
 * - Shipping calculations recalculated
 * - Tax calculations recalculated
 * - Address validation service called
 * 
 * @author Customer Domain Team
 */
@Getter
public class AddressUpdatedEvent extends CustomerEvent {
    
    private final Long addressId;

    public AddressUpdatedEvent(UUID customerId, Long version, Long addressId) {
        super(customerId, version);
        this.addressId = addressId;
    }

    @Override
    public String getEventType() {
        return "customer.address.updated";
    }

    @Override
    public String toString() {
        return String.format(
            "AddressUpdatedEvent[customerId=%d, addressId=%d, occurredAt=%s]",
            getCustomerId(), addressId, getOccurredAt()
        );
    }
}