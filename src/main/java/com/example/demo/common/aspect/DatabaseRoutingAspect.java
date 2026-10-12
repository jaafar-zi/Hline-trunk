package com.example.demo.common.aspect;

import com.example.demo.common.annotation.ReadOnly;
import com.example.demo.config.database.DatabaseContextHolder;
import com.example.demo.config.database.DatabaseType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;

/**
 * Database routing aspect for read/write splitting
 *
 * Features:
 * - Automatic routing to read replicas for read-only operations
 * - Force routing to master for write operations
 * - Support for @ReadOnly annotation
 * - Support for @Transactional(readOnly=true)
 * - Transaction-aware routing
 * - Fallback to master on replica failure
 *
 * Execution order:
 * 1. This aspect (Order=0)
 * 2. @Transactional aspect (Order=Ordered.LOWEST_PRECEDENCE)
 *
 * @author Architecture Team
 * @version 1.0
 */
@Aspect
@Component
@Order(0) // Execute before @Transactional aspect
@Slf4j
public class DatabaseRoutingAspect {

    /**
     * Route database operations based on method annotations and naming
     * Targets: Service and Repository layers only
     */
    @Around("(" +
        "execution(* com.example.demo..application.command..*(..)) || " +
        "execution(* com.example.demo..application.query..*(..))  || " +
        "execution(* com.example.demo..application.service..*(..)) || " +
        "execution(* com.example.demo..domain.repository..*(..))" +
        ") && !@annotation(com.example.demo.common.annotation.IgnoreRouting)")
    public Object routeDataSource(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Class<?> declaringClass = method.getDeclaringClass();
        String methodName = method.getName();

        if (DatabaseContextHolder.isSet()) {
            log.trace("Database routing already set, skipping for: {}.{}",
                    declaringClass.getSimpleName(), methodName);
            return joinPoint.proceed();
        }

        boolean useReadReplica = shouldUseReadReplica(method, methodName);
        DatabaseType routeTo = useReadReplica ? DatabaseType.SLAVE : DatabaseType.MASTER;

        log.debug("Routing to {} for {}.{}",
                routeTo, declaringClass.getSimpleName(), methodName);

        DatabaseContextHolder.set(routeTo);

        try {
            return joinPoint.proceed();
        } catch (Exception e) {
            if (useReadReplica && shouldFallbackToMaster(e)) {
                log.warn("Read replica failed, falling back to master: {}", e.getMessage());
                DatabaseContextHolder.set(DatabaseType.MASTER);
                return joinPoint.proceed();
            }
            throw e;
        } finally {
            DatabaseContextHolder.clear();
        }
    }

    /**
     * Determine if method should use read replica
     * Priority:
     * 1. @ReadOnly annotation
     * 2. @Transactional(readOnly=true)
     * 3. Method naming convention
     */
    private boolean shouldUseReadReplica(Method method, String methodName) {
        // 1. Check @ReadOnly annotation on method
        ReadOnly readOnly = method.getAnnotation(ReadOnly.class);
        if (readOnly != null) {
            return readOnly.useReadReplica();
        }

        // 2. Check @ReadOnly annotation on class
        readOnly = method.getDeclaringClass().getAnnotation(ReadOnly.class);
        if (readOnly != null) {
            return readOnly.useReadReplica();
        }

        // 3. Check @Transactional(readOnly=true) on method
        Transactional transactional = method.getAnnotation(Transactional.class);
        if (transactional != null && transactional.readOnly()) {
            return true;
        }

        // 4. Check @Transactional(readOnly=true) on class
        transactional = method.getDeclaringClass().getAnnotation(Transactional.class);
        if (transactional != null && transactional.readOnly()) {
            return true;
        }

        // 5. Check method naming convention
        return isReadOperationByNaming(methodName);
    }

    /**
     * Check if method is a read operation based on naming convention
     */
    private boolean isReadOperationByNaming(String methodName) {
        String lowerMethodName = methodName.toLowerCase();

        // Explicit write operation prefixes - always go to MASTER
        String[] writePrefixes = {
                "save", "insert", "update", "delete", "remove",
                "create", "modify", "set", "add", "put", "write",
                "persist", "merge", "flush", "clear", "execute"
        };

        for (String prefix : writePrefixes) {
            if (lowerMethodName.startsWith(prefix)) {
                return false;
            }
        }

        // Read operation prefixes - can use SLAVE
        String[] readPrefixes = {
                "get", "find", "search", "query", "list", "count",
                "exists", "is", "has", "check", "read", "fetch",
                "load", "select", "retrieve"
        };

        for (String prefix : readPrefixes) {
            if (lowerMethodName.startsWith(prefix)) {
                return true;
            }
        }

        // Default to master for unknown operations (safer)
        log.debug("Unknown operation type for method: {}, defaulting to MASTER", methodName);
        return false;
    }

    /**
     * Check if should fallback to master on error
     */
    private boolean shouldFallbackToMaster(Exception e) {
        String message = e.getMessage();
        if (message == null) {
            return false;
        }

        String lowerMessage = message.toLowerCase();

        // Database connection/availability errors
        boolean isConnectionError = lowerMessage.contains("connection") ||
                lowerMessage.contains("timeout") ||
                lowerMessage.contains("unreachable") ||
                lowerMessage.contains("refused") ||
                lowerMessage.contains("closed") ||
                lowerMessage.contains("network") ||
                lowerMessage.contains("i/o error");

        if (isConnectionError) {
            log.warn("Database connection error detected, will fallback to master: {}", message);
        }

        return isConnectionError;
    }
}