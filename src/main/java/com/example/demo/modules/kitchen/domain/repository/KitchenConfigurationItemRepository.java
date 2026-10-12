package com.example.demo.modules.kitchen.domain.repository;

import com.example.demo.common.base.repository.BaseRepository;
import com.example.demo.modules.kitchen.domain.model.KitchenConfigurationItem;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Kitchen Configuration Item Repository
 *
 * Fix: Removed updateVariantForNonOverriddenItems — JPQL UPDATE cannot
 * use parameter navigation (:param.field). Colour cascade is handled
 * in the service layer by iterating items individually.
 */

@Repository
public interface KitchenConfigurationItemRepository
        extends BaseRepository<KitchenConfigurationItem, UUID> {

    // ====================================
    // Find by Configuration
    // ====================================

    @Query("SELECT i FROM KitchenConfigurationItem i " +
           "WHERE i.configuration.id = :configId " +
           "ORDER BY i.position ASC")
    List<KitchenConfigurationItem> findByConfigurationId(
        @Param("configId") UUID configId);

    @EntityGraph(attributePaths = {
        "product",
        "product.unitCategory",
        "product.images",
        "variant",
        "variant.cabinetColour"
    })
    @Query("SELECT i FROM KitchenConfigurationItem i " +
           "WHERE i.configuration.id = :configId " +
           "ORDER BY i.position ASC")
    List<KitchenConfigurationItem> findByConfigurationIdWithDetails(
        @Param("configId") UUID configId);

    // ====================================
    // Find by Product / Variant
    // ====================================

    @Query("SELECT i FROM KitchenConfigurationItem i " +
           "WHERE i.configuration.id = :configId " +
           "AND i.product.id = :productId")
    List<KitchenConfigurationItem> findByConfigurationIdAndProductId(
        @Param("configId") UUID configId,
        @Param("productId") UUID productId);

    @Query("SELECT i FROM KitchenConfigurationItem i " +
           "WHERE i.variant.id = :variantId")
    List<KitchenConfigurationItem> findByVariantId(
        @Param("variantId") UUID variantId);

    // ====================================
    // Colour Override Queries
    // ====================================

    @Query("SELECT i FROM KitchenConfigurationItem i " +
           "WHERE i.configuration.id = :configId " +
           "AND i.colourOverridden = false " +
           "ORDER BY i.position ASC")
    List<KitchenConfigurationItem> findNonOverriddenByConfigurationId(
        @Param("configId") UUID configId);

    @Query("SELECT i FROM KitchenConfigurationItem i " +
           "WHERE i.configuration.id = :configId " +
           "AND i.colourOverridden = true")
    List<KitchenConfigurationItem> findColourOverriddenByConfigurationId(
        @Param("configId") UUID configId);

    // ====================================
    // Statistics
    // ====================================

    @Query("SELECT COUNT(i) FROM KitchenConfigurationItem i " +
           "WHERE i.configuration.id = :configId")
    long countByConfigurationId(@Param("configId") UUID configId);

    @Query("SELECT SUM(i.quantity) FROM KitchenConfigurationItem i " +
           "WHERE i.configuration.id = :configId")
    Long sumQuantityByConfigurationId(@Param("configId") UUID configId);

    @Query("SELECT SUM(i.unitPrice.amount * i.quantity) " +
           "FROM KitchenConfigurationItem i " +
           "WHERE i.configuration.id = :configId")
    java.math.BigDecimal sumTotalByConfigurationId(@Param("configId") UUID configId);
}