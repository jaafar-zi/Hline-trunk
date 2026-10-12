package com.example.demo.config.cache;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Externalized cache configuration properties
 * Allows environment-specific cache settings
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "app.cache")
public class CacheProperties {

    private CaffeineProperties caffeine = new CaffeineProperties();
    private RedisProperties redis = new RedisProperties();

    @Data
    public static class CaffeineProperties {
        /**
         * Maximum number of entries in L1 cache
         */
        private long maxSize = 10000;

        /**
         * Time after which entries expire after write
         */
        private Duration expireAfterWrite = Duration.ofMinutes(5);

        /**
         * Time after which entries expire after access
         */
        private Duration expireAfterAccess;

        /**
         * Initial capacity of cache
         */
        private int initialCapacity = 100;

        /**
         * Enable statistics recording
         */
        private boolean recordStats = true;
    }

    @Data
    public static class RedisProperties {
        /**
         * Default TTL for cache entries
         */
        private Duration defaultTtl = Duration.ofMinutes(30);

        /**
         * Whether to cache null values
         */
        private boolean cacheNullValues = false;

        /**
         * Whether to use key prefix
         */
        private boolean useKeyPrefix = true;

        /**
         * Cache key prefix
         */
        private String keyPrefix = "app:";

        /**
         * Per-cache TTL configurations
         * Example: users-entity=1h, products-entity=2h
         */
        private Map<String, Duration> ttl = new HashMap<>();

        /**
         * Enable transaction support
         */
        private boolean transactionAware = true;
    }
}