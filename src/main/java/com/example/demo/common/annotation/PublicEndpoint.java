package com.example.demo.common.annotation;

import java.lang.annotation.*;

/**
 * Marks endpoints as public (no authentication required)
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PublicEndpoint {
    String description() default "";
}