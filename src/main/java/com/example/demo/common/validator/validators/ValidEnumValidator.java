package com.example.demo.common.validator.validators;

import com.example.demo.common.validator.constraints.ValidEnum;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Validator for @ValidEnum annotation
 *
 * Validates that a string value is a valid enum constant
 *
 * @author Architecture Team
 * @version 1.0
 */
@Slf4j
public class ValidEnumValidator implements ConstraintValidator<ValidEnum, String> {

    private Set<String> acceptableValues;
    private boolean ignoreCase;
    private boolean allowNull;
    private Class<? extends Enum<?>> enumClass;

    @Override
    public void initialize(ValidEnum constraintAnnotation) {
        this.enumClass = constraintAnnotation.enumClass();
        this.ignoreCase = constraintAnnotation.ignoreCase();
        this.allowNull = constraintAnnotation.allowNull();

        // Extract all enum constant names
        this.acceptableValues = Arrays.stream(enumClass.getEnumConstants())
                .map(Enum::name)
                .collect(Collectors.toSet());

        log.debug("ValidEnumValidator initialized for enum: {}, values: {}",
                enumClass.getSimpleName(), acceptableValues);
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // Handle null
        if (value == null || value.trim().isEmpty()) {
            return allowNull;
        }

        String valueToCheck = value.trim();

        // Check if value is valid enum constant
        boolean isValid;
        if (ignoreCase) {
            isValid = acceptableValues.stream()
                    .anyMatch(acceptable -> acceptable.equalsIgnoreCase(valueToCheck));
        } else {
            isValid = acceptableValues.contains(valueToCheck);
        }

        if (!isValid) {
            buildConstraintViolation(context,
                    String.format("Value must be one of: %s",
                            String.join(", ", acceptableValues)));
        }

        return isValid;
    }

    /**
     * Build custom constraint violation message
     */
    private void buildConstraintViolation(ConstraintValidatorContext context, String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message)
                .addConstraintViolation();
    }

    /**
     * Try to convert string to enum value
     */
    public static <E extends Enum<E>> E toEnum(Class<E> enumClass, String value, boolean ignoreCase) {
        if (value == null) {
            return null;
        }

        try {
            if (ignoreCase) {
                for (E enumConstant : enumClass.getEnumConstants()) {
                    if (enumConstant.name().equalsIgnoreCase(value)) {
                        return enumConstant;
                    }
                }
                return null;
            } else {
                return Enum.valueOf(enumClass, value);
            }
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}