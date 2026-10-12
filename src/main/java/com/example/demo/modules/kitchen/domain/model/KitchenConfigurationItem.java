package com.example.demo.modules.kitchen.domain.model;

import com.example.demo.common.base.entity.AuditableAggregateRoot;
import com.example.demo.common.event.EntityUpdatedEvent;
import com.example.demo.common.util.ChangeTracker;
import com.example.demo.modules.product.domain.model.vo.Money;
import jakarta.persistence.*;
import lombok.*;

/**
 * Kitchen Configuration Item Entity
 *
 * A single unit within a kitchen configuration.
 * References a product and a specific variant (colour + assembly).
 *
 * Business Rules:
 * - Must belong to a KitchenConfiguration
 * - Must reference a KitchenProduct and KitchenProductVariant
 * - Quantity must be positive
 * - Variant can be overridden per-item (different from global colour)
 * - Unit price is captured from variant at time of adding
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
    name = "kitchen_configuration_items",
    indexes = {
        @Index(name = "idx_kci_config", columnList = "configuration_id"),
        @Index(name = "idx_kci_product", columnList = "product_id"),
        @Index(name = "idx_kci_variant", columnList = "variant_id")
    }
)
public class KitchenConfigurationItem extends AuditableAggregateRoot {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "configuration_id", nullable = false)
    private KitchenConfiguration configuration;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private KitchenProduct product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id", nullable = false)
    private KitchenProductVariant variant;

    @Column(name = "label", length = 200)
    private String label;                     // "Left of sink", "Under hob"

    @Column(name = "position")
    @Builder.Default
    private Integer position = 0;

    @Column(name = "quantity", nullable = false)
    @Builder.Default
    private Integer quantity = 1;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "amount", column = @Column(name = "unit_price_amount", precision = 19, scale = 4)),
        @AttributeOverride(name = "currency", column = @Column(name = "unit_price_currency", length = 3))
    })
    private Money unitPrice;

    /**
     * If true, this item uses a colour different from the global selection.
     */
    @Column(name = "colour_overridden", nullable = false)
    @Builder.Default
    private Boolean colourOverridden = false;

    // ====================================
    // Factory Methods
    // ====================================

    public static KitchenConfigurationItem create(
            KitchenConfiguration configuration,
            KitchenProduct product,
            KitchenProductVariant variant,
            int quantity,
            int position) {

        return KitchenConfigurationItem.builder()
            .configuration(configuration)
            .product(product)
            .variant(variant)
            .quantity(quantity)
            .position(position)
            .unitPrice(variant.getPrice())
            .colourOverridden(false)
            .build();
    }

    // ====================================
    // Domain Logic
    // ====================================

    public void updateQuantity(int newQuantity) {
        if (newQuantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        ChangeTracker tracker = new ChangeTracker();
        tracker.track("quantity", this.quantity, newQuantity);
        this.quantity = newQuantity;
        if (tracker.hasChanges()) {
            registerEvent(new EntityUpdatedEvent<>(this, tracker.getChanges()));
        }
    }

    public void overrideVariant(KitchenProductVariant newVariant) {
        if (newVariant == null) {
            throw new IllegalArgumentException("Variant cannot be null");
        }
        ChangeTracker tracker = new ChangeTracker();
        tracker.track("variant", this.variant.getSku(), newVariant.getSku());
        tracker.track("unitPrice", this.unitPrice, newVariant.getPrice());

        this.variant = newVariant;
        this.unitPrice = newVariant.getPrice();
        this.colourOverridden = true;

        if (tracker.hasChanges()) {
            registerEvent(new EntityUpdatedEvent<>(this, tracker.getChanges()));
        }
    }

    /**
     * Reset to use the global colour (remove override)
     */
    public void resetToGlobalColour(KitchenProductVariant globalVariant) {
        if (globalVariant == null) {
            throw new IllegalArgumentException("Variant cannot be null");
        }
        this.variant = globalVariant;
        this.unitPrice = globalVariant.getPrice();
        this.colourOverridden = false;
    }

    public void updateLabel(String label) {
        this.label = label;
    }

    // ====================================
    // Calculated
    // ====================================

    @Transient
    public Money getLineTotal() {
        if (unitPrice == null) {
            return Money.zero("GBP");
        }
        return unitPrice.multiplyBy(quantity);
    }

    @Transient
    public String getProductName() {
        return product != null ? product.getName() : null;
    }

    @Transient
    public String getVariantName() {
        return variant != null ? variant.getName() : null;
    }
}