package com.example.demo.common.exception;

/**
 * Thrown when an entity with a given ID is not found in the repository.
 */
public class EntityNotFoundException extends RuntimeException {

    public EntityNotFoundException(Class<?> entityClass, Object id) {
        super(entityClass.getSimpleName() + " not found with id: " + id);
    }
}
