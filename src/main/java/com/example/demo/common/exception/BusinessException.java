package com.example.demo.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Base exception for all business logic errors
 *
 * Features:
 * - HTTP status code
 * - Error code for client handling
 * - Detailed error message
 * - Support for additional context
 *
 * @author Architecture Team
 * @version 1.0
 */
@Getter
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * HTTP status code
     */
    private final HttpStatus httpStatus;

    /**
     * Application-specific error code
     */
    private final String errorCode;

    /**
     * Detailed error message
     */
    private final String details;

    /**
     * Additional context/metadata
     */
    private final Object[] args;

    /**
     * Constructor with message only
     */
    public BusinessException(String message) {
        super(message);
        this.httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        this.errorCode = ErrorCode.INTERNAL_ERROR;
        this.details = null;
        this.args = null;
    }

    /**
     * Constructor with message and cause
     */
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        this.errorCode = ErrorCode.INTERNAL_ERROR;
        this.details = null;
        this.args = null;
    }

    /**
     * Constructor with HTTP status and message
     */
    public BusinessException(HttpStatus httpStatus, String message) {
        super(message);
        this.httpStatus = httpStatus;
        this.errorCode = ErrorCode.BUSINESS_ERROR;
        this.details = null;
        this.args = null;
    }

    /**
     * Constructor with HTTP status, error code, and message
     */
    public BusinessException(HttpStatus httpStatus, String errorCode, String message) {
        super(message);
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
        this.details = null;
        this.args = null;
    }

    /**
     * Full constructor with details
     */
    public BusinessException(HttpStatus httpStatus, String errorCode, String message, String details) {
        super(message);
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
        this.details = details;
        this.args = null;
    }

    /**
     * Full constructor with cause
     */
    public BusinessException(HttpStatus httpStatus, String errorCode, String message, String details, Throwable cause) {
        super(message, cause);
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
        this.details = details;
        this.args = null;
    }

    /**
     * Constructor with arguments for message formatting
     */
    public BusinessException(HttpStatus httpStatus, String errorCode, String message, Object... args) {
        super(message);
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
        this.details = null;
        this.args = args;
    }

    /**
     * Full constructor with all parameters
     */
    public BusinessException(HttpStatus httpStatus, String errorCode, String message, String details, Object[] args) {
        super(message);
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
        this.details = details;
        this.args = args;
    }

    /**
     * Full constructor with all parameters and cause
     */
    public BusinessException(HttpStatus httpStatus, String errorCode, String message, String details, Object[] args, Throwable cause) {
        super(message, cause);
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
        this.details = details;
        this.args = args;
    }

    /**
     * Get HTTP status code as integer
     */
    public int getStatusCode() {
        return httpStatus.value();
    }

    /**
     * Get formatted error message
     */
    public String getFormattedMessage() {
        if (args != null && args.length > 0) {
            try {
                return String.format(getMessage(), args);
            } catch (Exception e) {
                // If formatting fails, return original message
                return getMessage();
            }
        }
        return getMessage();
    }

    /**
     * Get details or message if details is null
     */
    public String getDetailsOrMessage() {
        return details != null ? details : getMessage();
    }

    /**
     * Check if has details
     */
    public boolean hasDetails() {
        return details != null && !details.isEmpty();
    }

    /**
     * Check if has args
     */
    public boolean hasArgs() {
        return args != null && args.length > 0;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName())
                .append(": [")
                .append(errorCode)
                .append("] ")
                .append(getFormattedMessage());

        if (hasDetails()) {
            sb.append(" - Details: ").append(details);
        }

        return sb.toString();
    }
}