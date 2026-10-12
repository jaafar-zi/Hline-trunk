package com.example.demo.modules.kitchen.application.dto.response;

import com.example.demo.common.base.dto.BaseDTO;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.UUID;

/**
 * Kitchen Finish Group Response DTO
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Kitchen finish group response")
public class KitchenFinishGroupResponse extends BaseDTO {

    @Schema(description = "Parent range ID")
    private UUID rangeId;

    @Schema(description = "Parent range name")
    private String rangeName;

    @Schema(description = "Finish type ID")
    private UUID finishTypeId;

    @Schema(description = "Finish type name", example = "SuperGloss")
    private String finishTypeName;

    @Schema(description = "Finish type code", example = "SUPERGLOSS")
    private String finishTypeCode;

    @Schema(description = "Group name", example = "J-Pull SuperGloss")
    private String name;

    @Schema(description = "URL slug", example = "supergloss")
    private String slug;

    @Schema(description = "Description")
    private String description;

    @Schema(description = "Whether group is active")
    private Boolean active;

    @Schema(description = "Display order")
    private Integer sortOrder;

    @Schema(description = "Collections in this group")
    private List<KitchenCollectionSummary> collections;

    @Schema(description = "Total collections count")
    private Long collectionCount;
}