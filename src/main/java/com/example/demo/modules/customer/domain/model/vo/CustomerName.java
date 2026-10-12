package com.example.demo.modules.customer.domain.model.vo;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.io.Serializable;

@Getter
@EqualsAndHashCode
public final class CustomerName implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final int MIN_NAME_LENGTH = 1;
    private static final int MAX_NAME_LENGTH = 50;

    private final String firstName;
    private final String lastName;

    /**
     * Protected no-arg constructor for JPA only.
     * Never use this directly.
     */
    protected CustomerName() {
        this.firstName = null;
        this.lastName  = null;
    }

    public CustomerName(String firstName, String lastName) {
        this.firstName = validateAndNormalize(firstName, "First name");
        this.lastName  = validateAndNormalize(lastName,  "Last name");
    }

    private String validateAndNormalize(String name, String fieldName) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(fieldName + " cannot be empty");
        }

        String trimmed = name.trim();

        if (trimmed.length() < MIN_NAME_LENGTH) {
            throw new IllegalArgumentException(
                fieldName + " must be at least " + MIN_NAME_LENGTH + " character(s)"
            );
        }

        if (trimmed.length() > MAX_NAME_LENGTH) {
            throw new IllegalArgumentException(
                fieldName + " cannot exceed " + MAX_NAME_LENGTH + " characters"
            );
        }

        return trimmed;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public String getFullNameReversed() {
        return lastName + ", " + firstName;
    }

    public String getInitials() {
        return String.valueOf(firstName.charAt(0)) + lastName.charAt(0);
    }

    @Override
    public String toString() {
        return getFullName();
    }
}