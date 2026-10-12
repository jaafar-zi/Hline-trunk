package com.example.demo.modules.order.domain.event;

import com.example.demo.common.event.DomainEvent;
import com.example.demo.modules.order.domain.model.vo.OrderStatus;
import com.example.demo.modules.order.domain.model.vo.ShippingStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Event published when an order is delivered
 * 
 * Triggers: Customer notification, review request, analytics
 */
@Getter
@Builder
public class OrderDeliveredEvent implements DomainEvent {
    
    @Builder.Default
    private final String eventId = UUID.randomUUID().toString();
    
    private final UUID orderId;
    private final String orderNumber;
    private final UUID customerId;
    private final String customerEmail;
    private final OrderStatus previousStatus;
    private final ShippingStatus previousShippingStatus;
    private final LocalDateTime occurredAt;
    private final String triggeredBy;
    private final String correlationId;
    private final String causationId;

    @Override
    public String getEventType() {
        return "order.delivered";
    }

    @Override
    public String getSource() {
        return "order-module";
    }

    @Override
    public boolean isAsync() {
        return true;
    }
}