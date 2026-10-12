package com.example.demo.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when attempting to create a duplicate entity
 *
 * @author Architecture Team
 * @version 1.0
 */
public class DuplicateEntityException extends BusinessException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructor with entity class and field
     */
    public DuplicateEntityException(Class<?> entityClass, String field, Object value) {
        super(
                HttpStatus.CONFLICT,
                ErrorCode.DUPLICATE_ENTITY,
                String.format("%s with %s '%s' already exists",
                        entityClass.getSimpleName(),
                        field,
                        value),
                String.format("A %s with the same %s already exists in the system",
                        entityClass.getSimpleName(),
                        field)
        );
    }

    /**
     * Constructor with entity name and field
     */
    public DuplicateEntityException(String entityName, String field, Object value) {
        super(
                HttpStatus.CONFLICT,
                ErrorCode.DUPLICATE_ENTITY,
                String.format("%s with %s '%s' already exists", entityName, field, value)
        );
    }

    /**
     * Constructor with custom message
     */
    public DuplicateEntityException(String message) {
        super(HttpStatus.CONFLICT, ErrorCode.DUPLICATE_ENTITY, message);
    }

    /**
     * Constructor with multiple fields
     */
    public static DuplicateEntityException withMultipleFields(Class<?> entityClass, String... fields) {
        String fieldList = String.join(", ", fields);
        return new DuplicateEntityException(
                String.format("%s with combination of %s already exists",
                        entityClass.getSimpleName(),
                        fieldList)
        );
    }
}