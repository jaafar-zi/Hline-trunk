package com.example.demo.modules.product.application.dto.filter;

import com.example.demo.common.base.dto.BaseFilter;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Filter DTO for ProductInventory queries
 *
 * @author PIM Team
 * @version 1.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Product inventory filter criteria")
public class ProductInventoryFilter extends BaseFilter {

    private static final long serialVersionUID = 1L;

    @Schema(
        description = "Stock status",
        example = "IN_STOCK",
        allowableValues = {"IN_STOCK", "LOW_STOCK", "OUT_OF_STOCK", "BACKORDER"}
    )
    private String stockStatus;

    @Schema(
        description = "Multiple stock statuses",
        example = "[\"IN_STOCK\", \"LOW_STOCK\"]"
    )
    private List<String> stockStatuses;

    @Schema(
        description = "Track inventory only",
        example = "true"
    )
    private Boolean trackInventory;

    @Schema(
        description = "In stock only",
        example = "true"
    )
    private Boolean inStock;

    @Schema(
        description = "Low stock only",
        example = "false"
    )
    private Boolean lowStock;

    @Schema(
        description = "Out of stock only",
        example = "false"
    )
    private Boolean outOfStock;

    @Schema(
        description = "Needs reorder",
        example = "false"
    )
    private Boolean needsReorder;

    @Schema(
        description = "Minimum available quantity",
        example = "10"
    )
    @Min(value = 0, message = "Minimum quantity cannot be negative")
    private Integer minQuantity;

    @Schema(
        description = "Maximum available quantity",
        example = "100"
    )
    @Min(value = 0, message = "Maximum quantity cannot be negative")
    private Integer maxQuantity;

    @Schema(
        description = "Minimum reserved quantity",
        example = "5"
    )
    @Min(value = 0, message = "Minimum reserved cannot be negative")
    private Integer minReserved;

    @Schema(
        description = "Allow backorder",
        example = "false"
    )
    private Boolean allowBackorder;

    @Schema(
        description = "Warehouse location",
        example = "WAREHOUSE-A"
    )
    private String warehouseLocation;

    @Schema(
        description = "Restocked after date",
        example = "2024-01-01T00:00:00"
    )
    private LocalDateTime restockedAfter;

    @Schema(
        description = "Last stock check before date",
        example = "2024-12-31T23:59:59"
    )
    private LocalDateTime stockCheckBefore;

    // Constructor for builder pattern compatibility
    @Builder
    public ProductInventoryFilter(
            // BaseFilter fields
            Integer page,
            Integer size,
            String sortBy,
            String sortDirection,
            Boolean excludeDeleted,
            // ProductInventoryFilter specific fields
            String stockStatus,
            List<String> stockStatuses,
            Boolean trackInventory,
            Boolean inStock,
            Boolean lowStock,
            Boolean outOfStock,
            Boolean needsReorder,
            Integer minQuantity,
            Integer maxQuantity,
            Integer minReserved,
            Boolean allowBackorder,
            String warehouseLocation,
            LocalDateTime restockedAfter,
            LocalDateTime stockCheckBefore
    ) {
        // Initialize parent fields with inventory-specific defaults
        this.page = page != null ? page : 0;
        this.size = size != null ? size : 50;
        this.sortBy = sortBy != null ? sortBy : "stockLevel.available";
        this.sortDirection = sortDirection != null ? sortDirection : "ASC";
        this.excludeDeleted = excludeDeleted != null ? excludeDeleted : true;
        
        // Initialize inventory-specific fields
        this.stockStatus = stockStatus;
        this.stockStatuses = stockStatuses;
        this.trackInventory = trackInventory;
        this.inStock = inStock;
        this.lowStock = lowStock;
        this.outOfStock = outOfStock;
        this.needsReorder = needsReorder;
        this.minQuantity = minQuantity;
        this.maxQuantity = maxQuantity;
        this.minReserved = minReserved;
        this.allowBackorder = allowBackorder;
        this.warehouseLocation = warehouseLocation;
        this.restockedAfter = restockedAfter;
        this.stockCheckBefore = stockCheckBefore;
    }

    @Override
    public void normalize() {
        super.normalize(); // Call parent normalization
        
        // Normalize stock status
        if (stockStatus != null) {
            stockStatus = stockStatus.trim().toUpperCase();
            if (stockStatus.isEmpty() || !isValidStockStatus(stockStatus)) {
                stockStatus = null;
            }
        }
        
        // Normalize stock statuses list
        if (stockStatuses != null && !stockStatuses.isEmpty()) {
            stockStatuses = stockStatuses.stream()
                .map(String::trim)
                .map(String::toUpperCase)
                .filter(this::isValidStockStatus)
                .distinct()
                .collect(Collectors.toList());
            
            if (stockStatuses.isEmpty()) {
                stockStatuses = null;
            }
        }
        
        // Normalize warehouse location
        if (warehouseLocation != null) {
            warehouseLocation = warehouseLocation.trim().toUpperCase();
            if (warehouseLocation.isEmpty()) {
                warehouseLocation = null;
            }
        }
        
        // Normalize sortBy to allowed values
        if (sortBy != null) {
            String normalizedSortBy = sortBy.trim().toLowerCase();
            if (!isValidSortField(normalizedSortBy)) {
                sortBy = "stockLevel.available"; // Default fallback
            } else {
                sortBy = normalizedSortBy;
            }
        }
        
        // Validate quantity range
        if (minQuantity != null && maxQuantity != null && minQuantity > maxQuantity) {
            Integer temp = minQuantity;
            minQuantity = maxQuantity;
            maxQuantity = temp;
        }
        
        // Validate date range
        if (restockedAfter != null && stockCheckBefore != null && restockedAfter.isAfter(stockCheckBefore)) {
            LocalDateTime temp = restockedAfter;
            restockedAfter = stockCheckBefore;
            stockCheckBefore = temp;
        }
        
        // Business logic: stockStatus takes precedence over stockStatuses
        if (stockStatus != null && stockStatuses != null) {
            stockStatuses = null;
        }
        
        // Business logic: mutually exclusive stock filters
        resolveConflictingStockFilters();
    }

    @Override
    public boolean hasFilters() {
        return stockStatus != null 
            || stockStatuses != null 
            || trackInventory != null 
            || inStock != null
            || lowStock != null 
            || outOfStock != null
            || needsReorder != null 
            || minQuantity != null 
            || maxQuantity != null
            || minReserved != null
            || allowBackorder != null
            || warehouseLocation != null
            || restockedAfter != null
            || stockCheckBefore != null;
    }

    @Override
    protected int getMaxPageSize() {
        return 100; // Inventory-specific max page size
    }

    /**
     * Validate stock status against allowed values
     */
    private boolean isValidStockStatus(String status) {
        return status != null && (
            status.equals("IN_STOCK") ||
            status.equals("LOW_STOCK") ||
            status.equals("OUT_OF_STOCK") ||
            status.equals("BACKORDER")
        );
    }

    /**
     * Validate sort field against allowed values
     */
    private boolean isValidSortField(String field) {
        return field != null && (
            field.equals("stocklevel.available") ||
            field.equals("stocklevel.quantity") ||
            field.equals("stocklevel.reserved") ||
            field.equals("lastrestockdate") ||
            field.equals("laststockcheck") ||
            field.equals("createdat") ||
            field.equals("updatedat")
        );
    }

    /**
     * Resolve conflicting stock filter flags
     * Only one of inStock, lowStock, outOfStock should be true
     */
    private void resolveConflictingStockFilters() {
        int trueCount = 0;
        if (Boolean.TRUE.equals(inStock)) trueCount++;
        if (Boolean.TRUE.equals(lowStock)) trueCount++;
        if (Boolean.TRUE.equals(outOfStock)) trueCount++;
        
        // If multiple are true, keep only the first one (priority order)
        if (trueCount > 1) {
            if (Boolean.TRUE.equals(inStock)) {
                lowStock = null;
                outOfStock = null;
            } else if (Boolean.TRUE.equals(lowStock)) {
                outOfStock = null;
            }
        }
    }

    /**
     * Check if filtering by stock status
     */
    public boolean isFilteringByStockStatus() {
        return stockStatus != null 
            || stockStatuses != null 
            || inStock != null 
            || lowStock != null 
            || outOfStock != null;
    }

    /**
     * Check if filtering by quantity
     */
    public boolean isFilteringByQuantity() {
        return minQuantity != null || maxQuantity != null || minReserved != null;
    }

    /**
     * Check if filtering by warehouse
     */
    public boolean isFilteringByWarehouse() {
        return warehouseLocation != null;
    }

    /**
     * Check if filtering by date range
     */
    public boolean hasDateRangeFilter() {
        return restockedAfter != null || stockCheckBefore != null;
    }

    /**
     * Check if filtering critical stock levels
     */
    public boolean isCriticalStockFilter() {
        return Boolean.TRUE.equals(lowStock) 
            || Boolean.TRUE.equals(outOfStock) 
            || Boolean.TRUE.equals(needsReorder);
    }

    /**
     * Get effective stock status list
     */
    public List<String> getEffectiveStockStatuses() {
        if (stockStatus != null) {
            return List.of(stockStatus);
        }
        return stockStatuses;
    }

    /**
     * Get effective page size with inventory-specific default
     */
    @Override
    public int getEffectiveSize() {
        return size != null ? size : 50;
    }
}