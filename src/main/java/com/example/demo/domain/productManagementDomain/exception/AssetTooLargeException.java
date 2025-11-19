package com.example.demo.domain.productManagementDomain.exception;

public class AssetTooLargeException extends RuntimeException {
    public AssetTooLargeException(String message) {
        super(message);
    }
}
