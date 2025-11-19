package com.example.demo.domain.productManagementDomain.exception;

public class PricingAlreadyAssignedException extends RuntimeException {
    public PricingAlreadyAssignedException(String message) {
        super(message);
    }
}
