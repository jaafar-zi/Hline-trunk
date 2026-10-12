package com.example.demo.common.validator;

import com.example.demo.common.validator.constraints.ValidPhone;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Phone number validator implementation
 *
 * Features:
 * - International phone number validation
 * - Country code validation
 * - Regional phone formats
 * - Extension support
 *
 * @author Architecture Team
 * @version 1.0
 */
@Slf4j
public class PhoneValidator implements ConstraintValidator<ValidPhone, String> {

    // International phone number pattern (E.164 format)
    private static final Pattern INTERNATIONAL_PATTERN = Pattern.compile(
            "^\\+?[1-9]\\d{1,14}$"
    );

    // Phone number pattern with optional formatting
    private static final Pattern FORMATTED_PATTERN = Pattern.compile(
            "^[+]?[(]?[0-9]{1,4}[)]?[-\\s\\.]?[(]?[0-9]{1,4}[)]?[-\\s\\.]?[0-9]{1,9}$"
    );

    // Country code patterns
    private static final Map<String, Pattern> COUNTRY_PATTERNS = new HashMap<>();

    static {
        // US/Canada format: +1 (123) 456-7890 or 1234567890
        COUNTRY_PATTERNS.put("US", Pattern.compile("^(\\+?1)?[-.\\s]?\\(?[2-9]\\d{2}\\)?[-.\\s]?\\d{3}[-.\\s]?\\d{4}$"));
        COUNTRY_PATTERNS.put("CA", Pattern.compile("^(\\+?1)?[-.\\s]?\\(?[2-9]\\d{2}\\)?[-.\\s]?\\d{3}[-.\\s]?\\d{4}$"));

        // UK format: +44 20 1234 5678
        COUNTRY_PATTERNS.put("GB", Pattern.compile("^(\\+?44|0)[-.\\s]?\\d{2,4}[-.\\s]?\\d{3,4}[-.\\s]?\\d{3,4}$"));

        // France format: +33 1 23 45 67 89
        COUNTRY_PATTERNS.put("FR", Pattern.compile("^(\\+?33|0)[-.\\s]?[1-9][-.\\s]?(\\d{2}[-.\\s]?){4}$"));

        // Germany format: +49 30 12345678
        COUNTRY_PATTERNS.put("DE", Pattern.compile("^(\\+?49|0)[-.\\s]?\\d{2,5}[-.\\s]?\\d{3,10}$"));

        // India format: +91 98765 43210
        COUNTRY_PATTERNS.put("IN", Pattern.compile("^(\\+?91|0)?[-.\\s]?[6-9]\\d{9}$"));
    }

    private boolean allowNull;
    private Set<String> allowedRegions;
    private boolean requireCountryCode;
    private boolean allowExtension;
    private ValidPhone.PhoneType type;
    private int minDigits;
    private int maxDigits;

    @Override
    public void initialize(ValidPhone constraintAnnotation) {
        this.allowNull = constraintAnnotation.allowNull();
        this.allowedRegions = Set.of(constraintAnnotation.regions());
        this.requireCountryCode = constraintAnnotation.requireCountryCode();
        this.allowExtension = constraintAnnotation.allowExtension();
        this.type = constraintAnnotation.type();
        this.minDigits = constraintAnnotation.minDigits();
        this.maxDigits = constraintAnnotation.maxDigits();

        log.debug("PhoneValidator initialized with regions: {}, requireCountryCode: {}",
                allowedRegions, requireCountryCode);
    }

    @Override
    public boolean isValid(String phone, ConstraintValidatorContext context) {
        // Handle null/empty
        if (phone == null || phone.trim().isEmpty()) {
            return allowNull;
        }

        phone = phone.trim();

        // Remove extension if present
        String basePhone = removeExtension(phone);

        // Normalize phone number (remove formatting)
        String normalized = normalizePhone(basePhone);

        // Check digit count
        int digitCount = countDigits(normalized);
        if (digitCount < minDigits || digitCount > maxDigits) {
            buildConstraintViolation(context,
                    String.format("Phone number must have between %d and %d digits", minDigits, maxDigits));
            return false;
        }

        // Check country code requirement
        if (requireCountryCode && !normalized.startsWith("+")) {
            buildConstraintViolation(context, "Country code is required (e.g., +1, +44)");
            return false;
        }

        // Validate against region-specific patterns
        if (!allowedRegions.isEmpty()) {
            boolean validForRegion = false;
            for (String region : allowedRegions) {
                Pattern pattern = COUNTRY_PATTERNS.get(region);
                if (pattern != null && pattern.matcher(basePhone).matches()) {
                    validForRegion = true;
                    break;
                }
            }

            if (!validForRegion) {
                buildConstraintViolation(context,
                        String.format("Phone number format is not valid for regions: %s", allowedRegions));
                return false;
            }
        } else {
            // General international format validation
            if (!FORMATTED_PATTERN.matcher(basePhone).matches()) {
                buildConstraintViolation(context, "Invalid phone number format");
                return false;
            }
        }

        return true;
    }

    /**
     * Remove extension from phone number
     */
    private String removeExtension(String phone) {
        if (!allowExtension) {
            return phone;
        }

        // Remove common extension patterns: ext, x, extension
        return phone.replaceAll("(?i)(\\s*(ext|x|extension)\\.?\\s*\\d+)", "").trim();
    }

    /**
     * Normalize phone number (remove formatting characters)
     */
    private String normalizePhone(String phone) {
        // Remove spaces, dashes, dots, parentheses
        return phone.replaceAll("[\\s\\-.()]", "");
    }

    /**
     * Count digits in phone number
     */
    private int countDigits(String phone) {
        return (int) phone.chars()
                .filter(Character::isDigit)
                .count();
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