package com.example.demo.modules.kitchen.application.dto.response;

import com.example.demo.common.base.dto.BaseDTO;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * Kitchen Range Response DTO
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Kitchen range response")
public class KitchenRangeResponse extends BaseDTO {

    @Schema(description = "Range code", example = "J_PULL")
    private String code;

    @Schema(description = "Range name", example = "J-Pull Kitchens")
    private String name;

    @Schema(description = "URL slug", example = "j-pull")
    private String slug;

    @Schema(description = "Short description")
    private String shortDescription;

    @Schema(description = "Full description")
    private String description;

    @Schema(description = "Hero image URL")
    private String heroImageUrl;

    @Schema(description = "Whether range is active")
    private Boolean active;

    @Schema(description = "Display order")
    private Integer sortOrder;

    @Schema(description = "Meta title for SEO")
    private String metaTitle;

    @Schema(description = "Meta description for SEO")
    private String metaDescription;

    @Schema(description = "Finish groups within this range")
    private List<KitchenFinishGroupResponse> finishGroups;

    @Schema(description = "Total number of active collections")
    private Long collectionCount;

    @Schema(description = "Total number of finish groups")
    private Long finishGroupCount;
}