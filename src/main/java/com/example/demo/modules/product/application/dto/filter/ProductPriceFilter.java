package com.example.demo.modules.product.application.dto.filter;

import com.example.demo.common.base.dto.BaseFilter;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Filter DTO for ProductPrice queries
 *
 * @author PIM Team
 * @version 1.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Product price filter criteria")
public class ProductPriceFilter extends BaseFilter {

    private static final long serialVersionUID = 1L;

    @Schema(
        description = "Product ID",
        example = "550e8400-e29b-41d4-a716-446655440000"
    )
    private UUID productId;

    @Schema(
        description = "Price type",
        example = "BASE",
        allowableValues = {"BASE", "PROMOTIONAL", "TIERED", "WHOLESALE"}
    )
    private String priceType;

    @Schema(
        description = "Multiple price types",
        example = "[\"BASE\", \"PROMOTIONAL\"]"
    )
    private List<String> priceTypes;

    @Schema(
        description = "Currency code (ISO 4217)",
        example = "USD"
    )
    private String currency;

    @Schema(
        description = "Active prices only",
        example = "true"
    )
    private Boolean activeOnly;

    @Schema(
        description = "Currently valid prices only (within validity period)",
        example = "true"
    )
    private Boolean currentlyValid;

    @Schema(
        description = "Scheduled prices (future validity)",
        example = "false"
    )
    private Boolean scheduled;

    @Schema(
        description = "Expired prices",
        example = "false"
    )
    private Boolean expired;

    @Schema(
        description = "Minimum price amount",
        example = "10.00"
    )
    @DecimalMin(value = "0.00", message = "Minimum price cannot be negative")
    private BigDecimal minAmount;

    @Schema(
        description = "Maximum price amount",
        example = "1000.00"
    )
    @DecimalMin(value = "0.00", message = "Maximum price cannot be negative")
    private BigDecimal maxAmount;

    @Schema(
        description = "Customer group",
        example = "RETAIL"
    )
    private String customerGroup;

    @Schema(
        description = "Market code",
        example = "US"
    )
    private String market;

    @Schema(
        description = "Has discount",
        example = "true"
    )
    private Boolean hasDiscount;

    @Schema(
        description = "Valid from date (after this date)",
        example = "2024-01-01T00:00:00"
    )
    private LocalDateTime validFromAfter;

    @Schema(
        description = "Valid to date (before this date)",
        example = "2024-12-31T23:59:59"
    )
    private LocalDateTime validToBefore;

    // Constructor for builder pattern compatibility
    @Builder
    public ProductPriceFilter(
            // BaseFilter fields
            Integer page,
            Integer size,
            String sortBy,
            String sortDirection,
            Boolean excludeDeleted,
            // ProductPriceFilter specific fields
            UUID productId,
            String priceType,
            List<String> priceTypes,
            String currency,
            Boolean activeOnly,
            Boolean currentlyValid,
            Boolean scheduled,
            Boolean expired,
            BigDecimal minAmount,
            BigDecimal maxAmount,
            String customerGroup,
            String market,
            Boolean hasDiscount,
            LocalDateTime validFromAfter,
            LocalDateTime validToBefore
    ) {
        // Initialize parent fields with price-specific defaults
        this.page = page != null ? page : 0;
        this.size = size != null ? size : 20;
        this.sortBy = sortBy != null ? sortBy : "priority";
        this.sortDirection = sortDirection != null ? sortDirection : "DESC";
        this.excludeDeleted = excludeDeleted != null ? excludeDeleted : true;
        
        // Initialize price-specific fields
        this.productId = productId;
        this.priceType = priceType;
        this.priceTypes = priceTypes;
        this.currency = currency;
        this.activeOnly = activeOnly;
        this.currentlyValid = currentlyValid;
        this.scheduled = scheduled;
        this.expired = expired;
        this.minAmount = minAmount;
        this.maxAmount = maxAmount;
        this.customerGroup = customerGroup;
        this.market = market;
        this.hasDiscount = hasDiscount;
        this.validFromAfter = validFromAfter;
        this.validToBefore = validToBefore;
    }

    @Override
    public void normalize() {
        super.normalize(); // Call parent normalization
        
        // Normalize price type
        if (priceType != null) {
            priceType = priceType.trim().toUpperCase();
            if (priceType.isEmpty() || !isValidPriceType(priceType)) {
                priceType = null;
            }
        }
        
        // Normalize price types list
        if (priceTypes != null && !priceTypes.isEmpty()) {
            priceTypes = priceTypes.stream()
                .map(String::trim)
                .map(String::toUpperCase)
                .filter(this::isValidPriceType)
                .distinct()
                .collect(Collectors.toList());
            
            if (priceTypes.isEmpty()) {
                priceTypes = null;
            }
        }
        
        // Normalize currency (ISO 4217 format)
        if (currency != null) {
            currency = currency.trim().toUpperCase();
            if (currency.isEmpty() || currency.length() != 3) {
                currency = null;
            }
        }
        
        // Normalize customer group
        if (customerGroup != null) {
            customerGroup = customerGroup.trim().toUpperCase();
            if (customerGroup.isEmpty()) {
                customerGroup = null;
            }
        }
        
        // Normalize market
        if (market != null) {
            market = market.trim().toUpperCase();
            if (market.isEmpty()) {
                market = null;
            }
        }
        
        // Normalize sortBy to allowed values
        if (sortBy != null) {
            String normalizedSortBy = sortBy.trim().toLowerCase();
            if (!isValidSortField(normalizedSortBy)) {
                sortBy = "priority"; // Default fallback
            } else {
                sortBy = normalizedSortBy;
            }
        }
        
        // Validate price range
        if (minAmount != null && maxAmount != null && minAmount.compareTo(maxAmount) > 0) {
            BigDecimal temp = minAmount;
            minAmount = maxAmount;
            maxAmount = temp;
        }
        
        // Normalize negative amounts to null
        if (minAmount != null && minAmount.compareTo(BigDecimal.ZERO) < 0) {
            minAmount = BigDecimal.ZERO;
        }
        if (maxAmount != null && maxAmount.compareTo(BigDecimal.ZERO) < 0) {
            maxAmount = null;
        }
        
        // Validate date range
        if (validFromAfter != null && validToBefore != null && validFromAfter.isAfter(validToBefore)) {
            LocalDateTime temp = validFromAfter;
            validFromAfter = validToBefore;
            validToBefore = temp;
        }
        
        // Business logic: priceType takes precedence over priceTypes
        if (priceType != null && priceTypes != null) {
            priceTypes = null;
        }
        
        // Business logic: resolve conflicting validity filters
        resolveConflictingValidityFilters();
    }

    @Override
    public boolean hasFilters() {
        return productId != null 
            || priceType != null 
            || priceTypes != null
            || currency != null 
            || activeOnly != null 
            || currentlyValid != null
            || scheduled != null
            || expired != null
            || minAmount != null 
            || maxAmount != null 
            || customerGroup != null
            || market != null
            || hasDiscount != null
            || validFromAfter != null
            || validToBefore != null;
    }

    @Override
    protected int getMaxPageSize() {
        return 100; // Price-specific max page size
    }

    /**
     * Validate price type against allowed values
     */
    private boolean isValidPriceType(String type) {
        return type != null && (
            type.equals("BASE") ||
            type.equals("PROMOTIONAL") ||
            type.equals("TIERED") ||
            type.equals("WHOLESALE") ||
            type.equals("SPECIAL") ||
            type.equals("CLEARANCE")
        );
    }

    /**
     * Validate sort field against allowed values
     */
    private boolean isValidSortField(String field) {
        return field != null && (
            field.equals("priority") ||
            field.equals("amount") ||
            field.equals("validfrom") ||
            field.equals("validto") ||
            field.equals("createdat") ||
            field.equals("updatedat") ||
            field.equals("pricetype")
        );
    }

    /**
     * Resolve conflicting validity filters
     * Only one of currentlyValid, scheduled, expired should be true
     */
    private void resolveConflictingValidityFilters() {
        int trueCount = 0;
        if (Boolean.TRUE.equals(currentlyValid)) trueCount++;
        if (Boolean.TRUE.equals(scheduled)) trueCount++;
        if (Boolean.TRUE.equals(expired)) trueCount++;
        
        // If multiple are true, keep only the first one (priority order)
        if (trueCount > 1) {
            if (Boolean.TRUE.equals(currentlyValid)) {
                scheduled = null;
                expired = null;
            } else if (Boolean.TRUE.equals(scheduled)) {
                expired = null;
            }
        }
    }

    /**
     * Check if filtering by price type
     */
    public boolean isFilteringByPriceType() {
        return priceType != null || priceTypes != null;
    }

    /**
     * Check if filtering by validity period
     */
    public boolean isFilteringByValidity() {
        return currentlyValid != null 
            || scheduled != null 
            || expired != null
            || validFromAfter != null
            || validToBefore != null;
    }

    /**
     * Check if filtering by price range
     */
    public boolean isFilteringByPriceRange() {
        return minAmount != null || maxAmount != null;
    }

    /**
     * Check if filtering by market segmentation
     */
    public boolean isFilteringByMarket() {
        return customerGroup != null || market != null;
    }

    /**
     * Check if filtering for promotional prices
     */
    public boolean isPromotionalFilter() {
        return "PROMOTIONAL".equals(priceType) 
            || (priceTypes != null && priceTypes.contains("PROMOTIONAL"))
            || Boolean.TRUE.equals(hasDiscount);
    }

    /**
     * Check if filtering for active and valid prices
     */
    public boolean isActiveAndValidFilter() {
        return Boolean.TRUE.equals(activeOnly) && Boolean.TRUE.equals(currentlyValid);
    }

    /**
     * Get effective price types list
     */
    public List<String> getEffectivePriceTypes() {
        if (priceType != null) {
            return List.of(priceType);
        }
        return priceTypes;
    }

    /**
     * Get effective validity status
     */
    public String getEffectiveValidityStatus() {
        if (Boolean.TRUE.equals(currentlyValid)) {
            return "CURRENT";
        } else if (Boolean.TRUE.equals(scheduled)) {
            return "SCHEDULED";
        } else if (Boolean.TRUE.equals(expired)) {
            return "EXPIRED";
        }
        return null;
    }

    /**
     * Check if date range is valid
     */
    public boolean hasValidDateRange() {
        if (validFromAfter == null && validToBefore == null) {
            return true;
        }
        if (validFromAfter == null || validToBefore == null) {
            return true;
        }
        return !validFromAfter.isAfter(validToBefore);
    }

    /**
     * Get effective page size with price-specific default
     */
    @Override
    public int getEffectiveSize() {
        return size != null ? size : 20;
    }

    /**
     * Check if filtering for current product pricing
     */
    public boolean isCurrentProductPricingFilter() {
        return productId != null 
            && Boolean.TRUE.equals(activeOnly) 
            && Boolean.TRUE.equals(currentlyValid);
    }
}