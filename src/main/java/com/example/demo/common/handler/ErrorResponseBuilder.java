package com.example.demo.common.handler;

import brave.Tracer;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Builder for creating consistent ApiErrorResponse objects
 *
 * Features:
 * - Automatic request context extraction
 * - Distributed tracing integration
 * - Environment-aware stack trace handling
 * - Request ID generation and tracking
 *
 * @author Architecture Team
 * @version 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ErrorResponseBuilder {

    private static final String REQUEST_ID_HEADER = "X-Request-ID";
    private static final int MAX_STACK_TRACE_ELEMENTS = 10;

    private final Tracer tracer;

    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    @Value("${app.error.include-stack-trace:false}")
    private boolean includeStackTrace;

    @Value("${app.error.documentation-base-url:https://docs.example.com/errors}")
    private String documentationBaseUrl;

    /**
     * Build error response with request context
     */
    public ApiErrorResponse build(
            Integer status,
            String errorCode,
            String message,
            String details,
            HttpServletRequest request) {

        ApiErrorResponse response = ApiErrorResponse.builder()
                .status(status)
                .errorCode(errorCode)
                .message(message)
                .details(details)
                .path(request != null ? request.getRequestURI() : null)
                .method(request != null ? request.getMethod() : null)
                .requestId(extractRequestId(request))
                .traceId(extractTraceId())
                .spanId(extractSpanId())
                .documentationUrl(buildDocumentationUrl(errorCode))
                .build();

        log.debug("Built error response: status={}, errorCode={}, requestId={}",
                status, errorCode, response.getRequestId());

        return response;
    }

    /**
     * Build error response from exception
     */
    public ApiErrorResponse buildFromException(
            Integer status,
            String errorCode,
            Exception ex,
            HttpServletRequest request) {

        ApiErrorResponse response = build(
                status,
                errorCode,
                ex.getMessage(),
                extractDetails(ex),
                request
        );

        // Add stack trace in development mode
        if (shouldIncludeStackTrace()) {
            response.setStackTrace(extractStackTrace(ex));
        }

        return response;
    }

    /**
     * Build validation error response
     */
    public ApiErrorResponse buildValidationError(
            String message,
            HttpServletRequest request) {

        return build(
                400,
                "VALIDATION_ERROR",
                message,
                "One or more fields have validation errors",
                request
        );
    }

    /**
     * Extract request ID from header or generate new one
     */
    private String extractRequestId(HttpServletRequest request) {
        if (request == null) {
            return generateRequestId();
        }

        String requestId = request.getHeader(REQUEST_ID_HEADER);
        if (requestId == null || requestId.isEmpty()) {
            requestId = generateRequestId();
            log.debug("Generated new request ID: {}", requestId);
        }

        return requestId;
    }

    /**
     * Generate unique request ID
     */
    private String generateRequestId() {
        return UUID.randomUUID().toString();
    }

    /**
     * Extract trace ID from distributed tracing
     */
    private String extractTraceId() {
        try {
            if (tracer != null && tracer.currentSpan() != null) {
                return tracer.currentSpan().context().traceIdString();
            }
        } catch (Exception e) {
            log.trace("Failed to extract trace ID", e);
        }
        return null;
    }

    /**
     * Extract span ID from distributed tracing
     */
    private String extractSpanId() {
        try {
            if (tracer != null && tracer.currentSpan() != null) {
                return tracer.currentSpan().context().spanIdString();
            }
        } catch (Exception e) {
            log.trace("Failed to extract span ID", e);
        }
        return null;
    }

    /**
     * Extract detailed message from exception
     */
    private String extractDetails(Exception ex) {
        if (ex.getCause() != null) {
            return ex.getCause().getMessage();
        }
        return null;
    }

    /**
     * Extract stack trace as list of strings
     */
    private List<String> extractStackTrace(Throwable ex) {
        return Arrays.stream(ex.getStackTrace())
                .limit(MAX_STACK_TRACE_ELEMENTS)
                .map(StackTraceElement::toString)
                .collect(Collectors.toList());
    }

    /**
     * Check if stack trace should be included
     */
    private boolean shouldIncludeStackTrace() {
        return includeStackTrace || isDevelopmentMode();
    }

    /**
     * Check if running in development mode
     */
    private boolean isDevelopmentMode() {
        return activeProfile != null &&
                (activeProfile.contains("dev") || activeProfile.contains("local"));
    }

    /**
     * Build documentation URL for error code
     */
    private String buildDocumentationUrl(String errorCode) {
        if (errorCode == null || errorCode.isEmpty()) {
            return null;
        }
        return documentationBaseUrl + "/" + errorCode.toLowerCase();
    }

    /**
     * Add suggestions based on error type
     */
    public ApiErrorResponse addSuggestionsForError(
            ApiErrorResponse response,
            String errorCode) {

        switch (errorCode) {
            case "VALIDATION_ERROR":
                response.addSuggestion("Check that all required fields are provided");
                response.addSuggestion("Verify field formats match expected patterns");
                break;
            case "ENTITY_NOT_FOUND":
                response.addSuggestion("Verify the ID is correct");
                response.addSuggestion("Check if the resource was deleted");
                break;
            case "UNAUTHORIZED":
                response.addSuggestion("Ensure you are logged in");
                response.addSuggestion("Check if your session has expired");
                break;
            case "FORBIDDEN":
                response.addSuggestion("Verify you have the required permissions");
                response.addSuggestion("Contact your administrator for access");
                break;
            case "DUPLICATE_ENTITY":
                response.addSuggestion("Use a different value for unique fields");
                response.addSuggestion("Check if this resource already exists");
                break;
            default:
                // No suggestions for unknown error codes
                break;
        }

        return response;
    }
}