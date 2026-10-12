package com.example.demo.modules.kitchen.domain.model;

import com.example.demo.common.base.entity.SoftDeletableAggregateRoot;
import com.example.demo.common.event.DomainEvent;
import com.example.demo.common.event.EntityDeletedEvent;
import com.example.demo.common.event.EntityRestoredEvent;
import com.example.demo.common.event.EntityUpdatedEvent;
import com.example.demo.common.util.ChangeTracker;
import com.example.demo.modules.kitchen.domain.model.vo.AssemblyType;
import com.example.demo.modules.product.domain.model.vo.Money;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Kitchen Product Variant Entity
 *
 * The exact purchasable SKU — a specific combination of:
 *   - Cabinet colour (white, oak, anthracite)
 *   - Assembly type (flatpack, rigid)
 *
 * Examples:
 *   JP-SG-CASH-B150PO-WHT-FP  (White Cabinet, Flatpack)   £120
 *   JP-SG-CASH-B150PO-OAK-FP  (Oak Cabinet, Flatpack)     £130
 *   JP-SG-CASH-B150PO-WHT-RG  (White Cabinet, Rigid)      £145
 *
 * Business Rules:
 * - Must belong to a KitchenProduct
 * - SKU must be unique
 * - Cabinet colour must reference a KitchenColour
 * - Price includes cabinet colour and assembly premiums
 * - Cannot be activated if parent product is inactive
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
    name = "kitchen_product_variants",
    indexes = {
        @Index(name = "idx_kpv_product", columnList = "product_id"),
        @Index(name = "idx_kpv_sku", columnList = "sku"),
        @Index(name = "idx_kpv_cabinet_colour", columnList = "cabinet_colour_id"),
        @Index(name = "idx_kpv_active", columnList = "active")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_kpv_sku", columnNames = "sku")
    }
)
public class KitchenProductVariant extends SoftDeletableAggregateRoot {

    // ====================================
    // Core Information
    // ====================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private KitchenProduct product;

    @Column(name = "sku", nullable = false, length = 50)
    private String sku;

    @Column(name = "name", length = 300)
    private String name;              // "White Cabinet - Flatpack"

    // ====================================
    // Configuration Options
    // ====================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cabinet_colour_id", nullable = false)
    private KitchenColour cabinetColour;

    @Enumerated(EnumType.STRING)
    @Column(name = "assembly_type", nullable = false, length = 20)
    private AssemblyType assemblyType;

    // ====================================
    // Pricing
    // ====================================

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "amount", column = @Column(name = "price_amount", nullable = false, precision = 19, scale = 4)),
        @AttributeOverride(name = "currency", column = @Column(name = "price_currency", nullable = false, length = 3))
    })
    private Money price;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "amount", column = @Column(name = "cabinet_colour_premium_amount", precision = 19, scale = 4)),
        @AttributeOverride(name = "currency", column = @Column(name = "cabinet_colour_premium_currency", length = 3))
    })
    private Money cabinetColourPremium;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "amount", column = @Column(name = "assembly_premium_amount", precision = 19, scale = 4)),
        @AttributeOverride(name = "currency", column = @Column(name = "assembly_premium_currency", length = 3))
    })
    private Money assemblyPremium;

    // ====================================
    // Stock
    // ====================================

    @Column(name = "in_stock", nullable = false)
    @Builder.Default
    private Boolean inStock = true;

    @Column(name = "stock_quantity")
    private Integer stockQuantity;

    @Column(name = "lead_time_days")
    private Integer leadTimeDays;

    // ====================================
    // Status
    // ====================================

    @Column(name = "active", nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Column(name = "is_default", nullable = false)
    @Builder.Default
    private Boolean isDefault = false;

    @Column(name = "sort_order")
    @Builder.Default
    private Integer sortOrder = 0;

    // ====================================
    // Factory Methods
    // ====================================

    public static KitchenProductVariant create(
            KitchenProduct product,
            String sku,
            KitchenColour cabinetColour,
            AssemblyType assemblyType,
            Money price) {

        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null");
        }
        if (sku == null || sku.isBlank()) {
            throw new IllegalArgumentException("SKU cannot be null or empty");
        }
        if (cabinetColour == null) {
            throw new IllegalArgumentException("Cabinet colour cannot be null");
        }
        if (assemblyType == null) {
            throw new IllegalArgumentException("Assembly type cannot be null");
        }
        if (price == null) {
            throw new IllegalArgumentException("Price cannot be null");
        }

        String variantName = cabinetColour.getName() + " Cabinet - " + assemblyType.getDisplayName();

        return KitchenProductVariant.builder()
            .product(product)
            .sku(sku.toUpperCase())
            .name(variantName)
            .cabinetColour(cabinetColour)
            .assemblyType(assemblyType)
            .price(price)
            .active(true)
            .isDefault(false)
            .inStock(true)
            .sortOrder(0)
            .build();
    }

    // ====================================
    // Domain Logic - Pricing
    // ====================================

    public void updatePrice(Money newPrice) {
        if (newPrice == null) {
            throw new IllegalArgumentException("Price cannot be null");
        }
        ChangeTracker tracker = new ChangeTracker();
        tracker.track("price", this.price, newPrice);
        this.price = newPrice;
        if (tracker.hasChanges()) {
            registerEvent(new EntityUpdatedEvent<>(this, tracker.getChanges()));
        }
    }

    public void updatePremiums(Money cabinetColourPremium, Money assemblyPremium) {
        ChangeTracker tracker = new ChangeTracker();

        if (cabinetColourPremium != null) {
            tracker.track("cabinetColourPremium", this.cabinetColourPremium, cabinetColourPremium);
            this.cabinetColourPremium = cabinetColourPremium;
        }
        if (assemblyPremium != null) {
            tracker.track("assemblyPremium", this.assemblyPremium, assemblyPremium);
            this.assemblyPremium = assemblyPremium;
        }

        if (tracker.hasChanges()) {
            registerEvent(new EntityUpdatedEvent<>(this, tracker.getChanges()));
        }
    }

    // ====================================
    // Domain Logic - Stock
    // ====================================

    public void updateStock(boolean inStock, Integer quantity) {
        ChangeTracker tracker = new ChangeTracker();
        tracker.track("inStock", this.inStock, inStock);
        tracker.track("stockQuantity", this.stockQuantity, quantity);

        this.inStock = inStock;
        this.stockQuantity = quantity;

        if (tracker.hasChanges()) {
            registerEvent(new EntityUpdatedEvent<>(this, tracker.getChanges()));
        }
    }

    // ====================================
    // Domain Logic - Activation
    // ====================================

    public void activate() {
        if (Boolean.TRUE.equals(this.active)) {
            throw new IllegalStateException("Variant is already active");
        }
        if (!Boolean.TRUE.equals(product.getActive())) {
            throw new IllegalStateException("Cannot activate variant when parent product is inactive");
        }
        this.active = true;
        ChangeTracker tracker = new ChangeTracker();
        tracker.track("active", false, true);
        registerEvent(new EntityUpdatedEvent<>(this, tracker.getChanges()));
    }

    public void deactivate() {
        if (Boolean.FALSE.equals(this.active)) {
            throw new IllegalStateException("Variant is already inactive");
        }
        this.active = false;
        ChangeTracker tracker = new ChangeTracker();
        tracker.track("active", true, false);
        registerEvent(new EntityUpdatedEvent<>(this, tracker.getChanges()));
    }

    public void markAsDefault() {
        if (Boolean.TRUE.equals(this.isDefault)) return;

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

    @Transient
    public boolean isAvailable() {
        return Boolean.TRUE.equals(active) && Boolean.TRUE.equals(inStock);
    }

    @Transient
    public boolean isSellable() {
        return Boolean.TRUE.equals(active)
            && product != null
            && Boolean.TRUE.equals(product.getActive());
    }

    @Transient
    public String getCabinetColourName() {
        return cabinetColour != null ? cabinetColour.getName() : null;
    }

    // ====================================
    // SoftDeletableAggregateRoot
    // ====================================

    @Override
    protected DomainEvent createDeletedEvent(String deletedBy) {
        return EntityDeletedEvent.builder()
            .entityClass(KitchenProductVariant.class)
            .entityType("KitchenProductVariant")
            .entityId(this.getId())
            .deletedBy(deletedBy)
            .softDelete(true)
            .occurredAt(LocalDateTime.now())
            .build();
    }

    @Override
    protected DomainEvent createRestoredEvent(String restoredBy, String previousDeletedBy) {
        return EntityRestoredEvent.builder()
            .entityClass(KitchenProductVariant.class)
            .entityType("KitchenProductVariant")
            .entityId(this.getId())
            .restoredBy(restoredBy)
            .previousDeletedBy(previousDeletedBy)
            .previousDeletedAt(this.getDeletedAt())
            .occurredAt(LocalDateTime.now())
            .build();
    }

    @Override
    public String toString() {
        return String.format("KitchenProductVariant[id=%s, sku=%s, name=%s, price=%s, active=%s]",
            getId(), sku, name, price, active);
    }
}