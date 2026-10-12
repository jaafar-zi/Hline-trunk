package com.example.demo.modules.kitchen.domain.repository;

import com.example.demo.common.base.repository.SoftDeleteRepository;
import com.example.demo.modules.kitchen.domain.model.KitchenProductVariant;
import com.example.demo.modules.kitchen.domain.model.vo.AssemblyType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Kitchen Product Variant Repository
 */
@Repository
public interface KitchenProductVariantRepository
        extends SoftDeleteRepository<KitchenProductVariant, UUID> {

    // ====================================
    // Find by SKU
    // ====================================

    @Query("SELECT v FROM KitchenProductVariant v WHERE v.sku = :sku AND v.deleted = false")
    Optional<KitchenProductVariant> findBySku(@Param("sku") String sku);

    @Query("SELECT CASE WHEN COUNT(v) > 0 THEN true ELSE false END " +
           "FROM KitchenProductVariant v WHERE v.sku = :sku AND v.deleted = false")
    boolean existsBySku(@Param("sku") String sku);

    @Query("SELECT CASE WHEN COUNT(v) > 0 THEN true ELSE false END " +
           "FROM KitchenProductVariant v " +
           "WHERE v.sku = :sku AND v.id <> :id AND v.deleted = false")
    boolean existsBySkuAndIdNot(@Param("sku") String sku, @Param("id") UUID id);

    // ====================================
    // Find by Product
    // ====================================

    @Query("SELECT v FROM KitchenProductVariant v " +
           "WHERE v.product.id = :productId AND v.deleted = false " +
           "ORDER BY v.sortOrder ASC")
    List<KitchenProductVariant> findByProductId(@Param("productId") UUID productId);

    @Query("SELECT v FROM KitchenProductVariant v " +
           "WHERE v.product.id = :productId AND v.active = true AND v.deleted = false " +
           "ORDER BY v.sortOrder ASC")
    List<KitchenProductVariant> findActiveByProductId(@Param("productId") UUID productId);

    @Query("SELECT v FROM KitchenProductVariant v " +
           "WHERE v.product.id = :productId AND v.isDefault = true AND v.deleted = false")
    Optional<KitchenProductVariant> findDefaultByProductId(@Param("productId") UUID productId);

    // ====================================
    // Find by Colour (the key resolver)
    // ====================================

    /**
     * Resolve variant for a product given a cabinet colour.
     * Used when the global colour changes.
     */
    @Query("SELECT v FROM KitchenProductVariant v " +
           "WHERE v.product.id = :productId " +
           "AND v.cabinetColour.id = :colourId " +
           "AND v.active = true AND v.deleted = false " +
           "ORDER BY v.sortOrder ASC")
    List<KitchenProductVariant> findByProductIdAndCabinetColourId(
        @Param("productId") UUID productId,
        @Param("colourId") UUID colourId
    );

    /**
     * Resolve exact variant for product + colour + assembly type
     */
    @Query("SELECT v FROM KitchenProductVariant v " +
           "WHERE v.product.id = :productId " +
           "AND v.cabinetColour.id = :colourId " +
           "AND v.assemblyType = :assemblyType " +
           "AND v.active = true AND v.deleted = false")
    Optional<KitchenProductVariant> findByProductIdAndCabinetColourIdAndAssemblyType(
        @Param("productId") UUID productId,
        @Param("colourId") UUID colourId,
        @Param("assemblyType") AssemblyType assemblyType
    );

    // ====================================
    // Find by Assembly Type
    // ====================================

    @Query("SELECT v FROM KitchenProductVariant v " +
           "WHERE v.product.id = :productId " +
           "AND v.assemblyType = :assemblyType " +
           "AND v.active = true AND v.deleted = false")
    List<KitchenProductVariant> findByProductIdAndAssemblyType(
        @Param("productId") UUID productId,
        @Param("assemblyType") AssemblyType assemblyType
    );

    // ====================================
    // Find with Relations
    // ====================================

    @EntityGraph(attributePaths = {"product", "cabinetColour"})
    @Query("SELECT v FROM KitchenProductVariant v WHERE v.id = :id AND v.deleted = false")
    Optional<KitchenProductVariant> findByIdWithDetails(@Param("id") UUID id);

    @EntityGraph(attributePaths = {"cabinetColour"})
    @Query("SELECT v FROM KitchenProductVariant v " +
           "WHERE v.product.id = :productId AND v.deleted = false " +
           "ORDER BY v.sortOrder ASC")
    List<KitchenProductVariant> findByProductIdWithColour(@Param("productId") UUID productId);

    // ====================================
    // Stock
    // ====================================

    @Query("SELECT v FROM KitchenProductVariant v " +
           "WHERE v.product.id = :productId " +
           "AND v.inStock = true AND v.active = true AND v.deleted = false")
    List<KitchenProductVariant> findInStockByProductId(@Param("productId") UUID productId);

    @Query("SELECT v FROM KitchenProductVariant v " +
           "WHERE v.inStock = false AND v.active = true AND v.deleted = false")
    List<KitchenProductVariant> findOutOfStock();

    // ====================================
    // Statistics
    // ====================================

    @Query("SELECT COUNT(v) FROM KitchenProductVariant v " +
           "WHERE v.product.id = :productId AND v.active = true AND v.deleted = false")
    long countActiveByProductId(@Param("productId") UUID productId);

    @Query("SELECT MIN(v.price.amount) FROM KitchenProductVariant v " +
           "WHERE v.product.id = :productId AND v.active = true AND v.deleted = false")
    java.math.BigDecimal findMinPriceByProductId(@Param("productId") UUID productId);
}