package com.example.demo.modules.kitchen.domain.event;

import com.example.demo.common.event.DomainEvent;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KitchenColourChangedEvent implements DomainEvent {
    private static final long serialVersionUID = 1L;
    @Builder.Default
    private String eventId = UUID.randomUUID().toString();
    private UUID configurationId;
    private UUID collectionId;
    private UUID oldColourId;
    private String oldColourName;
    private UUID newColourId;
    private String newColourName;
    private int affectedItemCount;
    @Builder.Default
    private LocalDateTime occurredAt = LocalDateTime.now();

    @Override public String getEventType() { return "kitchen.configuration.colour_changed"; }
}