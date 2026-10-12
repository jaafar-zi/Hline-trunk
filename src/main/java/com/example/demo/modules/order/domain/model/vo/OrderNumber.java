package com.example.demo.modules.order.domain.model.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Order Number Value Object
 * 
 * Represents a unique order identifier
 * Format: ORD-YYYYMMDD-XXXXXX (e.g., ORD-20240115-000001)
 * 
 * @author Order Team
 * @version 1.0
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class OrderNumber implements Serializable {

    private static final AtomicLong sequence = new AtomicLong(1);
    private static final String PREFIX = "ORD";

    @Column(name = "order_number", nullable = false, unique = true, length = 50)
    private String value;

    /**
     * Generate new order number
     */
    public static OrderNumber generate() {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String seq = String.format("%06d", sequence.getAndIncrement());
        return new OrderNumber(PREFIX + "-" + date + "-" + seq);
    }

    /**
     * Create from existing value
     */
    public static OrderNumber of(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Order number cannot be null or empty");
        }
        return new OrderNumber(value.toUpperCase().trim());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OrderNumber)) return false;
        OrderNumber that = (OrderNumber) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}