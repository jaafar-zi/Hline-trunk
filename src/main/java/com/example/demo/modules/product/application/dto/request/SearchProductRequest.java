package com.example.demo.modules.product.application.dto.request;

import com.example.demo.common.base.dto.BaseRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Request DTO for searching products
 *
 * @author PIM Team
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Search product request")
public class SearchProductRequest extends BaseRequest {

    private static final long serialVersionUID = 1L;

    @Schema(
        description = "Search keyword (searches in name, SKU, description)",
        example = "wireless headphones"
    )
    private String keyword;

    @Schema(
        description = "Product status filter",
        example = "PUBLISHED",
        allowableValues = {"DRAFT", "READY", "PUBLISHED", "UNPUBLISHED", "DISCONTINUED"}
    )
    private String status;

    @Schema(
        description = "Category ID filter",
        example = "550e8400-e29b-41d4-a716-446655440000"
    )
    private UUID categoryId;

    @Schema(
        description = "Category IDs filter (search in multiple categories)",
        example = "[\"550e8400-e29b-41d4-a716-446655440000\", \"660e8400-e29b-41d4-a716-446655440001\"]"
    )
    private List<UUID> categoryIds;

    @Schema(
        description = "Brand filter",
        example = "AudioTech"
    )
    private String brand;

    @Schema(
        description = "Multiple brands filter",
        example = "[\"AudioTech\", \"SoundPro\"]"
    )
    private List<String> brands;

    @Schema(
        description = "Minimum price",
        example = "50.00"
    )
    @Min(value = 0, message = "Minimum price cannot be negative")
    private BigDecimal minPrice;

    @Schema(
        description = "Maximum price",
        example = "200.00"
    )
    @Min(value = 0, message = "Maximum price cannot be negative")
    private BigDecimal maxPrice;

    @Schema(
        description = "In stock only",
        example = "true"
    )
    private Boolean inStock;

    @Schema(
        description = "Has promotional price",
        example = "false"
    )
    private Boolean hasPromotion;

    @Schema(
        description = "Attribute filters (key-value pairs)",
        example = "{\"color\": \"Black\", \"connectivity\": \"Bluetooth\"}"
    )
    private java.util.Map<String, String> attributes;

    @Schema(
        description = "Sort field",
        example = "name",
        allowableValues = {"name", "price", "createdAt", "publishedAt", "popularity"}
    )
    private String sortBy = "createdAt";

    @Schema(
        description = "Sort direction",
        example = "DESC",
        allowableValues = {"ASC", "DESC"}
    )
    private String sortDirection = "DESC";

    @Schema(
        description = "Page number (0-based)",
        example = "0"
    )
    @Min(value = 0, message = "Page number cannot be negative")
    private Integer page = 0;

    @Schema(
        description = "Page size",
        example = "20"
    )
    @Min(value = 1, message = "Page size must be at least 1")
    @Max(value = 100, message = "Page size cannot exceed 100")
    private Integer size = 20;

    @Override
    public void validate() {
        super.validate();

        if (keyword != null) {
            keyword = keyword.trim();
        }

        if (status != null) {
            status = status.trim().toUpperCase();
        }

        if (brand != null) {
            brand = brand.trim();
        }

        if (sortBy != null) {
            sortBy = sortBy.trim().toLowerCase();
        }

        if (sortDirection != null) {
            sortDirection = sortDirection.trim().toUpperCase();
        }

        // Validate price range
        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            throw new IllegalArgumentException("Minimum price cannot be greater than maximum price");
        }
    }
}