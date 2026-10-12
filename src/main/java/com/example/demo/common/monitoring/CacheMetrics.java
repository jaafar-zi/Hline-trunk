package com.example.demo.common.monitoring;

import com.example.demo.common.constant.CacheConstants;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Cache Metrics Collection and Monitoring
 *
 * Features:
 * - Cache hit/miss tracking
 * - Cache size monitoring
 * - Cache eviction tracking
 * - Cache performance metrics
 * - Per-cache statistics
 * - Memory usage tracking
 *
 * @author Architecture Team
 * @version 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CacheMetrics {

    private final MeterRegistry meterRegistry;
    private final CacheManager cacheManager;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String METRIC_PREFIX = "cache.";

    // Cache statistics storage
    private final Map<String, CacheStatistics> cacheStats = new ConcurrentHashMap<>();

    /**
     * Record cache hit
     */
    public void recordCacheHit(String cacheName) {
        getCacheStatistics(cacheName).incrementHits();

        meterRegistry.counter(METRIC_PREFIX + "hits",
                        Tags.of("cache", cacheName))
                .increment();

        log.debug("Cache hit recorded for cache: {}", cacheName);
    }

    /**
     * Record cache miss
     */
    public void recordCacheMiss(String cacheName) {
        getCacheStatistics(cacheName).incrementMisses();

        meterRegistry.counter(METRIC_PREFIX + "misses",
                        Tags.of("cache", cacheName))
                .increment();

        log.debug("Cache miss recorded for cache: {}", cacheName);
    }

    /**
     * Record cache put operation
     */
    public void recordCachePut(String cacheName, long size) {
        getCacheStatistics(cacheName).incrementPuts();

        meterRegistry.counter(METRIC_PREFIX + "puts",
                        Tags.of("cache", cacheName))
                .increment();

        meterRegistry.summary(METRIC_PREFIX + "put.size",
                        Tags.of("cache", cacheName))
                .record(size);
    }

    /**
     * Record cache eviction
     */
    public void recordCacheEviction(String cacheName, String reason) {
        getCacheStatistics(cacheName).incrementEvictions();

        meterRegistry.counter(METRIC_PREFIX + "evictions",
                        Tags.of("cache", cacheName, "reason", reason))
                .increment();

        log.debug("Cache eviction recorded for cache: {}, reason: {}", cacheName, reason);
    }

    /**
     * Record cache clear operation
     */
    public void recordCacheClear(String cacheName) {
        getCacheStatistics(cacheName).incrementClears();

        meterRegistry.counter(METRIC_PREFIX + "clears",
                        Tags.of("cache", cacheName))
                .increment();
    }

    /**
     * Record cache load time
     */
    public void recordCacheLoadTime(String cacheName, long milliseconds) {
        meterRegistry.timer(METRIC_PREFIX + "load.time",
                        Tags.of("cache", cacheName))
                .record(java.time.Duration.ofMillis(milliseconds));
    }

    /**
     * Update cache size gauge
     */
    public void updateCacheSize(String cacheName, long size) {
        getCacheStatistics(cacheName).setCurrentSize(size);

        meterRegistry.gauge(METRIC_PREFIX + "size",
                Tags.of("cache", cacheName),
                size);
    }

    /**
     * Update cache memory usage
     */
    public void updateCacheMemoryUsage(String cacheName, long bytes) {
        meterRegistry.gauge(METRIC_PREFIX + "memory.bytes",
                Tags.of("cache", cacheName),
                bytes);
    }

    /**
     * Get or create cache statistics
     */
    private CacheStatistics getCacheStatistics(String cacheName) {
        return cacheStats.computeIfAbsent(cacheName, k -> {
            CacheStatistics stats = new CacheStatistics(cacheName);
            registerGauges(stats);
            return stats;
        });
    }

    /**
     * Register gauges for cache statistics
     */
    private void registerGauges(CacheStatistics stats) {
        String cacheName = stats.getCacheName();

        meterRegistry.gauge(METRIC_PREFIX + "hit.ratio",
                Tags.of("cache", cacheName),
                stats, CacheStatistics::getHitRatio);

        meterRegistry.gauge(METRIC_PREFIX + "miss.ratio",
                Tags.of("cache", cacheName),
                stats, CacheStatistics::getMissRatio);

        meterRegistry.gauge(METRIC_PREFIX + "total.requests",
                Tags.of("cache", cacheName),
                stats, CacheStatistics::getTotalRequests);
    }

    /**
     * Collect cache metrics periodically
     */
    @Scheduled(fixedRate = 60000) // Every minute
    public void collectCacheMetrics() {
        log.debug("Collecting cache metrics");

        Collection<String> cacheNames = cacheManager.getCacheNames();

        for (String cacheName : cacheNames) {
            try {
                Cache cache = cacheManager.getCache(cacheName);
                if (cache != null) {
                    collectCacheInfo(cacheName, cache);
                }
            } catch (Exception e) {
                log.error("Failed to collect metrics for cache: {}", cacheName, e);
            }
        }
    }

    /**
     * Collect information for a specific cache
     */
    private void collectCacheInfo(String cacheName, Cache cache) {
        Object nativeCache = cache.getNativeCache();

        if (nativeCache instanceof RedisCache) {
            collectRedisCacheInfo(cacheName);
        } else if (nativeCache instanceof com.github.benmanes.caffeine.cache.Cache) {
            collectCaffeineCacheInfo(cacheName,
                    (com.github.benmanes.caffeine.cache.Cache<?, ?>) nativeCache);
        }
    }

    /**
     * Collect Redis cache information
     */
    private void collectRedisCacheInfo(String cacheName) {
        try {
            String pattern = CacheConstants.buildKey(cacheName, "*");
            Set<String> keys = redisTemplate.keys(pattern);

            if (keys != null) {
                long size = keys.size();
                updateCacheSize(cacheName, size);

                // Estimate memory usage (approximate)
                long estimatedMemory = size * 1024; // Rough estimate
                updateCacheMemoryUsage(cacheName, estimatedMemory);
            }
        } catch (Exception e) {
            log.error("Failed to collect Redis cache info for: {}", cacheName, e);
        }
    }

    /**
     * Collect Caffeine cache information
     */
    private void collectCaffeineCacheInfo(String cacheName,
                                          com.github.benmanes.caffeine.cache.Cache<?, ?> cache) {
        try {
            long size = cache.estimatedSize();
            updateCacheSize(cacheName, size);

            // Get cache stats if available
            com.github.benmanes.caffeine.cache.stats.CacheStats stats = cache.stats();

            CacheStatistics cacheStats = getCacheStatistics(cacheName);
            cacheStats.setHits(stats.hitCount());
            cacheStats.setMisses(stats.missCount());
            cacheStats.setEvictions(stats.evictionCount());

        } catch (Exception e) {
            log.error("Failed to collect Caffeine cache info for: {}", cacheName, e);
        }
    }

    /**
     * Get cache statistics summary
     */
    public Map<String, CacheStatistics> getCacheStatisticsSummary() {
        return new HashMap<>(cacheStats);
    }

    /**
     * Get statistics for specific cache
     */
    public CacheStatistics getCacheStatistics(String cacheName, boolean createIfMissing) {
        if (createIfMissing) {
            return getCacheStatistics(cacheName);
        }
        return cacheStats.get(cacheName);
    }

    /**
     * Reset statistics for a cache
     */
    public void resetCacheStatistics(String cacheName) {
        CacheStatistics stats = cacheStats.get(cacheName);
        if (stats != null) {
            stats.reset();
            log.info("Reset statistics for cache: {}", cacheName);
        }
    }

    /**
     * Reset all cache statistics
     */
    public void resetAllCacheStatistics() {
        cacheStats.values().forEach(CacheStatistics::reset);
        log.info("Reset all cache statistics");
    }

    /**
     * Cache statistics holder
     */
    @lombok.Data
    public static class CacheStatistics {
        private final String cacheName;
        private final AtomicLong hits = new AtomicLong(0);
        private final AtomicLong misses = new AtomicLong(0);
        private final AtomicLong puts = new AtomicLong(0);
        private final AtomicLong evictions = new AtomicLong(0);
        private final AtomicLong clears = new AtomicLong(0);
        private volatile long currentSize = 0;
        private volatile long createdTime = System.currentTimeMillis();

        public CacheStatistics(String cacheName) {
            this.cacheName = cacheName;
        }

        public void incrementHits() {
            hits.incrementAndGet();
        }

        public void incrementMisses() {
            misses.incrementAndGet();
        }

        public void incrementPuts() {
            puts.incrementAndGet();
        }

        public void incrementEvictions() {
            evictions.incrementAndGet();
        }

        public void incrementClears() {
            clears.incrementAndGet();
        }

        public void setHits(long value) {
            hits.set(value);
        }

        public void setMisses(long value) {
            misses.set(value);
        }

        public void setEvictions(long value) {
            evictions.set(value);
        }

        public long getTotalRequests() {
            return hits.get() + misses.get();
        }

        public double getHitRatio() {
            long total = getTotalRequests();
            return total == 0 ? 0.0 : (double) hits.get() / total;
        }

        public double getMissRatio() {
            long total = getTotalRequests();
            return total == 0 ? 0.0 : (double) misses.get() / total;
        }

        public void reset() {
            hits.set(0);
            misses.set(0);
            puts.set(0);
            evictions.set(0);
            clears.set(0);
            currentSize = 0;
            createdTime = System.currentTimeMillis();
        }

        public Map<String, Object> toMap() {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("cacheName", cacheName);
            map.put("hits", hits.get());
            map.put("misses", misses.get());
            map.put("puts", puts.get());
            map.put("evictions", evictions.get());
            map.put("clears", clears.get());
            map.put("currentSize", currentSize);
            map.put("totalRequests", getTotalRequests());
            map.put("hitRatio", String.format("%.2f%%", getHitRatio() * 100));
            map.put("missRatio", String.format("%.2f%%", getMissRatio() * 100));
            map.put("uptimeMs", System.currentTimeMillis() - createdTime);
            return map;
        }
    }
}