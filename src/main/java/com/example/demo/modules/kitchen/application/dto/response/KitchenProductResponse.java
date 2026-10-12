package com.example.demo.modules.kitchen.application.dto.response;

import com.example.demo.common.base.dto.BaseDTO;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Kitchen Product Response DTO - Full detail (product page)
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Kitchen product full response")
public class KitchenProductResponse extends BaseDTO {

    @Schema(description = "SKU", example = "JP-SG-CASH-B150PO")
    private String sku;

    @Schema(description = "Product name")
    private String name;

    @Schema(description = "URL slug")
    private String slug;

    @Schema(description = "Short description")
    private String shortDescription;

    @Schema(description = "Full description")
    private String description;

    // Context
    @Schema(description = "Collection ID")
    private UUID collectionId;

    @Schema(description = "Collection name")
    private String collectionName;

    @Schema(description = "Collection slug")
    private String collectionSlug;

    @Schema(description = "Range ID")
    private UUID rangeId;

    @Schema(description = "Range name")
    private String rangeName;

    @Schema(description = "Range slug")
    private String rangeSlug;

    @Schema(description = "Unit category ID")
    private UUID unitCategoryId;

    @Schema(description = "Unit category name")
    private String unitCategoryName;

    @Schema(description = "Unit category code")
    private String unitCategoryCode;

    // Dimensions
    @Schema(description = "Width in mm")
    private Integer widthMm;

    @Schema(description = "Height in mm")
    private Integer heightMm;

    @Schema(description = "Depth in mm")
    private Integer depthMm;

    @Schema(description = "Formatted dimensions string", example = "150 x 720 x 560 mm")
    private String formattedDimensions;

    // Pricing
    @Schema(description = "Base price amount")
    private BigDecimal basePrice;

    @Schema(description = "Currency", example = "GBP")
    private String currency;

    // Flags
    @Schema(description = "Whether product is active")
    private Boolean active;

    @Schema(description = "Whether product is popular")
    private Boolean isPopular;

    @Schema(description = "Whether product can be ordered standalone")
    private Boolean standaloneOrderable;

    // Images
    @Schema(description = "Product images")
    private List<KitchenProductImageResponse> images;

    @Schema(description = "Primary image URL")
    private String primaryImageUrl;

    // Variants
    @Schema(description = "All variants")
    private List<KitchenProductVariantResponse> variants;

    @Schema(description = "Default variant")
    private KitchenProductVariantResponse defaultVariant;

    @Schema(description = "Available cabinet colours (from variants)")
    private List<KitchenColourResponse> availableCabinetColours;

    // SEO
    @Schema(description = "Meta title")
    private String metaTitle;

    @Schema(description = "Meta description")
    private String metaDescription;
}