package com.example.demo.modules.product.domain.event;

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
 * Event published when stock is reserved for an order
 * 
 * Used for:
 * - Order fulfillment tracking
 * - Inventory allocation
 * - Available quantity calculations
 * - Reservation timeout monitoring
 * 
 * @author PIM Team
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockReservationEvent implements DomainEvent {

    private static final long serialVersionUID = 1L;

    @Builder.Default
    private String eventId = UUID.randomUUID().toString();

    private UUID productId;
    private String sku;

    /**
     * Reservation details
     */
    private UUID reservationId;
    private String reservationType; // RESERVED, RELEASED, FULFILLED, EXPIRED
    private Integer quantity;
    private Integer previousReserved;
    private Integer newReserved;
    private Integer availableAfterReservation;

    /**
     * Order/Reference information
     */
    private UUID orderId;
    private String orderNumber;
    private UUID customerId;
    private String customerName;

    /**
     * Timing
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime reservedAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime expiresAt;

    private Integer timeoutMinutes; // Reservation timeout

    /**
     * Location
     */
    private String warehouseLocation;
    private String binLocation;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    @Builder.Default
    private LocalDateTime occurredAt = LocalDateTime.now();

    private String correlationId;
    private String causationId;
    private String source;
    private EventMetadata metadata;

    private String reservedBy;
    private String reason;

    public StockReservationEvent(
        UUID productId,
        String sku,
        UUID orderId,
        Integer quantity,
        String reservationType
    ) {
        this.eventId = UUID.randomUUID().toString();
        this.productId = productId;
        this.sku = sku;
        this.orderId = orderId;
        this.quantity = quantity;
        this.reservationType = reservationType;
        this.occurredAt = LocalDateTime.now();
        this.source = "product-inventory-service";
    }

    @Override
    public String getEventType() {
        return "product.inventory.stock_reservation";
    }

    @Override
    public String getTriggeredBy() {
        return reservedBy != null ? reservedBy : "order-service";
    }

    @Override
    public String getSource() {
        return source != null ? source : "product-inventory-service";
    }

    @Override
    public boolean isAsync() {
        return true;
    }

    /**
     * Check if reservation is expired
     */
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * Get minutes until expiration
     */
    public Long getMinutesUntilExpiration() {
        if (expiresAt == null) {
            return null;
        }
        return java.time.temporal.ChronoUnit.MINUTES.between(
            LocalDateTime.now(),
            expiresAt
        );
    }

    @Override
    public String toString() {
        return String.format(
            "StockReservationEvent{productId=%s, sku=%s, order=%s, quantity=%d, type=%s}",
            productId, sku, orderNumber, quantity, reservationType
        );
    }
}