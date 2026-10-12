package com.example.demo.common.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.ThreadMXBean;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Aspect
@Component
@Slf4j
public class PerformanceAspect {

    private final RedisTemplate<String, Object> redisTemplate;
    private final boolean redisAvailable;

    // Dedicated single thread for async Redis writes
    // Never blocks the main thread or any transaction
    private final ExecutorService redisWriter =
            Executors.newSingleThreadExecutor(r -> {
                Thread t = new Thread(r, "perf-redis-writer");
                t.setDaemon(true);
                return t;
            });

    @Value("${performance.monitoring.enabled:true}")
    private boolean monitoringEnabled;

    @Value("${performance.slow.threshold.ms:1000}")
    private long slowThresholdMs;

    @Value("${performance.critical.threshold.ms:5000}")
    private long criticalThresholdMs;

    private static final String PERFORMANCE_METRICS_PREFIX = "perf:metrics:";
    private static final String SLOW_METHODS_KEY = "perf:slow-methods";
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    private final Map<String, PerformanceStats> performanceStatsCache = new ConcurrentHashMap<>();
    private final MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
    private final ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();

    public PerformanceAspect(
            @Autowired(required = false) RedisTemplate<String, Object> redisTemplate
    ) {
        this.redisTemplate = redisTemplate;
        this.redisAvailable = redisTemplate != null;

        if (this.redisAvailable) {
            log.info("PerformanceAspect initialized WITH Redis — writes are async");
        } else {
            log.warn("PerformanceAspect initialized WITHOUT Redis — in-memory only");
        }
    }

    @Pointcut("execution(* com.example.demo..service..*(..)) || " +
            "execution(* com.example.demo..controller..*(..)) || " +
            "execution(* com.example.demo..repository..*(..))")
    public void monitoredMethods() {}

    @Around("monitoredMethods()")
    public Object monitorPerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        if (!monitoringEnabled) {
            return joinPoint.proceed();
        }

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String methodKey = getMethodKey(signature);

        PerformanceMetrics metrics = new PerformanceMetrics();
        metrics.setMethodName(methodKey);
        metrics.setStartTime(System.currentTimeMillis());
        metrics.setStartTimestamp(LocalDateTime.now());

        captureSystemMetrics(metrics, true);

        Throwable exception = null;

        try {
            return joinPoint.proceed();
        } catch (Throwable t) {
            exception = t;
            metrics.setFailed(true);
            throw t;
        } finally {
            long executionTime = System.currentTimeMillis() - metrics.getStartTime();
            metrics.setExecutionTimeMs(executionTime);
            metrics.setEndTime(System.currentTimeMillis());
            metrics.setEndTimestamp(LocalDateTime.now());

            captureSystemMetrics(metrics, false);

            // Everything after this runs synchronously but Redis writes are async
            updateStatistics(methodKey, executionTime, exception != null);
            checkPerformanceThresholds(methodKey, executionTime, metrics);

            // Redis write is fire-and-forget on a separate thread
            // NEVER blocks the calling thread or any active DB transaction
            writeMetricsAsync(methodKey, metrics, exception);
        }
    }

    private void writeMetricsAsync(
            String methodKey,
            PerformanceMetrics metrics,
            Throwable exception
    ) {
        if (!redisAvailable) return;

        redisWriter.submit(() -> {
            try {
                String metricsKey = PERFORMANCE_METRICS_PREFIX + methodKey
                        + ":" + System.currentTimeMillis();
                redisTemplate.opsForValue().set(metricsKey, metrics, 1, TimeUnit.HOURS);

                if (metrics.getExecutionTimeMs() > slowThresholdMs) {
                    logDetailedMetrics(methodKey, metrics, exception);
                }
            } catch (Exception e) {
                log.warn("Async Redis write failed for {}: {}", methodKey, e.getMessage());
            }
        });
    }

    private void captureSystemMetrics(PerformanceMetrics metrics, boolean isStart) {
        try {
            long heapUsed = memoryMXBean.getHeapMemoryUsage().getUsed();
            long nonHeapUsed = memoryMXBean.getNonHeapMemoryUsage().getUsed();
            int threadCount = threadMXBean.getThreadCount();

            if (isStart) {
                metrics.setHeapMemoryBefore(heapUsed);
                metrics.setNonHeapMemoryBefore(nonHeapUsed);
                metrics.setThreadCountBefore(threadCount);
            } else {
                metrics.setHeapMemoryAfter(heapUsed);
                metrics.setNonHeapMemoryAfter(nonHeapUsed);
                metrics.setThreadCountAfter(threadCount);
                metrics.setMemoryUsed(heapUsed - metrics.getHeapMemoryBefore());
            }
        } catch (Exception e) {
            log.warn("Failed to capture system metrics: {}", e.getMessage());
        }
    }

    private void updateStatistics(String methodKey, long executionTime, boolean failed) {
        PerformanceStats stats = performanceStatsCache.computeIfAbsent(
                methodKey, k -> new PerformanceStats()
        );

        synchronized (stats) {
            stats.incrementInvocationCount();
            stats.addExecutionTime(executionTime);
            if (failed) stats.incrementFailureCount();
            if (executionTime > stats.getMaxExecutionTime())
                stats.setMaxExecutionTime(executionTime);
            if (executionTime < stats.getMinExecutionTime() || stats.getMinExecutionTime() == 0)
                stats.setMinExecutionTime(executionTime);
            stats.setLastExecutionTime(executionTime);
            stats.setLastInvocationTime(LocalDateTime.now());
        }
    }

    private void checkPerformanceThresholds(
            String methodKey,
            long executionTime,
            PerformanceMetrics metrics
    ) {
        if (executionTime > criticalThresholdMs) {
            logCriticalPerformance(methodKey, executionTime, metrics);
            recordSlowMethodAsync(methodKey, executionTime, "CRITICAL");
        } else if (executionTime > slowThresholdMs) {
            logSlowPerformance(methodKey, executionTime, metrics);
            recordSlowMethodAsync(methodKey, executionTime, "SLOW");
        }
    }

    private void logDetailedMetrics(
            String methodKey,
            PerformanceMetrics metrics,
            Throwable exception
    ) {
        Map<String, Object> detailsMap = new LinkedHashMap<>();
        detailsMap.put("method", methodKey);
        detailsMap.put("executionTimeMs", metrics.getExecutionTimeMs());
        detailsMap.put("memoryUsedBytes", metrics.getMemoryUsed());
        detailsMap.put("memoryUsedMB", metrics.getMemoryUsed() / (1024 * 1024));
        detailsMap.put("threadCountBefore", metrics.getThreadCountBefore());
        detailsMap.put("threadCountAfter", metrics.getThreadCountAfter());
        detailsMap.put("startTime", metrics.getStartTimestamp().format(FORMATTER));
        detailsMap.put("endTime", metrics.getEndTimestamp().format(FORMATTER));
        detailsMap.put("failed", metrics.isFailed());
        if (exception != null) detailsMap.put("exception", exception.getClass().getName());
        log.debug("Performance Metrics: {}", detailsMap);
    }

    private void logSlowPerformance(String methodKey, long executionTime, PerformanceMetrics metrics) {
        log.warn("SLOW METHOD: {} took {}ms (threshold: {}ms). Memory: {} bytes",
                methodKey, executionTime, slowThresholdMs, metrics.getMemoryUsed());
    }

    private void logCriticalPerformance(String methodKey, long executionTime, PerformanceMetrics metrics) {
        log.error("CRITICAL PERFORMANCE: {} took {}ms (threshold: {}ms). Memory: {} bytes",
                methodKey, executionTime, criticalThresholdMs, metrics.getMemoryUsed());
        List<String> suggestions = generateOptimizationSuggestions(methodKey, metrics);
        if (!suggestions.isEmpty()) {
            log.error("Suggestions for {}: {}", methodKey, suggestions);
        }
    }

    private void recordSlowMethodAsync(String methodKey, long executionTime, String severity) {
        if (!redisAvailable) return;

        redisWriter.submit(() -> {
            try {
                Map<String, Object> slowMethod = new HashMap<>();
                slowMethod.put("method", methodKey);
                slowMethod.put("executionTimeMs", executionTime);
                slowMethod.put("severity", severity);
                slowMethod.put("timestamp", LocalDateTime.now().format(FORMATTER));

                redisTemplate.opsForList().leftPush(SLOW_METHODS_KEY, slowMethod);
                redisTemplate.expire(SLOW_METHODS_KEY, 24, TimeUnit.HOURS);
                redisTemplate.opsForList().trim(SLOW_METHODS_KEY, 0, 99);
            } catch (Exception e) {
                log.warn("Failed to record slow method async: {}", e.getMessage());
            }
        });
    }

    private List<String> generateOptimizationSuggestions(
            String methodKey,
            PerformanceMetrics metrics
    ) {
        List<String> suggestions = new ArrayList<>();

        long memoryUsedMB = metrics.getMemoryUsed() / (1024 * 1024);
        if (memoryUsedMB > 100) {
            suggestions.add("High memory usage (" + memoryUsedMB
                    + "MB). Consider pagination or streaming.");
        }
        if (metrics.getExecutionTimeMs() > 10000) {
            suggestions.add("Very long execution. Consider async processing.");
        }

        PerformanceStats stats = performanceStatsCache.get(methodKey);
        if (stats != null) {
            if (stats.getInvocationCount() > 1000
                    && stats.getAverageExecutionTime() > slowThresholdMs) {
                suggestions.add("Frequently called slow method. Consider caching.");
            }
            if (stats.getFailureRate() > 0.1) {
                suggestions.add("High failure rate ("
                        + String.format("%.2f%%", stats.getFailureRate() * 100)
                        + "). Add circuit breaker.");
            }
        }

        if (methodKey.contains("Repository") || methodKey.contains("Dao")) {
            suggestions.add("DB operation. Verify indexing and query optimization.");
        }

        return suggestions;
    }

    private String getMethodKey(MethodSignature signature) {
        return signature.getDeclaringTypeName() + "." + signature.getName();
    }

    public PerformanceStats getStatistics(String methodKey) {
        return performanceStatsCache.get(methodKey);
    }

    public Map<String, PerformanceStats> getAllStatistics() {
        return new HashMap<>(performanceStatsCache);
    }

    public void resetStatistics() {
        performanceStatsCache.clear();
        log.info("Performance statistics reset");
    }

    public List<Object> getSlowMethods(int count) {
        if (!redisAvailable) return Collections.emptyList();
        try {
            return redisTemplate.opsForList().range(SLOW_METHODS_KEY, 0, count - 1);
        } catch (Exception e) {
            log.error("Failed to get slow methods: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    @lombok.Data
    public static class PerformanceMetrics {
        private String methodName;
        private long startTime;
        private long endTime;
        private long executionTimeMs;
        private LocalDateTime startTimestamp;
        private LocalDateTime endTimestamp;
        private long heapMemoryBefore;
        private long heapMemoryAfter;
        private long nonHeapMemoryBefore;
        private long nonHeapMemoryAfter;
        private long memoryUsed;
        private int threadCountBefore;
        private int threadCountAfter;
        private boolean failed;
    }

    @lombok.Data
    public static class PerformanceStats {
        private long invocationCount = 0;
        private long failureCount = 0;
        private long totalExecutionTime = 0;
        private long minExecutionTime = 0;
        private long maxExecutionTime = 0;
        private long lastExecutionTime = 0;
        private LocalDateTime lastInvocationTime;

        public void incrementInvocationCount() { invocationCount++; }
        public void incrementFailureCount()    { failureCount++; }
        public void addExecutionTime(long t)   { totalExecutionTime += t; }

        public double getAverageExecutionTime() {
            return invocationCount > 0 ? (double) totalExecutionTime / invocationCount : 0;
        }

        public double getFailureRate() {
            return invocationCount > 0 ? (double) failureCount / invocationCount : 0;
        }
    }
}