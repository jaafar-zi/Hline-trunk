package com.example.demo.modules.product.domain.model;

import com.example.demo.common.base.entity.SoftDeletableAggregateRoot;
import com.example.demo.common.event.DomainEvent;
import com.example.demo.common.event.EntityDeletedEvent;
import com.example.demo.common.event.EntityRestoredEvent;
import com.example.demo.common.event.EntityUpdatedEvent;
import com.example.demo.common.util.ChangeTracker;
import com.example.demo.modules.product.domain.event.ProductDiscontinuedEvent;
import com.example.demo.modules.product.domain.event.ProductPublishedEvent;
import com.example.demo.modules.product.domain.event.ProductUnpublishedEvent;
import com.example.demo.modules.product.domain.model.vo.Dimensions;
import com.example.demo.modules.product.domain.model.vo.ImageType;
import com.example.demo.modules.product.domain.model.vo.Money;
import com.example.demo.modules.product.domain.model.vo.PriceType;
import com.example.demo.modules.product.domain.model.vo.ProductStatus;
import com.example.demo.modules.product.domain.model.vo.SKU;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Product Aggregate Root
 *
 * Core entity in PIM system representing a sellable product
 * Manages product information, pricing, inventory, attributes, and media
 *
 * Business Rules:
 * - Product must have valid SKU
 * - Must belong to a category
 * - Must have at least one price to be published
 * - Must have all required attributes filled
 * - Must have at least one image to be published
 * - Stock tracking is optional (for digital products)
 *
 * Events Published:
 * - EntityCreatedEvent<Product> (via AbstractCommandService after save)
 * - EntityUpdatedEvent<Product> (with change tracking for all updates)
 * - ProductPublishedEvent (business-critical)
 * - ProductUnpublishedEvent (business-critical)
 * - ProductDiscontinuedEvent (business-critical)
 * - EntityDeletedEvent (soft delete)
 * - EntityRestoredEvent (restore)
 *
 * @author PIM Team
 * @version 1.0
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "products",
        indexes = {
                @Index(name = "idx_product_sku", columnList = "sku"),
                @Index(name = "idx_product_status", columnList = "status"),
                @Index(name = "idx_product_category", columnList = "category_id"),
                @Index(name = "idx_product_created_at", columnList = "created_at"),
                @Index(name = "idx_product_published_at", columnList = "published_at")
        }
)
public class Product extends SoftDeletableAggregateRoot {

    // ====================================
    // Core Product Information
    // ====================================

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "sku", unique = true, nullable = false, length = 50))
    private SKU sku;

    @Column(name = "name", nullable = false, length = 500)
    private String name;

    @Column(name = "short_description", length = 1000)
    private String shortDescription;

    @Column(name = "long_description", columnDefinition = "TEXT")
    private String longDescription;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    @Builder.Default
    private ProductStatus status = ProductStatus.DRAFT;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(name = "brand", length = 255)
    private String brand;

    @Column(name = "manufacturer", length = 255)
    private String manufacturer;

    @Embedded
    private Dimensions dimensions;

    @Column(name = "url_slug", unique = true, length = 500)
    private String urlSlug;

    @Column(name = "meta_title", length = 255)
    private String metaTitle;

    @Column(name = "meta_description", length = 500)
    private String metaDescription;

    @Column(name = "search_keywords", length = 1000)
    private String searchKeywords;

    // ====================================
    // Product Lifecycle
    // ====================================

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "discontinued_at")
    private LocalDateTime discontinuedAt;

    @Column(name = "available_from")
    private LocalDateTime availableFrom;

    @Column(name = "available_until")
    private LocalDateTime availableUntil;

    // ====================================
    // Relationships (Aggregate Components)
    // ====================================

    @OneToMany(
            mappedBy = "product",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @Builder.Default
    private Set<ProductVariant> variants = new HashSet<>();

    @OneToMany(
            mappedBy = "product",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.EAGER
    )
    @OrderBy("displayOrder ASC, key ASC")
    @Builder.Default
    private Set<ProductAttribute> attributes = new HashSet<>();

    @OneToMany(
            mappedBy = "product",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @OrderBy("displayOrder ASC, createdAt ASC")
    @Builder.Default
    private Set<ProductImage> images = new HashSet<>();

    @OneToMany(
            mappedBy = "product",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @OrderBy("priority DESC, createdAt DESC")
    @Builder.Default
    private Set<ProductPrice> prices = new HashSet<>();

    @OneToOne(
            mappedBy = "product",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private ProductInventory inventory;

    // ====================================
    // Factory Methods
    // ====================================

    /**
     * Create new product in draft status
     *
     * Events Published:
     * - EntityCreatedEvent<Product> (via AbstractCommandService after save)
     */
    public static Product create(SKU sku, String name, Category category) {
        if (sku == null) {
            throw new IllegalArgumentException("SKU cannot be null");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Product name cannot be null or empty");
        }
        if (category == null) {
            throw new IllegalArgumentException("Category cannot be null");
        }

        return Product.builder()
                .sku(sku)
                .name(name)
                .category(category)
                .status(ProductStatus.DRAFT)
                .urlSlug(generateSlug(name, sku))
                .build();
        // No event registration - handled by AbstractCommandService
    }

    public static Product create(String name, Category category) {
        return create(SKU.generate(), name, category);
    }

    public static Product createWithCategoryPrefix(String name, Category category) {
        String prefix = category.getCode() != null ? category.getCode() : "PROD";
        return create(SKU.generate(prefix), name, category);
    }

    // ====================================
    // Domain Logic - Basic Information
    // ====================================

    /**
     * Update basic product information
     *
     * Events Published:
     * - EntityUpdatedEvent<Product> (with change tracking)
     */
    public void updateBasicInfo(String name, String shortDescription, String longDescription) {
        ChangeTracker tracker = new ChangeTracker();

        if (name != null && !name.equals(this.name)) {
            tracker.track("name", this.name, name);
            this.name = name;
            this.urlSlug = generateSlug(name, this.sku);
        }

        if (shortDescription != null && !shortDescription.equals(this.shortDescription)) {
            tracker.track("shortDescription", this.shortDescription, shortDescription);
            this.shortDescription = shortDescription;
        }

        if (longDescription != null && !longDescription.equals(this.longDescription)) {
            tracker.track("longDescription", this.longDescription, longDescription);
            this.longDescription = longDescription;
        }

        if (tracker.hasChanges()) {
            registerEvent(new EntityUpdatedEvent<>(this, tracker.getChanges()));
        }
    }

    /**
     * Update branding information
     */
    public void updateBranding(String brand, String manufacturer) {
        ChangeTracker tracker = new ChangeTracker();

        tracker.track("brand", this.brand, brand);
        tracker.track("manufacturer", this.manufacturer, manufacturer);

        this.brand = brand;
        this.manufacturer = manufacturer;

        if (tracker.hasChanges()) {
            registerEvent(new EntityUpdatedEvent<>(this, tracker.getChanges()));
        }
    }

    /**
     * Update dimensions
     */
    public void updateDimensions(Dimensions dimensions) {
        if (!Objects.equals(this.dimensions, dimensions)) {
            ChangeTracker tracker = new ChangeTracker();
            tracker.track("dimensions", this.dimensions, dimensions);

            this.dimensions = dimensions;

            registerEvent(new EntityUpdatedEvent<>(this, tracker.getChanges()));
        }
    }

    /**
     * Update SEO information
     */
    public void updateSEO(String metaTitle, String metaDescription, String searchKeywords) {
        ChangeTracker tracker = new ChangeTracker();

        tracker.track("metaTitle", this.metaTitle, metaTitle);
        tracker.track("metaDescription", this.metaDescription, metaDescription);
        tracker.track("searchKeywords", this.searchKeywords, searchKeywords);

        this.metaTitle = metaTitle;
        this.metaDescription = metaDescription;
        this.searchKeywords = searchKeywords;

        if (tracker.hasChanges()) {
            registerEvent(new EntityUpdatedEvent<>(this, tracker.getChanges()));
        }
    }

    /**
     * Change category
     */
    public void changeCategory(Category newCategory) {
        if (newCategory == null) {
            throw new IllegalArgumentException("Category cannot be null");
        }

        if (!Objects.equals(this.category, newCategory)) {
            Category oldCategory = this.category;
            this.category = newCategory;

            ChangeTracker tracker = new ChangeTracker();
            tracker.track("category",
                    oldCategory.getName(),
                    newCategory.getName());

            registerEvent(new EntityUpdatedEvent<>(this, tracker.getChanges()));
        }
    }

    // ====================================
    // Domain Logic - Attributes
    // ====================================

    /**
     * Add attribute
     *
     * Events Published:
     * - EntityUpdatedEvent<Product> (with change tracking)
     */
    public void addAttribute(ProductAttribute attribute) {
        if (attribute == null) {
            throw new IllegalArgumentException("Attribute cannot be null");
        }

        if (hasAttribute(attribute.getKey())) {
            throw new IllegalStateException("Attribute already exists: " + attribute.getKey());
        }

        attribute.setProduct(this);
        this.attributes.add(attribute);

        EntityUpdatedEvent<Product> event = new EntityUpdatedEvent<>(this);
        event.addChange("attributes", null, "Added attribute: " + attribute.getKey());
        registerEvent(event);
    }

    /**
     * Remove attribute
     */
    public void removeAttribute(String key) {
        ProductAttribute attribute = getAttribute(key);
        if (attribute == null) {
            throw new IllegalArgumentException("Attribute not found: " + key);
        }

        if (Boolean.TRUE.equals(attribute.getRequired())) {
            throw new IllegalStateException("Cannot remove required attribute: " + key);
        }

        this.attributes.remove(attribute);

        EntityUpdatedEvent<Product> event = new EntityUpdatedEvent<>(this);
        event.addChange("attributes", "Removed attribute: " + key, null);
        registerEvent(event);
    }

    /**
     * Update attribute value
     */
    public void updateAttribute(String key, String value) {
        ProductAttribute attribute = getAttribute(key);
        if (attribute == null) {
            throw new IllegalArgumentException("Attribute not found: " + key);
        }

        String oldValue = attribute.getValue();
        attribute.updateValue(value);

        ChangeTracker tracker = new ChangeTracker();
        tracker.track("attributes." + key, oldValue, value);
        registerEvent(new EntityUpdatedEvent<>(this, tracker.getChanges()));
    }

    public ProductAttribute getAttribute(String key) {
        return attributes.stream()
                .filter(attr -> attr.getKey().equals(key))
                .findFirst()
                .orElse(null);
    }

    public String getAttributeValue(String key) {
        ProductAttribute attribute = getAttribute(key);
        return attribute != null ? attribute.getValue() : null;
    }

    public boolean hasAttribute(String key) {
        return getAttribute(key) != null;
    }

    @Transient
    public Set<ProductAttribute> getRequiredAttributes() {
        return attributes.stream()
                .filter(attr -> Boolean.TRUE.equals(attr.getRequired()))
                .collect(Collectors.toSet());
    }

    public boolean hasAllRequiredAttributes() {
        return getRequiredAttributes().stream()
                .allMatch(attr -> attr.getValue() != null && !attr.getValue().isBlank());
    }

    // ====================================
    // Domain Logic - Images/Media
    // ====================================

    /**
     * Add image
     *
     * Events Published:
     * - EntityUpdatedEvent<Product> (with change tracking)
     */
    public void addImage(ProductImage image) {
        if (image == null) {
            throw new IllegalArgumentException("Image cannot be null");
        }

        image.setProduct(this);
        this.images.add(image);

        EntityUpdatedEvent<Product> event = new EntityUpdatedEvent<>(this);
        event.addChange("images", null, "Added image: " + image.getFileName());
        registerEvent(event);
    }

    /**
     * Remove image
     */
    public void removeImage(UUID imageId) {
        ProductImage image = images.stream()
                .filter(img -> img.getId().equals(imageId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Image not found: " + imageId));

        this.images.remove(image);

        EntityUpdatedEvent<Product> event = new EntityUpdatedEvent<>(this);
        event.addChange("images", "Removed image: " + image.getFileName(), null);
        registerEvent(event);
    }

    /**
     * Set primary image
     */
    public void setPrimaryImage(UUID imageId) {
        images.forEach(img -> {
            if (img.getImageType() == ImageType.PRIMARY) {
                img.setImageType(ImageType.GALLERY);
            }
        });

        ProductImage image = images.stream()
                .filter(img -> img.getId().equals(imageId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Image not found: " + imageId));

        image.setImageType(ImageType.PRIMARY);

        EntityUpdatedEvent<Product> event = new EntityUpdatedEvent<>(this);
        event.addChange("primaryImage", null, image.getFileName());
        registerEvent(event);
    }

    @Transient
    public ProductImage getPrimaryImage() {
        return images.stream()
                .filter(img -> img.getImageType() == ImageType.PRIMARY)
                .findFirst()
                .orElse(null);
    }

    public boolean hasImages() {
        return images != null && !images.isEmpty();
    }

    // ====================================
    // Domain Logic - Pricing
    // ====================================

    /**
     * Add price
     *
     * Events Published:
     * - EntityUpdatedEvent<Product> (with change tracking)
     */
    public void addPrice(ProductPrice price) {
        if (price == null) {
            throw new IllegalArgumentException("Price cannot be null");
        }

        price.setProduct(this);
        this.prices.add(price);

        EntityUpdatedEvent<Product> event = new EntityUpdatedEvent<>(this);
        event.addChange("prices", null,
                String.format("Added %s price: %s", price.getPriceType(), price.getPrice()));
        registerEvent(event);
    }

    /**
     * Update price
     */
    public void updatePrice(UUID priceId, Money newPrice) {
        ProductPrice price = prices.stream()
                .filter(p -> p.getId().equals(priceId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Price not found: " + priceId));

        Money oldPrice = price.getPrice();
        price.setPrice(newPrice);

        ChangeTracker tracker = new ChangeTracker();
        tracker.track("price." + price.getPriceType(), oldPrice, newPrice);
        registerEvent(new EntityUpdatedEvent<>(this, tracker.getChanges()));
    }

    @Transient
    public ProductPrice getCurrentPrice() {
        return prices.stream()
                .filter(p -> p.getPriceType() == PriceType.BASE)
                .filter(ProductPrice::isActive)
                .findFirst()
                .orElse(null);
    }

    @Transient
    public ProductPrice getPromotionalPrice() {
        return prices.stream()
                .filter(p -> p.getPriceType() == PriceType.PROMOTIONAL)
                .filter(ProductPrice::isActive)
                .findFirst()
                .orElse(null);
    }

    public boolean hasValidPrice() {
        return getCurrentPrice() != null;
    }

    // ====================================
    // Domain Logic - Inventory
    // ====================================

    /**
     * Set inventory
     */
    public void setInventory(ProductInventory inventory) {
        if (inventory != null) {
            inventory.setProduct(this);
        }
        this.inventory = inventory;
    }

    /**
     * Update stock quantity
     *
     * Events Published:
     * - EntityUpdatedEvent<Product> (with change tracking)
     */
    public void updateStock(int quantity, String reason) {
        if (this.inventory == null) {
            throw new IllegalStateException("Product has no inventory tracking");
        }

        int oldQuantity = this.inventory.getStockLevel().getQuantity();
        this.inventory.setQuantity(quantity);

        ChangeTracker tracker = new ChangeTracker();
        tracker.track("stock.quantity", oldQuantity, quantity);
        tracker.track("stock.reason", null, reason);
        registerEvent(new EntityUpdatedEvent<>(this, tracker.getChanges()));
    }

    @Transient
    public boolean isInStock() {
        return inventory != null && inventory.isInStock();
    }

    @Transient
    public boolean isLowStock() {
        return inventory != null && inventory.isLowStock();
    }

    // ====================================
    // Domain Logic - Variants
    // ====================================

    /**
     * Add variant
     */
    public void addVariant(ProductVariant variant) {
        if (variant == null) {
            throw new IllegalArgumentException("Variant cannot be null");
        }

        variant.setProduct(this);
        this.variants.add(variant);

        EntityUpdatedEvent<Product> event = new EntityUpdatedEvent<>(this);
        event.addChange("variants", null, "Added variant: " + variant.getSku().getValue());
        registerEvent(event);
    }

    /**
     * Remove variant
     */
    public void removeVariant(SKU variantSku) {
        ProductVariant variant = variants.stream()
                .filter(v -> v.getSku().equals(variantSku))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Variant not found: " + variantSku));

        this.variants.remove(variant);

        EntityUpdatedEvent<Product> event = new EntityUpdatedEvent<>(this);
        event.addChange("variants", "Removed variant: " + variantSku, null);
        registerEvent(event);
    }

    public boolean hasVariants() {
        return variants != null && !variants.isEmpty();
    }

    // ====================================
    // Domain Logic - Publishing
    // ====================================

    /**
     * Publish product
     *
     * Events Published:
     * - ProductPublishedEvent (domain-specific, business-critical)
     */
    public void publish() {
        validatePublishing();

        if (this.status == ProductStatus.PUBLISHED) {
            throw new IllegalStateException("Product is already published");
        }

        ProductStatus oldStatus = this.status;
        this.status = ProductStatus.PUBLISHED;
        this.publishedAt = LocalDateTime.now();

        registerEvent(ProductPublishedEvent.builder()
                .productId(this.getId())
                .sku(this.sku.getValue())
                .productName(this.name)
                .categoryId(this.category.getId())
                .categoryName(this.category.getName())
                .publishedBy(getCurrentUsername())
                .previousStatus(oldStatus)
                .occurredAt(LocalDateTime.now())
                .build());
    }

    /**
     * Unpublish product
     *
     * Events Published:
     * - ProductUnpublishedEvent (domain-specific)
     */
    public void unpublish(String reason) {
        if (this.status != ProductStatus.PUBLISHED) {
            throw new IllegalStateException("Product is not published");
        }

        this.status = ProductStatus.UNPUBLISHED;

        registerEvent(ProductUnpublishedEvent.builder()
                .productId(this.getId())
                .sku(this.sku.getValue())
                .productName(this.name)
                .reason(reason)
                .unpublishedBy(getCurrentUsername())
                .occurredAt(LocalDateTime.now())
                .build());
    }

    /**
     * Mark as ready for publishing
     */
    public void markAsReady() {
        validatePublishing();

        if (this.status != ProductStatus.DRAFT && this.status != ProductStatus.UNPUBLISHED) {
            throw new IllegalStateException("Can only mark draft or unpublished products as ready");
        }

        ChangeTracker tracker = new ChangeTracker();
        tracker.track("status", this.status, ProductStatus.READY);

        this.status = ProductStatus.READY;

        registerEvent(new EntityUpdatedEvent<>(this, tracker.getChanges()));
    }

    /**
     * Discontinue product
     *
     * Events Published:
     * - ProductDiscontinuedEvent (domain-specific)
     */
    public void discontinue(String reason) {
        if (this.status == ProductStatus.DISCONTINUED) {
            throw new IllegalStateException("Product is already discontinued");
        }

        ProductStatus oldStatus = this.status;
        this.status = ProductStatus.DISCONTINUED;
        this.discontinuedAt = LocalDateTime.now();

        registerEvent(ProductDiscontinuedEvent.builder()
                .productId(this.getId())
                .sku(this.sku.getValue())
                .productName(this.name)
                .reason(reason)
                .previousStatus(oldStatus)
                .discontinuedBy(getCurrentUsername())
                .occurredAt(LocalDateTime.now())
                .build());
    }

    /**
     * Validate product can be published
     */
    public void validatePublishing() {
        List<String> errors = new ArrayList<>();

        if (!hasValidPrice()) {
            errors.add("Product must have at least one valid price");
        }

        if (!hasAllRequiredAttributes()) {
            Set<String> missingAttrs = getRequiredAttributes().stream()
                    .filter(attr -> attr.getValue() == null || attr.getValue().isBlank())
                    .map(ProductAttribute::getKey)
                    .collect(Collectors.toSet());
            errors.add("Missing required attributes: " + String.join(", ", missingAttrs));
        }

        if (!hasImages()) {
            errors.add("Product must have at least one image");
        }

        if (!errors.isEmpty()) {
            throw new IllegalStateException(
                    "Product cannot be published: " + String.join("; ", errors)
            );
        }
    }

    public boolean canBePublished() {
        try {
            validatePublishing();
            return true;
        } catch (IllegalStateException e) {
            return false;
        }
    }

    // ====================================
    // Domain Logic - Availability
    // ====================================

    public void setAvailabilityPeriod(LocalDateTime from, LocalDateTime until) {
        if (from != null && until != null && from.isAfter(until)) {
            throw new IllegalArgumentException("Available from must be before available until");
        }

        ChangeTracker tracker = new ChangeTracker();
        tracker.track("availableFrom", this.availableFrom, from);
        tracker.track("availableUntil", this.availableUntil, until);

        this.availableFrom = from;
        this.availableUntil = until;

        if (tracker.hasChanges()) {
            registerEvent(new EntityUpdatedEvent<>(this, tracker.getChanges()));
        }
    }

    @Transient
    public boolean isAvailableNow() {
        LocalDateTime now = LocalDateTime.now();

        if (availableFrom != null && now.isBefore(availableFrom)) {
            return false;
        }

        if (availableUntil != null && now.isAfter(availableUntil)) {
            return false;
        }

        return true;
    }

    // ====================================
    // SoftDeletableAggregateRoot Implementation
    // ====================================

    @Override
    protected DomainEvent createDeletedEvent(String deletedBy) {
        return EntityDeletedEvent.builder()
                .entityClass(Product.class)
                .entityType("Product")
                .entityId(this.getId())
                .deletedBy(deletedBy)
                .softDelete(true)
                .reason("Product soft-deleted: " + this.name)
                .occurredAt(LocalDateTime.now())
                .build();
    }

    @Override
    protected DomainEvent createRestoredEvent(String restoredBy, String previousDeletedBy) {
        return EntityRestoredEvent.builder()
                .entityClass(Product.class)
                .entityType("Product")
                .entityId(this.getId())
                .restoredBy(restoredBy)
                .previousDeletedBy(previousDeletedBy)
                .previousDeletedAt(this.getDeletedAt())
                .occurredAt(LocalDateTime.now())
                .build();
    }

    // ====================================
    // Helper Methods
    // ====================================

    private static String generateSlug(String name, SKU sku) {
        String slug = name.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .trim();

        return slug + "-" + sku.getValue().toLowerCase();
    }

    @Transient
    private String getCurrentUsername() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                return authentication.getName();
            }
        } catch (Exception e) {
            // Ignore
        }
        return "system";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Product)) return false;
        Product product = (Product) o;
        return sku != null && sku.equals(product.sku);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return String.format("Product[id=%s, sku=%s, name=%s, status=%s]",
                getId(), sku, name, status);
    }
}