package com.example.demo.common.annotation;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * Rate limiting annotation
 *
 * Limits the number of method invocations within a time window:
 * - Per user
 * - Per IP address
 * - Per API key
 * - Global
 *
 * @author Architecture Team
 * @version 1.0
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimited {

    /**
     * Maximum number of requests allowed
     */
    int limit() default 100;

    /**
     * Time window duration
     */
    long duration() default 60;

    /**
     * Time unit for duration
     */
    TimeUnit timeUnit() default TimeUnit.SECONDS;

    /**
     * Rate limit scope
     */
    RateLimitScope scope() default RateLimitScope.USER;

    /**
     * Custom key for rate limiting (SpEL expression)
     * Examples: "#userId", "#apiKey", "#request.remoteAddr"
     */
    String key() default "";

    /**
     * Error message when rate limit is exceeded
     */
    String message() default "Rate limit exceeded. Please try again later.";

    /**
     * HTTP status code to return when rate limited (default 429)
     */
    int statusCode() default 429;

    /**
     * Whether to include retry-after header
     */
    boolean includeRetryAfter() default true;

    /**
     * Strategy for handling rate limit violations
     */
    RateLimitStrategy strategy() default RateLimitStrategy.REJECT;

    /**
     * Rate limit scope enum
     */
    enum RateLimitScope {
        /**
         * Per authenticated user
         */
        USER,

        /**
         * Per IP address
         */
        IP,

        /**
         * Per API key
         */
        API_KEY,

        /**
         * Global (applies to all requests)
         */
        GLOBAL,

        /**
         * Custom (uses the key expression)
         */
        CUSTOM
    }

    /**
     * Rate limit strategy
     */
    enum RateLimitStrategy {
        /**
         * Reject the request immediately
         */
        REJECT,

        /**
         * Queue the request and process later
         */
        QUEUE,

        /**
         * Throttle (slow down) the request
         */
        THROTTLE
    }
}