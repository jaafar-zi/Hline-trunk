package com.example.demo.common.monitoring;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cache Event Logger
 *
 * Features:
 * - Cache operation logging
 * - Cache event tracking
 * - Cache performance monitoring
 * - Cache hit/miss analytics
 * - Cache operation history
 * - Alert on cache issues
 *
 * @author Architecture Team
 * @version 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CacheEventLogger {

    private final CacheManager cacheManager;
    private final CacheMetrics cacheMetrics;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    private static final int MAX_EVENT_HISTORY = 1000;

    // Event history storage
    private final Queue<CacheEvent> eventHistory = new LinkedList<>();
    private final Map<String, CacheOperationStats> operationStats = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        log.info("Cache Event Logger initialized");
        logCacheConfiguration();
    }

    /**
     * Log cache configuration on startup
     */
    private void logCacheConfiguration() {
        Collection<String> cacheNames = cacheManager.getCacheNames();
        log.info("Configured caches: {}", cacheNames);

        for (String cacheName : cacheNames) {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                log.info("Cache [{}] - Type: {}", cacheName, cache.getClass().getSimpleName());
            }
        }
    }

    /**
     * Log cache get operation
     */
    public void logCacheGet(String cacheName, Object key, boolean hit) {
        CacheEvent event = CacheEvent.builder()
                .cacheName(cacheName)
                .operation(CacheOperation.GET)
                .key(String.valueOf(key))
                .hit(hit)
                .timestamp(LocalDateTime.now())
                .build();

        addEventToHistory(event);
        updateOperationStats(cacheName, CacheOperation.GET, hit);

        if (hit) {
            cacheMetrics.recordCacheHit(cacheName);
            log.debug("Cache HIT: cache={}, key={}", cacheName, maskKey(key));
        } else {
            cacheMetrics.recordCacheMiss(cacheName);
            log.debug("Cache MISS: cache={}, key={}", cacheName, maskKey(key));
        }
    }

    /**
     * Log cache put operation
     */
    public void logCachePut(String cacheName, Object key, Object value) {
        long valueSize = estimateSize(value);

        CacheEvent event = CacheEvent.builder()
                .cacheName(cacheName)
                .operation(CacheOperation.PUT)
                .key(String.valueOf(key))
                .valueSize(valueSize)
                .timestamp(LocalDateTime.now())
                .build();

        addEventToHistory(event);
        updateOperationStats(cacheName, CacheOperation.PUT, true);
        cacheMetrics.recordCachePut(cacheName, valueSize);

        log.debug("Cache PUT: cache={}, key={}, size={} bytes",
                cacheName, maskKey(key), valueSize);
    }

    /**
     * Log cache evict operation
     */
    public void logCacheEvict(String cacheName, Object key) {
        CacheEvent event = CacheEvent.builder()
                .cacheName(cacheName)
                .operation(CacheOperation.EVICT)
                .key(String.valueOf(key))
                .timestamp(LocalDateTime.now())
                .build();

        addEventToHistory(event);
        updateOperationStats(cacheName, CacheOperation.EVICT, true);
        cacheMetrics.recordCacheEviction(cacheName, "manual");

        log.debug("Cache EVICT: cache={}, key={}", cacheName, maskKey(key));
    }

    /**
     * Log cache clear operation
     */
    public void logCacheClear(String cacheName) {
        CacheEvent event = CacheEvent.builder()
                .cacheName(cacheName)
                .operation(CacheOperation.CLEAR)
                .timestamp(LocalDateTime.now())
                .build();

        addEventToHistory(event);
        updateOperationStats(cacheName, CacheOperation.CLEAR, true);
        cacheMetrics.recordCacheClear(cacheName);

        log.info("Cache CLEAR: cache={}", cacheName);
    }

    /**
     * Log cache load operation with timing
     */
    public void logCacheLoad(String cacheName, Object key, long loadTimeMs) {
        CacheEvent event = CacheEvent.builder()
                .cacheName(cacheName)
                .operation(CacheOperation.LOAD)
                .key(String.valueOf(key))
                .loadTimeMs(loadTimeMs)
                .timestamp(LocalDateTime.now())
                .build();

        addEventToHistory(event);
        cacheMetrics.recordCacheLoadTime(cacheName, loadTimeMs);

        if (loadTimeMs > 1000) {
            log.warn("SLOW Cache LOAD: cache={}, key={}, loadTime={}ms",
                    cacheName, maskKey(key), loadTimeMs);
        } else {
            log.debug("Cache LOAD: cache={}, key={}, loadTime={}ms",
                    cacheName, maskKey(key), loadTimeMs);
        }
    }

    /**
     * Log cache error
     */
    public void logCacheError(String cacheName, String operation, Exception error) {
        CacheEvent event = CacheEvent.builder()
                .cacheName(cacheName)
                .operation(CacheOperation.ERROR)
                .errorMessage(error.getMessage())
                .timestamp(LocalDateTime.now())
                .build();

        addEventToHistory(event);

        log.error("Cache ERROR: cache={}, operation={}, error={}",
                cacheName, operation, error.getMessage(), error);
    }

    /**
     * Log cache statistics periodically
     */
    @Async
    public void logCacheStatistics() {
        Collection<String> cacheNames = cacheManager.getCacheNames();

        log.info("=== Cache Statistics ===");

        for (String cacheName : cacheNames) {
            CacheOperationStats stats = operationStats.get(cacheName);
            if (stats != null) {
                log.info("Cache [{}]: Gets={}, Puts={}, Evicts={}, Clears={}, HitRatio={:.2f}%",
                        cacheName,
                        stats.getGetCount(),
                        stats.getPutCount(),
                        stats.getEvictCount(),
                        stats.getClearCount(),
                        stats.getHitRatio() * 100);
            }
        }

        log.info("========================");
    }

    /**
     * Get cache operation statistics
     */
    public Map<String, CacheOperationStats> getOperationStatistics() {
        return new HashMap<>(operationStats);
    }

    /**
     * Get recent cache events
     */
    public List<CacheEvent> getRecentEvents(int limit) {
        synchronized (eventHistory) {
            return eventHistory.stream()
                    .limit(limit)
                    .collect(java.util.stream.Collectors.toList());
        }
    }

    /**
     * Get events by cache name
     */
    public List<CacheEvent> getEventsByCacheName(String cacheName, int limit) {
        synchronized (eventHistory) {
            return eventHistory.stream()
                    .filter(event -> event.getCacheName().equals(cacheName))
                    .limit(limit)
                    .collect(java.util.stream.Collectors.toList());
        }
    }

    /**
     * Clear event history
     */
    public void clearEventHistory() {
        synchronized (eventHistory) {
            eventHistory.clear();
            log.info("Cache event history cleared");
        }
    }

    /**
     * Reset operation statistics
     */
    public void resetOperationStatistics() {
        operationStats.clear();
        log.info("Cache operation statistics reset");
    }

    /**
     * Add event to history
     */
    private void addEventToHistory(CacheEvent event) {
        synchronized (eventHistory) {
            eventHistory.offer(event);

            // Maintain max size
            while (eventHistory.size() > MAX_EVENT_HISTORY) {
                eventHistory.poll();
            }
        }
    }

    /**
     * Update operation statistics
     */
    private void updateOperationStats(String cacheName, CacheOperation operation, boolean success) {
        CacheOperationStats stats = operationStats.computeIfAbsent(
                cacheName,
                k -> new CacheOperationStats()
        );

        switch (operation) {
            case GET:
                stats.incrementGetCount();
                if (success) {
                    stats.incrementHitCount();
                }
                break;
            case PUT:
                stats.incrementPutCount();
                break;
            case EVICT:
                stats.incrementEvictCount();
                break;
            case CLEAR:
                stats.incrementClearCount();
                break;
        }
    }

    /**
     * Estimate object size
     */
    private long estimateSize(Object value) {
        if (value == null) {
            return 0;
        }

        // Simple estimation - can be improved with instrumentation
        if (value instanceof String) {
            return ((String) value).length() * 2L; // Approximate
        } else if (value instanceof Collection) {
            return ((Collection<?>) value).size() * 100L; // Very rough estimate
        } else if (value instanceof Map) {
            return ((Map<?, ?>) value).size() * 100L;
        }

        return 100L; // Default estimate
    }

    /**
     * Mask sensitive key information
     */
    private String maskKey(Object key) {
        if (key == null) {
            return "null";
        }

        String keyStr = key.toString();

        // Mask if key contains sensitive patterns
        if (keyStr.toLowerCase().contains("password") ||
                keyStr.toLowerCase().contains("token") ||
                keyStr.toLowerCase().contains("secret")) {
            return "***MASKED***";
        }

        // Truncate long keys
        if (keyStr.length() > 50) {
            return keyStr.substring(0, 47) + "...";
        }

        return keyStr;
    }

    /**
     * Cache Event DTO
     */
    @lombok.Data
    @lombok.Builder
    public static class CacheEvent {
        private String cacheName;
        private CacheOperation operation;
        private String key;
        private boolean hit;
        private long valueSize;
        private long loadTimeMs;
        private String errorMessage;
        private LocalDateTime timestamp;

        public String getFormattedTimestamp() {
            return timestamp.format(FORMATTER);
        }
    }

    /**
     * Cache Operation Type
     */
    public enum CacheOperation {
        GET,
        PUT,
        EVICT,
        CLEAR,
        LOAD,
        ERROR
    }

    /**
     * Cache Operation Statistics
     */
        @lombok.Data
        public static class CacheOperationStats {
        private long getCount = 0;
        private long putCount = 0;
        private long evictCount = 0;
        private long clearCount = 0;
        private long hitCount = 0;
        private long createdTime = System.currentTimeMillis();  
        private long lastUpdatedTime = System.currentTimeMillis();  

        public void incrementGetCount() {
            getCount++;
            this.lastUpdatedTime = System.currentTimeMillis();
        }

        public void incrementPutCount() {
            putCount++;
            this.lastUpdatedTime = System.currentTimeMillis();
        }

        public void incrementEvictCount() {
            evictCount++;
            this.lastUpdatedTime = System.currentTimeMillis();
        }

        public void incrementClearCount() {
            clearCount++;
            this.lastUpdatedTime = System.currentTimeMillis();
        }

        public void incrementHitCount() {
            hitCount++;
            this.lastUpdatedTime = System.currentTimeMillis();
        }

        public double getHitRatio() {
            return getCount == 0 ? 0.0 : (double) hitCount / getCount;
        }

        public long getTotalOperations() {
            return getCount + putCount + evictCount + clearCount;
        }
    }
}