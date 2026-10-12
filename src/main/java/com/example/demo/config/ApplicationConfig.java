package com.example.demo.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringBootVersion;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import jakarta.validation.Validator;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import java.time.Instant;

/**
 * Main application configuration
 *
 * Central configuration that ties together all components
 *
 * @author Architecture Team
 * @version 1.0
 */
@Slf4j
@Configuration
@EnableAspectJAutoProxy
@EnableScheduling
@ConfigurationPropertiesScan(basePackages = "com.example.demo.config")
public class ApplicationConfig {

    /**
     * Bean validator for @Valid annotations
     */
    @Bean
    public Validator validator() {
        return new LocalValidatorFactoryBean();
    }
}

/**
 * Logs application startup information
 * 
 * @author Architecture Team
 * @version 1.0
 */
@Slf4j
@Component
class ApplicationStartupLogger {

    private final ApplicationContext applicationContext;
    private final Environment environment;
    private final Instant startTime = Instant.now();

    public ApplicationStartupLogger(ApplicationContext applicationContext, Environment environment) {
        this.applicationContext = applicationContext;
        this.environment = environment;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        Duration startupTime = Duration.between(startTime, Instant.now());
        
        String[] activeProfiles = environment.getActiveProfiles();
        String profiles = activeProfiles.length == 0 ? 
            "default" : 
            String.join(", ", activeProfiles);

        String protocol = "http";
        if (environment.getProperty("server.ssl.key-store") != null) {
            protocol = "https";
        }

        String serverPort = environment.getProperty("server.port", "8080");
        String contextPath = environment.getProperty("server.servlet.context-path", "/");
        String hostAddress = "localhost";

        try {
            hostAddress = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            log.warn("Could not determine host address");
        }

        String applicationName = environment.getProperty("spring.application.name", "HLine Application");

        log.info("=".repeat(80));
        log.info("🚀 Application '{}' started successfully!", applicationName);
        log.info("=".repeat(80));
        log.info("📍 Access URLs:");
        log.info("   Local:      {}://localhost:{}{}", protocol, serverPort, contextPath);
        log.info("   External:   {}://{}:{}{}", protocol, hostAddress, serverPort, contextPath);
        log.info("=".repeat(80));
        log.info("🔧 Configuration:");
        log.info("   Profile(s):       {}", profiles);
        log.info("   Java Version:     {}", System.getProperty("java.version"));
        log.info("   Spring Boot:      {}", SpringBootVersion.getVersion());
        log.info("   Loaded Beans:     {}", applicationContext.getBeanDefinitionCount());
        log.info("   Startup Time:     {} ms", startupTime.toMillis());
        log.info("=".repeat(80));

        // Log database info if available
        logDatabaseInfo();
        
        // Log Kafka info if available
        logKafkaInfo();
        
        // Log Redis info if available
        logRedisInfo();

        log.info("=".repeat(80));
        log.info("✅ Application is ready to accept requests!");
        log.info("=".repeat(80));
    }

    private void logDatabaseInfo() {
        String masterUrl = environment.getProperty("spring.datasource.master.url");
        String slaveUrl = environment.getProperty("spring.datasource.slave.url");
        
        if (masterUrl != null) {
            log.info("📊 Database:");
            log.info("   Master DB:        {}", sanitizeUrl(masterUrl));
            if (slaveUrl != null && !slaveUrl.equals(masterUrl)) {
                log.info("   Slave DB:         {}", sanitizeUrl(slaveUrl));
            }
        }
    }

    private void logKafkaInfo() {
        String kafkaServers = environment.getProperty("spring.kafka.bootstrap-servers");
        if (kafkaServers != null) {
            log.info("📨 Kafka:");
            log.info("   Bootstrap:        {}", kafkaServers);
            log.info("   Consumer Group:   {}", environment.getProperty("spring.kafka.consumer.group-id", "N/A"));
        }
    }

    private void logRedisInfo() {
        String redisHost = environment.getProperty("spring.redis.host");
        String redisPort = environment.getProperty("spring.redis.port");
        if (redisHost != null) {
            log.info("🗄️  Redis:");
            log.info("   Host:             {}:{}", redisHost, redisPort);
        }
    }

    private String sanitizeUrl(String url) {
        if (url == null) {
            return "N/A";
        }
        // Remove password from URL for logging
        return url.replaceAll(":[^:@]+@", ":***@");
    }
}