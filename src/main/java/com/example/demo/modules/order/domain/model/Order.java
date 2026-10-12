package com.example.demo.modules.order.domain.model;

import com.example.demo.common.base.entity.SoftDeletableAggregateRoot;
import com.example.demo.common.event.DomainEvent;
import com.example.demo.common.event.EntityDeletedEvent;
import com.example.demo.common.event.EntityRestoredEvent;
import com.example.demo.common.event.EntityUpdatedEvent;
import com.example.demo.common.util.ChangeTracker;
import com.example.demo.modules.order.domain.event.*;
import com.example.demo.modules.order.domain.model.vo.*;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Order Aggregate Root
 *
 * Represents a customer order in the e-commerce system
 * Manages order lifecycle, items, pricing, payment, and shipping
 *
 * Business Rules:
 * - Order must have at least one item
 * - Order number must be unique
 * - Total must match sum of all items
 * - Cannot modify confirmed orders
 * - Cannot cancel shipped orders
 * - Payment must be authorized before shipping
 *
 * Events Published:
 * - OrderCreatedEvent (business-critical)
 * - OrderConfirmedEvent (business-critical)
 * - OrderPaidEvent (business-critical)
 * - OrderShippedEvent (business-critical)
 * - OrderDeliveredEvent (business-critical)
 * - OrderCancelledEvent (business-critical)
 * - OrderRefundedEvent (business-critical)
 * - EntityUpdatedEvent<Order> (with change tracking)
 * - EntityDeletedEvent (soft delete)
 * - EntityRestoredEvent (restore)
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
@Table(
        name = "orders",
        indexes = {
                @Index(name = "idx_order_number", columnList = "order_number"),
                @Index(name = "idx_order_customer_id", columnList = "customer_id"),
                @Index(name = "idx_order_status", columnList = "status"),
                @Index(name = "idx_order_payment_status", columnList = "payment_status"),
                @Index(name = "idx_order_created_at", columnList = "created_at"),
                @Index(name = "idx_order_customer_email", columnList = "customer_email")
        }
)
public class Order extends SoftDeletableAggregateRoot {

    // ====================================
    // Core Order Information
    // ====================================

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "order_number"))
    private OrderNumber orderNumber;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "customer_email", nullable = false, length = 255)
    private String customerEmail;

    @Column(name = "customer_first_name", nullable = false, length = 100)
    private String customerFirstName;

    @Column(name = "customer_last_name", nullable = false, length = 100)
    private String customerLastName;

    @Column(name = "customer_phone", length = 20)
    private String customerPhone;

    // ====================================
    // Order Status
    // ====================================

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    @Builder.Default
    private OrderStatus status = OrderStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 50)
    @Builder.Default
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "shipping_status", nullable = false, length = 50)
    @Builder.Default
    private ShippingStatus shippingStatus = ShippingStatus.NOT_SHIPPED;

    // ====================================
    // Addresses
    // ====================================

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "streetAddress", column = @Column(name = "shipping_street_address")),
            @AttributeOverride(name = "streetAddress2", column = @Column(name = "shipping_street_address_2")),
            @AttributeOverride(name = "city", column = @Column(name = "shipping_city")),
            @AttributeOverride(name = "stateProvince", column = @Column(name = "shipping_state_province")),
            @AttributeOverride(name = "postalCode", column = @Column(name = "shipping_postal_code")),
            @AttributeOverride(name = "country", column = @Column(name = "shipping_country")),
            @AttributeOverride(name = "phone", column = @Column(name = "shipping_phone"))
    })
    private Address shippingAddress;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "streetAddress", column = @Column(name = "billing_street_address")),
            @AttributeOverride(name = "streetAddress2", column = @Column(name = "billing_street_address_2")),
            @AttributeOverride(name = "city", column = @Column(name = "billing_city")),
            @AttributeOverride(name = "stateProvince", column = @Column(name = "billing_state_province")),
            @AttributeOverride(name = "postalCode", column = @Column(name = "billing_postal_code")),
            @AttributeOverride(name = "country", column = @Column(name = "billing_country")),
            @AttributeOverride(name = "phone", column = @Column(name = "billing_phone"))
    })
    private Address billingAddress;

    // ====================================
    // Pricing
    // ====================================

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "subtotal_amount")),
            @AttributeOverride(name = "currency", column = @Column(name = "subtotal_currency"))
    })
    private Money subtotal;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "tax_amount")),
            @AttributeOverride(name = "currency", column = @Column(name = "tax_currency"))
    })
    private Money taxAmount;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "shipping_cost_amount")),
            @AttributeOverride(name = "currency", column = @Column(name = "shipping_cost_currency"))
    })
    private Money shippingCost;

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
    private Money totalAmount;

    // ====================================
    // Payment Information
    // ====================================

    @Column(name = "payment_method", length = 50)
    private String paymentMethod;

    @Column(name = "payment_transaction_id", length = 255)
    private String paymentTransactionId;

    @Column(name = "payment_authorized_at")
    private LocalDateTime paymentAuthorizedAt;

    @Column(name = "payment_captured_at")
    private LocalDateTime paymentCapturedAt;

    // ====================================
    // Shipping Information
    // ====================================

    @Column(name = "shipping_method", length = 100)
    private String shippingMethod;

    @Column(name = "tracking_number", length = 255)
    private String trackingNumber;

    @Column(name = "carrier", length = 100)
    private String carrier;

    @Column(name = "shipped_at")
    private LocalDateTime shippedAt;

    @Column(name = "estimated_delivery_date")
    private LocalDateTime estimatedDeliveryDate;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    // ====================================
    // Additional Information
    // ====================================

    @Column(name = "customer_notes", columnDefinition = "TEXT")
    private String customerNotes;

    @Column(name = "internal_notes", columnDefinition = "TEXT")
    private String internalNotes;

    @Column(name = "discount_code", length = 50)
    private String discountCode;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    // ====================================
    // Order Lifecycle Timestamps
    // ====================================

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "cancellation_reason", length = 500)
    private String cancellationReason;

    @Column(name = "refunded_at")
    private LocalDateTime refundedAt;

    @Column(name = "refund_reason", length = 500)
    private String refundReason;

    // ====================================
    // Relationships (Aggregate Components)
    // ====================================

    @OneToMany(
            mappedBy = "order",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.EAGER
    )
    @Builder.Default
    private Set<OrderItem> items = new HashSet<>();

    // ====================================
    // Factory Methods
    // ====================================

    /**
     * Create new order
     *
     * Events Published:
     * - OrderCreatedEvent (via afterCreate hook)
     */
    public static Order create(
            UUID customerId,
            String customerEmail,
            String firstName,
            String lastName,
            Address shippingAddress,
            Address billingAddress,
            String currency) {

        validateCustomerInfo(customerId, customerEmail, firstName, lastName);
        shippingAddress.validate();
        billingAddress.validate();

        return Order.builder()
                .orderNumber(OrderNumber.generate())
                .customerId(customerId)
                .customerEmail(customerEmail)
                .customerFirstName(firstName)
                .customerLastName(lastName)
                .shippingAddress(shippingAddress)
                .billingAddress(billingAddress)
                .status(OrderStatus.PENDING)
                .paymentStatus(PaymentStatus.PENDING)
                .shippingStatus(ShippingStatus.NOT_SHIPPED)
                .subtotal(Money.zero(currency))
                .taxAmount(Money.zero(currency))
                .shippingCost(Money.zero(currency))
                .discountAmount(Money.zero(currency))
                .totalAmount(Money.zero(currency))
                .build();
    }

    private static void validateCustomerInfo(UUID customerId, String email, String firstName, String lastName) {
        if (customerId == null) {
            throw new IllegalArgumentException("Customer ID cannot be null");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Customer email cannot be null or empty");
        }
        if (firstName == null || firstName.isBlank()) {
            throw new IllegalArgumentException("Customer first name cannot be null or empty");
        }
        if (lastName == null || lastName.isBlank()) {
            throw new IllegalArgumentException("Customer last name cannot be null or empty");
        }
    }

    // ====================================
    // Domain Logic - Order Items
    // ====================================

    /**
     * Add item to order
     *
     * Events Published:
     * - EntityUpdatedEvent<Order> (with change tracking)
     */
    public OrderItem addItem(
            UUID productId,
            String productSku,
            String productName,
            Money unitPrice,
            int quantity) {

        ensureOrderIsModifiable();

        // Check if item already exists
        Optional<OrderItem> existing = items.stream()
                .filter(item -> item.getProductId().equals(productId))
                .findFirst();

        if (existing.isPresent()) {
            // Update existing item quantity
            OrderItem item = existing.get();
            int newQuantity = item.getQuantity() + quantity;
            item.updateQuantity(newQuantity);

            EntityUpdatedEvent<Order> event = new EntityUpdatedEvent<>(this);
            event.addChange("items." + productSku, item.getQuantity(), newQuantity);
            registerEvent(event);

            recalculateTotals();
            return item;
        } else {
            // Add new item
            OrderItem item = OrderItem.create(productId, productSku, productName, unitPrice, quantity);
            item.setOrder(this);
            this.items.add(item);

            EntityUpdatedEvent<Order> event = new EntityUpdatedEvent<>(this);
            event.addChange("items", null, "Added item: " + productSku);
            registerEvent(event);

            recalculateTotals();
            return item;
        }
    }

    /**
     * Remove item from order
     */
    public void removeItem(UUID itemId) {
        ensureOrderIsModifiable();

        OrderItem item = items.stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Item not found: " + itemId));

        this.items.remove(item);

        EntityUpdatedEvent<Order> event = new EntityUpdatedEvent<>(this);
        event.addChange("items", "Removed item: " + item.getProductSku(), null);
        registerEvent(event);

        recalculateTotals();
    }

    /**
     * Update item quantity
     */
    public void updateItemQuantity(UUID itemId, int newQuantity) {
        ensureOrderIsModifiable();

        OrderItem item = getItem(itemId);
        int oldQuantity = item.getQuantity();
        item.updateQuantity(newQuantity);

        ChangeTracker tracker = new ChangeTracker();
        tracker.track("items." + item.getProductSku() + ".quantity", oldQuantity, newQuantity);
        registerEvent(new EntityUpdatedEvent<>(this, tracker.getChanges()));

        recalculateTotals();
    }

    public OrderItem getItem(UUID itemId) {
        return items.stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Item not found: " + itemId));
    }

    public boolean hasItems() {
        return items != null && !items.isEmpty();
    }

    public int getTotalItemCount() {
        return items.stream()
                .mapToInt(OrderItem::getQuantity)
                .sum();
    }

    // ====================================
    // Domain Logic - Pricing
    // ====================================

    /**
     * Recalculate order totals
     */
    public void recalculateTotals() {
        if (items.isEmpty()) {
            this.subtotal = Money.zero(getCurrency());
            this.taxAmount = Money.zero(getCurrency());
            this.totalAmount = Money.zero(getCurrency());
            return;
        }

        // Sum all item subtotals
        this.subtotal = items.stream()
                .map(OrderItem::getSubtotal)
                .reduce(Money::add)
                .orElse(Money.zero(getCurrency()));

        // Sum all item taxes
        this.taxAmount = items.stream()
                .map(OrderItem::getTaxAmount)
                .reduce(Money::add)
                .orElse(Money.zero(getCurrency()));

        // Total = subtotal + tax + shipping - discount
        this.totalAmount = subtotal
                .add(taxAmount)
                .add(shippingCost != null ? shippingCost : Money.zero(getCurrency()))
                .subtract(discountAmount != null ? discountAmount : Money.zero(getCurrency()));
    }

    /**
     * Apply discount
     */
    public void applyDiscount(Money discount, String discountCode) {
        ensureOrderIsModifiable();

        if (discount.isNegative()) {
            throw new IllegalArgumentException("Discount cannot be negative");
        }

        Money oldDiscount = this.discountAmount;
        this.discountAmount = discount;
        this.discountCode = discountCode;

        ChangeTracker tracker = new ChangeTracker();
        tracker.track("discountAmount", oldDiscount, discount);
        tracker.track("discountCode", this.discountCode, discountCode);
        registerEvent(new EntityUpdatedEvent<>(this, tracker.getChanges()));

        recalculateTotals();
    }

    /**
     * Set shipping cost
     */
    public void setShippingCost(Money cost, String method) {
        ensureOrderIsModifiable();

        Money oldCost = this.shippingCost;
        this.shippingCost = cost;
        this.shippingMethod = method;

        ChangeTracker tracker = new ChangeTracker();
        tracker.track("shippingCost", oldCost, cost);
        tracker.track("shippingMethod", this.shippingMethod, method);
        registerEvent(new EntityUpdatedEvent<>(this, tracker.getChanges()));

        recalculateTotals();
    }

    /**
     * Apply tax rate to all items
     */
    public void applyTaxRate(BigDecimal taxRate) {
        ensureOrderIsModifiable();

        items.forEach(item -> item.setTaxRate(taxRate));
        recalculateTotals();

        EntityUpdatedEvent<Order> event = new EntityUpdatedEvent<>(this);
        event.addChange("taxRate", null, taxRate);
        registerEvent(event);
    }

    private String getCurrency() {
        return subtotal != null ? subtotal.getCurrency() : "USD";
    }

    // ====================================
    // Domain Logic - Order Lifecycle
    // ====================================

    /**
     * Confirm order
     *
     * Events Published:
     * - OrderConfirmedEvent (business-critical)
     */
    public void confirm() {
        if (this.status != OrderStatus.PENDING) {
            throw new IllegalStateException("Only pending orders can be confirmed");
        }

        if (!hasItems()) {
            throw new IllegalStateException("Cannot confirm order without items");
        }

        OrderStatus oldStatus = this.status;
        this.status = OrderStatus.CONFIRMED;
        this.confirmedAt = LocalDateTime.now();

        registerEvent(OrderConfirmedEvent.builder()
                .orderId(this.getId())
                .orderNumber(this.orderNumber.getValue())
                .customerId(this.customerId)
                .customerEmail(this.customerEmail)
                .totalAmount(this.totalAmount)
                .itemCount(this.items.size())
                .previousStatus(oldStatus)
                .occurredAt(this.confirmedAt)
                .triggeredBy(getCurrentUsername())
                .build());
    }

    /**
     * Mark order as paid
     *
     * Events Published:
     * - OrderPaidEvent (business-critical)
     */
    public void markAsPaid(String transactionId, String paymentMethod) {
        if (this.paymentStatus.isPaid()) {
            throw new IllegalStateException("Order is already paid");
        }

        PaymentStatus oldPaymentStatus = this.paymentStatus;
        this.paymentStatus = PaymentStatus.CAPTURED;
        this.paymentTransactionId = transactionId;
        this.paymentMethod = paymentMethod;
        this.paymentCapturedAt = LocalDateTime.now();

        // Update order status to PAID if confirmed
        if (this.status == OrderStatus.CONFIRMED) {
            this.status = OrderStatus.PAID;
        }

        registerEvent(OrderPaidEvent.builder()
                .orderId(this.getId())
                .orderNumber(this.orderNumber.getValue())
                .customerId(this.customerId)
                .totalAmount(this.totalAmount)
                .paymentMethod(paymentMethod)
                .transactionId(transactionId)
                .previousPaymentStatus(oldPaymentStatus)
                .occurredAt(this.paymentCapturedAt)
                .triggeredBy(getCurrentUsername())
                .build());
    }

    /**
     * Mark order as shipped
     *
     * Events Published:
     * - OrderShippedEvent (business-critical)
     */
    public void ship(String trackingNumber, String carrier, LocalDateTime estimatedDelivery) {
        if (!this.paymentStatus.isPaid()) {
            throw new IllegalStateException("Cannot ship order that is not paid");
        }

        if (this.shippingStatus.isDelivered()) {
            throw new IllegalStateException("Order is already delivered");
        }

        OrderStatus oldStatus = this.status;
        ShippingStatus oldShippingStatus = this.shippingStatus;

        this.status = OrderStatus.SHIPPED;
        this.shippingStatus = ShippingStatus.SHIPPED;
        this.trackingNumber = trackingNumber;
        this.carrier = carrier;
        this.shippedAt = LocalDateTime.now();
        this.estimatedDeliveryDate = estimatedDelivery;

        registerEvent(OrderShippedEvent.builder()
                .orderId(this.getId())
                .orderNumber(this.orderNumber.getValue())
                .customerId(this.customerId)
                .customerEmail(this.customerEmail)
                .shippingAddress(this.shippingAddress)
                .trackingNumber(trackingNumber)
                .carrier(carrier)
                .estimatedDeliveryDate(estimatedDelivery)
                .previousStatus(oldStatus)
                .previousShippingStatus(oldShippingStatus)
                .occurredAt(this.shippedAt)
                .triggeredBy(getCurrentUsername())
                .build());
    }

    /**
     * Mark order as delivered
     *
     * Events Published:
     * - OrderDeliveredEvent (business-critical)
     */
    public void markAsDelivered() {
        if (this.status != OrderStatus.SHIPPED) {
            throw new IllegalStateException("Only shipped orders can be marked as delivered");
        }

        OrderStatus oldStatus = this.status;
        ShippingStatus oldShippingStatus = this.shippingStatus;

        this.status = OrderStatus.DELIVERED;
        this.shippingStatus = ShippingStatus.DELIVERED;
        this.deliveredAt = LocalDateTime.now();

        registerEvent(OrderDeliveredEvent.builder()
                .orderId(this.getId())
                .orderNumber(this.orderNumber.getValue())
                .customerId(this.customerId)
                .customerEmail(this.customerEmail)
                .occurredAt(this.deliveredAt)
                .previousStatus(oldStatus)
                .previousShippingStatus(oldShippingStatus)
                .build());
    }

    /**
     * Cancel order
     *
     * Events Published:
     * - OrderCancelledEvent (business-critical)
     */
    public void cancel(String reason) {
        if (!this.status.isCancellable()) {
            throw new IllegalStateException(
                    String.format("Cannot cancel order in status: %s", this.status));
        }

        OrderStatus oldStatus = this.status;
        this.status = OrderStatus.CANCELLED;
        this.cancelledAt = LocalDateTime.now();
        this.cancellationReason = reason;

        registerEvent(OrderCancelledEvent.builder()
                .orderId(this.getId())
                .orderNumber(this.orderNumber.getValue())
                .customerId(this.customerId)
                .customerEmail(this.customerEmail)
                .reason(reason)
                .previousStatus(oldStatus)
                .occurredAt(this.cancelledAt)
                .triggeredBy(getCurrentUsername())
                .build());
    }

    /**
     * Refund order
     *
     * Events Published:
     * - OrderRefundedEvent (business-critical)
     */
    public void refund(String reason) {
        if (!this.paymentStatus.isPaid()) {
            throw new IllegalStateException("Cannot refund order that is not paid");
        }

        PaymentStatus oldPaymentStatus = this.paymentStatus;
        OrderStatus oldStatus = this.status;

        this.paymentStatus = PaymentStatus.REFUNDED;
        this.status = OrderStatus.REFUNDED;
        this.refundedAt = LocalDateTime.now();
        this.refundReason = reason;

        registerEvent(OrderRefundedEvent.builder()
                .orderId(this.getId())
                .orderNumber(this.orderNumber.getValue())
                .customerId(this.customerId)
                .refundAmount(this.totalAmount)
                .reason(reason)
                .previousStatus(oldStatus)
                .previousPaymentStatus(oldPaymentStatus)
                .occurredAt(this.refundedAt)
                .triggeredBy(getCurrentUsername())
                .build());
    }

    // ====================================
    // Domain Logic - Updates
    // ====================================

    /**
     * Update shipping address
     */
    public void updateShippingAddress(Address newAddress) {
        ensureOrderIsModifiable();
        newAddress.validate();

        Address oldAddress = this.shippingAddress;
        this.shippingAddress = newAddress;

        ChangeTracker tracker = new ChangeTracker();
        tracker.track("shippingAddress", oldAddress.getFullAddress(), newAddress.getFullAddress());
        registerEvent(new EntityUpdatedEvent<>(this, tracker.getChanges()));
    }

    /**
     * Update customer notes
     */
    public void updateCustomerNotes(String notes) {
        String oldNotes = this.customerNotes;
        this.customerNotes = notes;

        ChangeTracker tracker = new ChangeTracker();
        tracker.track("customerNotes", oldNotes, notes);
        registerEvent(new EntityUpdatedEvent<>(this, tracker.getChanges()));
    }

    /**
     * Update internal notes
     */
    public void updateInternalNotes(String notes) {
        String oldNotes = this.internalNotes;
        this.internalNotes = notes;

        ChangeTracker tracker = new ChangeTracker();
        tracker.track("internalNotes", oldNotes, notes);
        registerEvent(new EntityUpdatedEvent<>(this, tracker.getChanges()));
    }

    // ====================================
    // Validation
    // ====================================

    private void ensureOrderIsModifiable() {
        if (this.status == OrderStatus.SHIPPED || this.status == OrderStatus.DELIVERED) {
            throw new IllegalStateException(
                    String.format("Cannot modify order in status: %s", this.status));
        }
        if (this.status == OrderStatus.CANCELLED || this.status == OrderStatus.REFUNDED) {
            throw new IllegalStateException(
                    String.format("Cannot modify cancelled or refunded order"));
        }
    }

    // ====================================
    // Query Methods
    // ====================================

    @Transient
    public boolean isPaid() {
        return paymentStatus.isPaid();
    }

    @Transient
    public boolean isShipped() {
        return status == OrderStatus.SHIPPED || status == OrderStatus.DELIVERED;
    }

    @Transient
    public boolean isDelivered() {
        return status == OrderStatus.DELIVERED;
    }

    @Transient
    public boolean isCancelled() {
        return status == OrderStatus.CANCELLED;
    }

    @Transient
    public boolean canBeCancelled() {
        return status.isCancellable();
    }

    @Transient
    public String getCustomerFullName() {
        return customerFirstName + " " + customerLastName;
    }

    // ====================================
    // SoftDeletableAggregateRoot Implementation
    // ====================================

    @Override
    protected DomainEvent createDeletedEvent(String deletedBy) {
        return EntityDeletedEvent.builder()
                .entityClass(Order.class)
                .entityType("Order")
                .entityId(this.getId())
                .deletedBy(deletedBy)
                .softDelete(true)
                .reason("Order soft-deleted: " + this.orderNumber)
                .occurredAt(LocalDateTime.now())
                .build();
    }

    @Override
    protected DomainEvent createRestoredEvent(String restoredBy, String previousDeletedBy) {
        return EntityRestoredEvent.builder()
                .entityClass(Order.class)
                .entityType("Order")
                .entityId(this.getId())
                .restoredBy(restoredBy)
                .previousDeletedBy(previousDeletedBy)
                .previousDeletedAt(this.getDeletedAt())
                .occurredAt(LocalDateTime.now())
                .build();
    }

    // ====================================
    // Helper Methods
    // ====================================

    @Transient
    private String getCurrentUsername() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                return authentication.getName();
            }
        } catch (Exception e) {
            // Ignore
        }
        return "system";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Order)) return false;
        Order order = (Order) o;
        return orderNumber != null && orderNumber.equals(order.orderNumber);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return String.format("Order[id=%s, orderNumber=%s, status=%s, total=%s]",
                getId(), orderNumber, status, totalAmount);
    }
}