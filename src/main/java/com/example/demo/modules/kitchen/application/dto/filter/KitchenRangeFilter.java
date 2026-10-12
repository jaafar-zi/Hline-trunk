package com.example.demo.modules.kitchen.application.dto.filter;

import com.example.demo.common.base.dto.BaseFilter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * Kitchen Range Filter
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Kitchen range filter")
public class KitchenRangeFilter extends BaseFilter {

    @Schema(description = "Search keyword")
    private String keyword;

    @Schema(description = "Filter by active status")
    private Boolean active;

    @Schema(description = "Filter by code")
    private String code;

    @Override
    public boolean hasFilters() {
        return keyword != null
            || active != null
            || code != null;
    }

    @Override
    public void normalize() {
        super.normalize();
        if (keyword != null) keyword = keyword.trim();
        if (code != null) code = code.trim().toUpperCase();
        if (sortBy == null) sortBy = "sortOrder";
        if (sortDirection == null) sortDirection = "ASC";
    }
}