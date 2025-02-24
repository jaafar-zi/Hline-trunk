package com.example.demo.productCatalog.repository;

import com.example.demo.productCatalog.dto.ProductPricingDTO;
import com.example.demo.productCatalog.entity.Pricing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PricingRepository extends JpaRepository<Pricing, Long> {

    @Query("""
    SELECT new com.example.demo.productCatalog.dto.ProductPricingDTO(
        p.id, pr.id, p.currency, p.oldPrice, p.price, p.promoFlag
    ) 
    FROM Pricing p 
    JOIN p.products pr 
    WHERE pr.id IN :productIds 
    AND p.currency = :currency
    """)
    List<ProductPricingDTO> getPricingByTheProductIds(
            @Param("productIds") List<Long> productIds,
            @Param("currency") String currency
    );


    @Query("""
    SELECT p FROM Pricing p
    JOIN p.products prod
    WHERE prod.id = :productId
    """)
    List<Pricing> findByProductId(@Param("productId") Long productId);


    // Find pricing entries by currency
    List<Pricing> findByCurrency(String currency);

    @Query("""
    SELECT p FROM Pricing p
    JOIN p.products prod
    WHERE prod.id = :productId AND p.currency = :currency
    """)
    List<Pricing> findByProductIdAndCurrency(@Param("productId") Long productId, @Param("currency") String currency);


    @Query("SELECT COUNT(p) FROM Pricing p JOIN p.products pr WHERE pr.id = :productId")
    int countByProductId(@Param("productId") Long productId);

}