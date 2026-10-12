package com.example.demo.modules.order.domain.model.vo;

/**
 * Payment Status Enumeration
 * 
 * @author Order Team
 * @version 1.0
 */
public enum PaymentStatus {
    
    PENDING("Pending", "Payment is pending"),
    AUTHORIZED("Authorized", "Payment has been authorized"),
    CAPTURED("Captured", "Payment has been captured"),
    FAILED("Failed", "Payment has failed"),
    REFUNDED("Refunded", "Payment has been refunded"),
    PARTIALLY_REFUNDED("Partially Refunded", "Payment has been partially refunded"),
    CANCELLED("Cancelled", "Payment has been cancelled");

    private final String displayName;
    private final String description;

    PaymentStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public boolean isPaid() {
        return this == AUTHORIZED || this == CAPTURED;
    }

    public boolean isFinal() {
        return this == CAPTURED || this == REFUNDED || this == CANCELLED;
    }
}