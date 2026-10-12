package com.example.demo.modules.order.application.mapper;

import com.example.demo.common.mapper.BaseMapper;
import com.example.demo.modules.order.application.dto.request.AddressRequest;
import com.example.demo.modules.order.application.dto.request.CreateOrderRequest;
import com.example.demo.modules.order.application.dto.request.OrderItemRequest;
import com.example.demo.modules.order.application.dto.request.UpdateOrderRequest;
import com.example.demo.modules.order.application.dto.response.AddressResponse;
import com.example.demo.modules.order.application.dto.response.OrderResponse;
import com.example.demo.modules.order.application.dto.response.OrderSummaryResponse;
import com.example.demo.modules.order.domain.model.Order;
import com.example.demo.modules.order.domain.model.OrderItem;
import com.example.demo.modules.order.domain.model.vo.Address;
import com.example.demo.modules.order.domain.model.vo.Money;
import com.example.demo.modules.order.domain.model.vo.OrderNumber;
import org.mapstruct.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * MapStruct mapper for Order entity <-> DTOs
 * 
 * Performance optimized - focuses on essential mappings only
 *
 * @author Order Team
 * @version 1.0
 */
@Mapper(
    componentModel = "spring",
    uses = {OrderItemMapper.class},
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
    injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
public abstract class OrderMapper implements BaseMapper<Order, OrderResponse> {

    // ====================================
    // Entity to Response - FULL
    // ====================================

    @Override
    @Mapping(target = "orderNumber", expression = "java(extractOrderNumber(order.getOrderNumber()))")
    @Mapping(target = "customerId", source = "customerId")
    @Mapping(target = "customerEmail", source = "customerEmail")
    @Mapping(target = "customerFirstName", source = "customerFirstName")
    @Mapping(target = "customerLastName", source = "customerLastName")
    @Mapping(target = "customerFullName", expression = "java(extractCustomerFullName(order))")
    @Mapping(target = "customerPhone", source = "customerPhone")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "paymentStatus", source = "paymentStatus")
    @Mapping(target = "shippingStatus", source = "shippingStatus")
    @Mapping(target = "shippingAddress", expression = "java(mapAddress(order.getShippingAddress()))")
    @Mapping(target = "billingAddress", expression = "java(mapAddress(order.getBillingAddress()))")
    @Mapping(target = "items", source = "items")
    @Mapping(target = "itemCount", expression = "java(extractItemCount(order.getItems()))")
    @Mapping(target = "totalQuantity", expression = "java(extractTotalQuantity(order))")
    @Mapping(target = "subtotalAmount", expression = "java(extractAmount(order.getSubtotal()))")
    @Mapping(target = "taxAmount", expression = "java(extractAmount(order.getTaxAmount()))")
    @Mapping(target = "shippingCost", expression = "java(extractAmount(order.getShippingCost()))")
    @Mapping(target = "discountAmount", expression = "java(extractAmount(order.getDiscountAmount()))")
    @Mapping(target = "totalAmount", expression = "java(extractAmount(order.getTotalAmount()))")
    @Mapping(target = "currency", expression = "java(extractCurrency(order.getTotalAmount()))")
    @Mapping(target = "paymentMethod", source = "paymentMethod")
    @Mapping(target = "paymentTransactionId", source = "paymentTransactionId")
    @Mapping(target = "shippingMethod", source = "shippingMethod")
    @Mapping(target = "trackingNumber", source = "trackingNumber")
    @Mapping(target = "carrier", source = "carrier")
    @Mapping(target = "customerNotes", source = "customerNotes")
    @Mapping(target = "internalNotes", source = "internalNotes")
    @Mapping(target = "discountCode", source = "discountCode")
    @Mapping(target = "ipAddress", source = "ipAddress")
    @Mapping(target = "userAgent", source = "userAgent")
    @Mapping(target = "confirmedAt", source = "confirmedAt")
    @Mapping(target = "paymentAuthorizedAt", source = "paymentAuthorizedAt")
    @Mapping(target = "paymentCapturedAt", source = "paymentCapturedAt")
    @Mapping(target = "shippedAt", source = "shippedAt")
    @Mapping(target = "estimatedDeliveryDate", source = "estimatedDeliveryDate")
    @Mapping(target = "deliveredAt", source = "deliveredAt")
    @Mapping(target = "cancelledAt", source = "cancelledAt")
    @Mapping(target = "cancellationReason", source = "cancellationReason")
    @Mapping(target = "refundedAt", source = "refundedAt")
    @Mapping(target = "refundReason", source = "refundReason")
    public abstract OrderResponse toDto(Order order);

    /**
     * Alias for better readability
     */
    public OrderResponse toResponse(Order order) {
        return order != null ? toDto(order) : null;
    }

    /**
     * Map list of orders
     */
    public List<OrderResponse> toResponseList(List<Order> orders) {
        return toDtoList(orders);
    }

    // ====================================
    // Entity to Summary Response - LEAN
    // ====================================

    @Mapping(target = "orderNumber", expression = "java(extractOrderNumber(order.getOrderNumber()))")
    @Mapping(target = "customerId", source = "customerId")
    @Mapping(target = "customerFullName", expression = "java(extractCustomerFullName(order))")
    @Mapping(target = "customerEmail", source = "customerEmail")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "paymentStatus", source = "paymentStatus")
    @Mapping(target = "itemCount", expression = "java(extractItemCount(order.getItems()))")
    @Mapping(target = "totalAmount", expression = "java(extractAmount(order.getTotalAmount()))")
    @Mapping(target = "currency", expression = "java(extractCurrency(order.getTotalAmount()))")
    @Mapping(target = "confirmedAt", source = "confirmedAt")
    @Mapping(target = "deliveredAt", source = "deliveredAt")
    public abstract OrderSummaryResponse toSummaryResponse(Order order);

    /**
     * Map to summary list
     */
    public List<OrderSummaryResponse> toSummaryList(List<Order> orders) {
        if (orders == null || orders.isEmpty()) {
            return Collections.emptyList();
        }

        return orders.stream()
                .map(this::toSummaryResponse)
                .collect(Collectors.toList());
    }

    // ====================================
    // Request to Entity - CREATE
    // ====================================

    /**
     * Map CreateOrderRequest to Order entity
     */
    public Order fromCreateRequest(CreateOrderRequest request) {
        if (request == null) {
            return null;
        }

        return Order.create(
                request.getCustomerId(),
                request.getCustomerEmail(),
                request.getCustomerFirstName(),
                request.getCustomerLastName(),
                createAddress(request.getShippingAddress()),
                createAddress(request.getBillingAddress()),
                request.getCurrency()
        );
    }

    @Override
    public Order toEntity(OrderResponse dto) {
        throw new UnsupportedOperationException(
            "Cannot create entity from response DTO. Use fromCreateRequest() instead."
        );
    }

    // ====================================
    // Request to Entity - UPDATE
    // ====================================

    /**
     * Update Order entity from UpdateOrderRequest
     */
    public void updateEntityFromRequest(UpdateOrderRequest request, Order order) {
        if (request == null || order == null) {
            return;
        }

        if (request.getShippingAddress() != null) {
            order.updateShippingAddress(createAddress(request.getShippingAddress()));
        }

        if (request.getBillingAddress() != null) {
            order.setBillingAddress(createAddress(request.getBillingAddress()));
        }

        if (request.getCustomerNotes() != null) {
            order.updateCustomerNotes(request.getCustomerNotes());
        }

        if (request.getInternalNotes() != null) {
            order.updateInternalNotes(request.getInternalNotes());
        }
    }

    // ====================================
    // Helper Methods
    // ====================================

    /**
     * Extract order number value
     */
    protected String extractOrderNumber(OrderNumber orderNumber) {
        return orderNumber != null ? orderNumber.getValue() : null;
    }

    /**
     * Extract customer full name
     */
    protected String extractCustomerFullName(Order order) {
        if (order == null) {
            return null;
        }
        return order.getCustomerFullName();
    }

    /**
     * Extract item count
     */
    protected Integer extractItemCount(Set<OrderItem> items) {
        return items != null ? items.size() : 0;
    }

    /**
     * Extract total quantity
     */
    protected Integer extractTotalQuantity(Order order) {
        if (order == null) {
            return 0;
        }
        return order.getTotalItemCount();
    }

    /**
     * Extract money amount
     */
    protected BigDecimal extractAmount(Money money) {
        return money != null ? money.getAmount() : null;
    }

    /**
     * Extract currency
     */
    protected String extractCurrency(Money money) {
        return money != null ? money.getCurrency() : null;
    }

    /**
     * Create Address from request
     */
    protected Address createAddress(AddressRequest request) {
        if (request == null) {
            return null;
        }

        return Address.builder()
                .streetAddress(request.getStreetAddress())
                .streetAddress2(request.getStreetAddress2())
                .city(request.getCity())
                .stateProvince(request.getStateProvince())
                .postalCode(request.getPostalCode())
                .country(request.getCountry())
                .phone(request.getPhone())
                .build();
    }

    /**
     * Map Address to AddressResponse
     */
    protected AddressResponse mapAddress(Address address) {
        if (address == null) {
            return null;
        }

        return AddressResponse.builder()
                .streetAddress(address.getStreetAddress())
                .streetAddress2(address.getStreetAddress2())
                .city(address.getCity())
                .stateProvince(address.getStateProvince())
                .postalCode(address.getPostalCode())
                .country(address.getCountry())
                .phone(address.getPhone())
                .fullAddress(address.getFullAddress())
                .build();
    }

    // ====================================
    // Helper for Money creation (used in OrderCommandService)
    // ====================================

    /**
     * Create Money from OrderItemRequest
     */
    public Money toUnitPriceMoney(OrderItemRequest request, String currency) {
        if (request == null || request.getUnitPrice() == null) {
            return null;
        }
        return Money.of(request.getUnitPrice(), currency);
    }

    /**
     * Create Money for discount
     */
    public Money toDiscountMoney(BigDecimal discountAmount, String currency) {
        if (discountAmount == null) {
            return Money.zero(currency);
        }
        return Money.of(discountAmount, currency);
    }

    // ====================================
    // Specialized Mappings
    // ====================================

    /**
     * Map to sorted list by creation date (newest first)
     */
    public List<OrderSummaryResponse> toSortedListByDate(List<Order> orders) {
        if (orders == null || orders.isEmpty()) {
            return Collections.emptyList();
        }

        return orders.stream()
                .map(this::toSummaryResponse)
                .sorted(Comparator.comparing(OrderSummaryResponse::getCreatedAt,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());
    }

    /**
     * Filter paid orders
     */
    public List<OrderSummaryResponse> toPaidOrders(List<Order> orders) {
        if (orders == null || orders.isEmpty()) {
            return Collections.emptyList();
        }

        return orders.stream()
                .filter(Order::isPaid)
                .map(this::toSummaryResponse)
                .collect(Collectors.toList());
    }

    /**
     * Filter shipped orders
     */
    public List<OrderSummaryResponse> toShippedOrders(List<Order> orders) {
        if (orders == null || orders.isEmpty()) {
            return Collections.emptyList();
        }

        return orders.stream()
                .filter(Order::isShipped)
                .map(this::toSummaryResponse)
                .collect(Collectors.toList());
    }

    /**
     * Filter delivered orders
     */
    public List<OrderSummaryResponse> toDeliveredOrders(List<Order> orders) {
        if (orders == null || orders.isEmpty()) {
            return Collections.emptyList();
        }

        return orders.stream()
                .filter(Order::isDelivered)
                .map(this::toSummaryResponse)
                .collect(Collectors.toList());
    }

    // ====================================
    // Minimal Summary for Dropdowns
    // ====================================

    /**
     * Create minimal order summary for references
     */
    public OrderMinimal toMinimal(Order order) {
        if (order == null) {
            return null;
        }

        return OrderMinimal.builder()
                .id(order.getId())
                .orderNumber(extractOrderNumber(order.getOrderNumber()))
                .customerFullName(extractCustomerFullName(order))
                .status(order.getStatus().name())
                .totalAmount(extractAmount(order.getTotalAmount()))
                .currency(extractCurrency(order.getTotalAmount()))
                .createdAt(order.getCreatedAt())
                .build();
    }

    /**
     * Map to minimal list
     */
    public List<OrderMinimal> toMinimalList(List<Order> orders) {
        if (orders == null || orders.isEmpty()) {
            return Collections.emptyList();
        }

        return orders.stream()
                .map(this::toMinimal)
                .collect(Collectors.toList());
    }

    // ====================================
    // Nested DTOs
    // ====================================

    /**
     * Minimal order summary for dropdowns, references
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class OrderMinimal {
        private java.util.UUID id;
        private String orderNumber;
        private String customerFullName;
        private String status;
        private java.math.BigDecimal totalAmount;
        private String currency;
        private java.time.LocalDateTime createdAt;

        public String getFormattedTotal() {
            if (totalAmount == null || currency == null) {
                return null;
            }
            return currency + " " + totalAmount;
        }

        public String getDisplayName() {
            return orderNumber + " - " + customerFullName;
        }
    }
}