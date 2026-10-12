package com.example.demo.common.controller;

import com.example.demo.common.monitoring.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Monitoring and Metrics Controller
 */
@RestController
@RequestMapping("/api/monitoring")
@RequiredArgsConstructor
public class MonitoringController {

    private final BusinessMetrics businessMetrics;
    private final CacheMetrics cacheMetrics;
    private final CacheEventLogger cacheEventLogger;
    private final CacheVerifier cacheVerifier;

    @GetMapping("/metrics/business")
    public ResponseEntity<BusinessMetrics.MetricsSummary> getBusinessMetrics() {
        return ResponseEntity.ok(businessMetrics.getMetricsSummary());
    }

    @GetMapping("/metrics/cache")
    public ResponseEntity<Map<String, CacheMetrics.CacheStatistics>> getCacheMetrics() {
        return ResponseEntity.ok(cacheMetrics.getCacheStatisticsSummary());
    }

    @GetMapping("/metrics/cache/{cacheName}")
    public ResponseEntity<CacheMetrics.CacheStatistics> getCacheMetricsByName(
            @PathVariable String cacheName) {
        CacheMetrics.CacheStatistics stats =
                cacheMetrics.getCacheStatistics(cacheName, false);

        if (stats == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(stats);
    }

    @GetMapping("/cache/events")
    public ResponseEntity<List<CacheEventLogger.CacheEvent>> getCacheEvents(
            @RequestParam(defaultValue = "100") int limit) {
        return ResponseEntity.ok(cacheEventLogger.getRecentEvents(limit));
    }

    @GetMapping("/cache/events/{cacheName}")
    public ResponseEntity<List<CacheEventLogger.CacheEvent>> getCacheEventsByName(
            @PathVariable String cacheName,
            @RequestParam(defaultValue = "100") int limit) {
        return ResponseEntity.ok(
                cacheEventLogger.getEventsByCacheName(cacheName, limit)
        );
    }

    @GetMapping("/cache/verification")
    public ResponseEntity<Map<String, CacheVerifier.VerificationResult>> getCacheVerification() {
        return ResponseEntity.ok(cacheVerifier.getVerificationReport());
    }

    @PostMapping("/cache/verify/{cacheName}")
    public ResponseEntity<CacheVerifier.VerificationResult> verifyCacheByName(
            @PathVariable String cacheName) {
        CacheVerifier.VerificationResult result =
                cacheVerifier.verifyCache(cacheName);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/cache/heal/{cacheName}")
    public ResponseEntity<Map<String, Object>> healCache(
            @PathVariable String cacheName) {
        boolean success = cacheVerifier.healCache(cacheName);

        Map<String, Object> response = new HashMap<>();
        response.put("cacheName", cacheName);
        response.put("healed", success);
        response.put("timestamp", System.currentTimeMillis());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/cache/reset-stats/{cacheName}")
    public ResponseEntity<Void> resetCacheStats(@PathVariable String cacheName) {
        cacheMetrics.resetCacheStatistics(cacheName);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/cache/health")
    public ResponseEntity<Map<String, Object>> getCacheHealth() {
        Map<String, Object> health = new HashMap<>();
        health.put("allHealthy", cacheVerifier.areAllCachesHealthy());
        health.put("unhealthyCaches", cacheVerifier.getUnhealthyCaches());
        health.put("lastVerification", cacheVerifier.getLastVerificationTime());

        return ResponseEntity.ok(health);
    }
}
