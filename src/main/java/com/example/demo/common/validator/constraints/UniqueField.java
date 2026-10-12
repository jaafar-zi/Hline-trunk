package com.example.demo.common.validator.constraints;

import com.example.demo.common.validator.validators.UniqueFieldValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Validates that a field value is unique in the database
 *
 * Features:
 * - Database uniqueness check
 * - Support for update operations (exclude current entity)
 * - Custom repository queries
 * - Multiple field combinations
 *
 * Usage:
 * <pre>
 * {@code
 * public class UserRequest {
 *     @UniqueField(
 *         entity = User.class,
 *         field = "email",
 *         message = "Email already exists"
 *     )
 *     private String email;
 *
 *     @UniqueField(
 *         entity = User.class,
 *         field = "username",
 *         excludeId = true
 *     )
 *     private String username;
 * }
 * }
 * </pre>
 *
 * @author Architecture Team
 * @version 1.0
 */
@Documented
@Constraint(validatedBy = UniqueFieldValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface UniqueField {

    String message() default "Value already exists";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /**
     * Entity class to check against
     */
    Class<?> entity();

    /**
     * Field name in entity
     */
    String field();

    /**
     * Repository bean name (optional)
     * If not provided, will try to auto-detect
     */
    String repository() default "";

    /**
     * Exclude current entity ID (for updates)
     */
    boolean excludeId() default false;

    /**
     * ID field name in request (for updates)
     */
    String idField() default "id";

    /**
     * Case-sensitive comparison
     */
    boolean caseSensitive() default true;

    /**
     * Allow null values
     */
    boolean allowNull() default false;

    /**
     * Custom query method name in repository
     */
    String queryMethod() default "";
}