package com.example.demo.modules.product.domain.event;

import com.example.demo.common.event.DomainEvent;
import com.example.demo.common.event.EventMetadata;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Event published when product is restocked
 * 
 * Used for:
 * - Restock notifications
 * - Back-in-stock alerts to customers
 * - Supply chain tracking
 * - Purchasing analytics
 * 
 * @author PIM Team
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestockEvent implements DomainEvent {

    private static final long serialVersionUID = 1L;

    @Builder.Default
    private String eventId = UUID.randomUUID().toString();

    private UUID productId;
    private String sku;
    private String productName;

    /**
     * Restock details
     */
    private Integer restockQuantity;
    private Integer previousQuantity;
    private Integer newQuantity;
    private String restockType; // PURCHASE_ORDER, TRANSFER, RETURN, PRODUCTION

    /**
     * Purchase order information
     */
    private UUID purchaseOrderId;
    private String purchaseOrderNumber;
    private UUID supplierId;
    private String supplierName;
    private String supplierSku;

    /**
     * Financial details
     */
    private BigDecimal unitCost;
    private BigDecimal totalCost;
    private String currency;

    /**
     * Delivery details
     */
    private String trackingNumber;
    private String carrier;
    private LocalDateTime orderedDate;
    private LocalDateTime expectedDeliveryDate;
    private LocalDateTime actualDeliveryDate;
    private Integer leadTimeDays;

    /**
     * Quality control
     */
    private Boolean qualityCheckPassed;
    private Integer damagedUnits;
    private Integer acceptedUnits;
    private Integer rejectedUnits;
    private String qualityNotes;

    /**
     * Location details
     */
    private String warehouseLocation;
    private String binLocation;

    /**
     * Customer notifications
     */
    private Integer waitlistCustomers; // Number of customers to notify
    private Boolean notifyCustomers;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    @Builder.Default
    private LocalDateTime occurredAt = LocalDateTime.now();

    private String correlationId;
    private String causationId;
    private String source;
    private EventMetadata metadata;

    private String receivedBy;
    private String notes;

    public RestockEvent(
        UUID productId,
        String sku,
        String productName,
        Integer restockQuantity,
        String restockType
    ) {
        this.eventId = UUID.randomUUID().toString();
        this.productId = productId;
        this.sku = sku;
        this.productName = productName;
        this.restockQuantity = restockQuantity;
        this.restockType = restockType;
        this.occurredAt = LocalDateTime.now();
        this.source = "product-inventory-service";
    }

    @Override
    public String getEventType() {
        return "product.inventory.restocked";
    }

    @Override
    public String getTriggeredBy() {
        return receivedBy != null ? receivedBy : "receiving-dept";
    }

    @Override
    public String getSource() {
        return source != null ? source : "product-inventory-service";
    }

    @Override
    public boolean isAsync() {
        return true;
    }

    /**
     * Check if restock was on time
     */
    public boolean isOnTime() {
        if (expectedDeliveryDate == null || actualDeliveryDate == null) {
            return true;
        }
        return !actualDeliveryDate.isAfter(expectedDeliveryDate);
    }

    /**
     * Get days early/late
     */
    public Long getDeliveryVarianceDays() {
        if (expectedDeliveryDate == null || actualDeliveryDate == null) {
            return null;
        }
        return java.time.temporal.ChronoUnit.DAYS.between(
            expectedDeliveryDate,
            actualDeliveryDate
        );
    }

    /**
     * Check if had quality issues
     */
    public boolean hasQualityIssues() {
        return Boolean.FALSE.equals(qualityCheckPassed) ||
               (damagedUnits != null && damagedUnits > 0) ||
               (rejectedUnits != null && rejectedUnits > 0);
    }

    /**
     * Get acceptance rate
     */
    public Double getAcceptanceRate() {
        if (restockQuantity == null || restockQuantity == 0) {
            return 100.0;
        }
        int accepted = acceptedUnits != null ? acceptedUnits : restockQuantity;
        return (double) accepted / restockQuantity * 100;
    }

    @Override
    public String toString() {
        return String.format(
            "RestockEvent{productId=%s, sku=%s, quantity=%d, supplier=%s, type=%s}",
            productId, sku, restockQuantity, supplierName, restockType
        );
    }
}