package com.example.demo.modules.kitchen.domain.repository;

import com.example.demo.common.base.repository.SoftDeleteRepository;
import com.example.demo.modules.kitchen.domain.model.KitchenCollection;
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
 * Kitchen Collection Repository
 */
@Repository
public interface KitchenCollectionRepository extends SoftDeleteRepository<KitchenCollection, UUID> {

    // ====================================
    // Find by Slug
    // ====================================

    @Query("SELECT c FROM KitchenCollection c WHERE c.slug = :slug AND c.deleted = false")
    Optional<KitchenCollection> findBySlug(@Param("slug") String slug);

    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END " +
           "FROM KitchenCollection c WHERE c.slug = :slug AND c.deleted = false")
    boolean existsBySlug(@Param("slug") String slug);

    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END " +
           "FROM KitchenCollection c WHERE c.slug = :slug AND c.id <> :id AND c.deleted = false")
    boolean existsBySlugAndIdNot(@Param("slug") String slug, @Param("id") UUID id);

    // ====================================
    // Find by Range / FinishGroup
    // ====================================

    @Query("SELECT c FROM KitchenCollection c " +
           "JOIN c.finishGroup fg " +
           "WHERE fg.range.id = :rangeId AND c.deleted = false " +
           "ORDER BY c.sortOrder ASC")
    List<KitchenCollection> findByRangeId(@Param("rangeId") UUID rangeId);

    @Query("SELECT c FROM KitchenCollection c " +
           "JOIN c.finishGroup fg " +
           "WHERE fg.range.id = :rangeId AND c.active = true AND c.deleted = false " +
           "ORDER BY c.sortOrder ASC")
    List<KitchenCollection> findActiveByRangeId(@Param("rangeId") UUID rangeId);

    @Query("SELECT c FROM KitchenCollection c " +
           "WHERE c.finishGroup.id = :finishGroupId AND c.deleted = false " +
           "ORDER BY c.sortOrder ASC")
    List<KitchenCollection> findByFinishGroupId(@Param("finishGroupId") UUID finishGroupId);

    @Query("SELECT c FROM KitchenCollection c " +
           "WHERE c.finishGroup.id = :finishGroupId AND c.active = true AND c.deleted = false " +
           "ORDER BY c.sortOrder ASC")
    List<KitchenCollection> findActiveByFinishGroupId(@Param("finishGroupId") UUID finishGroupId);

    // ====================================
    // Find by Colour
    // ====================================

    @Query("SELECT c FROM KitchenCollection c " +
           "WHERE c.colour.id = :colourId AND c.deleted = false")
    List<KitchenCollection> findByColourId(@Param("colourId") UUID colourId);

    @Query("SELECT c FROM KitchenCollection c " +
           "JOIN c.finishGroup fg " +
           "WHERE fg.range.id = :rangeId AND c.colour.id = :colourId " +
           "AND c.deleted = false")
    Optional<KitchenCollection> findByRangeIdAndColourId(
        @Param("rangeId") UUID rangeId,
        @Param("colourId") UUID colourId
    );

    /**
     * Find sibling collections (same range, different colour)
     * Used for the colour switcher on the collection detail page
     */
    @Query("SELECT c FROM KitchenCollection c " +
           "JOIN c.finishGroup fg " +
           "WHERE fg.range.id = :rangeId " +
           "AND c.id <> :collectionId " +
           "AND c.active = true AND c.deleted = false " +
           "ORDER BY c.sortOrder ASC")
    List<KitchenCollection> findSiblingsByRangeId(
        @Param("rangeId") UUID rangeId,
        @Param("collectionId") UUID collectionId
    );

    // ====================================
    // Find by Range Slug + Collection Slug (URL lookup)
    // ====================================

    @Query("SELECT c FROM KitchenCollection c " +
           "JOIN c.finishGroup fg " +
           "WHERE fg.range.slug = :rangeSlug AND c.slug = :collectionSlug " +
           "AND c.deleted = false")
    Optional<KitchenCollection> findByRangeSlugAndCollectionSlug(
        @Param("rangeSlug") String rangeSlug,
        @Param("collectionSlug") String collectionSlug
    );

    // ====================================
    // Find with Relations
    // ====================================

    @EntityGraph(attributePaths = {
        "finishGroup",
        "finishGroup.range",
        "finishGroup.finishType",
        "colour",
        "defaultCabinetColour",
        "availableCabinetColours"
    })
    @Query("SELECT c FROM KitchenCollection c WHERE c.slug = :slug AND c.deleted = false")
    Optional<KitchenCollection> findBySlugWithDetails(@Param("slug") String slug);

    @EntityGraph(attributePaths = {
        "finishGroup",
        "finishGroup.range",
        "finishGroup.finishType",
        "colour",
        "defaultCabinetColour",
        "availableCabinetColours",
        "products",
        "products.unitCategory",
        "products.variants",
        "products.variants.cabinetColour",
        "products.images"
    })
    @Query("SELECT c FROM KitchenCollection c WHERE c.slug = :slug AND c.deleted = false")
    Optional<KitchenCollection> findBySlugWithProducts(@Param("slug") String slug);

    @EntityGraph(attributePaths = {
        "finishGroup",
        "finishGroup.range",
        "colour",
        "defaultCabinetColour",
        "availableCabinetColours"
    })
    @Query("SELECT c FROM KitchenCollection c " +
           "WHERE c.finishGroup.id = :finishGroupId AND c.deleted = false")
    List<KitchenCollection> findByFinishGroupIdWithDetails(
        @Param("finishGroupId") UUID finishGroupId
    );

    // ====================================
    // Search
    // ====================================

    @Query("SELECT c FROM KitchenCollection c " +
           "JOIN c.finishGroup fg " +
           "WHERE c.deleted = false AND c.active = true AND (" +
           "LOWER(c.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.colour.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(fg.range.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<KitchenCollection> search(@Param("searchTerm") String searchTerm, Pageable pageable);

    // ====================================
    // Statistics
    // ====================================

    @Query("SELECT COUNT(c) FROM KitchenCollection c " +
           "WHERE c.active = true AND c.deleted = false")
    long countActive();

    @Query("SELECT COUNT(c) FROM KitchenCollection c " +
           "JOIN c.finishGroup fg " +
           "WHERE fg.range.id = :rangeId AND c.deleted = false")
    long countByRangeId(@Param("rangeId") UUID rangeId);

    @Query("SELECT COUNT(c) FROM KitchenCollection c " +
           "WHERE c.finishGroup.id = :finishGroupId AND c.deleted = false")
    long countByFinishGroupId(@Param("finishGroupId") UUID finishGroupId);
}