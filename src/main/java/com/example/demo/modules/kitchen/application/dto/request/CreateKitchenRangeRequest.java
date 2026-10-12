package com.example.demo.modules.kitchen.application.dto.request;

import com.example.demo.common.base.dto.BaseRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * Create Kitchen Range Request
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Create kitchen range request")
public class CreateKitchenRangeRequest extends BaseRequest {

    @NotBlank(message = "Range code is required")
    @Size(max = 50, message = "Range code cannot exceed 50 characters")
    @Schema(description = "Range code", example = "J_PULL", required = true)
    private String code;

    @NotBlank(message = "Range name is required")
    @Size(max = 100, message = "Range name cannot exceed 100 characters")
    @Schema(description = "Range name", example = "J-Pull Kitchens", required = true)
    private String name;

    @Size(max = 500, message = "Short description cannot exceed 500 characters")
    @Schema(description = "Short description")
    private String shortDescription;

    @Schema(description = "Full description")
    private String description;

    @Schema(description = "Hero image URL")
    private String heroImageUrl;

    @Schema(description = "Meta title for SEO")
    private String metaTitle;

    @Schema(description = "Meta description for SEO")
    private String metaDescription;

    @Schema(description = "Display order", example = "0")
    private Integer sortOrder;

    @Override
    public void normalize() {
        super.normalize();
        if (code != null) code = code.trim().toUpperCase().replaceAll("[^A-Z0-9]", "_");
        if (name != null) name = name.trim();
        if (shortDescription != null) shortDescription = shortDescription.trim();
    }
}