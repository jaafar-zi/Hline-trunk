package com.example.demo.modules.product.domain.repository;

import com.example.demo.common.base.repository.SoftDeleteRepository;
import com.example.demo.modules.product.domain.model.Product;
import com.example.demo.modules.product.domain.model.vo.ProductStatus;
import com.example.demo.modules.product.domain.model.vo.SKU;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Product Repository
 *
 * Repository for Product aggregate root
 * Contains custom query methods for product-specific operations
 *
 * Performance Considerations:
 * - Uses EntityGraph for eager loading when needed
 * - Separate methods for different fetch strategies
 * - Optimized queries for common use cases
 *
 * @author PIM Team
 * @version 1.0
 */
@Repository
public interface ProductRepository extends SoftDeleteRepository<Product, UUID> {

    // ====================================
    // Find by SKU
    // ====================================

    /**
     * Find product by SKU
     * Most common query - used for product lookup
     */
    @Query("SELECT p FROM Product p WHERE p.sku = :sku")
    Optional<Product> findBySku(@Param("sku") SKU sku);

    /**
     * Find product by SKU value
     */
    @Query("SELECT p FROM Product p WHERE p.sku.value = :skuValue")
    Optional<Product> findBySkuValue(@Param("skuValue") String skuValue);

    /**
     * Find product by SKU with all relationships
     */
    @EntityGraph(attributePaths = {
        "category",
        "attributes",
        "images",
        "prices",
        "inventory",
        "variants"
    })
    @Query("SELECT p FROM Product p WHERE p.sku.value = :skuValue")
    Optional<Product> findBySkuWithRelations(@Param("skuValue") String skuValue);

    /**
     * Check if SKU exists
     */
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Product p WHERE p.sku.value = :skuValue")
    boolean existsBySku(@Param("skuValue") String skuValue);

    /**
     * Check if SKU exists excluding specific product
     */
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Product p " +
           "WHERE p.sku.value = :skuValue AND p.id <> :productId")
    boolean existsBySkuAndIdNot(@Param("skuValue") String skuValue, @Param("productId") UUID productId);

     /**
     * Check if category exists
     */
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Product p WHERE p.category.id = :categoryId AND p.deleted = false")
    boolean existsByCategoryId(@Param("categoryId") UUID id);

    // ====================================
    // Find by Status
    // ====================================

    /**
     * Find all published products
     */
    @Query("SELECT p FROM Product p WHERE p.status = 'PUBLISHED' AND p.deleted = false")
    List<Product> findAllPublished();

    /**
     * Find all published products with pagination
     */
    @Query("SELECT p FROM Product p WHERE p.status = 'PUBLISHED' AND p.deleted = false")
    Page<Product> findAllPublished(Pageable pageable);

    /**
     * Find products by status
     */
    @Query("SELECT p FROM Product p WHERE p.status = :status AND p.deleted = false")
    List<Product> findByStatus(@Param("status") ProductStatus status);

    /**
     * Find products by status with pagination
     */
    @Query("SELECT p FROM Product p WHERE p.status = :status AND p.deleted = false")
    Page<Product> findByStatus(@Param("status") ProductStatus status, Pageable pageable);

    /**
     * Find products by multiple statuses
     */
    @Query("SELECT p FROM Product p WHERE p.status IN :statuses AND p.deleted = false")
    List<Product> findByStatusIn(@Param("statuses") List<ProductStatus> statuses);

    /**
     * Find all draft products
     */
    @Query("SELECT p FROM Product p WHERE p.status = 'DRAFT' AND p.deleted = false")
    List<Product> findAllDrafts();

    /**
     * Find all ready products (ready to be published)
     */
    @Query("SELECT p FROM Product p WHERE p.status = 'READY' AND p.deleted = false")
    List<Product> findAllReady();

    /**
     * Find all discontinued products
     */
    @Query("SELECT p FROM Product p WHERE p.status = 'DISCONTINUED' AND p.deleted = false")
    List<Product> findAllDiscontinued();

    // ====================================
    // Find by Category
    // ====================================

    /**
     * Find products by category ID
     */
    @Query("SELECT p FROM Product p WHERE p.category.id = :categoryId AND p.deleted = false")
    List<Product> findByCategoryId(@Param("categoryId") UUID categoryId);

    /**
     * Find products by category ID with pagination
     */
    @Query("SELECT p FROM Product p WHERE p.category.id = :categoryId AND p.deleted = false")
    Page<Product> findByCategoryId(@Param("categoryId") UUID categoryId, Pageable pageable);

    /**
     * Find published products by category
     */
    @Query("SELECT p FROM Product p WHERE p.category.id = :categoryId " +
           "AND p.status = 'PUBLISHED' AND p.deleted = false")
    Page<Product> findPublishedByCategoryId(@Param("categoryId") UUID categoryId, Pageable pageable);

    /**
     * Find products by category IDs
     */
    @Query("SELECT p FROM Product p WHERE p.category.id IN :categoryIds AND p.deleted = false")
    List<Product> findByCategoryIdIn(@Param("categoryIds") List<UUID> categoryIds);

    /**
     * Count products by category
     */
    @Query("SELECT COUNT(p) FROM Product p WHERE p.category.id = :categoryId AND p.deleted = false")
    long countByCategoryId(@Param("categoryId") UUID categoryId);

    // ====================================
    // Find by Brand/Manufacturer
    // ====================================

    /**
     * Find products by brand
     */
    @Query("SELECT p FROM Product p WHERE LOWER(p.brand) = LOWER(:brand) AND p.deleted = false")
    List<Product> findByBrand(@Param("brand") String brand);

    /**
     * Find products by brand with pagination
     */
    @Query("SELECT p FROM Product p WHERE LOWER(p.brand) = LOWER(:brand) AND p.deleted = false")
    Page<Product> findByBrand(@Param("brand") String brand, Pageable pageable);

    /**
     * Find products by manufacturer
     */
    @Query("SELECT p FROM Product p WHERE LOWER(p.manufacturer) = LOWER(:manufacturer) AND p.deleted = false")
    List<Product> findByManufacturer(@Param("manufacturer") String manufacturer);

    /**
     * Find distinct brands
     */
    @Query("SELECT DISTINCT p.brand FROM Product p WHERE p.brand IS NOT NULL AND p.deleted = false ORDER BY p.brand")
    List<String> findAllBrands();

    /**
     * Find distinct manufacturers
     */
    @Query("SELECT DISTINCT p.manufacturer FROM Product p WHERE p.manufacturer IS NOT NULL " +
           "AND p.deleted = false ORDER BY p.manufacturer")
    List<String> findAllManufacturers();

    // ====================================
    // Search
    // ====================================

    /**
     * Search products by name, SKU, or description
     */
    @Query("SELECT p FROM Product p WHERE p.deleted = false AND (" +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.sku.value) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.shortDescription) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.searchKeywords) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Product> searchProducts(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Search published products only
     */
    @Query("SELECT p FROM Product p WHERE p.status = 'PUBLISHED' AND p.deleted = false AND (" +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.sku.value) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.shortDescription) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.searchKeywords) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Product> searchPublishedProducts(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Search products by category and search term
     */
    @Query("SELECT p FROM Product p WHERE p.category.id = :categoryId AND p.deleted = false AND (" +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.sku.value) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Product> searchProductsByCategory(
        @Param("categoryId") UUID categoryId,
        @Param("searchTerm") String searchTerm,
        Pageable pageable
    );

    // ====================================
    // Find by Date Range
    // ====================================

    /**
     * Find products created between dates
     */
    @Query("SELECT p FROM Product p WHERE p.deleted = false " +
           "AND p.createdAt BETWEEN :startDate AND :endDate")
    List<Product> findByCreatedAtBetween(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    /**
     * Find products published between dates
     */
    @Query("SELECT p FROM Product p WHERE p.deleted = false " +
           "AND p.publishedAt BETWEEN :startDate AND :endDate")
    List<Product> findByPublishedAtBetween(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    /**
     * Find products discontinued between dates
     */
    @Query("SELECT p FROM Product p WHERE p.deleted = false " +
           "AND p.discontinuedAt BETWEEN :startDate AND :endDate")
    List<Product> findByDiscontinuedAtBetween(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    // ====================================
    // Find by Availability
    // ====================================

    /**
     * Find currently available products
     */
    @Query("SELECT p FROM Product p WHERE p.status = 'PUBLISHED' AND p.deleted = false " +
           "AND (p.availableFrom IS NULL OR p.availableFrom <= :now) " +
           "AND (p.availableUntil IS NULL OR p.availableUntil >= :now)")
    Page<Product> findCurrentlyAvailable(@Param("now") LocalDateTime now, Pageable pageable);

    /**
     * Find products scheduled for future availability
     */
    @Query("SELECT p FROM Product p WHERE p.deleted = false " +
           "AND p.availableFrom > :now")
    List<Product> findScheduledProducts(@Param("now") LocalDateTime now);

    /**
     * Find products with expired availability
     */
    @Query("SELECT p FROM Product p WHERE p.deleted = false " +
           "AND p.availableUntil < :now AND p.status = 'PUBLISHED'")
    List<Product> findExpiredProducts(@Param("now") LocalDateTime now);

    // ====================================
    // Find with Inventory
    // ====================================

    /**
     * Find products with low stock
     */
    @Query("SELECT p FROM Product p JOIN p.inventory i " +
           "WHERE p.deleted = false AND i.stockStatus = 'LOW_STOCK'")
    List<Product> findLowStockProducts();

    /**
     * Find products out of stock
     */
    @Query("SELECT p FROM Product p JOIN p.inventory i " +
           "WHERE p.deleted = false AND i.stockStatus = 'OUT_OF_STOCK'")
    List<Product> findOutOfStockProducts();

    /**
     * Find products in stock
     */
    @Query("SELECT p FROM Product p JOIN p.inventory i " +
           "WHERE p.deleted = false AND i.stockStatus = 'IN_STOCK'")
    Page<Product> findInStockProducts(Pageable pageable);

    /**
     * Find products needing reorder
     */
    @Query("SELECT p FROM Product p JOIN p.inventory i " +
           "WHERE p.deleted = false AND i.stockLevel.available <= i.reorderPoint")
    List<Product> findProductsNeedingReorder();

    // ====================================
    // Statistics
    // ====================================

    /**
     * Count products by status
     */
    @Query("SELECT COUNT(p) FROM Product p WHERE p.status = :status AND p.deleted = false")
    long countByStatus(@Param("status") ProductStatus status);

    /**
     * Count published products
     */
    @Query("SELECT COUNT(p) FROM Product p WHERE p.status = 'PUBLISHED' AND p.deleted = false")
    long countPublished();

    /**
     * Count products created today
     */   
    @Query("SELECT COUNT(p) FROM Product p WHERE FUNCTION('DATE', p.createdAt) = CURRENT_DATE AND p.deleted = false")
    long countCreatedToday();

    /**
     * Count products published today
     */
    @Query("SELECT COUNT(p) FROM Product p WHERE FUNCTION('DATE', p.publishedAt) = CURRENT_DATE AND p.deleted = false")
    long countPublishedToday();

    /**
     * Count products by brand
     */
    @Query("SELECT COUNT(p) FROM Product p WHERE LOWER(p.brand) = LOWER(:brand) AND p.deleted = false")
    long countByBrand(@Param("brand") String brand);

    /**
     * Count products with variants
     */
    @Query("SELECT COUNT(p) FROM Product p WHERE SIZE(p.variants) > 0 AND p.deleted = false")
    long countWithVariants();

    /**
     * Count products without images
     */
    @Query("SELECT COUNT(p) FROM Product p WHERE SIZE(p.images) = 0 AND p.deleted = false")
    long countWithoutImages();

    // ====================================
    // Eager Loading Queries
    // ====================================

    /**
     * Find product with all relationships
     */
    @EntityGraph(attributePaths = {
        "category",
        "attributes",
        "images",
        "prices",
        "inventory",
        "variants"
    })
    @Query("SELECT p FROM Product p WHERE p.id = :id")
    Optional<Product> findByIdWithAll(@Param("id") UUID id);

    /**
     * Find product with pricing
     */
    @EntityGraph(attributePaths = {"prices", "category"})
    @Query("SELECT p FROM Product p WHERE p.id = :id")
    Optional<Product> findByIdWithPricing(@Param("id") UUID id);

    /**
     * Find product with inventory
     */
    @EntityGraph(attributePaths = {"inventory", "category"})
    @Query("SELECT p FROM Product p WHERE p.id = :id")
    Optional<Product> findByIdWithInventory(@Param("id") UUID id);

    /**
     * Find product with variants
     */
    @EntityGraph(attributePaths = {"variants", "variants.inventory", "category"})
    @Query("SELECT p FROM Product p WHERE p.id = :id")
    Optional<Product> findByIdWithVariants(@Param("id") UUID id);

    /**
     * Find product with media
     */
    @EntityGraph(attributePaths = {"images", "category"})
    @Query("SELECT p FROM Product p WHERE p.id = :id")
    Optional<Product> findByIdWithMedia(@Param("id") UUID id);

    // ====================================
    // Bulk Operations
    // ====================================

    /**
     * Find products by IDs
     */
    @Query("SELECT p FROM Product p WHERE p.id IN :ids AND p.deleted = false")
    List<Product> findByIdIn(@Param("ids") List<UUID> ids);

    /**
     * Find products by IDs with relationships
     */
    @EntityGraph(attributePaths = {"category", "prices", "inventory"})
    @Query("SELECT p FROM Product p WHERE p.id IN :ids AND p.deleted = false")
    List<Product> findByIdInWithRelations(@Param("ids") List<UUID> ids);

    /**
     * Find product IDs by category
     */
    @Query("SELECT p.id FROM Product p WHERE p.category.id = :categoryId AND p.deleted = false")
    Set<UUID> findIdsByCategoryId(@Param("categoryId") UUID categoryId);

    /**
     * Find product IDs by status
     */
    @Query("SELECT p.id FROM Product p WHERE p.status = :status AND p.deleted = false")
    Set<UUID> findIdsByStatus(@Param("status") ProductStatus status);

    // ====================================
    // Validation
    // ====================================

    /**
     * Find products without prices
     */
    @Query("SELECT p FROM Product p WHERE SIZE(p.prices) = 0 AND p.deleted = false")
    List<Product> findWithoutPrices();

    /**
     * Find products missing required attributes
     */
    @Query("SELECT p FROM Product p WHERE p.deleted = false " +
           "AND EXISTS (SELECT a FROM ProductAttribute a WHERE a.product = p AND a.required = true " +
           "AND (a.value IS NULL OR a.value = ''))")
    List<Product> findWithMissingRequiredAttributes();

    /**
     * Find products ready to publish
     */
    @Query("SELECT p FROM Product p WHERE p.status = 'READY' AND p.deleted = false")
    List<Product> findReadyToPublish();

    boolean existsByNameAndCategoryId(String name, UUID categoryId);
}