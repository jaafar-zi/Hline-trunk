package com.example.demo.config.observability;

import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.micrometer.core.instrument.binder.system.UptimeMetrics;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Metrics configuration with Micrometer
 *
 * Features:
 * - Custom metrics registration
 * - Common tags for all metrics
 * - JVM metrics
 * - System metrics
 * - @Timed annotation support
 *
 * @author Architecture Team
 * @version 1.0
 */
@Slf4j
@Configuration
public class MetricsConfig {

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${spring.profiles.active:default}")
    private String environment;

    /**
     * Customize meter registry with common tags
     */
    @Bean
    public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
        return registry -> {
            registry.config().commonTags(
                    "application", applicationName,
                    "environment", environment,
                    "region", System.getenv().getOrDefault("REGION", "local"),
                    "instance", getInstanceId()
            );

            log.info("Metrics common tags configured: application={}, environment={}",
                    applicationName, environment);
        };
    }

    /**
     * Enable @Timed annotation support
     */
    @Bean
    public TimedAspect timedAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }

    /**
     * JVM memory metrics
     */
    @Bean
    public JvmMemoryMetrics jvmMemoryMetrics() {
        return new JvmMemoryMetrics();
    }

    /**
     * JVM GC metrics
     */
    @Bean
    public JvmGcMetrics jvmGcMetrics() {
        return new JvmGcMetrics();
    }

    /**
     * JVM thread metrics
     */
    @Bean
    public JvmThreadMetrics jvmThreadMetrics() {
        return new JvmThreadMetrics();
    }

    /**
     * JVM class loader metrics
     */
    @Bean
    public ClassLoaderMetrics classLoaderMetrics() {
        return new ClassLoaderMetrics();
    }

    /**
     * System processor metrics
     */
    @Bean
    public ProcessorMetrics processorMetrics() {
        return new ProcessorMetrics();
    }

    /**
     * Application uptime metrics
     */
    @Bean
    public UptimeMetrics uptimeMetrics() {
        return new UptimeMetrics();
    }

    /**
     * Get instance ID (hostname or container ID)
     */
    private String getInstanceId() {
        try {
            // Try to get container ID from environment
            String containerId = System.getenv("CONTAINER_ID");
            if (containerId != null && !containerId.isEmpty()) {
                return containerId;
            }

            // Fallback to hostname
            return java.net.InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            log.warn("Unable to determine instance ID", e);
            return "unknown";
        }
    }
}