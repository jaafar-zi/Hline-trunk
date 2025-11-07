package com.example.demo.domain.userManagementDomain.address.exception;

public class DuplicateDefaultAddressException extends AddressException {
    public DuplicateDefaultAddressException(Long userId) {
        super("User with ID " + userId + " already has a default address");
    }
}
