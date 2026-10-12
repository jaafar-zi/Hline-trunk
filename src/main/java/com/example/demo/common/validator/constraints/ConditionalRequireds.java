package com.example.demo.common.validator.constraints;

import java.lang.annotation.*;

/**
 * Container for multiple @ConditionalRequired annotations
 * 
 * @author Architecture Team
 * @version 1.0
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ConditionalRequireds {
    ConditionalRequired[] value();
}