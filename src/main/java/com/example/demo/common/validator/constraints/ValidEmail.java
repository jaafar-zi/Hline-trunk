package com.example.demo.common.validator.constraints;

import com.example.demo.common.validator.EmailValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Custom email validation annotation
 *
 * Features:
 * - RFC 5322 compliant email validation
 * - Custom validation message
 * - Configurable domain whitelist/blacklist
 *
 * Usage:
 * <pre>
 * {@code
 * public class UserRequest {
 *     @ValidEmail
 *     private String email;
 *
 *     @ValidEmail(message = "Corporate email required", allowedDomains = {"company.com"})
 *     private String corporateEmail;
 * }
 * }
 * </pre>
 *
 * @author Architecture Team
 * @version 1.0
 */
@Documented
@Constraint(validatedBy = EmailValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidEmail {

    /**
     * Validation error message
     */
    String message() default "Invalid email format";

    /**
     * Validation groups
     */
    Class<?>[] groups() default {};

    /**
     * Payload for clients
     */
    Class<? extends Payload>[] payload() default {};

    /**
     * Allow empty/null values
     */
    boolean allowNull() default false;

    /**
     * Allowed email domains (whitelist)
     * Empty array means all domains allowed
     */
    String[] allowedDomains() default {};

    /**
     * Blocked email domains (blacklist)
     */
    String[] blockedDomains() default {};

    /**
     * Require specific TLD (top-level domain)
     * Example: "com", "org", "edu"
     */
    String[] requiredTlds() default {};

    /**
     * Maximum email length
     */
    int maxLength() default 255;

    /**
     * Allow plus-addressing (email+tag@domain.com)
     */
    boolean allowPlusAddressing() default true;

    /**
     * Require verified domains only
     */
    boolean requireVerifiedDomain() default false;
}