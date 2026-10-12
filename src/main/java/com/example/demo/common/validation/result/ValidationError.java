package com.example.demo.common.validation.result;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Validation error (blocking issue)
 * 
 * @author Architecture Team
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidationError {

    /**
     * Field name (dot notation for nested fields)
     */
    private String field;

    /**
     * Error code (for i18n lookup)
     */
    private String code;

    /**
     * Human-readable error message
     */
    private String message;

    /**
     * Rejected value
     */
    private Object rejectedValue;

    /**
     * Error severity
     */
    @Builder.Default
    private ErrorSeverity severity = ErrorSeverity.ERROR;

    /**
     * Additional error metadata
     */
    private java.util.Map<String, Object> metadata;

    // ====================================
    // Factory Methods
    // ====================================

    public static ValidationError of(String field, String message) {
        return ValidationError.builder()
                .field(field)
                .message(message)
                .build();
    }

    public static ValidationError of(String field, String message, Object rejectedValue) {
        return ValidationError.builder()
                .field(field)
                .message(message)
                .rejectedValue(rejectedValue)
                .build();
    }

    public static ValidationError of(String field, String code, String message) {
        return ValidationError.builder()
                .field(field)
                .code(code)
                .message(message)
                .build();
    }

    public static ValidationError of(String field, String code, String message, Object rejectedValue) {
        return ValidationError.builder()
                .field(field)
                .code(code)
                .message(message)
                .rejectedValue(rejectedValue)
                .build();
    }

    public enum ErrorSeverity {
        ERROR,
        CRITICAL
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (field != null) {
            sb.append(field).append(": ");
        }
        sb.append(message);
        if (rejectedValue != null) {
            sb.append(" (rejected value: ").append(rejectedValue).append(")");
        }
        return sb.toString();
    }
}