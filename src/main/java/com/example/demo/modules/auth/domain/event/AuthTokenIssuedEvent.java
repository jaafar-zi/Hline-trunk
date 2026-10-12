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
 * Event published when an auth token is issued (login success)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthTokenIssuedEvent implements DomainEvent {

    private static final long serialVersionUID = 1L;

    @Builder.Default
    private String eventId = UUID.randomUUID().toString();

    private String subjectId;      // UUID as string (User or Customer ID)
    private String subjectType;    // "USER" or "CUSTOMER"
    private String email;
    private String ipAddress;
    private String userAgent;
    private String tokenType;      // "ACCESS" | "REFRESH"

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    @Builder.Default
    private LocalDateTime occurredAt = LocalDateTime.now();

    private String source;

    @Override public String getEventType()   { return "auth.token_issued"; }
    @Override public String getTriggeredBy() { return email; }
    @Override public String getSource()      { return source != null ? source : "demo-app"; }
    @Override public boolean isAsync()       { return true; }
}