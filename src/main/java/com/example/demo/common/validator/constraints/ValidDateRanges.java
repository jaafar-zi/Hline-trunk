package com.example.demo.common.validator.constraints;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ValidDateRanges {
    ValidDateRange[] value();
}