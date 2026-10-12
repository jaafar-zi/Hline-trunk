package com.example.demo.modules.customer.domain.event;

import java.util.UUID;

import lombok.Getter;

/**
 * Event published when a customer is soft deleted
 * 
 * Soft delete preserves customer data for:
 * - Regulatory compliance
 * - Historical order records
 * - Analytics
 * 
 * Business Impact:
 * - Customer cannot login
 * - All active orders cancelled
 * - Subscriptions cancelled
 * - Data retention policy applied
 * - GDPR compliance workflow triggered
 * 
 * @author Customer Domain Team
 */
@Getter
public class CustomerDeletedEvent extends CustomerEvent {
    
    private final String email;
    private final String deletedBy;

    public CustomerDeletedEvent(
            UUID customerId,
            Long version,
            String email,
            String deletedBy) {
        super(customerId, version, deletedBy);
        this.email = email;
        this.deletedBy = deletedBy;
    }

    @Override
    public String getEventType() {
        return "customer.deleted";
    }

    @Override
    public boolean isAsync() {
        return false; // Sync for immediate enforcement
    }

    @Override
    public String toString() {
        return String.format(
            "CustomerDeletedEvent[customerId=%s, email=%s, deletedBy=%s, occurredAt=%s]",
            getCustomerId(), email, deletedBy, getOccurredAt()
        );
    }
}