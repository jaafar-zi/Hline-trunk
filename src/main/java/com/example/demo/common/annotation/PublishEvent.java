package com.example.demo.common.annotation;

import java.lang.annotation.*;

/**
 * Publishes domain events after method execution
 *
 * Supports:
 * - Synchronous event publishing
 * - Asynchronous event publishing
 * - Kafka/RabbitMQ integration
 * - Conditional event publishing
 *
 * @author Architecture Team
 * @version 1.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PublishEvent {

    /**
     * Event type to publish
     */
    String eventType();

    /**
     * Event topic/channel (for Kafka/RabbitMQ)
     */
    String topic() default "";

    /**
     * Event description
     */
    String description() default "";

    /**
     * Whether to publish asynchronously
     */
    boolean async() default true;

    /**
     * Publishing strategy
     */
    PublishStrategy strategy() default PublishStrategy.AFTER_SUCCESS;

    /**
     * SpEL expression for conditional publishing
     * Example: "#result != null", "#result.status == 'COMPLETED'"
     */
    String condition() default "";

    /**
     * SpEL expression to extract event payload from method result
     * Example: "#result", "#result.data"
     */
    String payloadExpression() default "#result";

    /**
     * Whether to include method parameters in event
     */
    boolean includeParameters() default false;

    /**
     * Event priority (for queue-based systems)
     */
    int priority() default 5;

    /**
     * Retry attempts for failed publishing
     */
    int retryAttempts() default 3;

    /**
     * Whether to use transaction-aware publishing
     * (publish only if transaction commits successfully)
     */
    boolean transactional() default true;

    /**
     * Publishing strategy enum
     */
    enum PublishStrategy {
        /**
         * Publish after successful method execution
         */
        AFTER_SUCCESS,

        /**
         * Publish after method execution (success or failure)
         */
        AFTER_COMPLETION,

        /**
         * Publish only on method failure
         */
        ON_FAILURE,

        /**
         * Publish before method execution
         */
        BEFORE_EXECUTION
    }
}