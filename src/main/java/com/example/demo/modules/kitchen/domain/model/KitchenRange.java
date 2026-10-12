package com.example.demo.modules.kitchen.domain.model;

import com.example.demo.common.base.entity.SoftDeletableAggregateRoot;
import com.example.demo.common.event.DomainEvent;
import com.example.demo.common.event.EntityDeletedEvent;
import com.example.demo.common.event.EntityRestoredEvent;
import com.example.demo.common.event.EntityUpdatedEvent;
import com.example.demo.common.util.ChangeTracker;
import com.example.demo.modules.kitchen.domain.event.KitchenRangeActivatedEvent;
import com.example.demo.modules.kitchen.domain.event.KitchenRangeDeactivatedEvent;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Kitchen Range Aggregate Root
 *
 * Top-level grouping for kitchen styles.
 * Defines the door handle/form factor of the kitchen.
 * Fully configurable by back office.
 *
 * Examples: J-Pull, Shaker, Slab, Handleless
 *
 * Business Rules:
 * - Code must be unique
 * - Cannot deactivate range with active collections
 * - Slug auto-generated from name
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
    name = "kitchen_ranges",
    indexes = {
        @Index(name = "idx_kitchen_range_code", columnList = "code"),
        @Index(name = "idx_kitchen_range_slug", columnList = "slug"),
        @Index(name = "idx_kitchen_range_active", columnList = "active")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_kitchen_range_code", columnNames = "code"),
        @UniqueConstraint(name = "uk_kitchen_range_slug", columnNames = "slug")
    }
)
public class KitchenRange extends SoftDeletableAggregateRoot {

    // ====================================
    // Core Information
    // ====================================

    @Column(name = "code", nullable = false, length = 50)
    private String code;              // "J_PULL", "SHAKER", "SLAB"

    @Column(name = "name", nullable = false, length = 100)
    private String name;              // "J-Pull", "Shaker", "Slab"

    @Column(name = "slug", nullable = false, length = 150)
    private String slug;              // "j-pull", "shaker", "slab"

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

    // SEO
    @Column(name = "meta_title", length = 255)
    private String metaTitle;

    @Column(name = "meta_description", length = 500)
    private String metaDescription;

    // ====================================
    // Relationships
    // ====================================

    @OneToMany(
        mappedBy = "range",
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    @Builder.Default
    private Set<KitchenFinishGroup> finishGroups = new HashSet<>();

    // ====================================
    // Factory Methods
    // ====================================

    public static KitchenRange create(String code, String name) {
        validateCode(code);
        validateName(name);

        String normalizedCode = code.toUpperCase().replaceAll("[^A-Z0-9]", "_");
        String slug = generateSlug(name);

        return KitchenRange.builder()
            .code(normalizedCode)
            .name(name)
            .slug(slug)
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
            validateName(name);
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
        if (!java.util.Objects.equals(this.heroImageUrl, heroImageUrl)) {
            ChangeTracker tracker = new ChangeTracker();
            tracker.track("heroImageUrl", this.heroImageUrl, heroImageUrl);
            this.heroImageUrl = heroImageUrl;
            registerEvent(new EntityUpdatedEvent<>(this, tracker.getChanges()));
        }
    }

    public void updateSEO(String metaTitle, String metaDescription) {
        ChangeTracker tracker = new ChangeTracker();
        tracker.track("metaTitle", this.metaTitle, metaTitle);
        tracker.track("metaDescription", this.metaDescription, metaDescription);
        this.metaTitle = metaTitle;
        this.metaDescription = metaDescription;
        if (tracker.hasChanges()) {
            registerEvent(new EntityUpdatedEvent<>(this, tracker.getChanges()));
        }
    }

    // ====================================
    // Domain Logic - Finish Groups
    // ====================================

    public void addFinishGroup(KitchenFinishGroup finishGroup) {
        if (finishGroup == null) {
            throw new IllegalArgumentException("Finish group cannot be null");
        }
        finishGroup.setRange(this);
        this.finishGroups.add(finishGroup);

        EntityUpdatedEvent<KitchenRange> event = new EntityUpdatedEvent<>(this);
        event.addChange("finishGroups", null, "Added: " + finishGroup.getName());
        registerEvent(event);
    }

    public void removeFinishGroup(KitchenFinishGroup finishGroup) {
        if (finishGroup == null) {
            throw new IllegalArgumentException("Finish group cannot be null");
        }
        if (!this.finishGroups.contains(finishGroup)) {
            throw new IllegalStateException("Finish group not found in this range");
        }

        long activeCollections = finishGroup.getCollections().stream()
            .filter(c -> Boolean.TRUE.equals(c.getActive()))
            .count();

        if (activeCollections > 0) {
            throw new IllegalStateException(
                "Cannot remove finish group with " + activeCollections + " active collections"
            );
        }

        this.finishGroups.remove(finishGroup);

        EntityUpdatedEvent<KitchenRange> event = new EntityUpdatedEvent<>(this);
        event.addChange("finishGroups", "Removed: " + finishGroup.getName(), null);
        registerEvent(event);
    }

    // ====================================
    // Domain Logic - Activation
    // ====================================

    public void activate() {
        if (Boolean.TRUE.equals(this.active)) {
            throw new IllegalStateException("Kitchen range is already active");
        }
        this.active = true;
        registerEvent(KitchenRangeActivatedEvent.builder()
            .rangeId(this.getId())
            .rangeCode(this.code)
            .rangeName(this.name)
            .occurredAt(LocalDateTime.now())
            .build());
    }

    public void deactivate(String reason) {
        if (Boolean.FALSE.equals(this.active)) {
            throw new IllegalStateException("Kitchen range is already inactive");
        }

        long activeCollections = finishGroups.stream()
            .flatMap(fg -> fg.getCollections().stream())
            .filter(c -> Boolean.TRUE.equals(c.getActive()))
            .count();

        if (activeCollections > 0) {
            throw new IllegalStateException(
                "Cannot deactivate range with " + activeCollections + " active collections"
            );
        }

        this.active = false;
        registerEvent(KitchenRangeDeactivatedEvent.builder()
            .rangeId(this.getId())
            .rangeCode(this.code)
            .rangeName(this.name)
            .reason(reason)
            .occurredAt(LocalDateTime.now())
            .build());
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

    private static void validateCode(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Range code cannot be null or empty");
        }
    }

    private static void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Range name cannot be null or empty");
        }
    }

    // ====================================
    // SoftDeletableAggregateRoot
    // ====================================

    @Override
    protected DomainEvent createDeletedEvent(String deletedBy) {
        return EntityDeletedEvent.builder()
            .entityClass(KitchenRange.class)
            .entityType("KitchenRange")
            .entityId(this.getId())
            .deletedBy(deletedBy)
            .softDelete(true)
            .occurredAt(LocalDateTime.now())
            .build();
    }

    @Override
    protected DomainEvent createRestoredEvent(String restoredBy, String previousDeletedBy) {
        return EntityRestoredEvent.builder()
            .entityClass(KitchenRange.class)
            .entityType("KitchenRange")
            .entityId(this.getId())
            .restoredBy(restoredBy)
            .previousDeletedBy(previousDeletedBy)
            .previousDeletedAt(this.getDeletedAt())
            .occurredAt(LocalDateTime.now())
            .build();
    }

    @Override
    public String toString() {
        return String.format("KitchenRange[id=%s, code=%s, name=%s, active=%s]",
            getId(), code, name, active);
    }
}