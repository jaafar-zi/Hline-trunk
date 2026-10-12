package com.example.demo.common.base.service.query;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * Query service interface for read operations
 * Part of CQRS pattern
 *
 * @param <T> Entity type
 * @param <ID> ID type
 *
 * @author Architecture Team
 * @version 1.0
 */
public interface QueryService<T, ID> {

    /**
     * Find entity by ID (cached)
     *
     * @param id Entity ID
     * @return Entity
     */
    T findById(ID id);

    /**
     * Find entity by ID as Optional (non-cached)
     *
     * @param id Entity ID
     * @return Optional of entity
     */
    Optional<T> findByIdOptional(ID id);

    /**
     * Find all entities (cached)
     *
     * @return List of all entities
     */
    List<T> findAll();

    /**
     * Find all entities with pagination (cached)
     *
     * @param pageable Pagination info
     * @return Page of entities
     */
    Page<T> findAll(Pageable pageable);

    /**
     * Check if entity exists by ID
     *
     * @param id Entity ID
     * @return true if exists
     */
    boolean existsById(ID id);

    /**
     * Count all entities
     *
     * @return Total count
     */
    long count();

    /**
     * Find entities by IDs
     *
     * @param ids List of IDs
     * @return List of entities
     */
    List<T> findAllById(List<ID> ids);
}