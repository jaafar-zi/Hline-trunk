package com.example.demo.common.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Event fired when an entity is created
 *
 * @param <T> Entity type
 *
 * @author Architecture Team
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EntityCreatedEvent<T> implements DomainEvent {

    private static final long serialVersionUID = 1L;

    /**
     * Event ID (unique)
     */
    @Builder.Default
    private String eventId = UUID.randomUUID().toString();

    /**
     * The created entity
     */
    private T entity;

    /**
     * Entity class name
     */
    private String entityType;

    /**
     * Entity ID
     */
    private Object entityId;

    /**
     * When event occurred
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    @Builder.Default
    private LocalDateTime occurredAt = LocalDateTime.now();

    /**
     * User who created the entity
     */
    private String createdBy;

    /**
     * Correlation ID for tracing
     */
    private String correlationId;

    /**
     * Source service/module
     */
    private String source;

    /**
     * Additional metadata
     */
    private EventMetadata metadata;

    /**
     * Constructor with entity only
     */
    public EntityCreatedEvent(T entity) {
        this.eventId = UUID.randomUUID().toString();
        this.entity = entity;
        this.entityType = entity != null ? entity.getClass().getSimpleName() : "Unknown";
        this.entityId = extractEntityId(entity);
        this.occurredAt = LocalDateTime.now();
        this.createdBy = getCurrentUsername();
        this.source = "demo-app";
    }

    /**
     * Constructor with entity and user
     */
    public EntityCreatedEvent(T entity, String createdBy) {
        this(entity);
        this.createdBy = createdBy;
    }

    @Override
    public String getEventType() {
        return "entity.created." + (entityType != null ? entityType.toLowerCase() : "unknown");
    }

    @Override
    public String getTriggeredBy() {
        return createdBy != null ? createdBy : getCurrentUsername();
    }

    @Override
    public String getSource() {
        return source != null ? source : "demo-app";
    }

    /**
     * Extract entity ID using reflection
     */
    private Object extractEntityId(T entity) {
        if (entity == null) {
            return null;
        }

        Class<?> clazz = entity.getClass();

        while (clazz != null && clazz != Object.class) {
            try {
                java.lang.reflect.Field idField = clazz.getDeclaredField("id");
                idField.setAccessible(true);
                return idField.get(entity);
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            } catch (Exception e) {
                return null;
            }
        }

        return null;
    }

    /**
     * Get current username from security context
     */
    private String getCurrentUsername() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                return authentication.getName();
            }
        } catch (Exception e) {
            // Ignore
        }
        return "system";
    }

    @Override
    public String toString() {
        return String.format("EntityCreatedEvent{entityType='%s', entityId=%s, createdBy='%s', occurredAt=%s}",
                entityType, entityId, createdBy, occurredAt);
    }
}