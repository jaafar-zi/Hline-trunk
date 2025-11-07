package com.example.demo.domain.userManagementDomain.user.exception;

public class UserNotFoundException extends UserException {
    public UserNotFoundException(String message) {
        super(message);
    }

    public UserNotFoundException(Integer userId) {
        super("User with ID " + userId + " not found");
    }
}
