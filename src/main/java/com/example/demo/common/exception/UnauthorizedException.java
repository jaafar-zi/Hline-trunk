package com.example.demo.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when user is not authenticated
 *
 * @author Architecture Team
 * @version 1.0
 */
public class UnauthorizedException extends BusinessException {

    private static final long serialVersionUID = 1L;

    /**
     * Default constructor
     */
    public UnauthorizedException() {
        super(
                HttpStatus.UNAUTHORIZED,
                ErrorCode.UNAUTHORIZED,
                "Authentication required",
                "You must be authenticated to access this resource"
        );
    }

    /**
     * Constructor with custom message
     */
    public UnauthorizedException(String message) {
        super(HttpStatus.UNAUTHORIZED, ErrorCode.UNAUTHORIZED, message);
    }

    /**
     * Constructor with message and details
     */
    public UnauthorizedException(String message, String details) {
        super(HttpStatus.UNAUTHORIZED, ErrorCode.UNAUTHORIZED, message, details);
    }

    /**
     * Invalid credentials exception
     */
    public static UnauthorizedException invalidCredentials() {
        return new UnauthorizedException(
                "Invalid credentials",
                "The username or password you entered is incorrect"
        );
    }

    /**
     * Token expired exception
     */
    public static UnauthorizedException tokenExpired() {
        return new UnauthorizedException(
                "Authentication token expired",
                "Your session has expired. Please login again"
        );
    }

    /**
     * Invalid token exception
     */
    public static UnauthorizedException invalidToken() {
        return new UnauthorizedException(
                "Invalid authentication token",
                "The authentication token is invalid or malformed"
        );
    }
}