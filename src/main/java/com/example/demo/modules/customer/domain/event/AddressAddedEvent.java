package com.example.demo.modules.customer.domain.event;

import java.util.UUID;

import com.example.demo.modules.customer.domain.model.vo.AddressType;

import lombok.Getter;

/**
 * Event published when a new address is added to customer
 * 
 * Business Impact:
 * - Shipping calculations may change
 * - Tax calculations may change
 * - Address validation service called
 * - Customer notification sent
 * 
 * @author Customer Domain Team
 */
@Getter
public class AddressAddedEvent extends CustomerEvent {
    
    private final Long addressId;
    private final AddressType addressType;

    public AddressAddedEvent(
            UUID customerId,
            Long version,
            Long addressId,
            AddressType addressType) {
        super(customerId, version);
        this.addressId = addressId;
        this.addressType = addressType;
    }

    @Override
    public String getEventType() {
        return "customer.address.added";
    }

    @Override
    public String toString() {
        return String.format(
            "AddressAddedEvent[customerId=%d, addressId=%d, type=%s, occurredAt=%s]",
            getCustomerId(), addressId, addressType, getOccurredAt()
        );
    }
}