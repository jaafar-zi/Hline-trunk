package com.example.demo.modules.order.domain.repository;

import com.example.demo.common.base.repository.BaseRepository;
import com.example.demo.modules.order.domain.model.OrderItem;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Order Item Repository
 * 
 * Provides data access operations for OrderItem entity
 * Note: OrderItem doesn't use soft delete (uses BaseRepository instead)
 * 
 * @author Order Team
 * @version 1.0
 */
@Repository
public interface OrderItemRepository extends BaseRepository<OrderItem, UUID>, JpaSpecificationExecutor<OrderItem> {

    // ====================================
    // Basic Finders
    // ====================================

    /**
     * Find all items for an order
     */
    List<OrderItem> findByOrderId(UUID orderId);

    /**
     * Find items by product ID
     */
    List<OrderItem> findByProductId(UUID productId);

    /**
     * Find items by product SKU
     */
    List<OrderItem> findByProductSku(String productSku);

    /**
     * Find items by product variant ID
     */
    List<OrderItem> findByProductVariantId(UUID productVariantId);

    // ====================================
    // Count Queries
    // ====================================

    /**
     * Count items in an order
     */
    long countByOrderId(UUID orderId);

    /**
     * Count orders containing a product
     */
    long countByProductId(UUID productId);

    /**
     * Count orders containing a product variant
     */
    long countByProductVariantId(UUID productVariantId);

    // ====================================
    // Aggregation Queries
    // ====================================

    /**
     * Get total quantity sold for a product
     */
    @Query("SELECT SUM(oi.quantity) FROM OrderItem oi WHERE oi.productId = :productId")
    Long getTotalQuantitySoldForProduct(@Param("productId") UUID productId);

    /**
     * Get total revenue for a product
     */
    @Query("SELECT SUM(oi.total.amount) FROM OrderItem oi WHERE oi.productId = :productId")
    Double getTotalRevenueForProduct(@Param("productId") UUID productId);

    /**
     * Get best selling products
     */
    @Query("SELECT oi.productId, oi.productName, SUM(oi.quantity) as totalSold " +
           "FROM OrderItem oi " +
           "GROUP BY oi.productId, oi.productName " +
           "ORDER BY totalSold DESC")
    List<Object[]> getBestSellingProducts();

    /**
     * Get top products by revenue
     */
    @Query("SELECT oi.productId, oi.productName, SUM(oi.total.amount) as totalRevenue " +
           "FROM OrderItem oi " +
           "GROUP BY oi.productId, oi.productName " +
           "ORDER BY totalRevenue DESC")
    List<Object[]> getTopProductsByRevenue();

    // ====================================
    // Search Queries
    // ====================================

    /**
     * Search items by product name
     */
    List<OrderItem> findByProductNameContainingIgnoreCase(String productName);

    /**
     * Find items in order by product name
     */
    @Query("SELECT oi FROM OrderItem oi WHERE oi.order.id = :orderId AND LOWER(oi.productName) LIKE LOWER(CONCAT('%', :productName, '%'))")
    List<OrderItem> findByOrderIdAndProductNameContaining(@Param("orderId") UUID orderId, @Param("productName") String productName);

    // ====================================
    // Bulk Operations
    // ====================================

    /**
     * Delete all items for an order
     */
    void deleteByOrderId(UUID orderId);

    /**
     * Find items by multiple product IDs
     */
    @Query("SELECT oi FROM OrderItem oi WHERE oi.productId IN :productIds")
    List<OrderItem> findByProductIds(@Param("productIds") List<UUID> productIds);
}