package com.example.demo.modules.kitchen.application.dto.filter;

import com.example.demo.common.base.dto.BaseFilter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.UUID;

/**
 * Kitchen Collection Filter
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Kitchen collection filter")
public class KitchenCollectionFilter extends BaseFilter {

    @Schema(description = "Search keyword")
    private String keyword;

    @Schema(description = "Filter by range ID")
    private UUID rangeId;

    @Schema(description = "Filter by finish group ID")
    private UUID finishGroupId;

    @Schema(description = "Filter by colour ID")
    private UUID colourId;

    @Schema(description = "Filter by active status")
    private Boolean active;

    @Schema(description = "Filter by finish type code")
    private String finishTypeCode;

    @Override
    public boolean hasFilters() {
        return keyword != null
            || rangeId != null
            || finishGroupId != null
            || colourId != null
            || active != null
            || finishTypeCode != null;
    }

    @Override
    public void normalize() {
        super.normalize();
        if (keyword != null) keyword = keyword.trim();
        if (finishTypeCode != null) finishTypeCode = finishTypeCode.trim().toUpperCase();
        if (sortBy == null) sortBy = "sortOrder";
        if (sortDirection == null) sortDirection = "ASC";
    }
}