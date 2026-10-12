package com.example.demo.common.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Event fired when an entity is updated
 *
 * Features:
 * - Carries the updated entity
 * - Tracks field-level changes (what changed, old/new values)
 * - Supports partial updates tracking
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
public class EntityUpdatedEvent<T> implements DomainEvent {

    private static final long serialVersionUID = 1L;

    /**
     * Event ID (unique)
     */
    @Builder.Default
    private String eventId = UUID.randomUUID().toString();

    /**
     * The updated entity (current state)
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
     * User who updated the entity
     */
    private String updatedBy;

    /**
     * Correlation ID for tracing
     */
    private String correlationId;

    /**
     * Source service/module
     */
    private String source;

    /**
     * Field-level changes
     *
     * Format:
     * {
     *   "fieldName": {
     *     "old": "previous value",
     *     "new": "current value"
     *   }
     * }
     *
     * Example:
     * {
     *   "displayName": {
     *     "old": "Manager Role",
     *     "new": "Senior Manager"
     *   },
     *   "priority": {
     *     "old": 5,
     *     "new": 10
     *   }
     * }
     */
    @Builder.Default
    private Map<String, FieldChange> changes = new HashMap<>();

    /**
     * Additional metadata
     */
    private EventMetadata metadata;

    /**
     * Constructor with entity only
     */
    public EntityUpdatedEvent(T entity) {
        this.eventId = UUID.randomUUID().toString();
        this.entity = entity;
        this.entityType = entity != null ? entity.getClass().getSimpleName() : "Unknown";
        this.entityId = extractEntityId(entity);
        this.occurredAt = LocalDateTime.now();
        this.updatedBy = getCurrentUsername();
        this.source = "demo-app";
        this.changes = new HashMap<>();
    }

    /**
     * Constructor with entity and changes
     */
    public EntityUpdatedEvent(T entity, Map<String, FieldChange> changes) {
        this(entity);
        this.changes = changes != null ? changes : new HashMap<>();
    }

    /**
     * Add a field change
     */
    public void addChange(String fieldName, Object oldValue, Object newValue) {
        this.changes.put(fieldName, new FieldChange(oldValue, newValue));
    }

    /**
     * Check if specific field changed
     */
    public boolean hasFieldChanged(String fieldName) {
        return changes.containsKey(fieldName);
    }

    /**
     * Get old value of a field
     */
    public Object getOldValue(String fieldName) {
        FieldChange change = changes.get(fieldName);
        return change != null ? change.getOldValue() : null;
    }

    /**
     * Get new value of a field
     */
    public Object getNewValue(String fieldName) {
        FieldChange change = changes.get(fieldName);
        return change != null ? change.getNewValue() : null;
    }

    /**
     * Check if any changes exist
     */
    public boolean hasChanges() {
        return !changes.isEmpty();
    }

    /**
     * Get number of changed fields
     */
    public int getChangeCount() {
        return changes.size();
    }

    @Override
    public String getEventType() {
        return "entity.updated." + (entityType != null ? entityType.toLowerCase() : "unknown");
    }

    @Override
    public String getTriggeredBy() {
        return updatedBy != null ? updatedBy : getCurrentUsername();
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
        return String.format("EntityUpdatedEvent{entityType='%s', entityId=%s, updatedBy='%s', changes=%d, occurredAt=%s}",
                entityType, entityId, updatedBy, changes.size(), occurredAt);
    }

    /**
     * Field change holder
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FieldChange {
        private Object oldValue;
        private Object newValue;

        @Override
        public String toString() {
            return String.format("%s → %s", oldValue, newValue);
        }
    }
}