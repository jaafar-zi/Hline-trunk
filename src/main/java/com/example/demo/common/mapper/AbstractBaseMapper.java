package com.example.demo.common.mapper;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

/**
 * Abstract base implementation of BaseMapper
 * Provides template for mappers that need to extend rather than implement
 *
 * Use this when you need:
 * - Shared state across mapper methods
 * - Common initialization logic
 * - Helper methods not in the interface
 *
 * @param <E> Entity type
 * @param <D> DTO type
 *
 * @author Architecture Team
 * @version 1.0
 */
public abstract class AbstractBaseMapper<E, D> implements BaseMapper<E, D> {

    /**
     * Optional: Add reference to other mappers for complex mapping scenarios
     * Example: private final AddressMapper addressMapper;
     */

    /**
     * Template method for custom entity validation before mapping
     * Override in subclasses if validation needed
     *
     * @param entity the entity to validate
     * @throws IllegalArgumentException if validation fails
     */
    protected void validateEntity(@Nullable E entity) {
        // Default: no validation
        // Subclasses can override
    }

    /**
     * Template method for custom DTO validation before mapping
     * Override in subclasses if validation needed
     *
     * @param dto the DTO to validate
     * @throws IllegalArgumentException if validation fails
     */
    protected void validateDto(@Nullable D dto) {
        // Default: no validation
        // Subclasses can override
    }

    /**
     * Hook method called after entity to DTO mapping
     * Useful for post-processing or audit logging
     *
     * @param entity the source entity
     * @param dto the resulting DTO
     */
    protected void afterToDto(@Nullable E entity, @Nullable D dto) {
        // Default: no-op
        // Subclasses can override for logging, etc.
    }

    /**
     * Hook method called after DTO to entity mapping
     * Useful for setting default values or computed fields
     *
     * @param dto the source DTO
     * @param entity the resulting entity
     */
    protected void afterToEntity(@Nullable D dto, @Nullable E entity) {
        // Default: no-op
        // Subclasses can override
    }

    /**
     * Helper method to check if entity has ID (is persisted)
     * Override based on your ID strategy
     *
     * @param entity the entity to check
     * @return true if entity appears to be persisted
     */
    protected boolean isPersisted(@Nullable E entity) {
        // Default implementation - override in subclasses
        return entity != null;
    }

    /**
     * Helper method for null-safe string mapping
     */
    protected String mapString(@Nullable String value) {
        return value != null ? value.trim() : null;
    }

    /**
     * Helper method for null-safe string mapping with default
     */
    protected String mapStringWithDefault(@Nullable String value, @NonNull String defaultValue) {
        return value != null && !value.trim().isEmpty() ? value.trim() : defaultValue;
    }
}