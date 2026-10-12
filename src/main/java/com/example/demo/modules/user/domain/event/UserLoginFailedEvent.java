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
 * Event published when user login fails
 *
 * Critical for security monitoring and threat detection
 *
 * @author Architecture Team
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserLoginFailedEvent implements DomainEvent {

    private static final long serialVersionUID = 1L;

    @Builder.Default
    private String eventId = UUID.randomUUID().toString();

    private UUID userId;
    private String email;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    @Builder.Default
    private LocalDateTime occurredAt = LocalDateTime.now();

    private int failedAttempts;
    private String reason;
    private String ipAddress;
    private String userAgent;
    private String correlationId;
    private String source;
    private EventMetadata metadata;

    /**
     * Was account locked as a result?
     */
    private boolean accountLocked;

    /**
     * Is this a suspicious login attempt?
     */
    private boolean suspicious;

    public UserLoginFailedEvent(UUID userId, String email, int failedAttempts, LocalDateTime attemptedAt) {
        this.eventId = UUID.randomUUID().toString();
        this.userId = userId;
        this.email = email;
        this.failedAttempts = failedAttempts;
        this.occurredAt = attemptedAt != null ? attemptedAt : LocalDateTime.now();
        this.source = "demo-app";
        this.reason = "invalid_credentials";
    }

    @Override
    public String getEventType() {
        return "user.login_failed";
    }

    @Override
    public String getTriggeredBy() {
        return email != null ? email : "anonymous";
    }

    @Override
    public String getSource() {
        return source != null ? source : "demo-app";
    }

    @Override
    public boolean isAsync() {
        return true; // Async for security monitoring
    }

    @Override
    public String toString() {
        return String.format("UserLoginFailedEvent{userId=%s, email=%s, attempts=%d, locked=%s}",
                userId, email, failedAttempts, accountLocked);
    }
}