package com.example.demo.modules.product.application.dto.response;

import com.example.demo.common.base.dto.BaseDTO;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.Locale;
import java.util.UUID;

/**
 * Simplified product response for list views
 * 
 * Optimized DTO with only essential fields for product listings,
 * search results, and grid displays
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
@Schema(description = "Product list item response - optimized for listings and grids")
public class ProductListResponse extends BaseDTO {

    private static final long serialVersionUID = 1L;

    // ====================================
    // Core Product Information
    // ====================================

    @Schema(
        description = "Product SKU",
        example = "PROD-20240115-A1B2C3",
        required = true
    )
    private String sku;

    @Schema(
        description = "Product name",
        example = "Product Name",
        required = true
    )
    private String name;

    @Schema(
        description = "Short description for list display",
        example = "Description of the product for quick overview in listings"
    )
    private String shortDescription;

    @Schema(
        description = "Product status",
        example = "PUBLISHED",
        allowableValues = {"DRAFT", "PENDING_REVIEW", "PUBLISHED", "ARCHIVED", "DISCONTINUED"}
    )
    private String status;

    @Schema(
        description = "Product type",
        example = "SIMPLE",
        allowableValues = {"SIMPLE", "CONFIGURABLE", "BUNDLE", "VIRTUAL"}
    )
    private String productType;

    @Schema(
        description = "SEO-friendly URL slug",
        example = "URL Slug for the product, used in SEO-friendly URLs"
    )
    private String slug;

    // ====================================
    // Category & Brand
    // ====================================

    @Schema(
        description = "Category ID",
        example = "770e8400-e29b-41d4-a716-446655440000"
    )
    private UUID categoryId;

    @Schema(
        description = "Category name",
        example = "Category Name"
    )
    private String categoryName;

    @Schema(
        description = "Category path for breadcrumbs",
        example = "All Products > Category Name"
    )
    private String categoryPath;

    @Schema(
        description = "Brand name",
        example = "Brand Name"
    )
    private String brand;

    @Schema(
        description = "Manufacturer name",
        example = "Manufacturer Name"
    )
    private String manufacturer;

    // ====================================
    // Images
    // ====================================

    @Schema(
        description = "Primary product image URL",
        example = "https://cdn.example.com/products/headphones-001.jpg"
    )
    private String primaryImageUrl;

    @Schema(
        description = "Thumbnail image URL",
        example = "https://cdn.example.com/products/thumbs/headphones-001-thumb.jpg"
    )
    private String thumbnailUrl;

    @Schema(
        description = "Number of product images",
        example = "5"
    )
    private Integer imageCount;

    // ====================================
    // Pricing
    // ====================================

    @Schema(
        description = "Current selling price",
        example = "99.99",
        required = true
    )
    private BigDecimal currentPrice;

    @Schema(
        description = "Original price (before discount)",
        example = "119.99"
    )
    private BigDecimal originalPrice;

    @Schema(
        description = "Currency code (ISO 4217)",
        example = "USD"
    )
    @Builder.Default
    private String currency = "USD";

    @Schema(
        description = "Formatted price for display",
        example = "$99.99"
    )
    private String formattedPrice;

    @Schema(
        description = "Formatted original price",
        example = "$119.99"
    )
    private String formattedOriginalPrice;

    @Schema(
        description = "Price range (for configurable products)",
        example = "$99.99 - $149.99"
    )
    private String priceRange;

    // ====================================
    // Promotions & Discounts
    // ====================================

    @Schema(
        description = "Has active promotion",
        example = "true"
    )
    private Boolean hasPromotion;

    @Schema(
        description = "Discount percentage",
        example = "16.67"
    )
    private BigDecimal discountPercentage;

    @Schema(
        description = "Discount amount",
        example = "20.00"
    )
    private BigDecimal discountAmount;

    @Schema(
        description = "Promotion label",
        example = "SALE"
    )
    private String promotionLabel;

    @Schema(
        description = "Promotion end date",
        example = "2024-12-31T23:59:59"
    )
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime promotionEndsAt;

    // ====================================
    // Inventory
    // ====================================

    @Schema(
        description = "Stock status",
        example = "IN_STOCK",
        allowableValues = {"IN_STOCK", "LOW_STOCK", "OUT_OF_STOCK", "BACKORDER", "DISCONTINUED"}
    )
    private String stockStatus;

    @Schema(
        description = "Available quantity",
        example = "150"
    )
    private Integer availableQuantity;

    @Schema(
        description = "Is currently in stock",
        example = "true"
    )
    private Boolean inStock;

    @Schema(
        description = "Low stock threshold reached",
        example = "false"
    )
    private Boolean lowStock;

    @Schema(
        description = "Expected restock date",
        example = "2024-02-15T00:00:00"
    )
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime expectedRestockDate;

    // ====================================
    // Variants
    // ====================================

    @Schema(
        description = "Product has variants/options",
        example = "true"
    )
    private Boolean hasVariants;

    @Schema(
        description = "Number of variants",
        example = "3"
    )
    private Integer variantCount;

    @Schema(
        description = "Available variant options summary",
        example = "Colors: Red, Blue, Black | Sizes: S, M, L"
    )
    private String variantOptionsSummary;

    // ====================================
    // Ratings & Reviews
    // ====================================

    @Schema(
        description = "Average rating",
        example = "4.5"
    )
    private BigDecimal averageRating;

    @Schema(
        description = "Number of reviews",
        example = "127"
    )
    private Integer reviewCount;

    // ====================================
    // Product Flags
    // ====================================

    @Schema(
        description = "Is featured product",
        example = "true"
    )
    private Boolean featured;

    @Schema(
        description = "Is new arrival",
        example = "true"
    )
    private Boolean newArrival;

    @Schema(
        description = "Is bestseller",
        example = "false"
    )
    private Boolean bestseller;

    @Schema(
        description = "Is on sale",
        example = "true"
    )
    private Boolean onSale;

    // ====================================
    // Dates
    // ====================================

    @Schema(
        description = "Publication date",
        example = "2024-01-20T10:30:00"
    )
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime publishedAt;

    @Schema(
        description = "Last stock update",
        example = "2024-01-25T15:45:00"
    )
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime lastStockUpdate;

    // ====================================
    // Additional Metadata
    // ====================================

    @Schema(
        description = "Product tags",
        example = "[\"category1\", \"category2\", \"category3\"]"
    )
    private java.util.List<String> tags;

    @Schema(
        description = "Custom attributes for filtering",
        example = "{\"color\": \"black\", \"weight\": \"250g\"}"
    )
    private java.util.Map<String, String> attributes;

    // ====================================
    // Convenience Methods
    // ====================================

    /**
     * Check if product is available for purchase
     */
    public boolean isAvailableForPurchase() {
        return Boolean.TRUE.equals(inStock)
            && "PUBLISHED".equals(status)
            && isActive();
    }

    /**
     * Check if product has discount
     */
    public boolean hasDiscount() {
        return hasPromotion != null && hasPromotion
            || (originalPrice != null && currentPrice != null 
                && originalPrice.compareTo(currentPrice) > 0);
    }

    /**
     * Calculate discount percentage
     */
    public BigDecimal calculateDiscountPercentage() {
        if (discountPercentage != null) {
            return discountPercentage;
        }
        
        if (originalPrice == null || currentPrice == null 
            || originalPrice.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal discount = originalPrice.subtract(currentPrice);
        return discount.divide(originalPrice, 4, RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(100))
            .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calculate discount amount
     */
    public BigDecimal calculateDiscountAmount() {
        if (discountAmount != null) {
            return discountAmount;
        }
        
        if (originalPrice == null || currentPrice == null) {
            return BigDecimal.ZERO;
        }
        
        return originalPrice.subtract(currentPrice);
    }

    /**
     * Get formatted current price
     */
    public String getFormattedPrice() {
        if (formattedPrice != null) {
            return formattedPrice;
        }
        return formatPrice(currentPrice, currency);
    }

    /**
     * Get formatted original price
     */
    public String getFormattedOriginalPrice() {
        if (formattedOriginalPrice != null) {
            return formattedOriginalPrice;
        }
        return formatPrice(originalPrice, currency);
    }

    /**
     * Format price with currency
     */
    public static String formatPrice(BigDecimal price, String currencyCode) {
        if (price == null) {
            return null;
        }
        
        try {
            Currency curr = Currency.getInstance(currencyCode != null ? currencyCode : "USD");
            NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.US);
            formatter.setCurrency(curr);
            return formatter.format(price);
        } catch (Exception e) {
            return currencyCode + " " + price.setScale(2, RoundingMode.HALF_UP);
        }
    }

    /**
     * Get savings amount
     */
    public BigDecimal getSavings() {
        return calculateDiscountAmount();
    }

    /**
     * Get savings percentage display
     */
    public String getSavingsDisplay() {
        BigDecimal percentage = calculateDiscountPercentage();
        if (percentage.compareTo(BigDecimal.ZERO) > 0) {
            return "Save " + percentage.setScale(0, RoundingMode.HALF_UP) + "%";
        }
        return null;
    }

    /**
     * Check if product is configurable (has variants)
     */
    public boolean isConfigurable() {
        return "CONFIGURABLE".equals(productType) 
            || Boolean.TRUE.equals(hasVariants);
    }

    /**
     * Check if promotion is active
     */
    public boolean hasActivePromotion() {
        if (!Boolean.TRUE.equals(hasPromotion)) {
            return false;
        }
        
        if (promotionEndsAt == null) {
            return true;
        }
        
        return LocalDateTime.now().isBefore(promotionEndsAt);
    }

    /**
     * Check if product is new (published within last 30 days)
     */
    public boolean isNew() {
        if (Boolean.TRUE.equals(newArrival)) {
            return true;
        }
        
        if (publishedAt == null) {
            return false;
        }
        
        return publishedAt.isAfter(LocalDateTime.now().minusDays(30));
    }

    /**
     * Check if stock is low
     */
    public boolean isLowStock() {
        if (Boolean.TRUE.equals(lowStock)) {
            return true;
        }
        
        return "LOW_STOCK".equals(stockStatus);
    }

    /**
     * Get stock status display message
     */
    public String getStockStatusDisplay() {
        if (Boolean.TRUE.equals(inStock)) {
            if (isLowStock()) {
                return "Only " + availableQuantity + " left in stock";
            }
            return "In Stock";
        } else if ("BACKORDER".equals(stockStatus)) {
            if (expectedRestockDate != null) {
                return "Available for backorder - Ships " + expectedRestockDate.toLocalDate();
            }
            return "Available for backorder";
        } else if ("OUT_OF_STOCK".equals(stockStatus)) {
            if (expectedRestockDate != null) {
                return "Out of stock - Expected " + expectedRestockDate.toLocalDate();
            }
            return "Out of Stock";
        }
        return stockStatus;
    }

    /**
     * Get product badges (NEW, SALE, FEATURED, etc.)
     */
    public java.util.List<String> getBadges() {
        java.util.List<String> badges = new java.util.ArrayList<>();
        
        if (isNew()) {
            badges.add("NEW");
        }
        if (Boolean.TRUE.equals(featured)) {
            badges.add("FEATURED");
        }
        if (Boolean.TRUE.equals(bestseller)) {
            badges.add("BESTSELLER");
        }
        if (hasActivePromotion()) {
            badges.add(promotionLabel != null ? promotionLabel : "SALE");
        }
        if (isLowStock()) {
            badges.add("LIMITED");
        }
        
        return badges;
    }

    /**
     * Get display image URL (primary or thumbnail)
     */
    public String getDisplayImageUrl() {
        return primaryImageUrl != null ? primaryImageUrl : thumbnailUrl;
    }

    /**
     * Check if product has good rating
     */
    public boolean hasGoodRating() {
        return averageRating != null 
            && averageRating.compareTo(BigDecimal.valueOf(4.0)) >= 0
            && reviewCount != null 
            && reviewCount >= 5;
    }

    /**
     * Get rating display
     */
    public String getRatingDisplay() {
        if (averageRating == null || reviewCount == null) {
            return "No reviews yet";
        }
        return String.format("%.1f ★ (%d reviews)", 
            averageRating.doubleValue(), reviewCount);
    }

    /**
     * Get category breadcrumb
     */
    public String getCategoryBreadcrumb() {
        return categoryPath != null ? categoryPath : categoryName;
    }

    @Override
    public String toString() {
        return String.format(
            "ProductListResponse[id=%s, sku=%s, name=%s, price=%s, stock=%s]",
            id, sku, name, getFormattedPrice(), stockStatus
        );
    }
}