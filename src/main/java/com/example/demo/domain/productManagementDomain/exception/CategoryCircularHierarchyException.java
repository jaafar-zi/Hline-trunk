package com.example.demo.domain.productManagementDomain.exception;

public class CategoryCircularHierarchyException extends RuntimeException {
    public CategoryCircularHierarchyException(String message) {
        super(message);
    }
}
