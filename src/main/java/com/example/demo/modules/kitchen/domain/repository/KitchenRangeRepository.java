package com.example.demo.modules.kitchen.domain.repository;

import com.example.demo.common.base.repository.SoftDeleteRepository;
import com.example.demo.modules.kitchen.domain.model.KitchenRange;
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
 * Kitchen Range Repository
 */
@Repository
public interface KitchenRangeRepository extends SoftDeleteRepository<KitchenRange, UUID> {

    // ====================================
    // Find by Code / Slug
    // ====================================

    @Query("SELECT r FROM KitchenRange r WHERE r.code = :code AND r.deleted = false")
    Optional<KitchenRange> findByCode(@Param("code") String code);

    @Query("SELECT r FROM KitchenRange r WHERE r.slug = :slug AND r.deleted = false")
    Optional<KitchenRange> findBySlug(@Param("slug") String slug);

    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END " +
           "FROM KitchenRange r WHERE r.code = :code AND r.deleted = false")
    boolean existsByCode(@Param("code") String code);

    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END " +
           "FROM KitchenRange r WHERE r.slug = :slug AND r.deleted = false")
    boolean existsBySlug(@Param("slug") String slug);

    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END " +
           "FROM KitchenRange r WHERE r.code = :code AND r.id <> :id AND r.deleted = false")
    boolean existsByCodeAndIdNot(@Param("code") String code, @Param("id") UUID id);

    // ====================================
    // Find Active
    // ====================================

    @Query("SELECT r FROM KitchenRange r WHERE r.active = true AND r.deleted = false " +
           "ORDER BY r.sortOrder ASC, r.name ASC")
    List<KitchenRange> findAllActive();

    @Query("SELECT r FROM KitchenRange r WHERE r.active = true AND r.deleted = false " +
           "ORDER BY r.sortOrder ASC")
    Page<KitchenRange> findAllActive(Pageable pageable);

    @Query("SELECT r FROM KitchenRange r WHERE r.deleted = false ORDER BY r.sortOrder ASC")
    Page<KitchenRange> findAll(Pageable pageable);

    // ====================================
    // Find with Relations
    // ====================================

    @EntityGraph(attributePaths = {
        "finishGroups",
        "finishGroups.finishType",
        "finishGroups.collections",
        "finishGroups.collections.colour"
    })
    @Query("SELECT r FROM KitchenRange r WHERE r.slug = :slug AND r.deleted = false")
    Optional<KitchenRange> findBySlugWithCollections(@Param("slug") String slug);

    @EntityGraph(attributePaths = {
        "finishGroups",
        "finishGroups.finishType"
    })
    @Query("SELECT r FROM KitchenRange r WHERE r.id = :id AND r.deleted = false")
    Optional<KitchenRange> findByIdWithFinishGroups(@Param("id") UUID id);

    @EntityGraph(attributePaths = {
        "finishGroups",
        "finishGroups.finishType",
        "finishGroups.collections",
        "finishGroups.collections.colour",
        "finishGroups.collections.availableCabinetColours"
    })
    @Query("SELECT r FROM KitchenRange r WHERE r.id = :id AND r.deleted = false")
    Optional<KitchenRange> findByIdWithAll(@Param("id") UUID id);

    // ====================================
    // Search
    // ====================================

    @Query("SELECT r FROM KitchenRange r WHERE r.deleted = false AND (" +
           "LOWER(r.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(r.shortDescription) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<KitchenRange> search(@Param("searchTerm") String searchTerm, Pageable pageable);

    // ====================================
    // Statistics
    // ====================================

    @Query("SELECT COUNT(r) FROM KitchenRange r WHERE r.active = true AND r.deleted = false")
    long countActive();

    @Query("SELECT COUNT(fg) FROM KitchenFinishGroup fg " +
           "WHERE fg.range.id = :rangeId AND fg.deleted = false")
    long countFinishGroupsByRangeId(@Param("rangeId") UUID rangeId);

    @Query("SELECT COUNT(c) FROM KitchenCollection c " +
           "JOIN c.finishGroup fg WHERE fg.range.id = :rangeId AND c.deleted = false")
    long countCollectionsByRangeId(@Param("rangeId") UUID rangeId);
}