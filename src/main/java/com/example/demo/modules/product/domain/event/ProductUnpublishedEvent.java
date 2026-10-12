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
 * Event published when a product is unpublished from the catalog
 *
 * Critical business event that triggers:
 * - Catalog removal
 * - Search index removal
 * - Cache invalidation
 * - Active shopping cart cleanup
 * - Customer notifications (if in wishlists)
 * - Analytics tracking
 *
 * @author PIM Team
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductUnpublishedEvent implements DomainEvent {

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
     * Reason for unpublishing
     */
    private String reason;

    /**
     * Who unpublished the product
     */
    private String unpublishedBy;

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
        return "product.unpublished";
    }

    @Override
    public String getTriggeredBy() {
        return unpublishedBy != null ? unpublishedBy : "system";
    }

    @Override
    public String getSource() {
        return source != null ? source : "product-module";
    }

    @Override
    public boolean isAsync() {
        return true; // Async - triggers cleanup processes
    }

    @Override
    public String toString() {
        return String.format("ProductUnpublishedEvent{productId=%s, sku=%s, reason='%s', unpublishedBy='%s', occurredAt=%s}",
                productId, sku, reason, unpublishedBy, occurredAt);
    }
}
