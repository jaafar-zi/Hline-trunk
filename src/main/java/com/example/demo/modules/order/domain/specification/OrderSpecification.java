package com.example.demo.modules.order.domain.specification;

import com.example.demo.modules.order.application.dto.filter.OrderFilter;
import com.example.demo.modules.order.domain.model.Order;
import com.example.demo.modules.order.domain.model.vo.OrderStatus;
import com.example.demo.modules.order.domain.model.vo.PaymentStatus;
import com.example.demo.modules.order.domain.model.vo.ShippingStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Order Specification
 * 
 * Provides dynamic query building for Order entity
 * 
 * @author Order Team
 * @version 1.0
 */
public class OrderSpecification {

    /**
     * Create specification from filter
     */
    public static Specification<Order> fromFilter(OrderFilter filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Customer ID filter
            if (filter.getCustomerId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("customerId"), filter.getCustomerId()));
            }

            // Customer email filter
            if (filter.getCustomerEmail() != null && !filter.getCustomerEmail().isBlank()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("customerEmail")),
                        "%" + filter.getCustomerEmail().toLowerCase() + "%"
                ));
            }

            // Order status filter
            if (filter.getStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), filter.getStatus()));
            }

            // Payment status filter
            if (filter.getPaymentStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("paymentStatus"), filter.getPaymentStatus()));
            }

            // Shipping status filter
            if (filter.getShippingStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("shippingStatus"), filter.getShippingStatus()));
            }

            // Date range filters
            if (filter.getCreatedFrom() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), filter.getCreatedFrom()));
            }

            if (filter.getCreatedTo() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), filter.getCreatedTo()));
            }

            // Total amount range
            if (filter.getMinAmount() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("totalAmount").get("amount"), filter.getMinAmount()));
            }

            if (filter.getMaxAmount() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("totalAmount").get("amount"), filter.getMaxAmount()));
            }

            // Currency filter
            if (filter.getCurrency() != null && !filter.getCurrency().isBlank()) {
                predicates.add(criteriaBuilder.equal(root.get("totalAmount").get("currency"), filter.getCurrency()));
            }

            // Payment method filter
            if (filter.getPaymentMethod() != null && !filter.getPaymentMethod().isBlank()) {
                predicates.add(criteriaBuilder.equal(root.get("paymentMethod"), filter.getPaymentMethod()));
            }

            // Tracking number filter
            if (filter.getTrackingNumber() != null && !filter.getTrackingNumber().isBlank()) {
                predicates.add(criteriaBuilder.equal(root.get("trackingNumber"), filter.getTrackingNumber()));
            }

            // Exclude deleted (soft delete filter)
            if (filter.isExcludeDeleted()) {
                predicates.add(criteriaBuilder.equal(root.get("deleted"), false));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Find orders by customer
     */
    public static Specification<Order> hasCustomerId(UUID customerId) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("customerId"), customerId);
    }

    /**
     * Find orders by status
     */
    public static Specification<Order> hasStatus(OrderStatus status) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("status"), status);
    }

    /**
     * Find orders by payment status
     */
    public static Specification<Order> hasPaymentStatus(PaymentStatus paymentStatus) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("paymentStatus"), paymentStatus);
    }

    /**
     * Find orders by shipping status
     */
    public static Specification<Order> hasShippingStatus(ShippingStatus shippingStatus) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("shippingStatus"), shippingStatus);
    }

    /**
     * Find orders created after date
     */
    public static Specification<Order> createdAfter(LocalDateTime date) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), date);
    }

    /**
     * Find orders created before date
     */
    public static Specification<Order> createdBefore(LocalDateTime date) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), date);
    }

    /**
     * Find orders created between dates
     */
    public static Specification<Order> createdBetween(LocalDateTime startDate, LocalDateTime endDate) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.between(root.get("createdAt"), startDate, endDate);
    }

    /**
     * Find orders by customer email
     */
    public static Specification<Order> hasCustomerEmail(String email) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("customerEmail")),
                        "%" + email.toLowerCase() + "%"
                );
    }

    /**
     * Find orders by customer name
     */
    public static Specification<Order> hasCustomerName(String name) {
        return (root, query, criteriaBuilder) -> {
            String pattern = "%" + name.toLowerCase() + "%";
            return criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("customerFirstName")), pattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("customerLastName")), pattern)
            );
        };
    }

    /**
     * Exclude deleted orders
     */
    public static Specification<Order> notDeleted() {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("deleted"), false);
    }

    /**
     * Find active orders (not cancelled, not refunded, not delivered)
     */
    public static Specification<Order> isActive() {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.and(
                        criteriaBuilder.notEqual(root.get("status"), OrderStatus.CANCELLED),
                        criteriaBuilder.notEqual(root.get("status"), OrderStatus.REFUNDED),
                        criteriaBuilder.notEqual(root.get("status"), OrderStatus.DELIVERED)
                );
    }

    /**
     * Find pending orders
     */
    public static Specification<Order> isPending() {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("status"), OrderStatus.PENDING);
    }

    /**
     * Find paid orders
     */
    public static Specification<Order> isPaid() {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.in(root.get("paymentStatus")).value(PaymentStatus.CAPTURED).value(PaymentStatus.AUTHORIZED);
    }

    /**
     * Combine specifications with AND
     */
    public static Specification<Order> and(Specification<Order> spec1, Specification<Order> spec2) {
        return Specification.where(spec1).and(spec2);
    }

    /**
     * Combine specifications with OR
     */
    public static Specification<Order> or(Specification<Order> spec1, Specification<Order> spec2) {
        return Specification.where(spec1).or(spec2);
    }
}