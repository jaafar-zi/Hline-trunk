package com.example.demo.modules.kitchen.domain.repository;

import com.example.demo.common.base.repository.SoftDeleteRepository;
import com.example.demo.modules.kitchen.domain.model.KitchenUnitCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Kitchen Unit Category Repository
 */
@Repository
public interface KitchenUnitCategoryRepository
        extends SoftDeleteRepository<KitchenUnitCategory, UUID> {

    // ====================================
    // Find by Code / Slug
    // ====================================

    @Query("SELECT uc FROM KitchenUnitCategory uc WHERE uc.code = :code AND uc.deleted = false")
    Optional<KitchenUnitCategory> findByCode(@Param("code") String code);

    @Query("SELECT uc FROM KitchenUnitCategory uc WHERE uc.slug = :slug AND uc.deleted = false")
    Optional<KitchenUnitCategory> findBySlug(@Param("slug") String slug);

    @Query("SELECT CASE WHEN COUNT(uc) > 0 THEN true ELSE false END " +
           "FROM KitchenUnitCategory uc WHERE uc.code = :code AND uc.deleted = false")
    boolean existsByCode(@Param("code") String code);

    @Query("SELECT CASE WHEN COUNT(uc) > 0 THEN true ELSE false END " +
           "FROM KitchenUnitCategory uc " +
           "WHERE uc.code = :code AND uc.id <> :id AND uc.deleted = false")
    boolean existsByCodeAndIdNot(@Param("code") String code, @Param("id") UUID id);

    // ====================================
    // Find Active
    // ====================================

    @Query("SELECT uc FROM KitchenUnitCategory uc " +
           "WHERE uc.active = true AND uc.deleted = false " +
           "ORDER BY uc.sortOrder ASC, uc.name ASC")
    List<KitchenUnitCategory> findAllActive();

    @Query("SELECT uc FROM KitchenUnitCategory uc WHERE uc.deleted = false " +
           "ORDER BY uc.sortOrder ASC")
    Page<KitchenUnitCategory> findAll(Pageable pageable);

    // ====================================
    // Find by Collection (categories that have products)
    // ====================================

    @Query("SELECT DISTINCT p.unitCategory FROM KitchenProduct p " +
           "WHERE p.collection.id = :collectionId " +
           "AND p.active = true AND p.deleted = false " +
           "ORDER BY p.unitCategory.sortOrder ASC")
    List<KitchenUnitCategory> findCategoriesWithProductsByCollectionId(
        @Param("collectionId") UUID collectionId
    );

    // ====================================
    // Statistics
    // ====================================

    @Query("SELECT COUNT(uc) FROM KitchenUnitCategory uc " +
           "WHERE uc.active = true AND uc.deleted = false")
    long countActive();

    @Query("SELECT COUNT(p) FROM KitchenProduct p " +
           "WHERE p.unitCategory.id = :categoryId AND p.deleted = false")
    long countProductsByCategoryId(@Param("categoryId") UUID categoryId);
}