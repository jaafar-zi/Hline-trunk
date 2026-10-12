package com.example.demo.modules.product.application.dto.request;

import com.example.demo.common.base.dto.BaseRequest;
import com.example.demo.common.normalization.Normalize;
import com.example.demo.common.validator.constraints.ConditionalRequired;
import com.example.demo.common.validator.constraints.ValidCurrency;
import com.example.demo.common.validator.constraints.ValidDateRange;
import com.example.demo.common.validator.constraints.ValidEnum;
import com.example.demo.modules.product.domain.model.vo.PriceType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Request DTO for adding a product price
 *
 * @author PIM Team
 * @version 2.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Add product price request")
@ValidDateRange(min = "validFrom", max = "validTo", message = "Valid from date must be before valid to date")
@ConditionalRequired(
    field = "validFrom",
    dependsOn = "priceType",
    dependsOnValue = {"PROMOTIONAL"},
    message = "Promotional prices must have a valid from date"
)
@ConditionalRequired(
    field = "validTo",
    dependsOn = "priceType",
    dependsOnValue = {"PROMOTIONAL"},
    message = "Promotional prices must have a valid to date"
)
public class AddProductPriceRequest extends BaseRequest {

    private static final long serialVersionUID = 1L;

    @Schema(description = "Price amount", example = "99.99", required = true)
    @NotNull(message = "Price amount is required")
    @DecimalMin(value = "0.01", message = "Price must be positive")
    private BigDecimal amount;

    @Schema(description = "Currency code (ISO 4217)", example = "USD", required = true)
    @NotBlank(message = "Currency is required")
    @ValidCurrency
    @Normalize(Normalize.Strategy.UPPERCASE)
    private String currency;

    @Schema(
        description = "Price type",
        example = "BASE",
        allowableValues = {"BASE", "PROMOTIONAL", "TIERED", "WHOLESALE"},
        required = true
    )
    @NotBlank(message = "Price type is required")
    @ValidEnum(enumClass = PriceType.class, message = "Invalid price type")
    @Normalize(Normalize.Strategy.UPPERCASE)
    private String priceType;

    @Schema(description = "Valid from date", example = "2024-01-20T00:00:00")
    private LocalDateTime validFrom;

    @Schema(description = "Valid to date", example = "2024-12-31T23:59:59")
    private LocalDateTime validTo;

    @Schema(description = "Discount percentage (for promotional prices)", example = "20.0")
    @DecimalMin(value = "0", message = "Discount percentage cannot be negative")
    @DecimalMax(value = "100", message = "Discount percentage cannot exceed 100%")
    private BigDecimal discountPercentage;

    @Schema(description = "Tax inclusive", example = "false", defaultValue = "false")
    private Boolean taxInclusive = false;

    @Schema(description = "Tax rate (percentage)", example = "20.0")
    @DecimalMin(value = "0", message = "Tax rate cannot be negative")
    @DecimalMax(value = "100", message = "Tax rate cannot exceed 100%")
    private BigDecimal taxRate;

    @Schema(description = "Customer group", example = "RETAIL")
    @Size(max = 100, message = "Customer group cannot exceed 100 characters")
    @Normalize(Normalize.Strategy.TRIM)
    private String customerGroup;

    @Schema(description = "Market", example = "US")
    @Size(max = 50, message = "Market cannot exceed 50 characters")
    @Normalize(Normalize.Strategy.UPPERCASE)
    private String market;

    @Schema(description = "Priority (higher = more important)", example = "10")
    @Min(value = 0, message = "Priority cannot be negative")
    private Integer priority = 0;

    @Override
    public void normalize() {
        super.normalize();
        // All normalization is handled by @Normalize annotations
    }
}