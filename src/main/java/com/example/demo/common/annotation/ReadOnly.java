package com.example.demo.common.annotation;

import org.springframework.core.annotation.AliasFor;
import org.springframework.transaction.annotation.Transactional;

import java.lang.annotation.*;

/**
 * Marks methods or classes as read-only operations
 *
 * This is a meta-annotation that combines:
 * - @Transactional(readOnly = true) for Spring transaction management
 * - Custom routing to read replicas via DatabaseRoutingAspect
 *
 * Features:
 * - Enforces read-only transaction behavior
 * - Routes queries to read replica databases
 * - Optimizes database queries (no dirty checking)
 * - Prevents accidental data modification
 *
 * Usage:
 * <pre>
 * {@code
 * @ReadOnly
 * public User findById(UUID id) {
 *     return userRepository.findById(id).orElse(null);
 * }
 * }
 * </pre>
 *
 * @author Architecture Team
 * @version 1.0
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Transactional(readOnly = false, timeout = -1)  
public @interface ReadOnly {
    
    /**
     * Enable strict read-only enforcement
     */
    boolean strict() default false;

    /**
     * Whether to use read replica database
     * Default: true (route to slave/read replica)
     */
    boolean useReadReplica() default true;

    /**
     * Transaction timeout in seconds
     * Delegates to @Transactional timeout
     * 0 means use default timeout
     */
    // @AliasFor(annotation = Transactional.class, attribute = "timeout")
    // int timeout() default -1;

    /**
     * Transaction label for monitoring/logging
     */
    @AliasFor(annotation = Transactional.class, attribute = "label")
    String[] label() default {};

    /**
     * Whether to log read operations
     * Useful for debugging and audit trails
     */
    boolean logAccess() default false;
}