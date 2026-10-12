package com.example.demo.common.validator.constraints;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

import com.example.demo.common.validator.validators.PriceRangeValidator;

/**
 * Validates that min price <= max price
 * 
 * Usage (on class level):
 * @ValidPriceRange(min = "minPrice", max = "maxPrice")
 * public class ProductFilter { ... }
 * 
 * @author Architecture Team
 * @version 1.0
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PriceRangeValidator.class)
@Documented
public @interface ValidPriceRange {

    String message() default "Minimum price cannot be greater than maximum price";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /**
     * Field name for minimum price
     */
    String min();

    /**
     * Field name for maximum price
     */
    String max();

    /**
     * Allow null values
     */
    boolean allowNull() default true;
}