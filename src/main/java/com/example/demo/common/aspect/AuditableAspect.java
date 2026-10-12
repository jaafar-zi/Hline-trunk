package com.example.demo.common.aspect;

import com.example.demo.common.annotation.Auditable;
import com.example.demo.common.util.JsonUtils;
import com.example.demo.common.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Aspect for handling @Auditable annotation
 *
 * @author Architecture Team
 * @version 1.0
 */
@Aspect
// @Component
@Slf4j
@RequiredArgsConstructor
public class AuditableAspect {

    private final AuditLogService auditLogService;

    @Around("@annotation(com.example.demo.common.annotation.Auditable) || " +
            "@within(com.example.demo.common.annotation.Auditable)")
    public Object auditMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        Auditable auditable = method.getAnnotation(Auditable.class);
        if (auditable == null) {
            auditable = joinPoint.getTarget().getClass().getAnnotation(Auditable.class);
        }

        String auditId = UUID.randomUUID().toString();
        long startTime = System.currentTimeMillis();

        AuditLog auditLog = createAuditLog(joinPoint, auditable, auditId);

        Object result = null;
        Throwable exception = null;

        try {
            result = joinPoint.proceed();
            auditLog.setStatus("SUCCESS");

            if (auditable.logReturnValue() && !auditable.onFailureOnly()) {
                auditLog.setReturnValue(maskSensitiveData(result, auditable));
            }

            return result;

        } catch (Throwable t) {
            exception = t;
            auditLog.setStatus("FAILURE");
            auditLog.setErrorMessage(t.getMessage());
            auditLog.setErrorStackTrace(getStackTrace(t));
            throw t;

        } finally {
            long executionTime = System.currentTimeMillis() - startTime;

            if (auditable.logExecutionTime()) {
                auditLog.setExecutionTime(executionTime);
            }

            auditLog.setEndTime(LocalDateTime.now());

            // Only save audit log based on conditions
            boolean shouldAudit = shouldAudit(auditable, exception);

            if (shouldAudit) {
                saveAuditLog(auditLog, auditable);
            }
        }
    }

    private AuditLog createAuditLog(ProceedingJoinPoint joinPoint, Auditable auditable, String auditId) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();

        AuditLog auditLog = new AuditLog();
        auditLog.setAuditId(auditId);
        auditLog.setAction(getAction(auditable, signature));
        auditLog.setResourceType(auditable.resourceType());
        auditLog.setDescription(auditable.description());
        auditLog.setClassName(signature.getDeclaringTypeName());
        auditLog.setMethodName(signature.getName());
        auditLog.setUsername(SecurityUtils.getCurrentUsername().orElse("anonymous"));
        auditLog.setUserId(SecurityUtils.getCurrentUserId().orElse(null));
        auditLog.setIpAddress(getCurrentIpAddress());
        auditLog.setStartTime(LocalDateTime.now());
        auditLog.setLevel(auditable.level().name());

        if (auditable.logParameters()) {
            auditLog.setParameters(getParameters(joinPoint, auditable));
        }

        return auditLog;
    }

    private String getAction(Auditable auditable, MethodSignature signature) {
        if (!auditable.action().isEmpty()) {
            return auditable.action();
        }
        return signature.getName().toUpperCase();
    }

    private Map<String, Object> getParameters(ProceedingJoinPoint joinPoint, Auditable auditable) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] parameterNames = signature.getParameterNames();
        Object[] args = joinPoint.getArgs();

        Map<String, Object> parameters = new HashMap<>();

        for (int i = 0; i < parameterNames.length; i++) {
            if (!isExcluded(parameterNames[i], auditable.excludeFields())) {
                Object value = args[i];
                if (auditable.maskSensitiveData()) {
                    value = maskSensitiveData(value, auditable);
                }
                parameters.put(parameterNames[i], value);
            }
        }

        return parameters;
    }

    private Object maskSensitiveData(Object value, Auditable auditable) {
        if (value == null) {
            return null;
        }

        // Convert to JSON and mask sensitive fields
        try {
            String json = JsonUtils.toJson(value);
            // Mask common sensitive fields
            json = json.replaceAll("(\"password\"\\s*:\\s*\")[^\"]*\"", "$1***\"");
            json = json.replaceAll("(\"token\"\\s*:\\s*\")[^\"]*\"", "$1***\"");
            json = json.replaceAll("(\"secret\"\\s*:\\s*\")[^\"]*\"", "$1***\"");
            json = json.replaceAll("(\"apiKey\"\\s*:\\s*\")[^\"]*\"", "$1***\"");
            return json;
        } catch (Exception e) {
            return value.toString();
        }
    }

    private boolean isExcluded(String fieldName, String[] excludeFields) {
        return Arrays.asList(excludeFields).contains(fieldName);
    }

    private boolean shouldAudit(Auditable auditable, Throwable exception) {
        if (auditable.onSuccessOnly() && exception != null) {
            return false;
        }
        if (auditable.onFailureOnly() && exception == null) {
            return false;
        }
        return true;
    }

    private void saveAuditLog(AuditLog auditLog, Auditable auditable) {
        try {
            auditLogService.save(auditLog);

            switch (auditable.level()) {
                case INFO:
                    log.info("Audit: {} - {} by {}",
                            auditLog.getAction(), auditLog.getStatus(), auditLog.getUsername());
                    break;
                case WARN:
                    log.warn("Audit: {} - {} by {}",
                            auditLog.getAction(), auditLog.getStatus(), auditLog.getUsername());
                    break;
                case ERROR:
                    log.error("Audit: {} - {} by {}",
                            auditLog.getAction(), auditLog.getStatus(), auditLog.getUsername());
                    break;
                case DEBUG:
                    log.debug("Audit: {} - {} by {}",
                            auditLog.getAction(), auditLog.getStatus(), auditLog.getUsername());
                    break;
            }
        } catch (Exception e) {
            log.error("Failed to save audit log", e);
        }
    }

    private String getCurrentIpAddress() {
        // Implementation to get current request IP
        return "0.0.0.0"; // Placeholder
    }

    private String getStackTrace(Throwable t) {
        if (t == null) return null;
        return Arrays.toString(t.getStackTrace());
    }

    /**
     * Audit Log entity/DTO
     */
    @lombok.Data
    public static class AuditLog {
        private String auditId;
        private String action;
        private String resourceType;
        private String description;
        private String className;
        private String methodName;
        private String username;
        private Long userId;
        private String ipAddress;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private Long executionTime;
        private String level;
        private String status;
        private Map<String, Object> parameters;
        private Object returnValue;
        private String errorMessage;
        private String errorStackTrace;
    }

    /**
     * Audit Log Service interface
     */
    public interface AuditLogService {
        void save(AuditLog auditLog);
    }
}