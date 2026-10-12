package com.example.demo.modules.kitchen.domain.repository;

import com.example.demo.common.base.repository.SoftDeleteRepository;
import com.example.demo.modules.kitchen.domain.model.KitchenFinishType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Kitchen Finish Type Repository
 */
@Repository
public interface KitchenFinishTypeRepository extends SoftDeleteRepository<KitchenFinishType, UUID> {

    // ====================================
    // Find by Code / Name
    // ====================================

    @Query("SELECT ft FROM KitchenFinishType ft WHERE ft.code = :code AND ft.deleted = false")
    Optional<KitchenFinishType> findByCode(@Param("code") String code);

    @Query("SELECT CASE WHEN COUNT(ft) > 0 THEN true ELSE false END " +
           "FROM KitchenFinishType ft WHERE ft.code = :code AND ft.deleted = false")
    boolean existsByCode(@Param("code") String code);

    @Query("SELECT CASE WHEN COUNT(ft) > 0 THEN true ELSE false END " +
           "FROM KitchenFinishType ft WHERE ft.code = :code AND ft.id <> :id AND ft.deleted = false")
    boolean existsByCodeAndIdNot(@Param("code") String code, @Param("id") UUID id);

    // ====================================
    // Find Active
    // ====================================

    @Query("SELECT ft FROM KitchenFinishType ft WHERE ft.active = true AND ft.deleted = false " +
           "ORDER BY ft.sortOrder ASC, ft.name ASC")
    List<KitchenFinishType> findAllActive();

    @Query("SELECT ft FROM KitchenFinishType ft WHERE ft.deleted = false " +
           "ORDER BY ft.sortOrder ASC")
    Page<KitchenFinishType> findAll(Pageable pageable);

    // ====================================
    // Find by Range
    // ====================================

    @Query("SELECT DISTINCT fg.finishType FROM KitchenFinishGroup fg " +
           "WHERE fg.range.id = :rangeId AND fg.deleted = false AND fg.active = true")
    List<KitchenFinishType> findActiveByRangeId(@Param("rangeId") UUID rangeId);

    // ====================================
    // Statistics
    // ====================================

    @Query("SELECT COUNT(ft) FROM KitchenFinishType ft WHERE ft.active = true AND ft.deleted = false")
    long countActive();
}