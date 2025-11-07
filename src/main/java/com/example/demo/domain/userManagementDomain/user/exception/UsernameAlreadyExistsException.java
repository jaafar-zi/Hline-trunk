package com.example.demo.domain.userManagementDomain.user.exception;

public class UsernameAlreadyExistsException extends UserException {
    public UsernameAlreadyExistsException(String email) {
        super("A user with email '" + email + "' already exists.");
    }
}
