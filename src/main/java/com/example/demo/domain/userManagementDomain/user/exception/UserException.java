package com.example.demo.domain.userManagementDomain.user.exception;

public abstract class UserException extends RuntimeException {
    public UserException(String message) {
        super(message);
    }
}
