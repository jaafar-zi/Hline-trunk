package com.example.demo.domain.userManagementDomain.user.exception;

public class InvalidUserProfileException extends UserException {
    public InvalidUserProfileException(String reason) {
        super("Invalid user profile: " + reason);
    }
}
