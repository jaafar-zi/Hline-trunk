package com.example.demo.modules.kitchen.domain.model;

import com.example.demo.common.base.entity.SoftDeletableAggregateRoot;
import com.example.demo.common.event.DomainEvent;
import com.example.demo.common.event.EntityDeletedEvent;
import com.example.demo.common.event.EntityRestoredEvent;
import com.example.demo.common.event.EntityUpdatedEvent;
import com.example.demo.common.util.ChangeTracker;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Kitchen Finish Group Entity
 *
 * Groups collections by finish type within a range.
 * Configurable by back office.
 *
 * Examples:
 *   J-Pull → Matt Group
 *   J-Pull → Gloss Group
 *   J-Pull → SuperGloss Group
 *
 * Business Rules:
 * - Must belong to a KitchenRange
 * - Must reference a KitchenFinishType
 * - Code unique within range
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
    name = "kitchen_finish_groups",
    indexes = {
        @Index(name = "idx_finish_group_range", columnList = "range_id"),
        @Index(name = "idx_finish_group_active", columnList = "active")
    }
)
public class KitchenFinishGroup extends SoftDeletableAggregateRoot {

    // ====================================
    // Core Information
    // ====================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "range_id", nullable = false)
    private KitchenRange range;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "finish_type_id", nullable = false)
    private KitchenFinishType finishType;

    @Column(name = "name", nullable = false, length = 100)
    private String name;              // "J-Pull SuperGloss", "J-Pull Matt"

    @Column(name = "slug", nullable = false, length = 150)
    private String slug;              // "supergloss", "matt"

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Column(name = "sort_order")
    @Builder.Default
    private Integer sortOrder = 0;

    // ====================================
    // Relationships
    // ====================================

    @OneToMany(
        mappedBy = "finishGroup",
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    @Builder.Default
    private Set<KitchenCollection> collections = new HashSet<>();

    // ====================================
    // Factory Methods
    // ====================================

    public static KitchenFinishGroup create(
            KitchenRange range,
            KitchenFinishType finishType,
            String name) {

        if (range == null) {
            throw new IllegalArgumentException("Range cannot be null");
        }
        if (finishType == null) {
            throw new IllegalArgumentException("Finish type cannot be null");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name cannot be null or empty");
        }

        return KitchenFinishGroup.builder()
            .range(range)
            .finishType(finishType)
            .name(name)
            .slug(generateSlug(name))
            .active(true)
            .sortOrder(0)
            .build();
    }

    // ====================================
    // Domain Logic
    // ====================================

    public void updateDetails(String name, String description) {
        ChangeTracker tracker = new ChangeTracker();

        if (name != null && !name.equals(this.name)) {
            tracker.track("name", this.name, name);
            this.name = name;
            this.slug = generateSlug(name);
        }
        if (description != null && !description.equals(this.description)) {
            tracker.track("description", this.description, description);
            this.description = description;
        }

        if (tracker.hasChanges()) {
            registerEvent(new EntityUpdatedEvent<>(this, tracker.getChanges()));
        }
    }

    public void changeFinishType(KitchenFinishType newFinishType) {
        if (newFinishType == null) {
            throw new IllegalArgumentException("Finish type cannot be null");
        }
        if (!java.util.Objects.equals(this.finishType, newFinishType)) {
            ChangeTracker tracker = new ChangeTracker();
            tracker.track("finishType",
                this.finishType != null ? this.finishType.getName() : null,
                newFinishType.getName());
            this.finishType = newFinishType;
            registerEvent(new EntityUpdatedEvent<>(this, tracker.getChanges()));
        }
    }

    public void addCollection(KitchenCollection collection) {
        if (collection == null) {
            throw new IllegalArgumentException("Collection cannot be null");
        }
        collection.setFinishGroup(this);
        this.collections.add(collection);
    }

    public void activate() {
        if (Boolean.TRUE.equals(this.active)) {
            throw new IllegalStateException("Finish group is already active");
        }
        this.active = true;
        ChangeTracker tracker = new ChangeTracker();
        tracker.track("active", false, true);
        registerEvent(new EntityUpdatedEvent<>(this, tracker.getChanges()));
    }

    public void deactivate(String reason) {
        if (Boolean.FALSE.equals(this.active)) {
            throw new IllegalStateException("Finish group is already inactive");
        }
        long activeCollections = collections.stream()
            .filter(c -> Boolean.TRUE.equals(c.getActive()))
            .count();
        if (activeCollections > 0) {
            throw new IllegalStateException(
                "Cannot deactivate finish group with " + activeCollections + " active collections"
            );
        }
        this.active = false;
        ChangeTracker tracker = new ChangeTracker();
        tracker.track("active", true, false);
        registerEvent(new EntityUpdatedEvent<>(this, tracker.getChanges()));
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
            .entityClass(KitchenFinishGroup.class)
            .entityType("KitchenFinishGroup")
            .entityId(this.getId())
            .deletedBy(deletedBy)
            .softDelete(true)
            .occurredAt(LocalDateTime.now())
            .build();
    }

    @Override
    protected DomainEvent createRestoredEvent(String restoredBy, String previousDeletedBy) {
        return EntityRestoredEvent.builder()
            .entityClass(KitchenFinishGroup.class)
            .entityType("KitchenFinishGroup")
            .entityId(this.getId())
            .restoredBy(restoredBy)
            .previousDeletedBy(previousDeletedBy)
            .previousDeletedAt(this.getDeletedAt())
            .occurredAt(LocalDateTime.now())
            .build();
    }
}