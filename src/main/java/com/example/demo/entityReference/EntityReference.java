package com.example.demo.entityReference;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Optional;

@Getter
@Setter
@NoArgsConstructor
public class EntityReference<T> {
    private Long entityId; // ID of the entity
    private Class<T> entityClass; // Class type of the entity

    // ✅ Explicit constructor for JPQL queries
    public EntityReference(Long entityId, Class<T> entityClass) {
        this.entityId = entityId;
        this.entityClass = entityClass;
    }

    /**
     * Resolves the entity reference by querying the service.
     *
     * @param referenceService The service to resolve the reference.
     * @return The resolved entity wrapped in an Optional, or an empty Optional if not found.
     */
    public Optional<T> resolve(EntityReferenceService referenceService) {
        return referenceService.resolveReference(entityClass, entityId);
    }

    @Override
    public String toString() {
        return "EntityReference{" +
                "id=" + entityId +
                ", entityClass=" + entityClass.getSimpleName() +
                '}';
    }
}
