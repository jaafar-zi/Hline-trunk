package com.example.demo.common.aspect;

import com.example.demo.common.util.JsonUtils;
import com.example.demo.common.util.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Centralized logging aspect for all service layer methods
 *
 * Features:
 * - Method entry/exit logging
 * - Parameter and return value logging
 * - Exception logging
 * - Execution time tracking
 * - Request correlation
 * - Structured logging
 * - MDC (Mapped Diagnostic Context) support
 *
 * @author Architecture Team
 * @version 1.0
 */
@Aspect
@Component
@Slf4j
public class LoggingAspect {

    private static final String CORRELATION_ID_KEY = "correlationId";
    private static final String USER_ID_KEY = "userId";
    private static final String USERNAME_KEY = "username";
    private static final String REQUEST_URI_KEY = "requestUri";
    private static final String REQUEST_METHOD_KEY = "requestMethod";
    private static final String IP_ADDRESS_KEY = "ipAddress";

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    // Patterns for sensitive data masking
    private static final List<String> SENSITIVE_FIELDS = Arrays.asList(
            "password", "token", "secret", "apiKey", "creditCard",
            "ssn", "cvv", "pin", "privateKey"
    );

    /**
     * Pointcut for all service layer methods
     */
    @Pointcut("execution(* com.example.demo..service..*(..))")
    public void serviceLayer() {}

    /**
     * Pointcut for all controller methods
     */
    @Pointcut("execution(* com.example.demo..controller..*(..))")
    public void controllerLayer() {}

    /**
     * Pointcut for all repository methods
     */
    @Pointcut("execution(* com.example.demo..repository..*(..))")
    public void repositoryLayer() {}

    /**
     * Around advice for service layer methods
     */
    @Around("serviceLayer()")
    public Object logServiceMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        return logMethodExecution(joinPoint, "SERVICE");
    }

    /**
     * Around advice for controller methods
     */
    @Around("controllerLayer()")
    public Object logControllerMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        setupMDC();
        try {
            return logMethodExecution(joinPoint, "CONTROLLER");
        } finally {
            clearMDC();
        }
    }

    /**
     * Around advice for repository methods
     */
    @Around("repositoryLayer()")
    public Object logRepositoryMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        return logMethodExecution(joinPoint, "REPOSITORY");
    }

    /**
     * Core method execution logging
     */
    private Object logMethodExecution(ProceedingJoinPoint joinPoint, String layer) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String className = signature.getDeclaringTypeName();
        String methodName = signature.getName();
        String fullMethodName = className + "." + methodName;

        long startTime = System.currentTimeMillis();
        String executionId = UUID.randomUUID().toString().substring(0, 8);

        // Log method entry
        logMethodEntry(joinPoint, layer, fullMethodName, executionId);

        Object result = null;
        Throwable exception = null;

        try {
            result = joinPoint.proceed();
            return result;
        } catch (Throwable t) {
            exception = t;
            throw t;
        } finally {
            long executionTime = System.currentTimeMillis() - startTime;

            if (exception != null) {
                logMethodException(joinPoint, layer, fullMethodName, executionId, executionTime, exception);
            } else {
                logMethodExit(joinPoint, layer, fullMethodName, executionId, executionTime, result);
            }
        }
    }

    /**
     * Log method entry with parameters
     */
    private void logMethodEntry(ProceedingJoinPoint joinPoint, String layer,
                                String fullMethodName, String executionId) {
        if (!log.isDebugEnabled()) {
            return;
        }

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Object[] args = joinPoint.getArgs();
        String[] parameterNames = signature.getParameterNames();

        Map<String, Object> logData = new LinkedHashMap<>();
        logData.put("executionId", executionId);
        logData.put("layer", layer);
        logData.put("method", fullMethodName);
        logData.put("timestamp", LocalDateTime.now().format(FORMATTER));
        logData.put("event", "METHOD_ENTRY");

        if (args != null && args.length > 0) {
            Map<String, Object> parameters = new LinkedHashMap<>();
            for (int i = 0; i < args.length; i++) {
                String paramName = parameterNames != null && i < parameterNames.length
                        ? parameterNames[i]
                        : "arg" + i;
                parameters.put(paramName, maskSensitiveData(paramName, args[i]));
            }
            logData.put("parameters", parameters);
        }

        log.debug(">>> {}", JsonUtils.toJson(logData));
    }

    /**
     * Log method exit with result
     */
    private void logMethodExit(ProceedingJoinPoint joinPoint, String layer,
                               String fullMethodName, String executionId,
                               long executionTime, Object result) {
        if (!log.isDebugEnabled()) {
            return;
        }

        Map<String, Object> logData = new LinkedHashMap<>();
        logData.put("executionId", executionId);
        logData.put("layer", layer);
        logData.put("method", fullMethodName);
        logData.put("timestamp", LocalDateTime.now().format(FORMATTER));
        logData.put("event", "METHOD_EXIT");
        logData.put("executionTimeMs", executionTime);
        logData.put("status", "SUCCESS");

        if (result != null && shouldLogReturnValue(result)) {
            logData.put("returnValue", maskSensitiveData("result", result));
            logData.put("returnType", result.getClass().getSimpleName());
        }

        // Performance warning
        if (executionTime > 5000) {
            log.warn("<<< SLOW METHOD: {}", JsonUtils.toJson(logData));
        } else {
            log.debug("<<< {}", JsonUtils.toJson(logData));
        }
    }

    /**
     * Log method exception
     */
    private void logMethodException(ProceedingJoinPoint joinPoint, String layer,
                                    String fullMethodName, String executionId,
                                    long executionTime, Throwable exception) {
        Map<String, Object> logData = new LinkedHashMap<>();
        logData.put("executionId", executionId);
        logData.put("layer", layer);
        logData.put("method", fullMethodName);
        logData.put("timestamp", LocalDateTime.now().format(FORMATTER));
        logData.put("event", "METHOD_EXCEPTION");
        logData.put("executionTimeMs", executionTime);
        logData.put("status", "FAILURE");
        logData.put("exceptionType", exception.getClass().getName());
        logData.put("exceptionMessage", exception.getMessage());

        // Include stack trace for errors
        if (log.isTraceEnabled()) {
            logData.put("stackTrace", getStackTraceAsString(exception));
        }

        log.error("!!! {}", JsonUtils.toJson(logData));
    }

    /**
     * After throwing advice for exception logging
     */
    @AfterThrowing(pointcut = "serviceLayer() || controllerLayer() || repositoryLayer()",
            throwing = "exception")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable exception) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String methodName = signature.getDeclaringTypeName() + "." + signature.getName();

        Map<String, Object> errorLog = new LinkedHashMap<>();
        errorLog.put("event", "EXCEPTION_THROWN");
        errorLog.put("method", methodName);
        errorLog.put("exceptionType", exception.getClass().getName());
        errorLog.put("message", exception.getMessage());
        errorLog.put("timestamp", LocalDateTime.now().format(FORMATTER));

        log.error("Exception in method: {}", JsonUtils.toJson(errorLog), exception);
    }

    /**
     * Setup MDC with request context
     */
    private void setupMDC() {
        try {
            // Correlation ID
            String correlationId = UUID.randomUUID().toString();
            MDC.put(CORRELATION_ID_KEY, correlationId);

            // User information
            SecurityUtils.getCurrentUsername().ifPresent(username -> {
                MDC.put(USERNAME_KEY, username);
            });

            SecurityUtils.getCurrentUserId().ifPresent(userId -> {
                MDC.put(USER_ID_KEY, userId.toString());
            });

            // Request information
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                MDC.put(REQUEST_URI_KEY, request.getRequestURI());
                MDC.put(REQUEST_METHOD_KEY, request.getMethod());
                MDC.put(IP_ADDRESS_KEY, SecurityUtils.getClientIp(request));
            }

        } catch (Exception e) {
            log.warn("Failed to setup MDC", e);
        }
    }

    /**
     * Clear MDC
     */
    private void clearMDC() {
        MDC.clear();
    }

    /**
     * Mask sensitive data
     */
    private Object maskSensitiveData(String fieldName, Object value) {
        if (value == null) {
            return null;
        }

        // Check if field name contains sensitive keywords
        String lowerFieldName = fieldName.toLowerCase();
        boolean isSensitive = SENSITIVE_FIELDS.stream()
                .anyMatch(lowerFieldName::contains);

        if (isSensitive) {
            return "***MASKED***";
        }

        // For complex objects, convert to JSON and mask
        if (isComplexObject(value)) {
            try {
                String json = JsonUtils.toJson(value);
                return maskSensitiveFieldsInJson(json);
            } catch (Exception e) {
                return value.getClass().getSimpleName() + "@" +
                        Integer.toHexString(value.hashCode());
            }
        }

        return value;
    }

    /**
     * Mask sensitive fields in JSON string
     */
    private String maskSensitiveFieldsInJson(String json) {
        if (json == null) {
            return null;
        }

        String maskedJson = json;
        for (String field : SENSITIVE_FIELDS) {
            maskedJson = maskedJson.replaceAll(
                    "\"" + field + "\"\\s*:\\s*\"[^\"]*\"",
                    "\"" + field + "\":\"***MASKED***\""
            );
            maskedJson = maskedJson.replaceAll(
                    "\"" + field + "\"\\s*:\\s*[^,}\\]]+",
                    "\"" + field + "\":\"***MASKED***\""
            );
        }
        return maskedJson;
    }

    /**
     * Check if object is complex (not primitive or String)
     */
    private boolean isComplexObject(Object value) {
        return !(value instanceof String ||
                value instanceof Number ||
                value instanceof Boolean ||
                value instanceof Character ||
                value.getClass().isPrimitive());
    }

    /**
     * Check if return value should be logged
     */
    private boolean shouldLogReturnValue(Object result) {
        // Don't log large collections
        if (result instanceof Collection) {
            Collection<?> collection = (Collection<?>) result;
            return collection.size() <= 10;
        }

        // Don't log byte arrays or streams
        return !(result instanceof byte[] ||
                result instanceof java.io.InputStream ||
                result instanceof java.io.OutputStream);
    }

    /**
     * Get stack trace as string
     */
    private String getStackTraceAsString(Throwable throwable) {
        return Arrays.stream(throwable.getStackTrace())
                .limit(5)
                .map(StackTraceElement::toString)
                .collect(Collectors.joining("\n\t"));
    }

    /**
     * Log HTTP request details
     */
    @Before("controllerLayer()")
    public void logHttpRequest(JoinPoint joinPoint) {
        if (!log.isInfoEnabled()) {
            return;
        }

        try {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();

                Map<String, Object> requestLog = new LinkedHashMap<>();
                requestLog.put("event", "HTTP_REQUEST");
                requestLog.put("method", request.getMethod());
                requestLog.put("uri", request.getRequestURI());
                requestLog.put("queryString", request.getQueryString());
                requestLog.put("remoteAddr", request.getRemoteAddr());
                requestLog.put("userAgent", request.getHeader("User-Agent"));
                requestLog.put("timestamp", LocalDateTime.now().format(FORMATTER));

                // Log headers (excluding sensitive ones)
                Map<String, String> headers = new LinkedHashMap<>();
                Enumeration<String> headerNames = request.getHeaderNames();
                while (headerNames.hasMoreElements()) {
                    String headerName = headerNames.nextElement();
                    if (!isSensitiveHeader(headerName)) {
                        headers.put(headerName, request.getHeader(headerName));
                    }
                }
                requestLog.put("headers", headers);

                log.info("HTTP Request: {}", JsonUtils.toJson(requestLog));
            }
        } catch (Exception e) {
            log.warn("Failed to log HTTP request", e);
        }
    }

    /**
     * Check if header is sensitive
     */
    private boolean isSensitiveHeader(String headerName) {
        String lower = headerName.toLowerCase();
        return lower.contains("authorization") ||
                lower.contains("token") ||
                lower.contains("cookie") ||
                lower.contains("api-key");
    }
}