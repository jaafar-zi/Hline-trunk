package com.example.demo.common.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Event fired when multiple entities are created in bulk
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
public class EntityBulkCreatedEvent<T> implements DomainEvent {

    private static final long serialVersionUID = 1L;

    /**
     * Event ID (unique)
     */
    @Builder.Default
    private String eventId = UUID.randomUUID().toString();

    /**
     * The created entities
     */
    @Builder.Default
    private List<T> entities = new ArrayList<>();

    /**
     * Entity class name
     */
    private String entityType;

    /**
     * Entity IDs
     */
    @Builder.Default
    private List<Object> entityIds = new ArrayList<>();

    /**
     * Count of created entities
     */
    private int count;

    /**
     * When event occurred
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    @Builder.Default
    private LocalDateTime occurredAt = LocalDateTime.now();

    /**
     * User who created the entities
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
     * Constructor with entities list
     */
    public EntityBulkCreatedEvent(List<T> entities) {
        this.eventId = UUID.randomUUID().toString();
        this.entities = entities != null ? entities : new ArrayList<>();
        this.count = this.entities.size();
        this.entityType = !this.entities.isEmpty() ?
                this.entities.get(0).getClass().getSimpleName() : "Unknown";
        this.entityIds = extractEntityIds(this.entities);
        this.occurredAt = LocalDateTime.now();
        this.createdBy = getCurrentUsername();
        this.source = "demo-app";
    }

    /**
     * Constructor with entities and user
     */
    public EntityBulkCreatedEvent(List<T> entities, String createdBy) {
        this(entities);
        this.createdBy = createdBy;
    }

    @Override
    public String getEventType() {
        return "entity.bulk_created." + (entityType != null ? entityType.toLowerCase() : "unknown");
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
     * Extract entity IDs from entities list
     */
    private List<Object> extractEntityIds(List<T> entities) {
        if (entities == null || entities.isEmpty()) {
            return new ArrayList<>();
        }

        return entities.stream()
                .map(this::extractEntityId)
                .filter(id -> id != null)
                .collect(Collectors.toList());
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
        return String.format("EntityBulkCreatedEvent{entityType='%s', count=%d, createdBy='%s', occurredAt=%s}",
                entityType, count, createdBy, occurredAt);
    }
}