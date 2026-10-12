package com.example.demo.common.validator.constraints;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

import com.example.demo.common.validator.validators.SlugValidator;

/**
 * Validates URL slug format
 * 
 * Valid format: lowercase letters, numbers, and hyphens only
 * Example: electronics-and-gadgets
 * 
 * @author Architecture Team
 * @version 1.0
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = SlugValidator.class)
@Documented
public @interface ValidSlug {

    String message() default "Invalid URL slug format";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /**
     * Minimum length
     */
    int minLength() default 2;

    /**
     * Maximum length
     */
    int maxLength() default 255;

    /**
     * Allow null values
     */
    boolean allowNull() default true;
}