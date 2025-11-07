package com.example.demo.domain.userManagementDomain.user.exception;

public class UserDisabledException extends UserException {
    public UserDisabledException(Integer userId) {
        super("User with ID " + userId + " is disabled and cannot perform this action.");
    }
}
