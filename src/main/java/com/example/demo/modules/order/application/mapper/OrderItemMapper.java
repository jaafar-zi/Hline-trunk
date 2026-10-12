package com.example.demo.modules.order.application.mapper;

import com.example.demo.common.mapper.BaseMapper;
import com.example.demo.modules.order.application.dto.request.OrderItemRequest;
import com.example.demo.modules.order.application.dto.response.OrderItemResponse;
import com.example.demo.modules.order.domain.model.OrderItem;
import com.example.demo.modules.order.domain.model.vo.Money;
import org.mapstruct.*;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Lean MapStruct mapper for OrderItem entity <-> DTOs
 * 
 * Performance optimized
 *
 * @author Order Team
 * @version 1.0
 */
@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
    injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
public abstract class OrderItemMapper implements BaseMapper<OrderItem, OrderItemResponse> {

    // ====================================
    // Helper Methods
    // ====================================

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

    // ====================================
    // Entity to Response
    // ====================================

    @Override
    @Mapping(target = "productId", source = "productId")
    @Mapping(target = "productSku", source = "productSku")
    @Mapping(target = "productName", source = "productName")
    @Mapping(target = "productVariantId", source = "productVariantId")
    @Mapping(target = "productVariantSku", source = "productVariantSku")
    @Mapping(target = "unitPrice", expression = "java(extractAmount(item.getUnitPrice()))")
    @Mapping(target = "quantity", source = "quantity")
    @Mapping(target = "subtotal", expression = "java(extractAmount(item.getSubtotal()))")
    @Mapping(target = "taxRate", source = "taxRate")
    @Mapping(target = "taxAmount", expression = "java(extractAmount(item.getTaxAmount()))")
    @Mapping(target = "discountAmount", expression = "java(extractAmount(item.getDiscountAmount()))")
    @Mapping(target = "total", expression = "java(extractAmount(item.getTotal()))")
    @Mapping(target = "currency", expression = "java(extractCurrency(item.getUnitPrice()))")
    @Mapping(target = "notes", source = "notes")
    public abstract OrderItemResponse toDto(OrderItem item);

    /**
     * Alias for better readability
     */
    public OrderItemResponse toResponse(OrderItem item) {
        return item != null ? toDto(item) : null;
    }

    @Override
    public List<OrderItemResponse> toDtoList(List<OrderItem> items) {
        if (items == null || items.isEmpty()) {
            return Collections.emptyList();
        }
        return items.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ====================================
    // Request to Entity
    // ====================================

    @Override
    public OrderItem toEntity(OrderItemResponse dto) {
        throw new UnsupportedOperationException(
            "OrderItem should be created through Order aggregate"
        );
    }

    // ====================================
    // Custom Mapping Methods
    // ====================================

    /**
     * Map OrderItemRequest to Money for unit price
     */
    public Money toUnitPriceMoney(OrderItemRequest request, String currency) {
        if (request == null || request.getUnitPrice() == null) {
            return null;
        }
        return Money.of(request.getUnitPrice(), currency);
    }

    /**
     * Map discount amount to Money
     */
    public Money toDiscountMoney(BigDecimal discountAmount, String currency) {
        if (discountAmount == null) {
            return Money.zero(currency);
        }
        return Money.of(discountAmount, currency);
    }
}