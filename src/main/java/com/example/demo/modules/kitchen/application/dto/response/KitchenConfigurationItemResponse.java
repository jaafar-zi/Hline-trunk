package com.example.demo.modules.kitchen.application.dto.response;

import com.example.demo.common.base.dto.BaseDTO;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Kitchen Configuration Item Response DTO
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Kitchen configuration item response")
public class KitchenConfigurationItemResponse extends BaseDTO {

    @Schema(description = "Position in configuration")
    private Integer position;

    @Schema(description = "Optional label", example = "Left of sink")
    private String label;

    @Schema(description = "Product ID")
    private UUID productId;

    @Schema(description = "Product name")
    private String productName;

    @Schema(description = "Product slug")
    private String productSlug;

    @Schema(description = "Product primary image URL")
    private String productImageUrl;

    @Schema(description = "Unit category name")
    private String unitCategoryName;

    @Schema(description = "Dimensions string")
    private String dimensions;

    @Schema(description = "Variant ID")
    private UUID variantId;

    @Schema(description = "Variant SKU")
    private String variantSku;

    @Schema(description = "Cabinet colour name")
    private String cabinetColourName;

    @Schema(description = "Cabinet colour hex")
    private String cabinetColourHex;

    @Schema(description = "Assembly type display name")
    private String assemblyType;

    @Schema(description = "Whether colour has been overridden individually")
    private Boolean colourOverridden;

    @Schema(description = "Quantity")
    private Integer quantity;

    @Schema(description = "Unit price")
    private BigDecimal unitPrice;

    @Schema(description = "Line total (unitPrice × quantity)")
    private BigDecimal lineTotal;

    @Schema(description = "Currency")
    private String currency;
}