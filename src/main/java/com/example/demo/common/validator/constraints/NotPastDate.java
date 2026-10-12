package com.example.demo.common.validator.constraints;

import com.example.demo.common.validator.DateValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Validates that a date is not in the past
 *
 * @author Architecture Team
 * @version 1.0
 */
@Documented
@Constraint(validatedBy = DateValidator.NotPastDateValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface NotPastDate {

    String message() default "Date cannot be in the past";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /**
     * Allow today's date
     */
    boolean allowToday() default true;

    /**
     * Allow null values
     */
    boolean allowNull() default false;
}