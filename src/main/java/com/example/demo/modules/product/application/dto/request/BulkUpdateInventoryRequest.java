package com.example.demo.modules.product.application.dto.request;

import com.example.demo.common.base.dto.BaseRequest;
import com.example.demo.modules.product.domain.model.vo.SKU;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * Request DTO for bulk updating inventory
 *
 * @author PIM Team
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Bulk update inventory request")
public class BulkUpdateInventoryRequest extends BaseRequest {

    private static final long serialVersionUID = 1L;

    @Schema(
        description = "List of inventory updates",
        required = true
    )
    @NotNull(message = "Updates list is required")
    @NotEmpty(message = "Updates list cannot be empty")
    @Size(max = 1000, message = "Cannot update more than 1000 items at once")
    @Valid
    private List<InventoryUpdateItem> updates;

    @Schema(
        description = "Reason for bulk update",
        example = "Stock count reconciliation",
        required = true
    )
    @NotBlank(message = "Reason is required")
    @Size(min = 3, max = 500, message = "Reason must be between 3 and 500 characters")
    private String reason;

    @Override
    public void validate() {
        super.validate();

        if (reason != null) {
            reason = reason.trim();
        }

        if (updates != null) {
            updates.forEach(InventoryUpdateItem::validate);
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Individual inventory update item")
    public static class InventoryUpdateItem {

        @Schema(
            description = "Product ID",
            example = "550e8400-e29b-41d4-a716-446655440000",
            required = true
        )
        @NotNull(message = "Product ID is required")
        private UUID productId;

        @Schema(
            description = "Product SKU (alternative to productId)",
            example = "PROD-20240115-A1B2C3"
        )
        @Valid
        private SKU sku;

        @Schema(
            description = "New quantity",
            example = "150",
            required = true
        )
        @NotNull(message = "Quantity is required")
        @Min(value = 0, message = "Quantity cannot be negative")
        private Integer quantity;

        public void validate() {
            if (productId == null && (sku == null || sku.toString().isBlank())) {
                throw new IllegalArgumentException(
                    "Either productId or SKU must be provided"
                );
            }

            if (sku != null) {
                sku = sku.of(sku.toString().trim().toUpperCase());
            }
        }
    }
}