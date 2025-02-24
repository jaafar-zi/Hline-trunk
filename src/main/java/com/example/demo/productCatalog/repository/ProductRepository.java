package com.example.demo.productCatalog.repository;

import com.example.demo.productCatalog.dto.ProductCatalogDTO;
import com.example.demo.productCatalog.dto.ProductProjection;
import com.example.demo.productCatalog.entity.Product;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {


    @Query("""
        SELECT
            p.id AS id,
            p.name AS name,
            p.stockQuantity AS stockQuantity,
            p.category.id AS categoryId
        FROM Product p
    """)
    Page<ProductProjection> findAllProductProjection(Pageable pageable);

    @Query("""
        SELECT
            p.id AS id,
            p.name AS name,
            p.stockQuantity AS stockQuantity,
            p.category.id AS categoryId
        FROM Product p
        WHERE p.category.id = :categoryId
    """)
    Optional<ProductProjection> findProductByCategoryId(Long categoryId);


    Page<Product> findByNameContainingIgnoreCase(String keyword, Pageable pageable);


    Page<Product> findByCategoryId(Long categoryId, Pageable pageable);


    List<Product> findByStockQuantityGreaterThan(int minStock);



    @Query("SELECT p FROM Product p ORDER BY :sortBy")
    Page<ProductCatalogDTO> findAllSorted(@Param("sortBy") String sortBy, Pageable pageable);

    boolean existsByName(@NotBlank(message = "Product name cannot be empty") String name);

    @Query("""
        SELECT 
            p.id AS id, 
            p.name AS name, 
            p.stockQuantity AS stockQuantity, 
            p.category.id AS categoryId 
        FROM Product p
        WHERE LOWER(p.name) LIKE LOWER(:namePattern)
    """)
    Page<ProductProjection> findByNameLikeIgnoreCase(String namePattern, Pageable pageable);
}