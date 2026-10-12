package com.example.demo.common.validator.constraints;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

import com.example.demo.common.validator.validators.ImageUrlValidator;

/**
 * Validates image URL format
 * 
 * @author Architecture Team
 * @version 1.0
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ImageUrlValidator.class)
@Documented
public @interface ValidImageUrl {

    String message() default "Invalid image URL";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /**
     * Allowed extensions
     */
    String[] allowedExtensions() default {"jpg", "jpeg", "png", "webp", "gif", "svg"};

    /**
     * Require HTTPS
     */
    boolean requireHttps() default false;

    /**
     * Allow null values
     */
    boolean allowNull() default true;

    /**
     * Maximum URL length
     */
    int maxLength() default 2048;
}