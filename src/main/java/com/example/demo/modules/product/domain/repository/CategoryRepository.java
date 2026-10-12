package com.example.demo.modules.product.domain.repository;

import com.example.demo.common.base.repository.SoftDeleteRepository;
import com.example.demo.modules.product.domain.model.Category;
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
 * Category Repository
 *
 * Repository for Category entity
 * Supports hierarchical category structure
 *
 * @author PIM Team
 * @version 1.0
 */
@Repository
public interface CategoryRepository extends SoftDeleteRepository<Category, UUID> {

    // ====================================
    // Find by Code/Name
    // ====================================

    /**
     * Find category by code
     */
    @Query("SELECT c FROM Category c WHERE c.code = :code")
    Optional<Category> findByCode(@Param("code") String code);

    /**
     * Find category by name
     */
    @Query("SELECT c FROM Category c WHERE LOWER(c.name) = LOWER(:name)")
    Optional<Category> findByName(@Param("name") String name);

    /**
     * Check if code exists
     */
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Category c WHERE c.code = :code")
    boolean existsByCode(@Param("code") String code);

    /**
     * Check if name exists
     */
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Category c WHERE LOWER(c.name) = LOWER(:name)")
    boolean existsByName(@Param("name") String name);

     /**
     * Check if parent ID exists
     */
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Category c WHERE c.parent.id = :parentId AND c.deleted = false")
    boolean existsByParentId(@Param("parentId") UUID parentId);

    // ====================================
    // Hierarchical Queries
    // ====================================

    /**
     * Find root categories (no parent)
     */
    @Query("SELECT c FROM Category c WHERE c.parent IS NULL AND c.deleted = false ORDER BY c.displayOrder, c.name")
    List<Category> findRootCategories();

    /**
     * Find categories by parent ID
     */
    @Query("SELECT c FROM Category c WHERE c.parent.id = :parentId AND c.deleted = false ORDER BY c.displayOrder, c.name")
    List<Category> findByParentId(@Param("parentId") UUID parentId);

    /**
     * Find categories by parent with pagination
     */
    @Query("SELECT c FROM Category c WHERE c.parent.id = :parentId AND c.deleted = false ORDER BY c.displayOrder, c.name")
    Page<Category> findByParentId(@Param("parentId") UUID parentId, Pageable pageable);

    /**
     * Find category with children
     */
    @EntityGraph(attributePaths = {"children"})
    @Query("SELECT c FROM Category c WHERE c.id = :id")
    Optional<Category> findByIdWithChildren(@Param("id") UUID id);

    /**
     * Find category with full hierarchy
     */
    @EntityGraph(attributePaths = {"parent", "children"})
    @Query("SELECT c FROM Category c WHERE c.id = :id")
    Optional<Category> findByIdWithHierarchy(@Param("id") UUID id);

    /**
     * Count children of category
     */
    @Query("SELECT COUNT(c) FROM Category c WHERE c.parent.id = :parentId AND c.deleted = false")
    long countChildren(@Param("parentId") UUID parentId);

    /**
     * Check if category has children
     */
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Category c WHERE c.parent.id = :parentId AND c.deleted = false")
    boolean hasChildren(@Param("parentId") UUID parentId);

    // ====================================
    // Find by Status
    // ====================================

    /**
     * Find active categories
     */
    @Query("SELECT c FROM Category c WHERE c.active = true AND c.deleted = false ORDER BY c.displayOrder, c.name")
    List<Category> findAllActive();

    /**
     * Find active root categories
     */
    @Query("SELECT c FROM Category c WHERE c.active = true AND c.parent IS NULL AND c.deleted = false ORDER BY c.displayOrder, c.name")
    List<Category> findActiveRootCategories();

    /**
     * Find active categories by parent
     */
    @Query("SELECT c FROM Category c WHERE c.parent.id = :parentId AND c.active = true AND c.deleted = false ORDER BY c.displayOrder, c.name")
    List<Category> findActiveByParentId(@Param("parentId") UUID parentId);

    // ====================================
    // Statistics
    // ====================================

    /**
     * Count total categories
     */
    @Query("SELECT COUNT(c) FROM Category c WHERE c.deleted = false")
    long countAll();

    /**
     * Count active categories
     */
    @Query("SELECT COUNT(c) FROM Category c WHERE c.active = true AND c.deleted = false")
    long countActive();

    /**
     * Count root categories
     */
    @Query("SELECT COUNT(c) FROM Category c WHERE c.parent IS NULL AND c.deleted = false")
    long countRootCategories();
}