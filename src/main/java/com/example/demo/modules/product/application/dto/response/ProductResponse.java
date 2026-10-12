package com.example.demo.modules.product.application.dto.response;

import com.example.demo.common.base.dto.BaseDTO;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Minimal Product Response - Optimized for performance
 * 
 * Use this for listings, search results, and general queries.
 * For detailed information, call specific endpoints.
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
@Schema(description = "Product response - optimized for performance")
public class ProductResponse extends BaseDTO {

    private static final long serialVersionUID = 1L;

    // ====================================
    // Core Fields Only
    // ====================================

    @Schema(description = "Product SKU", example = "SKU-001", required = true)
    private String sku;

    @Schema(description = "Product name", example = "Product A", required = true)
    private String name;

    @Schema(description = "Short description", example = "High-quality product")
    private String shortDescription;

    @Schema(description = "Product status", example = "PUBLISHED")
    private String status;

    @Schema(description = "Product type", example = "SIMPLE")
    private String productType;

    @Schema(description = "URL slug", example = "product-a")
    private String slug;

    // ====================================
    // Category - ID Only (load via CategoryService if needed)
    // ====================================

    @Schema(description = "Category ID (use /categories/{id} for details)")
    private UUID categoryId;

    @Schema(description = "Category name (cached)", example = "Category A")
    private String categoryName;

    // ====================================
    // Brand
    // ====================================

    @Schema(description = "Brand name", example = "Brand X")
    private String brand;

    // ====================================
    // Pricing - Current Price Only
    // ====================================

    @Schema(description = "Current price", example = "99.99")
    private BigDecimal currentPrice;

    @Schema(description = "Original price (if on sale)", example = "119.99")
    private BigDecimal originalPrice;

    @Schema(description = "Currency", example = "USD")
    private String currency;

    // ====================================
    // Inventory - Basic Info Only
    // ====================================

    @Schema(description = "Stock status", example = "IN_STOCK")
    private String stockStatus;

    @Schema(description = "Available quantity", example = "100")
    private Integer availableQuantity;

    // ====================================
    // Media - Primary Image Only
    // ====================================

    @Schema(description = "Primary image URL")
    private String primaryImageUrl;

    @Schema(description = "Image count", example = "5")
    private Integer imageCount;

    // ====================================
    // Variants - Count Only
    // ====================================

    @Schema(description = "Has variants", example = "true")
    private Boolean hasVariants;

    @Schema(description = "Variant count", example = "3")
    private Integer variantCount;

    // ====================================
    // Lifecycle
    // ====================================

    @Schema(description = "Published date")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime publishedAt;

    // ====================================
    // Computed/Helper Methods
    // ====================================

    public boolean isAvailableForPurchase() {
        return "PUBLISHED".equals(status)
            && isActive()
            && isInStock();
    }

    public boolean isInStock() {
        return "IN_STOCK".equals(stockStatus)
            || (availableQuantity != null && availableQuantity > 0);
    }

    public boolean hasDiscount() {
        return originalPrice != null 
            && currentPrice != null 
            && originalPrice.compareTo(currentPrice) > 0;
    }

    public boolean isConfigurable() {
        return "CONFIGURABLE".equals(productType) || Boolean.TRUE.equals(hasVariants);
    }

    @Override
    public String toString() {
        return String.format("ProductResponse[id=%s, sku=%s, name=%s]", id, sku, name);
    }
}