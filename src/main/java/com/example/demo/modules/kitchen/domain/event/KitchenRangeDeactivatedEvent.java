package com.example.demo.modules.kitchen.domain.event;

import com.example.demo.common.event.DomainEvent;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KitchenRangeDeactivatedEvent implements DomainEvent {
    private static final long serialVersionUID = 1L;
    @Builder.Default
    private String eventId = UUID.randomUUID().toString();
    private UUID rangeId;
    private String rangeCode;
    private String rangeName;
    private String reason;
    @Builder.Default
    private LocalDateTime occurredAt = LocalDateTime.now();

    @Override public String getEventType() { return "kitchen.range.deactivated"; }
}