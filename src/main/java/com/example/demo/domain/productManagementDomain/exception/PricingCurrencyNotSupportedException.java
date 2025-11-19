package com.example.demo.domain.productManagementDomain.exception;

public class PricingCurrencyNotSupportedException extends RuntimeException {
    public PricingCurrencyNotSupportedException(String message) {
        super(message);
    }
}
