package com.example.demo.domain.productManagementDomain.repository;

import com.example.demo.domain.productManagementDomain.dto.ProductCatalogDTO;
import com.example.demo.domain.productManagementDomain.entity.Product;
import com.example.demo.domain.productManagementDomain.projection.ProductProjection;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing {@link Product} entities.
 * This interface extends JpaRepository to provide basic CRUD operations
 * and includes custom queries for retrieving and searching products.
 */
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * Retrieves a product projection by its ID.
     *
     * @param id The ID of the product.
     * @return An {@link Optional} containing {@link ProductProjection} if found, otherwise empty.
     */
    @Query("""
        SELECT
            p.id AS id,
            p.name AS name,
            p.stockQuantity AS stockQuantity,
            p.category.id AS categoryId
        FROM Product p
        WHERE p.id = :productId
    """)
    Optional<ProductProjection> findProductProjectionById(@Param("productId") Long id);

    /**
     * Retrieves all products as projections in a paginated format.
     *
     * @param pageable Pagination information.
     * @return A {@link Page} containing {@link ProductProjection} objects.
     */
    @Query("""
        SELECT
            p.id AS id,
            p.name AS name,
            p.stockQuantity AS stockQuantity,
            p.category.id AS categoryId
        FROM Product p
    """)
    Page<ProductProjection> findAllProductProjection(Pageable pageable);

    /**
     * Retrieves a product projection by category ID.
     *
     * @param categoryId The ID of the category.
     * @return An {@link Optional} containing {@link ProductProjection} if found, otherwise empty.
     */
    @Query("""
        SELECT
            p.id AS id,
            p.name AS name,
            p.stockQuantity AS stockQuantity,
            p.category.id AS categoryId
        FROM Product p
        WHERE p.category.id = :categoryId
    """)
    Optional<ProductProjection> findProductByCategoryId(@Param("categoryId") Long categoryId);

    /**
     * Searches for products whose names contain the given keyword (case-insensitive).
     *
     * @param keyword  The search keyword.
     * @param pageable Pagination information.
     * @return A paginated list of products matching the keyword.
     */
    Page<Product> findByNameContainingIgnoreCase(String keyword, Pageable pageable);

    /**
     * Retrieves all products that belong to a specific category in a paginated format.
     *
     * @param categoryId The ID of the category.
     * @param pageable   Pagination information.
     * @return A paginated list of products in the given category.
     */
    Page<Product> findByCategoryId(Long categoryId, Pageable pageable);

    /**
     * Finds all products that have a stock quantity greater than a specified minimum value.
     *
     * @param minStock The minimum stock quantity.
     * @return A list of products that have more stock than the given value.
     */
    List<Product> findByStockQuantityGreaterThan(int minStock);

    /**
     * Retrieves all products sorted dynamically based on a given attribute.
     *
     * @param sortBy   The attribute to sort by.
     * @param pageable Pagination information.
     * @return A paginated list of {@link ProductCatalogDTO} sorted by the specified field.
     */
    @Query("SELECT p FROM Product p ORDER BY :sortBy")
    Page<ProductCatalogDTO> findAllSorted(@Param("sortBy") String sortBy, Pageable pageable);

    /**
     * Checks if a product with the given name already exists.
     *
     * @param name The name of the product.
     * @return True if a product with the given name exists, otherwise false.
     */
    boolean existsByName(@NotBlank(message = "Product name cannot be empty") String name);

    /**
     * Searches for products whose names match a given pattern (case-insensitive).
     *
     * @param namePattern The name pattern to search for.
     * @param pageable    Pagination information.
     * @return A paginated list of {@link ProductProjection} matching the name pattern.
     */
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
