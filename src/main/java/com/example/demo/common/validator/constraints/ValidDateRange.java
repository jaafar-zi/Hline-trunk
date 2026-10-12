package com.example.demo.common.validator.constraints;

import com.example.demo.common.validator.validators.ValidDateRangeValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Validates that a date falls within a specific range
 *
 * @author Architecture Team
 * @version 1.0
 */
@Documented
@Constraint(validatedBy = ValidDateRangeValidator.class)
@Target({ ElementType.TYPE,ElementType.FIELD, ElementType.PARAMETER})
@Repeatable(ValidDateRanges.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidDateRange {

    String message() default "Date is outside allowed range";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /**
     * Minimum date (ISO format: yyyy-MM-dd)
     * Empty string means no minimum
     */
    String min() default "";

    /**
     * Maximum date (ISO format: yyyy-MM-dd)
     * Empty string means no maximum
     */
    String max() default "";

    /**
     * Allow null values
     */
    boolean allowNull() default false;

    /**
     * Use days offset from today instead of fixed dates
     */
    boolean useOffset() default false;

    /**
     * Minimum days offset from today (negative = past, positive = future)
     */
    int minDaysOffset() default 0;

    /**
     * Maximum days offset from today
     */
    int maxDaysOffset() default 0;
}