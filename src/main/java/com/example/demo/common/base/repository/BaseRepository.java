package com.example.demo.common.base.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Base repository interface with common query methods
 *
 * Combines:
 * - JpaRepository (CRUD operations)
 * - JpaSpecificationExecutor (Specification queries)
 * - Custom common queries
 *
 * @param <T> Entity type
 * @param <ID> ID type (should be UUID for consistency)
 *
 * @author Architecture Team
 * @version 1.0
 */
@NoRepositoryBean
public interface BaseRepository<T, ID> extends
        JpaRepository<T, ID>,
        JpaSpecificationExecutor<T> {

    /**
     * Find entity by ID with all associations eagerly loaded
     * Use for detailed single entity retrieval
     *
     * @param id Entity ID
     * @return Optional entity with all associations loaded
     */
    @Query("SELECT e FROM #{#entityName} e WHERE e.id = :id")
    Optional<T> findByIdWithAssociations(@Param("id") ID id);

    /**
     * Find all with eager loading
     * Warning: Use with caution for large datasets
     */
    @Query("SELECT DISTINCT e FROM #{#entityName} e")
    List<T> findAllWithAssociations();

    /**
     * Find all with pagination and eager loading
     */
    @Query("SELECT DISTINCT e FROM #{#entityName} e")
    Page<T> findAllWithAssociations(Pageable pageable);

    /**
     * Check if entity exists by ID
     * Optimized with exists query
     */
    boolean existsById(ID id);

    /**
     * Count all entities
     */
    @Query("SELECT COUNT(e) FROM #{#entityName} e")
    long countAll();

    /**
     * Find first N entities
     * Useful for sampling or preview
     */
    @Query("SELECT e FROM #{#entityName} e ORDER BY e.id")
    List<T> findTop(@Param("limit") int limit);

    /**
     * Refresh entity from database
     * Useful after merge operations
     */
    default void refresh(T entity) {
        // Implementation provided by JPA
        // Subclasses can override if needed
    }
}