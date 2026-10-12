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
 * Event published when user account is activated (email verified)
 *
 * @author Architecture Team
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserActivatedEvent implements DomainEvent {

    private static final long serialVersionUID = 1L;

    @Builder.Default
    private String eventId = UUID.randomUUID().toString();

    private UUID userId;
    private String email;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    @Builder.Default
    private LocalDateTime occurredAt = LocalDateTime.now();

    private String activatedBy;
    private String correlationId;
    private String source;
    private EventMetadata metadata;

    /**
     * Activation method (email_verification, admin_approval, etc.)
     */
    private String activationMethod;

    public UserActivatedEvent(UUID userId, String email, LocalDateTime activatedAt) {
        this.eventId = UUID.randomUUID().toString();
        this.userId = userId;
        this.email = email;
        this.occurredAt = activatedAt != null ? activatedAt : LocalDateTime.now();
        this.source = "demo-app";
        this.activationMethod = "email_verification";
    }

    @Override
    public String getEventType() {
        return "user.activated";
    }

    @Override
    public String getTriggeredBy() {
        return activatedBy != null ? activatedBy : email;
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
        return String.format("UserActivatedEvent{userId=%s, email=%s, method=%s}",
                userId, email, activationMethod);
    }
}