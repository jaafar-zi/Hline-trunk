package com.example.demo.common.validation.validator;

import com.example.demo.common.validation.result.ValidationResult;

/**
 * Base interface for all domain validators
 * 
 * @param <T> Type to validate
 * @author Architecture Team
 * @version 1.0
 */
public interface Validator<T> {

    /**
     * Validate the given object
     *
     * @param target Object to validate
     * @return Validation result
     */
    ValidationResult validate(T target);

    /**
     * Validate with context
     *
     * @param target Object to validate
     * @param context Validation context
     * @return Validation result
     */
    default ValidationResult validate(T target, ValidationContext context) {
        return validate(target);
    }

    /**
     * Check if this validator supports the given type
     *
     * @param clazz Class to check
     * @return true if supported
     */
    boolean supports(Class<?> clazz);
}