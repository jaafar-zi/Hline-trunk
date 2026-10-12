package com.example.demo.config.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Multi-level cache manager that combines L1 (Caffeine) and L2 (Redis) caches
 *
 * Cache hierarchy:
 * 1. Check L1 (Caffeine) - fastest, local
 * 2. If miss, check L2 (Redis) - distributed, shared
 * 3. If hit in L2, populate L1 for next access
 * 4. If miss in both, fetch from database
 *
 * Write operations update both L1 and L2
 * Eviction clears both levels
 *
 * @author Architecture Team
 * @version 1.0
 */
@Slf4j
public class MultiLevelCacheManager implements CacheManager {

    private final CacheManager l1CacheManager; // Caffeine
    private final CacheManager l2CacheManager; // Redis

    public MultiLevelCacheManager(
            CacheManager l1CacheManager,
            CacheManager l2CacheManager) {
        this.l1CacheManager = l1CacheManager;
        this.l2CacheManager = l2CacheManager;

        log.info("MultiLevelCacheManager initialized with L1: {} and L2: {}",
                l1CacheManager.getClass().getSimpleName(),
                l2CacheManager.getClass().getSimpleName());
    }

    @Override
    public Cache getCache(String name) {
        Cache l1Cache = l1CacheManager.getCache(name);
        Cache l2Cache = l2CacheManager.getCache(name);

        if (l1Cache == null && l2Cache == null) {
            log.warn("No cache found for name: {}", name);
            return null;
        }

        if (l1Cache == null) {
            log.debug("Only L2 cache available for: {}", name);
            return l2Cache;
        }

        if (l2Cache == null) {
            log.debug("Only L1 cache available for: {}", name);
            return l1Cache;
        }

        return new MultiLevelCache(name, l1Cache, l2Cache);
    }

    @Override
    public Collection<String> getCacheNames() {
        Set<String> names = new HashSet<>();
        names.addAll(l1CacheManager.getCacheNames());
        names.addAll(l2CacheManager.getCacheNames());
        return names;
    }

    /**
     * Get L1 cache manager (Caffeine)
     */
    public CacheManager getL1CacheManager() {
        return l1CacheManager;
    }

    /**
     * Get L2 cache manager (Redis)
     */
    public CacheManager getL2CacheManager() {
        return l2CacheManager;
    }
}