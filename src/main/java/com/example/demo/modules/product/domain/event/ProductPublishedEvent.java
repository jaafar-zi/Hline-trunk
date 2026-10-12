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
 * Event published when a product is published to the catalog
 *
 * Critical business event that triggers:
 * - Catalog synchronization
 * - Search index update
 * - Cache invalidation
 * - Notification to merchandising team
 * - Analytics tracking
 *
 * @author PIM Team
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductPublishedEvent implements DomainEvent {

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
     * Category ID
     */
    private UUID categoryId;

    /**
     * Category name
     */
    private String categoryName;

    /**
     * Who published the product
     */
    private String publishedBy;

    /**
     * Previous product status
     */
    private ProductStatus previousStatus;

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
        return "product.published";
    }

    @Override
    public String getTriggeredBy() {
        return publishedBy != null ? publishedBy : "system";
    }

    @Override
    public String getSource() {
        return source != null ? source : "product-module";
    }

    @Override
    public boolean isAsync() {
        return true; // Async - triggers multiple downstream processes
    }

    @Override
    public String toString() {
        return String.format("ProductPublishedEvent{productId=%s, sku=%s, productName='%s', publishedBy='%s', occurredAt=%s}",
                productId, sku, productName, publishedBy, occurredAt);
    }
}
