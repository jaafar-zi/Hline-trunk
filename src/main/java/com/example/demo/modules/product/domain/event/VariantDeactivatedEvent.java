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
 * Event published when product variant is deactivated
 * 
 * This event is used for:
 * - Product catalog updates (hide variant from website)
 * - Inventory system synchronization
 * - Search index updates
 * - Shopping cart validation
 * - Order cancellation checks
 * 
 * @author PIM Team
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VariantDeactivatedEvent implements DomainEvent {

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
     * Deactivation details
     */
    private String reason; // OUT_OF_STOCK, DISCONTINUED, MANUAL, SEASONAL, etc.
    private String deactivationType; // TEMPORARY, PERMANENT, MANUAL
    private Boolean isTemporary;

    /**
     * Inventory information (at time of deactivation)
     */
    private Integer remainingQuantity;
    private Boolean hadInventory;

    /**
     * Active period
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime activatedAt;

    private Long activeDurationHours; // How long was it active?

    /**
     * Business impact
     */
    private Integer pendingOrders; // Active orders containing this variant
    private Boolean hasActiveAlternatives; // Are there other active variants?

    /**
     * Metadata
     */
    private String deactivatedBy;

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
    public VariantDeactivatedEvent(
        UUID variantId,
        UUID productId,
        String sku,
        String variantName,
        String reason
    ) {
        this.eventId = UUID.randomUUID().toString();
        this.variantId = variantId;
        this.productId = productId;
        this.sku = sku;
        this.variantName = variantName;
        this.reason = reason;
        this.occurredAt = LocalDateTime.now();
        this.source = "product-variant-service";
        this.deactivationType = determineDeactivationType(reason);
    }

    @Override
    public String getEventType() {
        return "product.variant.deactivated";
    }

    @Override
    public String getTriggeredBy() {
        return deactivatedBy != null ? deactivatedBy : "system";
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
     * Determine deactivation type based on reason
     */
    private String determineDeactivationType(String reason) {
        if (reason == null) {
            return "MANUAL";
        }

        return switch (reason.toUpperCase()) {
            case "OUT_OF_STOCK" -> "TEMPORARY";
            case "DISCONTINUED" -> "PERMANENT";
            default -> "MANUAL";
        };
    }

    /**
     * Calculate active duration if activation time is known
     */
    public void calculateActiveDuration() {
        if (activatedAt != null) {
            this.activeDurationHours = java.time.temporal.ChronoUnit.HOURS.between(
                activatedAt,
                occurredAt
            );
        }
    }

    /**
     * Check if deactivation is permanent
     */
    public boolean isPermanent() {
        return "PERMANENT".equals(deactivationType);
    }

    /**
     * Check if requires immediate action
     */
    public boolean requiresImmediateAction() {
        return pendingOrders != null && pendingOrders > 0;
    }

    /**
     * Get formatted active duration
     */
    public String getFormattedDuration() {
        if (activeDurationHours == null) {
            return "Unknown";
        }

        long days = activeDurationHours / 24;
        long hours = activeDurationHours % 24;

        if (days > 0) {
            return String.format("%d days, %d hours", days, hours);
        } else {
            return String.format("%d hours", hours);
        }
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
            "VariantDeactivatedEvent{variantId=%s, productId=%s, sku=%s, variant=%s, reason=%s}",
            variantId, productId, sku, variantName, reason
        );
    }
}