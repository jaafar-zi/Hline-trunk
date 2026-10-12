package com.example.demo.modules.product.domain.event;

import com.example.demo.common.event.DomainEvent;
import com.example.demo.common.event.EventMetadata;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Event published when product variant is activated
 * 
 * This event is used for:
 * - Product catalog updates (show variant on website)
 * - Inventory system synchronization
 * - Search index updates
 * - Customer notifications
 * 
 * @author PIM Team
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VariantActivatedEvent implements DomainEvent {

    private static final long serialVersionUID = 1L;

    @Builder.Default
    private String eventId = UUID.randomUUID().toString();

    private UUID variantId;
    private UUID productId;
    private String sku;
    private String variantName;
    private String productName;

    /**
     * Variant attributes (e.g., size=Large, color=Red)
     */
    private Map<String, String> attributes;

    /**
     * Inventory information
     */
    private Integer availableQuantity;
    private Boolean isInStock;

    /**
     * Variant metadata
     */
    private Boolean isDefault;
    private Integer displayOrder;

    /**
     * Metadata
     */
    private String activatedBy;
    private String activationReason; // MANUAL, RESTOCK, SCHEDULED, etc.

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    @Builder.Default
    private LocalDateTime occurredAt = LocalDateTime.now();

    private String correlationId;
    private String causationId;
    private String source;
    private EventMetadata metadata;

    /**
     * Constructor with essential fields
     */
    public VariantActivatedEvent(
        UUID variantId,
        UUID productId,
        String sku,
        String variantName,
        Map<String, String> attributes
    ) {
        this.eventId = UUID.randomUUID().toString();
        this.variantId = variantId;
        this.productId = productId;
        this.sku = sku;
        this.variantName = variantName;
        this.attributes = attributes;
        this.occurredAt = LocalDateTime.now();
        this.source = "product-variant-service";
    }

    @Override
    public String getEventType() {
        return "product.variant.activated";
    }

    @Override
    public String getTriggeredBy() {
        return activatedBy != null ? activatedBy : "system";
    }

    @Override
    public String getSource() {
        return source != null ? source : "product-variant-service";
    }

    @Override
    public boolean isAsync() {
        return true; // Async for downstream systems
    }

    /**
     * Check if variant is ready for immediate sale
     */
    public boolean isReadyForSale() {
        return Boolean.TRUE.equals(isInStock) && 
               availableQuantity != null && 
               availableQuantity > 0;
    }

    /**
     * Get formatted variant attributes
     */
    public String getFormattedAttributes() {
        if (attributes == null || attributes.isEmpty()) {
            return "";
        }

        return attributes.entrySet().stream()
            .map(entry -> entry.getKey() + ": " + entry.getValue())
            .reduce((a, b) -> a + ", " + b)
            .orElse("");
    }

    @Override
    public String toString() {
        return String.format(
            "VariantActivatedEvent{variantId=%s, productId=%s, sku=%s, variant=%s, inStock=%s}",
            variantId, productId, sku, variantName, isInStock
        );
    }
}