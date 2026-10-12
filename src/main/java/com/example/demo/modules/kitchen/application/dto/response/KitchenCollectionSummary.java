package com.example.demo.modules.kitchen.application.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Lightweight collection summary for listing pages and switchers
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Kitchen collection summary")
public class KitchenCollectionSummary {

    @Schema(description = "Collection ID")
    private UUID id;

    @Schema(description = "Collection name")
    private String name;

    @Schema(description = "URL slug")
    private String slug;

    @Schema(description = "Range slug (for URL building)")
    private String rangeSlug;

    @Schema(description = "Colour name")
    private String colourName;

    @Schema(description = "Colour hex value")
    private String colourHex;

    @Schema(description = "Swatch image URL")
    private String swatchImageUrl;

    @Schema(description = "Hero image URL")
    private String heroImageUrl;

    @Schema(description = "Starting from price")
    private BigDecimal startingFromPrice;

    @Schema(description = "Product count")
    private Long productCount;

    @Schema(description = "Whether this is the currently selected collection")
    private Boolean selected;

    @Schema(description = "Whether collection is active")
    private Boolean active;
}