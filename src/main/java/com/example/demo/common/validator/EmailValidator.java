package com.example.demo.common.validator;

import com.example.demo.common.validator.constraints.ValidEmail;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Email validator implementation
 *
 * Features:
 * - RFC 5322 compliant email validation
 * - Domain whitelist/blacklist
 * - TLD validation
 * - Plus-addressing support
 *
 * @author Architecture Team
 * @version 1.0
 */
@Slf4j
public class EmailValidator implements ConstraintValidator<ValidEmail, String> {

    // RFC 5322 compliant email regex (simplified version)
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,}$"
    );

    // Common disposable email domains
    private static final Set<String> DISPOSABLE_DOMAINS = new HashSet<>(Arrays.asList(
            "tempmail.com", "throwaway.email", "guerrillamail.com", "mailinator.com",
            "10minutemail.com", "trashmail.com", "sharklasers.com"
    ));

    private boolean allowNull;
    private Set<String> allowedDomains;
    private Set<String> blockedDomains;
    private Set<String> requiredTlds;
    private int maxLength;
    private boolean allowPlusAddressing;
    private boolean requireVerifiedDomain;

    @Override
    public void initialize(ValidEmail constraintAnnotation) {
        this.allowNull = constraintAnnotation.allowNull();
        this.allowedDomains = new HashSet<>(Arrays.asList(constraintAnnotation.allowedDomains()));
        this.blockedDomains = new HashSet<>(Arrays.asList(constraintAnnotation.blockedDomains()));
        this.requiredTlds = new HashSet<>(Arrays.asList(constraintAnnotation.requiredTlds()));
        this.maxLength = constraintAnnotation.maxLength();
        this.allowPlusAddressing = constraintAnnotation.allowPlusAddressing();
        this.requireVerifiedDomain = constraintAnnotation.requireVerifiedDomain();

        log.debug("EmailValidator initialized with allowedDomains: {}, blockedDomains: {}",
                allowedDomains, blockedDomains);
    }

    @Override
    public boolean isValid(String email, ConstraintValidatorContext context) {
        // Handle null/empty
        if (email == null || email.trim().isEmpty()) {
            return allowNull;
        }

        email = email.trim().toLowerCase();

        // Check length
        if (email.length() > maxLength) {
            buildConstraintViolation(context,
                    String.format("Email exceeds maximum length of %d characters", maxLength));
            return false;
        }

        // Check basic format
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            buildConstraintViolation(context, "Invalid email format");
            return false;
        }

        // Check plus-addressing
        if (!allowPlusAddressing && email.contains("+")) {
            buildConstraintViolation(context, "Plus-addressing not allowed");
            return false;
        }

        // Extract domain
        String domain = extractDomain(email);
        if (domain == null) {
            buildConstraintViolation(context, "Invalid email domain");
            return false;
        }

        // Check domain whitelist
        if (!allowedDomains.isEmpty() && !allowedDomains.contains(domain)) {
            buildConstraintViolation(context,
                    String.format("Email domain '%s' is not in the allowed list", domain));
            return false;
        }

        // Check domain blacklist
        if (blockedDomains.contains(domain)) {
            buildConstraintViolation(context,
                    String.format("Email domain '%s' is blocked", domain));
            return false;
        }

        // Check disposable email domains
        if (requireVerifiedDomain && DISPOSABLE_DOMAINS.contains(domain)) {
            buildConstraintViolation(context, "Disposable email addresses are not allowed");
            return false;
        }

        // Check TLD
        if (!requiredTlds.isEmpty()) {
            String tld = extractTld(domain);
            if (!requiredTlds.contains(tld)) {
                buildConstraintViolation(context,
                        String.format("Email domain must have one of these TLDs: %s", requiredTlds));
                return false;
            }
        }

        return true;
    }

    /**
     * Extract domain from email
     */
    private String extractDomain(String email) {
        int atIndex = email.lastIndexOf('@');
        if (atIndex == -1 || atIndex == email.length() - 1) {
            return null;
        }
        return email.substring(atIndex + 1);
    }

    /**
     * Extract TLD from domain
     */
    private String extractTld(String domain) {
        int lastDot = domain.lastIndexOf('.');
        if (lastDot == -1 || lastDot == domain.length() - 1) {
            return "";
        }
        return domain.substring(lastDot + 1);
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