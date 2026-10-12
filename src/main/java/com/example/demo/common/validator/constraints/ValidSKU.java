package com.example.demo.common.validator.constraints;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

import com.example.demo.common.validator.validators.SKUValidator;

/**
 * Validates SKU format
 * 
 * Valid formats:
 * - Auto-generated: PROD-20240115-A1B2C3
 * - With prefix: ELEC-20240115-A1B2C3
 * - Custom: CUSTOM-SKU-123
 * 
 * @author Architecture Team
 * @version 1.0
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = SKUValidator.class)
@Documented
public @interface ValidSKU {

    String message() default "Invalid SKU format";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /**
     * Minimum length
     */
    int minLength() default 5;

    /**
     * Maximum length
     */
    int maxLength() default 50;

    /**
     * Required prefix (optional)
     */
    String prefix() default "";

    /**
     * Allow null values
     */
    boolean allowNull() default true;

    /**
     * Pattern to match (optional, overrides default pattern)
     */
    String pattern() default "";
}