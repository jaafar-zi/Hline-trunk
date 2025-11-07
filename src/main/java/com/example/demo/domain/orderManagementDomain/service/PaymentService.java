package com.example.demo.domain.orderManagementDomain.service;

import com.example.demo.domain.orderManagementDomain.dto.PaymentDto;
import com.example.demo.domain.orderManagementDomain.entity.Payment;
import com.example.demo.domain.orderManagementDomain.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class PaymentService {

    private final PaymentRepository paymentRepository;

    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    public PaymentDto createPayment(PaymentDto dto) {
        Payment payment = toEntity(dto);
        payment.setCreatedAt(LocalDateTime.now());
        payment.setUpdatedAt(LocalDateTime.now());
        return toDto(paymentRepository.save(payment));
    }

    public PaymentDto updatePaymentStatus(Long paymentId, String status) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found"));
        payment.setStatus(status);
        payment.setUpdatedAt(LocalDateTime.now());
        return toDto(paymentRepository.save(payment));
    }

    @Transactional(readOnly = true)
    public Optional<PaymentDto> getPaymentById(Long paymentId) {
        return paymentRepository.findById(paymentId).map(this::toDto);
    }

    @Transactional(readOnly = true)
    public List<PaymentDto> getAllPayments() {
        return paymentRepository.findAll()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PaymentDto> getPaymentsByOrderId(Long orderId) {
        return paymentRepository.findAll()
                .stream()
                .filter(payment -> payment.getOrderId().equals(orderId))
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public PaymentDto toDto(Payment payment) {
        PaymentDto dto = new PaymentDto();
        dto.setId(payment.getId());
        dto.setOrderId(payment.getOrderId());
        dto.setAmount(payment.getAmount());
        dto.setPaymentMethod(payment.getPaymentMethod());
        dto.setStatus(payment.getStatus());
        dto.setTransactionId(payment.getTransactionId());
        dto.setCreatedAt(payment.getCreatedAt());
        dto.setUpdatedAt(payment.getUpdatedAt());
        return dto;
    }

    public Payment toEntity(PaymentDto dto) {
        Payment payment = new Payment();
        payment.setId(dto.getId());
        payment.setOrderId(dto.getOrderId());
        payment.setAmount(dto.getAmount());
        payment.setPaymentMethod(dto.getPaymentMethod());
        payment.setStatus(dto.getStatus());
        payment.setTransactionId(dto.getTransactionId());
        payment.setCreatedAt(dto.getCreatedAt());
        payment.setUpdatedAt(dto.getUpdatedAt());
        return payment;
    }
}

