package com.example.demo.modules.product.application.dto.request;

import com.example.demo.common.base.dto.BaseRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Request DTO for moving a category to a different parent
 *
 * @author PIM Team
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Move category request")
public class MoveCategoryRequest extends BaseRequest {

    private static final long serialVersionUID = 1L;

    @Schema(
        description = "New parent category ID (null to make root category)",
        example = "660e8400-e29b-41d4-a716-446655440001"
    )
    private UUID newParentId;

    @Schema(
        description = "New display order under the parent",
        example = "3"
    )
    private Integer displayOrder;

    @Schema(
        description = "Move all child categories as well",
        example = "true",
        defaultValue = "true"
    )
    private Boolean moveChildren = true;

    @Override
    public void validate() {
        super.validate();

        // No additional validation needed
    }
}