package com.example.demo.modules.order.domain.model.vo;

/**
 * Shipping Status Enumeration
 * 
 * @author Order Team
 * @version 1.0
 */
public enum ShippingStatus {
    
    NOT_SHIPPED("Not Shipped", "Order has not been shipped yet"),
    PREPARING("Preparing", "Order is being prepared for shipment"),
    SHIPPED("Shipped", "Order has been shipped"),
    IN_TRANSIT("In Transit", "Order is in transit"),
    OUT_FOR_DELIVERY("Out for Delivery", "Order is out for delivery"),
    DELIVERED("Delivered", "Order has been delivered"),
    DELIVERY_FAILED("Delivery Failed", "Delivery attempt failed"),
    RETURNED("Returned", "Order has been returned");

    private final String displayName;
    private final String description;

    ShippingStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public boolean isDelivered() {
        return this == DELIVERED;
    }

    public boolean isInProgress() {
        return this == SHIPPED || this == IN_TRANSIT || this == OUT_FOR_DELIVERY;
    }
}