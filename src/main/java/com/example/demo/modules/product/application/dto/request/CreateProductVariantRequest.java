package com.example.demo.modules.product.application.dto.request;

import com.example.demo.common.base.dto.BaseRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Request DTO for creating a product variant
 *
 * @author PIM Team
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Create product variant request")
public class CreateProductVariantRequest extends BaseRequest {

    private static final long serialVersionUID = 1L;

    @Schema(
        description = "Variant SKU (leave null for auto-generation)",
        example = "PROD-20240115-A1B2C3-L-BLK"
    )
    @Size(max = 50, message = "SKU cannot exceed 50 characters")
    @Pattern(
        regexp = "^[A-Z0-9-]*$",
        message = "SKU must contain only uppercase letters, numbers, and hyphens"
    )
    private String sku;

    @Schema(
        description = "Variant name",
        example = "Large - Black",
        required = true
    )
    @NotBlank(message = "Variant name is required")
    @Size(min = 2, max = 255, message = "Variant name must be between 2 and 255 characters")
    private String name;

    @Schema(
        description = "Variant description",
        example = "Large size in black color"
    )
    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    @Schema(
        description = "Variant attributes (e.g., size, color)",
        example = "{\"size\": \"Large\", \"color\": \"Black\"}",
        required = true
    )
    @NotNull(message = "Attributes are required")
    @Size(min = 1, message = "At least one attribute is required")
    private Map<String, String> attributes;

    @Schema(
        description = "Variant-specific price (optional, uses parent product price if not set)"
    )
    @Valid
    private VariantPriceRequest price;

    @Schema(
        description = "Initial inventory quantity",
        example = "50"
    )
    @Min(value = 0, message = "Initial quantity cannot be negative")
    private Integer initialQuantity = 0;

    @Schema(
        description = "Display order",
        example = "1"
    )
    @Min(value = 0, message = "Display order cannot be negative")
    private Integer displayOrder = 0;

    @Schema(
        description = "Is this the default variant?",
        example = "false",
        defaultValue = "false"
    )
    private Boolean isDefault = false;

    @Schema(
        description = "Barcode (EAN, UPC, etc.)",
        example = "1234567890123"
    )
    @Size(max = 100, message = "Barcode cannot exceed 100 characters")
    private String barcode;

    @Schema(
        description = "Barcode type",
        example = "EAN13",
        allowableValues = {"EAN13", "UPC", "ISBN", "CODE128"}
    )
    @Size(max = 20, message = "Barcode type cannot exceed 20 characters")
    private String barcodeType;

    @Schema(
        description = "Supplier SKU",
        example = "SUP-12345"
    )
    @Size(max = 100, message = "Supplier SKU cannot exceed 100 characters")
    private String supplierSku;

    @Override
    public void validate() {
        super.validate();

        if (name != null) {
            name = name.trim();
        }

        if (sku != null && !sku.isBlank()) {
            sku = sku.trim().toUpperCase();
        }

        // Validate attributes
        if (attributes != null && !attributes.isEmpty()) {
            // Normalize attribute keys to lowercase
            Map<String, String> normalizedAttributes = new java.util.HashMap<>();
            attributes.forEach((key, value) -> {
                if (key != null && value != null) {
                    normalizedAttributes.put(key.trim().toLowerCase(), value.trim());
                }
            });
            this.attributes = normalizedAttributes;
        }

        if (price != null) {
            price.validate();
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Variant price information")
    public static class VariantPriceRequest {

        @Schema(description = "Price amount", example = "109.99")
        @DecimalMin(value = "0.01", message = "Price must be positive")
        private BigDecimal amount;

        @Schema(description = "Currency code", example = "USD")
        @Size(min = 3, max = 3, message = "Currency must be 3 characters")
        @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be 3 uppercase letters")
        private String currency;

        @Schema(
            description = "Price adjustment from base product price",
            example = "10.00"
        )
        private BigDecimal priceAdjustment;

        @Schema(
            description = "Price adjustment type",
            example = "FIXED",
            allowableValues = {"FIXED", "PERCENTAGE"}
        )
        private String priceAdjustmentType;

        public void validate() {
            if (currency != null) {
                currency = currency.trim().toUpperCase();
            }
            if (priceAdjustmentType != null) {
                priceAdjustmentType = priceAdjustmentType.trim().toUpperCase();
            }

            // Either amount OR adjustment should be provided, not both
            if (amount != null && priceAdjustment != null) {
                throw new IllegalArgumentException(
                    "Provide either price amount or price adjustment, not both"
                );
            }

            // If adjustment is provided, type is required
            if (priceAdjustment != null && priceAdjustmentType == null) {
                throw new IllegalArgumentException(
                    "Price adjustment type is required when price adjustment is provided"
                );
            }
        }
    }
}