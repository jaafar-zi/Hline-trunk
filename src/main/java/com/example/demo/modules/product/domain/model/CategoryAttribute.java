package com.example.demo.modules.product.domain.model;

import com.example.demo.common.base.entity.SoftDeletableAggregateRoot;
import com.example.demo.common.event.DomainEvent;
import com.example.demo.common.event.EntityUpdatedEvent;
import com.example.demo.common.util.ChangeTracker;
import com.example.demo.modules.product.domain.model.vo.AttributeType;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Category Attribute Entity
 *
 * Defines attribute templates that products in a category should have
 * These serve as a schema/template for product attributes
 *
 * Business Rules:
 * - Attribute key must be unique within category
 * - Required attributes must be filled in products
 * - Attribute type determines validation rules
 * - Display order controls UI presentation
 *
 * Events Published:
 * - EntityUpdatedEvent<CategoryAttribute> (with change tracking)
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
        name = "category_attributes",
        indexes = {
                @Index(name = "idx_category_attr_category", columnList = "category_id"),
                @Index(name = "idx_category_attr_key", columnList = "category_id, key"),
                @Index(name = "idx_category_attr_order", columnList = "display_order")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_category_attribute_key",
                        columnNames = {"category_id", "key"}
                )
        }
)
public class CategoryAttribute extends SoftDeletableAggregateRoot {

    // ====================================
    // Core Information
    // ====================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(name = "key", nullable = false, length = 100)
    private String key;

    @Column(name = "label", nullable = false, length = 255)
    private String label;

    @Column(name = "description", length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "attribute_type", nullable = false, length = 50)
    @Builder.Default
    private AttributeType attributeType = AttributeType.TEXT;

    @Column(name = "required", nullable = false)
    @Builder.Default
    private Boolean required = false;

    @Column(name = "searchable", nullable = false)
    @Builder.Default
    private Boolean searchable = false;

    @Column(name = "filterable", nullable = false)
    @Builder.Default
    private Boolean filterable = false;

    @Column(name = "display_order")
    @Builder.Default
    private Integer displayOrder = 0;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private Boolean active = true;

    // ====================================
    // Validation Rules
    // ====================================

    @Column(name = "validation_rules", columnDefinition = "TEXT")
    private String validationRules; // JSON format

    @Column(name = "default_value", length = 1000)
    private String defaultValue;

    @Column(name = "min_length")
    private Integer minLength;

    @Column(name = "max_length")
    private Integer maxLength;

    @Column(name = "pattern", length = 500)
    private String pattern;

    // ====================================
    // Options (for SELECT, MULTI_SELECT types)
    // ====================================

    @OneToMany(
            mappedBy = "categoryAttribute",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @OrderBy("displayOrder ASC, value ASC")
    @Builder.Default
    private Set<AttributeOption> options = new HashSet<>();

    // ====================================
    // UI Display
    // ====================================

    @Column(name = "group_name", length = 100)
    private String groupName;

    @Column(name = "help_text", length = 500)
    private String helpText;

    @Column(name = "placeholder", length = 255)
    private String placeholder;

    // ====================================
    // Factory Methods
    // ====================================

    /**
     * Create attribute template
     */
    public static CategoryAttribute create(
            Category category,
            String key,
            String label,
            AttributeType type,
            boolean required
    ) {
        validateKey(key);
        validateLabel(label);

        if (category == null) {
            throw new IllegalArgumentException("Category cannot be null");
        }

        return CategoryAttribute.builder()
                .category(category)
                .key(key)
                .label(label)
                .attributeType(type != null ? type : AttributeType.TEXT)
                .required(required)
                .active(true)
                .searchable(false)
                .filterable(false)
                .displayOrder(0)
                .build();
    }

    // ====================================
    // Domain Logic - Basic Information
    // ====================================

    /**
     * Update basic information
     *
     * Events Published:
     * - EntityUpdatedEvent<CategoryAttribute> (with change tracking)
     */
    public void updateBasicInfo(String label, String description) {
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
     * Update attribute type
     */
    public void updateType(AttributeType newType) {
        if (newType == null) {
            throw new IllegalArgumentException("Attribute type cannot be null");
        }

        if (this.attributeType != newType) {
            // Validate type change is safe
            validateTypeChange(newType);

            ChangeTracker tracker = new ChangeTracker();
            tracker.track("attributeType", this.attributeType, newType);

            this.attributeType = newType;

            // Clear options if changing away from SELECT types
            if (newType != AttributeType.SELECT && newType != AttributeType.MULTI_SELECT) {
                if (!this.options.isEmpty()) {
                    this.options.clear();
                    tracker.track("options", "cleared", null);
                }
            }

            registerEvent(new EntityUpdatedEvent<>(this, tracker.getChanges()));
        }
    }

    /**
     * Update flags
     */
    public void updateFlags(Boolean required, Boolean searchable, Boolean filterable) {
        ChangeTracker tracker = new ChangeTracker();

        if (required != null && !required.equals(this.required)) {
            tracker.track("required", this.required, required);
            this.required = required;
        }

        if (searchable != null && !searchable.equals(this.searchable)) {
            tracker.track("searchable", this.searchable, searchable);
            this.searchable = searchable;
        }

        if (filterable != null && !filterable.equals(this.filterable)) {
            tracker.track("filterable", this.filterable, filterable);
            this.filterable = filterable;
        }

        if (tracker.hasChanges()) {
            registerEvent(new EntityUpdatedEvent<>(this, tracker.getChanges()));
        }
    }

    /**
     * Update display information
     */
    public void updateDisplay(Integer displayOrder, String groupName, String helpText, String placeholder) {
        ChangeTracker tracker = new ChangeTracker();

        tracker.track("displayOrder", this.displayOrder, displayOrder);
        tracker.track("groupName", this.groupName, groupName);
        tracker.track("helpText", this.helpText, helpText);
        tracker.track("placeholder", this.placeholder, placeholder);

        this.displayOrder = displayOrder;
        this.groupName = groupName;
        this.helpText = helpText;
        this.placeholder = placeholder;

        if (tracker.hasChanges()) {
            registerEvent(new EntityUpdatedEvent<>(this, tracker.getChanges()));
        }
    }

    // ====================================
    // Domain Logic - Validation Rules
    // ====================================

    /**
     * Update validation rules
     */
    public void updateValidationRules(String validationRules, String defaultValue) {
        ChangeTracker tracker = new ChangeTracker();

        tracker.track("validationRules", this.validationRules, validationRules);
        tracker.track("defaultValue", this.defaultValue, defaultValue);

        this.validationRules = validationRules;
        this.defaultValue = defaultValue;

        if (tracker.hasChanges()) {
            registerEvent(new EntityUpdatedEvent<>(this, tracker.getChanges()));
        }
    }

    /**
     * Update length constraints
     */
    public void updateLengthConstraints(Integer minLength, Integer maxLength) {
        if (minLength != null && maxLength != null && minLength > maxLength) {
            throw new IllegalArgumentException("Min length cannot be greater than max length");
        }

        ChangeTracker tracker = new ChangeTracker();

        tracker.track("minLength", this.minLength, minLength);
        tracker.track("maxLength", this.maxLength, maxLength);

        this.minLength = minLength;
        this.maxLength = maxLength;

        if (tracker.hasChanges()) {
            registerEvent(new EntityUpdatedEvent<>(this, tracker.getChanges()));
        }
    }

    /**
     * Update validation pattern
     */
    public void updatePattern(String pattern) {
        if (pattern != null && !pattern.isBlank()) {
            // Validate regex pattern
            try {
                java.util.regex.Pattern.compile(pattern);
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid regex pattern: " + e.getMessage());
            }
        }

        if (!java.util.Objects.equals(this.pattern, pattern)) {
            ChangeTracker tracker = new ChangeTracker();
            tracker.track("pattern", this.pattern, pattern);

            this.pattern = pattern;

            registerEvent(new EntityUpdatedEvent<>(this, tracker.getChanges()));
        }
    }

    // ====================================
    // Domain Logic - Options
    // ====================================

    /**
     * Add option (for SELECT/MULTI_SELECT types)
     */
    public void addOption(AttributeOption option) {
        if (this.attributeType != AttributeType.SELECT &&
                this.attributeType != AttributeType.MULTI_SELECT) {
            throw new IllegalStateException(
                    "Options can only be added to SELECT or MULTI_SELECT attributes");
        }

        if (option == null) {
            throw new IllegalArgumentException("Option cannot be null");
        }

        if (hasOption(option.getValue())) {
            throw new IllegalStateException("Option already exists: " + option.getValue());
        }

        option.setCategoryAttribute(this);
        this.options.add(option);

        EntityUpdatedEvent<CategoryAttribute> event = new EntityUpdatedEvent<>(this);
        event.addChange("options", null, "Added option: " + option.getValue());
        registerEvent(event);
    }

    /**
     * Remove option
     */
    public void removeOption(String value) {
        AttributeOption option = getOption(value);
        if (option == null) {
            throw new IllegalArgumentException("Option not found: " + value);
        }

        this.options.remove(option);

        EntityUpdatedEvent<CategoryAttribute> event = new EntityUpdatedEvent<>(this);
        event.addChange("options", "Removed option: " + value, null);
        registerEvent(event);
    }

    public AttributeOption getOption(String value) {
        return options.stream()
                .filter(opt -> opt.getValue().equals(value))
                .findFirst()
                .orElse(null);
    }

    public boolean hasOption(String value) {
        return getOption(value) != null;
    }

    // ====================================
    // Domain Logic - Activation
    // ====================================

    /**
     * Activate attribute
     */
    public void activate() {
        if (Boolean.TRUE.equals(this.active)) {
            throw new IllegalStateException("Attribute is already active");
        }

        this.active = true;

        ChangeTracker tracker = new ChangeTracker();
        tracker.track("active", false, true);
        registerEvent(new EntityUpdatedEvent<>(this, tracker.getChanges()));
    }

    /**
     * Deactivate attribute
     */
    public void deactivate() {
        if (Boolean.FALSE.equals(this.active)) {
            throw new IllegalStateException("Attribute is already inactive");
        }

        if (Boolean.TRUE.equals(this.required)) {
            throw new IllegalStateException("Cannot deactivate required attribute");
        }

        this.active = false;

        ChangeTracker tracker = new ChangeTracker();
        tracker.track("active", true, false);
        registerEvent(new EntityUpdatedEvent<>(this, tracker.getChanges()));
    }

    // ====================================
    // Validation
    // ====================================

    /**
     * Validate value against attribute rules
     */
    public void validateValue(String value) {
        if (Boolean.TRUE.equals(required) && (value == null || value.isBlank())) {
            throw new IllegalArgumentException(
                    String.format("Required attribute '%s' cannot be empty", label));
        }

        if (value != null && !value.isBlank()) {
            // Length validation
            if (minLength != null && value.length() < minLength) {
                throw new IllegalArgumentException(
                        String.format("Attribute '%s' must be at least %d characters", label, minLength));
            }

            if (maxLength != null && value.length() > maxLength) {
                throw new IllegalArgumentException(
                        String.format("Attribute '%s' cannot exceed %d characters", label, maxLength));
            }

            // Pattern validation
            if (pattern != null && !value.matches(pattern)) {
                throw new IllegalArgumentException(
                        String.format("Attribute '%s' does not match required pattern", label));
            }

            // Type-specific validation
            validateTypeSpecific(value);

            // Options validation
            if ((attributeType == AttributeType.SELECT || attributeType == AttributeType.MULTI_SELECT)
                    && !options.isEmpty()) {
                boolean validOption = options.stream()
                        .anyMatch(opt -> opt.getValue().equals(value));
                if (!validOption) {
                    throw new IllegalArgumentException(
                            String.format("Invalid option for attribute '%s': %s", label, value));
                }
            }
        }
    }

    private void validateTypeSpecific(String value) {
        try {
            switch (attributeType) {
                case NUMBER:
                    Double.parseDouble(value);
                    break;
                case BOOLEAN:
                    if (!value.equalsIgnoreCase("true") && !value.equalsIgnoreCase("false")) {
                        throw new IllegalArgumentException("Must be true or false");
                    }
                    break;
                case DATE:
                    java.time.LocalDate.parse(value);
                    break;
                case DATETIME:
                    java.time.LocalDateTime.parse(value);
                    break;
                case EMAIL:
                    if (!value.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                        throw new IllegalArgumentException("Invalid email format");
                    }
                    break;
                case URL:
                    new java.net.URL(value);
                    break;
                // TEXT, TEXTAREA, SELECT, MULTI_SELECT don't need additional validation
                default:
                    break;
            }
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    String.format("Invalid %s value for attribute '%s': %s",
                            attributeType.name().toLowerCase(), label, e.getMessage()));
        }
    }

    private void validateTypeChange(AttributeType newType) {
        // Could add logic to prevent unsafe type changes
        // For example, if there are existing product attributes using this template
    }

    private static void validateKey(String key) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Attribute key cannot be null or empty");
        }
        if (key.length() > 100) {
            throw new IllegalArgumentException("Attribute key cannot exceed 100 characters");
        }
        if (!key.matches("^[a-z][a-z0-9_]*$")) {
            throw new IllegalArgumentException(
                    "Attribute key must start with lowercase letter and contain only lowercase letters, numbers, and underscores");
        }
    }

    private static void validateLabel(String label) {
        if (label == null || label.isBlank()) {
            throw new IllegalArgumentException("Attribute label cannot be null or empty");
        }
        if (label.length() > 255) {
            throw new IllegalArgumentException("Attribute label cannot exceed 255 characters");
        }
    }

    // ====================================
    // Helper Methods
    // ====================================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CategoryAttribute)) return false;
        CategoryAttribute that = (CategoryAttribute) o;
        return getId() != null && getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return String.format("CategoryAttribute[id=%s, key=%s, label=%s, type=%s, required=%s]",
                getId(), key, label, attributeType, required);
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
