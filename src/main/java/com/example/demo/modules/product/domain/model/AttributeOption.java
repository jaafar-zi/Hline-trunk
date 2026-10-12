package com.example.demo.modules.product.domain.model;

import com.example.demo.common.base.entity.SoftDeletableAggregateRoot;
import com.example.demo.common.event.DomainEvent;
import com.example.demo.common.event.EntityUpdatedEvent;
import com.example.demo.common.util.ChangeTracker;
import jakarta.persistence.*;
import lombok.*;

/**
 * Attribute Option Entity
 *
 * Represents a selectable option for SELECT/MULTI_SELECT attributes
 *
 * Business Rules:
 * - Option value must be unique within attribute
 * - Display order controls presentation order
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
        name = "attribute_options",
        indexes = {
                @Index(name = "idx_attr_option_attribute", columnList = "category_attribute_id"),
                @Index(name = "idx_attr_option_value", columnList = "category_attribute_id, value"),
                @Index(name = "idx_attr_option_order", columnList = "display_order")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_attribute_option_value",
                        columnNames = {"category_attribute_id", "value"}
                )
        }
)
public class AttributeOption extends SoftDeletableAggregateRoot {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_attribute_id", nullable = false)
    private CategoryAttribute categoryAttribute;

    @Column(name = "value", nullable = false, length = 255)
    private String value;

    @Column(name = "label", nullable = false, length = 255)
    private String label;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "display_order")
    @Builder.Default
    private Integer displayOrder = 0;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Column(name = "color_code", length = 7)
    private String colorCode; // For visual attributes like color

    @Column(name = "icon", length = 255)
    private String icon;

    // ====================================
    // Factory Methods
    // ====================================

    public static AttributeOption create(String value, String label) {
        validateValue(value);
        validateLabel(label);

        return AttributeOption.builder()
                .value(value)
                .label(label)
                .active(true)
                .displayOrder(0)
                .build();
    }

    // ====================================
    // Domain Logic
    // ====================================

    /**
     * Update option information
     */
    public void updateInfo(String label, String description) {
        ChangeTracker tracker = new ChangeTracker();

        if (label != null && !label.equals(this.label)) {
            validateLabel(label);
            tracker.track("label", this.label, label);
            this.label = label;
        }

        if (description != null && !description.equals(this.description)) {
            tracker.track("description", this.description, description);
            this.description = description;
        }

        if (tracker.hasChanges()) {
            registerEvent(new EntityUpdatedEvent<>(this, tracker.getChanges()));
        }
    }

    /**
     * Update display settings
     */
    public void updateDisplay(Integer displayOrder, String colorCode, String icon) {
        ChangeTracker tracker = new ChangeTracker();

        tracker.track("displayOrder", this.displayOrder, displayOrder);
        tracker.track("colorCode", this.colorCode, colorCode);
        tracker.track("icon", this.icon, icon);

        this.displayOrder = displayOrder;
        this.colorCode = colorCode;
        this.icon = icon;

        if (tracker.hasChanges()) {
            registerEvent(new EntityUpdatedEvent<>(this, tracker.getChanges()));
        }
    }

    /**
     * Activate option
     */
    public void activate() {
        if (Boolean.TRUE.equals(this.active)) {
            throw new IllegalStateException("Option is already active");
        }

        this.active = true;

        ChangeTracker tracker = new ChangeTracker();
        tracker.track("active", false, true);
        registerEvent(new EntityUpdatedEvent<>(this, tracker.getChanges()));
    }

    /**
     * Deactivate option
     */
    public void deactivate() {
        if (Boolean.FALSE.equals(this.active)) {
            throw new IllegalStateException("Option is already inactive");
        }

        this.active = false;

        ChangeTracker tracker = new ChangeTracker();
        tracker.track("active", true, false);
        registerEvent(new EntityUpdatedEvent<>(this, tracker.getChanges()));
    }

    // ====================================
    // Validation
    // ====================================

    private static void validateValue(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Option value cannot be null or empty");
        }
        if (value.length() > 255) {
            throw new IllegalArgumentException("Option value cannot exceed 255 characters");
        }
    }

    private static void validateLabel(String label) {
        if (label == null || label.isBlank()) {
            throw new IllegalArgumentException("Option label cannot be null or empty");
        }
        if (label.length() > 255) {
            throw new IllegalArgumentException("Option label cannot exceed 255 characters");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AttributeOption)) return false;
        AttributeOption that = (AttributeOption) o;
        return getId() != null && getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return String.format("AttributeOption[id=%s, value=%s, label=%s]",
                getId(), value, label);
    }

    @Override
    protected DomainEvent createDeletedEvent(String deletedBy) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'createDeletedEvent'");
    }

    @Override
    protected DomainEvent createRestoredEvent(String restoredBy, String previousDeletedBy) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'createRestoredEvent'");
    }
}
