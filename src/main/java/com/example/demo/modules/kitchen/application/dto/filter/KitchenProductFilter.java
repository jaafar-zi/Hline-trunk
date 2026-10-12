package com.example.demo.modules.kitchen.application.dto.filter;

import com.example.demo.common.base.dto.BaseFilter;
import com.example.demo.modules.kitchen.domain.model.vo.AssemblyType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Kitchen Product Filter
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Kitchen product filter")
public class KitchenProductFilter extends BaseFilter {

    @Schema(description = "Search keyword")
    private String keyword;

    @Schema(description = "Filter by collection ID")
    private UUID collectionId;

    @Schema(description = "Filter by unit category ID")
    private UUID unitCategoryId;

    @Schema(description = "Filter by unit category code")
    private String unitCategoryCode;

    @Schema(description = "Filter by cabinet colour ID")
    private UUID cabinetColourId;

    @Schema(description = "Filter by assembly type")
    private AssemblyType assemblyType;

    @Schema(description = "Filter by active status")
    private Boolean active;

    @Schema(description = "Filter popular only")
    private Boolean popularOnly;

    @Schema(description = "Filter in stock only")
    private Boolean inStockOnly;

    @Schema(description = "Minimum price")
    private BigDecimal minPrice;

    @Schema(description = "Maximum price")
    private BigDecimal maxPrice;

    @Schema(description = "Minimum width in mm")
    private Integer minWidthMm;

    @Schema(description = "Maximum width in mm")
    private Integer maxWidthMm;

    @Override
    public boolean hasFilters() {
        return keyword != null
            || collectionId != null
            || unitCategoryId != null
            || unitCategoryCode != null
            || cabinetColourId != null
            || assemblyType != null
            || active != null
            || popularOnly != null
            || inStockOnly != null
            || minPrice != null
            || maxPrice != null
            || minWidthMm != null
            || maxWidthMm != null;
    }

    @Override
    public void normalize() {
        super.normalize();
        if (keyword != null) keyword = keyword.trim();
        if (unitCategoryCode != null) unitCategoryCode = unitCategoryCode.trim().toUpperCase();
        if (sortBy == null) sortBy = "sortOrder";
        if (sortDirection == null) sortDirection = "ASC";
    }
}