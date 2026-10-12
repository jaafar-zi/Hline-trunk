package com.example.demo.modules.product.application.dto.request;

import com.example.demo.common.base.dto.BaseRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating product inventory
 *
 * @author PIM Team
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Update inventory request")
public class UpdateInventoryRequest extends BaseRequest {

    private static final long serialVersionUID = 1L;

    @Schema(description = "New quantity", example = "100", required = true)
    @NotNull(message = "Quantity is required")
    @Min(value = 0, message = "Quantity cannot be negative")
    private Integer quantity;

    @Schema(
        description = "Reason for update",
        example = "Manual adjustment after stock count",
        required = true
    )
    @NotBlank(message = "Reason is required")
    @Size(min = 3, max = 500, message = "Reason must be between 3 and 500 characters")
    private String reason;

    @Schema(description = "Low stock threshold", example = "10")
    @Min(value = 0, message = "Low stock threshold cannot be negative")
    private Integer lowStockThreshold;

    @Schema(description = "Reorder point", example = "20")
    @Min(value = 0, message = "Reorder point cannot be negative")
    private Integer reorderPoint;

    @Schema(description = "Reorder quantity", example = "50")
    @Min(value = 1, message = "Reorder quantity must be positive")
    private Integer reorderQuantity;

    @Override
    public void validate() {
        super.validate();

        if (reason != null) {
            reason = reason.trim();
        }
    }
}