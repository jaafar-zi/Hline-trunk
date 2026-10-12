package com.example.demo.modules.kitchen.application.dto.response;

import com.example.demo.common.base.dto.BaseDTO;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Kitchen Product Variant Response DTO
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Kitchen product variant response")
public class KitchenProductVariantResponse extends BaseDTO {

    @Schema(description = "Product ID")
    private UUID productId;

    @Schema(description = "SKU", example = "JP-SG-CASH-B150PO-WHT-FP")
    private String sku;

    @Schema(description = "Variant name", example = "White Cabinet - Flatpack")
    private String name;

    @Schema(description = "Cabinet colour ID")
    private UUID cabinetColourId;

    @Schema(description = "Cabinet colour name", example = "White")
    private String cabinetColourName;

    @Schema(description = "Cabinet colour hex")
    private String cabinetColourHex;

    @Schema(description = "Assembly type", example = "FLATPACK")
    private String assemblyType;

    @Schema(description = "Assembly type display name", example = "Flatpack")
    private String assemblyTypeDisplayName;

    @Schema(description = "Price amount")
    private BigDecimal price;

    @Schema(description = "Currency", example = "GBP")
    private String currency;

    @Schema(description = "Cabinet colour premium amount")
    private BigDecimal cabinetColourPremium;

    @Schema(description = "Assembly premium amount")
    private BigDecimal assemblyPremium;

    @Schema(description = "Whether variant is in stock")
    private Boolean inStock;

    @Schema(description = "Stock quantity")
    private Integer stockQuantity;

    @Schema(description = "Lead time in days")
    private Integer leadTimeDays;

    @Schema(description = "Whether variant is active")
    private Boolean active;

    @Schema(description = "Whether this is the default variant")
    private Boolean isDefault;

    @Schema(description = "Display order")
    private Integer sortOrder;
}