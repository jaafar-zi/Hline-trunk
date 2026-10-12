package com.example.demo.config.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;

import java.util.concurrent.Callable;

/**
 * Multi-level cache implementation
 * Coordinates between L1 (Caffeine) and L2 (Redis) caches
 *
 * Read flow:
 * 1. Try L1 (local, fast)
 * 2. If L1 miss, try L2 (distributed)
 * 3. If L2 hit, populate L1 (cache warming)
 * 4. If both miss, return null (will fetch from DB)
 *
 * Write flow:
 * 1. Write to both L1 and L2 simultaneously
 *
 * Eviction flow:
 * 1. Evict from both L1 and L2
 *
 * @author Architecture Team
 * @version 1.0
 */
@Slf4j
public class MultiLevelCache implements Cache {

    private final String name;
    private final Cache l1Cache; // Caffeine
    private final Cache l2Cache; // Redis

    public MultiLevelCache(String name, Cache l1Cache, Cache l2Cache) {
        this.name = name;
        this.l1Cache = l1Cache;
        this.l2Cache = l2Cache;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Object getNativeCache() {
        return l2Cache.getNativeCache(); // Return Redis as primary
    }

    @Override
    public ValueWrapper get(Object key) {
        // Try L1 first (fastest)
        ValueWrapper l1Value = l1Cache.get(key);
        if (l1Value != null) {
            log.trace("L1 cache hit for key: {} in cache: {}", key, name);
            return l1Value;
        }

        log.trace("L1 cache miss for key: {} in cache: {}", key, name);

        // Try L2 if L1 miss
        ValueWrapper l2Value = l2Cache.get(key);
        if (l2Value != null) {
            log.trace("L2 cache hit for key: {} in cache: {}", key, name);

            // Populate L1 for next access (cache warming)
            l1Cache.put(key, l2Value.get());
            log.trace("Warmed L1 cache with key: {} in cache: {}", key, name);

            return l2Value;
        }

        log.trace("L2 cache miss for key: {} in cache: {}", key, name);
        return null;
    }

    @Override
    public <T> T get(Object key, Class<T> type) {
        // Try L1 first
        T l1Value = l1Cache.get(key, type);
        if (l1Value != null) {
            log.trace("L1 cache hit (typed) for key: {} in cache: {}", key, name);
            return l1Value;
        }

        // Try L2 if L1 miss
        T l2Value = l2Cache.get(key, type);
        if (l2Value != null) {
            log.trace("L2 cache hit (typed) for key: {} in cache: {}", key, name);

            // Populate L1
            l1Cache.put(key, l2Value);

            return l2Value;
        }

        return null;
    }

    @Override
    public <T> T get(Object key, Callable<T> valueLoader) {
        // Try L1 first
        T l1Value = l1Cache.get(key, (Class<T>) null);
        if (l1Value != null) {
            return l1Value;
        }

        // Try L2
        T l2Value = l2Cache.get(key, (Class<T>) null);
        if (l2Value != null) {
            l1Cache.put(key, l2Value);
            return l2Value;
        }

        // Load value using valueLoader
        try {
            T value = valueLoader.call();
            if (value != null) {
                put(key, value);
            }
            return value;
        } catch (Exception e) {
            throw new ValueRetrievalException(key, valueLoader, e);
        }
    }

    @Override
    public void put(Object key, Object value) {
        log.trace("Putting key: {} in cache: {}", key, name);

        // Write to both levels
        l1Cache.put(key, value);
        l2Cache.put(key, value);
    }

    @Override
    public ValueWrapper putIfAbsent(Object key, Object value) {
        ValueWrapper existingValue = get(key);
        if (existingValue == null) {
            put(key, value);
            return null;
        }
        return existingValue;
    }

    @Override
    public void evict(Object key) {
        log.trace("Evicting key: {} from cache: {}", key, name);

        // Evict from both levels
        l1Cache.evict(key);
        l2Cache.evict(key);
    }

    @Override
    public boolean evictIfPresent(Object key) {
        boolean l1Evicted = l1Cache.evictIfPresent(key);
        boolean l2Evicted = l2Cache.evictIfPresent(key);
        return l1Evicted || l2Evicted;
    }

    @Override
    public void clear() {
        log.debug("Clearing cache: {}", name);

        // Clear both levels
        l1Cache.clear();
        l2Cache.clear();
    }

    @Override
    public boolean invalidate() {
        boolean l1Invalidated = l1Cache.invalidate();
        boolean l2Invalidated = l2Cache.invalidate();
        return l1Invalidated && l2Invalidated;
    }

    /**
     * Get L1 cache (Caffeine)
     */
    public Cache getL1Cache() {
        return l1Cache;
    }

    /**
     * Get L2 cache (Redis)
     */
    public Cache getL2Cache() {
        return l2Cache;
    }
}