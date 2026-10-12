package com.example.demo.config.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Ticker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.cache.interceptor.SimpleKeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Central cache configuration
 * Configures multi-level caching with Caffeine (L1) and Redis (L2)
 *
 * Features:
 * - Multi-level cache manager
 * - Custom key generation
 * - Error handling
 * - Per-cache TTL configuration
 * - Specialized Caffeine builders for different use cases
 *
 * @author Architecture Team
 * @version 2.0
 */
@Slf4j
@Configuration
@EnableCaching
@RequiredArgsConstructor
public class CacheConfig implements CachingConfigurer {

    private final CacheProperties cacheProperties;

    /**
     * Primary cache manager combining Caffeine and Redis
     * Checks L1 (Caffeine) first, then L2 (Redis), then database
     */
    @Bean
    @Primary
    @ConditionalOnProperty(name = "spring.cache.type", havingValue = "redis", matchIfMissing = true)
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        log.info("Initializing Multi-Level Cache Manager");

        CaffeineCacheManager caffeineManager = caffeineCacheManager();
        RedisCacheManager redisManager = redisCacheManager(redisConnectionFactory);

        MultiLevelCacheManager multiLevelCacheManager =
                new MultiLevelCacheManager(caffeineManager, redisManager);

        log.info("Multi-Level Cache Manager initialized successfully");
        return multiLevelCacheManager;
    }

    /**
     * L1 Cache Manager (Caffeine - Local, Fast)
     * Used for frequently accessed data with short TTL
     */
    @Bean
    public CaffeineCacheManager caffeineCacheManager() {
        log.info("Configuring Caffeine Cache Manager");

        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(defaultCaffeineBuilder());

        log.info("Caffeine Cache configured with maxSize={}, expireAfterWrite={}",
                cacheProperties.getCaffeine().getMaxSize(),
                cacheProperties.getCaffeine().getExpireAfterWrite());

        return cacheManager;
    }

    /**
     * Default Caffeine cache builder
     */
    @Bean
    public Caffeine<Object, Object> defaultCaffeineBuilder() {
        return Caffeine.newBuilder()
                .maximumSize(cacheProperties.getCaffeine().getMaxSize())
                .expireAfterWrite(
                        cacheProperties.getCaffeine().getExpireAfterWrite().getSeconds(),
                        TimeUnit.SECONDS
                )
                .initialCapacity(cacheProperties.getCaffeine().getInitialCapacity())
                .recordStats()
                .ticker(Ticker.systemTicker());
    }

    /**
     * High-frequency cache for data accessed multiple times per second
     * Example: User sessions, frequently accessed products
     */
    @Bean
    public Caffeine<Object, Object> highFrequencyCaffeineBuilder() {
        log.debug("Creating high-frequency Caffeine builder");
        return Caffeine.newBuilder()
                .maximumSize(5000)
                .expireAfterAccess(2, TimeUnit.MINUTES)
                .initialCapacity(500)
                .recordStats();
    }

    /**
     * Large cache for bulk data
     * Example: Product catalogs, reference data
     */
    @Bean
    public Caffeine<Object, Object> bulkCaffeineBuilder() {
        log.debug("Creating bulk Caffeine builder");
        return Caffeine.newBuilder()
                .maximumSize(50000)
                .expireAfterWrite(1, TimeUnit.HOURS)
                .initialCapacity(5000)
                .recordStats();
    }

    /**
     * Short-lived cache for temporary data
     * Example: Search results, dynamic queries
     */
    @Bean
    public Caffeine<Object, Object> shortLivedCaffeineBuilder() {
        log.debug("Creating short-lived Caffeine builder");
        return Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(30, TimeUnit.SECONDS)
                .initialCapacity(100)
                .recordStats();
    }

    /**
     * Create named Caffeine caches with specific configurations
     */
    @Bean
    public List<CaffeineCache> caffeineCaches() {
        log.info("Creating named Caffeine caches");
        return Arrays.asList(
                new CaffeineCache("default", defaultCaffeineBuilder().build()),
                new CaffeineCache("users-local", defaultCaffeineBuilder().build()),
                new CaffeineCache("products-local", defaultCaffeineBuilder().build()),
                new CaffeineCache("sessions-local", highFrequencyCaffeineBuilder().build()),
                new CaffeineCache("search-results", shortLivedCaffeineBuilder().build()),
                new CaffeineCache("catalog-bulk", bulkCaffeineBuilder().build())
        );
    }

    /**
     * L2 Cache Manager (Redis - Distributed, Shared)
     * Used for data that needs to be shared across instances
     */
    @Bean
    public RedisCacheManager redisCacheManager(RedisConnectionFactory connectionFactory) {
        log.info("Configuring Redis Cache Manager");

        // Configure ObjectMapper for JSON serialization
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.activateDefaultTyping(
                objectMapper.getPolymorphicTypeValidator(),
                ObjectMapper.DefaultTyping.NON_FINAL
        );

        GenericJackson2JsonRedisSerializer jsonSerializer =
                new GenericJackson2JsonRedisSerializer(objectMapper);

        // Default cache configuration
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration
                .defaultCacheConfig()
                .entryTtl(cacheProperties.getRedis().getDefaultTtl())
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair
                                .fromSerializer(new StringRedisSerializer())
                )
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair
                                .fromSerializer(jsonSerializer)
                )
                .disableCachingNullValues();

        if (cacheProperties.getRedis().isUseKeyPrefix()) {
            defaultConfig = defaultConfig.prefixCacheNameWith(
                    cacheProperties.getRedis().getKeyPrefix()
            );
        }

        // Per-cache configurations
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        RedisCacheConfiguration finalDefaultConfig = defaultConfig;
        cacheProperties.getRedis().getTtl().forEach((cacheName, ttl) -> {
            cacheConfigurations.put(cacheName, finalDefaultConfig.entryTtl(ttl));
            log.debug("Cache '{}' configured with TTL: {}", cacheName, ttl);
        });

        RedisCacheManager cacheManager = RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .transactionAware()
                .build();

        log.info("Redis Cache Manager configured with {} custom cache configurations",
                cacheConfigurations.size());

        return cacheManager;
    }

    /**
     * Custom key generator for cache keys
     * Generates keys based on method signature and parameters
     */
    @Bean
    @Override
    public KeyGenerator keyGenerator() {
        return new SimpleKeyGenerator() {
            @Override
            public Object generate(Object target, java.lang.reflect.Method method, Object... params) {
                StringBuilder sb = new StringBuilder();
                sb.append(target.getClass().getSimpleName());
                sb.append(".");
                sb.append(method.getName());
                sb.append(":");

                for (Object param : params) {
                    if (param != null) {
                        sb.append(param.toString());
                        sb.append(":");
                    }
                }

                return sb.toString();
            }
        };
    }

    /**
     * Custom error handler for cache operations
     * Logs errors but doesn't fail the application
     * 
     * Note: Named specifically to avoid conflict with Kafka's errorHandler
     */
    @Bean("cacheErrorHandler")  // ← CHANGED: Added explicit bean name
    @Override
    public CacheErrorHandler errorHandler() {
        return new CacheErrorHandler() {
            @Override
            public void handleCacheGetError(RuntimeException exception,
                                            org.springframework.cache.Cache cache,
                                            Object key) {
                log.error("Cache GET error - Cache: {}, Key: {}",
                        cache.getName(), key, exception);
                // Don't throw - degrade gracefully to database
            }

            @Override
            public void handleCachePutError(RuntimeException exception,
                                            org.springframework.cache.Cache cache,
                                            Object key,
                                            Object value) {
                log.error("Cache PUT error - Cache: {}, Key: {}",
                        cache.getName(), key, exception);
            }

            @Override
            public void handleCacheEvictError(RuntimeException exception,
                                              org.springframework.cache.Cache cache,
                                              Object key) {
                log.error("Cache EVICT error - Cache: {}, Key: {}",
                        cache.getName(), key, exception);
            }

            @Override
            public void handleCacheClearError(RuntimeException exception,
                                              org.springframework.cache.Cache cache) {
                log.error("Cache CLEAR error - Cache: {}",
                        cache.getName(), exception);
            }
        };
    }
}