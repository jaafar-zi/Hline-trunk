package com.example.demo.modules.user.domain.event;

import com.example.demo.common.event.DomainEvent;
import com.example.demo.common.event.EventMetadata;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Event published when user password is changed
 *
 * This is a business-critical event that requires:
 * - Security auditing
 * - User notification
 * - Session invalidation
 *
 * @author Architecture Team
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPasswordChangedEvent implements DomainEvent {

    private static final long serialVersionUID = 1L;

    @Builder.Default
    private String eventId = UUID.randomUUID().toString();

    private UUID userId;
    private String email;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    @Builder.Default
    private LocalDateTime occurredAt = LocalDateTime.now();

    private String changedBy;
    private String correlationId;
    private String source;
    private EventMetadata metadata;

    /**
     * Was this a self-service password change or admin reset?
     */
    private boolean selfService;

    /**
     * Was password expired when changed?
     */
    private boolean wasExpired;

    /**
     * IP address where change originated
     */
    private String ipAddress;

    public UserPasswordChangedEvent(UUID userId, String email, String changedBy) {
        this.eventId = UUID.randomUUID().toString();
        this.userId = userId;
        this.email = email;
        this.changedBy = changedBy;
        this.occurredAt = LocalDateTime.now();
        this.source = "demo-app";
        this.selfService = true;
    }

    @Override
    public String getEventType() {
        return "user.password_changed";
    }

    @Override
    public String getTriggeredBy() {
        return changedBy != null ? changedBy : "system";
    }

    @Override
    public String getSource() {
        return source != null ? source : "demo-app";
    }

    @Override
    public boolean isAsync() {
        return true; // Always async for notification/audit
    }

    @Override
    public String toString() {
        return String.format("UserPasswordChangedEvent{userId=%s, email=%s, changedBy=%s, selfService=%s}",
                userId, email, changedBy, selfService);
    }
}