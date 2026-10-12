package com.example.demo.modules.kitchen.application.dto.request;

import com.example.demo.common.base.dto.BaseRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * Update Kitchen Range Request
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Update kitchen range request")
public class UpdateKitchenRangeRequest extends BaseRequest {

    @Size(max = 100, message = "Range name cannot exceed 100 characters")
    @Schema(description = "Range name")
    private String name;

    @Size(max = 500)
    @Schema(description = "Short description")
    private String shortDescription;

    @Schema(description = "Full description")
    private String description;

    @Schema(description = "Hero image URL")
    private String heroImageUrl;

    @Schema(description = "Meta title")
    private String metaTitle;

    @Schema(description = "Meta description")
    private String metaDescription;

    @Schema(description = "Display order")
    private Integer sortOrder;

    @Override
    public void normalize() {
        super.normalize();
        if (name != null) name = name.trim();
        if (shortDescription != null) shortDescription = shortDescription.trim();
    }
}