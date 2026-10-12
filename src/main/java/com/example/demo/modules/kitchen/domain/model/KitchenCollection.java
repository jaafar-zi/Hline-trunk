package com.example.demo.modules.kitchen.domain.model;

import com.example.demo.common.base.entity.SoftDeletableAggregateRoot;
import com.example.demo.common.event.DomainEvent;
import com.example.demo.common.event.EntityDeletedEvent;
import com.example.demo.common.event.EntityRestoredEvent;
import com.example.demo.common.event.EntityUpdatedEvent;
import com.example.demo.common.util.ChangeTracker;
import com.example.demo.modules.kitchen.domain.event.KitchenCollectionActivatedEvent;
import com.example.demo.modules.kitchen.domain.event.KitchenCollectionDeactivatedEvent;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Kitchen Collection Entity
 *
 * Represents a specific aesthetic: Range + Finish + Colour.
 * This is what the customer selects when they say "I want J-Pull SuperGloss Cashmere".
 *
 * Examples:
 *   J-Pull SuperGloss Cashmere
 *   J-Pull Matt Anthracite
 *   Shaker Painted Sage Green
 *
 * Business Rules:
 * - Must belong to a KitchenFinishGroup (and transitively a KitchenRange)
 * - Must reference a KitchenColour
 * - Slug must be unique within the range
 * - Has a default cabinet colour
 * - Has a list of available cabinet colours
 * - Cannot deactivate with active products
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
    name = "kitchen_collections",
    indexes = {
        @Index(name = "idx_collection_finish_group", columnList = "finish_group_id"),
        @Index(name = "idx_collection_colour", columnList = "colour_id"),
        @Index(name = "idx_collection_slug", columnList = "slug"),
        @Index(name = "idx_collection_active", columnList = "active")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_collection_slug", columnNames = "slug")
    }
)
public class KitchenCollection extends SoftDeletableAggregateRoot {

    // ====================================
    // Core Information
    // ====================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "finish_group_id", nullable = false)
    private KitchenFinishGroup finishGroup;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "colour_id", nullable = false)
    private KitchenColour colour;

    @Column(name = "name", nullable = false, length = 200)
    private String name;                  // "J-Pull SuperGloss Cashmere"

    @Column(name = "slug", nullable = false, length = 250)
    private String slug;                  // "j-pull-supergloss-cashmere"

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "short_description", length = 500)
    private String shortDescription;

    @Column(name = "hero_image_url", length = 500)
    private String heroImageUrl;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Column(name = "sort_order")
    @Builder.Default
    private Integer sortOrder = 0;

    @Column(name = "lead_time_days")
    private Integer leadTimeDays;

    // ====================================
    // Default Cabinet Colour
    // ====================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "default_cabinet_colour_id")
    private KitchenColour defaultCabinetColour;

    // ====================================
    // Available Cabinet Colours (M2M)
    // ====================================

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "kitchen_collection_cabinet_colours",
        joinColumns = @JoinColumn(name = "collection_id"),
        inverseJoinColumns = @JoinColumn(name = "colour_id")
    )
    @Builder.Default
    private Set<KitchenColour> availableCabinetColours = new HashSet<>();

    // ====================================
    // Gallery Images
    // ====================================

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
        name = "kitchen_collection_images",
        joinColumns = @JoinColumn(name = "collection_id")
    )
    @Column(name = "image_url")
    @OrderColumn(name = "display_order")
    @Builder.Default
    private List<String> galleryImageUrls = new ArrayList<>();

    // ====================================
    // Features
    // ====================================

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
        name = "kitchen_collection_features",
        joinColumns = @JoinColumn(name = "collection_id")
    )
    @Column(name = "feature", length = 500)
    @OrderColumn(name = "display_order")
    @Builder.Default
    private List<String> features = new ArrayList<>();

    // ====================================
    // SEO
    // ====================================

    @Column(name = "meta_title", length = 255)
    private String metaTitle;

    @Column(name = "meta_description", length = 500)
    private String metaDescription;

    // ====================================
    // Products
    // ====================================

    @OneToMany(
        mappedBy = "collection",
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    @Builder.Default
    private Set<KitchenProduct> products = new HashSet<>();

    // ====================================
    // Factory Methods
    // ====================================

    public static KitchenCollection create(
            KitchenFinishGroup finishGroup,
            KitchenColour colour,
            String name) {

        if (finishGroup == null) {
            throw new IllegalArgumentException("Finish group cannot be null");
        }
        if (colour == null) {
            throw new IllegalArgumentException("Colour cannot be null");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name cannot be null or empty");
        }

        return KitchenCollection.builder()
            .finishGroup(finishGroup)
            .colour(colour)
            .name(name)
            .slug(generateSlug(name))
            .active(true)
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

    public void updateHeroImage(String heroImageUrl) {
        if (!Objects.equals(this.heroImageUrl, heroImageUrl)) {
            ChangeTracker tracker = new ChangeTracker();
            tracker.track("heroImageUrl", this.heroImageUrl, heroImageUrl);
            this.heroImageUrl = heroImageUrl;
            registerEvent(new EntityUpdatedEvent<>(this, tracker.getChanges()));
        }
    }

    public void updateLeadTime(Integer leadTimeDays) {
        if (!Objects.equals(this.leadTimeDays, leadTimeDays)) {
            ChangeTracker tracker = new ChangeTracker();
            tracker.track("leadTimeDays", this.leadTimeDays, leadTimeDays);
            this.leadTimeDays = leadTimeDays;
            registerEvent(new EntityUpdatedEvent<>(this, tracker.getChanges()));
        }
    }

    // ====================================
    // Domain Logic - Cabinet Colours
    // ====================================

    public void setDefaultCabinetColour(KitchenColour cabinetColour) {
        if (cabinetColour == null) {
            throw new IllegalArgumentException("Default cabinet colour cannot be null");
        }

        if (!this.availableCabinetColours.contains(cabinetColour)) {
            this.availableCabinetColours.add(cabinetColour);
        }

        ChangeTracker tracker = new ChangeTracker();
        tracker.track("defaultCabinetColour",
            this.defaultCabinetColour != null ? this.defaultCabinetColour.getName() : null,
            cabinetColour.getName());

        this.defaultCabinetColour = cabinetColour;
        registerEvent(new EntityUpdatedEvent<>(this, tracker.getChanges()));
    }

    public void addAvailableCabinetColour(KitchenColour cabinetColour) {
        if (cabinetColour == null) {
            throw new IllegalArgumentException("Cabinet colour cannot be null");
        }
        if (this.availableCabinetColours.contains(cabinetColour)) {
            throw new IllegalStateException("Cabinet colour already available: " + cabinetColour.getName());
        }

        this.availableCabinetColours.add(cabinetColour);

        EntityUpdatedEvent<KitchenCollection> event = new EntityUpdatedEvent<>(this);
        event.addChange("availableCabinetColours", null, "Added: " + cabinetColour.getName());
        registerEvent(event);
    }

    public void removeAvailableCabinetColour(KitchenColour cabinetColour) {
        if (cabinetColour == null) {
            throw new IllegalArgumentException("Cabinet colour cannot be null");
        }
        if (Objects.equals(this.defaultCabinetColour, cabinetColour)) {
            throw new IllegalStateException("Cannot remove the default cabinet colour");
        }

        this.availableCabinetColours.remove(cabinetColour);

        EntityUpdatedEvent<KitchenCollection> event = new EntityUpdatedEvent<>(this);
        event.addChange("availableCabinetColours", "Removed: " + cabinetColour.getName(), null);
        registerEvent(event);
    }

    // ====================================
    // Domain Logic - Gallery
    // ====================================

    public void addGalleryImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            throw new IllegalArgumentException("Image URL cannot be null or empty");
        }
        this.galleryImageUrls.add(imageUrl);
    }

    public void removeGalleryImage(String imageUrl) {
        this.galleryImageUrls.remove(imageUrl);
    }

    // ====================================
    // Domain Logic - Features
    // ====================================

    public void addFeature(String feature) {
        if (feature == null || feature.isBlank()) {
            throw new IllegalArgumentException("Feature cannot be null or empty");
        }
        this.features.add(feature);
    }

    public void removeFeature(String feature) {
        this.features.remove(feature);
    }

    // ====================================
    // Domain Logic - Activation
    // ====================================

    public void activate() {
        if (Boolean.TRUE.equals(this.active)) {
            throw new IllegalStateException("Collection is already active");
        }

        if (!Boolean.TRUE.equals(finishGroup.getActive())) {
            throw new IllegalStateException("Cannot activate collection when finish group is inactive");
        }

        this.active = true;
        registerEvent(KitchenCollectionActivatedEvent.builder()
            .collectionId(this.getId())
            .collectionName(this.name)
            .occurredAt(LocalDateTime.now())
            .build());
    }

    public void deactivate(String reason) {
        if (Boolean.FALSE.equals(this.active)) {
            throw new IllegalStateException("Collection is already inactive");
        }

        long activeProducts = products.stream()
            .filter(p -> Boolean.TRUE.equals(p.getActive()))
            .count();

        if (activeProducts > 0) {
            throw new IllegalStateException(
                "Cannot deactivate collection with " + activeProducts + " active products"
            );
        }

        this.active = false;
        registerEvent(KitchenCollectionDeactivatedEvent.builder()
            .collectionId(this.getId())
            .collectionName(this.name)
            .reason(reason)
            .occurredAt(LocalDateTime.now())
            .build());
    }

    // ====================================
    // Transient Helpers
    // ====================================

    /**
     * Get the range this collection belongs to (via finish group)
     */
    @Transient
    public KitchenRange getRange() {
        return finishGroup != null ? finishGroup.getRange() : null;
    }

    /**
     * Get finish type name
     */
    @Transient
    public String getFinishTypeName() {
        return finishGroup != null && finishGroup.getFinishType() != null
            ? finishGroup.getFinishType().getName()
            : null;
    }

    /**
     * Get colour name
     */
    @Transient
    public String getColourName() {
        return colour != null ? colour.getName() : null;
    }

    /**
     * Get colour hex value
     */
    @Transient
    public String getColourHex() {
        return colour != null ? colour.getHexValue() : null;
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
            .entityClass(KitchenCollection.class)
            .entityType("KitchenCollection")
            .entityId(this.getId())
            .deletedBy(deletedBy)
            .softDelete(true)
            .occurredAt(LocalDateTime.now())
            .build();
    }

    @Override
    protected DomainEvent createRestoredEvent(String restoredBy, String previousDeletedBy) {
        return EntityRestoredEvent.builder()
            .entityClass(KitchenCollection.class)
            .entityType("KitchenCollection")
            .entityId(this.getId())
            .restoredBy(restoredBy)
            .previousDeletedBy(previousDeletedBy)
            .previousDeletedAt(this.getDeletedAt())
            .occurredAt(LocalDateTime.now())
            .build();
    }

    @Override
    public String toString() {
        return String.format("KitchenCollection[id=%s, name=%s, colour=%s, active=%s]",
            getId(), name, getColourName(), active);
    }
}