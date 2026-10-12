package com.example.demo.common.validator.constraints;

import com.example.demo.common.validator.validators.ValidPasswordValidator;  // CORRECT IMPORT
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Custom password validation annotation
 *
 * Features:
 * - Configurable password strength requirements
 * - Common password checking
 * - Character type requirements
 * - Length constraints
 *
 * @author Architecture Team
 * @version 1.0
 */
@Documented
@Constraint(validatedBy = ValidPasswordValidator.class)  // FIXED: Use ValidPasswordValidator
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPassword {

    String message() default "Password does not meet security requirements";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /**
     * Minimum password length
     */
    int minLength() default 8;

    /**
     * Maximum password length
     */
    int maxLength() default 128;

    /**
     * Require at least one uppercase letter
     */
    boolean requireUppercase() default true;

    /**
     * Require at least one lowercase letter
     */
    boolean requireLowercase() default true;

    /**
     * Require at least one digit
     */
    boolean requireDigit() default true;

    /**
     * Require at least one special character
     */
    boolean requireSpecialChar() default true;

    /**
     * Allowed special characters
     */
    String allowedSpecialChars() default "!@#$%^&*()_+-=[]{}|;:,.<>?";

    /**
     * Disallow common passwords
     */
    boolean disallowCommon() default true;

    /**
     * Disallow username in password
     */
    boolean disallowUsername() default true;

    /**
     * Minimum number of unique characters
     */
    int minUniqueChars() default 5;

    /**
     * Password strength level
     */
    StrengthLevel strength() default StrengthLevel.MEDIUM;

    enum StrengthLevel {
        WEAK,    // Basic requirements
        MEDIUM,  // Standard security
        STRONG,  // High security
        MAXIMUM  // Maximum security
    }
}