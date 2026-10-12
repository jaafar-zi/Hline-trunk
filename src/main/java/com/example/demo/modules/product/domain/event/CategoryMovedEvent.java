package com.example.demo.modules.product.domain.event;

import com.example.demo.common.event.DomainEvent;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Event published when category is moved to different parent
 */
@Getter
@Builder
public class CategoryMovedEvent implements DomainEvent {
    @Builder.Default
    private final String eventId = UUID.randomUUID().toString();

    private final UUID categoryId;
    private final String categoryCode;
    private final String categoryName;

    private final UUID oldParentId;
    private final String oldParentName;

    private final UUID newParentId;
    private final String newParentName;

    private final Integer oldLevel;
    private final Integer newLevel;

    private final String oldPath;
    private final String newPath;

    private final LocalDateTime occurredAt;
    private final String triggeredBy;
    private final String correlationId;
    private final String causationId;

    @Override
    public String getEventType() {
        return "category.moved";
    }

    @Override
    public String getSource() {
        return "product-module";
    }

    @Override
    public boolean isAsync() {
        return true;
    }
}
