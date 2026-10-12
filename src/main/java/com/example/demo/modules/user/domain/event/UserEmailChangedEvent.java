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
 * Event published when user email address is changed
 *
 * This is a critical security event that triggers:
 * - Re-verification workflow
 * - Notification to both old and new email
 * - Security audit
 * - Session review
 *
 * @author Architecture Team
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserEmailChangedEvent implements DomainEvent {

    private static final long serialVersionUID = 1L;

    @Builder.Default
    private String eventId = UUID.randomUUID().toString();

    private UUID userId;
    private String oldEmail;
    private String newEmail;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    @Builder.Default
    private LocalDateTime occurredAt = LocalDateTime.now();

    private String changedBy;
    private String correlationId;
    private String source;
    private EventMetadata metadata;

    /**
     * Was this a self-service change or admin action?
     */
    private boolean selfService;

    /**
     * Does new email require verification?
     */
    @Builder.Default
    private boolean requiresVerification = true;

    /**
     * IP address where change occurred
     */
    private String ipAddress;

    /**
     * Verification token for new email
     */
    private String verificationToken;

    /**
     * Should sessions be terminated?
     */
    private boolean terminateSessions;

    public UserEmailChangedEvent(UUID userId, String oldEmail, String newEmail, String changedBy) {
        this.eventId = UUID.randomUUID().toString();
        this.userId = userId;
        this.oldEmail = oldEmail;
        this.newEmail = newEmail;
        this.changedBy = changedBy;
        this.occurredAt = LocalDateTime.now();
        this.source = "demo-app";
        this.selfService = true;
        this.requiresVerification = true;
    }

    @Override
    public String getEventType() {
        return "user.email_changed";
    }

    @Override
    public String getTriggeredBy() {
        return changedBy != null ? changedBy : oldEmail;
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
        return String.format("UserEmailChangedEvent{userId=%s, old=%s, new=%s, selfService=%s}",
                userId, oldEmail, newEmail, selfService);
    }
}