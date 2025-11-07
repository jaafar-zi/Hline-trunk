package com.example.demo.domain.userManagementDomain.user.exception;

public class UserAccountLockedException extends UserException {
    public UserAccountLockedException(Integer userId) {
        super("User with ID " + userId + " is locked and cannot perform this action.");
    }
}
