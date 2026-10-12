package com.example.demo.modules.order.application.dto.request;

import com.example.demo.common.base.dto.BaseRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Order Item Request DTO
 * 
 * @author Order Team
 * @version 1.0
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Order item details")
public class OrderItemRequest extends BaseRequest {

    @NotNull(message = "Product ID is required")
    @Schema(description = "Product ID", required = true)
    private UUID productId;

    @Schema(description = "Product variant ID")
    private UUID productVariantId;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    @Max(value = 10000, message = "Quantity must not exceed 10000")
    @Schema(description = "Quantity", required = true, example = "1")
    private Integer quantity;

    @NotNull(message = "Unit price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Unit price must be greater than 0")
    @Digits(integer = 15, fraction = 4, message = "Unit price format is invalid")
    @Schema(description = "Unit price", required = true, example = "29.99")
    private BigDecimal unitPrice;

    @DecimalMin(value = "0.0", message = "Tax rate must be 0 or greater")
    @DecimalMax(value = "1.0", message = "Tax rate must not exceed 1.0 (100%)")
    @Digits(integer = 1, fraction = 4, message = "Tax rate format is invalid")
    @Schema(description = "Tax rate (0.0 to 1.0)", example = "0.08")
    private BigDecimal taxRate;

    @DecimalMin(value = "0.0", message = "Discount amount cannot be negative")
    @Digits(integer = 15, fraction = 4, message = "Discount amount format is invalid")
    @Schema(description = "Discount amount", example = "5.00")
    private BigDecimal discountAmount;

    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    @Schema(description = "Item notes")
    private String notes;

    @Override
    public void normalize() {
        super.normalize();
        
        if (notes != null) {
            notes = notes.trim();
            if (notes.isEmpty()) {
                notes = null;
            }
        }
        
        // Set defaults
        if (taxRate == null) {
            taxRate = BigDecimal.ZERO;
        }
        
        if (discountAmount == null) {
            discountAmount = BigDecimal.ZERO;
        }
    }
}