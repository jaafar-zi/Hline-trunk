package com.example.demo.modules.product.domain.repository;

import com.example.demo.common.base.repository.SoftDeleteRepository;
import com.example.demo.modules.product.domain.model.ProductVariant;
import com.example.demo.modules.product.domain.model.vo.SKU;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Product Variant Repository
 *
 * Repository for ProductVariant entity
 * Contains queries for variant-specific operations
 *
 * @author PIM Team
 * @version 1.0
 */
@Repository
public interface ProductVariantRepository extends SoftDeleteRepository<ProductVariant, UUID> {

    // ====================================
    // Find by SKU
    // ====================================

    /**
     * Find variant by SKU
     */
    @Query("SELECT v FROM ProductVariant v WHERE v.sku = :sku")
    Optional<ProductVariant> findBySku(@Param("sku") SKU sku);

    /**
     * Find variant by SKU value
     */
    @Query("SELECT v FROM ProductVariant v WHERE v.sku.value = :skuValue")
    Optional<ProductVariant> findBySkuValue(@Param("skuValue") String skuValue);

    /**
     * Check if SKU exists
     */
    @Query("SELECT CASE WHEN COUNT(v) > 0 THEN true ELSE false END FROM ProductVariant v WHERE v.sku.value = :skuValue")
    boolean existsBySku(@Param("skuValue") String skuValue);

    // ====================================
    // Find by Product
    // ====================================

    /**
     * Find variants by product ID
     */
    @Query("SELECT v FROM ProductVariant v WHERE v.product.id = :productId AND v.deleted = false ORDER BY v.displayOrder")
    List<ProductVariant> findByProductId(@Param("productId") UUID productId);

    /**
     * Find active variants by product ID
     */
    @Query("SELECT v FROM ProductVariant v WHERE v.product.id = :productId AND v.active = true AND v.deleted = false ORDER BY v.displayOrder")
    List<ProductVariant> findActiveByProductId(@Param("productId") UUID productId);

    /**
     * Find variants by product ID with inventory
     */
    @EntityGraph(attributePaths = {"inventory"})
    @Query("SELECT v FROM ProductVariant v WHERE v.product.id = :productId AND v.deleted = false ORDER BY v.displayOrder")
    List<ProductVariant> findByProductIdWithInventory(@Param("productId") UUID productId);

    /**
     * Find default variant for product
     */
    @Query("SELECT v FROM ProductVariant v WHERE v.product.id = :productId AND v.isDefault = true AND v.deleted = false")
    Optional<ProductVariant> findDefaultByProductId(@Param("productId") UUID productId);

    /**
     * Count variants by product
     */
    @Query("SELECT COUNT(v) FROM ProductVariant v WHERE v.product.id = :productId AND v.deleted = false")
    long countByProductId(@Param("productId") UUID productId);

    /**
     * Count active variants by product
     */
    @Query("SELECT COUNT(v) FROM ProductVariant v WHERE v.product.id = :productId AND v.active = true AND v.deleted = false")
    long countActiveByProductId(@Param("productId") UUID productId);

    // ====================================
    // Find by Option Values
    // ====================================

    /**
     * Find variants by option name and value
     * Example: findByOptionNameAndValue("Size", "Large")
     */
    @Query("SELECT DISTINCT v FROM ProductVariant v JOIN v.optionValues opt " +
        "WHERE opt.optionName = :optionName AND opt.value = :value AND v.deleted = false " +
        "ORDER BY v.displayOrder")
    List<ProductVariant> findByOptionNameAndValue(
        @Param("optionName") String optionName, 
        @Param("value") String value
    );

    /**
     * Find variants by product and option value
     */
    @Query("SELECT DISTINCT v FROM ProductVariant v JOIN v.optionValues opt " +
        "WHERE v.product.id = :productId " +
        "AND opt.optionName = :optionName " +
        "AND opt.value = :value " +
        "AND v.deleted = false " +
        "ORDER BY v.displayOrder")
    List<ProductVariant> findByProductIdAndOption(
        @Param("productId") UUID productId,
        @Param("optionName") String optionName,
        @Param("value") String value
    );

    /**
     * Find variants by any option value
     */
    @Query("SELECT DISTINCT v FROM ProductVariant v JOIN v.optionValues opt " +
        "WHERE opt.value = :value AND v.deleted = false")
    List<ProductVariant> findByOptionValue(@Param("value") String value);

    /**
     * Check if variant exists with specific options
     */
    @Query("SELECT CASE WHEN COUNT(v) > 0 THEN true ELSE false END " +
        "FROM ProductVariant v JOIN v.optionValues opt " +
        "WHERE v.product.id = :productId " +
        "AND opt.optionName = :optionName " +
        "AND opt.value = :value")
    boolean existsByProductIdAndOption(
        @Param("productId") UUID productId,
        @Param("optionName") String optionName,
        @Param("value") String value
    );

    // ====================================
    // Find by Status
    // ====================================

    /**
     * Find all active variants
     */
    @Query("SELECT v FROM ProductVariant v WHERE v.active = true AND v.deleted = false")
    Page<ProductVariant> findAllActive(Pageable pageable);

    /**
     * Find variants in stock
     */
    @Query("SELECT v FROM ProductVariant v JOIN v.inventory i WHERE i.stockStatus = 'IN_STOCK' AND v.deleted = false")
    List<ProductVariant> findInStock();

    /**
     * Find variants out of stock
     */
    @Query("SELECT v FROM ProductVariant v JOIN v.inventory i WHERE i.stockStatus = 'OUT_OF_STOCK' AND v.deleted = false")
    List<ProductVariant> findOutOfStock();

    // ====================================
    // Statistics
    // ====================================

    /**
     * Count active variants
     */
    @Query("SELECT COUNT(v) FROM ProductVariant v WHERE v.active = true AND v.deleted = false")
    long countActive();

    /**
     * Count variants by product IDs
     */
    @Query("SELECT COUNT(v) FROM ProductVariant v WHERE v.product.id IN :productIds AND v.deleted = false")
    long countByProductIds(@Param("productIds") List<UUID> productIds);
}