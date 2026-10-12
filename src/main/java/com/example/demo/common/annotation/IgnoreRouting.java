package com.example.demo.common.annotation;

import java.lang.annotation.*;

/**
 * Marks methods that should bypass database routing logic
 * Useful for:
 * - Internal utility methods
 * - Methods that handle routing themselves
 * - Testing methods
 *
 * @author Architecture Team
 * @version 1.0
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface IgnoreRouting {
    /**
     * Reason for ignoring routing
     */
    String value() default "";
}