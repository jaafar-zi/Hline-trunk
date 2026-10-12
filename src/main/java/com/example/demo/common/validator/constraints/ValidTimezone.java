package com.example.demo.common.validator.constraints;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

import com.example.demo.common.validator.TimezoneValidator;

/**
 * Validates timezone ID
 *
 * @author Architecture Team
 * @version 1.0
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = TimezoneValidator.class)
@Documented
public @interface ValidTimezone {

    String message() default "Invalid timezone";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}