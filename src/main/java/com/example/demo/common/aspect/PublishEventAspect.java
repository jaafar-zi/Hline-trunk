package com.example.demo.common.aspect;

import com.example.demo.common.annotation.PublishEvent;
import com.example.demo.common.util.JsonUtils;
import com.example.demo.common.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Aspect for handling @PublishEvent annotation
 *
 * @author Architecture Team
 * @version 1.0
 */
@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class PublishEventAspect {

    private final ApplicationEventPublisher eventPublisher;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Around("@annotation(com.example.demo.common.annotation.PublishEvent)")
    public Object handleEventPublishing(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        PublishEvent publishEvent = method.getAnnotation(PublishEvent.class);

        Object result = null;
        Throwable exception = null;

        // Publish before execution if needed
        if (publishEvent.strategy() == PublishEvent.PublishStrategy.BEFORE_EXECUTION) {
            publishEventInternal(joinPoint, publishEvent, null, null);
        }

        try {
            result = joinPoint.proceed();

            // Publish after success
            if (publishEvent.strategy() == PublishEvent.PublishStrategy.AFTER_SUCCESS) {
                publishEventInternal(joinPoint, publishEvent, result, null);
            }

            return result;

        } catch (Throwable t) {
            exception = t;

            // Publish on failure
            if (publishEvent.strategy() == PublishEvent.PublishStrategy.ON_FAILURE) {
                publishEventInternal(joinPoint, publishEvent, null, t);
            }

            throw t;

        } finally {
            // Publish after completion (regardless of success/failure)
            if (publishEvent.strategy() == PublishEvent.PublishStrategy.AFTER_COMPLETION) {
                publishEventInternal(joinPoint, publishEvent, result, exception);
            }
        }
    }

    private void publishEventInternal(ProceedingJoinPoint joinPoint,
                                      PublishEvent publishEvent,
                                      Object result,
                                      Throwable exception) {

        // Check condition
        if (!evaluateCondition(publishEvent.condition(), result)) {
            log.debug("Event publishing condition not met for: {}", publishEvent.eventType());
            return;
        }

        DomainEvent event = createDomainEvent(joinPoint, publishEvent, result, exception);

        if (publishEvent.transactional() && TransactionSynchronizationManager.isSynchronizationActive()) {
            // Register for publishing after transaction commit
            TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            doPublish(event, publishEvent);
                        }
                    }
            );
        } else {
            doPublish(event, publishEvent);
        }
    }

    private void doPublish(DomainEvent event, PublishEvent publishEvent) {
        try {
            if (publishEvent.async()) {
                publishAsync(event, publishEvent);
            } else {
                publishSync(event, publishEvent);
            }

            log.info("Published event: {} to topic: {}",
                    event.getEventType(), publishEvent.topic());

        } catch (Exception e) {
            log.error("Failed to publish event: {}", event.getEventType(), e);

            if (publishEvent.retryAttempts() > 0) {
                retryPublish(event, publishEvent, publishEvent.retryAttempts());
            }
        }
    }

    @Async
    public void publishAsync(DomainEvent event, PublishEvent publishEvent) {
        publishSync(event, publishEvent);
    }

    private void publishSync(DomainEvent event, PublishEvent publishEvent) {
        if (!publishEvent.topic().isEmpty()) {
            // Publish to Kafka
            String eventJson = JsonUtils.toJson(event);
            kafkaTemplate.send(publishEvent.topic(), event.getEventId(), eventJson);
        } else {
            // Publish as Spring application event
            eventPublisher.publishEvent(event);
        }
    }

    private void retryPublish(DomainEvent event, PublishEvent publishEvent, int attemptsLeft) {
        if (attemptsLeft <= 0) {
            log.error("Max retry attempts reached for event: {}", event.getEventType());
            return;
        }

        try {
            Thread.sleep(1000); // Wait before retry
            publishSync(event, publishEvent);
        } catch (Exception e) {
            log.warn("Retry failed for event: {}, attempts left: {}",
                    event.getEventType(), attemptsLeft - 1);
            retryPublish(event, publishEvent, attemptsLeft - 1);
        }
    }

    private DomainEvent createDomainEvent(ProceedingJoinPoint joinPoint,
                                          PublishEvent publishEvent,
                                          Object result,
                                          Throwable exception) {

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();

        DomainEvent event = new DomainEvent();
        event.setEventId(UUID.randomUUID().toString());
        event.setEventType(publishEvent.eventType());
        event.setDescription(publishEvent.description());
        event.setTimestamp(LocalDateTime.now());
        event.setSource(signature.getDeclaringTypeName() + "." + signature.getName());
        event.setUserId(SecurityUtils.getCurrentUserId().orElse(null));
        event.setUsername(SecurityUtils.getCurrentUsername().orElse("system"));
        event.setPriority(publishEvent.priority());

        // Set payload
        if (!publishEvent.payloadExpression().isEmpty()) {
            Object payload = evaluatePayloadExpression(publishEvent.payloadExpression(), result);
            event.setPayload(payload);
        }

        // Include parameters if needed
        if (publishEvent.includeParameters()) {
            event.setParameters(getMethodParameters(joinPoint));
        }

        // Set status
        event.setStatus(exception == null ? "SUCCESS" : "FAILURE");

        if (exception != null) {
            event.setErrorMessage(exception.getMessage());
        }

        return event;
    }

    private boolean evaluateCondition(String condition, Object result) {
        if (condition == null || condition.isEmpty()) {
            return true;
        }

        // Simple implementation - can be enhanced with SpEL
        if (condition.equals("#result != null")) {
            return result != null;
        }

        return true;
    }

    private Object evaluatePayloadExpression(String expression, Object result) {
        if (expression.equals("#result")) {
            return result;
        }

        // Can be enhanced with SpEL evaluation
        return result;
    }

    private Map<String, Object> getMethodParameters(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] parameterNames = signature.getParameterNames();
        Object[] args = joinPoint.getArgs();

        Map<String, Object> parameters = new HashMap<>();
        for (int i = 0; i < parameterNames.length; i++) {
            parameters.put(parameterNames[i], args[i]);
        }

        return parameters;
    }

    /**
     * Domain Event DTO
     */
    @lombok.Data
    public static class DomainEvent {
        private String eventId;
        private String eventType;
        private String description;
        private LocalDateTime timestamp;
        private String source;
        private Long userId;
        private String username;
        private int priority;
        private Object payload;
        private Map<String, Object> parameters;
        private String status;
        private String errorMessage;
        private Map<String, String> metadata;
    }
}