package com.example.demo.modules.customer.domain.event;

import java.util.UUID;

import lombok.Getter;

/**
 * Event published when an address is removed from customer
 * 
 * Business Impact:
 * - Active orders using this address may need attention
 * - Customer notification sent
 * - Cannot be preferred address anymore
 * 
 * @author Customer Domain Team
 */
@Getter
public class AddressRemovedEvent extends CustomerEvent {
    
    private final Long addressId;

    public AddressRemovedEvent(UUID customerId, Long version, Long addressId) {
        super(customerId, version);
        this.addressId = addressId;
    }

    @Override
    public String getEventType() {
        return "customer.address.removed";
    }

    @Override
    public String toString() {
        return String.format(
            "AddressRemovedEvent[customerId=%d, addressId=%d, occurredAt=%s]",
            getCustomerId(), addressId, getOccurredAt()
        );
    }
}