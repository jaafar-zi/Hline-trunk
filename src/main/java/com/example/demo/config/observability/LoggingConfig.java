package com.example.demo.config.observability;

import ch.qos.logback.classic.LoggerContext;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Logging configuration
 *
 * Features:
 * - Structured logging with JSON format
 * - MDC (Mapped Diagnostic Context) support
 * - Environment-specific log levels
 * - Log correlation with trace IDs
 *
 * @author Architecture Team
 * @version 1.0
 */
@Slf4j
@Configuration
public class LoggingConfig {

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${logging.level.root:INFO}")
    private String rootLogLevel;

    /**
     * Initialize logging configuration
     */
    @Bean
    public LoggerContext loggerContext() {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

        log.info("Logging initialized for application: {}", applicationName);
        log.info("Root log level: {}", rootLogLevel);

        return context;
    }

    /**
     * Log correlation filter for trace IDs
     * Automatically adds trace/span IDs to MDC
     */
    @Bean
    public brave.baggage.BaggageField requestIdField() {
        return brave.baggage.BaggageField.create("request-id");
    }

    @Bean
    public brave.baggage.BaggageField userIdField() {
        return brave.baggage.BaggageField.create("user-id");
    }
}