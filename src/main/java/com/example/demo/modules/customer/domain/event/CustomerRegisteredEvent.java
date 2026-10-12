package com.example.demo.modules.customer.domain.event;

import java.util.UUID;

import lombok.Getter;

/**
 * Event published when a new customer is registered
 * 
 * This is the creation event for the Customer aggregate
 * 
 * Business Impact:
 * - Welcome email should be sent
 * - Customer profile created in CRM
 * - Analytics tracking initiated
 * 
 * @author Customer Domain Team
 */
@Getter
public class CustomerRegisteredEvent extends CustomerEvent {
    
    private final String email;
    private final String firstName;
    private final String lastName;

    public CustomerRegisteredEvent(
            UUID customerId,
            Long version,
            String email,
            String firstName,
            String lastName) {
        super(customerId, version);
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    @Override
    public String getEventType() {
        return "customer.registered";
    }

    @Override
    public String toString() {
        return String.format(
            "CustomerRegisteredEvent[customerId=%d, email=%s, name=%s %s, occurredAt=%s]",
            getCustomerId(), email, firstName, lastName, getOccurredAt()
        );
    }
}