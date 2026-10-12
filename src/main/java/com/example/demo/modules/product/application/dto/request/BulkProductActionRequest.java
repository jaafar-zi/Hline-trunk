package com.example.demo.modules.product.application.dto.request;

import com.example.demo.common.base.dto.BaseRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * Request DTO for bulk product actions
 *
 * @author PIM Team
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Bulk product action request")
public class BulkProductActionRequest extends BaseRequest {

    private static final long serialVersionUID = 1L;

    @Schema(
        description = "List of product IDs",
        required = true
    )
    @NotNull(message = "Product IDs are required")
    @NotEmpty(message = "Product IDs list cannot be empty")
    @Size(max = 500, message = "Cannot process more than 500 products at once")
    private List<UUID> productIds;

    @Schema(
        description = "Action to perform",
        example = "PUBLISH",
        allowableValues = {"PUBLISH", "UNPUBLISH", "ARCHIVE", "DELETE", "RESTORE", "CHANGE_CATEGORY"},
        required = true
    )
    @NotBlank(message = "Action is required")
    private String action;

    @Schema(
        description = "Target category ID (for CHANGE_CATEGORY action)",
        example = "550e8400-e29b-41d4-a716-446655440000"
    )
    private UUID targetCategoryId;

    @Schema(
        description = "Reason for action",
        example = "End of season sale"
    )
    @Size(max = 500, message = "Reason cannot exceed 500 characters")
    private String reason;

    @Override
    public void validate() {
        super.validate();

        if (action != null) {
            action = action.trim().toUpperCase();
        }

        if (reason != null) {
            reason = reason.trim();
        }

        // Validate category ID is required for CHANGE_CATEGORY action
        if ("CHANGE_CATEGORY".equals(action) && targetCategoryId == null) {
            throw new IllegalArgumentException("Target category ID is required for CHANGE_CATEGORY action");
        }
    }
}