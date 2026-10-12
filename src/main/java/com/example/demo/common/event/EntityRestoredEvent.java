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
 * Event fired when a soft-deleted entity is restored
 *
 * @author Architecture Team
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EntityRestoredEvent implements DomainEvent {

    private static final long serialVersionUID = 1L;

    /**
     * Event ID (unique)
     */
    @Builder.Default
    private String eventId = UUID.randomUUID().toString();

    /**
     * Entity class
     */
    private Class<?> entityClass;

    /**
     * Entity class name
     */
    private String entityType;

    /**
     * ID of restored entity
     */
    private Object entityId;

    /**
     * When event occurred
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    @Builder.Default
    private LocalDateTime occurredAt = LocalDateTime.now();

    /**
     * User who restored the entity
     */
    private String restoredBy;

    /**
     * User who originally deleted the entity
     */
    private String previousDeletedBy;

    /**
     * When the entity was originally deleted
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime previousDeletedAt;

    /**
     * Correlation ID for tracing
     */
    private String correlationId;

    /**
     * Source service/module
     */
    private String source;

    /**
     * Restoration reason (optional)
     */
    private String reason;

    /**
     * Additional metadata
     */
    private EventMetadata metadata;

    /**
     * Constructor with entity class and ID
     */
    public EntityRestoredEvent(Class<?> entityClass, Object entityId) {
        this.eventId = UUID.randomUUID().toString();
        this.entityClass = entityClass;
        this.entityType = entityClass.getSimpleName();
        this.entityId = entityId;
        this.occurredAt = LocalDateTime.now();
        this.restoredBy = getCurrentUsername();
        this.source = "demo-app";
    }

    /**
     * Constructor with entity class, ID, and user
     */
    public EntityRestoredEvent(Class<?> entityClass, Object entityId, String restoredBy) {
        this(entityClass, entityId);
        this.restoredBy = restoredBy;
    }

    /**
     * Constructor with entity class, ID, restoration user, and previous deletion info
     */
    public EntityRestoredEvent(Class<?> entityClass, Object entityId, String restoredBy, String previousDeletedBy) {
        this(entityClass, entityId, restoredBy);
        this.previousDeletedBy = previousDeletedBy;
    }

    /**
     * Constructor with full restoration details
     */
    public EntityRestoredEvent(Class<?> entityClass, Object entityId, String restoredBy,
                               String previousDeletedBy, LocalDateTime previousDeletedAt) {
        this(entityClass, entityId, restoredBy, previousDeletedBy);
        this.previousDeletedAt = previousDeletedAt;
    }

    @Override
    public String getEventType() {
        return "entity.restored." + (entityType != null ? entityType.toLowerCase() : "unknown");
    }

    @Override
    public String getTriggeredBy() {
        return restoredBy != null ? restoredBy : getCurrentUsername();
    }

    @Override
    public String getSource() {
        return source != null ? source : "demo-app";
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
        return String.format("EntityRestoredEvent{entityType='%s', entityId=%s, restoredBy='%s', previousDeletedBy='%s', occurredAt=%s}",
                entityType, entityId, restoredBy, previousDeletedBy, occurredAt);
    }
}