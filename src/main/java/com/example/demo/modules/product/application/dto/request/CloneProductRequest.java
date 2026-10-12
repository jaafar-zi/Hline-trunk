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
 * Request DTO for cloning a product
 *
 * @author PIM Team
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Clone product request")
public class CloneProductRequest extends BaseRequest {

    private static final long serialVersionUID = 1L;

    @Schema(
        description = "New product name",
        example = "Premium Wireless Headphones v2",
        required = true
    )
    @NotBlank(message = "New product name is required")
    @Size(min = 3, max = 500, message = "Product name must be between 3 and 500 characters")
    private String newName;

    @Schema(
        description = "New SKU (leave null for auto-generation)",
        example = "PROD-20240115-X1Y2Z3"
    )
    private String newSku;

    @Schema(
        description = "Clone variants",
        example = "true",
        defaultValue = "true"
    )
    private Boolean cloneVariants = true;

    @Schema(
        description = "Clone prices",
        example = "true",
        defaultValue = "true"
    )
    private Boolean clonePrices = true;

    @Schema(
        description = "Clone images",
        example = "true",
        defaultValue = "true"
    )
    private Boolean cloneImages = true;

    @Schema(
        description = "Clone attributes",
        example = "true",
        defaultValue = "true"
    )
    private Boolean cloneAttributes = true;

    @Schema(
        description = "Clone inventory settings (but not quantities)",
        example = "false",
        defaultValue = "false"
    )
    private Boolean cloneInventory = false;

    @Schema(
        description = "Target category ID (use original if null)",
        example = "550e8400-e29b-41d4-a716-446655440000"
    )
    private UUID targetCategoryId;

    @Override
    public void validate() {
        super.validate();

        if (newName != null) {
            newName = newName.trim();
        }

        if (newSku != null && !newSku.isBlank()) {
            newSku = newSku.trim().toUpperCase();
        }
    }
}