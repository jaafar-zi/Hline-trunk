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
 * Event published when user is added to group
 *
 * Triggers:
 * - Group membership cache update
 * - User notification
 * - Permission recalculation (if group has role-based permissions)
 * - Audit logging
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAddedToGroupEvent implements DomainEvent {

    private static final long serialVersionUID = 1L;

    @Builder.Default
    private String eventId = UUID.randomUUID().toString();

    private UUID userId;
    private String userEmail;
    private UUID userGroupId;
    private String groupName;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    @Builder.Default
    private LocalDateTime occurredAt = LocalDateTime.now();

    private String addedBy;
    private String correlationId;
    private String source;
    private EventMetadata metadata;

    /**
     * Was user automatically added (e.g., via rules)?
     */
    private boolean automatic;

    /**
     * Current group member count after addition
     */
    private Long groupMemberCount;

    public UserAddedToGroupEvent(UUID userId, String userEmail, UUID userGroupId, String groupName, String addedBy) {
        this.eventId = UUID.randomUUID().toString();
        this.userId = userId;
        this.userEmail = userEmail;
        this.userGroupId = userGroupId;
        this.groupName = groupName;
        this.addedBy = addedBy;
        this.occurredAt = LocalDateTime.now();
        this.source = "demo-app";
        this.automatic = false;
    }

    @Override
    public String getEventType() {
        return "user_group.user_added";
    }

    @Override
    public String getTriggeredBy() {
        return addedBy != null ? addedBy : "system";
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
        return String.format("UserAddedToGroupEvent{user=%s, group=%s, auto=%s}",
                userEmail, groupName, automatic);
    }
}