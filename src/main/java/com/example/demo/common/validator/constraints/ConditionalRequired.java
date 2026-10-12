package com.example.demo.common.validator.constraints;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

import com.example.demo.common.validator.validators.ConditionalRequiredValidator;

/**
 * Conditionally requires a field based on another field's value
 * 
 * Usage:
 * @ConditionalRequired(
 *     field = "validFrom",
 *     dependsOn = "priceType",
 *     dependsOnValue = "PROMOTIONAL"
 * )
 * 
 * @author Architecture Team
 * @version 1.0
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ConditionalRequiredValidator.class)
@Documented
@Repeatable(ConditionalRequireds.class)
public @interface ConditionalRequired {

    String message() default "Field is required when condition is met";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /**
     * Field that should be required
     */
    String field();

    /**
     * Field to check condition against
     */
    String dependsOn();

    /**
     * Expected value(s) of dependsOn field
     */
    String[] dependsOnValue();

    /**
     * Negate the condition
     */
    boolean negate() default false;
}