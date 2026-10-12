package com.example.demo.common.mapper;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Abstract base mapper for entity <-> DTO conversions
 *
 * Features:
 * - Bidirectional mapping (entity <-> DTO)
 * - Null-safe collection mapping
 * - Support for List, Set, and generic Collections
 * - Partial entity updates from DTOs
 * - MapStruct integration ready
 * - Thread-safe operations
 *
 * Type Parameters:
 * @param <E> Entity type
 * @param <D> DTO type
 *
 * @author Architecture Team
 * @version 1.0
 */
public interface BaseMapper<E, D> {

    // ====================================
    // Single Object Mapping
    // ====================================

    /**
     * Convert entity to DTO
     *
     * @param entity the source entity
     * @return the mapped DTO, or null if entity is null
     */
    @Nullable
    D toDto(@Nullable E entity);

    /**
     * Convert DTO to entity
     *
     * @param dto the source DTO
     * @return the mapped entity, or null if dto is null
     */
    @Nullable
    E toEntity(@Nullable D dto);

    // ====================================
    // List Mapping
    // ====================================

    /**
     * Convert list of entities to list of DTOs
     *
     * @param entities the source entity list
     * @return non-null list of DTOs (empty if input is null/empty)
     */
    @NonNull
    default List<D> toDtoList(@Nullable List<E> entities) {
        if (entities == null || entities.isEmpty()) {
            return Collections.emptyList();
        }
        return entities.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Convert list of DTOs to list of entities
     *
     * @param dtos the source DTO list
     * @return non-null list of entities (empty if input is null/empty)
     */
    @NonNull
    default List<E> toEntityList(@Nullable List<D> dtos) {
        if (dtos == null || dtos.isEmpty()) {
            return Collections.emptyList();
        }
        return dtos.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }

    // ====================================
    // Set Mapping
    // ====================================

    /**
     * Convert set of entities to set of DTOs
     *
     * @param entities the source entity set
     * @return non-null set of DTOs (empty if input is null/empty)
     */
    @NonNull
    default Set<D> toDtoSet(@Nullable Set<E> entities) {
        if (entities == null || entities.isEmpty()) {
            return Collections.emptySet();
        }
        return entities.stream()
                .map(this::toDto)
                .collect(Collectors.toSet());
    }

    /**
     * Convert set of DTOs to set of entities
     *
     * @param dtos the source DTO set
     * @return non-null set of entities (empty if input is null/empty)
     */
    @NonNull
    default Set<E> toEntitySet(@Nullable Set<D> dtos) {
        if (dtos == null || dtos.isEmpty()) {
            return Collections.emptySet();
        }
        return dtos.stream()
                .map(this::toEntity)
                .collect(Collectors.toSet());
    }

    // ====================================
    // Generic Collection Mapping
    // ====================================

    /**
     * Convert collection of entities to list of DTOs
     *
     * @param entities the source entity collection
     * @return non-null list of DTOs (empty if input is null/empty)
     */
    @NonNull
    default List<D> toDtoCollection(@Nullable Collection<E> entities) {
        if (entities == null || entities.isEmpty()) {
            return Collections.emptyList();
        }
        return entities.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Convert collection of DTOs to list of entities
     *
     * @param dtos the source DTO collection
     * @return non-null list of entities (empty if input is null/empty)
     */
    @NonNull
    default List<E> toEntityCollection(@Nullable Collection<D> dtos) {
        if (dtos == null || dtos.isEmpty()) {
            return Collections.emptyList();
        }
        return dtos.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }

    // ====================================
    // Partial Update Support
    // ====================================

    /**
     * Update existing entity with values from DTO
     * Useful for PATCH operations where only changed fields are updated
     *
     * Default implementation does nothing - override in concrete mappers
     *
     * @param dto the source DTO with updated values
     * @param entity the target entity to update
     * @return the updated entity
     */
    @NonNull
    default E updateEntityFromDto(@NonNull D dto, @NonNull E entity) {
        // Default: no-op
        // Subclasses should override for specific update logic
        return entity;
    }

    /**
     * Create a new entity or update existing based on DTO state
     * Useful for upsert operations
     *
     * @param dto the source DTO
     * @param existingEntity the existing entity (nullable)
     * @return new entity if existingEntity is null, otherwise updated entity
     */
    @NonNull
    default E toEntityOrUpdate(@NonNull D dto, @Nullable E existingEntity) {
        if (existingEntity == null) {
            return toEntity(dto);
        }
        return updateEntityFromDto(dto, existingEntity);
    }
}