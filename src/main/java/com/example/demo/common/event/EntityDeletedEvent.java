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
 * Event fired when an entity is deleted
 *
 * @author Architecture Team
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EntityDeletedEvent implements DomainEvent {

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
     * ID of deleted entity
     */
    private Object entityId;

    /**
     * Soft delete flag
     */
    @Builder.Default
    private boolean softDelete = false;

    /**
     * When event occurred
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    @Builder.Default
    private LocalDateTime occurredAt = LocalDateTime.now();

    /**
     * User who deleted the entity
     */
    private String deletedBy;

    /**
     * Correlation ID for tracing
     */
    private String correlationId;

    /**
     * Source service/module
     */
    private String source;

    /**
     * Deletion reason (optional)
     */
    private String reason;

    /**
     * Additional metadata
     */
    private EventMetadata metadata;

    /**
     * Constructor with entity class and ID
     */
    public EntityDeletedEvent(Class<?> entityClass, Object entityId) {
        this.eventId = UUID.randomUUID().toString();
        this.entityClass = entityClass;
        this.entityType = entityClass.getSimpleName();
        this.entityId = entityId;
        this.occurredAt = LocalDateTime.now();
        this.deletedBy = getCurrentUsername();
        this.source = "demo-app";
    }

    /**
     * Constructor with entity class, ID, and user
     */
    public EntityDeletedEvent(Class<?> entityClass, Object entityId, String deletedBy) {
        this(entityClass, entityId);
        this.deletedBy = deletedBy;
    }

    /**
     * Constructor with entity class, ID, user, and soft delete flag
     */
    public EntityDeletedEvent(Class<?> entityClass, Object entityId, String deletedBy, boolean softDelete) {
        this(entityClass, entityId, deletedBy);
        this.softDelete = softDelete;
    }

    @Override
    public String getEventType() {
        String deleteType = softDelete ? "soft_deleted" : "deleted";
        return "entity." + deleteType + "." + (entityType != null ? entityType.toLowerCase() : "unknown");
    }

    @Override
    public String getTriggeredBy() {
        return deletedBy != null ? deletedBy : getCurrentUsername();
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
        return String.format("EntityDeletedEvent{entityType='%s', entityId=%s, deletedBy='%s', softDelete=%b, occurredAt=%s}",
                entityType, entityId, deletedBy, softDelete, occurredAt);
    }
}