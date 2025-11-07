package com.example.demo.domain.userManagementDomain.user.exception;

public class InvalidEmailException extends UserException {
    public InvalidEmailException(String email) {
        super("Invalid or duplicate email: " + email);
    }
}
