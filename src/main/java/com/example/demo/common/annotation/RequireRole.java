package com.example.demo.common.annotation;

import java.lang.annotation.*;

/**
 * Requires specific roles for endpoint access
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequireRole {
    String[] value();
    boolean requireAll() default false;
}