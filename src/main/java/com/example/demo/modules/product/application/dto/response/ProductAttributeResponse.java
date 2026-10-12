package com.example.demo.modules.product.application.dto.response;

import com.example.demo.common.base.dto.BaseDTO;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

/**
 * Lean Product Attribute Response - Performance optimized
 *
 * @author PIM Team
 * @version 1.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Product attribute response - optimized")
public class ProductAttributeResponse extends BaseDTO {

    private static final long serialVersionUID = 1L;

    // ====================================
    // Reference
    // ====================================

    @Schema(description = "Product ID reference (use ProductService if needed)")
    private UUID productId;

    @Schema(description = "Category attribute template ID (if from template)")
    private UUID categoryAttributeId;

    // ====================================
    // Core Attribute Data
    // ====================================

    @Schema(
        description = "Attribute key (unique within product)",
        example = "material",
        required = true
    )
    private String key;

    @Schema(
        description = "Attribute label for display",
        example = "Material",
        required = true
    )
    private String label;

    @Schema(
        description = "Attribute value",
        example = "Cotton"
    )
    private String value;

    @Schema(
        description = "Formatted value (with unit if applicable)",
        example = "100 kg"
    )
    private String formattedValue;

    @Schema(
        description = "Attribute type",
        example = "TEXT",
        allowableValues = {"TEXT", "NUMBER", "BOOLEAN", "DATE", "DATETIME", "SELECT", "MULTISELECT", "EMAIL", "URL", "COLOR"}
    )
    private String attributeType;

    // ====================================
    // Display & Behavior
    // ====================================

    @Schema(
        description = "Is required attribute",
        example = "false"
    )
    private Boolean required;

    @Schema(
        description = "Is searchable",
        example = "true"
    )
    private Boolean searchable;

    @Schema(
        description = "Is filterable",
        example = "true"
    )
    private Boolean filterable;

    @Schema(
        description = "Display order",
        example = "1"
    )
    private Integer displayOrder;

    @Schema(
        description = "Attribute group name",
        example = "Specifications"
    )
    private String groupName;

    @Schema(
        description = "Unit of measurement",
        example = "kg"
    )
    private String unit;

    @Schema(
        description = "Help text for users",
        example = "Primary material used in manufacturing"
    )
    private String helpText;

    // ====================================
    // Helper Methods
    // ====================================

    /**
     * Check if attribute has a value
     */
    public boolean hasValue() {
        return value != null && !value.isBlank();
    }

    /**
     * Check if attribute is from template
     */
    public boolean isFromTemplate() {
        return categoryAttributeId != null;
    }

    /**
     * Check if attribute is custom
     */
    public boolean isCustom() {
        return categoryAttributeId == null;
    }

    /**
     * Check if value is valid (required constraint)
     */
    public boolean isValid() {
        if (Boolean.TRUE.equals(required)) {
            return hasValue();
        }
        return true;
    }

    /**
     * Get display value (formatted or raw)
     */
    public String getDisplayValue() {
        if (formattedValue != null && !formattedValue.isBlank()) {
            return formattedValue;
        }
        return value;
    }

    @Override
    public String toString() {
        return String.format("ProductAttributeResponse[id=%s, key=%s, label=%s, value=%s]",
            id, key, label, value);
    }
}