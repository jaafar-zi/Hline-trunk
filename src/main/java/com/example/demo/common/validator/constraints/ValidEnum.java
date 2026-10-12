package com.example.demo.common.validator.constraints;

import com.example.demo.common.validator.validators.ValidEnumValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Validates that a string value is a valid enum constant
 *
 * @author Architecture Team
 * @version 1.0
 */
@Documented
@Constraint(validatedBy = ValidEnumValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER,ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidEnum {

    String message() default "Invalid enum value";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /**
     * Enum class
     */
    Class<? extends Enum<?>> enumClass();

    /**
     * Case-insensitive comparison
     */
    boolean ignoreCase() default false;

    /**
     * Allow null values
     */
    boolean allowNull() default false;
}