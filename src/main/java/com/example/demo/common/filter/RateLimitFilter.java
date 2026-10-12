package com.example.demo.common.filter;

import com.example.demo.common.constant.SecurityConstants;
import com.example.demo.common.util.JsonUtils;
import com.example.demo.common.util.SecurityUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;


import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Filter for rate limiting HTTP requests
 *
 * Features:
 * - IP-based rate limiting
 * - User-based rate limiting
 * - API key-based rate limiting
 * - Path-specific rate limits
 * - Sliding window algorithm
 * - Configurable limits per tier
 * - Rate limit headers (X-RateLimit-*)
 * - Retry-After header
 *
 * @author Architecture Team
 * @version 1.0
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 3)
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String RATE_LIMIT_PREFIX = "ratelimit:";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Default rate limits (requests per window)
    private static final int DEFAULT_LIMIT = 100;
    private static final int ANONYMOUS_LIMIT = 20;
    private static final int AUTHENTICATED_LIMIT = 100;
    private static final int PREMIUM_LIMIT = 1000;
    private static final long WINDOW_SIZE_SECONDS = 60; // 1 minute window

    // Paths to exclude from rate limiting
    private static final Set<String> EXCLUDED_PATHS = new HashSet<>(Arrays.asList(
            "/actuator/health",
            "/actuator/info",
            "/swagger-ui",
            "/v3/api-docs"
    ));

    // Path-specific limits
    private static final Map<String, Integer> PATH_LIMITS = new HashMap<>();

    static {
        PATH_LIMITS.put("/api/public/search", 10);
        PATH_LIMITS.put("/api/public/contact", 5);
        PATH_LIMITS.put("/api/auth/login", 5);
        PATH_LIMITS.put("/api/auth/register", 3);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        if (shouldNotRateLimit(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String rateLimitKey = buildRateLimitKey(request);
        int limit = determineRateLimit(request);

        RateLimitResult result = checkRateLimit(rateLimitKey, limit);

        // Add rate limit headers
        addRateLimitHeaders(response, result, limit);

        if (result.isAllowed()) {
            log.debug("Rate limit check passed for key: {} ({}/{})",
                    rateLimitKey, result.getCurrentCount(), limit);
            filterChain.doFilter(request, response);
        } else {
            handleRateLimitExceeded(request, response, result);
        }
    }

    /**
     * Check if request should be excluded from rate limiting
     */
    private boolean shouldNotRateLimit(HttpServletRequest request) {
        String path = request.getRequestURI();
        return EXCLUDED_PATHS.stream().anyMatch(path::startsWith);
    }

    /**
     * Build rate limit key based on request context
     */
    private String buildRateLimitKey(HttpServletRequest request) {
        StringBuilder keyBuilder = new StringBuilder(RATE_LIMIT_PREFIX);

        String path = request.getRequestURI();
        keyBuilder.append("path:").append(path.replaceAll("/", ":")).append(":");

        // Try to get user-based key first
        Optional<String> username = SecurityUtils.getCurrentUsername();
        if (username.isPresent() && !username.get().equals("anonymousUser")) {
            keyBuilder.append("user:").append(username.get());
            return keyBuilder.toString();
        }

        // Try API key
        String apiKey = request.getHeader("X-API-Key");
        if (apiKey != null && !apiKey.isEmpty()) {
            keyBuilder.append("apikey:").append(apiKey);
            return keyBuilder.toString();
        }

        // Fall back to IP-based limiting
        String clientIp = SecurityUtils.getClientIp(request);
        keyBuilder.append("ip:").append(clientIp);

        return keyBuilder.toString();
    }

    /**
     * Determine rate limit based on user tier and path
     */
    private int determineRateLimit(HttpServletRequest request) {
        String path = request.getRequestURI();

        // Check path-specific limits first
        for (Map.Entry<String, Integer> entry : PATH_LIMITS.entrySet()) {
            if (path.startsWith(entry.getKey())) {
                return entry.getValue();
            }
        }

        // Check user tier
        if (SecurityUtils.isAuthenticated()) {
            if (isPremiumUser()) {
                return PREMIUM_LIMIT;
            }
            return AUTHENTICATED_LIMIT;
        }

        return ANONYMOUS_LIMIT;
    }

    /**
     * Check if current user is premium
     */
    private boolean isPremiumUser() {
        return SecurityUtils.hasRole("PREMIUM") || SecurityUtils.hasRole("ENTERPRISE");
    }

    /**
     * Check rate limit using sliding window algorithm
     */
    private RateLimitResult checkRateLimit(String key, int limit) {
        try {
            // Get current timestamp
            long now = System.currentTimeMillis();
            long windowStart = now - (WINDOW_SIZE_SECONDS * 1000);

            // Use Redis sorted set for sliding window
            String sortedSetKey = key + ":requests";

            // Remove old entries outside the window
            redisTemplate.opsForZSet().removeRangeByScore(sortedSetKey, 0, windowStart);

            // Get current count
            Long currentCount = redisTemplate.opsForZSet().zCard(sortedSetKey);

            if (currentCount == null) {
                currentCount = 0L;
            }

            boolean allowed = currentCount < limit;

            if (allowed) {
                // Add new request to sorted set
                redisTemplate.opsForZSet().add(sortedSetKey, UUID.randomUUID().toString(), now);

                // Set expiration
                redisTemplate.expire(sortedSetKey, WINDOW_SIZE_SECONDS + 10, TimeUnit.SECONDS);

                currentCount++;
            }

            // Calculate retry after
            Long oldestTimestamp = null;
            Set<Object> oldest = Collections.singleton(redisTemplate.opsForZSet().range(sortedSetKey, 0, 0));
            if (oldest != null && !oldest.isEmpty()) {
                Double score = redisTemplate.opsForZSet().score(sortedSetKey, oldest.iterator().next());
                if (score != null) {
                    oldestTimestamp = score.longValue();
                }
            }

            long retryAfter = 0;
            if (!allowed && oldestTimestamp != null) {
                retryAfter = ((oldestTimestamp + (WINDOW_SIZE_SECONDS * 1000)) - now) / 1000;
                retryAfter = Math.max(retryAfter, 0);
            }

            return new RateLimitResult(allowed, currentCount.intValue(), limit, retryAfter);

        } catch (Exception e) {
            log.error("Rate limit check failed for key: {}", key, e);
            // Fail open - allow request if Redis is down
            return new RateLimitResult(true, 0, limit, 0);
        }
    }

    /**
     * Add rate limit headers to response
     */
    private void addRateLimitHeaders(HttpServletResponse response, RateLimitResult result, int limit) {
        response.setHeader("X-RateLimit-Limit", String.valueOf(limit));
        response.setHeader("X-RateLimit-Remaining",
                String.valueOf(Math.max(0, limit - result.getCurrentCount())));
        response.setHeader("X-RateLimit-Reset",
                String.valueOf(System.currentTimeMillis() / 1000 + WINDOW_SIZE_SECONDS));

        if (!result.isAllowed()) {
            response.setHeader("Retry-After", String.valueOf(result.getRetryAfter()));
        }
    }

    /**
     * Handle rate limit exceeded
     */
    private void handleRateLimitExceeded(HttpServletRequest request,
                                         HttpServletResponse response,
                                         RateLimitResult result) throws IOException {

        log.warn("Rate limit exceeded for request: {} {} from IP: {}",
                request.getMethod(),
                request.getRequestURI(),
                SecurityUtils.getClientIp(request));

        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        Map<String, Object> errorResponse = new LinkedHashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now().format(FORMATTER));
        errorResponse.put("status", HttpStatus.TOO_MANY_REQUESTS.value());
        errorResponse.put("error", "Too Many Requests");
        errorResponse.put("message", "Rate limit exceeded. Please try again later.");
        errorResponse.put("path", request.getRequestURI());
        errorResponse.put("retryAfter", result.getRetryAfter());
        errorResponse.put("limit", result.getLimit());
        errorResponse.put("remaining", 0);

        response.getWriter().write(JsonUtils.toJson(errorResponse));
        response.getWriter().flush();
    }

    /**
     * Rate limit result
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    private static class RateLimitResult {
        private boolean allowed;
        private int currentCount;
        private int limit;
        private long retryAfter; // in seconds
    }

    @Override
    protected boolean shouldNotFilterAsyncDispatch() {
        return false;
    }
}