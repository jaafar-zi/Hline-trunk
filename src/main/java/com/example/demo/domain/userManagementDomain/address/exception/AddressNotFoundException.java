package com.example.demo.domain.userManagementDomain.address.exception;

public class AddressNotFoundException extends AddressException {
    public AddressNotFoundException(Long id) {
        super("Address with ID " + id + " not found");
    }
}

