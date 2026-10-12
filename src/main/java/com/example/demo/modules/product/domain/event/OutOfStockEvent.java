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
 * Event published when product goes out of stock
 * 
 * This is a critical business event used for:
 * - Product availability status updates
 * - Customer notifications (back-in-stock alerts)
 * - Marketing campaign adjustments
 * - Emergency reordering
 * - Sales analytics
 * 
 * @author PIM Team
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OutOfStockEvent implements DomainEvent {

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
    private Integer previousQuantity;
    private Integer reservedQuantity;
    private Boolean backorderAllowed;
    private Integer backorderLimit;

    /**
     * Sales impact
     */
    private Integer pendingOrders; // Orders waiting for stock
    private Integer waitlistCount; // Customers on waitlist
    private Double estimatedLostRevenue;

    /**
     * Restock information
     */
    private LocalDateTime expectedRestockDate;
    private Integer expectedRestockQuantity;
    private String restockStatus; // ORDERED, IN_TRANSIT, DELAYED, UNKNOWN
    private UUID purchaseOrderId;

    /**
     * Historical context
     */
    private Integer stockoutCount; // How many times out of stock this month/year
    private LocalDateTime lastStockoutDate;
    private Integer daysOutOfStock; // Total days out of stock

    /**
     * Business impact
     */
    private String impactLevel; // LOW, MEDIUM, HIGH, CRITICAL
    private Boolean isPopularProduct;
    private Boolean isSeasonalProduct;
    private Double averageDailySales;

    /**
     * Action flags
     */
    private Boolean notifyCustomers; // Send back-in-stock notifications
    private Boolean hideListing; // Hide from website
    private Boolean emergencyReorder; // Trigger emergency purchase order
    private Boolean notifySupplier; // Alert supplier

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
    private String reason; // SOLD_OUT, DAMAGED, RECALLED, TRANSFER, etc.
    private String detectedBy;

    public OutOfStockEvent(
        UUID productId,
        String sku,
        String productName
    ) {
        this.eventId = UUID.randomUUID().toString();
        this.productId = productId;
        this.sku = sku;
        this.productName = productName;
        this.occurredAt = LocalDateTime.now();
        this.source = "product-inventory-service";
        this.impactLevel = "HIGH";
    }

    @Override
    public String getEventType() {
        return "product.inventory.out_of_stock";
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
        return false; // Sync - critical business event
    }

    /**
     * Check if requires immediate action
     */
    public boolean requiresImmediateAction() {
        return "CRITICAL".equals(impactLevel) || 
               (isPopularProduct != null && isPopularProduct) ||
               (pendingOrders != null && pendingOrders > 10);
    }

    /**
     * Check if customers should be notified
     */
    public boolean shouldNotifyCustomers() {
        return Boolean.TRUE.equals(notifyCustomers) && waitlistCount != null && waitlistCount > 0;
    }

    /**
     * Check if product should be hidden
     */
    public boolean shouldHideProduct() {
        return Boolean.TRUE.equals(hideListing) && !Boolean.TRUE.equals(backorderAllowed);
    }

    /**
     * Get estimated days until restock
     */
    public Long getDaysUntilRestock() {
        if (expectedRestockDate == null) {
            return null;
        }
        return java.time.temporal.ChronoUnit.DAYS.between(
            LocalDateTime.now(),
            expectedRestockDate
        );
    }

    @Override
    public String toString() {
        return String.format(
            "OutOfStockEvent{productId=%s, sku=%s, product=%s, impact=%s, backorder=%s}",
            productId, sku, productName, impactLevel, backorderAllowed
        );
    }
}