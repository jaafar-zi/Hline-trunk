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
 * Event published when user logs out
 *
 * Triggers:
 * - Session invalidation
 * - Token revocation
 * - Security audit
 * - Analytics
 *
 * @author Architecture Team
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserLogoutEvent implements DomainEvent {

    private static final long serialVersionUID = 1L;

    @Builder.Default
    private String eventId = UUID.randomUUID().toString();

    private UUID userId;
    private String email;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    @Builder.Default
    private LocalDateTime occurredAt = LocalDateTime.now();

    private String sessionId;
    private String correlationId;
    private String source;
    private EventMetadata metadata;

    /**
     * Logout type (explicit, timeout, forced, security)
     */
    private String logoutType;

    /**
     * IP address where logout occurred
     */
    private String ipAddress;

    /**
     * Session duration in seconds
     */
    private Long sessionDurationSeconds;

    /**
     * Was this a forced logout (admin action, security event)?
     */
    private boolean forced;

    /**
     * Should all sessions be terminated?
     */
    private boolean terminateAllSessions;

    public UserLogoutEvent(UUID userId, String email, String sessionId, LocalDateTime logoutAt) {
        this.eventId = UUID.randomUUID().toString();
        this.userId = userId;
        this.email = email;
        this.sessionId = sessionId;
        this.occurredAt = logoutAt != null ? logoutAt : LocalDateTime.now();
        this.source = "demo-app";
        this.logoutType = "explicit";
        this.forced = false;
    }

    @Override
    public String getEventType() {
        return "user.logged_out";
    }

    @Override
    public String getTriggeredBy() {
        return email;
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
        return String.format("UserLogoutEvent{userId=%s, email=%s, type=%s, forced=%s}",
                userId, email, logoutType, forced);
    }
}