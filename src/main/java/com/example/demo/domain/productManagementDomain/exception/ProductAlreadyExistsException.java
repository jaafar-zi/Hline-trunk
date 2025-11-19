package com.example.demo.domain.productManagementDomain.exception;

import com.example.demo.common.exception.ConflictException;

public class ProductAlreadyExistsException extends ConflictException {
    public ProductAlreadyExistsException(String name) {
        super("Product already exists with name: " + name);
    }
}
