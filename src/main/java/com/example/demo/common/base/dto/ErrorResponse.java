package com.example.demo.common.base.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Standard error response structure
 *
 * @author Architecture Team
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Error response structure")
public class ErrorResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "HTTP status code", example = "400")
    private Integer status;

    @Schema(description = "Error code for client handling", example = "VALIDATION_ERROR")
    private String errorCode;

    @Schema(description = "Error message", example = "Validation failed")
    private String message;

    @Schema(description = "Detailed error description")
    private String details;

    @Schema(description = "Error timestamp", example = "2024-01-24T10:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    @Schema(description = "Request path", example = "/api/v1/users")
    private String path;

    @Schema(description = "Field validation errors")
    private Map<String, String> fieldErrors;

    @Schema(description = "List of validation violations")
    private List<ValidationError> violations;

    @Schema(description = "Stack trace (only in development)")
    private List<String> stackTrace;

    @Schema(description = "Request ID for tracing")
    private String requestId;

    @Schema(description = "Additional error metadata")
    private Map<String, Object> metadata;

    // ====================================
    // Factory Methods
    // ====================================

    public static ErrorResponse of(Integer status, String message) {
        return ErrorResponse.builder()
                .status(status)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static ErrorResponse of(Integer status, String errorCode, String message) {
        return ErrorResponse.builder()
                .status(status)
                .errorCode(errorCode)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static ErrorResponse of(Integer status, String errorCode, String message, String details) {
        return ErrorResponse.builder()
                .status(status)
                .errorCode(errorCode)
                .message(message)
                .details(details)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static ErrorResponse validationError(Map<String, String> fieldErrors) {
        return ErrorResponse.builder()
                .status(400)
                .errorCode("VALIDATION_ERROR")
                .message("Validation failed")
                .fieldErrors(fieldErrors)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static ErrorResponse validationError(List<ValidationError> violations) {
        return ErrorResponse.builder()
                .status(400)
                .errorCode("VALIDATION_ERROR")
                .message("Validation failed")
                .violations(violations)
                .timestamp(LocalDateTime.now())
                .build();
    }

    // ====================================
    // Helper Methods
    // ====================================

    public ErrorResponse withPath(String path) {
        this.path = path;
        return this;
    }

    public ErrorResponse withRequestId(String requestId) {
        this.requestId = requestId;
        return this;
    }

    public ErrorResponse withStackTrace(List<String> stackTrace) {
        this.stackTrace = stackTrace;
        return this;
    }

    public ErrorResponse addFieldError(String field, String error) {
        if (this.fieldErrors == null) {
            this.fieldErrors = new HashMap<>();
        }
        this.fieldErrors.put(field, error);
        return this;
    }

    public ErrorResponse addViolation(String field, String message, Object rejectedValue) {
        if (this.violations == null) {
            this.violations = new ArrayList<>();
        }
        this.violations.add(new ValidationError(field, message, rejectedValue));
        return this;
    }

    public ErrorResponse addMetadata(String key, Object value) {
        if (this.metadata == null) {
            this.metadata = new HashMap<>();
        }
        this.metadata.put(key, value);
        return this;
    }

    // ====================================
    // Inner Class
    // ====================================

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Validation error details")
    public static class ValidationError implements Serializable {

        private static final long serialVersionUID = 1L;

        @Schema(description = "Field name", example = "email")
        private String field;

        @Schema(description = "Error message", example = "must be a valid email address")
        private String message;

        @Schema(description = "Rejected value")
        private Object rejectedValue;

        @Schema(description = "Error code", example = "Email")
        private String code;

        public ValidationError(String field, String message, Object rejectedValue) {
            this.field = field;
            this.message = message;
            this.rejectedValue = rejectedValue;
        }
    }
}