package com.example.demo.domain.orderManagementDomain.exception;

public class InvalidOrderStateException extends RuntimeException {
    public InvalidOrderStateException(String action) {
        super("Order cannot be " + action + " in its current state");
    }
}
