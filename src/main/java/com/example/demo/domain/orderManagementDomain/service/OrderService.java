package com.example.demo.domain.orderManagementDomain.service;

import com.example.demo.domain.orderManagementDomain.distanceCalculator.GovernorateDistance;
import com.example.demo.domain.orderManagementDomain.dto.OrderDto;
import com.example.demo.domain.orderManagementDomain.dto.OrderItemDto;
import com.example.demo.domain.orderManagementDomain.entity.*;
import com.example.demo.orderManagementDomain.entity.*;
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
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    @Autowired
    private final OrderRepository orderRepository;
    @Autowired
    private final ProductRepository productRepository;
    @Autowired
    private final InventoryService inventoryService;
    @Autowired
    private final AddressRepository addressRepository;
    @Autowired
    private final UserService userService;

    @Transactional(readOnly = true)
    public Page<OrderDto> getOrders(Pageable pageable) {
        return orderRepository.findAll(pageable).map(this::mapToDto);
    }

    @Transactional
    public OrderDto placeOrder(PlaceOrderRequest request) {
        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        // Validate & reserve stock
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

        Address shippingAddress = addressRepository.findById(request.getShippingAddressId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid address"));

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

        for (OrderItem item : orderItems) {
            item.setOrder(order);
            order.getItems().add(item);
        }

        return mapToDto(orderRepository.save(order));
    }

    @Transactional
    public OrderDto cancelOrder(Long orderId) {
        Order order = getOrder(orderId);
        if (order.getStatus() != OrderStatus.NEW && order.getStatus() != OrderStatus.PROCESSING) {
            throw new InvalidOrderStateException("Cannot cancel an order that is not new or processing");
        }

        for (OrderItem item : order.getItems()) {
            inventoryService.releaseReservedStock(item.getProduct().getId(), item.getQuantity());
        }

        order.setStatus(OrderStatus.CANCELLED);
        return mapToDto(orderRepository.save(order));
    }

    @Transactional
    public OrderDto startProcessing(Long orderId) {
        Order order = getOrder(orderId);
        if (order.getStatus() != OrderStatus.NEW) {
            throw new InvalidOrderStateException("Only new orders can be moved to processing");
        }
        order.setStatus(OrderStatus.PROCESSING);
        return mapToDto(orderRepository.save(order));
    }

    @Transactional
    public OrderDto shipOrder(Long orderId) {
        Order order = getOrder(orderId);
        if (order.getStatus() != OrderStatus.PROCESSING) {
            throw new InvalidOrderStateException("Cannot ship an order that is not processing");
        }

        for (OrderItem item : order.getItems()) {
            inventoryService.deductStock(item.getProduct().getId(), item.getQuantity());
        }

        order.setStatus(OrderStatus.SHIPPED);
        return mapToDto(orderRepository.save(order));
    }

    @Transactional
    public OrderDto deliverOrder(Long orderId) {
        Order order = getOrder(orderId);
        if (order.getStatus() != OrderStatus.SHIPPED) {
            throw new InvalidOrderStateException("Cannot deliver an order that has not been shipped");
        }

        order.setStatus(OrderStatus.DELIVERED);
        return mapToDto(orderRepository.save(order));
    }

    @Transactional
    public OrderDto returnOrder(Long orderId) {
        Order order = getOrder(orderId);
        if (order.getStatus() != OrderStatus.DELIVERED) {
            throw new InvalidOrderStateException("Cannot return an order that is not delivered");
        }

        order.setStatus(OrderStatus.RETURNED);
        return mapToDto(orderRepository.save(order));
    }

    public Order getOrder(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public List<OrderDto> getOrdersByStatus(OrderStatus status) {
        return orderRepository.findByStatus(status).stream()
                .map(this::mapToDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrderDto getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .map(this::mapToDto)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
    }

    private int calculateShippingCost(String destination) {
        int baseCost = 50;
        int distance = GovernorateDistance.getDistance(destination);
        return baseCost + distance; // 1 DT per km
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
                .toList();

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
}
