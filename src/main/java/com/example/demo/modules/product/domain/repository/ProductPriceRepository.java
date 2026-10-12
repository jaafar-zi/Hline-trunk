package com.example.demo.modules.product.domain.repository;

import com.example.demo.common.base.repository.SoftDeleteRepository;
import com.example.demo.modules.product.domain.model.ProductPrice;
import com.example.demo.modules.product.domain.model.vo.PriceType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Product Price Repository
 *
 * Repository for ProductPrice entity
 * Optimized for pricing queries and validations
 *
 * @author PIM Team
 * @version 1.0
 */
@Repository
public interface ProductPriceRepository extends SoftDeleteRepository<ProductPrice, UUID> {

    // ====================================
    // Find by Product
    // ====================================

    /**
     * Find all prices for a product
     */
    @Query("SELECT p FROM ProductPrice p WHERE p.product.id = :productId AND p.deleted = false ORDER BY p.priority DESC")
    List<ProductPrice> findByProductId(@Param("productId") UUID productId);

    /**
     * Find active prices for a product
     */
    @Query("SELECT p FROM ProductPrice p WHERE p.product.id = :productId AND p.active = true " +
           "AND p.deleted = false ORDER BY p.priority DESC")
    List<ProductPrice> findActiveByProductId(@Param("productId") UUID productId);

    /**
     * Count prices for product
     */
    @Query("SELECT COUNT(p) FROM ProductPrice p WHERE p.product.id = :productId AND p.deleted = false")
    long countByProductId(@Param("productId") UUID productId);

    // ====================================
    // Find by Price Type
    // ====================================

    /**
     * Find price by product and type
     */
    @Query("SELECT p FROM ProductPrice p WHERE p.product.id = :productId " +
           "AND p.priceType = :priceType AND p.deleted = false")
    Optional<ProductPrice> findByProductIdAndType(
        @Param("productId") UUID productId,
        @Param("priceType") PriceType priceType
    );

    /**
     * Find active price by product and type
     */
    @Query("SELECT p FROM ProductPrice p WHERE p.product.id = :productId " +
           "AND p.priceType = :priceType AND p.active = true AND p.deleted = false")
    Optional<ProductPrice> findActiveByProductIdAndType(
        @Param("productId") UUID productId,
        @Param("priceType") PriceType priceType
    );

    /**
     * Find base price for product
     */
    @Query("SELECT p FROM ProductPrice p WHERE p.product.id = :productId " +
           "AND p.priceType = 'BASE' AND p.active = true AND p.deleted = false")
    Optional<ProductPrice> findBasePrice(@Param("productId") UUID productId);

    /**
     * Find promotional price for product
     */
    @Query("SELECT p FROM ProductPrice p WHERE p.product.id = :productId " +
           "AND p.priceType = 'PROMOTIONAL' AND p.active = true AND p.deleted = false " +
           "ORDER BY p.priority DESC")
    Optional<ProductPrice> findPromotionalPrice(@Param("productId") UUID productId);

    /**
     * Find prices by type
     */
    @Query("SELECT p FROM ProductPrice p WHERE p.priceType = :priceType AND p.deleted = false")
    List<ProductPrice> findByPriceType(@Param("priceType") PriceType priceType);

    /**
     * Find active promotional prices
     */
    @Query("SELECT p FROM ProductPrice p WHERE p.priceType = 'PROMOTIONAL' " +
           "AND p.active = true AND p.deleted = false")
    List<ProductPrice> findActivePromotionalPrices();

    // ====================================
    // Find by Currency
    // ====================================

    /**
     * Find prices by currency
     */
    @Query("SELECT p FROM ProductPrice p WHERE p.price.currency = :currency AND p.deleted = false")
    Page<ProductPrice> findByCurrency(@Param("currency") String currency, Pageable pageable);

    /**
     * Find active prices by currency
     */
    @Query("SELECT p FROM ProductPrice p WHERE p.price.currency = :currency " +
           "AND p.active = true AND p.deleted = false")
    List<ProductPrice> findActiveByCurrency(@Param("currency") String currency);

    // ====================================
    // Find by Validity Period
    // ====================================

    /**
     * Find currently valid prices
     */
    @Query("SELECT p FROM ProductPrice p WHERE p.active = true AND p.deleted = false " +
           "AND (p.validFrom IS NULL OR p.validFrom <= :now) " +
           "AND (p.validTo IS NULL OR p.validTo >= :now)")
    List<ProductPrice> findCurrentlyValid(@Param("now") LocalDateTime now);

    /**
     * Find currently valid prices for product
     */
    @Query("SELECT p FROM ProductPrice p WHERE p.product.id = :productId " +
           "AND p.active = true AND p.deleted = false " +
           "AND (p.validFrom IS NULL OR p.validFrom <= :now) " +
           "AND (p.validTo IS NULL OR p.validTo >= :now) " +
           "ORDER BY p.priority DESC")
    List<ProductPrice> findCurrentlyValidByProductId(
        @Param("productId") UUID productId,
        @Param("now") LocalDateTime now
    );

    /**
     * Find scheduled prices (future)
     */
    @Query("SELECT p FROM ProductPrice p WHERE p.validFrom > :now AND p.deleted = false")
    List<ProductPrice> findScheduledPrices(@Param("now") LocalDateTime now);

    /**
     * Find expired prices
     */
    @Query("SELECT p FROM ProductPrice p WHERE p.validTo < :now " +
           "AND p.active = true AND p.deleted = false")
    List<ProductPrice> findExpiredPrices(@Param("now") LocalDateTime now);

    /**
     * Find prices expiring soon
     */
    @Query("SELECT p FROM ProductPrice p WHERE p.active = true AND p.deleted = false " +
           "AND p.validTo BETWEEN :now AND :expiryDate")
    List<ProductPrice> findExpiringSoon(
        @Param("now") LocalDateTime now,
        @Param("expiryDate") LocalDateTime expiryDate
    );

    // ====================================
    // Find by Customer Segment
    // ====================================

    /**
     * Find prices by customer group
     */
    @Query("SELECT p FROM ProductPrice p WHERE p.customerGroup = :customerGroup " +
           "AND p.active = true AND p.deleted = false")
    List<ProductPrice> findByCustomerGroup(@Param("customerGroup") String customerGroup);

    /**
     * Find prices by market
     */
    @Query("SELECT p FROM ProductPrice p WHERE p.market = :market " +
           "AND p.active = true AND p.deleted = false")
    List<ProductPrice> findByMarket(@Param("market") String market);

    /**
     * Find prices by customer group and market
     */
    @Query("SELECT p FROM ProductPrice p WHERE p.customerGroup = :customerGroup " +
           "AND p.market = :market AND p.active = true AND p.deleted = false")
    List<ProductPrice> findByCustomerGroupAndMarket(
        @Param("customerGroup") String customerGroup,
        @Param("market") String market
    );

    // ====================================
    // Statistics
    // ====================================

    /**
     * Count active prices
     */
    @Query("SELECT COUNT(p) FROM ProductPrice p WHERE p.active = true AND p.deleted = false")
    long countActive();

    /**
     * Count by price type
     */
    @Query("SELECT COUNT(p) FROM ProductPrice p WHERE p.priceType = :priceType AND p.deleted = false")
    long countByPriceType(@Param("priceType") PriceType priceType);

    /**
     * Count promotional prices
     */
    @Query("SELECT COUNT(p) FROM ProductPrice p WHERE p.priceType = 'PROMOTIONAL' " +
           "AND p.active = true AND p.deleted = false")
    long countActivePromotional();

    /**
     * Count expired prices
     */
    @Query("SELECT COUNT(p) FROM ProductPrice p WHERE p.validTo < :now " +
           "AND p.active = true AND p.deleted = false")
    long countExpired(@Param("now") LocalDateTime now);

    // ====================================
    // Validation
    // ====================================

    /**
     * Check if product has base price
     */
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END " +
           "FROM ProductPrice p WHERE p.product.id = :productId " +
           "AND p.priceType = 'BASE' AND p.active = true AND p.deleted = false")
    boolean hasBasePrice(@Param("productId") UUID productId);

    /**
     * Check if product has active price
     */
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END " +
           "FROM ProductPrice p WHERE p.product.id = :productId " +
           "AND p.active = true AND p.deleted = false")
    boolean hasActivePrice(@Param("productId") UUID productId);

    /**
     * Find products without base price
     */
    @Query("SELECT DISTINCT p.product.id FROM ProductPrice p WHERE p.deleted = false " +
           "AND p.product.id NOT IN (SELECT pr.product.id FROM ProductPrice pr " +
           "WHERE pr.priceType = 'BASE' AND pr.active = true AND pr.deleted = false)")
    List<UUID> findProductIdsWithoutBasePrice();

    // ====================================
    // Bulk Operations
    // ====================================

    /**
     * Find prices by product IDs
     */
    @Query("SELECT p FROM ProductPrice p WHERE p.product.id IN :productIds " +
           "AND p.active = true AND p.deleted = false")
    List<ProductPrice> findByProductIdIn(@Param("productIds") List<UUID> productIds);

    /**
     * Find base prices for multiple products
     */
    @Query("SELECT p FROM ProductPrice p WHERE p.product.id IN :productIds " +
           "AND p.priceType = 'BASE' AND p.active = true AND p.deleted = false")
    List<ProductPrice> findBasePricesByProductIds(@Param("productIds") List<UUID> productIds);
}