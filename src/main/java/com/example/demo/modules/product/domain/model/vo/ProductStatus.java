package com.example.demo.modules.product.domain.model.vo;

import lombok.Getter;

/**
 * Product Status Enum
 * 
 * Represents the lifecycle status of a product
 * 
 * Status Flow:
 * DRAFT → READY → PUBLISHED → UNPUBLISHED
 *   ↓                           ↓
 * DISCONTINUED ← ← ← ← ← ← ← ← ←
 * 
 * @author PIM Team
 * @version 1.0
 */
@Getter
public enum ProductStatus {

    /**
     * Initial state - product is being created/edited
     * - Not visible to customers
     * - Can be edited freely
     * - May be missing required information
     */
    DRAFT(
        "Draft",
        "Product is in draft state and not ready for publishing",
        false,
        true,
        0
    ),

    /**
     * Product is complete and ready to be published
     * - All required information filled
     * - Passed validation
     * - Awaiting approval/publishing
     */
    READY(
        "Ready",
        "Product is ready for publishing",
        false,
        true,
        1
    ),

    /**
     * Product is live and available for sale
     * - Visible to customers
     * - Available for purchase (if in stock)
     * - Can still be edited (with caution)
     */
    PUBLISHED(
        "Published",
        "Product is published and available",
        true,
        true,
        2
    ),

    /**
     * Product was published but has been taken offline
     * - Not visible to customers
     * - Can be re-published
     * - Maintains history
     */
    UNPUBLISHED(
        "Unpublished",
        "Product has been unpublished",
        false,
        true,
        3
    ),

    /**
     * Product is no longer sold
     * - Not visible to customers
     * - Cannot be re-published without review
     * - Maintains for historical/reporting purposes
     */
    DISCONTINUED(
        "Discontinued",
        "Product has been discontinued",
        false,
        false,
        4
    );

    private final String displayName;
    private final String description;
    private final boolean visibleToCustomers;
    private final boolean allowEditing;
    private final int sortOrder;

    ProductStatus(
        String displayName,
        String description,
        boolean visibleToCustomers,
        boolean allowEditing,
        int sortOrder
    ) {
        this.displayName = displayName;
        this.description = description;
        this.visibleToCustomers = visibleToCustomers;
        this.allowEditing = allowEditing;
        this.sortOrder = sortOrder;
    }

    /**
     * Check if product is in a sellable state
     */
    public boolean isSellable() {
        return this == PUBLISHED;
    }

    /**
     * Check if product can transition to target status
     */
    public boolean canTransitionTo(ProductStatus targetStatus) {
        if (targetStatus == null) {
            return false;
        }

        return switch (this) {
            case DRAFT -> targetStatus == READY || 
                         targetStatus == DISCONTINUED;
                         
            case READY -> targetStatus == PUBLISHED || 
                         targetStatus == DRAFT || 
                         targetStatus == DISCONTINUED;
                         
            case PUBLISHED -> targetStatus == UNPUBLISHED || 
                             targetStatus == DISCONTINUED;
                             
            case UNPUBLISHED -> targetStatus == PUBLISHED || 
                               targetStatus == DRAFT || 
                               targetStatus == DISCONTINUED;
                               
            case DISCONTINUED -> false; // Cannot transition from discontinued
        };
    }

    /**
     * Get valid next statuses from current status
     */
    public ProductStatus[] getValidNextStatuses() {
        return switch (this) {
            case DRAFT -> new ProductStatus[]{READY, DISCONTINUED};
            case READY -> new ProductStatus[]{PUBLISHED, DRAFT, DISCONTINUED};
            case PUBLISHED -> new ProductStatus[]{UNPUBLISHED, DISCONTINUED};
            case UNPUBLISHED -> new ProductStatus[]{PUBLISHED, DRAFT, DISCONTINUED};
            case DISCONTINUED -> new ProductStatus[]{};
        };
    }

    /**
     * Check if status requires validation
     */
    public boolean requiresValidation() {
        return this == READY || this == PUBLISHED;
    }

    /**
     * Check if status is active (not discontinued)
     */
    public boolean isActive() {
        return this != DISCONTINUED;
    }

    @Override
    public String toString() {
        return displayName;
    }
}