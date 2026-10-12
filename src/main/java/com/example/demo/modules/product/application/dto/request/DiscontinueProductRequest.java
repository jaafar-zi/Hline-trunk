package com.example.demo.modules.product.application.dto.request;

import com.example.demo.common.base.dto.BaseRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Request DTO for discontinuing a product
 *
 * @author PIM Team
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Discontinue product request")
public class DiscontinueProductRequest extends BaseRequest {

    private static final long serialVersionUID = 1L;

    @Schema(
        description = "Reason for discontinuation",
        example = "End of product lifecycle",
        required = true
    )
    @NotBlank(message = "Reason is required")
    @Size(min = 5, max = 500, message = "Reason must be between 5 and 500 characters")
    private String reason;

    @Schema(
        description = "Replacement product ID (if any)",
        example = "660e8400-e29b-41d4-a716-446655440001"
    )
    private UUID replacementProductId;

    @Schema(
        description = "Notify customers",
        example = "true",
        defaultValue = "true"
    )
    private Boolean notifyCustomers = true;

    @Schema(
        description = "Clear remaining inventory",
        example = "false",
        defaultValue = "false"
    )
    private Boolean clearInventory = false;

    @Override
    public void validate() {
        super.validate();

        if (reason != null) {
            reason = reason.trim();
        }
    }
}