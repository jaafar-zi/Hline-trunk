package com.example.demo.modules.order.application.dto.filter;

import com.example.demo.common.base.dto.BaseFilter;
import com.example.demo.modules.order.domain.model.vo.OrderStatus;
import com.example.demo.modules.order.domain.model.vo.PaymentStatus;
import com.example.demo.modules.order.domain.model.vo.ShippingStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Order Filter DTO
 * 
 * Used for filtering and searching orders
 * 
 * @author Order Team
 * @version 1.0
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderFilter extends BaseFilter {

    private UUID customerId;
    private String customerEmail;
    private String customerName;
    
    private OrderStatus status;
    private PaymentStatus paymentStatus;
    private ShippingStatus shippingStatus;
    
    private LocalDateTime createdFrom;
    private LocalDateTime createdTo;
    
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private String currency;
    
    private String paymentMethod;
    private String trackingNumber;
    
    @Builder.Default
    private boolean excludeDeleted = true;

    @Override
    public boolean hasFilters() {
        return customerId != null
                || (customerEmail != null && !customerEmail.isBlank())
                || (customerName != null && !customerName.isBlank())
                || status != null
                || paymentStatus != null
                || shippingStatus != null
                || createdFrom != null
                || createdTo != null
                || minAmount != null
                || maxAmount != null
                || (currency != null && !currency.isBlank())
                || (paymentMethod != null && !paymentMethod.isBlank())
                || (trackingNumber != null && !trackingNumber.isBlank());
    }

    @Override
    public void normalize() {
        super.normalize();
        
        // Normalize string fields
        if (customerEmail != null) {
            customerEmail = customerEmail.trim().toLowerCase();
        }
        
        if (customerName != null) {
            customerName = customerName.trim();
        }
        
        if (currency != null) {
            currency = currency.trim().toUpperCase();
        }
        
        if (paymentMethod != null) {
            paymentMethod = paymentMethod.trim();
        }
        
        if (trackingNumber != null) {
            trackingNumber = trackingNumber.trim();
        }
        
        // Validate date range
        if (createdFrom != null && createdTo != null && createdFrom.isAfter(createdTo)) {
            LocalDateTime temp = createdFrom;
            createdFrom = createdTo;
            createdTo = temp;
        }
        
        // Validate amount range
        if (minAmount != null && maxAmount != null && minAmount.compareTo(maxAmount) > 0) {
            BigDecimal temp = minAmount;
            minAmount = maxAmount;
            maxAmount = temp;
        }
    }
}