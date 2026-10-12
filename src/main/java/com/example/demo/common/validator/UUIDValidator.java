package com.example.demo.common.validator;


import com.example.demo.common.validator.constraints.ValidUUID;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.UUID;

/**
 * UUID format validator
 *
 * @author Architecture Team
 * @version 1.0
 */
public class UUIDValidator implements ConstraintValidator<ValidUUID, String> {

    private boolean allowNull;

    @Override
    public void initialize(ValidUUID constraintAnnotation) {
        this.allowNull = constraintAnnotation.allowNull();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return allowNull;
        }

        try {
            UUID.fromString(value);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
