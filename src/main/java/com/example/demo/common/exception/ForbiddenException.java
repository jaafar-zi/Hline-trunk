package com.example.demo.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when user lacks permission to access a resource
 *
 * @author Architecture Team
 * @version 1.0
 */
public class ForbiddenException extends BusinessException {

    private static final long serialVersionUID = 1L;

    /**
     * Default constructor
     */
    public ForbiddenException() {
        super(
                HttpStatus.FORBIDDEN,
                ErrorCode.FORBIDDEN,
                "Access denied",
                "You do not have permission to access this resource"
        );
    }

    /**
     * Constructor with custom message
     */
    public ForbiddenException(String message) {
        super(HttpStatus.FORBIDDEN, ErrorCode.FORBIDDEN, message);
    }

    /**
     * Constructor with message and details
     */
    public ForbiddenException(String message, String details) {
        super(HttpStatus.FORBIDDEN, ErrorCode.FORBIDDEN, message, details);
    }

    /**
     * Insufficient permissions exception
     */
    public static ForbiddenException insufficientPermissions(String requiredPermission) {
        return new ForbiddenException(
                "Insufficient permissions",
                String.format("This operation requires '%s' permission", requiredPermission)
        );
    }

    /**
     * Resource ownership exception
     */
    public static ForbiddenException notResourceOwner(String resourceType) {
        return new ForbiddenException(
                "Access denied",
                String.format("You can only access your own %s", resourceType)
        );
    }

    /**
     * Role required exception
     */
    public static ForbiddenException roleRequired(String role) {
        return new ForbiddenException(
                "Insufficient privileges",
                String.format("This operation requires '%s' role", role)
        );
    }
}