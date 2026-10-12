package com.example.demo.modules.order.application.query;

import com.example.demo.common.annotation.ReadOnly;
import com.example.demo.common.base.service.query.AbstractQueryService;
import com.example.demo.common.exception.EntityNotFoundException;
import com.example.demo.common.normalization.NormalizationService;
import com.example.demo.modules.order.application.dto.filter.OrderFilter;
import com.example.demo.modules.order.application.dto.response.OrderResponse;
import com.example.demo.modules.order.application.dto.response.OrderSummaryResponse;
import com.example.demo.modules.order.application.mapper.OrderMapper;
import com.example.demo.modules.order.domain.model.Order;
import com.example.demo.modules.order.domain.model.vo.OrderStatus;
import com.example.demo.modules.order.domain.model.vo.PaymentStatus;
import com.example.demo.modules.order.domain.repository.OrderRepository;
import com.example.demo.modules.order.domain.specification.OrderSpecification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Order Query Service
 * 
 * Handles all read operations for orders
 * Extends AbstractQueryService for standard queries + domain-specific queries
 * 
 * @author Order Team
 * @version 1.0
 */
@Slf4j
@Service
@Transactional(readOnly = true)
public class OrderQueryService extends AbstractQueryService<Order, UUID> {

    private static final Set<String> VALID_SORT_FIELDS = Set.of(
        "orderNumber", "createdAt", "confirmedAt", "totalAmount", 
        "status", "paymentStatus", "customerEmail"
    );

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;

    public OrderQueryService(
            OrderRepository orderRepository,
            NormalizationService normalizationService,
            OrderMapper orderMapper) {
        super(orderRepository, normalizationService);
        this.orderRepository = orderRepository;
        this.orderMapper = orderMapper;
    }

    // ====================================
    // AbstractQueryService Implementation
    // ====================================

    @Override
    protected String getEntityCacheName() {
        return "orders";
    }

    @Override
    protected Class<Order> getEntityClass() {
        return Order.class;
    }

    @Override
    protected boolean isValidSortField(String field) {
        return VALID_SORT_FIELDS.contains(field);
    }

    @Override
    protected String getDefaultSortField() {
        return "createdAt";
    }

    // ====================================
    // Filter-Based Queries
    // ====================================

    /**
     * Search orders with filter (paginated)
     */
    @ReadOnly
    public Page<OrderSummaryResponse> searchOrders(OrderFilter filter) {
        log.debug("Searching orders with filter");

        Specification<Order> spec = OrderSpecification.fromFilter(filter);
        Page<Order> orders = findWithFilter(filter, spec);

        return orders.map(orderMapper::toSummaryResponse);
    }

    /**
     * Find all orders with filter (no pagination)
     */
    @ReadOnly
    public List<OrderSummaryResponse> findAllOrders(OrderFilter filter) {
        log.debug("Finding all orders with filter");

        Specification<Order> spec = OrderSpecification.fromFilter(filter);
        List<Order> orders = findAllWithFilter(filter, spec);

        return orderMapper.toSummaryList(orders);
    }

    // ====================================
    // Specific Queries (Cached)
    // ====================================

    /**
     * Get order by ID (full details)
     */
    @ReadOnly
    @Cacheable(value = "orders", key = "'id:' + #id")
    public OrderResponse getOrderById(UUID id) {
        log.debug("Getting order by ID: {}", id);

        Order order = findById(id);
        return orderMapper.toDto(order);
    }

    /**
     * Get order by order number
     */
    @ReadOnly
    @Cacheable(value = "orders", key = "'number:' + #orderNumber")
    public OrderResponse getOrderByNumber(String orderNumber) {
        log.debug("Getting order by number: {}", orderNumber);

        Order order = orderRepository.findByOrderNumberValue(orderNumber)
                .orElseThrow(() -> EntityNotFoundException.byField(
                        Order.class, "orderNumber", orderNumber));

        return orderMapper.toDto(order);
    }

    // ====================================
    // Customer Queries
    // ====================================

    /**
     * Get orders for customer
     */
    @ReadOnly
    public List<OrderSummaryResponse> getOrdersByCustomer(UUID customerId) {
        log.debug("Getting orders for customer: {}", customerId);

        List<Order> orders = orderRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);
        return orderMapper.toSummaryList(orders);
    }

    /**
     * Get orders by customer and status
     */
    @ReadOnly
    public List<OrderSummaryResponse> getOrdersByCustomerAndStatus(UUID customerId, OrderStatus status) {
        log.debug("Getting orders for customer: {} with status: {}", customerId, status);

        List<Order> orders = orderRepository.findByCustomerIdAndStatus(customerId, status);
        return orderMapper.toSummaryList(orders);
    }

    // ====================================
    // Status Queries
    // ====================================

    /**
     * Get orders by status
     */
    @ReadOnly
    public List<OrderSummaryResponse> getOrdersByStatus(OrderStatus status) {
        log.debug("Getting orders with status: {}", status);

        List<Order> orders = orderRepository.findByStatus(status);
        return orderMapper.toSummaryList(orders);
    }

    /**
     * Get orders by payment status
     */
    @ReadOnly
    public List<OrderSummaryResponse> getOrdersByPaymentStatus(PaymentStatus paymentStatus) {
        log.debug("Getting orders with payment status: {}", paymentStatus);

        List<Order> orders = orderRepository.findByPaymentStatus(paymentStatus);
        return orderMapper.toSummaryList(orders);
    }

    // ====================================
    // Operational Queries
    // ====================================

    /**
     * Get pending orders older than date
     */
    @ReadOnly
    public List<OrderSummaryResponse> getPendingOrdersOlderThan(LocalDateTime date) {
        log.debug("Getting pending orders older than: {}", date);

        List<Order> orders = orderRepository.findPendingOrdersOlderThan(date);
        return orderMapper.toSummaryList(orders);
    }

    /**
     * Get confirmed orders awaiting payment
     */
    @ReadOnly
    public List<OrderSummaryResponse> getConfirmedOrdersAwaitingPayment() {
        log.debug("Getting confirmed orders awaiting payment");

        List<Order> orders = orderRepository.findConfirmedOrdersAwaitingPayment();
        return orderMapper.toSummaryList(orders);
    }

    /**
     * Get paid orders awaiting shipment
     */
    @ReadOnly
    public List<OrderSummaryResponse> getPaidOrdersAwaitingShipment() {
        log.debug("Getting paid orders awaiting shipment");

        List<Order> orders = orderRepository.findPaidOrdersAwaitingShipment();
        return orderMapper.toSummaryList(orders);
    }

    /**
     * Get shipped orders not yet delivered
     */
    @ReadOnly
    public List<OrderSummaryResponse> getShippedOrdersNotDelivered() {
        log.debug("Getting shipped orders not yet delivered");

        List<Order> orders = orderRepository.findShippedOrdersNotDelivered();
        return orderMapper.toSummaryList(orders);
    }

    /**
     * Get order by tracking number
     */
    @ReadOnly
    public OrderResponse getOrderByTrackingNumber(String trackingNumber) {
        log.debug("Getting order by tracking number: {}", trackingNumber);

        Order order = orderRepository.findByTrackingNumber(trackingNumber)
                .orElseThrow(() -> EntityNotFoundException.byField(Order.class, "trackingNumber", trackingNumber));

        return orderMapper.toDto(order);
    }

    // ====================================
    // Statistics & Aggregations
    // ====================================

    /**
     * Count orders by customer
     */
    @ReadOnly
    public long countOrdersByCustomer(UUID customerId) {
        log.debug("Counting orders for customer: {}", customerId);
        return orderRepository.countByCustomerId(customerId);
    }

    /**
     * Count orders by status
     */
    @ReadOnly
    public long countOrdersByStatus(OrderStatus status) {
        log.debug("Counting orders with status: {}", status);
        return orderRepository.countByStatus(status);
    }

    /**
     * Get total revenue for customer
     */
    @ReadOnly
    public Double getTotalRevenueForCustomer(UUID customerId) {
        log.debug("Getting total revenue for customer: {}", customerId);
        Double revenue = orderRepository.getTotalRevenueForCustomer(customerId);
        return revenue != null ? revenue : 0.0;
    }

    /**
     * Get average order value for customer
     */
    @ReadOnly
    public Double getAverageOrderValueForCustomer(UUID customerId) {
        log.debug("Getting average order value for customer: {}", customerId);
        Double avg = orderRepository.getAverageOrderValueForCustomer(customerId);
        return avg != null ? avg : 0.0;
    }

    /**
     * Get total revenue for date range
     */
    @ReadOnly
    public Double getTotalRevenueBetween(LocalDateTime startDate, LocalDateTime endDate) {
        log.debug("Getting total revenue between {} and {}", startDate, endDate);
        Double revenue = orderRepository.getTotalRevenueBetween(startDate, endDate);
        return revenue != null ? revenue : 0.0;
    }

    /**
     * Check if order number exists
     */
    @ReadOnly
    public boolean orderNumberExists(String orderNumber) {
        log.debug("Checking if order number exists: {}", orderNumber);
        return orderRepository.existsByOrderNumberValue(orderNumber);
    }
}