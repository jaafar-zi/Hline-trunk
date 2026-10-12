package com.example.demo.modules.kitchen.application.dto.response;

import com.example.demo.common.base.dto.BaseDTO;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

/**
 * Kitchen Product Image Response DTO
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Kitchen product image response")
public class KitchenProductImageResponse extends BaseDTO {

    @Schema(description = "Image URL")
    private String url;

    @Schema(description = "Thumbnail URL")
    private String thumbnailUrl;

    @Schema(description = "Alt text")
    private String altText;

    @Schema(description = "Whether this is the primary image")
    private Boolean isPrimary;

    @Schema(description = "Sort order")
    private Integer sortOrder;

    @Schema(description = "Variant ID (if variant-specific image)")
    private UUID variantId;
}