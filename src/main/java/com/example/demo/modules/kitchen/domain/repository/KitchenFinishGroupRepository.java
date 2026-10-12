package com.example.demo.modules.kitchen.domain.repository;

import com.example.demo.common.base.repository.SoftDeleteRepository;
import com.example.demo.modules.kitchen.domain.model.KitchenFinishGroup;
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
 * Kitchen Finish Group Repository
 */
@Repository
public interface KitchenFinishGroupRepository extends SoftDeleteRepository<KitchenFinishGroup, UUID> {

    // ====================================
    // Find by Range
    // ====================================

    @Query("SELECT fg FROM KitchenFinishGroup fg " +
           "WHERE fg.range.id = :rangeId AND fg.deleted = false " +
           "ORDER BY fg.sortOrder ASC")
    List<KitchenFinishGroup> findByRangeId(@Param("rangeId") UUID rangeId);

    @Query("SELECT fg FROM KitchenFinishGroup fg " +
           "WHERE fg.range.id = :rangeId AND fg.active = true AND fg.deleted = false " +
           "ORDER BY fg.sortOrder ASC")
    List<KitchenFinishGroup> findActiveByRangeId(@Param("rangeId") UUID rangeId);

    @Query("SELECT fg FROM KitchenFinishGroup fg " +
           "WHERE fg.range.id = :rangeId AND fg.deleted = false")
    Page<KitchenFinishGroup> findByRangeId(@Param("rangeId") UUID rangeId, Pageable pageable);

    // ====================================
    // Find by Slug
    // ====================================

    @Query("SELECT fg FROM KitchenFinishGroup fg " +
           "WHERE fg.range.id = :rangeId AND fg.slug = :slug AND fg.deleted = false")
    Optional<KitchenFinishGroup> findByRangeIdAndSlug(
        @Param("rangeId") UUID rangeId,
        @Param("slug") String slug
    );

    // ====================================
    // Find by Finish Type
    // ====================================

    @Query("SELECT fg FROM KitchenFinishGroup fg " +
           "WHERE fg.finishType.id = :finishTypeId AND fg.deleted = false")
    List<KitchenFinishGroup> findByFinishTypeId(@Param("finishTypeId") UUID finishTypeId);

    @Query("SELECT fg FROM KitchenFinishGroup fg " +
           "WHERE fg.range.id = :rangeId AND fg.finishType.id = :finishTypeId " +
           "AND fg.deleted = false")
    Optional<KitchenFinishGroup> findByRangeIdAndFinishTypeId(
        @Param("rangeId") UUID rangeId,
        @Param("finishTypeId") UUID finishTypeId
    );

    // ====================================
    // Find with Relations
    // ====================================

    @EntityGraph(attributePaths = {
        "range",
        "finishType",
        "collections",
        "collections.colour"
    })
    @Query("SELECT fg FROM KitchenFinishGroup fg WHERE fg.id = :id AND fg.deleted = false")
    Optional<KitchenFinishGroup> findByIdWithCollections(@Param("id") UUID id);

    @EntityGraph(attributePaths = {"range", "finishType"})
    @Query("SELECT fg FROM KitchenFinishGroup fg " +
           "WHERE fg.range.id = :rangeId AND fg.deleted = false")
    List<KitchenFinishGroup> findByRangeIdWithDetails(@Param("rangeId") UUID rangeId);

    // ====================================
    // Existence Checks
    // ====================================

    @Query("SELECT CASE WHEN COUNT(fg) > 0 THEN true ELSE false END " +
           "FROM KitchenFinishGroup fg " +
           "WHERE fg.range.id = :rangeId AND fg.finishType.id = :finishTypeId " +
           "AND fg.deleted = false")
    boolean existsByRangeIdAndFinishTypeId(
        @Param("rangeId") UUID rangeId,
        @Param("finishTypeId") UUID finishTypeId
    );

    // ====================================
    // Statistics
    // ====================================

    @Query("SELECT COUNT(fg) FROM KitchenFinishGroup fg " +
           "WHERE fg.range.id = :rangeId AND fg.active = true AND fg.deleted = false")
    long countActiveByRangeId(@Param("rangeId") UUID rangeId);

    @Query("SELECT COUNT(fg) FROM KitchenFinishGroup fg " +
           "WHERE fg.finishType.id = :finishTypeId AND fg.deleted = false")
    long countByFinishTypeId(@Param("finishTypeId") UUID finishTypeId);
}