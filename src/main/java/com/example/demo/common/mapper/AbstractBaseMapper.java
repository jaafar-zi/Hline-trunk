package com.example.demo.common.mapper;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Abstract base class for entity <-> DTO mapping.
 * Provides default list mapping methods and structure for specific mappers.
 */
public abstract class AbstractBaseMapper<E, D> {

    /**
     * Convert a single entity to its corresponding DTO.
     */
    public abstract D toDto(E entity);

    /**
     * Convert a DTO to its corresponding entity.
     */
    public abstract E toEntity(D dto);

    /**
     * Convert a list of entities to a list of DTOs.
     */
    public List<D> toDtoList(List<E> entities) {
        if (entities == null) return List.of();
        return entities.stream()
                .filter(Objects::nonNull)
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Convert a list of DTOs to a list of entities.
     */
    public List<E> toEntityList(List<D> dtos) {
        if (dtos == null) return List.of();
        return dtos.stream()
                .filter(Objects::nonNull)
                .map(this::toEntity)
                .collect(Collectors.toList());
    }

    /**
     * Optionally override this in child mappers
     * for custom partial updates.
     */
    public void updateEntityFromDto(D dto, E entity) {
        // Default: do nothing
        // Subclasses can override if needed.
    }
}
