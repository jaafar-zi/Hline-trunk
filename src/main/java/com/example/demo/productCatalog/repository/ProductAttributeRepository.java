package com.example.demo.productCatalog.repository;

import com.example.demo.productCatalog.dto.ProductAttributeDTO;
import com.example.demo.productCatalog.entity.ProductAttribute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductAttributeRepository extends JpaRepository<ProductAttribute, Long> {

    @Query("SELECT new com.example.demo.productCatalog.dto.ProductAttributeDTO(a.id, p.id, a.attributeName, a.attributeValue) " +
            "FROM ProductAttribute a JOIN a.products p " +
            "WHERE p.id IN :productIds AND a.lang = :lang")
    List<ProductAttributeDTO> getAttributesByProductIds(@Param("productIds") List<Long> productIds, @Param("lang") String lang);

    // Corrected method to find all attributes for a specific product
    @Query("SELECT a FROM ProductAttribute a JOIN a.products p WHERE p.id = :productId")
    List<ProductAttribute> findByProductId(@Param("productId") Long productId);

    // method to get product attributes by id and also the language
    @Query("SELECT a FROM ProductAttribute a JOIN a.products p WHERE p.id = :productId  AND a.lang = :lang")
    List<ProductAttributeDTO> findByProductIdAndLang(@Param("productId") Long productId,@Param("lang") String lang);

    // Find attributes by attribute name
    List<ProductAttribute> findByAttributeName(String attributeName);

    // Find attributes by attribute name and value
    List<ProductAttribute> findByAttributeNameAndAttributeValue(String attributeName, String attributeValue);

    // Corrected method to find attributes by product ID and attribute name
    @Query("SELECT a FROM ProductAttribute a JOIN a.products p WHERE p.id = :productId AND a.attributeName = :attributeName")
    List<ProductAttribute> findByProductIdAndAttributeName(@Param("productId") Long productId, @Param("attributeName") String attributeName);

    @Query("SELECT COUNT(a) FROM ProductAttribute a JOIN a.products p WHERE p.id = :productId")
    int countByProductId(@Param("productId") Long productId);
}

