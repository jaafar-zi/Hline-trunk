package com.example.demo.modules.product.domain.model.vo;

import lombok.Getter;

/**
 * Stock Status Enum
 * 
 * Represents current stock availability status
 * 
 * @author PIM Team
 * @version 1.0
 */
@Getter
public enum StockStatus {

    IN_STOCK(
        "In Stock",
        "Product is available for immediate shipment",
        true,
        0
    ),

    LOW_STOCK(
        "Low Stock",
        "Product stock is running low",
        true,
        1
    ),

    OUT_OF_STOCK(
        "Out of Stock",
        "Product is currently unavailable",
        false,
        2
    ),

    BACKORDER(
        "Backorder",
        "Product can be ordered but will ship later",
        true,
        3
    ),

    DISCONTINUED(
        "Discontinued",
        "Product is no longer available",
        false,
        4
    ),

    PREORDER(
        "Pre-order",
        "Product available for pre-order",
        true,
        5
    );

    private final String displayName;
    private final String description;
    private final boolean available;
    private final int sortOrder;

    StockStatus(
        String displayName,
        String description,
        boolean available,
        int sortOrder
    ) {
        this.displayName = displayName;
        this.description = description;
        this.available = available;
        this.sortOrder = sortOrder;
    }

    public boolean isAvailable() {
        return available;
    }

    public boolean requiresAlert() {
        return this == LOW_STOCK || this == OUT_OF_STOCK;
    }

    @Override
    public String toString() {
        return displayName;
    }
}