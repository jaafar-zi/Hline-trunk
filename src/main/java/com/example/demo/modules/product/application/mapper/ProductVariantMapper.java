package com.example.demo.modules.product.application.mapper;

import com.example.demo.common.mapper.BaseMapper;
import com.example.demo.modules.product.application.dto.request.CreateProductVariantRequest;
import com.example.demo.modules.product.application.dto.request.UpdateProductVariantRequest;
import com.example.demo.modules.product.application.dto.response.ProductVariantResponse;
import com.example.demo.modules.product.domain.model.ProductVariant;
import com.example.demo.modules.product.domain.model.VariantInventory;
import com.example.demo.modules.product.domain.model.vo.Money;
import com.example.demo.modules.product.domain.model.vo.SKU;
import com.example.demo.modules.product.domain.model.vo.VariantOption;
import org.mapstruct.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
    injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
public abstract class ProductVariantMapper implements BaseMapper<ProductVariant, ProductVariantResponse> {

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
     * Extract price amount from variant's effective price
     */
    protected BigDecimal extractPriceAmount(ProductVariant variant) {
        if (variant == null) {
            return null;
        }
        Money effectivePrice = variant.getEffectivePrice();
        return effectivePrice != null ? effectivePrice.getAmount() : null;
    }

    /**
     * Extract currency from variant's effective price
     */
    protected String extractCurrency(ProductVariant variant) {
        if (variant == null) {
            return null;
        }
        Money effectivePrice = variant.getEffectivePrice();
        return effectivePrice != null ? effectivePrice.getCurrency() : null;
    }

    /**
     * Convert VariantOptions to Map<String, String> attributes
     */
    protected Map<String, String> convertOptionsToAttributes(List<VariantOption> optionValues) {
        if (optionValues == null || optionValues.isEmpty()) {
            return new HashMap<>();
        }
        
        return optionValues.stream()
            .collect(Collectors.toMap(
                VariantOption::getOptionName,
                VariantOption::getValue,
                (existing, replacement) -> replacement, // Handle duplicates
                LinkedHashMap::new // Preserve order
            ));
    }

    /**
     * Convert Map attributes to List<VariantOption>
     */
    protected List<VariantOption> convertAttributesToOptions(Map<String, String> attributes) {
        if (attributes == null || attributes.isEmpty()) {
            return new ArrayList<>();
        }
        
        return attributes.entrySet().stream()
            .map(entry -> VariantOption.builder()
                .optionName(entry.getKey())
                .value(entry.getValue())
                .build())
            .collect(Collectors.toList());
    }

    /**
     * Extract stock status from inventory
     */
    protected String extractStockStatus(VariantInventory inventory) {
        if (inventory == null || inventory.getStockStatus() == null) {
            return "UNKNOWN";
        }
        return inventory.getStockStatus().name();
    }

    /**
     * Extract available quantity from inventory
     */
    protected Integer extractAvailableQuantity(VariantInventory inventory) {
        if (inventory == null || inventory.getStockLevel() == null) {
            return 0;
        }
        return inventory.getStockLevel().getAvailable();
    }

    /**
     * Extract primary image URL
     */
    protected String extractPrimaryImageUrl(ProductVariant variant) {
        if (variant == null || variant.getImageUrls() == null || variant.getImageUrls().isEmpty()) {
            return null;
        }
        return variant.getImageUrls().get(0);
    }

    /**
     * Create SKU value object (can be null for auto-generation)
     */
    protected SKU createSku(String skuValue) {
        if (skuValue == null || skuValue.isBlank()) {
            return null;
        }
        return SKU.of(skuValue.trim().toUpperCase());
    }

    /**
     * Create Money from VariantPriceRequest
     */
    protected Money createPrice(CreateProductVariantRequest.VariantPriceRequest priceRequest) {
        if (priceRequest == null || priceRequest.getAmount() == null) {
            return null;
        }
        
        String currency = priceRequest.getCurrency() != null 
            ? priceRequest.getCurrency() 
            : "USD";
            
        return Money.of(priceRequest.getAmount(), currency);
    }

    /**
     * Extract price adjustment amount
     */
    protected BigDecimal extractPriceAdjustment(CreateProductVariantRequest.VariantPriceRequest priceRequest) {
        return priceRequest != null ? priceRequest.getPriceAdjustment() : null;
    }

    /**
     * Extract price adjustment type
     */
    protected ProductVariant.PriceAdjustmentType extractPriceAdjustmentType(
            CreateProductVariantRequest.VariantPriceRequest priceRequest
    ) {
        if (priceRequest == null || priceRequest.getPriceAdjustmentType() == null) {
            return null;
        }
        
        try {
            return ProductVariant.PriceAdjustmentType.valueOf(
                priceRequest.getPriceAdjustmentType().toUpperCase()
            );
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Update price - create new Money with new amount, keeping currency
     */
    protected Money updatePrice(BigDecimal newAmount, Money currentPrice) {
        if (newAmount == null) {
            return currentPrice;
        }
        
        String currency = currentPrice != null ? currentPrice.getCurrency() : "USD";
        return Money.of(newAmount, currency);
    }

    // ====================================
    // Entity to Response - LEAN
    // ====================================

    @Override
    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "sku", expression = "java(extractSkuValue(variant.getSku()))")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "attributes", expression = "java(convertOptionsToAttributes(variant.getOptionValues()))")
    @Mapping(target = "price", expression = "java(extractPriceAmount(variant))")
    @Mapping(target = "currency", expression = "java(extractCurrency(variant))")
    @Mapping(target = "priceAdjustment", source = "priceAdjustment")
    @Mapping(target = "stockStatus", expression = "java(extractStockStatus(variant.getInventory()))")
    @Mapping(target = "availableQuantity", expression = "java(extractAvailableQuantity(variant.getInventory()))")
    @Mapping(target = "primaryImageUrl", expression = "java(extractPrimaryImageUrl(variant))")
    @Mapping(target = "displayOrder", source = "displayOrder")
    @Mapping(target = "isDefault", source = "isDefault")
    @Mapping(target = "active", source = "active")
    @Mapping(target = "barcode", source = "barcode")
    public abstract ProductVariantResponse toDto(ProductVariant variant);

    /**
     * Alias for better readability
     */
    public ProductVariantResponse toResponse(ProductVariant variant) {
        return variant != null ? toDto(variant) : null;
    }

    @Override
    public List<ProductVariantResponse> toDtoList(List<ProductVariant> variants) {
        if (variants == null || variants.isEmpty()) {
            return Collections.emptyList();
        }
        return variants.stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    public List<ProductVariantResponse> toSortedList(List<ProductVariant> variants) {
        if (variants == null || variants.isEmpty()) {
            return Collections.emptyList();
        }

        return variants.stream()
            .map(this::toResponse)
            .sorted(Comparator.comparing(ProductVariantResponse::getDisplayOrder)
                .thenComparing(v -> v.getName() != null ? v.getName() : ""))
            .collect(Collectors.toList());
    }

    // ====================================
    // Request to Entity - CREATE
    // ====================================

    @Mapping(target = "product", ignore = true)
    @Mapping(target = "sku", expression = "java(createSku(request.getSku()))")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "optionValues", expression = "java(convertAttributesToOptions(request.getAttributes()))")
    @Mapping(target = "price", expression = "java(createPrice(request.getPrice()))")
    @Mapping(target = "priceAdjustment", expression = "java(extractPriceAdjustment(request.getPrice()))")
    @Mapping(target = "priceAdjustmentType", expression = "java(extractPriceAdjustmentType(request.getPrice()))")
    @Mapping(target = "displayOrder", source = "displayOrder")
    @Mapping(target = "isDefault", source = "isDefault")
    @Mapping(target = "barcode", source = "barcode")
    @Mapping(target = "active", constant = "true")
    @Mapping(target = "inventory", ignore = true)
    @Mapping(target = "imageUrls", ignore = true)
    public abstract ProductVariant fromCreateRequest(CreateProductVariantRequest request);

    @Override
    public ProductVariant toEntity(ProductVariantResponse dto) {
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
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "sku", ignore = true)
    @Mapping(target = "name", source = "name")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "optionValues", expression = "java(convertAttributesToOptions(request.getAttributes()))")
    @Mapping(target = "price", expression = "java(updatePrice(request.getPrice(), entity.getPrice()))")
    @Mapping(target = "displayOrder", source = "displayOrder")
    @Mapping(target = "barcode", source = "barcode")
    @Mapping(target = "priceAdjustment", ignore = true)
    @Mapping(target = "priceAdjustmentType", ignore = true)
    @Mapping(target = "isDefault", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "inventory", ignore = true)
    @Mapping(target = "imageUrls", ignore = true)
    public abstract void updateEntityFromRequest(
        UpdateProductVariantRequest request,
        @MappingTarget ProductVariant entity
    );

    // ====================================
    // Specialized Mappings
    // ====================================

    public List<ProductVariantResponse> toActiveVariants(List<ProductVariant> variants) {
        if (variants == null || variants.isEmpty()) {
            return Collections.emptyList();
        }

        return variants.stream()
            .filter(v -> Boolean.TRUE.equals(v.getActive()))
            .map(this::toResponse)
            .sorted(Comparator.comparing(ProductVariantResponse::getDisplayOrder))
            .collect(Collectors.toList());
    }

    public List<ProductVariantResponse> toInStockVariants(List<ProductVariant> variants) {
        if (variants == null || variants.isEmpty()) {
            return Collections.emptyList();
        }

        return variants.stream()
            .filter(v -> v.getInventory() != null && v.getInventory().isInStock())
            .map(this::toResponse)
            .sorted(Comparator.comparing(ProductVariantResponse::getDisplayOrder))
            .collect(Collectors.toList());
    }

    public ProductVariantResponse toDefaultVariant(List<ProductVariant> variants) {
        if (variants == null || variants.isEmpty()) {
            return null;
        }

        return variants.stream()
            .filter(v -> Boolean.TRUE.equals(v.getIsDefault()))
            .findFirst()
            .map(this::toResponse)
            .orElse(null);
    }

    public VariantSummary toSummary(ProductVariant variant) {
        if (variant == null) {
            return null;
        }

        return VariantSummary.builder()
            .id(variant.getId())
            .sku(extractSkuValue(variant.getSku()))
            .name(variant.getName())
            .price(extractPriceAmount(variant))
            .currency(extractCurrency(variant))
            .inStock(variant.getInventory() != null && variant.getInventory().isInStock())
            .active(variant.getActive())
            .build();
    }

    public List<VariantSummary> toSummaryList(List<ProductVariant> variants) {
        if (variants == null || variants.isEmpty()) {
            return Collections.emptyList();
        }

        return variants.stream()
            .map(this::toSummary)
            .sorted(Comparator.comparing(VariantSummary::getSku, 
                Comparator.nullsLast(Comparator.naturalOrder())))
            .collect(Collectors.toList());
    }

    // ====================================
    // Nested DTO
    // ====================================

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class VariantSummary {
        private UUID id;
        private String sku;
        private String name;
        private BigDecimal price;
        private String currency;
        private Boolean inStock;
        private Boolean active;

        public String getFormattedPrice() {
            if (price == null || currency == null) {
                return null;
            }
            return currency + " " + price;
        }
    }
}