package com.example.demo.config.async;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Custom exception handler for async method execution
 * Logs exceptions that occur in @Async methods
 *
 * @author Architecture Team
 * @version 1.0
 */
@Slf4j
public class AsyncExceptionHandler implements AsyncUncaughtExceptionHandler {

    @Override
    public void handleUncaughtException(Throwable throwable, Method method, Object... params) {
        log.error("Async method '{}' threw exception with parameters: {}",
                method.getName(),
                Arrays.toString(params),
                throwable);

        // Could also:
        // - Send alert to monitoring system
        // - Log to external error tracking service
        // - Trigger compensation logic
    }
}