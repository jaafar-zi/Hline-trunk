package com.example.demo.common.aspect;

import com.example.demo.common.annotation.ReadOnly;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * Aspect for handling @ReadOnly annotation
 *
 * @author Architecture Team
 * @version 1.0
 */
@Aspect
@Component
@Slf4j
public class ReadOnlyAspect {

    @Around("@annotation(com.example.demo.common.annotation.ReadOnly) || " +
            "@within(com.example.demo.common.annotation.ReadOnly)")
    public Object enforceReadOnly(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        ReadOnly readOnly = method.getAnnotation(ReadOnly.class);
        if (readOnly == null) {
            readOnly = joinPoint.getTarget().getClass().getAnnotation(ReadOnly.class);
        }

        if (readOnly.logAccess()) {
            log.debug("Read-only access: {}.{}",
                    signature.getDeclaringTypeName(),
                    signature.getName());
        }

        // Set read-only context
        ReadOnlyContext.setReadOnly(true);
        ReadOnlyContext.setStrict(readOnly.strict());
        ReadOnlyContext.setUseReadReplica(readOnly.useReadReplica());

        try {
            // if (readOnly.timeout() > 0) {
            //     return executeWithTimeout(joinPoint, readOnly.timeout());
            // } else {
            //     return joinPoint.proceed();
            // }
            return joinPoint.proceed();
        } finally {
            ReadOnlyContext.clear();
        }
    }

    private Object executeWithTimeout(ProceedingJoinPoint joinPoint, int timeoutSeconds) throws Throwable {
        // Implementation with timeout
        return joinPoint.proceed();
    }

    /**
     * Thread-local context for read-only operations
     */
    public static class ReadOnlyContext {
        private static final ThreadLocal<Boolean> readOnly = new ThreadLocal<>();
        private static final ThreadLocal<Boolean> strict = new ThreadLocal<>();
        private static final ThreadLocal<Boolean> useReadReplica = new ThreadLocal<>();

        public static void setReadOnly(boolean value) {
            readOnly.set(value);
        }

        public static boolean isReadOnly() {
            return Boolean.TRUE.equals(readOnly.get());
        }

        public static void setStrict(boolean value) {
            strict.set(value);
        }

        public static boolean isStrict() {
            return Boolean.TRUE.equals(strict.get());
        }

        public static void setUseReadReplica(boolean value) {
            useReadReplica.set(value);
        }

        public static boolean shouldUseReadReplica() {
            return Boolean.TRUE.equals(useReadReplica.get());
        }

        public static void clear() {
            readOnly.remove();
            strict.remove();
            useReadReplica.remove();
        }
    }
}