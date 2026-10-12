package com.example.demo.modules.product.application.dto.response;

import com.example.demo.common.base.dto.BaseDTO;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

/**
 * Lean Product Variant Response - Performance optimized
 * 
 * Use for variant listings. For detailed info, call specific endpoints.
 *
 * @author PIM Team
 * @version 1.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Product variant response - optimized")
public class ProductVariantResponse extends BaseDTO {

    private static final long serialVersionUID = 1L;

    // ====================================
    // Core Fields Only
    // ====================================

    @Schema(description = "Product ID reference (use ProductService if needed)")
    private UUID productId;

    @Schema(description = "Variant SKU", example = "SKU-001-VAR-L-BLK", required = true)
    private String sku;

    @Schema(description = "Variant name", example = "Large - Black")
    private String name;

    @Schema(description = "Variant attributes", example = "{\"size\": \"Large\", \"color\": \"Black\"}")
    private Map<String, String> attributes;

    // ====================================
    // Pricing - Basic Only
    // ====================================

    @Schema(description = "Variant price", example = "109.99")
    private BigDecimal price;

    @Schema(description = "Currency", example = "USD")
    private String currency;

    @Schema(description = "Price adjustment from base", example = "10.00")
    private BigDecimal priceAdjustment;

    // ====================================
    // Inventory - Basic Only
    // ====================================

    @Schema(description = "Stock status", example = "IN_STOCK")
    private String stockStatus;

    @Schema(description = "Available quantity", example = "50")
    private Integer availableQuantity;

    // ====================================
    // Media - Primary Only
    // ====================================

    @Schema(description = "Primary image URL")
    private String primaryImageUrl;

    // ====================================
    // Configuration
    // ====================================

    @Schema(description = "Display order", example = "1")
    private Integer displayOrder;

    @Schema(description = "Is default variant", example = "false")
    private Boolean isDefault;

    @Schema(description = "Is active", example = "true")
    private Boolean active;

    @Schema(description = "Barcode", example = "1234567890123")
    private String barcode;

    // ====================================
    // Helper Methods
    // ====================================

    public boolean isInStock() {
        return "IN_STOCK".equals(stockStatus) 
            || (availableQuantity != null && availableQuantity > 0);
    }

    public boolean isAvailableForPurchase() {
        return Boolean.TRUE.equals(active) && isActive() && isInStock();
    }

    public boolean hasCustomPrice() {
        return priceAdjustment != null && priceAdjustment.compareTo(BigDecimal.ZERO) != 0;
    }

    public String getAttribute(String key) {
        return attributes != null ? attributes.get(key) : null;
    }

    @Override
    public String toString() {
        return String.format("ProductVariantResponse[id=%s, sku=%s, name=%s]", id, sku, name);
    }
}