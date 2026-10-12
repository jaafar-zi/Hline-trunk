package com.example.demo.modules.kitchen.application.dto.request;

import com.example.demo.common.base.dto.BaseRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.UUID;

/**
 * Add Item to Kitchen Configuration Request
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Add item to configuration request")
public class AddConfigurationItemRequest extends BaseRequest {

    @NotNull(message = "Product ID is required")
    @Schema(description = "Product ID", required = true)
    private UUID productId;

    @NotNull(message = "Variant ID is required")
    @Schema(description = "Variant ID", required = true)
    private UUID variantId;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    @Max(value = 100, message = "Quantity cannot exceed 100")
    @Schema(description = "Quantity", example = "1", required = true)
    private Integer quantity;

    @Size(max = 200)
    @Schema(description = "Optional label", example = "Left of sink")
    private String label;

    @Override
    public void normalize() {
        super.normalize();
        if (label != null) label = label.trim();
    }
}