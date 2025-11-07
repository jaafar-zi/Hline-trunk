package com.example.demo.domain.orderManagementDomain.exception;

public class OrderNotFoundException extends RuntimeException {
  public OrderNotFoundException(Long id) {
    super("Order not found with ID " + id);
  }
}
