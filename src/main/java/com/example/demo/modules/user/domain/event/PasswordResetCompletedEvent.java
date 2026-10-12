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
 * Event published when password reset is successfully completed
 *
 * Triggers:
 * - Confirmation email
 * - Session invalidation
 * - Security audit
 * - Token cleanup
 *
 * @author Architecture Team
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetCompletedEvent implements DomainEvent {

    private static final long serialVersionUID = 1L;

    @Builder.Default
    private String eventId = UUID.randomUUID().toString();

    private UUID userId;
    private String email;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    @Builder.Default
    private LocalDateTime occurredAt = LocalDateTime.now();

    private String resetToken;
    private String correlationId;
    private String source;
    private EventMetadata metadata;

    /**
     * IP address where reset was completed
     */
    private String ipAddress;

    /**
     * Time between request and completion
     */
    private Long resetDurationMinutes;

    /**
     * Should all existing sessions be terminated?
     */
    @Builder.Default
    private boolean terminateAllSessions = true;

    public PasswordResetCompletedEvent(UUID userId, String email, String resetToken) {
        this.eventId = UUID.randomUUID().toString();
        this.userId = userId;
        this.email = email;
        this.resetToken = resetToken;
        this.occurredAt = LocalDateTime.now();
        this.source = "demo-app";
        this.terminateAllSessions = true;
    }

    @Override
    public String getEventType() {
        return "user.password_reset_completed";
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
        return String.format("PasswordResetCompletedEvent{userId=%s, email=%s}",
                userId, email);
    }
}