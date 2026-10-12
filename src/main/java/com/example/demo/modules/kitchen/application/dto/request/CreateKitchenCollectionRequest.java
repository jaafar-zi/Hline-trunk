package com.example.demo.modules.kitchen.application.dto.request;

import com.example.demo.common.base.dto.BaseRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.UUID;

/**
 * Create Kitchen Collection Request
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Create kitchen collection request")
public class CreateKitchenCollectionRequest extends BaseRequest {

    @NotNull(message = "Finish group ID is required")
    @Schema(description = "Finish group ID", required = true)
    private UUID finishGroupId;

    @NotNull(message = "Colour ID is required")
    @Schema(description = "Door colour ID", required = true)
    private UUID colourId;

    @NotBlank(message = "Collection name is required")
    @Size(max = 200)
    @Schema(description = "Collection name", example = "J-Pull SuperGloss Cashmere", required = true)
    private String name;

    @Size(max = 500)
    @Schema(description = "Short description")
    private String shortDescription;

    @Schema(description = "Full description")
    private String description;

    @Schema(description = "Hero image URL")
    private String heroImageUrl;

    @Schema(description = "Lead time in days", example = "14")
    private Integer leadTimeDays;

    @Schema(description = "Default cabinet colour ID")
    private UUID defaultCabinetColourId;

    @Schema(description = "Meta title")
    private String metaTitle;

    @Schema(description = "Meta description")
    private String metaDescription;

    @Override
    public void normalize() {
        super.normalize();
        if (name != null) name = name.trim();
        if (shortDescription != null) shortDescription = shortDescription.trim();
    }
}