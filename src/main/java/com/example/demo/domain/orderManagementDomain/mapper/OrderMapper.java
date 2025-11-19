package com.example.demo.domain.orderManagementDomain.mapper;

import com.example.demo.common.mapper.AbstractBaseMapper;
import com.example.demo.domain.orderManagementDomain.dto.OrderDto;
import com.example.demo.domain.orderManagementDomain.dto.OrderItemDto;
import com.example.demo.domain.orderManagementDomain.entity.Order;
import com.example.demo.domain.orderManagementDomain.entity.OrderItem;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrderMapper extends AbstractBaseMapper<Order, OrderDto> {

    @Override
    public OrderDto toDto(Order order) {
        if (order == null) return null;

        List<OrderItemDto> itemDtos = order.getItems() == null ? List.of() :
                order.getItems().stream()
                        .map(this::mapToItemDto)
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
                order.getUser() != null
                        ? order.getUser().getFirstname() + " " + order.getUser().getLastname()
                        : null,
                order.getUser() != null ? order.getUser().getEmail() : null,
                order.getShippingAddress() != null ? order.getShippingAddress().getId() : null,
                itemDtos
        );
    }

    @Override
    public Order toEntity(OrderDto dto) {
        if (dto == null) return null;

        Order order = new Order();
        order.setId(dto.getOrderId());
        order.setOrderDate(dto.getOrderDate());
        order.setStatus(dto.getStatus());
        order.setTrackingNumber(dto.getTrackingNumber());
        order.setPaymentMethod(dto.getPaymentMethod());
        order.setTotalAmount(dto.getTotalAmount());
        order.setShippingCost(dto.getShippingCost());
        order.setCustomerNote(dto.getCustomerNote());
        return order;
    }

    private OrderItemDto mapToItemDto(OrderItem item) {
        return new OrderItemDto(
                item.getId(),
                item.getProduct() != null ? item.getProduct().getId() : null,
                item.getQuantity(),
                item.getUnitPrice(),
                item.getTotalPrice()
        );
    }

    @Override
    public void updateEntityFromDto(OrderDto dto, Order entity) {
        if (dto.getStatus() != null) entity.setStatus(dto.getStatus());
        if (dto.getPaymentMethod() != null) entity.setPaymentMethod(dto.getPaymentMethod());
        if (dto.getCustomerNote() != null) entity.setCustomerNote(dto.getCustomerNote());
        if (dto.getTotalAmount() != null) entity.setTotalAmount(dto.getTotalAmount());
        if (dto.getShippingCost() != null) entity.setShippingCost(dto.getShippingCost());
    }
}
