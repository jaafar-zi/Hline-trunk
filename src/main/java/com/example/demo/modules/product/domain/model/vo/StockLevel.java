package com.example.demo.modules.product.domain.model.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

/**
 * StockLevel Value Object
 * 
 * Represents inventory stock levels with quantity tracking
 * 
 * Business Rules:
 * - Quantity on hand (total physical stock)
 * - Reserved (allocated but not yet shipped)
 * - Available (quantity - reserved)
 * - All values must be >= 0
 * 
 * @author PIM Team
 * @version 1.0
 */
@Embeddable
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class StockLevel implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final int UNLIMITED = Integer.MAX_VALUE;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name = "reserved", nullable = false)
    private int reserved;

    @Column(name = "available", nullable = false)
    private int available;

    /**
     * Create stock level
     */
    public static StockLevel of(int quantity, int reserved) {
        validateQuantity(quantity);
        validateQuantity(reserved);

        if (reserved > quantity) {
            throw new IllegalArgumentException(
                "Reserved quantity cannot exceed total quantity"
            );
        }

        return new StockLevel(quantity, reserved, quantity - reserved);
    }

    /**
     * Create stock level with only quantity
     */
    public static StockLevel of(int quantity) {
        return of(quantity, 0);
    }

    /**
     * Create zero stock level
     */
    public static StockLevel zero() {
        return new StockLevel(0, 0, 0);
    }

    /**
     * Create unlimited stock level (for digital products)
     */
    public static StockLevel unlimited() {
        return new StockLevel(UNLIMITED, 0, UNLIMITED);
    }

    /**
     * Set new total quantity
     */
    public StockLevel setQuantity(int newQuantity) {
        validateQuantity(newQuantity);

        if (newQuantity < reserved) {
            throw new IllegalArgumentException(
                String.format(
                    "Cannot set quantity to %d when %d units are reserved",
                    newQuantity, reserved
                )
            );
        }

        return new StockLevel(newQuantity, reserved, newQuantity - reserved);
    }

    /**
     * Add to quantity
     */
    public StockLevel add(int amount) {
        validateQuantity(amount);
        int newQuantity = quantity + amount;
        return new StockLevel(newQuantity, reserved, newQuantity - reserved);
    }

    /**
     * Subtract from quantity
     */
    public StockLevel subtract(int amount) {
        validateQuantity(amount);
        
        if (amount > quantity) {
            throw new IllegalArgumentException(
                String.format("Cannot subtract %d from quantity of %d", amount, quantity)
            );
        }

        int newQuantity = quantity - amount;

        if (newQuantity < reserved) {
            throw new IllegalArgumentException(
                String.format(
                    "Cannot subtract %d: would result in quantity (%d) less than reserved (%d)",
                    amount, newQuantity, reserved
                )
            );
        }

        return new StockLevel(newQuantity, reserved, newQuantity - reserved);
    }

    /**
     * Reserve quantity
     */
    public StockLevel reserve(int amount) {
        validateQuantity(amount);

        if (amount > available) {
            throw new IllegalArgumentException(
                String.format(
                    "Cannot reserve %d units: only %d available",
                    amount, available
                )
            );
        }

        int newReserved = reserved + amount;
        return new StockLevel(quantity, newReserved, quantity - newReserved);
    }

    /**
     * Release reservation
     */
    public StockLevel release(int amount) {
        validateQuantity(amount);

        if (amount > reserved) {
            throw new IllegalArgumentException(
                String.format(
                    "Cannot release %d units: only %d reserved",
                    amount, reserved
                )
            );
        }

        int newReserved = reserved - amount;
        return new StockLevel(quantity, newReserved, quantity - newReserved);
    }

    /**
     * Fulfill reservation (remove from both quantity and reserved)
     */
    public StockLevel fulfill(int amount) {
        validateQuantity(amount);

        if (amount > reserved) {
            throw new IllegalArgumentException(
                String.format(
                    "Cannot fulfill %d units: only %d reserved",
                    amount, reserved
                )
            );
        }

        int newQuantity = quantity - amount;
        int newReserved = reserved - amount;
        return new StockLevel(newQuantity, newReserved, newQuantity - newReserved);
    }

    /**
     * Check if unlimited
     */
    public boolean isUnlimited() {
        return quantity == UNLIMITED;
    }

    /**
     * Check if has stock
     */
    public boolean hasStock() {
        return available > 0;
    }

    /**
     * Validate quantity value
     */
    private static void validateQuantity(int value) {
        if (value < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        }
    }

    @Override
    public String toString() {
        if (isUnlimited()) {
            return "StockLevel[UNLIMITED]";
        }
        return String.format(
            "StockLevel[quantity=%d, reserved=%d, available=%d]",
            quantity, reserved, available
        );
    }
}