package com.example.demo.common.validator.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

import com.example.demo.common.validator.constraints.ValidSKU;

/**
 * SKU validator implementation
 * 
 * @author Architecture Team
 * @version 1.0
 */
public class SKUValidator implements ConstraintValidator<ValidSKU, String> {

    // Default pattern: uppercase letters, numbers, hyphens
    private static final Pattern DEFAULT_PATTERN = Pattern.compile("^[A-Z0-9-]+$");

    private int minLength;
    private int maxLength;
    private String prefix;
    private boolean allowNull;
    private Pattern customPattern;

    @Override
    public void initialize(ValidSKU annotation) {
        this.minLength = annotation.minLength();
        this.maxLength = annotation.maxLength();
        this.prefix = annotation.prefix();
        this.allowNull = annotation.allowNull();
        
        if (!annotation.pattern().isBlank()) {
            this.customPattern = Pattern.compile(annotation.pattern());
        }
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // Null check
        if (value == null) {
            return allowNull;
        }

        // Blank check
        if (value.isBlank()) {
            return false;
        }

        String sku = value.trim();

        // Length check
        if (sku.length() < minLength || sku.length() > maxLength) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    String.format("SKU length must be between %d and %d characters", minLength, maxLength)
            ).addConstraintViolation();
            return false;
        }

        // Prefix check
        if (!prefix.isBlank() && !sku.startsWith(prefix)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    String.format("SKU must start with prefix '%s'", prefix)
            ).addConstraintViolation();
            return false;
        }

        // Pattern check
        Pattern patternToUse = customPattern != null ? customPattern : DEFAULT_PATTERN;
        if (!patternToUse.matcher(sku).matches()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    "SKU must contain only uppercase letters, numbers, and hyphens"
            ).addConstraintViolation();
            return false;
        }

        return true;
    }
}