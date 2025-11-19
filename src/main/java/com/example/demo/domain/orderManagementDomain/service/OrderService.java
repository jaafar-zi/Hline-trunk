package com.example.demo.domain.orderManagementDomain.service;

import com.example.demo.common.base.service.AbstractBaseService;
import com.example.demo.domain.orderManagementDomain.distanceCalculator.GovernorateDistance;
import com.example.demo.domain.orderManagementDomain.dto.OrderDto;
import com.example.demo.domain.orderManagementDomain.dto.OrderItemDto;
import com.example.demo.domain.orderManagementDomain.entity.*;
import com.example.demo.domain.orderManagementDomain.exception.InsufficientStockException;
import com.example.demo.domain.orderManagementDomain.exception.InvalidOrderStateException;
import com.example.demo.domain.orderManagementDomain.exception.OrderNotFoundException;
import com.example.demo.domain.orderManagementDomain.repository.OrderRepository;
import com.example.demo.domain.productManagementDomain.entity.Pricing;
import com.example.demo.domain.productManagementDomain.entity.Product;
import com.example.demo.domain.productManagementDomain.repository.ProductRepository;
import com.example.demo.domain.productManagementDomain.service.InventoryService;
import com.example.demo.domain.userManagementDomain.address.entity.Address;
import com.example.demo.domain.userManagementDomain.address.repository.AddressRepository;
import com.example.demo.domain.userManagementDomain.user.entity.User;
import com.example.demo.domain.userManagementDomain.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class OrderService extends AbstractBaseService<Order, Long> {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final InventoryService inventoryService;
    private final AddressRepository addressRepository;
    private final UserService userService;

    public OrderService(OrderRepository orderRepository,
                        ProductRepository productRepository,
                        InventoryService inventoryService,
                        AddressRepository addressRepository,
                        UserService userService) {
        super(orderRepository);
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.inventoryService = inventoryService;
        this.addressRepository = addressRepository;
        this.userService = userService;
    }

    @Override
    protected String getCacheName() {
        return "orders";
    }

    @Override
    protected Class<?> getEntityClass() {
        return Order.class;
    }

    // ───────────────────────────────────────────────
    //  READ METHODS
    // ───────────────────────────────────────────────

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "orders", key = "'page:' + #pageable.pageNumber")
    public Page<OrderDto> getOrders(Pageable pageable) {
        return orderRepository.findAll(pageable).map(this::mapToDto);
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "orders", key = "'id:' + #orderId")
    public OrderDto getOrderById(Long orderId) {
        return findById(orderId)
                .map(this::mapToDto)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "orders", key = "'status:' + #status")
    public List<OrderDto> getOrdersByStatus(OrderStatus status) {
        return orderRepository.findByStatus(status).stream()
                .map(this::mapToDto)
                .toList();
    }

    // ───────────────────────────────────────────────
    //  BUSINESS OPERATIONS
    // ───────────────────────────────────────────────

    @Transactional
    public OrderDto placeOrder(PlaceOrderRequest request) {
        log.info("Placing order for {} items", request.getItems().size());

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        // 1️⃣ Validate and reserve stock
        for (ItemRequest itemReq : request.getItems()) {
            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new OrderNotFoundException(itemReq.getProductId()));

            if (!inventoryService.isStockAvailable(product.getId(), itemReq.getQuantity())) {
                throw new InsufficientStockException(product.getId());
            }

            inventoryService.reserveStock(product.getId(), itemReq.getQuantity());

            BigDecimal unitPrice = product.getPricingList().stream()
                    .findFirst()
                    .map(Pricing::getPrice)
                    .map(BigDecimal::valueOf)
                    .orElseThrow(() -> new RuntimeException("No pricing available"));

            OrderItem item = OrderItem.builder()
                    .product(product)
                    .quantity(itemReq.getQuantity())
                    .unitPrice(unitPrice)
                    .build();

            orderItems.add(item);
            totalAmount = totalAmount.add(unitPrice.multiply(BigDecimal.valueOf(itemReq.getQuantity())));
        }

        // 2️⃣ Build order
        Address shippingAddress = addressRepository.findById(request.getShippingAddressId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid shipping address"));

        int shippingCost = calculateShippingCost(shippingAddress.getCity());
        User currentUser = userService.getCurrentUser();

        Order order = Order.builder()
                .status(OrderStatus.NEW)
                .shippingAddress(shippingAddress)
                .user(currentUser)
                .paymentMethod(request.getPaymentMethod())
                .customerNote(request.getCustomerNote())
                .totalAmount(totalAmount)
                .shippingCost(BigDecimal.valueOf(shippingCost))
                .items(new ArrayList<>())
                .build();

        orderItems.forEach(item -> {
            item.setOrder(order);
            order.getItems().add(item);
        });

        return mapToDto(save(order));
    }

    @Transactional
    public OrderDto cancelOrder(Long orderId) {
        Order order = findByIdOrThrow(orderId);

        if (order.getStatus() != OrderStatus.NEW && order.getStatus() != OrderStatus.PROCESSING) {
            throw new InvalidOrderStateException("Cannot cancel an order that is not new or processing");
        }

        order.getItems().forEach(item ->
                inventoryService.releaseReservedStock(item.getProduct().getId(), item.getQuantity())
        );

        order.setStatus(OrderStatus.CANCELLED);
        return mapToDto(save(order));
    }

    @Transactional
    public OrderDto startProcessing(Long orderId) {
        Order order = findByIdOrThrow(orderId);
        if (order.getStatus() != OrderStatus.NEW) {
            throw new InvalidOrderStateException("Only new orders can be moved to processing");
        }
        order.setStatus(OrderStatus.PROCESSING);
        return mapToDto(save(order));
    }

    @Transactional
    public OrderDto shipOrder(Long orderId) {
        Order order = findByIdOrThrow(orderId);
        if (order.getStatus() != OrderStatus.PROCESSING) {
            throw new InvalidOrderStateException("Cannot ship an order that is not processing");
        }

        order.getItems().forEach(item ->
                inventoryService.deductStock(item.getProduct().getId(), item.getQuantity())
        );

        order.setStatus(OrderStatus.SHIPPED);
        return mapToDto(save(order));
    }

    @Transactional
    public OrderDto deliverOrder(Long orderId) {
        Order order = findByIdOrThrow(orderId);
        if (order.getStatus() != OrderStatus.SHIPPED) {
            throw new InvalidOrderStateException("Cannot deliver an order that has not been shipped");
        }
        order.setStatus(OrderStatus.DELIVERED);
        return mapToDto(save(order));
    }

    @Transactional
    public OrderDto returnOrder(Long orderId) {
        Order order = findByIdOrThrow(orderId);
        if (order.getStatus() != OrderStatus.DELIVERED) {
            throw new InvalidOrderStateException("Cannot return an order that is not delivered");
        }
        order.setStatus(OrderStatus.RETURNED);
        return mapToDto(save(order));
    }

    // ───────────────────────────────────────────────
    //  UTILS
    // ───────────────────────────────────────────────

    private int calculateShippingCost(String destination) {
        int baseCost = 50;
        int distance = GovernorateDistance.getDistance(destination);
        return baseCost + distance;
    }

    private OrderDto mapToDto(Order order) {
        List<OrderItemDto> itemDtos = order.getItems().stream()
                .map(item -> new OrderItemDto(
                        item.getId(),
                        item.getProduct().getId(),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        item.getTotalPrice()
                ))
                .collect(Collectors.toList());

        return new OrderDto(
                order.getId(),
                order.getOrderDate(),
                order.getStatus(),
                order.getTrackingNumber(),
                order.getPaymentMethod(),
                order.getTotalAmount(),
                order.getShippingCost(),
                order.getCustomerNote(),
                order.getUser().getFirstname() + " " + order.getUser().getLastname(),
                order.getUser().getEmail(),
                order.getShippingAddress().getId(),
                itemDtos
        );
    }

    @Override
    protected void onAfterSave(Order entity) {
        log.debug("Order {} saved with status {}", entity.getId(), entity.getStatus());
    }

    @Override
    protected void onAfterDelete(Long id) {
        log.debug("Order {} deleted", id);
    }
}
