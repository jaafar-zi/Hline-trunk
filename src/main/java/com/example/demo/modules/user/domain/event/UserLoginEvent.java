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
 * Event published when user logs in successfully
 *
 * @author Architecture Team
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserLoginEvent implements DomainEvent {

    private static final long serialVersionUID = 1L;

    @Builder.Default
    private String eventId = UUID.randomUUID().toString();

    private UUID userId;
    private String email;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    @Builder.Default
    private LocalDateTime occurredAt = LocalDateTime.now();

    private String ipAddress;
    private String userAgent;
    private String sessionId;
    private String correlationId;
    private String source;
    private EventMetadata metadata;

    /**
     * Login method (password, oauth, mfa, etc.)
     */
    private String loginMethod;

    /**
     * Device type (mobile, desktop, tablet)
     */
    private String deviceType;

    /**
     * Location (city, country)
     */
    private String location;

    public UserLoginEvent(UUID userId, String email, LocalDateTime loginAt) {
        this.eventId = UUID.randomUUID().toString();
        this.userId = userId;
        this.email = email;
        this.occurredAt = loginAt != null ? loginAt : LocalDateTime.now();
        this.source = "demo-app";
        this.loginMethod = "password";
    }

    @Override
    public String getEventType() {
        return "user.logged_in";
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
        return true; // Async for analytics/audit
    }

    @Override
    public String toString() {
        return String.format("UserLoginEvent{userId=%s, email=%s, ip=%s, method=%s}",
                userId, email, ipAddress, loginMethod);
    }
}