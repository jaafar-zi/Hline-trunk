package com.example.demo.modules.product.domain.event;

import com.example.demo.common.event.DomainEvent;
import com.example.demo.common.event.EventMetadata;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Event published when product stock level changes
 * 
 * This is a business-critical event used for:
 * - Inventory tracking and auditing
 * - Reorder point triggers
 * - Analytics and reporting
 * - Integration with warehouse management systems
 * 
 * @author PIM Team
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockLevelChangedEvent implements DomainEvent {

    private static final long serialVersionUID = 1L;

    @Builder.Default
    private String eventId = UUID.randomUUID().toString();

    private UUID productId;
    private String sku;
    private String productName;

    /**
     * Stock quantities
     */
    private Integer previousQuantity;
    private Integer newQuantity;
    private Integer previousAvailable;
    private Integer newAvailable;
    private Integer quantityChange;

    /**
     * Change details
     */
    private String changeType; // INCREASE, DECREASE, RESTOCK, REMOVAL, ADJUSTMENT, SALE, RETURN
    private String reason;
    private String referenceId; // Order ID, PO ID, Adjustment ID, etc.
    private String referenceType; // ORDER, PURCHASE_ORDER, ADJUSTMENT, TRANSFER, etc.

    /**
     * Location details
     */
    private String warehouseLocation;
    private String binLocation;

    /**
     * Tracking
     */
    private String changedBy;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    @Builder.Default
    private LocalDateTime occurredAt = LocalDateTime.now();

    private String correlationId;
    private String causationId;
    private String source;
    private EventMetadata metadata;

    /**
     * Additional context
     */
    private String notes;
    private Boolean triggerReorder; // Flag to trigger reorder process

    public StockLevelChangedEvent(
        UUID productId,
        String sku,
        Integer previousQuantity,
        Integer newQuantity,
        String changeType
    ) {
        this.eventId = UUID.randomUUID().toString();
        this.productId = productId;
        this.sku = sku;
        this.previousQuantity = previousQuantity;
        this.newQuantity = newQuantity;
        this.changeType = changeType;
        this.quantityChange = newQuantity - previousQuantity;
        this.occurredAt = LocalDateTime.now();
        this.source = "product-inventory-service";
    }

    @Override
    public String getEventType() {
        return "product.inventory.stock_level_changed";
    }

    @Override
    public String getTriggeredBy() {
        return changedBy != null ? changedBy : "system";
    }

    @Override
    public String getSource() {
        return source != null ? source : "product-inventory-service";
    }

    @Override
    public boolean isAsync() {
        return true; // Async for analytics and downstream systems
    }

    /**
     * Check if this was a stock increase
     */
    public boolean isStockIncrease() {
        return quantityChange != null && quantityChange > 0;
    }

    /**
     * Check if this was a stock decrease
     */
    public boolean isStockDecrease() {
        return quantityChange != null && quantityChange < 0;
    }

    /**
     * Get absolute change amount
     */
    public Integer getAbsoluteChange() {
        return quantityChange != null ? Math.abs(quantityChange) : 0;
    }

    @Override
    public String toString() {
        return String.format(
            "StockLevelChangedEvent{productId=%s, sku=%s, change=%d->%d (%s), type=%s}",
            productId, sku, previousQuantity, newQuantity, 
            quantityChange > 0 ? "+" + quantityChange : quantityChange,
            changeType
        );
    }
}