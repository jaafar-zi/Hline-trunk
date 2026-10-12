package com.example.demo.modules.product.application.dto.request;

import com.example.demo.common.base.dto.BaseRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Request DTO for updating an existing product
 *
 * All fields are optional - only provided fields will be updated
 *
 * @author PIM Team
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Update product request")
public class UpdateProductRequest extends BaseRequest {

    private static final long serialVersionUID = 1L;

    @Schema(
        description = "Product name",
        example = "Premium Wireless Headphones - Updated"
    )
    @Size(min = 3, max = 500, message = "Product name must be between 3 and 500 characters")
    private String name;

    @Schema(
        description = "Short description",
        example = "Updated short description"
    )
    @Size(max = 1000, message = "Short description cannot exceed 1000 characters")
    private String shortDescription;

    @Schema(
        description = "Long description",
        example = "Updated detailed product information"
    )
    @Size(max = 10000, message = "Long description cannot exceed 10000 characters")
    private String longDescription;

    @Schema(
        description = "Category ID",
        example = "550e8400-e29b-41d4-a716-446655440000"
    )
    private UUID categoryId;

    @Schema(
        description = "Brand name",
        example = "AudioTech Pro"
    )
    @Size(max = 255, message = "Brand name cannot exceed 255 characters")
    private String brand;

    @Schema(
        description = "Manufacturer name",
        example = "AudioTech Industries Ltd"
    )
    @Size(max = 255, message = "Manufacturer name cannot exceed 255 characters")
    private String manufacturer;

    @Schema(description = "Product dimensions")
    @Valid
    private UpdateDimensionsRequest dimensions;

    @Schema(
        description = "SEO meta title",
        example = "Buy Premium Wireless Headphones | Updated"
    )
    @Size(max = 255, message = "Meta title cannot exceed 255 characters")
    private String metaTitle;

    @Schema(
        description = "SEO meta description",
        example = "Updated meta description"
    )
    @Size(max = 500, message = "Meta description cannot exceed 500 characters")
    private String metaDescription;

    @Schema(
        description = "Search keywords",
        example = "wireless, headphones, premium, noise cancellation"
    )
    @Size(max = 1000, message = "Search keywords cannot exceed 1000 characters")
    private String searchKeywords;

    @Override
    public void validate() {
        super.validate();

        // Trim strings
        if (name != null) {
            name = name.trim();
        }
        if (brand != null) {
            brand = brand.trim();
        }
        if (manufacturer != null) {
            manufacturer = manufacturer.trim();
        }

        // Validate dimensions if provided
        if (dimensions != null) {
            dimensions.validate();
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Updated dimensions")
    public static class UpdateDimensionsRequest {

        @Schema(description = "Length in cm", example = "21.0")
        @DecimalMin(value = "0.01", message = "Length must be positive")
        @DecimalMax(value = "9999.99", message = "Length cannot exceed 9999.99")
        private BigDecimal length;

        @Schema(description = "Width in cm", example = "15.5")
        @DecimalMin(value = "0.01", message = "Width must be positive")
        @DecimalMax(value = "9999.99", message = "Width cannot exceed 9999.99")
        private BigDecimal width;

        @Schema(description = "Height in cm", example = "9.0")
        @DecimalMin(value = "0.01", message = "Height must be positive")
        @DecimalMax(value = "9999.99", message = "Height cannot exceed 9999.99")
        private BigDecimal height;

        @Schema(description = "Weight in kg", example = "0.38")
        @DecimalMin(value = "0.01", message = "Weight must be positive")
        @DecimalMax(value = "9999.99", message = "Weight cannot exceed 9999.99")
        private BigDecimal weight;

        @Schema(description = "Dimension unit", example = "CM")
        private String dimensionUnit;

        @Schema(description = "Weight unit", example = "KG")
        private String weightUnit;

        public void validate() {
            if (dimensionUnit != null) {
                dimensionUnit = dimensionUnit.toUpperCase();
            }
            if (weightUnit != null) {
                weightUnit = weightUnit.toUpperCase();
            }
        }
    }
}