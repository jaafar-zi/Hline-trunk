package com.example.demo.common.validator.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

import com.example.demo.common.validator.constraints.ValidSlug;

/**
 * Slug validator implementation
 * 
 * @author Architecture Team
 * @version 1.0
 */
public class SlugValidator implements ConstraintValidator<ValidSlug, String> {

    private static final Pattern SLUG_PATTERN = Pattern.compile("^[a-z0-9]+(?:-[a-z0-9]+)*$");

    private int minLength;
    private int maxLength;
    private boolean allowNull;

    @Override
    public void initialize(ValidSlug annotation) {
        this.minLength = annotation.minLength();
        this.maxLength = annotation.maxLength();
        this.allowNull = annotation.allowNull();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return allowNull;
        }

        if (value.isBlank()) {
            return false;
        }

        String slug = value.trim();

        // Length check
        if (slug.length() < minLength || slug.length() > maxLength) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    String.format("Slug length must be between %d and %d characters", minLength, maxLength)
            ).addConstraintViolation();
            return false;
        }

        // Format check
        if (!SLUG_PATTERN.matcher(slug).matches()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    "Slug must contain only lowercase letters, numbers, and hyphens (no spaces, no consecutive hyphens)"
            ).addConstraintViolation();
            return false;
        }

        return true;
    }
}