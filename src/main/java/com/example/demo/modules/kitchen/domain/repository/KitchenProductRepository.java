package com.example.demo.modules.kitchen.domain.repository;

import com.example.demo.common.base.repository.SoftDeleteRepository;
import com.example.demo.modules.kitchen.domain.model.KitchenProduct;
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
 * Kitchen Product Repository
 */
@Repository
public interface KitchenProductRepository extends SoftDeleteRepository<KitchenProduct, UUID> {

    // ====================================
    // Find by SKU / Slug
    // ====================================

    @Query("SELECT p FROM KitchenProduct p WHERE p.sku = :sku AND p.deleted = false")
    Optional<KitchenProduct> findBySku(@Param("sku") String sku);

    @Query("SELECT p FROM KitchenProduct p WHERE p.slug = :slug AND p.deleted = false")
    Optional<KitchenProduct> findBySlug(@Param("slug") String slug);

    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END " +
           "FROM KitchenProduct p WHERE p.sku = :sku AND p.deleted = false")
    boolean existsBySku(@Param("sku") String sku);

    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END " +
           "FROM KitchenProduct p WHERE p.sku = :sku AND p.id <> :id AND p.deleted = false")
    boolean existsBySkuAndIdNot(@Param("sku") String sku, @Param("id") UUID id);

    // ====================================
    // Find by URL path (range + collection + unit slug)
    // ====================================

    @Query("SELECT p FROM KitchenProduct p " +
           "JOIN p.collection c " +
           "JOIN c.finishGroup fg " +
           "WHERE fg.range.slug = :rangeSlug " +
           "AND c.slug = :collectionSlug " +
           "AND p.slug = :productSlug " +
           "AND p.deleted = false")
    Optional<KitchenProduct> findByFullPath(
        @Param("rangeSlug") String rangeSlug,
        @Param("collectionSlug") String collectionSlug,
        @Param("productSlug") String productSlug
    );

    // ====================================
    // Find by Collection
    // ====================================

    @Query("SELECT p FROM KitchenProduct p " +
           "WHERE p.collection.id = :collectionId AND p.deleted = false " +
           "ORDER BY p.unitCategory.sortOrder ASC, p.sortOrder ASC")
    List<KitchenProduct> findByCollectionId(@Param("collectionId") UUID collectionId);

    @Query("SELECT p FROM KitchenProduct p " +
           "WHERE p.collection.id = :collectionId AND p.active = true AND p.deleted = false " +
           "ORDER BY p.unitCategory.sortOrder ASC, p.sortOrder ASC")
    List<KitchenProduct> findActiveByCollectionId(@Param("collectionId") UUID collectionId);

    @Query("SELECT p FROM KitchenProduct p " +
           "WHERE p.collection.id = :collectionId AND p.deleted = false")
    Page<KitchenProduct> findByCollectionId(
        @Param("collectionId") UUID collectionId,
        Pageable pageable
    );

    // ====================================
    // Find by Category
    // ====================================

    @Query("SELECT p FROM KitchenProduct p " +
           "WHERE p.unitCategory.id = :categoryId AND p.deleted = false " +
           "ORDER BY p.sortOrder ASC")
    List<KitchenProduct> findByCategoryId(@Param("categoryId") UUID categoryId);

    @Query("SELECT p FROM KitchenProduct p " +
           "WHERE p.collection.id = :collectionId " +
           "AND p.unitCategory.id = :categoryId " +
           "AND p.active = true AND p.deleted = false " +
           "ORDER BY p.sortOrder ASC")
    List<KitchenProduct> findByCollectionIdAndCategoryId(
        @Param("collectionId") UUID collectionId,
        @Param("categoryId") UUID categoryId
    );

    // ====================================
    // Find Popular
    // ====================================

    @Query("SELECT p FROM KitchenProduct p " +
           "WHERE p.collection.id = :collectionId " +
           "AND p.isPopular = true AND p.active = true AND p.deleted = false " +
           "ORDER BY p.unitCategory.sortOrder ASC, p.sortOrder ASC")
    List<KitchenProduct> findPopularByCollectionId(@Param("collectionId") UUID collectionId);

    @Query("SELECT p FROM KitchenProduct p " +
           "WHERE p.collection.id = :collectionId " +
           "AND p.unitCategory.id = :categoryId " +
           "AND p.isPopular = true AND p.active = true AND p.deleted = false " +
           "ORDER BY p.sortOrder ASC")
    List<KitchenProduct> findPopularByCollectionIdAndCategoryId(
        @Param("collectionId") UUID collectionId,
        @Param("categoryId") UUID categoryId
    );

    // ====================================
    // Find with Relations
    // ====================================

    @EntityGraph(attributePaths = {
        "collection",
        "collection.colour",
        "collection.finishGroup",
        "collection.finishGroup.range",
        "collection.availableCabinetColours",
        "unitCategory",
        "variants",
        "variants.cabinetColour",
        "images"
    })
    @Query("SELECT p FROM KitchenProduct p WHERE p.id = :id AND p.deleted = false")
    Optional<KitchenProduct> findByIdWithAll(@Param("id") UUID id);

    @EntityGraph(attributePaths = {
        "collection",
        "collection.colour",
        "collection.finishGroup",
        "collection.finishGroup.range",
        "unitCategory",
        "variants",
        "variants.cabinetColour",
        "images"
    })
    @Query("SELECT p FROM KitchenProduct p " +
           "JOIN p.collection c JOIN c.finishGroup fg " +
           "WHERE fg.range.slug = :rangeSlug " +
           "AND c.slug = :collectionSlug " +
           "AND p.slug = :productSlug " +
           "AND p.deleted = false")
    Optional<KitchenProduct> findByFullPathWithAll(
        @Param("rangeSlug") String rangeSlug,
        @Param("collectionSlug") String collectionSlug,
        @Param("productSlug") String productSlug
    );

    @EntityGraph(attributePaths = {
        "unitCategory",
        "variants",
        "variants.cabinetColour",
        "images"
    })
    @Query("SELECT p FROM KitchenProduct p " +
           "WHERE p.collection.id = :collectionId " +
           "AND p.active = true AND p.deleted = false " +
           "ORDER BY p.unitCategory.sortOrder ASC, p.sortOrder ASC")
    List<KitchenProduct> findActiveByCollectionIdWithVariants(
        @Param("collectionId") UUID collectionId
    );

    // ====================================
    // Search
    // ====================================

    @Query("SELECT p FROM KitchenProduct p " +
           "WHERE p.collection.id = :collectionId " +
           "AND p.active = true AND p.deleted = false AND (" +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.sku) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.shortDescription) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<KitchenProduct> searchByCollection(
        @Param("collectionId") UUID collectionId,
        @Param("searchTerm") String searchTerm,
        Pageable pageable
    );

    // ====================================
    // Statistics
    // ====================================

    @Query("SELECT COUNT(p) FROM KitchenProduct p " +
           "WHERE p.collection.id = :collectionId " +
           "AND p.active = true AND p.deleted = false")
    long countActiveByCollectionId(@Param("collectionId") UUID collectionId);

    @Query("SELECT COUNT(p) FROM KitchenProduct p " +
           "WHERE p.unitCategory.id = :categoryId AND p.deleted = false")
    long countByCategoryId(@Param("categoryId") UUID categoryId);

    @Query("SELECT COUNT(p) FROM KitchenProduct p " +
           "WHERE p.collection.id = :collectionId " +
           "AND p.unitCategory.id = :categoryId " +
           "AND p.active = true AND p.deleted = false")
    long countByCollectionIdAndCategoryId(
        @Param("collectionId") UUID collectionId,
        @Param("categoryId") UUID categoryId
    );

    @Query("SELECT MIN(p.basePrice.amount) FROM KitchenProduct p " +
           "WHERE p.collection.id = :collectionId " +
           "AND p.active = true AND p.deleted = false")
    java.math.BigDecimal findMinPriceByCollectionId(@Param("collectionId") UUID collectionId);
}