package com.example.demo.modules.product.application.dto.request;

import com.example.demo.common.base.dto.BaseRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Request DTO for activating/deactivating a category
 *
 * @author PIM Team
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Toggle category status request")
public class ToggleCategoryStatusRequest extends BaseRequest {

    private static final long serialVersionUID = 1L;

    @Schema(
        description = "Active status",
        example = "false",
        required = true
    )
    @NotNull(message = "Active status is required")
    private Boolean active;

    @Schema(
        description = "Apply to all child categories",
        example = "true",
        defaultValue = "false"
    )
    private Boolean applyToChildren = false;

    @Schema(
        description = "Reason for status change",
        example = "Seasonal category - out of season"
    )
    private String reason;

    @Override
    public void validate() {
        super.validate();

        if (reason != null) {
            reason = reason.trim();
        }
    }
}