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
 * Kitchen Finish Type Entity
 *
 * Represents a finish type (Matt, Gloss, SuperGloss, Woodgrain etc.)
 * Fully configurable by back office.
 *
 * Business Rules:
 * - Code must be unique
 * - Cannot delete finish type used by active finish groups
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
    name = "kitchen_finish_types",
    indexes = {
        @Index(name = "idx_finish_type_code", columnList = "code"),
        @Index(name = "idx_finish_type_active", columnList = "active")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_finish_type_code", columnNames = "code")
    }
)
public class KitchenFinishType extends SoftDeletableAggregateRoot {

    @Column(name = "code", nullable = false, length = 50)
    private String code;              // "MATT", "GLOSS", "SUPERGLOSS"

    @Column(name = "name", nullable = false, length = 100)
    private String name;              // "Matt", "Gloss", "SuperGloss"

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

    public static KitchenFinishType create(String code, String name, String description) {
        validateCode(code);
        validateName(name);

        return KitchenFinishType.builder()
            .code(code.toUpperCase().replaceAll("\\s+", "_"))
            .name(name)
            .description(description)
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
            validateName(name);
            tracker.track("name", this.name, name);
            this.name = name;
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
            throw new IllegalStateException("Finish type is already active");
        }
        this.active = true;
        ChangeTracker tracker = new ChangeTracker();
        tracker.track("active", false, true);
        registerEvent(new EntityUpdatedEvent<>(this, tracker.getChanges()));
    }

    public void deactivate() {
        if (Boolean.FALSE.equals(this.active)) {
            throw new IllegalStateException("Finish type is already inactive");
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
            throw new IllegalArgumentException("Finish type code cannot be null or empty");
        }
    }

    private static void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Finish type name cannot be null or empty");
        }
    }

    // ====================================
    // SoftDeletableAggregateRoot
    // ====================================

    @Override
    protected DomainEvent createDeletedEvent(String deletedBy) {
        return EntityDeletedEvent.builder()
            .entityClass(KitchenFinishType.class)
            .entityType("KitchenFinishType")
            .entityId(this.getId())
            .deletedBy(deletedBy)
            .softDelete(true)
            .occurredAt(LocalDateTime.now())
            .build();
    }

    @Override
    protected DomainEvent createRestoredEvent(String restoredBy, String previousDeletedBy) {
        return EntityRestoredEvent.builder()
            .entityClass(KitchenFinishType.class)
            .entityType("KitchenFinishType")
            .entityId(this.getId())
            .restoredBy(restoredBy)
            .previousDeletedBy(previousDeletedBy)
            .previousDeletedAt(this.getDeletedAt())
            .occurredAt(LocalDateTime.now())
            .build();
    }
}