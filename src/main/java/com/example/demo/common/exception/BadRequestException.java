package com.example.demo.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown for invalid request data
 *
 * @author Architecture Team
 * @version 1.0
 */
public class BadRequestException extends BusinessException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructor with message
     */
    public BadRequestException(String message) {
        super(HttpStatus.BAD_REQUEST, ErrorCode.BAD_REQUEST, message);
    }

    /**
     * Constructor with message and details
     */
    public BadRequestException(String message, String details) {
        super(HttpStatus.BAD_REQUEST, ErrorCode.BAD_REQUEST, message, details);
    }

    /**
     * Invalid parameter exception
     */
    public static BadRequestException invalidParameter(String paramName, String reason) {
        return new BadRequestException(
                String.format("Invalid parameter: %s", paramName),
                reason
        );
    }

    /**
     * Missing parameter exception
     */
    public static BadRequestException missingParameter(String paramName) {
        return new BadRequestException(
                String.format("Missing required parameter: %s", paramName),
                String.format("The parameter '%s' is required but was not provided", paramName)
        );
    }

    /**
     * Invalid format exception
     */
    public static BadRequestException invalidFormat(String fieldName, String expectedFormat) {
        return new BadRequestException(
                String.format("Invalid format for field: %s", fieldName),
                String.format("Expected format: %s", expectedFormat)
        );
    }
}