package com.example.demo.domain.orderManagementDomain.service;

import com.example.demo.common.base.service.AbstractBaseService;
import com.example.demo.common.exception.EntityNotFoundException;
import com.example.demo.domain.orderManagementDomain.dto.PaymentDto;
import com.example.demo.domain.orderManagementDomain.entity.Payment;
import com.example.demo.domain.orderManagementDomain.repository.PaymentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class PaymentService extends AbstractBaseService<Payment, Long> {

    private final PaymentRepository paymentRepository;

    public PaymentService(PaymentRepository paymentRepository) {
        super(paymentRepository);
        this.paymentRepository = paymentRepository;
    }

    @Override
    protected String getCacheName() {
        return "payments";
    }

    @Override
    protected Class<?> getEntityClass() {
        return Payment.class;
    }

    // ───────────────────────────────────────────────
    //  CRUD METHODS USING BASE SERVICE
    // ───────────────────────────────────────────────

    @Transactional
    public PaymentDto createPayment(PaymentDto dto) {
        Payment payment = toEntity(dto);
        payment.setCreatedAt(LocalDateTime.now());
        payment.setUpdatedAt(LocalDateTime.now());
        return toDto(save(payment));
    }

    @Transactional
    public PaymentDto updatePaymentStatus(Long paymentId, String status) {
        Payment payment = findByIdOrThrow(paymentId);
        payment.setStatus(status);
        payment.setUpdatedAt(LocalDateTime.now());
        return toDto(save(payment));
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "payments", key = "'id:' + #paymentId")
    public PaymentDto getPaymentById(Long paymentId) {
        return findById(paymentId)
                .map(this::toDto)
                .orElseThrow(() -> new EntityNotFoundException(Payment.class, paymentId));
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "payments", key = "'all'")
    public List<PaymentDto> getAllPayments() {
        return findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "payments", key = "'order:' + #orderId")
    public List<PaymentDto> getPaymentsByOrderId(Long orderId) {
        return paymentRepository.findAll().stream()
                .filter(payment -> payment.getOrderId().equals(orderId))
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    // ───────────────────────────────────────────────
    //  DTO ↔ ENTITY MAPPING
    // ───────────────────────────────────────────────

    private PaymentDto toDto(Payment payment) {
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

    private Payment toEntity(PaymentDto dto) {
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

    // ───────────────────────────────────────────────
    //  HOOKS
    // ───────────────────────────────────────────────

    @Override
    protected void onAfterSave(Payment entity) {
        log.debug("Payment {} saved for order {}", entity.getId(), entity.getOrderId());
    }

    @Override
    protected void onAfterDelete(Long id) {
        log.debug("Payment {} deleted", id);
    }
}


