package com.example.demo.common.monitoring;

import io.micrometer.core.instrument.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Business Metrics Collection and Reporting
 *
 * Features:
 * - Custom business metrics
 * - Counter metrics
 * - Gauge metrics
 * - Timer metrics
 * - Distribution summaries
 * - Tagged metrics
 * - Real-time monitoring
 *
 * @author Architecture Team
 * @version 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BusinessMetrics {

    private final MeterRegistry meterRegistry;

    // Metric name prefixes
    private static final String METRIC_PREFIX = "business.";
    private static final String USER_PREFIX = METRIC_PREFIX + "user.";
    private static final String ORDER_PREFIX = METRIC_PREFIX + "order.";
    private static final String PAYMENT_PREFIX = METRIC_PREFIX + "payment.";
    private static final String API_PREFIX = METRIC_PREFIX + "api.";
    private static final String PRODUCT_PREFIX = METRIC_PREFIX + "product.";

    // Cached metrics for performance
    private final Map<String, Counter> counterCache = new ConcurrentHashMap<>();
    private final Map<String, Timer> timerCache = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> gaugeCache = new ConcurrentHashMap<>();

    // ========== User Metrics ==========

    /**
     * Track user registration
     */
    public void recordUserRegistration(String registrationSource) {
        incrementCounter(USER_PREFIX + "registrations.total",
                "source", registrationSource);
        log.debug("Recorded user registration from source: {}", registrationSource);
    }

    /**
     * Track user login
     */
    public void recordUserLogin(String loginMethod, boolean success) {
        String status = success ? "success" : "failure";
        incrementCounter(USER_PREFIX + "logins.total",
                "method", loginMethod,
                "status", status);
        log.debug("Recorded user login: method={}, success={}", loginMethod, success);
    }

    /**
     * Track active users
     */
    public void updateActiveUsers(long count) {
        updateGauge(USER_PREFIX + "active.count", count);
    }

    /**
     * Track user session duration
     */
    public void recordSessionDuration(Duration duration, String userType) {
        recordTimer(USER_PREFIX + "session.duration", duration,
                "userType", userType);
    }

    // ========== Order Metrics ==========

    /**
     * Track order creation
     */
    public void recordOrderCreated(String orderType, double amount) {
        incrementCounter(ORDER_PREFIX + "created.total",
                "type", orderType);

        recordDistributionSummary(ORDER_PREFIX + "amount", amount,
                "type", orderType);

        log.debug("Recorded order creation: type={}, amount={}", orderType, amount);
    }

    /**
     * Track order completion
     */
    public void recordOrderCompleted(String orderType, Duration processingTime) {
        incrementCounter(ORDER_PREFIX + "completed.total",
                "type", orderType);

        recordTimer(ORDER_PREFIX + "processing.time", processingTime,
                "type", orderType);
    }

    /**
     * Track order cancellation
     */
    public void recordOrderCancelled(String orderType, String reason) {
        incrementCounter(ORDER_PREFIX + "cancelled.total",
                "type", orderType,
                "reason", reason);
    }

    /**
     * Track order value
     */
    public void recordOrderValue(double value, String currency) {
        recordDistributionSummary(ORDER_PREFIX + "value", value,
                "currency", currency);
    }

    /**
     * Update pending orders count
     */
    public void updatePendingOrders(long count) {
        updateGauge(ORDER_PREFIX + "pending.count", count);
    }

    // ========== Payment Metrics ==========

    /**
     * Track payment attempt
     */
    public void recordPaymentAttempt(String paymentMethod, String provider) {
        incrementCounter(PAYMENT_PREFIX + "attempts.total",
                "method", paymentMethod,
                "provider", provider);
    }

    /**
     * Track payment success
     */
    public void recordPaymentSuccess(String paymentMethod, double amount, String currency) {
        incrementCounter(PAYMENT_PREFIX + "success.total",
                "method", paymentMethod);

        recordDistributionSummary(PAYMENT_PREFIX + "amount.success", amount,
                "method", paymentMethod,
                "currency", currency);
    }

    /**
     * Track payment failure
     */
    public void recordPaymentFailure(String paymentMethod, String errorCode) {
        incrementCounter(PAYMENT_PREFIX + "failures.total",
                "method", paymentMethod,
                "errorCode", errorCode);
    }

    /**
     * Track payment processing time
     */
    public void recordPaymentProcessingTime(Duration duration, String paymentMethod) {
        recordTimer(PAYMENT_PREFIX + "processing.time", duration,
                "method", paymentMethod);
    }

    /**
     * Track refund
     */
    public void recordRefund(double amount, String reason) {
        incrementCounter(PAYMENT_PREFIX + "refunds.total",
                "reason", reason);

        recordDistributionSummary(PAYMENT_PREFIX + "refund.amount", amount,
                "reason", reason);
    }

    // ========== API Metrics ==========

    /**
     * Track API call
     */
    public void recordApiCall(String endpoint, String method, int statusCode) {
        incrementCounter(API_PREFIX + "calls.total",
                "endpoint", endpoint,
                "method", method,
                "status", String.valueOf(statusCode));
    }

    /**
     * Track API response time
     */
    public void recordApiResponseTime(String endpoint, Duration duration) {
        recordTimer(API_PREFIX + "response.time", duration,
                "endpoint", endpoint);
    }

    /**
     * Track API error
     */
    public void recordApiError(String endpoint, String errorType) {
        incrementCounter(API_PREFIX + "errors.total",
                "endpoint", endpoint,
                "errorType", errorType);
    }

    /**
     * Track rate limit hits
     */
    public void recordRateLimitHit(String endpoint, String userId) {
        incrementCounter(API_PREFIX + "ratelimit.hits.total",
                "endpoint", endpoint);
    }

    // ========== Product Metrics ==========

    /**
     * Track product view
     */
    public void recordProductView(String productId, String category) {
        incrementCounter(PRODUCT_PREFIX + "views.total",
                "category", category);
    }

    /**
     * Track product search
     */
    public void recordProductSearch(String searchTerm, int resultCount) {
        incrementCounter(PRODUCT_PREFIX + "searches.total");

        recordDistributionSummary(PRODUCT_PREFIX + "search.results", resultCount);
    }

    /**
     * Track inventory update
     */
    public void recordInventoryUpdate(String productId, int quantityChange) {
        incrementCounter(PRODUCT_PREFIX + "inventory.updates.total");
    }

    /**
     * Update low stock products count
     */
    public void updateLowStockProducts(long count) {
        updateGauge(PRODUCT_PREFIX + "lowstock.count", count);
    }

    // ========== Custom Metrics ==========

    /**
     * Record custom business event
     */
    public void recordCustomEvent(String eventName, String... tags) {
        incrementCounter(METRIC_PREFIX + "custom." + eventName, tags);
    }

    /**
     * Record custom metric value
     */
    public void recordCustomValue(String metricName, double value, String... tags) {
        recordDistributionSummary(METRIC_PREFIX + "custom." + metricName, value, tags);
    }

    /**
     * Record custom timer
     */
    public void recordCustomTimer(String metricName, Duration duration, String... tags) {
        recordTimer(METRIC_PREFIX + "custom." + metricName, duration, tags);
    }

    // ========== Helper Methods ==========

    /**
     * Increment counter with tags
     */
    private void incrementCounter(String name, String... tags) {
        String cacheKey = buildCacheKey(name, tags);

        Counter counter = counterCache.computeIfAbsent(cacheKey, k ->
                Counter.builder(name)
                        .tags(tags)
                        .description("Business metric: " + name)
                        .register(meterRegistry)
        );

        counter.increment();
    }

    /**
     * Record timer with tags
     */
    private void recordTimer(String name, Duration duration, String... tags) {
        String cacheKey = buildCacheKey(name, tags);

        Timer timer = timerCache.computeIfAbsent(cacheKey, k ->
                Timer.builder(name)
                        .tags(tags)
                        .description("Business timer: " + name)
                        .publishPercentiles(0.5, 0.95, 0.99)
                        .publishPercentileHistogram()
                        .register(meterRegistry)
        );

        timer.record(duration);
    }

    /**
     * Record distribution summary
     */
    private void recordDistributionSummary(String name, double value, String... tags) {
        DistributionSummary summary = DistributionSummary.builder(name)
                .tags(tags)
                .description("Business distribution: " + name)
                .publishPercentiles(0.5, 0.95, 0.99)
                .publishPercentileHistogram()
                .register(meterRegistry);

        summary.record(value);
    }

    /**
     * Update gauge value
     */
    private void updateGauge(String name, long value) {
        AtomicLong gaugeValue = gaugeCache.computeIfAbsent(name, k -> {
            AtomicLong atomic = new AtomicLong(value);
            Gauge.builder(name, atomic, AtomicLong::get)
                    .description("Business gauge: " + name)
                    .register(meterRegistry);
            return atomic;
        });

        gaugeValue.set(value);
    }

    /**
     * Build cache key from name and tags
     */
    private String buildCacheKey(String name, String... tags) {
        StringBuilder key = new StringBuilder(name);
        for (String tag : tags) {
            key.append(":").append(tag);
        }
        return key.toString();
    }

    /**
     * Get metric summary
     */
    public MetricsSummary getMetricsSummary() {
        MetricsSummary summary = new MetricsSummary();

        // Collect counter values
        counterCache.forEach((key, counter) ->
                summary.addCounter(key, counter.count())
        );

        // Collect gauge values
        gaugeCache.forEach((key, gauge) ->
                summary.addGauge(key, gauge.get())
        );

        return summary;
    }

    /**
     * Metrics summary DTO
     */
    @lombok.Data
    public static class MetricsSummary {
        private final Map<String, Double> counters = new ConcurrentHashMap<>();
        private final Map<String, Long> gauges = new ConcurrentHashMap<>();

        public void addCounter(String name, double value) {
            counters.put(name, value);
        }

        public void addGauge(String name, long value) {
            gauges.put(name, value);
        }
    }
}