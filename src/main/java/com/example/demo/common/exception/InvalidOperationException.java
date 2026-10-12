package com.example.demo.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when an operation is invalid in the current state
 *
 * @author Architecture Team
 * @version 1.0
 */
public class InvalidOperationException extends BusinessException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructor with message
     */
    public InvalidOperationException(String message) {
        super(HttpStatus.CONFLICT, ErrorCode.INVALID_OPERATION, message);
    }

    /**
     * Constructor with message and details
     */
    public InvalidOperationException(String message, String details) {
        super(HttpStatus.CONFLICT, ErrorCode.INVALID_OPERATION, message, details);
    }

    /**
     * Invalid state exception
     */
    public static InvalidOperationException invalidState(String entity, String currentState, String operation) {
        return new InvalidOperationException(
                String.format("Cannot %s %s in state: %s", operation, entity, currentState),
                String.format("The %s must be in a different state to perform this operation", entity)
        );
    }

    /**
     * Already processed exception
     */
    public static InvalidOperationException alreadyProcessed(String entity) {
        return new InvalidOperationException(
                String.format("%s has already been processed", entity),
                "This operation cannot be performed on processed items"
        );
    }

    /**
     * Deadline passed exception
     */
    public static InvalidOperationException deadlinePassed(String operation) {
        return new InvalidOperationException(
                String.format("Cannot %s: deadline has passed", operation),
                "This operation is no longer available after the deadline"
        );
    }
}