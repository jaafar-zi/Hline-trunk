package com.example.demo.common.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Detailed error information for logging and debugging
 *
 * @author Architecture Team
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorDetails {

    /**
     * Error code
     */
    private String errorCode;

    /**
     * Error message
     */
    private String message;

    /**
     * Detailed description
     */
    private String details;

    /**
     * Timestamp when error occurred
     */
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    /**
     * Request path that caused the error
     */
    private String path;

    /**
     * HTTP method
     */
    private String method;

    /**
     * User who encountered the error
     */
    private String user;

    /**
     * Request ID for tracing
     */
    private String requestId;

    /**
     * Trace ID for distributed tracing
     */
    private String traceId;

    /**
     * Additional context
     */
    private Map<String, Object> context;

    /**
     * Exception class name
     */
    private String exceptionClass;

    /**
     * Stack trace (for internal logging only)
     */
    private String stackTrace;
}