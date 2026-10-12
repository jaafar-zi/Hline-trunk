package com.example.demo.modules.product.application.dto.filter;

import com.example.demo.common.base.dto.BaseFilter;
import com.example.demo.common.normalization.Normalize;
import com.example.demo.common.validator.constraints.ValidDateRange;
import com.example.demo.common.validator.constraints.ValidEnum;
import com.example.demo.common.validator.constraints.ValidPriceRange;
import com.example.demo.modules.product.domain.model.vo.ProductStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Product filter criteria")
@ValidPriceRange(min = "minPrice", max = "maxPrice")
@ValidDateRange(min = "createdAfter", max = "createdBefore", message = "createdAfter")
@ValidDateRange(min = "publishedAfter", max = "publishedBefore", message = "publishedAfter")
@ValidDateRange(min = "updatedAfter", max = "updatedBefore", message = "updatedAfter")
@ValidDateRange(min = "availableFrom", max = "availableUntil", message = "availableFrom")
public class ProductFilter extends BaseFilter {

    private static final long serialVersionUID = 1L;

    @Schema(description = "Search keyword (searches in name, SKU, description, brand)", example = "wireless headphones")
    @Normalize(Normalize.Strategy.TRIM)
    private String keyword;

    @Schema(description = "Product SKU (exact match)", example = "PROD-20240115-A1B2C3")
    @Normalize({Normalize.Strategy.TRIM, Normalize.Strategy.UPPERCASE})
    private String sku;

    @Schema(description = "Product name (partial match)", example = "headphones")
    @Normalize(Normalize.Strategy.TRIM)
    private String name;

    @Schema(description = "Product IDs", example = "[\"550e8400-e29b-41d4-a716-446655440000\"]")
    private List<UUID> ids;

    @Schema(
        description = "Product status",
        example = "PUBLISHED",
        allowableValues = {"DRAFT", "READY", "PUBLISHED", "UNPUBLISHED", "DISCONTINUED"}
    )
    @ValidEnum(enumClass = ProductStatus.class, allowNull = true, message = "Invalid product status")
    @Normalize(Normalize.Strategy.UPPERCASE)
    private String status;

    @Schema(description = "Multiple product statuses", example = "[\"PUBLISHED\", \"READY\"]")
    private List<@ValidEnum(enumClass = ProductStatus.class) String> statuses;

    @Schema(description = "Only published products", example = "true")
    private Boolean publishedOnly;

    @Schema(description = "Include inactive products", example = "false")
    private Boolean includeInactive;

    @Schema(description = "Category ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID categoryId;

    @Schema(description = "Multiple category IDs", example = "[\"550e8400-e29b-41d4-a716-446655440000\"]")
    private List<UUID> categoryIds;

    @Schema(description = "Category code", example = "ELECTRONICS")
    @Normalize(Normalize.Strategy.UPPERCASE)
    private String categoryCode;

    @Schema(description = "Include subcategories in category filter", example = "false", defaultValue = "false")
    @Builder.Default
    private Boolean includeSubcategories = false;

    @Schema(description = "Brand name (exact match)", example = "AudioTech")
    @Normalize(Normalize.Strategy.TRIM)
    private String brand;

    @Schema(description = "Brand name (partial match)", example = "Audio")
    @Normalize(Normalize.Strategy.TRIM)
    private String brandLike;

    @Schema(description = "Multiple brands", example = "[\"AudioTech\", \"SoundPro\"]")
    private List<String> brands;

    @Schema(description = "Manufacturer name", example = "AudioTech Industries")
    @Normalize(Normalize.Strategy.TRIM)
    private String manufacturer;

    @Schema(description = "Minimum price", example = "50.00")
    @Min(value = 0, message = "Minimum price cannot be negative")
    private BigDecimal minPrice;

    @Schema(description = "Maximum price", example = "500.00")
    @Min(value = 0, message = "Maximum price cannot be negative")
    private BigDecimal maxPrice;

    @Schema(description = "Currency filter", example = "USD")
    @Normalize(Normalize.Strategy.UPPERCASE)
    private String currency;

    @Schema(description = "Has active price", example = "true")
    private Boolean hasActivePrice;

    @Schema(description = "Has promotional price", example = "false")
    private Boolean hasPromotionalPrice;

    @Schema(description = "In stock only", example = "true")
    private Boolean inStock;

    @Schema(description = "Out of stock only", example = "false")
    private Boolean outOfStock;

    @Schema(description = "Low stock only", example = "false")
    private Boolean lowStock;

    @Schema(description = "Minimum available quantity", example = "10")
    @Min(value = 0, message = "Minimum quantity cannot be negative")
    private Integer minQuantity;

    @Schema(description = "Maximum available quantity", example = "1000")
    @Min(value = 0, message = "Maximum quantity cannot be negative")
    private Integer maxQuantity;

    @Schema(description = "Attribute keys to filter by", example = "[\"color\"]")
    private List<String> attributeKeys;

    @Schema(description = "Attribute values to filter by (parallel to attributeKeys)", example = "[\"Black\"]")
    private List<String> attributeValues;

    @Schema(description = "Has all required attributes filled", example = "true")
    private Boolean hasAllRequiredAttributes;

    @Schema(description = "Has variants", example = "false")
    private Boolean hasVariants;

    @Schema(description = "Has images", example = "true")
    private Boolean hasImages;

    @Schema(description = "Has primary image", example = "true")
    private Boolean hasPrimaryImage;

    @Schema(description = "Created after date", example = "2026-01-01T00:00:00Z")
    private Instant createdAfter;

    @Schema(description = "Created before date", example = "2026-12-31T23:59:59Z")
    private Instant createdBefore;

    @Schema(description = "Published after date", example = "2026-01-01T00:00:00Z")
    private Instant publishedAfter;

    @Schema(description = "Published before date", example = "2026-12-31T23:59:59Z")
    private Instant publishedBefore;

    @Schema(description = "Updated after date", example = "2026-01-01T00:00:00Z")
    private Instant updatedAfter;

    @Schema(description = "Updated before date", example = "2026-12-31T23:59:59Z")
    private Instant updatedBefore;

    @Schema(description = "Available from date", example = "2026-01-01T00:00:00Z")
    private Instant availableFrom;

    @Schema(description = "Available until date", example = "2026-12-31T23:59:59Z")
    private Instant availableUntil;

    @Schema(description = "Ready to publish", example = "false")
    private Boolean readyToPublish;

    @Schema(description = "Incomplete products", example = "false")
    private Boolean incomplete;

    @Schema(description = "Page number (0-based)", example = "0")
    @Min(value = 0, message = "Page number cannot be negative")
    @Builder.Default
    private Integer page = 0;

    @Schema(description = "Page size", example = "20")
    @Min(value = 1, message = "Page size must be at least 1")
    @Max(value = 100, message = "Page size cannot exceed 100")
    @Builder.Default
    private Integer size = 20;

    @Schema(
        description = "Sort by field",
        example = "createdAt",
        allowableValues = {"name", "sku", "createdAt", "updatedAt", "publishedAt", "price"}
    )
    @Builder.Default
    private String sortBy = "createdAt";

    @Schema(description = "Sort direction", example = "DESC", allowableValues = {"ASC", "DESC"})
    @Builder.Default
    private String sortDirection = "DESC";

    @Override
    public boolean hasFilters() {
        return keyword != null || sku != null || name != null ||
            status != null || (statuses != null && !statuses.isEmpty()) ||
            categoryId != null || (categoryIds != null && !categoryIds.isEmpty()) ||
            brand != null || brandLike != null || (brands != null && !brands.isEmpty()) ||
            manufacturer != null ||
            minPrice != null || maxPrice != null || currency != null ||
            hasActivePrice != null || hasPromotionalPrice != null ||
            inStock != null || outOfStock != null || lowStock != null ||
            minQuantity != null || maxQuantity != null ||
            (attributeKeys != null && !attributeKeys.isEmpty()) ||
            (attributeValues != null && !attributeValues.isEmpty()) ||
            hasAllRequiredAttributes != null ||
            hasVariants != null ||
            hasImages != null || hasPrimaryImage != null ||
            createdAfter != null || createdBefore != null ||
            publishedAfter != null || publishedBefore != null ||
            updatedAfter != null || updatedBefore != null ||
            availableFrom != null || availableUntil != null ||
            readyToPublish != null || incomplete != null ||
            publishedOnly != null || includeInactive != null ||
            (ids != null && !ids.isEmpty());
    }

    @Override
    public void normalize() {
        super.normalize();

        if (statuses != null && !statuses.isEmpty()) {
            statuses = statuses.stream()
                .map(s -> s != null ? s.trim().toUpperCase() : null)
                .filter(s -> s != null && !s.isEmpty())
                .toList();
        }

        if (brands != null && !brands.isEmpty()) {
            brands = brands.stream()
                .map(b -> b != null ? b.trim() : null)
                .filter(b -> b != null && !b.isEmpty())
                .toList();
        }

        if (attributeKeys != null && !attributeKeys.isEmpty()) {
            attributeKeys = attributeKeys.stream()
                .map(k -> k != null ? k.trim().toLowerCase() : null)
                .filter(k -> k != null && !k.isEmpty())
                .toList();
        }

        if (attributeValues != null && !attributeValues.isEmpty()) {
            attributeValues = attributeValues.stream()
                .map(v -> v != null ? v.trim() : null)
                .filter(v -> v != null && !v.isEmpty())
                .toList();
        }
    }

    public Map<String, String> getAttributesAsMap() {
        if (attributeKeys == null || attributeValues == null || attributeKeys.isEmpty()) {
            return java.util.Collections.emptyMap();
        }
        Map<String, String> map = new java.util.LinkedHashMap<>();
        for (int i = 0; i < Math.min(attributeKeys.size(), attributeValues.size()); i++) {
            if (attributeKeys.get(i) != null && attributeValues.get(i) != null) {
                map.put(attributeKeys.get(i), attributeValues.get(i));
            }
        }
        return map;
    }
}