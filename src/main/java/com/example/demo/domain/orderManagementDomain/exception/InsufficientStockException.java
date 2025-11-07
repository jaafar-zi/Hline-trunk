package com.example.demo.domain.orderManagementDomain.exception;

public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(Long productId) {
        super("Insufficient stock for product ID " + productId);
    }
}
