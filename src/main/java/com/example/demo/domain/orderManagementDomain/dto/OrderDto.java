package com.example.demo.domain.orderManagementDomain.dto;

import com.example.demo.domain.orderManagementDomain.entity.OrderStatus;
import com.example.demo.domain.orderManagementDomain.entity.PaymentMethod;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@SuperBuilder
@Getter
@Setter
@ToString
@EqualsAndHashCode
@AllArgsConstructor
public class OrderDto {
    private Long orderId;
    private LocalDateTime orderDate;
    private OrderStatus status;
    private String trackingNumber;
    private PaymentMethod paymentMethod;

    private BigDecimal totalAmount;
    private BigDecimal shippingCost;
    private String customerNote;

    private String customerName;
    private String customerEmail;

    private Long shippingAddressId;

    private List<OrderItemDto> items;
}

