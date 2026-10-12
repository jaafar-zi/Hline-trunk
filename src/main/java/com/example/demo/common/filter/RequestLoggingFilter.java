package com.example.demo.common.filter;

import com.example.demo.common.util.JsonUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Filter for logging HTTP requests and responses
 *
 * Features:
 * - Request/Response logging with body
 * - Performance tracking
 * - Sensitive data masking
 * - Configurable exclusions
 * - Request/Response size tracking
 * - Status code tracking
 * - Error logging
 *
 * @author Architecture Team
 * @version 1.0
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 2)
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    private static final int MAX_PAYLOAD_LENGTH = 10000; // 10KB max for logging

    // Paths to exclude from logging
    private static final Set<String> EXCLUDED_PATHS = new HashSet<>(Arrays.asList(
            "/actuator/health",
            "/actuator/metrics",
            "/actuator/prometheus",
            "/favicon.ico",
            "/swagger-ui",
            "/v3/api-docs"
    ));

    // Headers to exclude from logging (sensitive data)
    private static final Set<String> EXCLUDED_HEADERS = new HashSet<>(Arrays.asList(
            "authorization",
            "x-auth-token",
            "cookie",
            "x-api-key",
            "x-api-secret"
    ));

    // Content types to log body
    private static final Set<String> LOGGABLE_CONTENT_TYPES = new HashSet<>(Arrays.asList(
            "application/json",
            "application/xml",
            "text/plain",
            "text/xml",
            "application/x-www-form-urlencoded"
    ));

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        if (shouldNotLog(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Wrap request and response to cache content
        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

        long startTime = System.currentTimeMillis();
        String requestId = UUID.randomUUID().toString();

        try {
            // Log request
            logRequest(wrappedRequest, requestId);

            // Process request
            filterChain.doFilter(wrappedRequest, wrappedResponse);

        } catch (Exception e) {
            log.error("Error processing request: {}", requestId, e);
            throw e;
        } finally {
            long duration = System.currentTimeMillis() - startTime;

            // Log response
            logResponse(wrappedRequest, wrappedResponse, requestId, duration);

            // Copy response body to actual response
            wrappedResponse.copyBodyToResponse();
        }
    }

    /**
     * Check if request should be excluded from logging
     */
    private boolean shouldNotLog(HttpServletRequest request) {
        String path = request.getRequestURI();
        return EXCLUDED_PATHS.stream().anyMatch(path::startsWith);
    }

    /**
     * Log HTTP request details
     */
    private void logRequest(ContentCachingRequestWrapper request, String requestId) {
        if (!log.isInfoEnabled()) {
            return;
        }

        Map<String, Object> logData = new LinkedHashMap<>();
        logData.put("requestId", requestId);
        logData.put("timestamp", LocalDateTime.now().format(FORMATTER));
        logData.put("type", "REQUEST");
        logData.put("method", request.getMethod());
        logData.put("uri", request.getRequestURI());
        logData.put("queryString", request.getQueryString());
        logData.put("remoteAddr", getClientIpAddress(request));
        logData.put("userAgent", request.getHeader("User-Agent"));
        logData.put("contentType", request.getContentType());
        logData.put("contentLength", request.getContentLength());

        // Log headers
        Map<String, String> headers = getRequestHeaders(request);
        if (!headers.isEmpty()) {
            logData.put("headers", headers);
        }

        // Log query parameters
        Map<String, String[]> params = request.getParameterMap();
        if (!params.isEmpty()) {
            logData.put("parameters", sanitizeParameters(params));
        }

        // Log request body (if applicable)
        if (shouldLogRequestBody(request)) {
            String body = getRequestBody(request);
            if (body != null && !body.isEmpty()) {
                logData.put("body", maskSensitiveData(body));
            }
        }

        log.info(">>> Incoming Request: {}", JsonUtils.toJson(logData));
    }

    /**
     * Log HTTP response details
     */
    private void logResponse(ContentCachingRequestWrapper request,
                             ContentCachingResponseWrapper response,
                             String requestId,
                             long duration) {
        if (!log.isInfoEnabled()) {
            return;
        }

        Map<String, Object> logData = new LinkedHashMap<>();
        logData.put("requestId", requestId);
        logData.put("timestamp", LocalDateTime.now().format(FORMATTER));
        logData.put("type", "RESPONSE");
        logData.put("method", request.getMethod());
        logData.put("uri", request.getRequestURI());
        logData.put("status", response.getStatus());
        logData.put("statusText", getStatusText(response.getStatus()));
        logData.put("durationMs", duration);
        logData.put("contentType", response.getContentType());
        logData.put("contentLength", response.getContentSize());

        // Log response headers
        Map<String, String> responseHeaders = getResponseHeaders(response);
        if (!responseHeaders.isEmpty()) {
            logData.put("headers", responseHeaders);
        }

        // Log response body (if applicable)
        if (shouldLogResponseBody(response)) {
            String body = getResponseBody(response);
            if (body != null && !body.isEmpty()) {
                logData.put("body", maskSensitiveData(truncateIfNeeded(body)));
            }
        }

        // Performance warning
        if (duration > 5000) {
            log.warn("<<< Slow Response: {}", JsonUtils.toJson(logData));
        } else if (response.getStatus() >= 400) {
            log.error("<<< Error Response: {}", JsonUtils.toJson(logData));
        } else {
            log.info("<<< Outgoing Response: {}", JsonUtils.toJson(logData));
        }
    }

    /**
     * Get client IP address from request
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        // Handle multiple IPs (take first one)
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        return ip;
    }

    /**
     * Get request headers
     */
    private Map<String, String> getRequestHeaders(HttpServletRequest request) {
        Map<String, String> headers = new LinkedHashMap<>();

        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();

            if (!EXCLUDED_HEADERS.contains(headerName.toLowerCase())) {
                headers.put(headerName, request.getHeader(headerName));
            } else {
                headers.put(headerName, "***MASKED***");
            }
        }

        return headers;
    }

    /**
     * Get response headers
     */
    private Map<String, String> getResponseHeaders(HttpServletResponse response) {
        Map<String, String> headers = new LinkedHashMap<>();

        Collection<String> headerNames = response.getHeaderNames();
        for (String headerName : headerNames) {
            if (!EXCLUDED_HEADERS.contains(headerName.toLowerCase())) {
                headers.put(headerName, response.getHeader(headerName));
            }
        }

        return headers;
    }

    /**
     * Sanitize query parameters
     */
    private Map<String, Object> sanitizeParameters(Map<String, String[]> params) {
        Map<String, Object> sanitized = new LinkedHashMap<>();

        params.forEach((key, values) -> {
            if (isSensitiveParameter(key)) {
                sanitized.put(key, "***MASKED***");
            } else if (values.length == 1) {
                sanitized.put(key, values[0]);
            } else {
                sanitized.put(key, Arrays.asList(values));
            }
        });

        return sanitized;
    }

    /**
     * Check if parameter is sensitive
     */
    private boolean isSensitiveParameter(String paramName) {
        String lower = paramName.toLowerCase();
        return lower.contains("password") ||
                lower.contains("token") ||
                lower.contains("secret") ||
                lower.contains("apikey");
    }

    /**
     * Get request body
     */
    private String getRequestBody(ContentCachingRequestWrapper request) {
        byte[] buf = request.getContentAsByteArray();
        if (buf.length > 0) {
            try {
                return new String(buf, 0, Math.min(buf.length, MAX_PAYLOAD_LENGTH),
                        request.getCharacterEncoding());
            } catch (UnsupportedEncodingException e) {
                return "[Unknown Encoding]";
            }
        }
        return null;
    }

    /**
     * Get response body
     */
    private String getResponseBody(ContentCachingResponseWrapper response) {
        byte[] buf = response.getContentAsByteArray();
        if (buf.length > 0) {
            try {
                return new String(buf, 0, Math.min(buf.length, MAX_PAYLOAD_LENGTH),
                        response.getCharacterEncoding());
            } catch (UnsupportedEncodingException e) {
                return "[Unknown Encoding]";
            }
        }
        return null;
    }

    /**
     * Check if should log request body
     */
    private boolean shouldLogRequestBody(HttpServletRequest request) {
        String contentType = request.getContentType();
        return contentType != null &&
                LOGGABLE_CONTENT_TYPES.stream()
                        .anyMatch(type -> contentType.toLowerCase().contains(type));
    }

    /**
     * Check if should log response body
     */
    private boolean shouldLogResponseBody(HttpServletResponse response) {
        String contentType = response.getContentType();
        return contentType != null &&
                LOGGABLE_CONTENT_TYPES.stream()
                        .anyMatch(type -> contentType.toLowerCase().contains(type));
    }

    /**
     * Mask sensitive data in JSON
     */
    private String maskSensitiveData(String content) {
        if (content == null) {
            return null;
        }

        String masked = content;

        // Mask password fields
        masked = masked.replaceAll("(\"password\"\\s*:\\s*\")[^\"]*\"", "$1***MASKED***\"");
        masked = masked.replaceAll("(\"token\"\\s*:\\s*\")[^\"]*\"", "$1***MASKED***\"");
        masked = masked.replaceAll("(\"secret\"\\s*:\\s*\")[^\"]*\"", "$1***MASKED***\"");
        masked = masked.replaceAll("(\"apiKey\"\\s*:\\s*\")[^\"]*\"", "$1***MASKED***\"");
        masked = masked.replaceAll("(\"creditCard\"\\s*:\\s*\")[^\"]*\"", "$1***MASKED***\"");

        return masked;
    }

    /**
     * Truncate content if needed
     */
    private String truncateIfNeeded(String content) {
        if (content.length() > MAX_PAYLOAD_LENGTH) {
            return content.substring(0, MAX_PAYLOAD_LENGTH) + "... [TRUNCATED]";
        }
        return content;
    }

    /**
     * Get HTTP status text
     */
    private String getStatusText(int status) {
        if (status >= 200 && status < 300) return "SUCCESS";
        if (status >= 300 && status < 400) return "REDIRECT";
        if (status >= 400 && status < 500) return "CLIENT_ERROR";
        if (status >= 500) return "SERVER_ERROR";
        return "UNKNOWN";
    }

    @Override
    protected boolean shouldNotFilterAsyncDispatch() {
        return false;
    }

    @Override
    protected boolean shouldNotFilterErrorDispatch() {
        return false;
    }
}