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
 * Event published when user account is deactivated
 *
 * Deactivation is reversible (vs deletion) and triggers:
 * - Session termination
 * - Access revocation
 * - Notification to user
 * - Audit logging
 *
 * @author Architecture Team
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDesactivatedEvent implements DomainEvent {

    private static final long serialVersionUID = 1L;

    @Builder.Default
    private String eventId = UUID.randomUUID().toString();

    private UUID userId;
    private String email;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    @Builder.Default
    private LocalDateTime occurredAt = LocalDateTime.now();

    private String deactivatedBy;
    private String reason;
    private String correlationId;
    private String source;
    private EventMetadata metadata;

    /**
     * Was this a self-service deactivation or admin action?
     */
    private boolean selfService;

    /**
     * Is this a temporary deactivation?
     */
    private boolean temporary;

    /**
     * When account will be reactivated (for temporary deactivations)
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime reactivateAt;

    /**
     * Should user data be retained?
     */
    private boolean retainData;

    public UserDesactivatedEvent(UUID userId, String email, String deactivatedBy, String reason) {
        this.eventId = UUID.randomUUID().toString();
        this.userId = userId;
        this.email = email;
        this.deactivatedBy = deactivatedBy;
        this.reason = reason;
        this.occurredAt = LocalDateTime.now();
        this.source = "demo-app";
        this.retainData = true;
        this.selfService = false;
    }

    @Override
    public String getEventType() {
        return "user.deactivated";
    }

    @Override
    public String getTriggeredBy() {
        return deactivatedBy != null ? deactivatedBy : "system";
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
        return String.format("UserDeactivatedEvent{userId=%s, email=%s, reason=%s, selfService=%s}",
                userId, email, reason, selfService);
    }
}