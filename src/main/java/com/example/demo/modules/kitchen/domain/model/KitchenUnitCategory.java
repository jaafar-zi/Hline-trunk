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

/**
 * Kitchen Unit Category Entity
 *
 * Represents a category of cabinet units within a kitchen.
 * Fully configurable by back office.
 * These are the accordion sections in the kitchen configurator.
 *
 * Examples:
 *   Base Kitchen Cabinets
 *   Wall Kitchen Cabinets
 *   Tall Kitchen Cabinets
 *   Panels & Appliance Doors
 *   Accessories
 *   Sinks & Taps
 *   Worktops
 *   Appliances
 *   Hobs / Ovens / Cooker Hoods
 *
 * Business Rules:
 * - Code must be unique
 * - Sort order controls accordion display order
 * - Cannot delete category with active products
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
    name = "kitchen_unit_categories",
    indexes = {
        @Index(name = "idx_unit_cat_code", columnList = "code"),
        @Index(name = "idx_unit_cat_active", columnList = "active"),
        @Index(name = "idx_unit_cat_sort", columnList = "sort_order")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_unit_cat_code", columnNames = "code")
    }
)
public class KitchenUnitCategory extends SoftDeletableAggregateRoot {

    @Column(name = "code", nullable = false, length = 50)
    private String code;              // "BASE", "WALL", "TALL", "PANEL"

    @Column(name = "name", nullable = false, length = 150)
    private String name;              // "Base Kitchen Cabinets"

    @Column(name = "slug", nullable = false, length = 200)
    private String slug;              // "base-kitchen-cabinets"

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "icon", length = 255)
    private String icon;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Column(name = "sort_order")
    @Builder.Default
    private Integer sortOrder = 0;

    // ====================================
    // Factory Methods
    // ====================================

    public static KitchenUnitCategory create(String code, String name) {
        validateCode(code);
        validateName(name);

        return KitchenUnitCategory.builder()
            .code(code.toUpperCase().replaceAll("[^A-Z0-9]", "_"))
            .name(name)
            .slug(generateSlug(name))
            .active(true)
            .sortOrder(0)
            .build();
    }

    // ====================================
    // Domain Logic
    // ====================================

    public void updateDetails(String name, String description, String icon, String imageUrl) {
        ChangeTracker tracker = new ChangeTracker();

        if (name != null && !name.equals(this.name)) {
            validateName(name);
            tracker.track("name", this.name, name);
            this.name = name;
            this.slug = generateSlug(name);
        }
        if (description != null && !description.equals(this.description)) {
            tracker.track("description", this.description, description);
            this.description = description;
        }
        if (icon != null && !icon.equals(this.icon)) {
            tracker.track("icon", this.icon, icon);
            this.icon = icon;
        }
        if (imageUrl != null && !imageUrl.equals(this.imageUrl)) {
            tracker.track("imageUrl", this.imageUrl, imageUrl);
            this.imageUrl = imageUrl;
        }

        if (tracker.hasChanges()) {
            registerEvent(new EntityUpdatedEvent<>(this, tracker.getChanges()));
        }
    }

    public void updateSortOrder(Integer sortOrder) {
        if (!java.util.Objects.equals(this.sortOrder, sortOrder)) {
            ChangeTracker tracker = new ChangeTracker();
            tracker.track("sortOrder", this.sortOrder, sortOrder);
            this.sortOrder = sortOrder;
            registerEvent(new EntityUpdatedEvent<>(this, tracker.getChanges()));
        }
    }

    public void activate() {
        if (Boolean.TRUE.equals(this.active)) {
            throw new IllegalStateException("Unit category is already active");
        }
        this.active = true;
        ChangeTracker tracker = new ChangeTracker();
        tracker.track("active", false, true);
        registerEvent(new EntityUpdatedEvent<>(this, tracker.getChanges()));
    }

    public void deactivate() {
        if (Boolean.FALSE.equals(this.active)) {
            throw new IllegalStateException("Unit category is already inactive");
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

    private static void validateCode(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Unit category code cannot be null or empty");
        }
    }

    private static void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Unit category name cannot be null or empty");
        }
    }

    // ====================================
    // SoftDeletableAggregateRoot
    // ====================================

    @Override
    protected DomainEvent createDeletedEvent(String deletedBy) {
        return EntityDeletedEvent.builder()
            .entityClass(KitchenUnitCategory.class)
            .entityType("KitchenUnitCategory")
            .entityId(this.getId())
            .deletedBy(deletedBy)
            .softDelete(true)
            .occurredAt(LocalDateTime.now())
            .build();
    }

    @Override
    protected DomainEvent createRestoredEvent(String restoredBy, String previousDeletedBy) {
        return EntityRestoredEvent.builder()
            .entityClass(KitchenUnitCategory.class)
            .entityType("KitchenUnitCategory")
            .entityId(this.getId())
            .restoredBy(restoredBy)
            .previousDeletedBy(previousDeletedBy)
            .previousDeletedAt(this.getDeletedAt())
            .occurredAt(LocalDateTime.now())
            .build();
    }
}