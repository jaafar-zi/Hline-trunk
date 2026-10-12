package com.example.demo.modules.kitchen.application.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;
import java.util.UUID;

/**
 * Unit category with its products - used on collection detail page
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Unit category with products")
public class KitchenUnitCategoryWithProductsResponse {

    @Schema(description = "Category ID")
    private UUID id;

    @Schema(description = "Category code", example = "BASE")
    private String code;

    @Schema(description = "Category name", example = "Base Kitchen Cabinets")
    private String name;

    @Schema(description = "Category slug")
    private String slug;

    @Schema(description = "Sort order")
    private Integer sortOrder;

    @Schema(description = "Products in this category")
    private List<KitchenProductSummary> products;

    @Schema(description = "Total product count")
    private Integer productCount;
}