package com.example.demo.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when an entity is not found
 *
 * @author Architecture Team
 * @version 1.0
 */
public class EntityNotFoundException extends BusinessException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructor with entity class and ID
     */
    public EntityNotFoundException(Class<?> entityClass, Object id) {
        super(
                HttpStatus.NOT_FOUND,
                ErrorCode.ENTITY_NOT_FOUND,
                String.format("%s with ID '%s' not found", entityClass.getSimpleName(), id),
                String.format("The requested %s does not exist in the system", entityClass.getSimpleName())
        );
    }

    /**
     * Constructor with entity name and ID
     */
    public EntityNotFoundException(String entityName, Object id) {
        super(
                HttpStatus.NOT_FOUND,
                ErrorCode.ENTITY_NOT_FOUND,
                String.format("%s with ID '%s' not found", entityName, id),
                String.format("The requested %s does not exist in the system", entityName)
        );
    }

    /**
     * Constructor with custom message
     */
    public EntityNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, ErrorCode.ENTITY_NOT_FOUND, message);
    }

    /**
     * Constructor with custom message and details
     */
    public EntityNotFoundException(String message, String details) {
        super(HttpStatus.NOT_FOUND, ErrorCode.ENTITY_NOT_FOUND, message, details);
    }

    /**
     * Constructor for not found by field
     */
    public static EntityNotFoundException byField(Class<?> entityClass, String fieldName, Object fieldValue) {
        return new EntityNotFoundException(
                String.format("%s with %s '%s' not found",
                        entityClass.getSimpleName(),
                        fieldName,
                        fieldValue)
        );
    }

    /**
     * Constructor for multiple entities not found
     */
    public static EntityNotFoundException multipleNotFound(Class<?> entityClass, int count) {
        return new EntityNotFoundException(
                String.format("%d %s(s) not found", count, entityClass.getSimpleName())
        );
    }
}