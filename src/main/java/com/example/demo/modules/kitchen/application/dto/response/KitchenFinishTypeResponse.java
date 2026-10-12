package com.example.demo.modules.kitchen.application.dto.response;

import com.example.demo.common.base.dto.BaseDTO;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * Kitchen Finish Type Response DTO
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Kitchen finish type response")
public class KitchenFinishTypeResponse extends BaseDTO {

    @Schema(description = "Finish type code", example = "SUPERGLOSS")
    private String code;

    @Schema(description = "Finish type name", example = "SuperGloss")
    private String name;

    @Schema(description = "Description")
    private String description;

    @Schema(description = "Whether finish type is active")
    private Boolean active;

    @Schema(description = "Display order")
    private Integer sortOrder;
}