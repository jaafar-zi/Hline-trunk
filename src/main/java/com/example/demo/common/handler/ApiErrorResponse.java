package com.example.demo.common.handler;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Standard API error response structure
 *
 * Features:
 * - Consistent error format across all endpoints
 * - Support for field-level validation errors
 * - Request tracing support
 * - Environment-aware detail exposure
 *
 * @author Architecture Team
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiErrorResponse {

    /**
     * HTTP status code
     */
    private Integer status;

    /**
     * Application-specific error code
     * Used for client-side error handling
     */
    private String errorCode;

    /**
     * Short error message
     */
    private String message;

    /**
     * Detailed error description
     * Provides additional context about the error
     */
    private String details;

    /**
     * Timestamp when error occurred
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
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
     * Request ID for tracing
     * Links error to specific request in logs
     */
    private String requestId;

    /**
     * Trace ID for distributed tracing
     * Links error across multiple services
     */
    private String traceId;

    /**
     * Span ID for distributed tracing
     */
    private String spanId;

    /**
     * Field-level validation errors
     * Map of field name to error message
     */
    private Map<String, String> fieldErrors;

    /**
     * List of validation errors with detailed information
     */
    private List<ValidationError> validationErrors;

    /**
     * Stack trace (only in development mode)
     * Should be null in production for security
     */
    private List<String> stackTrace;

    /**
     * Additional metadata
     * Flexible field for context-specific information
     */
    private Map<String, Object> metadata;

    /**
     * User-friendly suggestions to fix the error
     */
    private List<String> suggestions;

    /**
     * Documentation URL for this error
     */
    private String documentationUrl;

    /**
     * Create success response (for consistency in error handlers)
     */
    public static ApiErrorResponse success(String message) {
        return ApiErrorResponse.builder()
                .status(200)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Create simple error response
     */
    public static ApiErrorResponse error(Integer status, String message) {
        return ApiErrorResponse.builder()
                .status(status)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Create error response with code
     */
    public static ApiErrorResponse error(Integer status, String errorCode, String message) {
        return ApiErrorResponse.builder()
                .status(status)
                .errorCode(errorCode)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Create full error response
     */
    public static ApiErrorResponse error(
            Integer status,
            String errorCode,
            String message,
            String details) {
        return ApiErrorResponse.builder()
                .status(status)
                .errorCode(errorCode)
                .message(message)
                .details(details)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Add field error
     */
    public ApiErrorResponse addFieldError(String field, String message) {
        if (this.fieldErrors == null) {
            this.fieldErrors = new java.util.HashMap<>();
        }
        this.fieldErrors.put(field, message);
        return this;
    }

    /**
     * Add validation error
     */
    public ApiErrorResponse addValidationError(ValidationError error) {
        if (this.validationErrors == null) {
            this.validationErrors = new java.util.ArrayList<>();
        }
        this.validationErrors.add(error);
        return this;
    }

    /**
     * Add suggestion
     */
    public ApiErrorResponse addSuggestion(String suggestion) {
        if (this.suggestions == null) {
            this.suggestions = new java.util.ArrayList<>();
        }
        this.suggestions.add(suggestion);
        return this;
    }

    /**
     * Add metadata
     */
    public ApiErrorResponse addMetadata(String key, Object value) {
        if (this.metadata == null) {
            this.metadata = new java.util.HashMap<>();
        }
        this.metadata.put(key, value);
        return this;
    }

    /**
     * Check if has field errors
     */
    public boolean hasFieldErrors() {
        return fieldErrors != null && !fieldErrors.isEmpty();
    }

    /**
     * Check if has validation errors
     */
    public boolean hasValidationErrors() {
        return validationErrors != null && !validationErrors.isEmpty();
    }

    /**
     * Get total error count
     */
    public int getTotalErrorCount() {
        int count = 0;
        if (hasFieldErrors()) {
            count += fieldErrors.size();
        }
        if (hasValidationErrors()) {
            count += validationErrors.size();
        }
        return count;
    }
}