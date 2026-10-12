package com.example.demo.modules.product.domain.event;

import com.example.demo.common.event.DomainEvent;
import com.example.demo.common.event.EventMetadata;
import com.example.demo.modules.product.domain.model.vo.ProductStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Event published when a product is permanently discontinued
 *
 * Critical business event that triggers:
 * - Catalog permanent removal
 * - Inventory clearance workflows
 * - Purchase order cancellation
 * - Supplier notifications
 * - Customer notifications (alternatives/substitutes)
 * - Historical data archival
 * - Reporting/analytics
 *
 * NOTE: This is more severe than unpublishing - product won't be sold again
 *
 * @author PIM Team
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDiscontinuedEvent implements DomainEvent {

    private static final long serialVersionUID = 1L;

    /**
     * Event ID (unique)
     */
    @Builder.Default
    private String eventId = UUID.randomUUID().toString();

    /**
     * Product ID
     */
    private UUID productId;

    /**
     * Product SKU
     */
    private String sku;

    /**
     * Product name
     */
    private String productName;

    /**
     * Reason for discontinuation
     * Examples: "end-of-life", "supplier-discontinued", "low-demand", "quality-issues"
     */
    private String reason;

    /**
     * Previous product status before discontinuation
     */
    private ProductStatus previousStatus;

    /**
     * Who discontinued the product
     */
    private String discontinuedBy;

    /**
     * Suggested replacement product ID (optional)
     */
    private UUID replacementProductId;

    /**
     * Suggested replacement SKU (optional)
     */
    private String replacementSku;

    /**
     * When event occurred
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    @Builder.Default
    private LocalDateTime occurredAt = LocalDateTime.now();

    /**
     * Correlation ID for tracing
     */
    private String correlationId;

    /**
     * Source service/module
     */
    @Builder.Default
    private String source = "product-module";

    /**
     * Additional metadata
     */
    private EventMetadata metadata;

    @Override
    public String getEventType() {
        return "product.discontinued";
    }

    @Override
    public String getTriggeredBy() {
        return discontinuedBy != null ? discontinuedBy : "system";
    }

    @Override
    public String getSource() {
        return source != null ? source : "product-module";
    }

    @Override
    public boolean isAsync() {
        return true; // Async - triggers multiple business processes
    }

    @Override
    public String toString() {
        return String.format("ProductDiscontinuedEvent{productId=%s, sku=%s, reason='%s', discontinuedBy='%s', occurredAt=%s}",
                productId, sku, reason, discontinuedBy, occurredAt);
    }
}
