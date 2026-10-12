package com.example.demo.modules.product.application.mapper;

import com.example.demo.common.mapper.BaseMapper;
import com.example.demo.modules.product.application.dto.request.UpdateInventoryRequest;
import com.example.demo.modules.product.application.dto.response.ProductInventoryResponse;
import com.example.demo.modules.product.domain.model.ProductInventory;
import org.mapstruct.*;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Lean MapStruct mapper for ProductInventory entity <-> DTOs
 * 
 * Performance optimized
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
@Component
public abstract class ProductInventoryMapper implements BaseMapper<ProductInventory, ProductInventoryResponse> {

    // ====================================
    // Entity to Response - LEAN
    // ====================================

    /**
     * Map ProductInventory entity to ProductInventoryResponse (LEAN)
     */
    @Override
    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "variantId", ignore = true) // Not in entity yet
    @Mapping(target = "quantity", source = "stockLevel.quantity")
    @Mapping(target = "available", source = "stockLevel.available")
    @Mapping(target = "reserved", source = "stockLevel.reserved")
    @Mapping(target = "committed", source = "stockLevel.quantity")
    @Mapping(target = "stockStatus", expression = "java(mapStockStatus(inventory))")
    @Mapping(target = "trackInventory", source = "trackInventory")
    @Mapping(target = "allowBackorder", source = "allowBackorder")
    @Mapping(target = "lowStockThreshold", source = "lowStockThreshold")
    @Mapping(target = "reorderPoint", source = "reorderPoint")
    @Mapping(target = "warehouseLocation", source = "warehouseLocation")
    @Mapping(target = "lastRestockDate", source = "lastRestockDate")
    @Mapping(target = "nextRestockDate", source = "nextRestockDate")
    public abstract ProductInventoryResponse toDto(ProductInventory inventory);

    /**
     * Alias for better readability
     */
    public ProductInventoryResponse toResponse(ProductInventory inventory) {
        return inventory != null ? toDto(inventory) : null;
    }

    /**
     * Map list to responses
     */
    @Override
    public List<ProductInventoryResponse> toDtoList(List<ProductInventory> inventories) {
        if (inventories == null || inventories.isEmpty()) {
            return Collections.emptyList();
        }
        return inventories.stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    /**
     * Not supported - inventory created via domain factory
     */
    @Override
    public ProductInventory toEntity(ProductInventoryResponse dto) {
        throw new UnsupportedOperationException(
            "Cannot create inventory entity from response DTO. Use domain factory methods."
        );
    }

    // ====================================
    // Request to Entity - UPDATE
    // ====================================

    /**
     * Update existing ProductInventory from UpdateInventoryRequest
     * 
     * Note: This handles both stock quantity updates AND threshold settings
     */
    @BeanMapping(
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS
    )
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "sku", ignore = true) // Immutable
    @Mapping(target = "stockLevel", ignore = true) // Updated via service layer using domain method
    @Mapping(target = "stockStatus", ignore = true) // Calculated automatically by entity
    @Mapping(target = "trackInventory", ignore = true) // Not in request
    @Mapping(target = "allowBackorder", ignore = true) // Not in request
    @Mapping(target = "backorderLimit", ignore = true) // Not in request
    @Mapping(target = "lowStockThreshold", source = "lowStockThreshold")
    @Mapping(target = "reorderPoint", source = "reorderPoint")
    @Mapping(target = "reorderQuantity", source = "reorderQuantity")
    @Mapping(target = "warehouseLocation", ignore = true) // Not in request
    @Mapping(target = "binLocation", ignore = true) // Not in request
    @Mapping(target = "supplierSku", ignore = true) // Not in request
    @Mapping(target = "nextRestockDate", ignore = true) // Not in request
    @Mapping(target = "lastStockCheck", ignore = true) // Set by system
    @Mapping(target = "lastRestockDate", ignore = true) // Set by system
    public abstract void updateEntityFromRequest(
        UpdateInventoryRequest request,
        @MappingTarget ProductInventory entity
    );

    // ====================================
    // Helper Methods
    // ====================================

    /**
     * Map stock status enum to string
     */
    protected String mapStockStatus(ProductInventory inventory) {
        if (inventory == null || inventory.getStockStatus() == null) {
            return null;
        }
        return inventory.getStockStatus().name();
    }

    // ====================================
    // Specialized Mappings
    // ====================================

    /**
     * Map to minimal summary (for product lists)
     */
    public InventorySummary toSummary(ProductInventory inventory) {
        if (inventory == null) {
            return null;
        }

        return InventorySummary.builder()
            .stockStatus(mapStockStatus(inventory))
            .available(inventory.getStockLevel() != null ? 
                inventory.getStockLevel().getAvailable() : 0)
            .inStock(inventory.isInStock())
            .lowStock(inventory.isLowStock())
            .build();
    }

    /**
     * Map list to summaries
     */
    public List<InventorySummary> toSummaryList(List<ProductInventory> inventories) {
        if (inventories == null || inventories.isEmpty()) {
            return Collections.emptyList();
        }
        return inventories.stream()
            .map(this::toSummary)
            .collect(Collectors.toList());
    }

    /**
     * Filter low stock inventories
     */
    public List<ProductInventoryResponse> toLowStockList(List<ProductInventory> inventories) {
        if (inventories == null || inventories.isEmpty()) {
            return Collections.emptyList();
        }

        return inventories.stream()
            .filter(ProductInventory::isLowStock)
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    /**
     * Filter items needing reorder
     */
    public List<ProductInventoryResponse> toReorderList(List<ProductInventory> inventories) {
        if (inventories == null || inventories.isEmpty()) {
            return Collections.emptyList();
        }

        return inventories.stream()
            .filter(ProductInventory::needsReorder)
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    // ====================================
    // Nested DTO
    // ====================================

    /**
     * Ultra-lightweight inventory summary for product lists
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class InventorySummary {
        private String stockStatus;
        private Integer available;
        private Boolean inStock;
        private Boolean lowStock;
    }
}