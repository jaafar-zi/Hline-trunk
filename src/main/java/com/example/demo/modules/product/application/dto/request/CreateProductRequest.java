package com.example.demo.modules.product.application.dto.request;

import com.example.demo.common.base.dto.BaseRequest;
import com.example.demo.common.normalization.Normalize;
import com.example.demo.common.validation.groups.Create;
import com.example.demo.common.validator.constraints.ValidCurrency;
import com.example.demo.common.validator.constraints.ValidSKU;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

/**
 * Request DTO for creating a new product
 *
 * @author PIM Team
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Create product request")
public class CreateProductRequest extends BaseRequest {

    private static final long serialVersionUID = 1L;

    // ====================================
    // Basic Information
    // ====================================

    @Schema(
        description = "Product SKU (leave null for auto-generation)",
        example = "PROD-20240115-A1B2C3"
    )
    @ValidSKU(allowNull = true, groups = Create.class)
    @Normalize({Normalize.Strategy.TRIM, Normalize.Strategy.UPPERCASE})
    private String sku;

    @Schema(
        description = "Product name",
        example = "Premium Wireless Headphones",
        required = true
    )
    @NotBlank(message = "Product name is required", groups = Create.class)
    @Size(min = 3, max = 500, message = "Product name must be between 3 and 500 characters", groups = Create.class)
    @Normalize(Normalize.Strategy.TRIM)
    private String name;

    @Schema(
        description = "Short description (for listings)",
        example = "High-quality wireless headphones with noise cancellation"
    )
    @Size(max = 1000, message = "Short description cannot exceed 1000 characters", groups = Create.class)
    @Normalize(Normalize.Strategy.TRIM)
    private String shortDescription;

    @Schema(
        description = "Long description (detailed product information)",
        example = "These premium wireless headphones feature advanced noise cancellation..."
    )
    @Size(max = 10000, message = "Long description cannot exceed 10000 characters", groups = Create.class)
    @Normalize(Normalize.Strategy.TRIM)
    private String longDescription;

    @Schema(
        description = "Category ID",
        example = "550e8400-e29b-41d4-a716-446655440000",
        required = true
    )
    @NotNull(message = "Category is required", groups = Create.class)
    private UUID categoryId;

    @Schema(
        description = "Brand name",
        example = "AudioTech"
    )
    @Size(max = 255, message = "Brand name cannot exceed 255 characters", groups = Create.class)
    @Normalize(Normalize.Strategy.TRIM)
    private String brand;

    @Schema(
        description = "Manufacturer name",
        example = "AudioTech Industries"
    )
    @Size(max = 255, message = "Manufacturer name cannot exceed 255 characters", groups = Create.class)
    @Normalize(Normalize.Strategy.TRIM)
    private String manufacturer;

    // ====================================
    // Dimensions
    // ====================================

    @Schema(description = "Product dimensions")
    @Valid
    private DimensionsRequest dimensions;

    // ====================================
    // SEO
    // ====================================

    @Schema(
        description = "SEO meta title",
        example = "Buy Premium Wireless Headphones | Best Price"
    )
    @Size(max = 255, message = "Meta title cannot exceed 255 characters", groups = Create.class)
    @Normalize(Normalize.Strategy.TRIM)
    private String metaTitle;

    @Schema(
        description = "SEO meta description",
        example = "Shop premium wireless headphones with advanced noise cancellation..."
    )
    @Size(max = 500, message = "Meta description cannot exceed 500 characters", groups = Create.class)
    @Normalize(Normalize.Strategy.TRIM)
    private String metaDescription;

    @Schema(
        description = "Search keywords (comma-separated)",
        example = "wireless, headphones, bluetooth, noise cancellation"
    )
    @Size(max = 1000, message = "Search keywords cannot exceed 1000 characters", groups = Create.class)
    @Normalize(Normalize.Strategy.TRIM)
    private String searchKeywords;

    // ====================================
    // Attributes
    // ====================================

    @Schema(
        description = "Product attributes (key-value pairs)",
        example = "{\"color\": \"Black\", \"connectivity\": \"Bluetooth 5.0\"}"
    )
    private Map<String, String> attributes;

    // ====================================
    // Pricing (Optional - can be set later)
    // ====================================

    @Schema(description = "Base price")
    @Valid
    private CreatePriceRequest basePrice;

    // ====================================
    // Inventory (Optional - can be set later)
    // ====================================

    @Schema(description = "Initial inventory quantity")
    @Min(value = 0, message = "Initial quantity cannot be negative", groups = Create.class)
    private Integer initialQuantity;

    @Schema(
        description = "Track inventory",
        example = "true"
    )
    private Boolean trackInventory;

    // ====================================
    // Options
    // ====================================

    @Schema(
        description = "Auto-publish after creation",
        example = "false",
        defaultValue = "false"
    )
    private Boolean autoPublish = false;

    @Schema(
        description = "Generate SKU with category prefix",
        example = "true",
        defaultValue = "false"
    )
    private Boolean useCategoryPrefix = false;

    // ====================================
    // Normalization
    // ====================================

    @Override
    public void normalize() {
        super.normalize();

        // Normalize attributes map
        if (attributes != null && !attributes.isEmpty()) {
            Map<String, String> normalized = new java.util.HashMap<>();
            attributes.forEach((key, value) -> {
                if (key != null && value != null) {
                    String normalizedKey = key.trim().toLowerCase();
                    String normalizedValue = value.trim();
                    normalized.put(normalizedKey, normalizedValue);
                }
            });
            this.attributes = normalized;
        }

        // Normalize nested objects
        if (dimensions != null) {
            dimensions.normalize();
        }

        if (basePrice != null) {
            basePrice.normalize();
        }
    }

    // ====================================
    // Nested DTOs
    // ====================================

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Product dimensions")
    public static class DimensionsRequest {

        @Schema(description = "Length in cm", example = "20.5", required = true)
        @NotNull(message = "Length is required")
        @DecimalMin(value = "0.01", message = "Length must be positive")
        @DecimalMax(value = "9999.99", message = "Length cannot exceed 9999.99")
        private BigDecimal length;

        @Schema(description = "Width in cm", example = "15.0", required = true)
        @NotNull(message = "Width is required")
        @DecimalMin(value = "0.01", message = "Width must be positive")
        @DecimalMax(value = "9999.99", message = "Width cannot exceed 9999.99")
        private BigDecimal width;

        @Schema(description = "Height in cm", example = "8.5", required = true)
        @NotNull(message = "Height is required")
        @DecimalMin(value = "0.01", message = "Height must be positive")
        @DecimalMax(value = "9999.99", message = "Height cannot exceed 9999.99")
        private BigDecimal height;

        @Schema(description = "Weight in kg", example = "0.35", required = true)
        @NotNull(message = "Weight is required")
        @DecimalMin(value = "0.01", message = "Weight must be positive")
        @DecimalMax(value = "9999.99", message = "Weight cannot exceed 9999.99")
        private BigDecimal weight;

        @Schema(description = "Dimension unit", example = "CM", defaultValue = "CM")
        @Normalize(Normalize.Strategy.UPPERCASE)
        private String dimensionUnit = "CM";

        @Schema(description = "Weight unit", example = "KG", defaultValue = "KG")
        @Normalize(Normalize.Strategy.UPPERCASE)
        private String weightUnit = "KG";

        public void normalize() {
            if (dimensionUnit != null) {
                dimensionUnit = dimensionUnit.toUpperCase();
            }
            if (weightUnit != null) {
                weightUnit = weightUnit.toUpperCase();
            }
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Price information")
    public static class CreatePriceRequest {

        @Schema(description = "Price amount", example = "99.99", required = true)
        @NotNull(message = "Price amount is required")
        @DecimalMin(value = "0.01", message = "Price must be positive")
        private BigDecimal amount;

        @Schema(description = "Currency code (ISO 4217)", example = "USD", required = true)
        @NotBlank(message = "Currency is required")
        @ValidCurrency
        @Normalize(Normalize.Strategy.UPPERCASE)
        private String currency;

        @Schema(description = "Tax inclusive", example = "false", defaultValue = "false")
        private Boolean taxInclusive = false;

        @Schema(description = "Tax rate (percentage)", example = "20.0")
        @DecimalMin(value = "0", message = "Tax rate cannot be negative")
        @DecimalMax(value = "100", message = "Tax rate cannot exceed 100%")
        private BigDecimal taxRate;

        public void normalize() {
            if (currency != null) {
                currency = currency.trim().toUpperCase();
            }
        }
    }
}