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
 * Event published when user account is locked
 *
 * @author Architecture Team
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserLockedEvent implements DomainEvent {

    private static final long serialVersionUID = 1L;

    @Builder.Default
    private String eventId = UUID.randomUUID().toString();

    private UUID userId;
    private String email;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    @Builder.Default
    private LocalDateTime occurredAt = LocalDateTime.now();

    private String reason;
    private String lockedBy;
    private String correlationId;
    private String source;
    private EventMetadata metadata;

    /**
     * Is this a temporary lock (auto-unlock after duration)?
     */
    private boolean temporary;

    /**
     * Lock duration in minutes (for temporary locks)
     */
    private Integer durationMinutes;

    /**
     * When lock will expire (for temporary locks)
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime lockedUntil;

    public UserLockedEvent(UUID userId, String email, String reason, String lockedBy) {
        this.eventId = UUID.randomUUID().toString();
        this.userId = userId;
        this.email = email;
        this.reason = reason;
        this.lockedBy = lockedBy;
        this.occurredAt = LocalDateTime.now();
        this.source = "demo-app";
        this.temporary = false;
    }

    @Override
    public String getEventType() {
        return temporary ? "user.temporarily_locked" : "user.locked";
    }

    @Override
    public String getTriggeredBy() {
        return lockedBy != null ? lockedBy : "system";
    }

    @Override
    public String getSource() {
        return source != null ? source : "demo-app";
    }

    @Override
    public boolean isAsync() {
        return true;
    }

    @Override
    public String toString() {
        return String.format("UserLockedEvent{userId=%s, email=%s, reason=%s, temporary=%s}",
                userId, email, reason, temporary);
    }
}