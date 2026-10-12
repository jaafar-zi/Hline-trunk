package com.example.demo.common.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Filter for generating and propagating request IDs
 *
 * Features:
 * - Generates unique request ID for each request
 * - Propagates request ID through MDC
 * - Adds request ID to response headers
 * - Supports client-provided request IDs
 * - Correlation ID support
 * - Session ID tracking
 *
 * @author Architecture Team
 * @version 1.0
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestIdFilter extends OncePerRequestFilter {

    // Header names
    private static final String REQUEST_ID_HEADER = "X-Request-ID";
    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    private static final String SESSION_ID_HEADER = "X-Session-ID";
    private static final String TRACE_ID_HEADER = "X-Trace-ID";

    // MDC keys
    private static final String MDC_REQUEST_ID = "requestId";
    private static final String MDC_CORRELATION_ID = "correlationId";
    private static final String MDC_SESSION_ID = "sessionId";
    private static final String MDC_TRACE_ID = "traceId";
    private static final String MDC_CLIENT_IP = "clientIp";
    private static final String MDC_USER_AGENT = "userAgent";
    private static final String MDC_REQUEST_URI = "requestUri";
    private static final String MDC_REQUEST_METHOD = "requestMethod";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            // Setup request context
            setupRequestContext(request, response);

            // Process request
            filterChain.doFilter(request, response);

        } finally {
            // Clear MDC
            clearRequestContext();
        }
    }

    /**
     * Setup request context with IDs and metadata
     */
    private void setupRequestContext(HttpServletRequest request, HttpServletResponse response) {
        // Request ID - either from client or generate new
        String requestId = getOrGenerateRequestId(request);
        MDC.put(MDC_REQUEST_ID, requestId);
        response.setHeader(REQUEST_ID_HEADER, requestId);

        // Correlation ID - for tracing across multiple services
        String correlationId = getOrGenerateCorrelationId(request);
        MDC.put(MDC_CORRELATION_ID, correlationId);
        response.setHeader(CORRELATION_ID_HEADER, correlationId);

        // Session ID - for tracking user sessions
        String sessionId = getOrGenerateSessionId(request);
        if (sessionId != null) {
            MDC.put(MDC_SESSION_ID, sessionId);
            response.setHeader(SESSION_ID_HEADER, sessionId);
        }

        // Trace ID - for distributed tracing (Zipkin, Jaeger compatible)
        String traceId = getOrGenerateTraceId(request);
        MDC.put(MDC_TRACE_ID, traceId);
        response.setHeader(TRACE_ID_HEADER, traceId);

        // Additional context
        MDC.put(MDC_CLIENT_IP, getClientIp(request));
        MDC.put(MDC_USER_AGENT, request.getHeader("User-Agent"));
        MDC.put(MDC_REQUEST_URI, request.getRequestURI());
        MDC.put(MDC_REQUEST_METHOD, request.getMethod());

        log.debug("Request context initialized - RequestId: {}, CorrelationId: {}, TraceId: {}",
                requestId, correlationId, traceId);
    }

    /**
     * Get or generate request ID
     */
    private String getOrGenerateRequestId(HttpServletRequest request) {
        String requestId = request.getHeader(REQUEST_ID_HEADER);

        if (requestId == null || requestId.trim().isEmpty()) {
            requestId = generateUUID();
            log.debug("Generated new request ID: {}", requestId);
        } else {
            log.debug("Using client-provided request ID: {}", requestId);
        }

        return requestId;
    }

    /**
     * Get or generate correlation ID
     */
    private String getOrGenerateCorrelationId(HttpServletRequest request) {
        String correlationId = request.getHeader(CORRELATION_ID_HEADER);

        if (correlationId == null || correlationId.trim().isEmpty()) {
            // Use request ID as correlation ID if not provided
            correlationId = request.getHeader(REQUEST_ID_HEADER);

            if (correlationId == null || correlationId.trim().isEmpty()) {
                correlationId = generateUUID();
            }
        }

        return correlationId;
    }

    /**
     * Get or generate session ID
     */
    private String getOrGenerateSessionId(HttpServletRequest request) {
        // Try to get from header
        String sessionId = request.getHeader(SESSION_ID_HEADER);

        if (sessionId == null || sessionId.trim().isEmpty()) {
            // Try to get from HTTP session
            if (request.getSession(false) != null) {
                sessionId = request.getSession().getId();
            }
        }

        return sessionId;
    }

    /**
     * Get or generate trace ID
     */
    private String getOrGenerateTraceId(HttpServletRequest request) {
        // Check for existing trace ID (from upstream services)
        String traceId = request.getHeader(TRACE_ID_HEADER);

        // Check Zipkin/Jaeger headers
        if (traceId == null || traceId.trim().isEmpty()) {
            traceId = request.getHeader("X-B3-TraceId");
        }

        if (traceId == null || traceId.trim().isEmpty()) {
            traceId = generateTraceId();
        }

        return traceId;
    }

    /**
     * Get client IP address
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");

        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        // Handle multiple IPs
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        return ip;
    }

    /**
     * Generate UUID
     */
    private String generateUUID() {
        return UUID.randomUUID().toString();
    }

    /**
     * Generate trace ID (128-bit for compatibility with Zipkin)
     */
    private String generateTraceId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * Clear request context from MDC
     */
    private void clearRequestContext() {
        MDC.remove(MDC_REQUEST_ID);
        MDC.remove(MDC_CORRELATION_ID);
        MDC.remove(MDC_SESSION_ID);
        MDC.remove(MDC_TRACE_ID);
        MDC.remove(MDC_CLIENT_IP);
        MDC.remove(MDC_USER_AGENT);
        MDC.remove(MDC_REQUEST_URI);
        MDC.remove(MDC_REQUEST_METHOD);

        // Clear all MDC
        MDC.clear();
    }

    /**
     * Get current request ID from MDC
     */
    public static String getCurrentRequestId() {
        return MDC.get(MDC_REQUEST_ID);
    }

    /**
     * Get current correlation ID from MDC
     */
    public static String getCurrentCorrelationId() {
        return MDC.get(MDC_CORRELATION_ID);
    }

    /**
     * Get current trace ID from MDC
     */
    public static String getCurrentTraceId() {
        return MDC.get(MDC_TRACE_ID);
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