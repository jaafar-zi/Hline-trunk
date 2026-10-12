package com.example.demo.modules.order.domain.model.vo;

/**
 * Order Status Enumeration
 * 
 * Represents the lifecycle states of an order
 * 
 * @author Order Team
 * @version 1.0
 */
public enum OrderStatus {
    
    /**
     * Order created but not yet confirmed (initial state)
     */
    PENDING("Pending", "Order is awaiting confirmation"),
    
    /**
     * Order confirmed and payment authorized
     */
    CONFIRMED("Confirmed", "Order has been confirmed"),
    
    /**
     * Payment has been processed successfully
     */
    PAID("Paid", "Payment has been processed"),
    
    /**
     * Order is being prepared for shipment
     */
    PROCESSING("Processing", "Order is being processed"),
    
    /**
     * Order has been shipped to customer
     */
    SHIPPED("Shipped", "Order has been shipped"),
    
    /**
     * Order delivered to customer
     */
    DELIVERED("Delivered", "Order has been delivered"),
    
    /**
     * Order cancelled by customer or system
     */
    CANCELLED("Cancelled", "Order has been cancelled"),
    
    /**
     * Order returned by customer
     */
    RETURNED("Returned", "Order has been returned"),
    
    /**
     * Order refunded
     */
    REFUNDED("Refunded", "Order has been refunded"),
    
    /**
     * Order failed (payment failed, stock unavailable, etc.)
     */
    FAILED("Failed", "Order has failed");

    private final String displayName;
    private final String description;

    OrderStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Check if order can be cancelled
     */
    public boolean isCancellable() {
        return this == PENDING || this == CONFIRMED || this == PAID;
    }

    /**
     * Check if order is in a final state
     */
    public boolean isFinal() {
        return this == DELIVERED || this == CANCELLED || this == REFUNDED;
    }

    /**
     * Check if order is active
     */
    public boolean isActive() {
        return !isFinal() && this != FAILED;
    }
}