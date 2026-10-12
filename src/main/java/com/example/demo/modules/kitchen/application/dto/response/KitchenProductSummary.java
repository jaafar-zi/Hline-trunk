package com.example.demo.modules.kitchen.application.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Lightweight product summary for collection listing page
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Kitchen product summary")
public class KitchenProductSummary {

    @Schema(description = "Product ID")
    private UUID id;

    @Schema(description = "SKU")
    private String sku;

    @Schema(description = "Product name")
    private String name;

    @Schema(description = "URL slug")
    private String slug;

    @Schema(description = "Width in mm")
    private Integer widthMm;

    @Schema(description = "Height in mm")
    private Integer heightMm;

    @Schema(description = "Depth in mm")
    private Integer depthMm;

    @Schema(description = "Formatted dimensions")
    private String formattedDimensions;

    @Schema(description = "Starting from price")
    private BigDecimal startingFromPrice;

    @Schema(description = "Currency")
    private String currency;

    @Schema(description = "Primary image URL")
    private String primaryImageUrl;

    @Schema(description = "Unit category code")
    private String unitCategoryCode;

    @Schema(description = "Unit category name")
    private String unitCategoryName;

    @Schema(description = "Whether product is popular")
    private Boolean isPopular;

    @Schema(description = "Whether product is in stock")
    private Boolean inStock;

    @Schema(description = "Whether product has multiple variants")
    private Boolean hasMultipleVariants;
}