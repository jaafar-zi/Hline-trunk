package com.example.demo.domain.productManagementDomain.exception;


import javassist.NotFoundException;

public class ProductNotFoundException extends NotFoundException {
    public ProductNotFoundException(Long id) {
        super("Product not found with id: " + id);
    }
}
