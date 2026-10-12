package com.example.demo.common.aspect;

import com.example.demo.common.annotation.RateLimited;
import com.example.demo.common.util.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * Aspect for handling @RateLimited annotation
 *
 * @author Architecture Team
 * @version 1.0
 */
@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class RateLimitedAspect {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String RATE_LIMIT_PREFIX = "ratelimit:";

    @Around("@annotation(com.example.demo.common.annotation.RateLimited) || " +
            "@within(com.example.demo.common.annotation.RateLimited)")
    public Object checkRateLimit(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        RateLimited rateLimited = method.getAnnotation(RateLimited.class);
        if (rateLimited == null) {
            rateLimited = joinPoint.getTarget().getClass().getAnnotation(RateLimited.class);
        }

        String rateLimitKey = buildRateLimitKey(joinPoint, rateLimited);

        if (isRateLimitExceeded(rateLimitKey, rateLimited)) {
            handleRateLimitExceeded(rateLimited, rateLimitKey);
        }

        incrementRateLimit(rateLimitKey, rateLimited);

        return joinPoint.proceed();
    }

    private String buildRateLimitKey(ProceedingJoinPoint joinPoint, RateLimited rateLimited) {
        StringBuilder keyBuilder = new StringBuilder(RATE_LIMIT_PREFIX);

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        keyBuilder.append(signature.getDeclaringTypeName())
                .append(":")
                .append(signature.getName())
                .append(":");

        switch (rateLimited.scope()) {
            case USER:
                String username = SecurityUtils.getCurrentUsername().orElse("anonymous");
                keyBuilder.append("user:").append(username);
                break;

            case IP:
                String ipAddress = getClientIpAddress();
                keyBuilder.append("ip:").append(ipAddress);
                break;

            case API_KEY:
                String apiKey = getApiKey();
                keyBuilder.append("apikey:").append(apiKey);
                break;

            case GLOBAL:
                keyBuilder.append("global");
                break;

            case CUSTOM:
                if (!rateLimited.key().isEmpty()) {
                    String customKey = evaluateKey(joinPoint, rateLimited.key());
                    keyBuilder.append("custom:").append(customKey);
                } else {
                    keyBuilder.append("default");
                }
                break;
        }

        return keyBuilder.toString();
    }

    private boolean isRateLimitExceeded(String key, RateLimited rateLimited) {
        String value = redisTemplate.opsForValue().get(key);

        if (value == null) {
            return false;
        }

        try {
            int currentCount = Integer.parseInt(value);
            return currentCount >= rateLimited.limit();
        } catch (NumberFormatException e) {
            log.error("Invalid rate limit value for key: {}", key, e);
            return false;
        }
    }

    private void incrementRateLimit(String key, RateLimited rateLimited) {
        Long count = redisTemplate.opsForValue().increment(key);

        if (count != null && count == 1) {
            // Set expiration only on first increment
            long durationInSeconds = rateLimited.timeUnit().toSeconds(rateLimited.duration());
            redisTemplate.expire(key, durationInSeconds, TimeUnit.SECONDS);
        }
    }

    private void handleRateLimitExceeded(RateLimited rateLimited, String key) {
        switch (rateLimited.strategy()) {
            case REJECT:
                Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
                throw new RateLimitExceededException(
                        rateLimited.message(),
                        rateLimited.statusCode(),
                        ttl != null ? ttl : 0
                );

            case QUEUE:
                // Implementation for queueing
                log.warn("Request queued due to rate limit: {}", key);
                try {
                    Thread.sleep(1000); // Simple delay
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                break;

            case THROTTLE:
                // Implementation for throttling
                log.warn("Request throttled due to rate limit: {}", key);
                try {
                    Thread.sleep(500); // Slow down
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                break;
        }
    }

    private String getClientIpAddress() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            return SecurityUtils.getClientIp(request);
        }

        return "unknown";
    }

    private String getApiKey() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            String apiKey = request.getHeader("X-API-Key");
            return apiKey != null ? apiKey : "no-api-key";
        }

        return "no-api-key";
    }

    private String evaluateKey(ProceedingJoinPoint joinPoint, String keyExpression) {
        // Simple implementation - can be enhanced with SpEL
        return keyExpression;
    }
}

/**
 * Rate limit exceeded exception
 */
class RateLimitExceededException extends RuntimeException {
    private final int statusCode;
    private final long retryAfter;

    public RateLimitExceededException(String message, int statusCode, long retryAfter) {
        super(message);
        this.statusCode = statusCode;
        this.retryAfter = retryAfter;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public long getRetryAfter() {
        return retryAfter;
    }
}