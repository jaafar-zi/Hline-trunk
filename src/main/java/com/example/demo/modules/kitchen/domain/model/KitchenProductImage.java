package com.example.demo.modules.kitchen.domain.model;

import com.example.demo.common.base.entity.SoftDeletableAggregateRoot;
import com.example.demo.common.event.DomainEvent;
import com.example.demo.common.event.EntityDeletedEvent;
import com.example.demo.common.event.EntityRestoredEvent;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Kitchen Product Image Entity
 *
 * Images for kitchen products.
 * Can be tied to specific variants (e.g. show image only when Oak cabinet selected).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
    name = "kitchen_product_images",
    indexes = {
        @Index(name = "idx_kpi_product", columnList = "product_id"),
        @Index(name = "idx_kpi_sort", columnList = "sort_order")
    }
)
public class KitchenProductImage extends SoftDeletableAggregateRoot {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private KitchenProduct product;

    @Column(name = "url", nullable = false, length = 500)
    private String url;

    @Column(name = "thumbnail_url", length = 500)
    private String thumbnailUrl;

    @Column(name = "alt_text", length = 300)
    private String altText;

    @Column(name = "is_primary", nullable = false)
    @Builder.Default
    private Boolean isPrimary = false;

    @Column(name = "sort_order")
    @Builder.Default
    private Integer sortOrder = 0;

    /**
     * Optional: show this image only for a specific cabinet colour variant
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id")
    private KitchenProductVariant variant;

    // ====================================
    // Factory Methods
    // ====================================

    public static KitchenProductImage create(String url, String altText, boolean isPrimary) {
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("URL cannot be null or empty");
        }
        return KitchenProductImage.builder()
            .url(url)
            .altText(altText)
            .isPrimary(isPrimary)
            .sortOrder(0)
            .build();
    }

    // ====================================
    // SoftDeletableAggregateRoot
    // ====================================

    @Override
    protected DomainEvent createDeletedEvent(String deletedBy) {
        return EntityDeletedEvent.builder()
            .entityClass(KitchenProductImage.class)
            .entityType("KitchenProductImage")
            .entityId(this.getId())
            .deletedBy(deletedBy)
            .softDelete(true)
            .occurredAt(LocalDateTime.now())
            .build();
    }

    @Override
    protected DomainEvent createRestoredEvent(String restoredBy, String previousDeletedBy) {
        return EntityRestoredEvent.builder()
            .entityClass(KitchenProductImage.class)
            .entityType("KitchenProductImage")
            .entityId(this.getId())
            .restoredBy(restoredBy)
            .previousDeletedBy(previousDeletedBy)
            .previousDeletedAt(this.getDeletedAt())
            .occurredAt(LocalDateTime.now())
            .build();
    }
}