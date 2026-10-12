package com.example.demo.modules.product.application.mapper;

import com.example.demo.common.mapper.BaseMapper;
import com.example.demo.modules.product.application.dto.request.AddProductPriceRequest;
import com.example.demo.modules.product.application.dto.request.UpdatePriceRequest;
import com.example.demo.modules.product.application.dto.response.ProductPriceResponse;
import com.example.demo.modules.product.domain.model.ProductPrice;
import com.example.demo.modules.product.domain.model.vo.Money;
import com.example.demo.modules.product.domain.model.vo.PriceType;
import org.mapstruct.*;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Lean MapStruct mapper for ProductPrice entity <-> DTOs
 * 
 * Performance optimized - avoids heavy computed fields
 *
 * @author PIM Team
 * @version 2.0
 */
@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
    injectionStrategy = InjectionStrategy.CONSTRUCTOR,
    imports = {PriceType.class}
)
@Component
public abstract class ProductPriceMapper implements BaseMapper<ProductPrice, ProductPriceResponse> {

    // ====================================
    // Entity to Response - LEAN
    // ====================================

    /**
     * Map ProductPrice entity to ProductPriceResponse (LEAN)
     */
    @Override
    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "variantId", ignore = true) // Not in entity yet
    @Mapping(target = "amount", expression = "java(extractAmount(price))")
    @Mapping(target = "currency", expression = "java(extractCurrency(price))")
    @Mapping(target = "priceType", expression = "java(mapPriceType(price.getPriceType()))")
    @Mapping(target = "active", source = "active")
    @Mapping(target = "priority", source = "priority")
    @Mapping(target = "validFrom", source = "validFrom")
    @Mapping(target = "validTo", source = "validTo")
    @Mapping(target = "discountPercentage", source = "discountPercentage")
    @Mapping(target = "originalPrice", expression = "java(extractOriginalPrice(price))")
    @Mapping(target = "customerGroup", source = "customerGroup")
    @Mapping(target = "market", source = "market")
    @Mapping(target = "taxInclusive", source = "taxInclusive")
    @Mapping(target = "taxRate", source = "taxRate")
    public abstract ProductPriceResponse toDto(ProductPrice price);

    /**
     * Alias for better readability
     */
    public ProductPriceResponse toResponse(ProductPrice price) {
        return price != null ? toDto(price) : null;
    }

    /**
     * Map list to responses
     */
    @Override
    public List<ProductPriceResponse> toDtoList(List<ProductPrice> prices) {
        if (prices == null || prices.isEmpty()) {
            return Collections.emptyList();
        }
        return prices.stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    /**
     * Map to sorted list by priority
     */
    public List<ProductPriceResponse> toSortedList(List<ProductPrice> prices) {
        if (prices == null || prices.isEmpty()) {
            return Collections.emptyList();
        }

        return prices.stream()
            .map(this::toResponse)
            .sorted(Comparator.comparing(ProductPriceResponse::getPriority).reversed()
                .thenComparing(p -> p.getValidFrom() != null ? p.getValidFrom() : java.time.LocalDateTime.MIN))
            .collect(Collectors.toList());
    }

    // ====================================
    // Request to Entity - CREATE
    // ====================================

    /**
     * Map AddProductPriceRequest to ProductPrice entity
     */
    @Mapping(target = "product", ignore = true) // Set by service
    @Mapping(target = "price", expression = "java(createMoney(request.getAmount(), request.getCurrency()))")
    @Mapping(target = "priceType", expression = "java(parsePriceType(request.getPriceType()))")
    @Mapping(target = "active", constant = "true") // New prices are active by default
    @Mapping(target = "priority", source = "priority")
    @Mapping(target = "validFrom", source = "validFrom")
    @Mapping(target = "validTo", source = "validTo")
    @Mapping(target = "discountPercentage", source = "discountPercentage")
    @Mapping(target = "taxInclusive", source = "taxInclusive")
    @Mapping(target = "taxRate", source = "taxRate")
    @Mapping(target = "customerGroup", source = "customerGroup")
    @Mapping(target = "market", source = "market")
    // Calculated/set by service
    @Mapping(target = "discountAmount", ignore = true) // Calculated by entity
    @Mapping(target = "originalPrice", ignore = true) // Set by service if promotional
    @Mapping(target = "taxClass", ignore = true)
    @Mapping(target = "priceListId", ignore = true)
    @Mapping(target = "notes", ignore = true)
    public abstract ProductPrice fromAddRequest(AddProductPriceRequest request);

    /**
     * Not supported
     */
    @Override
    public ProductPrice toEntity(ProductPriceResponse dto) {
        throw new UnsupportedOperationException(
            "Cannot create entity from response DTO. Use fromAddRequest() instead."
        );
    }

    // ====================================
    // Request to Entity - UPDATE
    // ====================================

    /**
     * Update existing ProductPrice from UpdatePriceRequest
     */
    @BeanMapping(
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS
    )
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "price", expression = "java(updateMoney(request.getAmount(), entity.getPrice()))")
    @Mapping(target = "priceType", ignore = true) // Immutable
    @Mapping(target = "active", source = "active")
    @Mapping(target = "priority", source = "priority")
    @Mapping(target = "validFrom", source = "validFrom")
    @Mapping(target = "validTo", source = "validTo")
    @Mapping(target = "taxRate", source = "taxRate")
    // Cannot update via this method
    @Mapping(target = "discountPercentage", ignore = true)
    @Mapping(target = "discountAmount", ignore = true)
    @Mapping(target = "originalPrice", ignore = true)
    @Mapping(target = "taxInclusive", ignore = true)
    @Mapping(target = "taxClass", ignore = true)
    @Mapping(target = "priceListId", ignore = true)
    @Mapping(target = "customerGroup", ignore = true)
    @Mapping(target = "market", ignore = true)
    @Mapping(target = "notes", ignore = true)
    public abstract void updateEntityFromRequest(
        UpdatePriceRequest request,
        @MappingTarget ProductPrice entity
    );

    // ====================================
    // Helper Methods - Money Value Object
    // ====================================

    /**
     * Create Money value object from amount and currency
     */
    protected Money createMoney(BigDecimal amount, String currency) {
        if (amount == null || currency == null) {
            throw new IllegalArgumentException("Price amount and currency are required");
        }
        return Money.of(amount, currency);
    }

    /**
     * Update Money value object with new amount (keeping currency)
     */
    protected Money updateMoney(BigDecimal newAmount, Money currentMoney) {
        if (newAmount == null) {
            return currentMoney;
        }
        String currency = currentMoney != null ? currentMoney.getCurrency() : "USD";
        return Money.of(newAmount, currency);
    }

    /**
     * Extract amount from Money value object
     */
    protected BigDecimal extractAmount(ProductPrice price) {
        return price != null && price.getPrice() != null 
            ? price.getPrice().getAmount() 
            : null;
    }

    /**
     * Extract currency from Money value object
     */
    protected String extractCurrency(ProductPrice price) {
        return price != null && price.getPrice() != null 
            ? price.getPrice().getCurrency() 
            : null;
    }

    /**
     * Extract original price amount (if exists)
     */
    protected BigDecimal extractOriginalPrice(ProductPrice price) {
        return price != null && price.getOriginalPrice() != null 
            ? price.getOriginalPrice() 
            : null;
    }

    // ====================================
    // Helper Methods - Enum Mapping
    // ====================================

    /**
     * Map PriceType enum to string
     */
    protected String mapPriceType(PriceType priceType) {
        return priceType != null ? priceType.name() : null;
    }

    /**
     * Parse string to PriceType enum
     */
    protected PriceType parsePriceType(String priceType) {
        if (priceType == null || priceType.isBlank()) {
            return PriceType.BASE;
        }
        
        try {
            return PriceType.valueOf(priceType.toUpperCase());
        } catch (IllegalArgumentException e) {
            return PriceType.BASE;
        }
    }

    // ====================================
    // Specialized Mappings
    // ====================================

    /**
     * Filter active prices only
     */
    public List<ProductPriceResponse> toActivePrices(List<ProductPrice> prices) {
        if (prices == null || prices.isEmpty()) {
            return Collections.emptyList();
        }

        return prices.stream()
            .filter(p -> Boolean.TRUE.equals(p.getActive()))
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    /**
     * Filter currently valid prices
     */
    public List<ProductPriceResponse> toCurrentlyValidPrices(List<ProductPrice> prices) {
        if (prices == null || prices.isEmpty()) {
            return Collections.emptyList();
        }

        return prices.stream()
            .filter(ProductPrice::isActive) // Uses entity's isActive() method
            .map(this::toResponse)
            .sorted(Comparator.comparing(ProductPriceResponse::getPriority).reversed())
            .collect(Collectors.toList());
    }

    /**
     * Get highest priority valid price
     */
    public ProductPriceResponse toCurrentPrice(List<ProductPrice> prices) {
        if (prices == null || prices.isEmpty()) {
            return null;
        }

        return prices.stream()
            .filter(ProductPrice::isActive)
            .max(Comparator.comparing(ProductPrice::getPriority))
            .map(this::toResponse)
            .orElse(null);
    }

    /**
     * Filter promotional prices
     */
    public List<ProductPriceResponse> toPromotionalPrices(List<ProductPrice> prices) {
        if (prices == null || prices.isEmpty()) {
            return Collections.emptyList();
        }

        return prices.stream()
            .filter(p -> PriceType.PROMOTIONAL.equals(p.getPriceType()))
            .filter(ProductPrice::isActive)
            .map(this::toResponse)
            .sorted(Comparator.comparing(ProductPriceResponse::getValidFrom).reversed())
            .collect(Collectors.toList());
    }

    // ====================================
    // Utility Methods
    // ====================================

    /**
     * Create minimal price summary
     */
    public PriceSummary toSummary(ProductPrice price) {
        if (price == null) {
            return null;
        }

        return PriceSummary.builder()
            .amount(extractAmount(price))
            .currency(extractCurrency(price))
            .priceType(mapPriceType(price.getPriceType()))
            .active(price.getActive())
            .build();
    }

    /**
     * Map to summary list
     */
    public List<PriceSummary> toSummaryList(List<ProductPrice> prices) {
        if (prices == null || prices.isEmpty()) {
            return Collections.emptyList();
        }

        return prices.stream()
            .map(this::toSummary)
            .collect(Collectors.toList());
    }

    // ====================================
    // Nested DTO
    // ====================================

    /**
     * Lightweight price summary for simple displays
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class PriceSummary {
        private BigDecimal amount;
        private String currency;
        private String priceType;
        private Boolean active;

        public String getFormattedPrice() {
            if (amount == null || currency == null) {
                return null;
            }
            return currency + " " + amount;
        }
    }
}