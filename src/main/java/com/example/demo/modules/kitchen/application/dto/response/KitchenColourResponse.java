package com.example.demo.modules.kitchen.application.dto.response;

import com.example.demo.common.base.dto.BaseDTO;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * Kitchen Colour Response DTO
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Kitchen colour response")
public class KitchenColourResponse extends BaseDTO {

    @Schema(description = "Colour code", example = "CASHMERE")
    private String code;

    @Schema(description = "Colour name", example = "Cashmere")
    private String name;

    @Schema(description = "Hex colour value", example = "#F5E6D3")
    private String hexValue;

    @Schema(description = "Swatch image URL")
    private String swatchImageUrl;

    @Schema(description = "Description")
    private String description;

    @Schema(description = "Whether colour is active")
    private Boolean active;

    @Schema(description = "Display order")
    private Integer sortOrder;
}