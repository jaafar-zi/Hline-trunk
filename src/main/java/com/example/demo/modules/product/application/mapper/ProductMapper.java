package com.example.demo.modules.product.application.mapper;

import com.example.demo.common.mapper.BaseMapper;
import com.example.demo.modules.product.application.dto.request.CreateProductRequest;
import com.example.demo.modules.product.application.dto.request.UpdateProductRequest;
import com.example.demo.modules.product.application.dto.response.ProductResponse;
import com.example.demo.modules.product.domain.model.*;
import com.example.demo.modules.product.domain.model.vo.*;
import org.mapstruct.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Lean MapStruct mapper for Product entity <-> DTOs
 * 
 * Performance optimized - focuses on essential mappings only
 *
 * @author PIM Team
 * @version 2.0
 */
@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
    injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
public abstract class ProductMapper implements BaseMapper<Product, ProductResponse> {

    // ====================================
    // Helper Methods - MUST BE FIRST
    // ====================================

    /**
     * Extract SKU value as string
     */
    protected String extractSkuValue(SKU sku) {
        return sku != null ? sku.getValue() : null;
    }

    /**
     * Extract status value as string
     */
    protected String extractStatusValue(ProductStatus status) {
        return status != null ? status.name() : null;
    }

    /**
     * Extract category name
     */
    protected String extractCategoryName(Category category) {
        return category != null ? category.getName() : null;
    }

    /**
     * Extract current price amount
     */
    protected BigDecimal extractCurrentPriceAmount(Product product) {
        if (product == null) {
            return null;
        }

        // Promotional price takes precedence
        ProductPrice promoPrice = product.getPromotionalPrice();
        if (promoPrice != null && promoPrice.getPrice() != null) {
            return promoPrice.getPrice().getAmount();
        }

        // Fall back to current price
        ProductPrice currentPrice = product.getCurrentPrice();
        return currentPrice != null && currentPrice.getPrice() != null 
            ? currentPrice.getPrice().getAmount() 
            : null;
    }

    /**
     * Extract original price amount (if promotional price exists)
     */
    protected BigDecimal extractOriginalPriceAmount(Product product) {
        if (product == null) {
            return null;
        }

        ProductPrice promoPrice = product.getPromotionalPrice();
        if (promoPrice != null) {
            ProductPrice currentPrice = product.getCurrentPrice();
            return currentPrice != null && currentPrice.getPrice() != null 
                ? currentPrice.getPrice().getAmount() 
                : null;
        }
        
        return null;
    }

    /**
     * Extract currency
     */
    protected String extractCurrency(Product product) {
        if (product == null) {
            return null;
        }

        ProductPrice price = product.getCurrentPrice();
        return price != null && price.getPrice() != null 
            ? price.getPrice().getCurrency() 
            : null;
    }

    /**
     * Extract stock status
     */
    protected String extractStockStatus(ProductInventory inventory) {
        if (inventory == null || inventory.getStockStatus() == null) {
            return "UNKNOWN";
        }
        return inventory.getStockStatus().name();
    }

    /**
     * Extract available quantity
     */
    protected Integer extractAvailableQuantity(ProductInventory inventory) {
        if (inventory == null || inventory.getStockLevel() == null) {
            return 0;
        }
        return inventory.getStockLevel().getAvailable();
    }

    /**
     * Extract primary image URL
     */
    protected String extractPrimaryImageUrl(Set<ProductImage> images) {
        if (images == null || images.isEmpty()) {
            return null;
        }

        return images.stream()
            .filter(img -> img.getImageType() == ImageType.PRIMARY)
            .map(ProductImage::getUrl)
            .findFirst()
            .orElse(null);
    }

    /**
     * Count images
     */
    protected Integer countImages(Set<ProductImage> images) {
        return images != null ? images.size() : 0;
    }

    /**
     * Count variants
     */
    protected Integer countVariants(Set<ProductVariant> variants) {
        return variants != null ? variants.size() : 0;
    }

    /**
     * Determine product type
     */
    protected String determineProductType(Product product) {
        if (product == null) {
            return "SIMPLE";
        }
        return product.hasVariants() ? "CONFIGURABLE" : "SIMPLE";
    }

    /**
     * Create SKU from request
     */
    protected SKU createSku(CreateProductRequest request) {
        if (request == null) {
            return null;
        }
        
        if (request.getSku() != null && !request.getSku().isBlank()) {
            return SKU.of(request.getSku().trim().toUpperCase());
        }
        
        // Auto-generate will be handled in service/entity
        return null;
    }

    /**
     * Create Dimensions from request
     */
    protected Dimensions createDimensions(CreateProductRequest.DimensionsRequest dto) {
        if (dto == null) {
            return null;
        }

        Dimensions.DimensionUnit dimensionUnit = parseDimensionUnit(dto.getDimensionUnit());
        Dimensions.WeightUnit weightUnit = parseWeightUnit(dto.getWeightUnit());

        return Dimensions.of(
            dto.getLength(),
            dto.getWidth(),
            dto.getHeight(),
            dto.getWeight(),
            dimensionUnit,
            weightUnit
        );
    }

    /**
     * Update Dimensions
     */
    protected Dimensions updateDimensions(
        UpdateProductRequest.UpdateDimensionsRequest dto,
        Dimensions existing
    ) {
        if (dto == null) {
            return existing;
        }

        if (existing == null) {
            return Dimensions.of(
                dto.getLength(),
                dto.getWidth(),
                dto.getHeight(),
                dto.getWeight()
            );
        }

        return Dimensions.of(
            dto.getLength() != null ? dto.getLength() : existing.getLength(),
            dto.getWidth() != null ? dto.getWidth() : existing.getWidth(),
            dto.getHeight() != null ? dto.getHeight() : existing.getHeight(),
            dto.getWeight() != null ? dto.getWeight() : existing.getWeight(),
            dto.getDimensionUnit() != null ? parseDimensionUnit(dto.getDimensionUnit()) : existing.getDimensionUnit(),
            dto.getWeightUnit() != null ? parseWeightUnit(dto.getWeightUnit()) : existing.getWeightUnit()
        );
    }

    /**
     * Parse dimension unit
     */
    protected Dimensions.DimensionUnit parseDimensionUnit(String unit) {
        if (unit == null || unit.isBlank()) {
            return Dimensions.DimensionUnit.CM;
        }
        try {
            return Dimensions.DimensionUnit.valueOf(unit.toUpperCase());
        } catch (IllegalArgumentException e) {
            return Dimensions.DimensionUnit.CM;
        }
    }

    /**
     * Parse weight unit
     */
    protected Dimensions.WeightUnit parseWeightUnit(String unit) {
        if (unit == null || unit.isBlank()) {
            return Dimensions.WeightUnit.KG;
        }
        try {
            return Dimensions.WeightUnit.valueOf(unit.toUpperCase());
        } catch (IllegalArgumentException e) {
            return Dimensions.WeightUnit.KG;
        }
    }

    // ====================================
    // Entity to Response - LEAN
    // ====================================

    @Override
    @Mapping(target = "sku", expression = "java(extractSkuValue(product.getSku()))")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "shortDescription", source = "shortDescription")
    @Mapping(target = "status", expression = "java(extractStatusValue(product.getStatus()))")
    @Mapping(target = "productType", expression = "java(determineProductType(product))")
    @Mapping(target = "slug", source = "urlSlug")
    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "categoryName", expression = "java(extractCategoryName(product.getCategory()))")
    @Mapping(target = "brand", source = "brand")
    @Mapping(target = "currentPrice", expression = "java(extractCurrentPriceAmount(product))")
    @Mapping(target = "originalPrice", expression = "java(extractOriginalPriceAmount(product))")
    @Mapping(target = "currency", expression = "java(extractCurrency(product))")
    @Mapping(target = "stockStatus", expression = "java(extractStockStatus(product.getInventory()))")
    @Mapping(target = "availableQuantity", expression = "java(extractAvailableQuantity(product.getInventory()))")
    @Mapping(target = "primaryImageUrl", expression = "java(extractPrimaryImageUrl(product.getImages()))")
    @Mapping(target = "imageCount", expression = "java(countImages(product.getImages()))")
    @Mapping(target = "hasVariants", expression = "java(product.hasVariants())")
    @Mapping(target = "variantCount", expression = "java(countVariants(product.getVariants()))")
    @Mapping(target = "publishedAt", source = "publishedAt")
    public abstract ProductResponse toDto(Product product);

    /**
     * Alias for better readability
     */
    public ProductResponse toResponse(Product product) {
        return product != null ? toDto(product) : null;
    }

    @Override
    public List<ProductResponse> toDtoList(List<Product> products) {
        if (products == null || products.isEmpty()) {
            return Collections.emptyList();
        }
        return products.stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    // ====================================
    // Request to Entity - CREATE
    // ====================================

    @Mapping(target = "sku", expression = "java(createSku(request))")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "shortDescription", source = "shortDescription")
    @Mapping(target = "longDescription", source = "longDescription")
    @Mapping(target = "brand", source = "brand")
    @Mapping(target = "manufacturer", source = "manufacturer")
    @Mapping(target = "dimensions", expression = "java(createDimensions(request.getDimensions()))")
    @Mapping(target = "metaTitle", source = "metaTitle")
    @Mapping(target = "metaDescription", source = "metaDescription")
    @Mapping(target = "searchKeywords", source = "searchKeywords")
    @Mapping(target = "status", constant = "DRAFT")
    @Mapping(target = "category", ignore = true) // Set in service layer
    @Mapping(target = "urlSlug", ignore = true) // Generated in entity
    @Mapping(target = "attributes", ignore = true) // Set in service layer
    @Mapping(target = "prices", ignore = true) // Set in service layer
    @Mapping(target = "inventory", ignore = true) // Set in service layer
    @Mapping(target = "images", ignore = true) // Set in service layer
    @Mapping(target = "variants", ignore = true) // Set in service layer
    @Mapping(target = "publishedAt", ignore = true)
    @Mapping(target = "discontinuedAt", ignore = true)
    @Mapping(target = "availableFrom", ignore = true)
    @Mapping(target = "availableUntil", ignore = true)
    public abstract Product fromCreateRequest(CreateProductRequest request);

    @Override
    public Product toEntity(ProductResponse dto) {
        throw new UnsupportedOperationException(
            "Cannot create entity from response DTO. Use fromCreateRequest() instead."
        );
    }

    // ====================================
    // Request to Entity - UPDATE
    // ====================================

    @BeanMapping(
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS
    )
    @Mapping(target = "sku", ignore = true) // SKU is immutable
    @Mapping(target = "name", source = "name")
    @Mapping(target = "shortDescription", source = "shortDescription")
    @Mapping(target = "longDescription", source = "longDescription")
    @Mapping(target = "brand", source = "brand")
    @Mapping(target = "manufacturer", source = "manufacturer")
    @Mapping(target = "dimensions", expression = "java(updateDimensions(request.getDimensions(), entity.getDimensions()))")
    @Mapping(target = "metaTitle", source = "metaTitle")
    @Mapping(target = "metaDescription", source = "metaDescription")
    @Mapping(target = "searchKeywords", source = "searchKeywords")
    @Mapping(target = "category", ignore = true) // Set in service layer
    @Mapping(target = "status", ignore = true) // Changed via specific methods
    @Mapping(target = "attributes", ignore = true)
    @Mapping(target = "prices", ignore = true)
    @Mapping(target = "inventory", ignore = true)
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "variants", ignore = true)
    @Mapping(target = "publishedAt", ignore = true)
    @Mapping(target = "discontinuedAt", ignore = true)
    @Mapping(target = "availableFrom", ignore = true)
    @Mapping(target = "availableUntil", ignore = true)
    @Mapping(target = "urlSlug", ignore = true)
    public abstract void updateEntityFromRequest(
        UpdateProductRequest request,
        @MappingTarget Product entity
    );

    // ====================================
    // Specialized Mappings
    // ====================================

    /**
     * Map to sorted list by name
     */
    public List<ProductResponse> toSortedList(List<Product> products) {
        if (products == null || products.isEmpty()) {
            return Collections.emptyList();
        }

        return products.stream()
            .map(this::toResponse)
            .sorted(Comparator.comparing(ProductResponse::getName, 
                Comparator.nullsLast(Comparator.naturalOrder())))
            .collect(Collectors.toList());
    }

    /**
     * Filter published products only
     */
    public List<ProductResponse> toPublishedProducts(List<Product> products) {
        if (products == null || products.isEmpty()) {
            return Collections.emptyList();
        }

        return products.stream()
            .filter(p -> p.getStatus() == ProductStatus.PUBLISHED)
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    /**
     * Filter in-stock products
     */
    public List<ProductResponse> toInStockProducts(List<Product> products) {
        if (products == null || products.isEmpty()) {
            return Collections.emptyList();
        }

        return products.stream()
            .filter(Product::isInStock)
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    /**
     * Filter available products (published + in stock)
     */
    public List<ProductResponse> toAvailableProducts(List<Product> products) {
        if (products == null || products.isEmpty()) {
            return Collections.emptyList();
        }

        return products.stream()
            .filter(p -> p.getStatus() == ProductStatus.PUBLISHED)
            .filter(Product::isInStock)
            .filter(Product::isAvailableNow)
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    // ====================================
    // Summary Mappings
    // ====================================

    /**
     * Create minimal product summary
     */
    public ProductSummary toSummary(Product product) {
        if (product == null) {
            return null;
        }

        return ProductSummary.builder()
            .id(product.getId())
            .sku(extractSkuValue(product.getSku()))
            .name(product.getName())
            .status(extractStatusValue(product.getStatus()))
            .price(extractCurrentPriceAmount(product))
            .currency(extractCurrency(product))
            .inStock(product.isInStock())
            .imageUrl(extractPrimaryImageUrl(product.getImages()))
            .build();
    }

    /**
     * Map to summary list
     */
    public List<ProductSummary> toSummaryList(List<Product> products) {
        if (products == null || products.isEmpty()) {
            return Collections.emptyList();
        }

        return products.stream()
            .map(this::toSummary)
            .collect(Collectors.toList());
    }

    // ====================================
    // Nested DTOs
    // ====================================

    /**
     * Lightweight product summary for dropdowns, references
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ProductSummary {
        private UUID id;
        private String sku;
        private String name;
        private String status;
        private BigDecimal price;
        private String currency;
        private Boolean inStock;
        private String imageUrl;

        public String getFormattedPrice() {
            if (price == null || currency == null) {
                return null;
            }
            return currency + " " + price;
        }

        public boolean isPublished() {
            return "PUBLISHED".equals(status);
        }
    }
}