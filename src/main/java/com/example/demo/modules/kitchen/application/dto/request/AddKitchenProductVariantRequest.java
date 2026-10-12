package com.example.demo.modules.kitchen.application.dto.request;

import com.example.demo.common.base.dto.BaseRequest;
import com.example.demo.modules.kitchen.domain.model.vo.AssemblyType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Add Kitchen Product Variant Request
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Add product variant request")
public class AddKitchenProductVariantRequest extends BaseRequest {

    @NotBlank(message = "SKU is required")
    @Size(max = 50)
    @Schema(description = "Variant SKU", example = "JP-SG-CASH-B150PO-WHT-FP", required = true)
    private String sku;

    @NotNull(message = "Cabinet colour ID is required")
    @Schema(description = "Cabinet colour ID", required = true)
    private UUID cabinetColourId;

    @NotNull(message = "Assembly type is required")
    @Schema(description = "Assembly type", example = "FLATPACK", required = true,
            allowableValues = {"FLATPACK", "RIGID"})
    private AssemblyType assemblyType;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.00")
    @Schema(description = "Variant price", example = "120.00", required = true)
    private BigDecimal priceAmount;

    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3)
    @Schema(description = "Currency", example = "GBP", required = true)
    private String currency;

    @DecimalMin(value = "0.00")
    @Schema(description = "Cabinet colour premium amount", example = "10.00")
    private BigDecimal cabinetColourPremiumAmount;

    @DecimalMin(value = "0.00")
    @Schema(description = "Assembly premium amount", example = "25.00")
    private BigDecimal assemblyPremiumAmount;

    @Schema(description = "Lead time in days")
    private Integer leadTimeDays;

    @Schema(description = "Whether this is the default variant", defaultValue = "false")
    private Boolean isDefault = false;

    @Override
    public void normalize() {
        super.normalize();
        if (sku != null) sku = sku.trim().toUpperCase();
        if (currency != null) currency = currency.trim().toUpperCase();
    }
}