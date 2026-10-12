package com.example.demo.common.handler;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Detailed validation error information
 *
 * @author Architecture Team
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ValidationError {

    /**
     * Field name that failed validation
     */
    private String field;

    /**
     * Rejected value
     */
    private Object rejectedValue;

    /**
     * Error message
     */
    private String message;

    /**
     * Validation constraint that was violated
     */
    private String constraint;

    /**
     * Error code for this specific validation
     */
    private String code;

    /**
     * Object name (class name) where validation failed
     */
    private String objectName;

    /**
     * Create simple validation error
     */
    public static ValidationError of(String field, String message) {
        return ValidationError.builder()
                .field(field)
                .message(message)
                .build();
    }

    /**
     * Create validation error with rejected value
     */
    public static ValidationError of(String field, Object rejectedValue, String message) {
        return ValidationError.builder()
                .field(field)
                .rejectedValue(rejectedValue)
                .message(message)
                .build();
    }

    /**
     * Create full validation error
     */
    public static ValidationError of(
            String field,
            Object rejectedValue,
            String message,
            String constraint) {
        return ValidationError.builder()
                .field(field)
                .rejectedValue(rejectedValue)
                .message(message)
                .constraint(constraint)
                .build();
    }
}