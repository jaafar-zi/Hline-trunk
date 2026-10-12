package com.example.demo.modules.product.application.dto.filter;

import com.example.demo.common.base.dto.BaseFilter;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import lombok.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Filter DTO for ProductVariant queries
 *
 * @author PIM Team
 * @version 1.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Product variant filter criteria")
public class ProductVariantFilter extends BaseFilter {

    private static final long serialVersionUID = 1L;

    @Schema(
        description = "Product ID",
        example = "550e8400-e29b-41d4-a716-446655440000"
    )
    private UUID productId;

    @Schema(
        description = "Variant SKU",
        example = "PROD-20240115-A1B2C3-L-BLK"
    )
    private String sku;

    @Schema(
        description = "Variant name (partial match)",
        example = "Large"
    )
    private String name;

    @Schema(
        description = "Active variants only",
        example = "true"
    )
    private Boolean activeOnly;

    @Schema(
        description = "In stock only",
        example = "true"
    )
    private Boolean inStock;

    @Schema(
        description = "Default variant only",
        example = "false"
    )
    private Boolean defaultOnly;

    @Schema(
        description = "Variant attributes (key-value pairs)",
        example = "{\"size\": \"Large\", \"color\": \"Black\"}"
    )
    private Map<String, String> attributes;

    @Schema(
        description = "Minimum price",
        example = "50.00"
    )
    @DecimalMin(value = "0.00", message = "Minimum price cannot be negative")
    private BigDecimal minPrice;

    @Schema(
        description = "Maximum price",
        example = "200.00"
    )
    @DecimalMin(value = "0.00", message = "Maximum price cannot be negative")
    private BigDecimal maxPrice;

    @Schema(
        description = "Has custom price (different from base product price)",
        example = "true"
    )
    private Boolean hasCustomPrice;

    @Schema(
        description = "Minimum available quantity",
        example = "10"
    )
    @Min(value = 0, message = "Minimum quantity cannot be negative")
    private Integer minQuantity;

    @Schema(
        description = "Maximum available quantity",
        example = "1000"
    )
    @Min(value = 0, message = "Maximum quantity cannot be negative")
    private Integer maxQuantity;

    @Schema(
        description = "Variant barcode",
        example = "1234567890123"
    )
    private String barcode;

    @Schema(
        description = "Has images",
        example = "true"
    )
    private Boolean hasImages;

    // Constructor for builder pattern compatibility
    @Builder
    public ProductVariantFilter(
            // BaseFilter fields
            Integer page,
            Integer size,
            String sortBy,
            String sortDirection,
            Boolean excludeDeleted,
            // ProductVariantFilter specific fields
            UUID productId,
            String sku,
            String name,
            Boolean activeOnly,
            Boolean inStock,
            Boolean defaultOnly,
            Map<String, String> attributes,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Boolean hasCustomPrice,
            Integer minQuantity,
            Integer maxQuantity,
            String barcode,
            Boolean hasImages
    ) {
        // Initialize parent fields with variant-specific defaults
        this.page = page != null ? page : 0;
        this.size = size != null ? size : 20;
        this.sortBy = sortBy != null ? sortBy : "displayOrder";
        this.sortDirection = sortDirection != null ? sortDirection : "ASC";
        this.excludeDeleted = excludeDeleted != null ? excludeDeleted : true;
        
        // Initialize variant-specific fields
        this.productId = productId;
        this.sku = sku;
        this.name = name;
        this.activeOnly = activeOnly;
        this.inStock = inStock;
        this.defaultOnly = defaultOnly;
        this.attributes = attributes;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
        this.hasCustomPrice = hasCustomPrice;
        this.minQuantity = minQuantity;
        this.maxQuantity = maxQuantity;
        this.barcode = barcode;
        this.hasImages = hasImages;
    }

    @Override
    public void normalize() {
        super.normalize(); // Call parent normalization
        
        // Normalize SKU
        if (sku != null) {
            sku = sku.trim().toUpperCase();
            if (sku.isEmpty()) {
                sku = null;
            }
        }
        
        // Normalize name
        if (name != null) {
            name = name.trim();
            if (name.isEmpty()) {
                name = null;
            }
        }
        
        // Normalize barcode
        if (barcode != null) {
            barcode = barcode.trim();
            if (barcode.isEmpty()) {
                barcode = null;
            }
        }
        
        // Normalize attributes map
        if (attributes != null && !attributes.isEmpty()) {
            attributes = attributes.entrySet().stream()
                .filter(entry -> entry.getKey() != null && entry.getValue() != null)
                .filter(entry -> !entry.getKey().trim().isEmpty() && !entry.getValue().trim().isEmpty())
                .collect(Collectors.toMap(
                    entry -> entry.getKey().trim().toLowerCase(),
                    entry -> entry.getValue().trim(),
                    (v1, v2) -> v1, // In case of duplicates, keep first
                    HashMap::new
                ));
            
            if (attributes.isEmpty()) {
                attributes = null;
            }
        }
        
        // Normalize sortBy to allowed values
        if (sortBy != null) {
            String normalizedSortBy = sortBy.trim().toLowerCase();
            if (!isValidSortField(normalizedSortBy)) {
                sortBy = "displayOrder"; // Default fallback
            } else {
                sortBy = normalizedSortBy;
            }
        }
        
        // Validate price range
        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            BigDecimal temp = minPrice;
            minPrice = maxPrice;
            maxPrice = temp;
        }
        
        // Normalize negative prices to zero
        if (minPrice != null && minPrice.compareTo(BigDecimal.ZERO) < 0) {
            minPrice = BigDecimal.ZERO;
        }
        if (maxPrice != null && maxPrice.compareTo(BigDecimal.ZERO) < 0) {
            maxPrice = null;
        }
        
        // Validate quantity range
        if (minQuantity != null && maxQuantity != null && minQuantity > maxQuantity) {
            Integer temp = minQuantity;
            minQuantity = maxQuantity;
            maxQuantity = temp;
        }
        
        // Normalize negative quantities to zero
        if (minQuantity != null && minQuantity < 0) {
            minQuantity = 0;
        }
        if (maxQuantity != null && maxQuantity < 0) {
            maxQuantity = null;
        }
    }

    @Override
    public boolean hasFilters() {
        return productId != null 
            || sku != null 
            || name != null
            || activeOnly != null 
            || inStock != null
            || defaultOnly != null
            || attributes != null 
            || minPrice != null 
            || maxPrice != null
            || hasCustomPrice != null
            || minQuantity != null
            || maxQuantity != null
            || barcode != null
            || hasImages != null;
    }

    @Override
    protected int getMaxPageSize() {
        return 100; // Variant-specific max page size
    }

    /**
     * Validate sort field against allowed values
     */
    private boolean isValidSortField(String field) {
        return field != null && (
            field.equals("name") ||
            field.equals("sku") ||
            field.equals("displayorder") ||
            field.equals("createdat") ||
            field.equals("updatedat") ||
            field.equals("price") ||
            field.equals("quantity")
        );
    }

    /**
     * Check if filtering by product
     */
    public boolean isFilteringByProduct() {
        return productId != null;
    }

    /**
     * Check if filtering by availability
     */
    public boolean isFilteringByAvailability() {
        return inStock != null || minQuantity != null || maxQuantity != null;
    }

    /**
     * Check if filtering by price range
     */
    public boolean isFilteringByPriceRange() {
        return minPrice != null || maxPrice != null || hasCustomPrice != null;
    }

    /**
     * Check if filtering by attributes
     */
    public boolean isFilteringByAttributes() {
        return attributes != null && !attributes.isEmpty();
    }

    /**
     * Check if filtering by specific attribute
     */
    public boolean hasAttribute(String attributeName) {
        return attributes != null && attributes.containsKey(attributeName.toLowerCase());
    }

    /**
     * Get attribute value
     */
    public String getAttributeValue(String attributeName) {
        if (attributes == null) {
            return null;
        }
        return attributes.get(attributeName.toLowerCase());
    }

    /**
     * Get all attribute names
     */
    public java.util.Set<String> getAttributeNames() {
        if (attributes == null) {
            return java.util.Collections.emptySet();
        }
        return attributes.keySet();
    }

    /**
     * Check if filtering for active and in-stock variants
     */
    public boolean isActiveAndInStockFilter() {
        return Boolean.TRUE.equals(activeOnly) && Boolean.TRUE.equals(inStock);
    }

    /**
     * Check if filtering for default variant only
     */
    public boolean isDefaultVariantFilter() {
        return Boolean.TRUE.equals(defaultOnly);
    }

    /**
     * Check if filtering by identification (SKU or barcode)
     */
    public boolean isFilteringByIdentification() {
        return sku != null || barcode != null;
    }

    /**
     * Check if this is a comprehensive product variant search
     */
    public boolean isComprehensiveSearch() {
        return productId != null 
            && (activeOnly != null || inStock != null)
            && (attributes != null || minPrice != null || maxPrice != null);
    }

    /**
     * Get attribute count
     */
    public int getAttributeCount() {
        return attributes != null ? attributes.size() : 0;
    }

    /**
     * Check if price range is valid
     */
    public boolean hasValidPriceRange() {
        if (minPrice == null && maxPrice == null) {
            return true;
        }
        if (minPrice == null || maxPrice == null) {
            return true;
        }
        return minPrice.compareTo(maxPrice) <= 0;
    }

    /**
     * Check if quantity range is valid
     */
    public boolean hasValidQuantityRange() {
        if (minQuantity == null && maxQuantity == null) {
            return true;
        }
        if (minQuantity == null || maxQuantity == null) {
            return true;
        }
        return minQuantity <= maxQuantity;
    }

    /**
     * Get effective page size with variant-specific default
     */
    @Override
    public int getEffectiveSize() {
        return size != null ? size : 20;
    }

    /**
     * Add or update attribute filter
     */
    public void addAttribute(String key, String value) {
        if (this.attributes == null) {
            this.attributes = new HashMap<>();
        }
        this.attributes.put(key.toLowerCase(), value);
    }

    /**
     * Remove attribute filter
     */
    public void removeAttribute(String key) {
        if (this.attributes != null) {
            this.attributes.remove(key.toLowerCase());
            if (this.attributes.isEmpty()) {
                this.attributes = null;
            }
        }
    }

    /**
     * Clear all attributes
     */
    public void clearAttributes() {
        this.attributes = null;
    }
}