package com.example.demo.modules.product.domain.repository;

import com.example.demo.common.base.repository.BaseRepository;
import com.example.demo.modules.product.domain.model.ProductAttribute;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Product Attribute Repository
 *
 * Repository for ProductAttribute entity
 * Optimized for attribute management and validation
 *
 * @author PIM Team
 * @version 1.0
 */
@Repository
public interface ProductAttributeRepository extends BaseRepository<ProductAttribute, UUID> {

    // ====================================
    // Find by Product
    // ====================================

    /**
     * Find all attributes for a product
     */
    @Query("SELECT a FROM ProductAttribute a WHERE a.product.id = :productId ORDER BY a.displayOrder, a.key")
    List<ProductAttribute> findByProductId(@Param("productId") UUID productId);

    /**
     * Find optional attributes for a product  
     */
    @Query("SELECT a FROM ProductAttribute a WHERE a.product.id = :productId AND a.required = false ORDER BY a.displayOrder, a.key")
    List<ProductAttribute> findOptionalByProductId(@Param("productId") UUID productId);

    /**
     * Find attributes by product and required flag
     */
    @Query("SELECT a FROM ProductAttribute a WHERE a.product.id = :productId AND a.required = :required ORDER BY a.displayOrder, a.key")
    List<ProductAttribute> findByProductIdAndRequired(@Param("productId") UUID productId, @Param("required") boolean required);

    /**
     * Find attribute by product and key
     */
    @Query("SELECT a FROM ProductAttribute a WHERE a.product.id = :productId AND a.key = :key")
    Optional<ProductAttribute> findByProductIdAndKey(
        @Param("productId") UUID productId,
        @Param("key") String key
    );

    /**
     * Find distinct attribute keys across all products
     */
    @Query("SELECT DISTINCT LOWER(a.key) FROM ProductAttribute a")
    Set<String> findDistinctKeys();

    /**
     * Find attributes by value containing (case-insensitive)
     */
    @Query("SELECT a FROM ProductAttribute a WHERE LOWER(a.value) LIKE LOWER(CONCAT('%', :value, '%'))")
    List<ProductAttribute> findByValueContainingIgnoreCase(@Param("value") String value);

    /**
     * Count attributes for product
     */
    @Query("SELECT COUNT(a) FROM ProductAttribute a WHERE a.product.id = :productId")
    long countByProductId(@Param("productId") UUID productId);

    /**
     * Count required attributes for product
     */
    @Query("SELECT COUNT(a) FROM ProductAttribute a WHERE a.product.id = :productId AND a.required = :required")
    long countByProductIdAndRequired(@Param("productId") UUID productId, @Param("required") boolean required);

    // ====================================
    // Find by Key/Value
    // ====================================

    /**
     * Find attributes by key
     */
    @Query("SELECT a FROM ProductAttribute a WHERE a.key = :key")
    List<ProductAttribute> findByKey(@Param("key") String key);

    /**
     * Find attributes by key and value
     */
    @Query("SELECT a FROM ProductAttribute a WHERE a.key = :key AND a.value = :value")
    List<ProductAttribute> findByKeyAndValue(@Param("key") String key, @Param("value") String value);

    /**
     * Find products by attribute
     */
    @Query("SELECT a.product.id FROM ProductAttribute a WHERE a.key = :key AND a.value = :value")
    Set<UUID> findProductIdsByKeyAndValue(@Param("key") String key, @Param("value") String value);

    /**
     * Find distinct attribute keys
     */
    @Query("SELECT DISTINCT a.key FROM ProductAttribute a ORDER BY a.key")
    List<String> findAllDistinctKeys();

    /**
     * Find distinct values for key
     */
    @Query("SELECT DISTINCT a.value FROM ProductAttribute a WHERE a.key = :key AND a.value IS NOT NULL ORDER BY a.value")
    List<String> findDistinctValuesByKey(@Param("key") String key);

    // ====================================
    // Find Required Attributes
    // ====================================

    /**
     * Find required attributes for product
     */
    @Query("SELECT a FROM ProductAttribute a WHERE a.product.id = :productId AND a.required = true")
    List<ProductAttribute> findRequiredByProductId(@Param("productId") UUID productId);

    /**
     * Find empty required attributes
     */
    @Query("SELECT a FROM ProductAttribute a WHERE a.product.id = :productId " +
           "AND a.required = true AND (a.value IS NULL OR a.value = '')")
    List<ProductAttribute> findEmptyRequiredByProductId(@Param("productId") UUID productId);

    /**
     * Check if product has all required attributes filled
     */
    @Query("SELECT CASE WHEN COUNT(a) = 0 THEN true ELSE false END " +
           "FROM ProductAttribute a WHERE a.product.id = :productId " +
           "AND a.required = true AND (a.value IS NULL OR a.value = '')")
    boolean hasAllRequiredAttributesFilled(@Param("productId") UUID productId);

    /**
     * Count required attributes for product
     */
    @Query("SELECT COUNT(a) FROM ProductAttribute a WHERE a.product.id = :productId AND a.required = true")
    long countRequiredByProductId(@Param("productId") UUID productId);

    /**
     * Count filled required attributes
     */
    @Query("SELECT COUNT(a) FROM ProductAttribute a WHERE a.product.id = :productId " +
           "AND a.required = true AND a.value IS NOT NULL AND a.value <> ''")
    long countFilledRequiredByProductId(@Param("productId") UUID productId);

    // ====================================
    // Find by Category
    // ====================================

    /**
     * Find attributes by category
     */
    @Query("SELECT a FROM ProductAttribute a WHERE a.product.category.id = :categoryId")
    List<ProductAttribute> findByCategoryId(@Param("categoryId") UUID categoryId);

    /**
     * Find distinct keys by category
     */
    @Query("SELECT DISTINCT a.key FROM ProductAttribute a WHERE a.product.category.id = :categoryId ORDER BY a.key")
    List<String> findDistinctKeysByCategoryId(@Param("categoryId") UUID categoryId);

    // ====================================
    // Validation
    // ====================================

    /**
     * Check if attribute key exists for product
     */
    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END " +
           "FROM ProductAttribute a WHERE a.product.id = :productId AND a.key = :key")
    boolean existsByProductIdAndKey(@Param("productId") UUID productId, @Param("key") String key);

    /**
     * Find products with missing required attributes
     */
    @Query("SELECT DISTINCT a.product.id FROM ProductAttribute a " +
           "WHERE a.required = true AND (a.value IS NULL OR a.value = '')")
    Set<UUID> findProductIdsWithMissingRequiredAttributes();

    /**
     * Find products without any attributes
     */
    @Query("SELECT p.id FROM Product p WHERE p.id NOT IN (SELECT DISTINCT a.product.id FROM ProductAttribute a)")
    Set<UUID> findProductIdsWithoutAttributes();

    // ====================================
    // Statistics
    // ====================================

    /**
     * Count total attributes
     */
    @Query("SELECT COUNT(a) FROM ProductAttribute a")
    long countAll();

    /**
     * Count attributes by key
     */
    @Query("SELECT COUNT(a) FROM ProductAttribute a WHERE a.key = :key")
    long countByKey(@Param("key") String key);

    /**
     * Count required attributes
     */
    @Query("SELECT COUNT(a) FROM ProductAttribute a WHERE a.required = true")
    long countRequired();

    /**
     * Count empty attributes
     */
    @Query("SELECT COUNT(a) FROM ProductAttribute a WHERE a.value IS NULL OR a.value = ''")
    long countEmpty();

    // ====================================
    // Bulk Operations
    // ====================================

    /**
     * Find attributes by product IDs
     */
    @Query("SELECT a FROM ProductAttribute a WHERE a.product.id IN :productIds ORDER BY a.displayOrder, a.key")
    List<ProductAttribute> findByProductIdIn(@Param("productIds") List<UUID> productIds);

    /**
     * Delete attributes by product ID
     */
    @Query("DELETE FROM ProductAttribute a WHERE a.product.id = :productId")
    void deleteByProductId(@Param("productId") UUID productId);

    /**
     * Delete attributes by key
     */
    @Query("DELETE FROM ProductAttribute a WHERE a.key = :key")
    void deleteByKey(@Param("key") String key);
}