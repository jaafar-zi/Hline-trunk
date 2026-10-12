package com.example.demo.modules.customer.domain.event;

import java.util.UUID;

import lombok.Getter;

/**
 * Event published when customer profile information is updated
 * 
 * Includes changes to:
 * - Name
 * - Phone number
 * - Preferences
 * 
 * Business Impact:
 * - Profile sync with external systems
 * - Search index updated
 * - CRM system notified
 * 
 * @author Customer Domain Team
 */
@Getter
public class CustomerProfileUpdatedEvent extends CustomerEvent {
    
    private final String fullName;
    private final String phone;

    public CustomerProfileUpdatedEvent(
            UUID customerId,
            Long version,
            String fullName,
            String phone) {
        super(customerId, version);
        this.fullName = fullName;
        this.phone = phone;
    }

    public CustomerProfileUpdatedEvent(
            UUID customerId,
            Long version,
            String fullName,
            String phone,
            String triggeredBy) {
        super(customerId, version, triggeredBy);
        this.fullName = fullName;
        this.phone = phone;
    }

    @Override
    public String getEventType() {
        return "customer.profile.updated";
    }

    @Override
    public String toString() {
        return String.format(
            "CustomerProfileUpdatedEvent[customerId=%d, name=%s, phone=%s, occurredAt=%s]",
            getCustomerId(), fullName, phone, getOccurredAt()
        );
    }
}