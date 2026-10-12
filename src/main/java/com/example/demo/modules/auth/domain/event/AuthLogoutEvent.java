package com.example.demo.modules.auth.domain.event;

import com.example.demo.common.event.DomainEvent;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Event published on logout / token revocation
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthLogoutEvent implements DomainEvent {

    private static final long serialVersionUID = 1L;

    @Builder.Default
    private String eventId = UUID.randomUUID().toString();

    private String subjectId;
    private String subjectType;
    private String email;
    private String ipAddress;
    private boolean forced;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    @Builder.Default
    private LocalDateTime occurredAt = LocalDateTime.now();

    private String source;

    @Override public String getEventType()   { return "auth.logout"; }
    @Override public String getTriggeredBy() { return email; }
    @Override public String getSource()      { return source != null ? source : "demo-app"; }
    @Override public boolean isAsync()       { return true; }
}