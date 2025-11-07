package com.example.demo.domain.productManagementDomain.repository;

import com.example.demo.domain.productManagementDomain.entity.Pricing;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface PricingRepository extends JpaRepository<Pricing, Long> {

    @Query("""
    SELECT COUNT(p)
    FROM Pricing p
    JOIN p.products pr
    WHERE pr.id = :productId
""")
    int countByProductId(@Param("productId") Long productId);

    @Query("""
        SELECT DISTINCT p FROM Pricing p
        LEFT JOIN FETCH p.products pr
        WHERE p.id = :id
    """)
    Optional<Pricing> findByIdWithProducts(@Param("id") Long id);

    @Query("""
        SELECT DISTINCT p FROM Pricing p
        LEFT JOIN FETCH p.products pr
        WHERE pr.id = :productId
    """)
    List<Pricing> findByProductIdWithProducts(@Param("productId") Long productId);

    @Query("""
        SELECT DISTINCT p FROM Pricing p
        LEFT JOIN FETCH p.products pr
        WHERE p.currency = :currency
    """)
    List<Pricing> findByCurrencyWithProducts(@Param("currency") String currency);

    @Query("""
        SELECT DISTINCT p FROM Pricing p
        LEFT JOIN FETCH p.products pr
        WHERE pr.id = :productId AND p.currency = :currency
    """)
    Optional<Pricing> findByProductIdAndCurrencyWithProducts(
            @Param("productId") Long productId,
            @Param("currency") String currency
    );

    @Query("""
        SELECT DISTINCT p FROM Pricing p
        LEFT JOIN FETCH p.products pr
        WHERE pr.id IN :productIds AND p.currency = :currency
    """)
    List<Pricing> findByProductIdsAndCurrencyWithProducts(
            @Param("productIds") List<Long> productIds,
            @Param("currency") String currency
    );

    @Query("""
        SELECT p FROM Pricing p
        WHERE :productId NOT IN (
            SELECT pr.id FROM p.products pr
        )
    """)
    Page<Pricing> findPricingsNotAssignedToProduct(@Param("productId") Long productId, Pageable pageable);

    @Modifying
    @Transactional
    @Query("""
        UPDATE Pricing p
        SET p.currency = :currency,
            p.oldPrice = :oldPrice,
            p.price = :price,
            p.promoFlag = :promoFlag
        WHERE p.id = :id
    """)
    int updatePricingById(
            @Param("id") Long id,
            @Param("currency") String currency,
            @Param("oldPrice") Double oldPrice,
            @Param("price") Double price,
            @Param("promoFlag") boolean promoFlag
    );
}

