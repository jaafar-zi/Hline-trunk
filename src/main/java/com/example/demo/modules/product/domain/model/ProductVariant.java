package com.example.demo.modules.product.domain.model;

import com.example.demo.common.base.entity.SoftDeletableAggregateRoot;
import com.example.demo.common.event.DomainEvent;
import com.example.demo.common.event.EntityDeletedEvent;
import com.example.demo.common.event.EntityRestoredEvent;
import com.example.demo.common.event.EntityUpdatedEvent;
import com.example.demo.common.util.ChangeTracker;
import com.example.demo.modules.product.domain.event.VariantActivatedEvent;
import com.example.demo.modules.product.domain.event.VariantDeactivatedEvent;
import com.example.demo.modules.product.domain.model.vo.Money;
import com.example.demo.modules.product.domain.model.vo.ProductStatus;
import com.example.demo.modules.product.domain.model.vo.SKU;
import com.example.demo.modules.product.domain.model.vo.VariantOption;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Product Variant Entity
 * 
 * Represents a variant of a product (e.g., different size, color, style)
 * Part of Product aggregate but can have its own SKU, price, and inventory
 * 
 * Business Rules:
 * - Must have unique SKU
 * - Must belong to a parent product
 * - Cannot be activated if parent product is not published
 * - Must have at least one attribute (option value)
 * 
 * Events Published:
 * - EntityUpdatedEvent<ProductVariant>
 * - VariantActivatedEvent
 * - VariantDeactivatedEvent
 * - EntityDeletedEvent
 * - EntityRestoredEvent
 * 
 * @author PIM Team
 * @version 2.0
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
    name = "product_variants",
    indexes = {
        @Index(name = "idx_variant_product", columnList = "product_id"),
        @Index(name = "idx_variant_sku", columnList = "sku"),
        @Index(name = "idx_variant_active", columnList = "active")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_variant_sku", columnNames = "sku")
    }
)
public class ProductVariant extends SoftDeletableAggregateRoot {

    // ====================================
    // Core Information
    // ====================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "sku", unique = true, nullable = false, length = 50))
    private SKU sku;

    @Column(name = "name", length = 255)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Column(name = "display_order")
    @Builder.Default
    private Integer displayOrder = 0;

    // ====================================
    // Variant Options (e.g., Size: Large, Color: Red)
    // ====================================

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "product_variant_options",
        joinColumns = @JoinColumn(name = "variant_id")
    )
    @Builder.Default
    private List<VariantOption> optionValues = new ArrayList<>();

    // ====================================
    // Pricing
    // ====================================

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "amount", column = @Column(name = "price_amount", precision = 19, scale = 4)),
        @AttributeOverride(name = "currency", column = @Column(name = "price_currency", length = 3))
    })
    private Money price;

    @Column(name = "price_adjustment", precision = 19, scale = 4)
    private BigDecimal priceAdjustment;

    @Column(name = "price_adjustment_type", length = 20)
    @Enumerated(EnumType.STRING)
    private PriceAdjustmentType priceAdjustmentType;

    // ====================================
    // Inventory
    // ====================================

    @OneToOne(
        mappedBy = "variant",
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    private VariantInventory inventory;

    // ====================================
    // Media
    // ====================================

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
        name = "product_variant_images",
        joinColumns = @JoinColumn(name = "variant_id")
    )
    @Column(name = "image_url")
    @OrderColumn(name = "display_order")
    @Builder.Default
    private List<String> imageUrls = new ArrayList<>();

    // ====================================
    // Identifiers
    // ====================================

    @Column(name = "barcode", length = 100)
    private String barcode;

    @Column(name = "is_default", nullable = false)
    @Builder.Default
    private Boolean isDefault = false;

    // ====================================
    // Factory Methods
    // ====================================

    /**
     * Create variant with auto-generated SKU
     */
    public static ProductVariant create(Product product, String name, List<VariantOption> optionValues) {
        validateCreation(product, optionValues);

        SKU variantSku = SKU.generate(product.getSku().getValue());

        return ProductVariant.builder()
            .product(product)
            .sku(variantSku)
            .name(name)
            .optionValues(new ArrayList<>(optionValues))
            .active(true)
            .displayOrder(0)
            .isDefault(false)
            .build();
    }

    /**
     * Create variant with specific SKU
     */
    public static ProductVariant create(Product product, SKU sku, String name, List<VariantOption> optionValues) {
        validateCreation(product, optionValues);
        
        if (sku == null) {
            throw new IllegalArgumentException("SKU cannot be null");
        }

        return ProductVariant.builder()
            .product(product)
            .sku(sku)
            .name(name)
            .optionValues(new ArrayList<>(optionValues))
            .active(true)
            .displayOrder(0)
            .isDefault(false)
            .build();
    }

    private static void validateCreation(Product product, List<VariantOption> optionValues) {
        if (product == null) {
            throw new IllegalArgumentException("Parent product cannot be null");
        }
        if (optionValues == null || optionValues.isEmpty()) {
            throw new IllegalArgumentException("Variant must have at least one option value");
        }
    }

    // ====================================
    // Domain Logic - Pricing
    // ====================================

    /**
     * Set variant-specific price
     */
    public void setVariantPrice(Money price) {
        if (price == null) {
            return;
        }

        ChangeTracker tracker = new ChangeTracker();
        tracker.track("price", this.price, price);

        this.price = price;
        this.priceAdjustment = null;
        this.priceAdjustmentType = null;

        registerEvent(new EntityUpdatedEvent<>(this, tracker.getChanges()));
    }

    /**
     * Set price adjustment from base product price
     */
    public void setPriceAdjustment(BigDecimal adjustment, PriceAdjustmentType adjustmentType) {
        if (adjustment == null || adjustmentType == null) {
            throw new IllegalArgumentException("Adjustment and type cannot be null");
        }

        ChangeTracker tracker = new ChangeTracker();
        tracker.track("priceAdjustment", this.priceAdjustment, adjustment);
        tracker.track("priceAdjustmentType", this.priceAdjustmentType, adjustmentType);

        this.priceAdjustment = adjustment;
        this.priceAdjustmentType = adjustmentType;
        this.price = null;

        registerEvent(new EntityUpdatedEvent<>(this, tracker.getChanges()));
    }

    /**
     * Get effective price (variant price or calculated from parent)
     */
    @Transient
    public Money getEffectivePrice() {
        if (this.price != null) {
            return this.price;
        }

        Money basePrice = product != null ? product.getCurrentPrice().getPrice() : null;
        if (basePrice == null) {
            return null;
        }

        if (this.priceAdjustment == null || this.priceAdjustmentType == null) {
            return basePrice;
        }

        return switch (this.priceAdjustmentType) {
            case FIXED -> basePrice.add(Money.of(priceAdjustment, basePrice.getCurrency()));
            case PERCENTAGE -> basePrice.multiplyBy(
                BigDecimal.ONE.add(priceAdjustment.divide(BigDecimal.valueOf(100)))
            );
        };
    }

    // ====================================
    // Domain Logic - Inventory
    // ====================================

    /**
     * Set variant inventory
     */
    public void setInventory(VariantInventory inventory) {
        if (inventory != null) {
            inventory.setVariant(this);
        }
        this.inventory = inventory;
    }

    /**
     * Check if variant is in stock
     */
    @Transient
    public boolean isInStock() {
        return inventory != null && inventory.isInStock();
    }

    /**
     * Get available quantity
     */
    @Transient
    public int getAvailableQuantity() {
        return inventory != null ? inventory.getAvailableQuantity() : 0;
    }

    // ====================================
    // Domain Logic - Activation
    // ====================================

    /**
     * Activate variant
     */
    public void activate() {
        if (Boolean.TRUE.equals(this.active)) {
            throw new IllegalStateException("Variant is already active");
        }

        if (product != null && product.getStatus() != ProductStatus.PUBLISHED) {
            throw new IllegalStateException("Cannot activate variant when parent product is not published");
        }

        this.active = true;

        registerEvent(VariantActivatedEvent.builder()
            .variantId(this.getId())
            .productId(this.product != null ? this.product.getId() : null)
            .sku(this.sku.getValue())
            .variantName(this.name)
            .occurredAt(LocalDateTime.now())
            .build());
    }

    /**
     * Deactivate variant
     */
    public void deactivate(String reason) {
        if (Boolean.FALSE.equals(this.active)) {
            throw new IllegalStateException("Variant is already inactive");
        }

        if (Boolean.TRUE.equals(this.isDefault) && product != null) {
            long activeVariants = product.getVariants().stream()
                .filter(v -> Boolean.TRUE.equals(v.getActive()))
                .count();

            if (activeVariants <= 1) {
                throw new IllegalStateException("Cannot deactivate the only active variant");
            }
        }

        this.active = false;

        registerEvent(VariantDeactivatedEvent.builder()
            .variantId(this.getId())
            .productId(this.product != null ? this.product.getId() : null)
            .sku(this.sku.getValue())
            .variantName(this.name)
            .reason(reason)
            .occurredAt(LocalDateTime.now())
            .build());
    }

    /**
     * Mark as default variant
     */
    public void markAsDefault() {
        if (Boolean.TRUE.equals(this.isDefault)) {
            return;
        }

        if (product != null) {
            product.getVariants().forEach(v -> {
                if (Boolean.TRUE.equals(v.getIsDefault())) {
                    v.setIsDefault(false);
                }
            });
        }

        this.isDefault = true;

        ChangeTracker tracker = new ChangeTracker();
        tracker.track("isDefault", false, true);
        registerEvent(new EntityUpdatedEvent<>(this, tracker.getChanges()));
    }

    // ====================================
    // Query Methods
    // ====================================

    /**
     * Check if variant is available for sale
     */
    @Transient
    public boolean isAvailable() {
        return Boolean.TRUE.equals(active) && isInStock();
    }

    /**
     * Check if variant is sellable
     */
    @Transient
    public boolean isSellable() {
        return Boolean.TRUE.equals(active) && 
               product != null && 
               product.getStatus() == ProductStatus.PUBLISHED;
    }

    /**
     * Get primary image
     */
    @Transient
    public String getPrimaryImage() {
        return imageUrls.isEmpty() ? null : imageUrls.get(0);
    }

    /**
     * Get formatted variant options (e.g., "Size: Large, Color: Red")
     */
    @Transient
    public String getFormattedOptions() {
        return optionValues.stream()
            .map(option -> option.getOptionName() + ": " + option.getValue())
            .reduce((a, b) -> a + ", " + b)
            .orElse("");
    }

    // ====================================
    // SoftDeletableAggregateRoot Implementation
    // ====================================

    @Override
    protected DomainEvent createDeletedEvent(String deletedBy) {
        return EntityDeletedEvent.builder()
            .entityClass(ProductVariant.class)
            .entityType("ProductVariant")
            .entityId(this.getId())
            .deletedBy(deletedBy)
            .softDelete(true)
            .reason("Variant soft-deleted: " + this.name)
            .occurredAt(LocalDateTime.now())
            .build();
    }

    @Override
    protected DomainEvent createRestoredEvent(String restoredBy, String previousDeletedBy) {
        return EntityRestoredEvent.builder()
            .entityClass(ProductVariant.class)
            .entityType("ProductVariant")
            .entityId(this.getId())
            .restoredBy(restoredBy)
            .previousDeletedBy(previousDeletedBy)
            .previousDeletedAt(this.getDeletedAt())
            .occurredAt(LocalDateTime.now())
            .build();
    }

    // ====================================
    // Object Overrides
    // ====================================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProductVariant)) return false;
        ProductVariant that = (ProductVariant) o;
        return sku != null && sku.equals(that.sku);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return String.format("ProductVariant[id=%s, sku=%s, name=%s, active=%s]",
            getId(), sku, name, active);
    }

    // ====================================
    // Nested Types
    // ====================================

    /**
     * Price Adjustment Type
     */
    public enum PriceAdjustmentType {
        FIXED,      // Fixed amount adjustment (e.g., +$10)
        PERCENTAGE  // Percentage adjustment (e.g., +20%)
    }
}