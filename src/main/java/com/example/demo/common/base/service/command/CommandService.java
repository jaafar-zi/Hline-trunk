package com.example.demo.common.base.service.command;

import java.util.List;

/**
 * Command service interface for write operations
 * Part of CQRS pattern
 *
 * @param <T> Entity type
 * @param <ID> ID type
 *
 * @author Architecture Team
 * @version 1.0
 */
public interface CommandService<T, ID> {

    /**
     * Create new entity
     *
     * @param entity Entity to create
     * @return Created entity
     */
    T create(T entity);

    /**
     * Update existing entity
     *
     * @param id Entity ID
     * @param entity Updated entity data
     * @return Updated entity
     */
    T update(ID id, T entity);

    /**
     * Delete entity by ID
     *
     * @param id Entity ID
     */
    void delete(ID id);

    /**
     * Delete multiple entities
     *
     * @param ids List of entity IDs
     */
    void deleteAll(List<ID> ids);

    /**
     * Bulk create entities
     *
     * @param entities List of entities to create
     * @return List of created entities
     */
    List<T> createAll(List<T> entities);

    /**
     * Bulk update entities
     *
     * @param entities List of entities to update
     * @return List of updated entities
     */
    List<T> updateAll(List<T> entities);
}