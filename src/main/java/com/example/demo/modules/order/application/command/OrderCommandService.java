package com.example.demo.modules.order.application.command;

import com.example.demo.common.base.service.command.AbstractCommandService;
import com.example.demo.common.exception.EntityNotFoundException;
import com.example.demo.common.normalization.NormalizationService;
import com.example.demo.common.validation.result.ValidationResult;
import com.example.demo.modules.order.application.dto.request.*;
import com.example.demo.modules.order.application.mapper.OrderItemMapper;
import com.example.demo.modules.order.application.mapper.OrderMapper;
import com.example.demo.modules.order.domain.model.Order;
import com.example.demo.modules.order.domain.model.OrderItem;
import com.example.demo.modules.order.domain.model.vo.Money;
import com.example.demo.modules.order.domain.repository.OrderRepository;
import com.example.demo.modules.product.domain.model.Product;
import com.example.demo.modules.product.domain.repository.ProductRepository;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Order Command Service
 * 
 * Handles all write operations for orders
 * Extends AbstractCommandService for standard CRUD + domain-specific operations
 * 
 * @author Order Team
 * @version 1.0
 */
@Slf4j
@Service
@Transactional
public class OrderCommandService extends AbstractCommandService<Order, UUID> {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;

    public OrderCommandService(
            OrderRepository orderRepository,
            ProductRepository productRepository,
            OrderMapper orderMapper,
            OrderItemMapper orderItemMapper,
            CacheManager cacheManager,
            ApplicationEventPublisher eventPublisher,
            Validator validator,
            NormalizationService normalizationService) {
        super(orderRepository, cacheManager, eventPublisher, validator, normalizationService);
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
    }

    // ====================================
    // AbstractCommandService Implementation
    // ====================================

    @Override
    protected List<String> getCacheNames() {
        return List.of("orders", "order-summaries");
    }

    @Override
    protected Class<Order> getEntityClass() {
        return Order.class;
    }

    // ====================================
    // Order Creation
    // ====================================

    /**
     * Create new order
     */
    public Order createOrder(CreateOrderRequest request) {
        log.info("Creating new order for customer: {}", request.getCustomerId());

        return createFromRequest(request, req -> {
            // 1. Create order entity from request
            Order order = orderMapper.fromCreateRequest(req);
            
            // 2. Set additional optional fields
            setOptionalFields(order, req);

            // 3. Add items to order
            addItemsToOrder(order, req);

            // 4. Recalculate totals
            order.recalculateTotals();

            log.info("Order created with {} items, total: {}", 
                    order.getItems().size(), order.getTotalAmount());
            
            return order;
        });
    }

    /**
     * Set optional fields on order
     */
    private void setOptionalFields(Order order, CreateOrderRequest request) {
        if (request.getCustomerPhone() != null) {
            order.setCustomerPhone(request.getCustomerPhone());
        }
        if (request.getCustomerNotes() != null) {
            order.setCustomerNotes(request.getCustomerNotes());
        }
        if (request.getDiscountCode() != null) {
            order.setDiscountCode(request.getDiscountCode());
        }
        if (request.getIpAddress() != null) {
            order.setIpAddress(request.getIpAddress());
        }
        if (request.getUserAgent() != null) {
            order.setUserAgent(request.getUserAgent());
        }
    }

    /**
     * Add all items to order
     */
    private void addItemsToOrder(Order order, CreateOrderRequest request) {
        for (OrderItemRequest itemRequest : request.getItems()) {
            addItemToOrder(order, itemRequest, request.getCurrency());
        }
    }

    /**
     * Add single item to order
     */
    private void addItemToOrder(Order order, OrderItemRequest itemRequest, String currency) {
        // 1. Fetch product for validation and data
        Product product = productRepository.findById(itemRequest.getProductId())
                .orElseThrow(() -> new EntityNotFoundException(Product.class, itemRequest.getProductId()));

        // 2. Create Money VO for unit price
        Money unitPrice = orderItemMapper.toUnitPriceMoney(itemRequest, currency);

        // 3. Add item to order
        OrderItem item = order.addItem(
                product.getId(),
                product.getSku().getValue(),
                product.getName(),
                unitPrice,
                itemRequest.getQuantity()
        );

        // 4. Set optional item fields
        setOptionalItemFields(item, itemRequest, currency);

        // 5. Recalculate item totals
        item.calculateTotals();
    }

    /**
     * Set optional fields on order item
     */
    private void setOptionalItemFields(OrderItem item, OrderItemRequest itemRequest, String currency) {
        if (itemRequest.getProductVariantId() != null) {
            item.setProductVariantId(itemRequest.getProductVariantId());
            // TODO: Fetch variant SKU if needed
        }

        if (itemRequest.getTaxRate() != null) {
            item.setTaxRate(itemRequest.getTaxRate());
        }

        if (itemRequest.getDiscountAmount() != null) {
            Money discount = orderItemMapper.toDiscountMoney(itemRequest.getDiscountAmount(), currency);
            item.applyDiscount(discount);
        }

        if (itemRequest.getNotes() != null) {
            item.setNotes(itemRequest.getNotes());
        }
    }

    // ====================================
    // Order Updates
    // ====================================

    /**
     * Update order details
     */
    public Order updateOrder(UUID orderId, UpdateOrderRequest request) {
    log.info("Updating order: {}", orderId);
    
    // Use the same pattern as createOrder
    Order entity = buildOrderFromUpdateRequest(orderId, request);
    
    return update(orderId, entity);
}

/**
 * Build order entity from update request
 * Same pattern as create - validates, maps, and sets relationships
 */
private Order buildOrderFromUpdateRequest(UUID orderId, UpdateOrderRequest request) {
    // 1. Pre-process
    request.preProcess();
    
    // 2. Normalize
    request.normalize();
    
    // 3. Validate request
    ValidationResult validationResult = validateRequest(request);
    validationResult.throwIfInvalid();
    
    // 4. Find existing entity
    Order existing = orderRepository.findById(orderId)
            .orElseThrow(() -> new EntityNotFoundException(Order.class, orderId));
    
    // 5. Map request to existing entity (updates in place)
    orderMapper.updateEntityFromRequest(request, existing);
    
    // 6. Domain validation (if you have order-specific validators)
    // ValidationResult orderValidation = orderValidator.validateOrder(existing);
    // orderValidation.throwIfInvalid();
    
    // 7. Post-process
    request.postProcess();
    
    return existing;
}

    // ====================================
    // Order Lifecycle Operations
    // ====================================

    /**
     * Confirm order
     */
    public Order confirmOrder(UUID orderId) {
        log.info("Confirming order: {}", orderId);

        Order order = findOrderOrThrow(orderId);
        order.confirm();

        Order confirmed = repository.save(order);
        publishDomainEvents(confirmed);
        evictAllCaches();

        log.info("Order confirmed: {}", orderId);
        return confirmed;
    }

    /**
     * Process payment for order
     */
    public Order processPayment(UUID orderId, ProcessPaymentRequest request) {
        log.info("Processing payment for order: {}", orderId);

        // Pre-process and validate request
        request.preProcess();
        request.normalize();
        validateRequest(request).throwIfInvalid();

        // Process payment
        Order order = findOrderOrThrow(orderId);
        order.markAsPaid(request.getTransactionId(), request.getPaymentMethod());

        // Save and publish events
        Order paid = repository.save(order);
        publishDomainEvents(paid);
        evictAllCaches();

        request.postProcess();
        log.info("Payment processed for order: {}", orderId);
        return paid;
    }

    /**
     * Ship order
     */
    public Order shipOrder(UUID orderId, ShipOrderRequest request) {
        log.info("Shipping order: {}", orderId);

        // Pre-process and validate request
        request.preProcess();
        request.normalize();
        validateRequest(request).throwIfInvalid();

        // Ship order
        Order order = findOrderOrThrow(orderId);
        order.ship(
                request.getTrackingNumber(),
                request.getCarrier(),
                request.getEstimatedDeliveryDate()
        );

        // Save and publish events
        Order shipped = repository.save(order);
        publishDomainEvents(shipped);
        evictAllCaches();

        request.postProcess();
        log.info("Order shipped: {}", orderId);
        return shipped;
    }

    /**
     * Mark order as delivered
     */
    public Order markAsDelivered(UUID orderId) {
        log.info("Marking order as delivered: {}", orderId);

        Order order = findOrderOrThrow(orderId);
        order.markAsDelivered();

        Order delivered = repository.save(order);
        publishDomainEvents(delivered);
        evictAllCaches();

        log.info("Order delivered: {}", orderId);
        return delivered;
    }

    /**
     * Cancel order
     */
    public Order cancelOrder(UUID orderId, CancelOrderRequest request) {
        log.info("Cancelling order: {}", orderId);

        // Pre-process and validate request
        request.preProcess();
        request.normalize();
        validateRequest(request).throwIfInvalid();

        // Cancel order
        Order order = findOrderOrThrow(orderId);
        order.cancel(request.getReason());

        // Save and publish events
        Order cancelled = repository.save(order);
        publishDomainEvents(cancelled);
        evictAllCaches();

        request.postProcess();
        log.info("Order cancelled: {}", orderId);
        return cancelled;
    }

    /**
     * Refund order
     */
    public Order refundOrder(UUID orderId, RefundOrderRequest request) {
        log.info("Refunding order: {}", orderId);

        // Pre-process and validate request
        request.preProcess();
        request.normalize();
        validateRequest(request).throwIfInvalid();

        // Refund order
        Order order = findOrderOrThrow(orderId);
        order.refund(request.getReason());

        // Save and publish events
        Order refunded = repository.save(order);
        publishDomainEvents(refunded);
        evictAllCaches();

        request.postProcess();
        log.info("Order refunded: {}", orderId);
        return refunded;
    }

    // ====================================
    // Lifecycle Hooks
    // ====================================

    @Override
    protected void beforeCreate(Order entity) {
        log.debug("Preparing to create order for customer: {}", entity.getCustomerId());
        
        // Validate order has items
        if (!entity.hasItems()) {
            throw new IllegalStateException("Order must have at least one item");
        }

        // Validate order number uniqueness
        if (orderRepository.existsByOrderNumber(entity.getOrderNumber())) {
            throw new IllegalStateException("Order number already exists: " + entity.getOrderNumber());
        }
    }

    @Override
    protected void afterCreate(Order entity) {
        log.info("Order created: {} for customer: {} with total: {}", 
                entity.getOrderNumber(), entity.getCustomerId(), entity.getTotalAmount());
    }

    @Override
    protected void beforeUpdate(Order existing, Order updated) {
        log.debug("Preparing to update order: {}", existing.getOrderNumber());
    }

    @Override
    protected void afterUpdate(Order entity) {
        log.debug("Order updated: {}", entity.getOrderNumber());
    }

    @Override
    protected void beforeDelete(Order entity) {
        log.debug("Preparing to delete order: {}", entity.getOrderNumber());
        
        // Warn if deleting non-cancelled order
        if (entity.getStatus().isActive()) {
            log.warn("Deleting active order: {} (status: {})", 
                    entity.getOrderNumber(), entity.getStatus());
        }
    }

    @Override
    protected void afterDelete(Order entity) {
        log.info("Order deleted: {} for customer: {}", 
                entity.getOrderNumber(), entity.getCustomerId());
    }

    // ====================================
    // Helper Methods
    // ====================================

    private Order findOrderOrThrow(UUID id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Order.class, id));
    }
}