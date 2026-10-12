package com.example.demo.common.filter;

import com.example.demo.common.constant.SecurityConstants;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;


import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * CORS (Cross-Origin Resource Sharing) filter
 *
 * Features:
 * - Configurable allowed origins
 * - Configurable allowed methods
 * - Configurable allowed headers
 * - Credentials support
 * - Preflight request handling
 * - Origin validation
 * - Max age configuration
 *
 * @author Architecture Team
 * @version 1.0
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class CorsFilter extends OncePerRequestFilter {

    @Value("${cors.allowed-origins:*}")
    private String[] allowedOrigins;

    @Value("${cors.allowed-methods:GET,POST,PUT,DELETE,OPTIONS,PATCH}")
    private String[] allowedMethods;

    @Value("${cors.allowed-headers:*}")
    private String[] allowedHeaders;

    @Value("${cors.exposed-headers:Authorization,X-Total-Count,X-Page-Number}")
    private String[] exposedHeaders;

    @Value("${cors.allow-credentials:true}")
    private boolean allowCredentials;

    @Value("${cors.max-age:3600}")
    private long maxAge;

    // Patterns for dynamic origin matching
    private static final Set<String> ALLOWED_ORIGIN_PATTERNS = new HashSet<>(Arrays.asList(
            "https://*.yourdomain.com",
            "https://yourdomain.com",
            "http://localhost:*"
    ));

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String origin = request.getHeader("Origin");

        // Set CORS headers
        if (origin != null && isOriginAllowed(origin)) {
            setCorsHeaders(response, origin);
        }

        // Handle preflight request
        if (isPreflight(request)) {
            handlePreflightRequest(response);
            return;
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Check if origin is allowed
     */
    private boolean isOriginAllowed(String origin) {
        if (origin == null || origin.isEmpty()) {
            return false;
        }

        // Check if wildcard is allowed
        if (Arrays.asList(allowedOrigins).contains("*")) {
            log.debug("Origin allowed (wildcard): {}", origin);
            return true;
        }

        // Check exact match
        if (Arrays.asList(allowedOrigins).contains(origin)) {
            log.debug("Origin allowed (exact match): {}", origin);
            return true;
        }

        // Check pattern match
        if (matchesPattern(origin)) {
            log.debug("Origin allowed (pattern match): {}", origin);
            return true;
        }

        log.warn("Origin not allowed: {}", origin);
        return false;
    }

    /**
     * Check if origin matches allowed patterns
     */
    private boolean matchesPattern(String origin) {
        for (String pattern : ALLOWED_ORIGIN_PATTERNS) {
            if (matchPattern(origin, pattern)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Simple pattern matching for origins
     */
    private boolean matchPattern(String origin, String pattern) {
        if (pattern.contains("*")) {
            String regex = pattern.replace(".", "\\.")
                    .replace("*", ".*");
            return origin.matches(regex);
        }
        return origin.equals(pattern);
    }

    /**
     * Set CORS headers on response
     */
    private void setCorsHeaders(HttpServletResponse response, String origin) {
        // Allow origin
        response.setHeader("Access-Control-Allow-Origin", origin);

        // Allow credentials
        if (allowCredentials) {
            response.setHeader("Access-Control-Allow-Credentials", "true");
        }

        // Allow methods
        response.setHeader("Access-Control-Allow-Methods",
                String.join(", ", allowedMethods));

        // Allow headers
        response.setHeader("Access-Control-Allow-Headers",
                String.join(", ", allowedHeaders));

        // Expose headers
        response.setHeader("Access-Control-Expose-Headers",
                String.join(", ", exposedHeaders));

        // Max age
        response.setHeader("Access-Control-Max-Age", String.valueOf(maxAge));

        // Vary header for caching
        response.addHeader("Vary", "Origin");
    }

    /**
     * Check if request is a preflight request
     */
    private boolean isPreflight(HttpServletRequest request) {
        return HttpMethod.OPTIONS.matches(request.getMethod()) &&
                request.getHeader("Origin") != null &&
                request.getHeader("Access-Control-Request-Method") != null;
    }

    /**
     * Handle preflight request
     */
    private void handlePreflightRequest(HttpServletResponse response) {
        response.setStatus(HttpStatus.OK.value());
        log.debug("Handled preflight request successfully");
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