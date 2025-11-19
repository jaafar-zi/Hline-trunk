package com.example.demo.domain.productManagementDomain.exception;

import com.example.demo.common.exception.BadRequestException;

public class InvalidProductDataException extends BadRequestException {
    public InvalidProductDataException(String message) {
        super(message);
    }
}