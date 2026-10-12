package com.example.demo.modules.product.application.query;

import com.example.demo.common.annotation.ReadOnly;
import com.example.demo.common.base.service.query.AbstractQueryService;
import com.example.demo.common.normalization.NormalizationService;
import com.example.demo.modules.product.application.dto.filter.ProductInventoryFilter;
import com.example.demo.modules.product.application.dto.response.ProductInventoryResponse;
import com.example.demo.modules.product.application.mapper.ProductInventoryMapper;
import com.example.demo.modules.product.application.mapper.ProductMapper;
import com.example.demo.modules.product.domain.model.ProductInventory;
import com.example.demo.modules.product.domain.repository.ProductInventoryRepository;
import com.example.demo.modules.product.domain.specification.ProductInventorySpecification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Product inventory query service
 * 
 * Handles all read operations for product inventory
 * 
 * @author PIM Team
 * @version 3.0
 */
@Slf4j
@Service
@Transactional(readOnly = true)
public class ProductInventoryQueryService extends AbstractQueryService<ProductInventory, UUID> {

    private static final Set<String> VALID_SORT_FIELDS = Set.of(
        "stockLevel.quantity", "stockLevel.available", "stockLevel.reserved",
        "lastRestockDate", "lastStockCheck"
    );

    private final ProductInventoryRepository inventoryRepository;
    private final ProductInventoryMapper inventoryMapper;

    public ProductInventoryQueryService(
            ProductInventoryRepository inventoryRepository,
            NormalizationService normalizationService,
            ProductInventoryMapper inventoryMapper) {
        super(inventoryRepository, normalizationService);
        this.inventoryRepository = inventoryRepository;
        this.inventoryMapper = inventoryMapper;
    }

    @Override
    protected String getEntityCacheName() {
        return "product-inventories";
    }

    @Override
    protected Class<ProductInventory> getEntityClass() {
        return ProductInventory.class;
    }

    @Override
    protected boolean isValidSortField(String field) {
        return VALID_SORT_FIELDS.contains(field);
    }

    @Override
    protected String getDefaultSortField() {
        return "stockLevel.available";
    }

    // ====================================
    // Filter-Based Queries
    // ====================================

    /**
     * Search inventories with filter
     */
    @ReadOnly
    public Page<ProductInventoryResponse> searchInventories(ProductInventoryFilter filter) {
        log.debug("Searching product inventories with filter");

        Specification<ProductInventory> spec = ProductInventorySpecification.fromFilter(filter);
        Page<ProductInventory> inventories = findWithFilter(filter, spec);

        return inventories.map(inventoryMapper::toResponse);
    }

    /**
     * Find all inventories with filter
     */
    @ReadOnly
    public List<ProductInventoryResponse> findAllInventories(ProductInventoryFilter filter) {
        log.debug("Finding all product inventories with filter");

        Specification<ProductInventory> spec = ProductInventorySpecification.fromFilter(filter);
        List<ProductInventory> inventories = findAllWithFilter(filter, spec);

        return inventories.stream()
                .map(inventoryMapper::toResponse)
                .collect(Collectors.toList());
    }

    // ====================================
    // Specific Queries
    // ====================================

    /**
     * Get inventory by ID
     */
    @ReadOnly
    public ProductInventoryResponse getInventoryById(UUID id) {
        log.debug("Getting inventory by ID: {}", id);

        ProductInventory inventory = findById(id);
        return inventoryMapper.toResponse(inventory);
    }

    /**
     * Get inventory by product ID
     */
    @ReadOnly
    public ProductInventoryResponse getInventoryByProductId(UUID productId) {
        log.debug("Getting inventory for product: {}", productId);

        ProductInventory inventory = inventoryRepository.findByProductId(productId)
                .orElse(null);

        return inventory != null ? inventoryMapper.toResponse(inventory) : null;
    }

    /**
     * Get low stock inventories
     */
    @ReadOnly
    public List<ProductInventoryResponse> getLowStockInventories() {
        log.debug("Getting low stock inventories");

        ProductInventoryFilter filter = ProductInventoryFilter.builder()
                .lowStock(true)
                .build();

        return findAllInventories(filter);
    }

    /**
     * Get out of stock inventories
     */
    @ReadOnly
    public List<ProductInventoryResponse> getOutOfStockInventories() {
        log.debug("Getting out of stock inventories");

        ProductInventoryFilter filter = ProductInventoryFilter.builder()
                .outOfStock(true)
                .build();

        return findAllInventories(filter);
    }

    /**
     * Get inventories needing reorder
     */
    @ReadOnly
    public List<ProductInventoryResponse> getInventoriesNeedingReorder() {
        log.debug("Getting inventories needing reorder");

        ProductInventoryFilter filter = ProductInventoryFilter.builder()
                .needsReorder(true)
                .build();

        return findAllInventories(filter);
    }

    /**
     * Get inventories by stock status
     */
    @ReadOnly
    public List<ProductInventoryResponse> getInventoriesByStockStatus(String stockStatus) {
        log.debug("Getting inventories with stock status: {}", stockStatus);

        ProductInventoryFilter filter = ProductInventoryFilter.builder()
                .stockStatus(stockStatus)
                .build();

        return findAllInventories(filter);
    }

    /**
     * Get inventories by warehouse
     */
    @ReadOnly
    public List<ProductInventoryResponse> getInventoriesByWarehouse(String warehouseLocation) {
        log.debug("Getting inventories for warehouse: {}", warehouseLocation);

        ProductInventoryFilter filter = ProductInventoryFilter.builder()
                .warehouseLocation(warehouseLocation)
                .build();

        return findAllInventories(filter);
    }

    /**
     * Get inventory statistics
     */
    @ReadOnly
    public InventoryStatistics getStatistics() {
        log.debug("Getting inventory statistics");

        long totalInventories = count();
        
        long inStock = countWithFilter(ProductInventoryFilter.builder()
                .inStock(true)
                .build());
        
        long outOfStock = countWithFilter(ProductInventoryFilter.builder()
                .outOfStock(true)
                .build());
        
        long lowStock = countWithFilter(ProductInventoryFilter.builder()
                .lowStock(true)
                .build());
        
        long needsReorder = countWithFilter(ProductInventoryFilter.builder()
                .needsReorder(true)
                .build());

        return InventoryStatistics.builder()
                .totalInventories(totalInventories)
                .inStock(inStock)
                .outOfStock(outOfStock)
                .lowStock(lowStock)
                .needsReorder(needsReorder)
                .build();
    }

    private long countWithFilter(ProductInventoryFilter filter) {
        Specification<ProductInventory> spec = ProductInventorySpecification.fromFilter(filter);
        return countWithFilter(filter, spec);
    }

    // ====================================
    // Statistics DTO
    // ====================================

    @lombok.Data
    @lombok.Builder
    public static class InventoryStatistics {
        private long totalInventories;
        private long inStock;
        private long outOfStock;
        private long lowStock;
        private long needsReorder;
    }
}