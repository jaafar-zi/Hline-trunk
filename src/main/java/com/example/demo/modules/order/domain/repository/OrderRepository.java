package com.example.demo.modules.order.domain.repository;

import com.example.demo.common.base.repository.SoftDeleteRepository;
import com.example.demo.modules.order.domain.model.Order;
import com.example.demo.modules.order.domain.model.vo.OrderNumber;
import com.example.demo.modules.order.domain.model.vo.OrderStatus;
import com.example.demo.modules.order.domain.model.vo.PaymentStatus;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Order Repository
 * 
 * Provides data access operations for Order aggregate
 * 
 * @author Order Team
 * @version 1.0
 */
@Repository
public interface OrderRepository extends SoftDeleteRepository<Order, UUID>, JpaSpecificationExecutor<Order> {

    // ====================================
    // Basic Finders
    // ====================================

    /**
     * Find order by order number
     */
    Optional<Order> findByOrderNumber(OrderNumber orderNumber);

    /**
     * Find order by order number value
     */
    @Query("SELECT o FROM Order o WHERE o.orderNumber.value = :orderNumber")
    Optional<Order> findByOrderNumberValue(@Param("orderNumber") String orderNumber);

    /**
     * Check if order number exists
     */
    boolean existsByOrderNumber(OrderNumber orderNumber);

    @Query("SELECT CASE WHEN COUNT(o) > 0 THEN true ELSE false END FROM Order o WHERE o.orderNumber.value = :orderNumber")
    boolean existsByOrderNumberValue(@Param("orderNumber") String orderNumber);

    // ====================================
    // Customer Queries
    // ====================================

    /**
     * Find all orders for a customer
     */
    List<Order> findByCustomerId(UUID customerId);

    /**
     * Find orders for customer ordered by creation date descending
     */
    List<Order> findByCustomerIdOrderByCreatedAtDesc(UUID customerId);

    /**
     * Find orders by customer email
     */
    List<Order> findByCustomerEmail(String customerEmail);

    /**
     * Count orders by customer
     */
    long countByCustomerId(UUID customerId);

    // ====================================
    // Status Queries
    // ====================================

    /**
     * Find orders by status
     */
    List<Order> findByStatus(OrderStatus status);

    /**
     * Find orders by payment status
     */
    List<Order> findByPaymentStatus(PaymentStatus paymentStatus);

    /**
     * Find orders by customer and status
     */
    List<Order> findByCustomerIdAndStatus(UUID customerId, OrderStatus status);

    /**
     * Count orders by status
     */
    long countByStatus(OrderStatus status);

    /**
     * Count orders by payment status
     */
    long countByPaymentStatus(PaymentStatus paymentStatus);

    // ====================================
    // Date Range Queries
    // ====================================

    /**
     * Find orders created between dates
     */
    List<Order> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Find orders created after date
     */
    List<Order> findByCreatedAtAfter(LocalDateTime date);

    /**
     * Find orders confirmed between dates
     */
    List<Order> findByConfirmedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Find orders shipped between dates
     */
    List<Order> findByShippedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    // ====================================
    // Complex Queries
    // ====================================

    /**
     * Find pending orders older than specified date
     */
    @Query("SELECT o FROM Order o WHERE o.status = 'PENDING' AND o.createdAt < :date")
    List<Order> findPendingOrdersOlderThan(@Param("date") LocalDateTime date);

    /**
     * Find confirmed orders awaiting payment
     */
    @Query("SELECT o FROM Order o WHERE o.status = 'CONFIRMED' AND o.paymentStatus = 'PENDING'")
    List<Order> findConfirmedOrdersAwaitingPayment();

    /**
     * Find paid orders awaiting shipment
     */
    @Query("SELECT o FROM Order o WHERE o.paymentStatus IN ('AUTHORIZED', 'CAPTURED') AND o.shippingStatus = 'NOT_SHIPPED'")
    List<Order> findPaidOrdersAwaitingShipment();

    /**
     * Find shipped orders not yet delivered
     */
    @Query("SELECT o FROM Order o WHERE o.status = 'SHIPPED' AND o.deliveredAt IS NULL")
    List<Order> findShippedOrdersNotDelivered();

    /**
     * Find orders by tracking number
     */
    Optional<Order> findByTrackingNumber(String trackingNumber);

    /**
     * Find orders by payment transaction ID
     */
    Optional<Order> findByPaymentTransactionId(String transactionId);

    // ====================================
    // Aggregation Queries
    // ====================================

    /**
     * Get total revenue for customer
     */
    @Query("SELECT SUM(o.totalAmount.amount) FROM Order o WHERE o.customerId = :customerId AND o.paymentStatus = 'CAPTURED'")
    Double getTotalRevenueForCustomer(@Param("customerId") UUID customerId);

    /**
     * Get total revenue for date range
     */
    @Query("SELECT SUM(o.totalAmount.amount) FROM Order o WHERE o.paymentStatus = 'CAPTURED' AND o.createdAt BETWEEN :startDate AND :endDate")
    Double getTotalRevenueBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /**
     * Count orders by customer and status
     */
    long countByCustomerIdAndStatus(UUID customerId, OrderStatus status);

    /**
     * Get average order value for customer
     */
    @Query("SELECT AVG(o.totalAmount.amount) FROM Order o WHERE o.customerId = :customerId AND o.paymentStatus = 'CAPTURED'")
    Double getAverageOrderValueForCustomer(@Param("customerId") UUID customerId);

    // ====================================
    // Search Queries
    // ====================================

    /**
     * Search orders by customer name
     */
    @Query("SELECT o FROM Order o WHERE LOWER(o.customerFirstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(o.customerLastName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Order> searchByCustomerName(@Param("searchTerm") String searchTerm);

    /**
     * Search orders by customer email
     */
    List<Order> findByCustomerEmailContainingIgnoreCase(String email);

    // ====================================
    // Bulk Operations
    // ====================================

    /**
     * Find orders by IDs
     */
    @Query("SELECT o FROM Order o WHERE o.id IN :ids")
    List<Order> findByIds(@Param("ids") List<UUID> ids);

    /**
     * Delete orders by customer ID (for testing/admin purposes)
     */
    void deleteByCustomerId(UUID customerId);
}