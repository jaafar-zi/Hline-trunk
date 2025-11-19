package com.example.demo.domain.productManagementDomain.exception;

public class ProductHasDependenciesException extends RuntimeException {
    public ProductHasDependenciesException(String message) {
        super(message);
    }
}
