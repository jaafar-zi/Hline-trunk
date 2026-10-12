package com.example.demo.modules.user.domain.model.vo;

import com.example.demo.common.base.entity.BaseValueObject;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Email Value Object
 *
 * Represents a validated email address
 * Immutable and self-validating
 *
 * @author Architecture Team
 * @version 1.0
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // For JPA only
public class Email extends BaseValueObject {

    private static final long serialVersionUID = 1L;

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    private static final int MAX_EMAIL_LENGTH = 255;

    @Column(name = "email", length = MAX_EMAIL_LENGTH)
    private String value;

    /**
     * Private constructor - use factory method
     */
    private Email(String value) {
        this.value = value;
        validate(); // Call validation after construction
    }

    /**
     * Factory method to create Email from string
     *
     * @param email the email string
     * @return Email value object
     * @throws IllegalArgumentException if email is invalid
     */
    public static Email of(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }

        String normalized = email.trim().toLowerCase();
        return new Email(normalized);
    }

    /**
     * Validate email invariants
     */
    @Override
    protected void validate() {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Email value cannot be null or empty");
        }

        if (value.length() > MAX_EMAIL_LENGTH) {
            throw new IllegalArgumentException(
                    String.format("Email exceeds maximum length of %d characters", MAX_EMAIL_LENGTH)
            );
        }

        if (!EMAIL_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("Invalid email format: " + value);
        }
    }

    // ====================================
    // Business Methods
    // ====================================

    /**
     * Get email domain
     *
     * @return domain part of email (after @)
     */
    public String getDomain() {
        int atIndex = value.indexOf('@');
        return atIndex > 0 ? value.substring(atIndex + 1) : null;
    }

    /**
     * Get local part (before @)
     *
     * @return local part of email
     */
    public String getLocalPart() {
        int atIndex = value.indexOf('@');
        return atIndex > 0 ? value.substring(0, atIndex) : null;
    }

    /**
     * Check if email is from specific domain
     *
     * @param domain the domain to check
     * @return true if email is from the domain
     */
    public boolean isFromDomain(String domain) {
        if (domain == null) {
            return false;
        }
        return domain.equalsIgnoreCase(getDomain());
    }

    /**
     * Check if email is from any of the specified domains
     *
     * @param domains array of domains to check
     * @return true if email matches any domain
     */
    public boolean isFromAnyDomain(String... domains) {
        if (domains == null || domains.length == 0) {
            return false;
        }
        String emailDomain = getDomain();
        for (String domain : domains) {
            if (domain != null && domain.equalsIgnoreCase(emailDomain)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Mask email for display (j***n@example.com)
     *
     * @return masked email string
     */
    public String getMasked() {
        String local = getLocalPart();
        String domain = getDomain();

        if (local == null || domain == null) {
            return value;
        }

        String masked;
        if (local.length() <= 2) {
            masked = local.charAt(0) + "***";
        } else {
            masked = local.charAt(0) + "***" + local.charAt(local.length() - 1);
        }

        return masked + "@" + domain;
    }

    /**
     * Get anonymized version for GDPR compliance
     *
     * @return anonymized email
     */
    public String getAnonymized() {
        return "deleted@anonymized.local";
    }

    // ====================================
    // Value Object Contract
    // ====================================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Email email = (Email) o;
        return Objects.equals(value, email.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}