package com.example.demo.modules.order.domain.model;

import com.example.demo.common.base.entity.BaseEntity;
import com.example.demo.modules.order.domain.model.vo.Money;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Order Item Entity
 * 
 * Represents a line item in an order
 * Contains snapshot of product data at time of order
 * 
 * @author Order Team
 * @version 1.0
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "order_items")
public class OrderItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    // Product snapshot (at time of order)
    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "product_sku", nullable = false, length = 50)
    private String productSku;

    @Column(name = "product_name", nullable = false, length = 500)
    private String productName;

    @Column(name = "product_variant_id")
    private UUID productVariantId;

    @Column(name = "product_variant_sku", length = 50)
    private String productVariantSku;

    // Pricing snapshot
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "amount", column = @Column(name = "unit_price_amount")),
        @AttributeOverride(name = "currency", column = @Column(name = "unit_price_currency"))
    })
    private Money unitPrice;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "amount", column = @Column(name = "subtotal_amount")),
        @AttributeOverride(name = "currency", column = @Column(name = "subtotal_currency"))
    })
    private Money subtotal;

    @Column(name = "tax_rate", precision = 5, scale = 4)
    private BigDecimal taxRate;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "amount", column = @Column(name = "tax_amount")),
        @AttributeOverride(name = "currency", column = @Column(name = "tax_currency"))
    })
    private Money taxAmount;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "amount", column = @Column(name = "discount_amount")),
        @AttributeOverride(name = "currency", column = @Column(name = "discount_currency"))
    })
    private Money discountAmount;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "amount", column = @Column(name = "total_amount")),
        @AttributeOverride(name = "currency", column = @Column(name = "total_currency"))
    })
    private Money total;

    @Column(name = "notes", length = 1000)
    private String notes;

    // ====================================
    // Factory Methods
    // ====================================

    public static OrderItem create(
            UUID productId,
            String productSku,
            String productName,
            Money unitPrice,
            int quantity) {
        
        if (productId == null) {
            throw new IllegalArgumentException("Product ID cannot be null");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        if (unitPrice == null || !unitPrice.isPositive()) {
            throw new IllegalArgumentException("Unit price must be positive");
        }

        OrderItem item = OrderItem.builder()
                .productId(productId)
                .productSku(productSku)
                .productName(productName)
                .unitPrice(unitPrice)
                .quantity(quantity)
                .taxRate(BigDecimal.ZERO)
                .discountAmount(Money.zero(unitPrice.getCurrency()))
                .build();

        item.calculateTotals();
        return item;
    }

    // ====================================
    // Domain Logic
    // ====================================

    /**
     * Update quantity
     */
    public void updateQuantity(int newQuantity) {
        if (newQuantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        this.quantity = newQuantity;
        calculateTotals();
    }

    /**
     * Apply discount
     */
    public void applyDiscount(Money discount) {
        if (discount.isNegative()) {
            throw new IllegalArgumentException("Discount cannot be negative");
        }
        this.discountAmount = discount;
        calculateTotals();
    }

    /**
     * Set tax rate
     */
    public void setTaxRate(BigDecimal rate) {
        if (rate.compareTo(BigDecimal.ZERO) < 0 || rate.compareTo(BigDecimal.ONE) > 0) {
            throw new IllegalArgumentException("Tax rate must be between 0 and 1");
        }
        this.taxRate = rate;
        calculateTotals();
    }

    /**
     * Calculate all totals
     */
    public void calculateTotals() {
        // Subtotal = unit price * quantity
        this.subtotal = unitPrice.multiply(quantity);

        // Apply discount
        Money discountedSubtotal = subtotal.subtract(discountAmount);

        // Calculate tax
        this.taxAmount = discountedSubtotal.multiply(taxRate);

        // Total = subtotal - discount + tax
        this.total = discountedSubtotal.add(taxAmount);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OrderItem)) return false;
        OrderItem orderItem = (OrderItem) o;
        return getId() != null && getId().equals(orderItem.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return String.format("OrderItem[id=%s, productSku=%s, quantity=%d, total=%s]",
                getId(), productSku, quantity, total);
    }
}