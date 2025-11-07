package com.example.demo.domain.productManagementDomain.repository;

import com.example.demo.domain.productManagementDomain.dto.ProductAttributeDTO;
import com.example.demo.domain.productManagementDomain.entity.ProductAttribute;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for managing {@link ProductAttribute} entities.
 * This interface extends JpaRepository to provide basic CRUD operations
 * and includes custom queries for retrieving and updating product attributes.
 */
@Repository
public interface ProductAttributeRepository extends JpaRepository<ProductAttribute, Long> {

    // ───────────────────────────────────────────────
    //  FINDING DTOs
    // ───────────────────────────────────────────────

    @Query("""
        SELECT new com.example.demo.productManagementDomain.dto.ProductAttributeDTO(
            a.id, p.id, a.attributeName, a.lang, a.attributeValue)
        FROM ProductAttribute a
        JOIN a.products p
        WHERE p.id IN :productIds AND a.lang = :lang
    """)
    List<ProductAttributeDTO> getAttributesByProductIds(@Param("productIds") List<Long> productIds, @Param("lang") String lang);

    @Query("""
        SELECT new com.example.demo.productManagementDomain.dto.ProductAttributeDTO(
            a.id, p.id, a.attributeName, a.lang, a.attributeValue)
        FROM ProductAttribute a
        JOIN a.products p
        WHERE p.id = :productId AND a.lang = :lang
    """)
    List<ProductAttributeDTO> findProductAttributesByProductIdAndLang(@Param("productId") Long productId, @Param("lang") String lang);

    // ───────────────────────────────────────────────
    //  RELATIONAL FETCHING
    // ───────────────────────────────────────────────

    @Query("""
        SELECT a
        FROM ProductAttribute a
        JOIN a.products p
        WHERE p.id = :productId
    """)
    List<ProductAttribute> findByProductId(@Param("productId") Long productId);

    @Query("""
        SELECT pa FROM ProductAttribute pa 
        WHERE :productId NOT IN (
            SELECT p.id FROM pa.products p
        )
    """)
    Page<ProductAttribute> findAttributesNotAssignedToProduct(@Param("productId") Long productId, Pageable pageable);

    @Query("""
        SELECT a
        FROM ProductAttribute a
        JOIN a.products p
        WHERE p.id = :productId AND a.attributeName = :attributeName
    """)
    List<ProductAttribute> findByProductIdAndAttributeName(
            @Param("productId") Long productId,
            @Param("attributeName") String attributeName
    );

    // ───────────────────────────────────────────────
    //  SIMPLE QUERIES
    // ───────────────────────────────────────────────

    List<ProductAttribute> findByAttributeName(String attributeName);

    List<ProductAttribute> findByAttributeNameAndAttributeValue(String attributeName, String attributeValue);

    // ───────────────────────────────────────────────
    //  UPDATE
    // ───────────────────────────────────────────────

    @Modifying
    @Query("""
        UPDATE ProductAttribute a
        SET a.attributeName = :attributeName, a.attributeValue = :attributeValue
        WHERE a.id = :id
    """)
    void updateProductAttribute(
            @Param("id") Long id,
            @Param("attributeName") String attributeName,
            @Param("attributeValue") String attributeValue
    );

    @Query("""
        SELECT COUNT(a)
        FROM ProductAttribute a
        JOIN a.products p
        WHERE p.id = :productId
    """)
    int countByProductId(@Param("productId") Long productId);
}


