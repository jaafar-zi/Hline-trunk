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
 * Event published when product stock falls below threshold
 * 
 * This is an alert event used for:
 * - Automatic reordering triggers
 * - Inventory management alerts
 * - Supply chain notifications
 * - Dashboard warnings
 * 
 * @author PIM Team
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LowStockEvent implements DomainEvent {

    private static final long serialVersionUID = 1L;

    @Builder.Default
    private String eventId = UUID.randomUUID().toString();

    private UUID productId;
    private String sku;
    private String productName;
    private UUID categoryId;
    private String categoryName;

    /**
     * Stock information
     */
    private Integer currentQuantity;
    private Integer threshold;
    private Integer reservedQuantity;
    private Integer availableQuantity;

    /**
     * Reorder information
     */
    private Integer reorderPoint;
    private Integer reorderQuantity;
    private Integer recommendedOrderQuantity;
    private Boolean autoReorderEnabled;

    /**
     * Supplier information
     */
    private String supplierSku;
    private UUID preferredSupplierId;
    private String supplierName;
    private Integer leadTimeDays;

    /**
     * Sales velocity (for intelligent reordering)
     */
    private Double averageDailySales;
    private Integer estimatedDaysUntilStockout;

    /**
     * Alert details
     */
    private String severity; // LOW, MEDIUM, HIGH, CRITICAL
    private String alertLevel; // WARNING, URGENT
    private Boolean requiresImmediateAction;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    @Builder.Default
    private LocalDateTime occurredAt = LocalDateTime.now();

    private String correlationId;
    private String causationId;
    private String source;
    private EventMetadata metadata;

    /**
     * Tracking
     */
    private String warehouseLocation;
    private String detectedBy;

    public LowStockEvent(
        UUID productId,
        String sku,
        String productName,
        Integer currentQuantity,
        Integer threshold
    ) {
        this.eventId = UUID.randomUUID().toString();
        this.productId = productId;
        this.sku = sku;
        this.productName = productName;
        this.currentQuantity = currentQuantity;
        this.threshold = threshold;
        this.occurredAt = LocalDateTime.now();
        this.source = "product-inventory-service";
        this.severity = calculateSeverity(currentQuantity, threshold);
    }

    @Override
    public String getEventType() {
        return "product.inventory.low_stock";
    }

    @Override
    public String getTriggeredBy() {
        return detectedBy != null ? detectedBy : "inventory-monitor";
    }

    @Override
    public String getSource() {
        return source != null ? source : "product-inventory-service";
    }

    @Override
    public boolean isAsync() {
        return true; // Async for alerting systems
    }

    /**
     * Calculate severity based on how close to zero
     */
    private String calculateSeverity(Integer current, Integer threshold) {
        if (current == null || threshold == null) {
            return "MEDIUM";
        }

        double percentageOfThreshold = (double) current / threshold * 100;

        if (percentageOfThreshold <= 25) {
            return "CRITICAL";
        } else if (percentageOfThreshold <= 50) {
            return "HIGH";
        } else if (percentageOfThreshold <= 75) {
            return "MEDIUM";
        } else {
            return "LOW";
        }
    }

    /**
     * Check if critical (very low stock)
     */
    public boolean isCritical() {
        return "CRITICAL".equals(severity);
    }

    /**
     * Get stock percentage of threshold
     */
    public Double getStockPercentage() {
        if (currentQuantity == null || threshold == null || threshold == 0) {
            return 0.0;
        }
        return (double) currentQuantity / threshold * 100;
    }

    @Override
    public String toString() {
        return String.format(
            "LowStockEvent{productId=%s, sku=%s, product=%s, current=%d, threshold=%d, severity=%s}",
            productId, sku, productName, currentQuantity, threshold, severity
        );
    }
}