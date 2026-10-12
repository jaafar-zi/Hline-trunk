package com.example.demo.modules.customer.domain.event;

import lombok.Getter;

import java.util.UUID;

/**
 * Event published when a soft-deleted customer is restored
 * 
 * Restoration scenarios:
 * - Admin mistake correction
 * - Customer appeals deletion
 * - Legal requirement
 * 
 * Business Impact:
 * - Customer access restored
 * - Customer service notified
 * - Audit log updated
 * - Welcome back email sent
 * 
 * @author Customer Domain Team
 */
@Getter
public class CustomerRestoredEvent extends CustomerEvent {
    
    private final String email;
    private final String restoredBy;
    private final String previousDeletedBy;

    public CustomerRestoredEvent(
            UUID customerId,
            Long version,
            String email,
            String restoredBy,
            String previousDeletedBy) {
        super(customerId, version, restoredBy);
        this.email = email;
        this.restoredBy = restoredBy;
        this.previousDeletedBy = previousDeletedBy;
    }

    @Override
    public String getEventType() {
        return "customer.restored";
    }

    @Override
    public boolean isAsync() {
        return false; // Sync for immediate access restoration
    }

    @Override
    public String toString() {
        return String.format(
            "CustomerRestoredEvent[customerId=%s, email=%s, restoredBy=%s, " +
            "previousDeletedBy=%s, occurredAt=%s]",
            getCustomerId(), email, restoredBy, previousDeletedBy, getOccurredAt()
        );
    }
}