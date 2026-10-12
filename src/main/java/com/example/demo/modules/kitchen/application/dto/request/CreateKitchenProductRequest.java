package com.example.demo.modules.kitchen.application.dto.request;

import com.example.demo.common.base.dto.BaseRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Create Kitchen Product Request
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Create kitchen product request")
public class CreateKitchenProductRequest extends BaseRequest {

    @NotNull(message = "Collection ID is required")
    @Schema(description = "Collection ID", required = true)
    private UUID collectionId;

    @NotNull(message = "Unit category ID is required")
    @Schema(description = "Unit category ID", required = true)
    private UUID unitCategoryId;

    @NotBlank(message = "SKU is required")
    @Size(max = 50)
    @Schema(description = "Product SKU", example = "JP-SG-CASH-B150PO", required = true)
    private String sku;

    @NotBlank(message = "Product name is required")
    @Size(max = 300)
    @Schema(description = "Product name", example = "150 Base Pull Out Cabinet", required = true)
    private String name;

    @Schema(description = "Short description")
    private String shortDescription;

    @Schema(description = "Full description")
    private String description;

    @Positive(message = "Width must be positive")
    @Schema(description = "Width in mm", example = "150")
    private Integer widthMm;

    @Positive(message = "Height must be positive")
    @Schema(description = "Height in mm", example = "720")
    private Integer heightMm;

    @Positive(message = "Depth must be positive")
    @Schema(description = "Depth in mm", example = "560")
    private Integer depthMm;

    @NotNull(message = "Base price is required")
    @DecimalMin(value = "0.00", message = "Price cannot be negative")
    @Schema(description = "Base price amount", example = "120.00", required = true)
    private BigDecimal basePriceAmount;

    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be 3 characters")
    @Schema(description = "Currency code", example = "GBP", required = true)
    private String currency;

    @Schema(description = "Whether product can be ordered standalone", defaultValue = "true")
    private Boolean standaloneOrderable = true;

    @Schema(description = "Display order", example = "0")
    private Integer sortOrder;

    @Override
    public void normalize() {
        super.normalize();
        if (sku != null) sku = sku.trim().toUpperCase();
        if (name != null) name = name.trim();
        if (currency != null) currency = currency.trim().toUpperCase();
    }
}