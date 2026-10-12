package com.example.demo.common.validator.constraints;

import com.example.demo.common.validator.PhoneValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Custom phone number validation annotation
 *
 * Features:
 * - International phone number validation
 * - Country code validation
 * - Configurable regions
 * - Format normalization
 *
 * Usage:
 * <pre>
 * {@code
 * public class UserRequest {
 *     @ValidPhone
 *     private String phone;
 *
 *     @ValidPhone(regions = {"US", "CA"}, message = "Invalid North American phone number")
 *     private String naPhone;
 * }
 * }
 * </pre>
 *
 * @author Architecture Team
 * @version 1.0
 */
@Documented
@Constraint(validatedBy = PhoneValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPhone {

    /**
     * Validation error message
     */
    String message() default "Invalid phone number format";

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
     * Allowed regions/countries (ISO 3166-1 alpha-2 codes)
     * Example: "US", "CA", "GB", "FR"
     * Empty array means all regions allowed
     */
    String[] regions() default {};

    /**
     * Require country code prefix (+1, +44, etc.)
     */
    boolean requireCountryCode() default false;

    /**
     * Allow extension numbers (e.g., ext. 123)
     */
    boolean allowExtension() default true;

    /**
     * Phone number type
     */
    PhoneType type() default PhoneType.ANY;

    /**
     * Minimum number of digits (excluding country code)
     */
    int minDigits() default 7;

    /**
     * Maximum number of digits (excluding country code)
     */
    int maxDigits() default 15;

    /**
     * Phone number type enum
     */
    enum PhoneType {
        ANY,          // Any phone number
        MOBILE,       // Mobile/cell phone only
        FIXED_LINE,   // Landline only
        TOLL_FREE,    // Toll-free numbers
        PREMIUM_RATE  // Premium rate numbers
    }
}