package com.example.demo.modules.product.application.dto.request;

import com.example.demo.common.base.dto.BaseRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Request DTO for updating a product price
 *
 * @author PIM Team
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Update price request")
public class UpdatePriceRequest extends BaseRequest {

    private static final long serialVersionUID = 1L;

    @Schema(
        description = "New price amount",
        example = "89.99",
        required = true
    )
    @NotNull(message = "Price amount is required")
    @DecimalMin(value = "0.01", message = "Price must be positive")
    private BigDecimal amount;

    @Schema(
        description = "Valid from date",
        example = "2024-02-01T00:00:00"
    )
    private LocalDateTime validFrom;

    @Schema(
        description = "Valid to date",
        example = "2024-12-31T23:59:59"
    )
    private LocalDateTime validTo;

    @Schema(
        description = "Priority",
        example = "5"
    )
    @Min(value = 0, message = "Priority cannot be negative")
    private Integer priority;

    @Schema(
        description = "Tax rate (percentage)",
        example = "21.0"
    )
    @DecimalMin(value = "0", message = "Tax rate cannot be negative")
    @DecimalMax(value = "100", message = "Tax rate cannot exceed 100%")
    private BigDecimal taxRate;

    @Schema(
        description = "Active status",
        example = "true"
    )
    private Boolean active;

    @Override
    public void validate() {
        super.validate();

        if (validFrom != null && validTo != null && validFrom.isAfter(validTo)) {
            throw new IllegalArgumentException("Valid from date must be before valid to date");
        }
    }
}