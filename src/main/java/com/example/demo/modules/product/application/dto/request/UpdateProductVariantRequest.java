package com.example.demo.modules.product.application.dto.request;

import com.example.demo.common.base.dto.BaseRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Request DTO for updating a product variant
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
@Schema(description = "Update product variant request")
public class UpdateProductVariantRequest extends BaseRequest {

    private static final long serialVersionUID = 1L;

    @Schema(
        description = "Variant name",
        example = "Large - Black (Updated)"
    )
    @Size(min = 2, max = 255, message = "Variant name must be between 2 and 255 characters")
    private String name;

    @Schema(
        description = "Variant description",
        example = "Updated variant description"
    )
    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    @Schema(
        description = "Variant attributes",
        example = "{\"size\": \"Large\", \"color\": \"Black\", \"material\": \"Leather\"}"
    )
    private Map<String, String> attributes;

    @Schema(
        description = "Display order",
        example = "2"
    )
    @Min(value = 0, message = "Display order cannot be negative")
    private Integer displayOrder;

    @Schema(
        description = "Barcode",
        example = "1234567890124"
    )
    @Size(max = 100, message = "Barcode cannot exceed 100 characters")
    private String barcode;

    @Schema(
        description = "Barcode type",
        example = "EAN13"
    )
    @Size(max = 20, message = "Barcode type cannot exceed 20 characters")
    private String barcodeType;

    @Schema(
        description = "Supplier SKU",
        example = "SUP-12346"
    )
    @Size(max = 100, message = "Supplier SKU cannot exceed 100 characters")
    private String supplierSku;

    @Schema(
        description = "Updated price amount",
        example = "114.99"
    )
    @DecimalMin(value = "0.01", message = "Price must be positive")
    private BigDecimal price;

    @Override
    public void validate() {
        super.validate();

        if (name != null) {
            name = name.trim();
        }

        if (attributes != null && !attributes.isEmpty()) {
            Map<String, String> normalizedAttributes = new java.util.HashMap<>();
            attributes.forEach((key, value) -> {
                if (key != null && value != null) {
                    normalizedAttributes.put(key.trim().toLowerCase(), value.trim());
                }
            });
            this.attributes = normalizedAttributes;
        }
    }
}