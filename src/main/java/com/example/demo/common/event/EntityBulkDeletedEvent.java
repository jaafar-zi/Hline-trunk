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

/**
 * Event fired when multiple entities are deleted in bulk
 *
 * @author Architecture Team
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EntityBulkDeletedEvent implements DomainEvent {

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
     * IDs of deleted entities
     */
    @Builder.Default
    private List<Object> entityIds = new ArrayList<>();

    /**
     * Count of deleted entities
     */
    private int count;

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
     * User who deleted the entities
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
     * Additional metadata
     */
    private EventMetadata metadata;

    /**
     * Constructor with entity class and IDs
     */
    public EntityBulkDeletedEvent(Class<?> entityClass, List<Object> entityIds) {
        this.eventId = UUID.randomUUID().toString();
        this.entityClass = entityClass;
        this.entityType = entityClass.getSimpleName();
        this.entityIds = entityIds != null ? entityIds : new ArrayList<>();
        this.count = this.entityIds.size();
        this.occurredAt = LocalDateTime.now();
        this.deletedBy = getCurrentUsername();
        this.source = "demo-app";
    }

    @Override
    public String getEventType() {
        String deleteType = softDelete ? "soft_deleted" : "deleted";
        return "entity.bulk_" + deleteType + "." + (entityType != null ? entityType.toLowerCase() : "unknown");
    }

    @Override
    public String getTriggeredBy() {
        return deletedBy != null ? deletedBy : getCurrentUsername();
    }

    @Override
    public String getSource() {
        return source != null ? source : "demo-app";
    }

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
        return String.format("EntityBulkDeletedEvent{entityType='%s', count=%d, deletedBy='%s', softDelete=%b, occurredAt=%s}",
                entityType, count, deletedBy, softDelete, occurredAt);
    }
}