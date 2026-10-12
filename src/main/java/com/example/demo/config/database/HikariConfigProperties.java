package com.example.demo.config.database;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * HikariCP connection pool configuration properties
 * Externalized for environment-specific tuning
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "app.datasource.hikari")
public class HikariConfigProperties {

    /**
     * Maximum number of connections in the pool
     */
    private int maximumPoolSize = 20;

    /**
     * Minimum number of idle connections
     */
    private int minimumIdle = 5;

    /**
     * Maximum time (ms) to wait for a connection
     */
    private long connectionTimeout = 30000;

    /**
     * Maximum time (ms) a connection can sit idle in the pool
     */
    private long idleTimeout = 600000;

    /**
     * Maximum lifetime (ms) of a connection in the pool
     */
    private long maxLifetime = 1800000;

    /**
     * Connection leak detection threshold (ms)
     */
    private long leakDetectionThreshold = 60000;

    /**
     * Connection test query
     */
    private String connectionTestQuery = "SELECT 1";

    /**
     * Pool name
     */
    private String poolName = "HikariPool";

    /**
     * Auto-commit behavior
     */
    private boolean autoCommit = false;
}