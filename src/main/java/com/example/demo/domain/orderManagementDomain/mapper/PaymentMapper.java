package com.example.demo.domain.orderManagementDomain.mapper;

import com.example.demo.common.mapper.AbstractBaseMapper;
import com.example.demo.domain.orderManagementDomain.dto.PaymentDto;
import com.example.demo.domain.orderManagementDomain.entity.Payment;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapper extends AbstractBaseMapper<Payment, PaymentDto> {

    @Override
    public PaymentDto toDto(Payment entity) {
        if (entity == null) return null;

        PaymentDto dto = new PaymentDto();
        dto.setId(entity.getId());
        dto.setOrderId(entity.getOrderId());
        dto.setAmount(entity.getAmount());
        dto.setPaymentMethod(entity.getPaymentMethod());
        dto.setStatus(entity.getStatus());
        dto.setTransactionId(entity.getTransactionId());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }

    @Override
    public Payment toEntity(PaymentDto dto) {
        if (dto == null) return null;

        Payment entity = new Payment();
        entity.setId(dto.getId());
        entity.setOrderId(dto.getOrderId());
        entity.setAmount(dto.getAmount());
        entity.setPaymentMethod(dto.getPaymentMethod());
        entity.setStatus(dto.getStatus());
        entity.setTransactionId(dto.getTransactionId());
        entity.setCreatedAt(dto.getCreatedAt());
        entity.setUpdatedAt(dto.getUpdatedAt());
        return entity;
    }

    @Override
    public void updateEntityFromDto(PaymentDto dto, Payment entity) {
        if (dto.getStatus() != null) entity.setStatus(dto.getStatus());
        if (dto.getPaymentMethod() != null) entity.setPaymentMethod(dto.getPaymentMethod());
        if (dto.getAmount() != null) entity.setAmount(dto.getAmount());
    }
}
