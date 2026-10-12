package com.example.demo.config.observability;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * Custom health check configuration
 *
 * Features:
 * - Database health check
 * - Redis health check
 * - Cache health check
 * - Custom application health indicators
 *
 * @author Architecture Team
 * @version 1.0
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class HealthCheckConfig {

    /**
     * Database health indicator
     */
    @Bean
    public HealthIndicator dbHealthIndicator(DataSource dataSource) {
        return () -> {
            try {
                JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
                jdbcTemplate.queryForObject("SELECT 1", Integer.class);

                return Health.up()
                        .withDetail("database", "PostgreSQL")
                        .withDetail("status", "connected")
                        .build();
            } catch (Exception e) {
                log.error("Database health check failed", e);
                return Health.down()
                        .withDetail("database", "PostgreSQL")
                        .withDetail("error", e.getMessage())
                        .build();
            }
        };
    }

    /**
     * Redis health indicator
     */
    // @Bean
    // public HealthIndicator redisHealthIndicator(RedisConnectionFactory connectionFactory) {
    //     return () -> {
    //         try {
    //             String pong = connectionFactory.getConnection().ping();

    //             return Health.up()
    //                     .withDetail("redis", "connected")
    //                     .withDetail("response", pong)
    //                     .build();
    //         } catch (Exception e) {
    //             log.error("Redis health check failed", e);
    //             return Health.down()
    //                     .withDetail("redis", "disconnected")
    //                     .withDetail("error", e.getMessage())
    //                     .build();
    //         }
    //     };
    // }

    /**
     * Cache health indicator
     */
    @Bean
    public HealthIndicator cacheHealthIndicator(CacheManager cacheManager) {
        return () -> {
            try {
                int cacheCount = cacheManager.getCacheNames().size();

                return Health.up()
                        .withDetail("cacheManager", cacheManager.getClass().getSimpleName())
                        .withDetail("cacheCount", cacheCount)
                        .withDetail("caches", cacheManager.getCacheNames())
                        .build();
            } catch (Exception e) {
                log.error("Cache health check failed", e);
                return Health.down()
                        .withDetail("error", e.getMessage())
                        .build();
            }
        };
    }

    /**
     * Custom application health indicator
     */
    @Bean
    public HealthIndicator customHealthIndicator() {
        return () -> Health.up()
                .withDetail("application", "running")
                .withDetail("version", getApplicationVersion())
                .withDetail("uptime", getUptime())
                .build();
    }

    private String getApplicationVersion() {
        return getClass().getPackage().getImplementationVersion() != null
                ? getClass().getPackage().getImplementationVersion()
                : "development";
    }

    private String getUptime() {
        long uptimeMs = System.currentTimeMillis() -
                java.lang.management.ManagementFactory.getRuntimeMXBean().getStartTime();
        long seconds = uptimeMs / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;

        return String.format("%dh %dm %ds", hours, minutes % 60, seconds % 60);
    }
}