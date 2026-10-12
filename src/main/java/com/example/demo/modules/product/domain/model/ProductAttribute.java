package com.example.demo.modules.product.domain.model;

import com.example.demo.common.base.entity.SoftDeletableAggregateRoot;
import com.example.demo.common.event.DomainEvent;
import com.example.demo.common.event.EntityUpdatedEvent;
import com.example.demo.common.util.ChangeTracker;
import com.example.demo.modules.product.domain.model.vo.AttributeType;
import jakarta.persistence.*;
import lombok.*;

/**
 * Product Attribute Entity
 *
 * Represents actual attribute values for a specific product
 * Based on CategoryAttribute template from product's category
 *
 * Business Rules:
 * - Attribute must conform to category attribute template
 * - Required attributes must have values
 * - Values must pass validation rules from template
 * - Cannot modify key after creation
 *
 * Events Published:
 * - EntityUpdatedEvent<ProductAttribute> (with change tracking)
 *
 * Note: This is an Entity within Product aggregate, not a separate aggregate root
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
        name = "product_attributes",
        indexes = {
                @Index(name = "idx_product_attr_product", columnList = "product_id"),
                @Index(name = "idx_product_attr_key", columnList = "product_id, key"),
                @Index(name = "idx_product_attr_order", columnList = "display_order")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_product_attribute_key",
                        columnNames = {"product_id", "key"}
                )
        }
)
public class ProductAttribute extends SoftDeletableAggregateRoot {

    // ====================================
    // Core Information
    // ====================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "key", nullable = false, length = 100)
    private String key;

    @Column(name = "label", nullable = false, length = 255)
    private String label;

    @Column(name = "value", columnDefinition = "TEXT")
    private String value;

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

    // ====================================
    // UI Display
    // ====================================

    @Column(name = "group_name", length = 100)
    private String groupName;

    @Column(name = "unit", length = 50)
    private String unit; // e.g., "kg", "cm", "GB"

    @Column(name = "help_text", length = 500)
    private String helpText;

    // ====================================
    // Reference to Template
    // ====================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_attribute_id")
    private CategoryAttribute categoryAttribute;

    // ====================================
    // Factory Methods
    // ====================================

    /**
     * Create from category attribute template
     *
     * Events Published:
     * - None (event published by Product aggregate when attribute is added)
     */
    public static ProductAttribute createFromTemplate(CategoryAttribute template) {
        if (template == null) {
            throw new IllegalArgumentException("Category attribute template cannot be null");
        }

        return ProductAttribute.builder()
                .categoryAttribute(template)
                .key(template.getKey())
                .label(template.getLabel())
                .attributeType(template.getAttributeType())
                .required(template.getRequired())
                .searchable(template.getSearchable())
                .filterable(template.getFilterable())
                .displayOrder(template.getDisplayOrder())
                .groupName(template.getGroupName())
                .helpText(template.getHelpText())
                .value(template.getDefaultValue())
                .build();
    }

    /**
     * Create custom attribute (not from template)
     */
    public static ProductAttribute createCustom(
            String key,
            String label,
            AttributeType type,
            String value
    ) {
        validateKey(key);
        validateLabel(label);

        return ProductAttribute.builder()
                .key(key)
                .label(label)
                .attributeType(type != null ? type : AttributeType.TEXT)
                .value(value)
                .required(false)
                .searchable(false)
                .filterable(false)
                .displayOrder(0)
                .build();
    }

    // ====================================
    // Domain Logic - Value Management
    // ====================================

    /**
     * Update attribute value
     *
     * Events Published:
     * - EntityUpdatedEvent<ProductAttribute> (with change tracking)
     */
    public void updateValue(String newValue) {
        // Validate against template if exists
        if (categoryAttribute != null) {
            categoryAttribute.validateValue(newValue);
        } else {
            // Basic validation for custom attributes
            validateCustomValue(newValue);
        }

        if (!java.util.Objects.equals(this.value, newValue)) {
            ChangeTracker tracker = new ChangeTracker();
            tracker.track("value", this.value, newValue);

            this.value = newValue;

            registerEvent(new EntityUpdatedEvent<>(this, tracker.getChanges()));
        }
    }

    /**
     * Clear attribute value
     */
    public void clearValue() {
        if (Boolean.TRUE.equals(this.required)) {
            throw new IllegalStateException(
                    String.format("Cannot clear required attribute: %s", this.label));
        }

        if (this.value != null) {
            ChangeTracker tracker = new ChangeTracker();
            tracker.track("value", this.value, null);

            this.value = null;

            registerEvent(new EntityUpdatedEvent<>(this, tracker.getChanges()));
        }
    }

    // ====================================
    // Domain Logic - Metadata Updates
    // ====================================

    /**
     * Update display information
     *
     * Events Published:
     * - EntityUpdatedEvent<ProductAttribute> (with change tracking)
     */
    public void updateDisplay(String label, Integer displayOrder, String groupName, String unit) {
        ChangeTracker tracker = new ChangeTracker();

        if (label != null && !label.equals(this.label)) {
            validateLabel(label);
            tracker.track("label", this.label, label);
            this.label = label;
        }

        if (displayOrder != null && !displayOrder.equals(this.displayOrder)) {
            tracker.track("displayOrder", this.displayOrder, displayOrder);
            this.displayOrder = displayOrder;
        }

        if (groupName != null && !groupName.equals(this.groupName)) {
            tracker.track("groupName", this.groupName, groupName);
            this.groupName = groupName;
        }

        if (unit != null && !unit.equals(this.unit)) {
            tracker.track("unit", this.unit, unit);
            this.unit = unit;
        }

        if (tracker.hasChanges()) {
            registerEvent(new EntityUpdatedEvent<>(this, tracker.getChanges()));
        }
    }

    /**
     * Update flags
     */
    public void updateFlags(Boolean searchable, Boolean filterable) {
        ChangeTracker tracker = new ChangeTracker();

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
     * Sync with category attribute template
     * Updates metadata from template while preserving the value
     */
    public void syncWithTemplate() {
        if (categoryAttribute == null) {
            throw new IllegalStateException("No template to sync with");
        }

        ChangeTracker tracker = new ChangeTracker();

        if (!this.label.equals(categoryAttribute.getLabel())) {
            tracker.track("label", this.label, categoryAttribute.getLabel());
            this.label = categoryAttribute.getLabel();
        }

        if (!this.attributeType.equals(categoryAttribute.getAttributeType())) {
            tracker.track("attributeType", this.attributeType, categoryAttribute.getAttributeType());
            this.attributeType = categoryAttribute.getAttributeType();
        }

        if (!this.required.equals(categoryAttribute.getRequired())) {
            tracker.track("required", this.required, categoryAttribute.getRequired());
            this.required = categoryAttribute.getRequired();
        }

        if (!this.searchable.equals(categoryAttribute.getSearchable())) {
            tracker.track("searchable", this.searchable, categoryAttribute.getSearchable());
            this.searchable = categoryAttribute.getSearchable();
        }

        if (!this.filterable.equals(categoryAttribute.getFilterable())) {
            tracker.track("filterable", this.filterable, categoryAttribute.getFilterable());
            this.filterable = categoryAttribute.getFilterable();
        }

        if (!this.displayOrder.equals(categoryAttribute.getDisplayOrder())) {
            tracker.track("displayOrder", this.displayOrder, categoryAttribute.getDisplayOrder());
            this.displayOrder = categoryAttribute.getDisplayOrder();
        }

        if (!java.util.Objects.equals(this.groupName, categoryAttribute.getGroupName())) {
            tracker.track("groupName", this.groupName, categoryAttribute.getGroupName());
            this.groupName = categoryAttribute.getGroupName();
        }

        if (!java.util.Objects.equals(this.helpText, categoryAttribute.getHelpText())) {
            tracker.track("helpText", this.helpText, categoryAttribute.getHelpText());
            this.helpText = categoryAttribute.getHelpText();
        }

        if (tracker.hasChanges()) {
            registerEvent(new EntityUpdatedEvent<>(this, tracker.getChanges()));
        }
    }

    // ====================================
    // Query Methods
    // ====================================

    /**
     * Check if attribute has a value
     */
    @Transient
    public boolean hasValue() {
        return value != null && !value.isBlank();
    }

    /**
     * Check if value satisfies required constraint
     */
    @Transient
    public boolean isValid() {
        if (Boolean.TRUE.equals(required)) {
            return hasValue();
        }
        return true;
    }

    /**
     * Check if attribute is from template
     */
    @Transient
    public boolean isFromTemplate() {
        return categoryAttribute != null;
    }

    /**
     * Check if attribute is custom (not from template)
     */
    @Transient
    public boolean isCustom() {
        return categoryAttribute == null;
    }

    /**
     * Get formatted value based on type
     */
    @Transient
    public String getFormattedValue() {
        if (value == null || value.isBlank()) {
            return "";
        }

        try {
            switch (attributeType) {
                case NUMBER:
                    if (unit != null) {
                        return value + " " + unit;
                    }
                    return value;

                case BOOLEAN:
                    return Boolean.parseBoolean(value) ? "Yes" : "No";

                case DATE:
                    java.time.LocalDate date = java.time.LocalDate.parse(value);
                    return date.format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy"));

                case DATETIME:
                    java.time.LocalDateTime dateTime = java.time.LocalDateTime.parse(value);
                    return dateTime.format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"));

                default:
                    return value;
            }
        } catch (Exception e) {
            return value; // Return raw value if formatting fails
        }
    }

    /**
     * Get typed value
     */
    @Transient
    public Object getTypedValue() {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            switch (attributeType) {
                case NUMBER:
                    return Double.parseDouble(value);
                case BOOLEAN:
                    return Boolean.parseBoolean(value);
                case DATE:
                    return java.time.LocalDate.parse(value);
                case DATETIME:
                    return java.time.LocalDateTime.parse(value);
                default:
                    return value;
            }
        } catch (Exception e) {
            return value; // Return string if parsing fails
        }
    }

    // ====================================
    // Validation
    // ====================================

    /**
     * Validate custom attribute value (when no template exists)
     */
    private void validateCustomValue(String val) {
        if (Boolean.TRUE.equals(required) && (val == null || val.isBlank())) {
            throw new IllegalArgumentException(
                    String.format("Required attribute '%s' cannot be empty", label));
        }

        if (val != null && !val.isBlank()) {
            // Basic type validation
            validateTypeSpecific(val);
        }
    }

    private void validateTypeSpecific(String val) {
        try {
            switch (attributeType) {
                case NUMBER:
                    Double.parseDouble(val);
                    break;
                case BOOLEAN:
                    if (!val.equalsIgnoreCase("true") && !val.equalsIgnoreCase("false")) {
                        throw new IllegalArgumentException("Must be true or false");
                    }
                    break;
                case DATE:
                    java.time.LocalDate.parse(val);
                    break;
                case DATETIME:
                    java.time.LocalDateTime.parse(val);
                    break;
                case EMAIL:
                    if (!val.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                        throw new IllegalArgumentException("Invalid email format");
                    }
                    break;
                case URL:
                    new java.net.URL(val);
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    String.format("Invalid %s value for attribute '%s': %s",
                            attributeType.name().toLowerCase(), label, e.getMessage()));
        }
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

    /**
     * Clone attribute for variant or duplication
     */
    public ProductAttribute clone() {
        return ProductAttribute.builder()
                .categoryAttribute(this.categoryAttribute)
                .key(this.key)
                .label(this.label)
                .value(this.value)
                .attributeType(this.attributeType)
                .required(this.required)
                .searchable(this.searchable)
                .filterable(this.filterable)
                .displayOrder(this.displayOrder)
                .groupName(this.groupName)
                .unit(this.unit)
                .helpText(this.helpText)
                .build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProductAttribute)) return false;
        ProductAttribute that = (ProductAttribute) o;
        return getId() != null && getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    protected DomainEvent createDeletedEvent(String deletedBy) {
        return null;
    }

    @Override
    protected DomainEvent createRestoredEvent(String restoredBy, String previousDeletedBy) {
        return null;
    }

    @Override
    public String toString() {
        return String.format("ProductAttribute[id=%s, key=%s, label=%s, value=%s, type=%s]",
                getId(), key, label, value, attributeType);
    }
}
