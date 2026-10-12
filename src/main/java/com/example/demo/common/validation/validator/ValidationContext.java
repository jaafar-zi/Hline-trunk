package com.example.demo.common.validation.validator;

import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import java.util.Map;

/**
 * Validation context
 * 
 * Carries additional information needed during validation
 * 
 * @author Architecture Team
 * @version 1.0
 */
@Data
@Builder
public class ValidationContext {

    /**
     * Operation type (CREATE, UPDATE, DELETE, etc.)
     */
    private String operation;

    /**
     * User performing the operation
     */
    private String user;

    /**
     * Validation mode (STRICT, LENIENT)
     */
    @Builder.Default
    private ValidationMode mode = ValidationMode.NORMAL;

    /**
     * Skip specific validators
     */
    @Singular
    private java.util.Set<String> skipValidators;

    /**
     * Additional context data
     */
    @Singular
    private Map<String, Object> attributes;

    public enum ValidationMode {
        STRICT,
        NORMAL,
        LENIENT
    }

    public static ValidationContext create() {
        return ValidationContext.builder().build();
    }

    public static ValidationContext strict() {
        return ValidationContext.builder()
                .mode(ValidationMode.STRICT)
                .build();
    }

    public static ValidationContext lenient() {
        return ValidationContext.builder()
                .mode(ValidationMode.LENIENT)
                .build();
    }

    public boolean shouldSkip(String validatorName) {
        return skipValidators != null && skipValidators.contains(validatorName);
    }

    public Object getAttribute(String key) {
        return attributes != null ? attributes.get(key) : null;
    }

    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String key, Class<T> type) {
        Object value = getAttribute(key);
        return value != null && type.isInstance(value) ? (T) value : null;
    }
}