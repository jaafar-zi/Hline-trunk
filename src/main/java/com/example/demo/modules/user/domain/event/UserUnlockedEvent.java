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
 * Event published when user account is unlocked
 *
 * @author Architecture Team
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserUnlockedEvent implements DomainEvent {

    private static final long serialVersionUID = 1L;

    @Builder.Default
    private String eventId = UUID.randomUUID().toString();

    private UUID userId;
    private String email;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    @Builder.Default
    private LocalDateTime occurredAt = LocalDateTime.now();

    private String unlockedBy;
    private String correlationId;
    private String source;
    private EventMetadata metadata;

    /**
     * Was unlock automatic (e.g., temporary lock expired)?
     */
    private boolean automatic;

    public UserUnlockedEvent(UUID userId, String email, String unlockedBy) {
        this.eventId = UUID.randomUUID().toString();
        this.userId = userId;
        this.email = email;
        this.unlockedBy = unlockedBy;
        this.occurredAt = LocalDateTime.now();
        this.source = "demo-app";
        this.automatic = false;
    }

    @Override
    public String getEventType() {
        return "user.unlocked";
    }

    @Override
    public String getTriggeredBy() {
        return unlockedBy != null ? unlockedBy : "system";
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
        return String.format("UserUnlockedEvent{userId=%s, email=%s, automatic=%s}",
                userId, email, automatic);
    }
}