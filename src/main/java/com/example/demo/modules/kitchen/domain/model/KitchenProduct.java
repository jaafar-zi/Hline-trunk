package com.example.demo.modules.kitchen.domain.model;

import com.example.demo.common.base.entity.SoftDeletableAggregateRoot;
import com.example.demo.common.event.DomainEvent;
import com.example.demo.common.event.EntityDeletedEvent;
import com.example.demo.common.event.EntityRestoredEvent;
import com.example.demo.common.event.EntityUpdatedEvent;
import com.example.demo.common.util.ChangeTracker;
import com.example.demo.modules.product.domain.model.vo.Money;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Kitchen Product Entity
 *
 * An independently purchasable cabinet/unit within a kitchen collection.
 * Belongs to a KitchenCollection AND a KitchenUnitCategory.
 *
 * Examples:
 *   J-Pull SuperGloss Cashmere 150 Base Pull Out Cabinet
 *   J-Pull SuperGloss Cashmere 600 Wall Double Door Cabinet
 *
 * Business Rules:
 * - Must belong to a KitchenCollection
 * - Must belong to a KitchenUnitCategory
 * - SKU must be unique
 * - Can be purchased standalone
 * - Has multiple variants (cabinet colour × assembly type)
 * - Base price = cheapest variant price
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
    name = "kitchen_products",
    indexes = {
        @Index(name = "idx_kp_collection", columnList = "collection_id"),
        @Index(name = "idx_kp_category", columnList = "unit_category_id"),
        @Index(name = "idx_kp_sku", columnList = "sku"),
        @Index(name = "idx_kp_slug", columnList = "slug"),
        @Index(name = "idx_kp_active", columnList = "active"),
        @Index(name = "idx_kp_popular", columnList = "is_popular")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_kp_sku", columnNames = "sku")
    }
)
public class KitchenProduct extends SoftDeletableAggregateRoot {

    // ====================================
    // Core Information
    // ====================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collection_id", nullable = false)
    private KitchenCollection collection;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unit_category_id", nullable = false)
    private KitchenUnitCategory unitCategory;

    @Column(name = "sku", nullable = false, length = 50)
    private String sku;

    @Column(name = "name", nullable = false, length = 300)
    private String name;              // "150 Base Pull Out Cabinet"

    @Column(name = "slug", nullable = false, length = 350)
    private String slug;              // "150-base-pull-out"

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "short_description", length = 500)
    private String shortDescription;

    // ====================================
    // Dimensions
    // ====================================

    @Column(name = "width_mm")
    private Integer widthMm;

    @Column(name = "height_mm")
    private Integer heightMm;

    @Column(name = "depth_mm")
    private Integer depthMm;

    // ====================================
    // Pricing
    // ====================================

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "amount", column = @Column(name = "base_price_amount", precision = 19, scale = 4)),
        @AttributeOverride(name = "currency", column = @Column(name = "base_price_currency", length = 3))
    })
    private Money basePrice;

    // ====================================
    // Status & Flags
    // ====================================

    @Column(name = "active", nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Column(name = "is_popular", nullable = false)
    @Builder.Default
    private Boolean isPopular = false;

    @Column(name = "standalone_orderable", nullable = false)
    @Builder.Default
    private Boolean standaloneOrderable = true;

    @Column(name = "sort_order")
    @Builder.Default
    private Integer sortOrder = 0;

    // ====================================
    // SEO
    // ====================================

    @Column(name = "meta_title", length = 255)
    private String metaTitle;

    @Column(name = "meta_description", length = 500)
    private String metaDescription;

    // ====================================
    // Relationships
    // ====================================

    @OneToMany(
        mappedBy = "product",
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    @Builder.Default
    private Set<KitchenProductVariant> variants = new HashSet<>();

    @OneToMany(
        mappedBy = "product",
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    @OrderBy("sortOrder ASC")
    @Builder.Default
    private Set<KitchenProductImage> images = new HashSet<>();

    // ====================================
    // Factory Methods
    // ====================================

    public static KitchenProduct create(
            KitchenCollection collection,
            KitchenUnitCategory unitCategory,
            String sku,
            String name,
            Integer widthMm,
            Integer heightMm,
            Integer depthMm,
            Money basePrice) {

        if (collection == null) {
            throw new IllegalArgumentException("Collection cannot be null");
        }
        if (unitCategory == null) {
            throw new IllegalArgumentException("Unit category cannot be null");
        }
        if (sku == null || sku.isBlank()) {
            throw new IllegalArgumentException("SKU cannot be null or empty");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name cannot be null or empty");
        }
        if (basePrice == null) {
            throw new IllegalArgumentException("Base price cannot be null");
        }

        return KitchenProduct.builder()
            .collection(collection)
            .unitCategory(unitCategory)
            .sku(sku.toUpperCase())
            .name(name)
            .slug(generateSlug(name))
            .widthMm(widthMm)
            .heightMm(heightMm)
            .depthMm(depthMm)
            .basePrice(basePrice)
            .active(true)
            .isPopular(false)
            .standaloneOrderable(true)
            .sortOrder(0)
            .build();
    }

    // ====================================
    // Domain Logic - Information
    // ====================================

    public void updateBasicInfo(String name, String shortDescription, String description) {
        ChangeTracker tracker = new ChangeTracker();

        if (name != null && !name.equals(this.name)) {
            tracker.track("name", this.name, name);
            this.name = name;
            this.slug = generateSlug(name);
        }
        if (shortDescription != null && !shortDescription.equals(this.shortDescription)) {
            tracker.track("shortDescription", this.shortDescription, shortDescription);
            this.shortDescription = shortDescription;
        }
        if (description != null && !description.equals(this.description)) {
            tracker.track("description", this.description, description);
            this.description = description;
        }

        if (tracker.hasChanges()) {
            registerEvent(new EntityUpdatedEvent<>(this, tracker.getChanges()));
        }
    }

    public void updateDimensions(Integer widthMm, Integer heightMm, Integer depthMm) {
        ChangeTracker tracker = new ChangeTracker();
        tracker.track("widthMm", this.widthMm, widthMm);
        tracker.track("heightMm", this.heightMm, heightMm);
        tracker.track("depthMm", this.depthMm, depthMm);

        this.widthMm = widthMm;
        this.heightMm = heightMm;
        this.depthMm = depthMm;

        if (tracker.hasChanges()) {
            registerEvent(new EntityUpdatedEvent<>(this, tracker.getChanges()));
        }
    }

    public void updateBasePrice(Money newPrice) {
        if (newPrice == null) {
            throw new IllegalArgumentException("Price cannot be null");
        }
        ChangeTracker tracker = new ChangeTracker();
        tracker.track("basePrice", this.basePrice, newPrice);
        this.basePrice = newPrice;
        if (tracker.hasChanges()) {
            registerEvent(new EntityUpdatedEvent<>(this, tracker.getChanges()));
        }
    }

    // ====================================
    // Domain Logic - Flags
    // ====================================

    public void markAsPopular() {
        if (Boolean.TRUE.equals(this.isPopular)) return;
        this.isPopular = true;
        ChangeTracker tracker = new ChangeTracker();
        tracker.track("isPopular", false, true);
        registerEvent(new EntityUpdatedEvent<>(this, tracker.getChanges()));
    }

    public void unmarkAsPopular() {
        if (Boolean.FALSE.equals(this.isPopular)) return;
        this.isPopular = false;
        ChangeTracker tracker = new ChangeTracker();
        tracker.track("isPopular", true, false);
        registerEvent(new EntityUpdatedEvent<>(this, tracker.getChanges()));
    }

    // ====================================
    // Domain Logic - Variants
    // ====================================

    public void addVariant(KitchenProductVariant variant) {
        if (variant == null) {
            throw new IllegalArgumentException("Variant cannot be null");
        }
        variant.setProduct(this);
        this.variants.add(variant);

        EntityUpdatedEvent<KitchenProduct> event = new EntityUpdatedEvent<>(this);
        event.addChange("variants", null, "Added variant: " + variant.getSku());
        registerEvent(event);
    }

    public void removeVariant(String variantSku) {
        KitchenProductVariant variant = variants.stream()
            .filter(v -> v.getSku().equals(variantSku))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Variant not found: " + variantSku));

        this.variants.remove(variant);

        EntityUpdatedEvent<KitchenProduct> event = new EntityUpdatedEvent<>(this);
        event.addChange("variants", "Removed variant: " + variantSku, null);
        registerEvent(event);
    }

    public boolean hasVariants() {
        return variants != null && !variants.isEmpty();
    }

    // ====================================
    // Domain Logic - Images
    // ====================================

    public void addImage(KitchenProductImage image) {
        if (image == null) {
            throw new IllegalArgumentException("Image cannot be null");
        }
        image.setProduct(this);
        this.images.add(image);
    }

    public void removeImage(UUID imageId) {
        KitchenProductImage image = images.stream()
            .filter(img -> img.getId().equals(imageId))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Image not found: " + imageId));
        this.images.remove(image);
    }

    @Transient
    public KitchenProductImage getPrimaryImage() {
        return images.stream()
            .filter(KitchenProductImage::getIsPrimary)
            .findFirst()
            .orElse(null);
    }

    // ====================================
    // Domain Logic - Activation
    // ====================================

    public void activate() {
        if (Boolean.TRUE.equals(this.active)) {
            throw new IllegalStateException("Product is already active");
        }
        if (!Boolean.TRUE.equals(collection.getActive())) {
            throw new IllegalStateException("Cannot activate product when collection is inactive");
        }
        this.active = true;
        ChangeTracker tracker = new ChangeTracker();
        tracker.track("active", false, true);
        registerEvent(new EntityUpdatedEvent<>(this, tracker.getChanges()));
    }

    public void deactivate() {
        if (Boolean.FALSE.equals(this.active)) {
            throw new IllegalStateException("Product is already inactive");
        }
        this.active = false;
        ChangeTracker tracker = new ChangeTracker();
        tracker.track("active", true, false);
        registerEvent(new EntityUpdatedEvent<>(this, tracker.getChanges()));
    }

    // ====================================
    // Transient Helpers
    // ====================================

    @Transient
    public String getFormattedDimensions() {
        if (widthMm == null && heightMm == null && depthMm == null) {
            return null;
        }
        return String.format("%d x %d x %d mm",
            widthMm != null ? widthMm : 0,
            heightMm != null ? heightMm : 0,
            depthMm != null ? depthMm : 0);
    }

    @Transient
    public String getRangeName() {
        return collection != null && collection.getRange() != null
            ? collection.getRange().getName()
            : null;
    }

    @Transient
    public String getCollectionName() {
        return collection != null ? collection.getName() : null;
    }

    @Transient
    public String getUnitCategoryName() {
        return unitCategory != null ? unitCategory.getName() : null;
    }

    // ====================================
    // Helper
    // ====================================

    private static String generateSlug(String name) {
        return name.toLowerCase()
            .replaceAll("[^a-z0-9\\s-]", "")
            .replaceAll("\\s+", "-")
            .replaceAll("-+", "-")
            .trim();
    }

    // ====================================
    // SoftDeletableAggregateRoot
    // ====================================

    @Override
    protected DomainEvent createDeletedEvent(String deletedBy) {
        return EntityDeletedEvent.builder()
            .entityClass(KitchenProduct.class)
            .entityType("KitchenProduct")
            .entityId(this.getId())
            .deletedBy(deletedBy)
            .softDelete(true)
            .occurredAt(LocalDateTime.now())
            .build();
    }

    @Override
    protected DomainEvent createRestoredEvent(String restoredBy, String previousDeletedBy) {
        return EntityRestoredEvent.builder()
            .entityClass(KitchenProduct.class)
            .entityType("KitchenProduct")
            .entityId(this.getId())
            .restoredBy(restoredBy)
            .previousDeletedBy(previousDeletedBy)
            .previousDeletedAt(this.getDeletedAt())
            .occurredAt(LocalDateTime.now())
            .build();
    }

    @Override
    public String toString() {
        return String.format("KitchenProduct[id=%s, sku=%s, name=%s, active=%s]",
            getId(), sku, name, active);
    }
}