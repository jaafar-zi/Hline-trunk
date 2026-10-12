package com.example.demo.common.normalization;

import java.lang.annotation.*;

/**
 * Marks a field for normalization
 * 
 * @author Architecture Team
 * @version 1.0
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Normalize {

    /**
     * Normalization strategy
     */
    Strategy[] value();

    enum Strategy {
        TRIM,
        UPPERCASE,
        LOWERCASE,
        EMPTY_TO_NULL,
        REMOVE_WHITESPACE,
        CAPITALIZE,
        SLUG
    }
}