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
 * Kitchen Collection Response DTO - Full detail
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Kitchen collection response")
public class KitchenCollectionResponse extends BaseDTO {

    @Schema(description = "Finish group ID")
    private UUID finishGroupId;

    @Schema(description = "Finish group name")
    private String finishGroupName;

    @Schema(description = "Range ID")
    private UUID rangeId;

    @Schema(description = "Range name", example = "J-Pull")
    private String rangeName;

    @Schema(description = "Range slug", example = "j-pull")
    private String rangeSlug;

    @Schema(description = "Colour ID")
    private UUID colourId;

    @Schema(description = "Colour name", example = "Cashmere")
    private String colourName;

    @Schema(description = "Colour hex value", example = "#F5E6D3")
    private String colourHex;

    @Schema(description = "Collection name", example = "J-Pull SuperGloss Cashmere")
    private String name;

    @Schema(description = "URL slug", example = "j-pull-supergloss-cashmere")
    private String slug;

    @Schema(description = "Short description")
    private String shortDescription;

    @Schema(description = "Full description")
    private String description;

    @Schema(description = "Hero image URL")
    private String heroImageUrl;

    @Schema(description = "Gallery image URLs")
    private List<String> galleryImageUrls;

    @Schema(description = "Feature list")
    private List<String> features;

    @Schema(description = "Whether collection is active")
    private Boolean active;

    @Schema(description = "Lead time in days")
    private Integer leadTimeDays;

    @Schema(description = "Default cabinet colour")
    private KitchenColourResponse defaultCabinetColour;

    @Schema(description = "Available cabinet colours")
    private List<KitchenColourResponse> availableCabinetColours;

    @Schema(description = "Sibling collections (same range, different colour)")
    private List<KitchenCollectionSummary> siblingCollections;

    @Schema(description = "Products grouped by category")
    private List<KitchenUnitCategoryWithProductsResponse> unitCategories;

    @Schema(description = "Starting from price")
    private BigDecimal startingFromPrice;

    @Schema(description = "Total active product count")
    private Long productCount;

    @Schema(description = "Meta title for SEO")
    private String metaTitle;

    @Schema(description = "Meta description for SEO")
    private String metaDescription;
}