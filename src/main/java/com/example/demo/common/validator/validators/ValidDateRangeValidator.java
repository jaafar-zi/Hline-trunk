package com.example.demo.common.validator.validators;

import com.example.demo.common.validator.constraints.ValidDateRange;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Validator for @ValidDateRange annotation
 *
 * Validates that a date falls within a specific range
 *
 * @author Architecture Team
 * @version 1.0
 */
@Slf4j
public class ValidDateRangeValidator implements ConstraintValidator<ValidDateRange, Object> {

    private static final DateTimeFormatter ISO_DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    private LocalDate minDate;
    private LocalDate maxDate;
    private boolean allowNull;
    private boolean useOffset;
    private int minDaysOffset;
    private int maxDaysOffset;

    @Override
    public void initialize(ValidDateRange constraintAnnotation) {
        this.allowNull = constraintAnnotation.allowNull();
        this.useOffset = constraintAnnotation.useOffset();
        this.minDaysOffset = constraintAnnotation.minDaysOffset();
        this.maxDaysOffset = constraintAnnotation.maxDaysOffset();

        if (useOffset) {
            // Calculate dates based on offset from today
            LocalDate today = LocalDate.now();
            this.minDate = minDaysOffset != 0 ? today.plusDays(minDaysOffset) : null;
            this.maxDate = maxDaysOffset != 0 ? today.plusDays(maxDaysOffset) : null;
        } else {
            // Parse fixed dates
            String minStr = constraintAnnotation.min();
            String maxStr = constraintAnnotation.max();

            this.minDate = minStr.isEmpty() ? null : LocalDate.parse(minStr, ISO_DATE_FORMATTER);
            this.maxDate = maxStr.isEmpty() ? null : LocalDate.parse(maxStr, ISO_DATE_FORMATTER);
        }

        log.debug("ValidDateRangeValidator initialized: min={}, max={}, useOffset={}",
                minDate, maxDate, useOffset);
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return allowNull;
        }

        LocalDate dateToValidate = convertToLocalDate(value);
        if (dateToValidate == null) {
            buildConstraintViolation(context, "Invalid date format");
            return false;
        }

        // Check minimum date
        if (minDate != null && dateToValidate.isBefore(minDate)) {
            buildConstraintViolation(context,
                    String.format("Date must not be before %s", minDate));
            return false;
        }

        // Check maximum date
        if (maxDate != null && dateToValidate.isAfter(maxDate)) {
            buildConstraintViolation(context,
                    String.format("Date must not be after %s", maxDate));
            return false;
        }

        return true;
    }

    /**
     * Convert various date types to LocalDate
     */
    private LocalDate convertToLocalDate(Object value) {
        if (value instanceof LocalDate) {
            return (LocalDate) value;
        } else if (value instanceof LocalDateTime) {
            return ((LocalDateTime) value).toLocalDate();
        } else if (value instanceof java.util.Date) {
            return ((java.util.Date) value).toInstant()
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDate();
        } else if (value instanceof String) {
            try {
                return LocalDate.parse((String) value, ISO_DATE_FORMATTER);
            } catch (Exception e) {
                log.warn("Failed to parse date string: {}", value);
                return null;
            }
        }
        return null;
    }

    /**
     * Build custom constraint violation message
     */
    private void buildConstraintViolation(ConstraintValidatorContext context, String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message)
                .addConstraintViolation();
    }
}