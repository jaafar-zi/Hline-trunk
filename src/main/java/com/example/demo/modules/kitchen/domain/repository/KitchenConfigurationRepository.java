package com.example.demo.modules.kitchen.domain.repository;

import com.example.demo.common.base.repository.BaseRepository;
import com.example.demo.modules.kitchen.domain.model.KitchenConfiguration;
import com.example.demo.modules.kitchen.domain.model.vo.KitchenConfigurationStatus;
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
 * Kitchen Configuration Repository
 *
 * Note: Extends BaseRepository (not SoftDeleteRepository)
 * Configurations are never soft-deleted — they are archived instead.
 */
@Repository
public interface KitchenConfigurationRepository extends BaseRepository<KitchenConfiguration, UUID> {

    // ====================================
    // Find by Customer
    // ====================================

    @Query("SELECT c FROM KitchenConfiguration c " +
           "WHERE c.customerId = :customerId " +
           "ORDER BY c.updatedAt DESC")
    List<KitchenConfiguration> findByCustomerId(@Param("customerId") UUID customerId);

    @Query("SELECT c FROM KitchenConfiguration c " +
           "WHERE c.customerId = :customerId " +
           "ORDER BY c.updatedAt DESC")
    Page<KitchenConfiguration> findByCustomerId(
        @Param("customerId") UUID customerId,
        Pageable pageable
    );

    @Query("SELECT c FROM KitchenConfiguration c " +
           "WHERE c.customerId = :customerId AND c.status = :status " +
           "ORDER BY c.updatedAt DESC")
    List<KitchenConfiguration> findByCustomerIdAndStatus(
        @Param("customerId") UUID customerId,
        @Param("status") KitchenConfigurationStatus status
    );

    @Query("SELECT c FROM KitchenConfiguration c " +
           "WHERE c.customerId = :customerId AND c.status = 'DRAFT' " +
           "ORDER BY c.updatedAt DESC")
    List<KitchenConfiguration> findDraftsByCustomerId(@Param("customerId") UUID customerId);

    // ====================================
    // Find by Session (guest users)
    // ====================================

    @Query("SELECT c FROM KitchenConfiguration c " +
           "WHERE c.sessionId = :sessionId " +
           "ORDER BY c.updatedAt DESC")
    List<KitchenConfiguration> findBySessionId(@Param("sessionId") String sessionId);

    @Query("SELECT c FROM KitchenConfiguration c " +
           "WHERE c.sessionId = :sessionId AND c.status = 'DRAFT' " +
           "ORDER BY c.updatedAt DESC")
    Optional<KitchenConfiguration> findActiveDraftBySessionId(
        @Param("sessionId") String sessionId
    );

    // ====================================
    // Find by Collection
    // ====================================

    @Query("SELECT c FROM KitchenConfiguration c " +
           "WHERE c.collection.id = :collectionId " +
           "ORDER BY c.createdAt DESC")
    Page<KitchenConfiguration> findByCollectionId(
        @Param("collectionId") UUID collectionId,
        Pageable pageable
    );

    // ====================================
    // Find with Relations
    // ====================================

    @EntityGraph(attributePaths = {
        "collection",
        "collection.colour",
        "collection.finishGroup",
        "collection.finishGroup.range",
        "selectedCabinetColour",
        "items",
        "items.product",
        "items.product.unitCategory",
        "items.variant",
        "items.variant.cabinetColour"
    })
    @Query("SELECT c FROM KitchenConfiguration c WHERE c.id = :id")
    Optional<KitchenConfiguration> findByIdWithAll(@Param("id") UUID id);

    @EntityGraph(attributePaths = {
        "collection",
        "collection.colour",
        "selectedCabinetColour",
        "items",
        "items.product",
        "items.variant"
    })
    @Query("SELECT c FROM KitchenConfiguration c WHERE c.customerId = :customerId AND c.id = :id")
    Optional<KitchenConfiguration> findByIdAndCustomerId(
        @Param("id") UUID id,
        @Param("customerId") UUID customerId
    );

    // ====================================
    // Statistics
    // ====================================

    @Query("SELECT COUNT(c) FROM KitchenConfiguration c WHERE c.status = :status")
    long countByStatus(@Param("status") KitchenConfigurationStatus status);

    @Query("SELECT COUNT(c) FROM KitchenConfiguration c " +
           "WHERE c.customerId = :customerId AND c.status = 'DRAFT'")
    long countDraftsByCustomerId(@Param("customerId") UUID customerId);
}