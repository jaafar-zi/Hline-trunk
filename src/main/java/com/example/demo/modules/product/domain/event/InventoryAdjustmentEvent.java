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
 * Event published when inventory is manually adjusted
 * 
 * Used for:
 * - Audit trail of manual adjustments
 * - Inventory reconciliation
 * - Loss prevention tracking
 * - Compliance reporting
 * 
 * @author PIM Team
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryAdjustmentEvent implements DomainEvent {

    private static final long serialVersionUID = 1L;

    @Builder.Default
    private String eventId = UUID.randomUUID().toString();

    private UUID productId;
    private String sku;
    private String productName;

    /**
     * Adjustment details
     */
    private UUID adjustmentId;
    private String adjustmentType; // CYCLE_COUNT, DAMAGED, LOST, FOUND, CORRECTION, TRANSFER
    private Integer previousQuantity;
    private Integer newQuantity;
    private Integer adjustmentAmount;

    /**
     * Reason and documentation
     */
    private String reason;
    private String reasonCategory; // SHRINKAGE, DAMAGE, COUNTING_ERROR, THEFT, EXPIRED, OTHER
    private String notes;
    private String documentReference; // Invoice, report, ticket number
    private Boolean approved;
    private String approvedBy;
    private LocalDateTime approvedAt;

    /**
     * Financial impact
     */
    private java.math.BigDecimal unitCost;
    private java.math.BigDecimal totalValue;
    private java.math.BigDecimal financialImpact;

    /**
     * Location details
     */
    private String warehouseLocation;
    private String binLocation;
    private String fromLocation;
    private String toLocation;

    /**
     * Cycle count information
     */
    private UUID cycleCountId;
    private Integer expectedQuantity;
    private Integer countedQuantity;
    private Integer variance;
    private String countedBy;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    @Builder.Default
    private LocalDateTime occurredAt = LocalDateTime.now();

    private String correlationId;
    private String causationId;
    private String source;
    private EventMetadata metadata;

    private String adjustedBy;
    private String supervisorId;

    public InventoryAdjustmentEvent(
        UUID productId,
        String sku,
        Integer previousQuantity,
        Integer newQuantity,
        String adjustmentType,
        String reason
    ) {
        this.eventId = UUID.randomUUID().toString();
        this.productId = productId;
        this.sku = sku;
        this.previousQuantity = previousQuantity;
        this.newQuantity = newQuantity;
        this.adjustmentAmount = newQuantity - previousQuantity;
        this.adjustmentType = adjustmentType;
        this.reason = reason;
        this.occurredAt = LocalDateTime.now();
        this.source = "product-inventory-service";
    }

    @Override
    public String getEventType() {
        return "product.inventory.adjustment";
    }

    @Override
    public String getTriggeredBy() {
        return adjustedBy != null ? adjustedBy : "warehouse-staff";
    }

    @Override
    public String getSource() {
        return source != null ? source : "product-inventory-service";
    }

    @Override
    public boolean isAsync() {
        return false; // Sync - requires audit trail
    }

    /**
     * Check if adjustment is negative (loss)
     */
    public boolean isNegativeAdjustment() {
        return adjustmentAmount != null && adjustmentAmount < 0;
    }

    /**
     * Check if adjustment is positive (gain)
     */
    public boolean isPositiveAdjustment() {
        return adjustmentAmount != null && adjustmentAmount > 0;
    }

    /**
     * Check if requires approval
     */
    public boolean requiresApproval() {
        if (adjustmentAmount == null) {
            return false;
        }
        // Adjustments over threshold or specific types require approval
        return Math.abs(adjustmentAmount) > 100 || 
               "LOST".equals(adjustmentType) || 
               "THEFT".equals(reasonCategory);
    }

    /**
     * Get absolute adjustment value
     */
    public Integer getAbsoluteAdjustment() {
        return adjustmentAmount != null ? Math.abs(adjustmentAmount) : 0;
    }

    @Override
    public String toString() {
        return String.format(
            "InventoryAdjustmentEvent{productId=%s, sku=%s, adjustment=%d, type=%s, reason=%s}",
            productId, sku, adjustmentAmount, adjustmentType, reason
        );
    }
}