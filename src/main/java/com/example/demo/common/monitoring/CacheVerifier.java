package com.example.demo.common.monitoring;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cache Verifier and Integrity Checker
 *
 * Features:
 * - Cache integrity verification
 * - Cache consistency checking
 * - Stale data detection
 * - Cache corruption detection
 * - Automatic cache healing
 * - Cache validation reports
 *
 * @author Architecture Team
 * @version 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CacheVerifier {

    private final CacheManager cacheManager;
    private final RedisTemplate<String, Object> redisTemplate;
    private final CacheEventLogger cacheEventLogger;

    private static final long STALE_THRESHOLD_MINUTES = 60;
    private static final int MAX_VERIFICATION_ERRORS = 10;

    // Verification results storage
    private final Map<String, VerificationResult> verificationResults = new ConcurrentHashMap<>();
    private LocalDateTime lastVerificationTime;

    /**
     * Verify all caches periodically
     */
    @Scheduled(cron = "0 */30 * * * *") // Every 30 minutes
    public void verifyAllCaches() {
        log.info("Starting cache verification");
        lastVerificationTime = LocalDateTime.now();

        Collection<String> cacheNames = cacheManager.getCacheNames();
        int totalCaches = cacheNames.size();
        int healthyCaches = 0;
        int unhealthyCaches = 0;

        for (String cacheName : cacheNames) {
            try {
                VerificationResult result = verifyCache(cacheName);
                verificationResults.put(cacheName, result);

                if (result.isHealthy()) {
                    healthyCaches++;
                } else {
                    unhealthyCaches++;
                    log.warn("Cache verification failed for: {} - {}",
                            cacheName, result.getIssues());
                }

            } catch (Exception e) {
                log.error("Error verifying cache: {}", cacheName, e);
                unhealthyCaches++;
            }
        }

        log.info("Cache verification completed: Total={}, Healthy={}, Unhealthy={}",
                totalCaches, healthyCaches, unhealthyCaches);
    }

    /**
     * Verify specific cache
     */
    public VerificationResult verifyCache(String cacheName) {
        log.debug("Verifying cache: {}", cacheName);

        VerificationResult result = new VerificationResult(cacheName);
        Cache cache = cacheManager.getCache(cacheName);

        if (cache == null) {
            result.addIssue("Cache not found");
            return result;
        }

        // Check 1: Verify cache is accessible
        if (!verifyCacheAccessibility(cache, result)) {
            return result;
        }

        // Check 2: Verify cache size is reasonable
        verifyCacheSize(cacheName, result);

        // Check 3: Check for stale entries
        checkStaleEntries(cacheName, result);

        // Check 4: Verify cache configuration
        verifyCacheConfiguration(cache, result);

        // Check 5: Test cache operations
        testCacheOperations(cache, result);

        result.setVerified(true);
        result.setVerificationTime(LocalDateTime.now());

        return result;
    }

    /**
     * Verify cache accessibility
     */
    private boolean verifyCacheAccessibility(Cache cache, VerificationResult result) {
        try {
            Object nativeCache = cache.getNativeCache();
            if (nativeCache == null) {
                result.addIssue("Native cache is null");
                return false;
            }
            return true;
        } catch (Exception e) {
            result.addIssue("Cache not accessible: " + e.getMessage());
            return false;
        }
    }

    /**
     * Verify cache size
     */
        /**
     * Verify cache size
     */
    private void verifyCacheSize(String cacheName, VerificationResult result) {
        try {
            // Get cache size from metrics - use the correct type
            CacheEventLogger.CacheOperationStats stats =
                    cacheEventLogger.getOperationStatistics().get(cacheName);

            if (stats != null) {
                long totalOps = stats.getTotalOperations();
                result.setCacheSize(totalOps);

                // Check cache health based on operations
                long getCount = stats.getGetCount();
                long putCount = stats.getPutCount();
                
                if (putCount > 0 && stats.getHitRatio() < 0.5) {
                    result.addWarning("Low cache hit ratio: " + 
                            String.format("%.2f%%", stats.getHitRatio() * 100));
                }

                if (getCount > 0 && putCount == 0) {
                    result.addWarning("Cache has reads but no writes detected");
                }
            }

        } catch (Exception e) {
            result.addIssue("Failed to verify cache size: " + e.getMessage());
        }
    }

    /**
     * Check for stale entries
     */
    private void checkStaleEntries(String cacheName, VerificationResult result) {
        try {
            // This is a simplified check - implement based on your cache metadata
            CacheEventLogger.CacheOperationStats stats =
                    cacheEventLogger.getOperationStatistics().get(cacheName);

            if (stats != null) {
                long uptimeMs = System.currentTimeMillis() - stats.getCreatedTime();
                long staleThresholdMs = STALE_THRESHOLD_MINUTES * 60 * 1000;

                if (uptimeMs > staleThresholdMs && stats.getPutCount() == 0) {
                    result.addWarning("No cache updates in " +
                            STALE_THRESHOLD_MINUTES + " minutes");
                }
            }

        } catch (Exception e) {
            result.addIssue("Failed to check stale entries: " + e.getMessage());
        }
    }

    /**
     * Verify cache configuration
     */
    private void verifyCacheConfiguration(Cache cache, VerificationResult result) {
        try {
            String cacheType = cache.getClass().getSimpleName();
            result.setCacheType(cacheType);

            // Add configuration details
            Map<String, String> config = new HashMap<>();
            config.put("type", cacheType);
            config.put("name", cache.getName());
            result.setConfiguration(config);

        } catch (Exception e) {
            result.addIssue("Failed to verify configuration: " + e.getMessage());
        }
    }

    /**
     * Test cache operations
     */
    private void testCacheOperations(Cache cache, VerificationResult result) {
        String testKey = "__cache_verification_test_key__";
        String testValue = "test_value_" + System.currentTimeMillis();

        try {
            // Test PUT
            cache.put(testKey, testValue);

            // Test GET
            Cache.ValueWrapper wrapper = cache.get(testKey);
            if (wrapper == null || !testValue.equals(wrapper.get())) {
                result.addIssue("Cache GET operation failed");
                return;
            }

            // Test EVICT
            cache.evict(testKey);
            wrapper = cache.get(testKey);
            if (wrapper != null) {
                result.addIssue("Cache EVICT operation failed");
                return;
            }

            result.setOperationsHealthy(true);

        } catch (Exception e) {
            result.addIssue("Cache operations test failed: " + e.getMessage());
            result.setOperationsHealthy(false);
        }
    }

    /**
     * Get verification report for all caches
     */
    public Map<String, VerificationResult> getVerificationReport() {
        return new HashMap<>(verificationResults);
    }

    /**
     * Get verification result for specific cache
     */
    public VerificationResult getVerificationResult(String cacheName) {
        return verificationResults.get(cacheName);
    }

    /**
     * Get last verification time
     */
    public LocalDateTime getLastVerificationTime() {
        return lastVerificationTime;
    }

    /**
     * Check if all caches are healthy
     */
    public boolean areAllCachesHealthy() {
        return verificationResults.values().stream()
                .allMatch(VerificationResult::isHealthy);
    }

    /**
     * Get unhealthy caches
     */
    public List<String> getUnhealthyCaches() {
        return verificationResults.entrySet().stream()
                .filter(entry -> !entry.getValue().isHealthy())
                .map(Map.Entry::getKey)
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Heal cache (attempt to fix issues)
     */
    public boolean healCache(String cacheName) {
        log.info("Attempting to heal cache: {}", cacheName);

        try {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache == null) {
                log.error("Cannot heal cache - cache not found: {}", cacheName);
                return false;
            }

            // Clear potentially corrupted cache
            cache.clear();
            cacheEventLogger.logCacheClear(cacheName);

            // Verify after healing
            VerificationResult result = verifyCache(cacheName);

            if (result.isHealthy()) {
                log.info("Cache healed successfully: {}", cacheName);
                return true;
            } else {
                log.warn("Cache healing did not resolve all issues: {}",
                        result.getIssues());
                return false;
            }

        } catch (Exception e) {
            log.error("Failed to heal cache: {}", cacheName, e);
            return false;
        }
    }

    /**
     * Verification Result DTO
     */
    @lombok.Data
    public static class VerificationResult {
        private final String cacheName;
        private boolean verified = false;
        private boolean healthy = true;
        private boolean operationsHealthy = false;
        private LocalDateTime verificationTime;
        private String cacheType;
        private long cacheSize;
        private Map<String, String> configuration = new HashMap<>();
        private List<String> issues = new ArrayList<>();
        private List<String> warnings = new ArrayList<>();

        public VerificationResult(String cacheName) {
            this.cacheName = cacheName;
        }

        public void addIssue(String issue) {
            issues.add(issue);
            healthy = false;
        }

        public void addWarning(String warning) {
            warnings.add(warning);
        }

        public boolean isHealthy() {
            return healthy && issues.isEmpty();
        }

        public Map<String, Object> toMap() {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("cacheName", cacheName);
            map.put("verified", verified);
            map.put("healthy", isHealthy());
            map.put("operationsHealthy", operationsHealthy);
            map.put("verificationTime", verificationTime);
            map.put("cacheType", cacheType);
            map.put("cacheSize", cacheSize);
            map.put("configuration", configuration);
            map.put("issues", issues);
            map.put("warnings", warnings);
            return map;
        }
    }
}