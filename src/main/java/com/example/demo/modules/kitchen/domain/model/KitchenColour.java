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
 * Kitchen Colour Entity
 *
 * Represents a colour option available across kitchen collections.
 * Managed by back office - fully configurable.
 *
 * Examples: White, Cashmere, Dust Grey, Anthracite, Sage Green
 *
 * Business Rules:
 * - Colour code must be unique
 * - Hex value must be valid
 * - Cannot delete colour used by active collections
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
    name = "kitchen_colours",
    indexes = {
        @Index(name = "idx_kitchen_colour_code", columnList = "code"),
        @Index(name = "idx_kitchen_colour_active", columnList = "active")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_kitchen_colour_code", columnNames = "code")
    }
)
public class KitchenColour extends SoftDeletableAggregateRoot {

    @Column(name = "code", nullable = false, length = 50)
    private String code;              // "CASHMERE", "WHITE", "DUST_GREY"

    @Column(name = "name", nullable = false, length = 100)
    private String name;              // "Cashmere", "White", "Dust Grey"

    @Column(name = "hex_value", length = 7)
    private String hexValue;          // "#F5E6D3"

    @Column(name = "swatch_image_url", length = 500)
    private String swatchImageUrl;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Column(name = "sort_order")
    @Builder.Default
    private Integer sortOrder = 0;

    // ====================================
    // Factory Methods
    // ====================================

    public static KitchenColour create(String code, String name, String hexValue) {
        validateCode(code);
        validateName(name);

        return KitchenColour.builder()
            .code(code.toUpperCase().replaceAll("\\s+", "_"))
            .name(name)
            .hexValue(hexValue)
            .active(true)
            .sortOrder(0)
            .build();
    }

    // ====================================
    // Domain Logic
    // ====================================

    public void updateDetails(String name, String hexValue, 
                               String swatchImageUrl, String description) {
        ChangeTracker tracker = new ChangeTracker();

        if (name != null && !name.equals(this.name)) {
            validateName(name);
            tracker.track("name", this.name, name);
            this.name = name;
        }
        if (hexValue != null && !hexValue.equals(this.hexValue)) {
            tracker.track("hexValue", this.hexValue, hexValue);
            this.hexValue = hexValue;
        }
        if (swatchImageUrl != null && !swatchImageUrl.equals(this.swatchImageUrl)) {
            tracker.track("swatchImageUrl", this.swatchImageUrl, swatchImageUrl);
            this.swatchImageUrl = swatchImageUrl;
        }
        if (description != null && !description.equals(this.description)) {
            tracker.track("description", this.description, description);
            this.description = description;
        }

        if (tracker.hasChanges()) {
            registerEvent(new EntityUpdatedEvent<>(this, tracker.getChanges()));
        }
    }

    public void activate() {
        if (Boolean.TRUE.equals(this.active)) {
            throw new IllegalStateException("Colour is already active");
        }
        this.active = true;
        ChangeTracker tracker = new ChangeTracker();
        tracker.track("active", false, true);
        registerEvent(new EntityUpdatedEvent<>(this, tracker.getChanges()));
    }

    public void deactivate() {
        if (Boolean.FALSE.equals(this.active)) {
            throw new IllegalStateException("Colour is already inactive");
        }
        this.active = false;
        ChangeTracker tracker = new ChangeTracker();
        tracker.track("active", true, false);
        registerEvent(new EntityUpdatedEvent<>(this, tracker.getChanges()));
    }

    // ====================================
    // Validation
    // ====================================

    private static void validateCode(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Colour code cannot be null or empty");
        }
    }

    private static void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Colour name cannot be null or empty");
        }
    }

    // ====================================
    // SoftDeletableAggregateRoot
    // ====================================

    @Override
    protected DomainEvent createDeletedEvent(String deletedBy) {
        return EntityDeletedEvent.builder()
            .entityClass(KitchenColour.class)
            .entityType("KitchenColour")
            .entityId(this.getId())
            .deletedBy(deletedBy)
            .softDelete(true)
            .occurredAt(LocalDateTime.now())
            .build();
    }

    @Override
    protected DomainEvent createRestoredEvent(String restoredBy, String previousDeletedBy) {
        return EntityRestoredEvent.builder()
            .entityClass(KitchenColour.class)
            .entityType("KitchenColour")
            .entityId(this.getId())
            .restoredBy(restoredBy)
            .previousDeletedBy(previousDeletedBy)
            .previousDeletedAt(this.getDeletedAt())
            .occurredAt(LocalDateTime.now())
            .build();
    }
}