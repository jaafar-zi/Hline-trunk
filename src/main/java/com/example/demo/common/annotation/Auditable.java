package com.example.demo.common.annotation;

import java.lang.annotation.*;

/**
 * Marks methods or classes for auditing
 *
 * Automatically logs method execution details including:
 * - User who performed the action
 * - Timestamp
 * - Method parameters
 * - Return value
 * - Execution time
 * - Success/failure status
 *
 * @author Architecture Team
 * @version 1.0
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Auditable {

    /**
     * Action name for audit log (e.g., "USER_CREATED", "ORDER_DELETED")
     */
    String action() default "";

    /**
     * Resource type being audited (e.g., "USER", "ORDER", "PRODUCT")
     */
    String resourceType() default "";

    /**
     * Description of the auditable action
     */
    String description() default "";

    /**
     * Whether to log method parameters
     */
    boolean logParameters() default true;

    /**
     * Whether to log return value
     */
    boolean logReturnValue() default true;

    /**
     * Whether to log execution time
     */
    boolean logExecutionTime() default true;

    /**
     * Severity level (INFO, WARN, ERROR)
     */
    AuditLevel level() default AuditLevel.INFO;

    /**
     * Whether to audit on success only
     */
    boolean onSuccessOnly() default false;

    /**
     * Whether to audit on failure only
     */
    boolean onFailureOnly() default false;

    /**
     * Whether to mask sensitive data in logs
     */
    boolean maskSensitiveData() default true;

    /**
     * Fields to exclude from audit log
     */
    String[] excludeFields() default {};

    /**
     * Audit level enum
     */
    enum AuditLevel {
        INFO,
        WARN,
        ERROR,
        DEBUG
    }
}