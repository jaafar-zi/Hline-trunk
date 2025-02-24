package com.example.demo.entityReference;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class EntityReferenceService {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Resolves an entity reference dynamically.
     *
     * @param entityType The class of the entity.
     * @param entityId The ID of the referenced entity.
     * @param <T> The entity type.
     * @return The resolved entity, or empty if not found.
     */
    public <T> Optional<T> resolveReference(Class<T> entityType, Long entityId) {
        if (entityId == null || entityType == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(entityManager.find(entityType, entityId));
    }

    /**
     * Converts an entity to an entity reference.
     *
     * @param entity The entity to convert.
     * @param <T> The entity type.
     * @return An entity reference containing the entity's class and ID.
     */
    public <T> EntityReference<T> of(T entity) {
        if (entity == null) {
            return null;
        }

        // Getting the class of the entity and its ID
        Long entityId = getEntityId(entity);
        return new EntityReference<>(entityId, (Class<T>) entity.getClass()); // Explicit cast to Class<T>
    }

    /**
     * Helper method to get the ID of the entity.
     *
     * @param entity The entity.
     * @param <T> The entity type.
     * @return The entity ID.
     */
    private <T> Long getEntityId(T entity) {
        // This assumes that the entity has a method getId(),
        // if not, it can be adjusted based on the actual entity's ID field.
        try {
            return (Long) entity.getClass().getMethod("getId").invoke(entity);
        } catch (Exception e) {
            throw new IllegalArgumentException("Entity does not have a valid ID getter method", e);
        }
    }
}
