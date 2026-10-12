package com.example.demo.modules.product.domain.repository;

import com.example.demo.common.base.repository.BaseRepository;
import com.example.demo.modules.product.domain.model.ProductInventory;
import com.example.demo.modules.product.domain.model.vo.StockStatus;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Product Inventory Repository
 *
 * Repository for ProductInventory entity
 * Optimized for inventory management operations
 *
 * @author PIM Team
 * @version 1.0
 */
@Repository
public interface ProductInventoryRepository extends BaseRepository<ProductInventory, UUID> {

    // ====================================
    // Find by Product/SKU
    // ====================================

    /**
     * Find inventory by product ID
     */
    @Query("SELECT i FROM ProductInventory i WHERE i.product.id = :productId")
    Optional<ProductInventory> findByProductId(@Param("productId") UUID productId);

    /**
     * Find inventory by SKU
     */
    @Query("SELECT i FROM ProductInventory i WHERE i.sku = :sku")
    Optional<ProductInventory> findBySku(@Param("sku") String sku);

    // ====================================
    // Find by Stock Status
    // ====================================

    /**
     * Find all low stock products
     */
    @Query("SELECT i FROM ProductInventory i WHERE i.stockStatus = 'LOW_STOCK'")
    List<ProductInventory> findLowStock();

    /**
     * Find all out of stock products
     */
    @Query("SELECT i FROM ProductInventory i WHERE i.stockStatus = 'OUT_OF_STOCK'")
    List<ProductInventory> findOutOfStock();

    /**
     * Find by stock status
     */
    @Query("SELECT i FROM ProductInventory i WHERE i.stockStatus = :status")
    List<ProductInventory> findByStockStatus(@Param("status") StockStatus status);

    /**
     * Find products needing reorder
     */
    @Query("SELECT i FROM ProductInventory i WHERE i.trackInventory = true " +
           "AND i.stockLevel.available <= i.reorderPoint AND i.reorderPoint IS NOT NULL")
    List<ProductInventory> findNeedingReorder();

    // ====================================
    // Find by Availability
    // ====================================

    /**
     * Find in-stock products
     */
    @Query("SELECT i FROM ProductInventory i WHERE i.stockLevel.available > 0")
    List<ProductInventory> findInStock();

    /**
     * Find products with specific quantity or more
     */
    @Query("SELECT i FROM ProductInventory i WHERE i.stockLevel.available >= :minQuantity")
    List<ProductInventory> findWithMinimumQuantity(@Param("minQuantity") int minQuantity);

    // ====================================
    // Statistics
    // ====================================

    /**
     * Count by stock status
     */
    @Query("SELECT COUNT(i) FROM ProductInventory i WHERE i.stockStatus = :status")
    long countByStockStatus(@Param("status") StockStatus status);

    /**
     * Count low stock
     */
    @Query("SELECT COUNT(i) FROM ProductInventory i WHERE i.stockStatus = 'LOW_STOCK'")
    long countLowStock();

    /**
     * Count out of stock
     */
    @Query("SELECT COUNT(i) FROM ProductInventory i WHERE i.stockStatus = 'OUT_OF_STOCK'")
    long countOutOfStock();

    /**
     * Count products needing reorder
     */
    @Query("SELECT COUNT(i) FROM ProductInventory i WHERE i.trackInventory = true " +
           "AND i.stockLevel.available <= i.reorderPoint AND i.reorderPoint IS NOT NULL")
    long countNeedingReorder();

    /**
     * Get total available quantity
     */
    @Query("SELECT SUM(i.stockLevel.available) FROM ProductInventory i WHERE i.trackInventory = true")
    Long getTotalAvailableQuantity();

    // ====================================
    // Find by Dates
    // ====================================

    /**
     * Find products restocked recently
     */
    @Query("SELECT i FROM ProductInventory i WHERE i.lastRestockDate >= :since")
    List<ProductInventory> findRestockedSince(@Param("since") LocalDateTime since);

    /**
     * Find products with scheduled restock
     */
    @Query("SELECT i FROM ProductInventory i WHERE i.nextRestockDate IS NOT NULL " +
           "AND i.nextRestockDate BETWEEN :start AND :end")
    List<ProductInventory> findScheduledForRestock(
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end
    );
}