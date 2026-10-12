package com.example.demo.common.validator;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Utility class for common validation operations
 *
 * Provides reusable validation methods that can be used across the application
 *
 * @author Architecture Team
 * @version 1.0
 */
@Slf4j
@UtilityClass
public class CustomValidators {

    private static final Pattern URL_PATTERN = Pattern.compile(
            "^(https?|ftp)://[\\w\\-]+(\\.[\\w\\-]+)+[/#?]?.*$"
    );

    private static final Pattern USERNAME_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_]{3,20}$"
    );

    private static final Pattern ALPHANUMERIC_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9]+$"
    );

    private static final Pattern NUMERIC_PATTERN = Pattern.compile(
            "^\\d+$"
    );

    private static final Pattern UUID_PATTERN = Pattern.compile(
            "^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern IP_V4_PATTERN = Pattern.compile(
            "^((25[0-5]|(2[0-4]|1\\d|[1-9]|)\\d)\\.?\\b){4}$"
    );

    private static final Pattern CREDIT_CARD_PATTERN = Pattern.compile(
            "^\\d{13,19}$"
    );

    // Common weak passwords
    private static final List<String> WEAK_PASSWORDS = Arrays.asList(
            "password", "123456", "12345678", "qwerty", "abc123",
            "password123", "admin", "letmein", "welcome", "monkey"
    );

    /**
     * Validate URL format
     */
    public static boolean isValidUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return false;
        }
        return URL_PATTERN.matcher(url.trim()).matches();
    }

    /**
     * Validate username format
     * Rules: 3-20 characters, alphanumeric and underscore only
     */
    public static boolean isValidUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        return USERNAME_PATTERN.matcher(username.trim()).matches();
    }

    /**
     * Validate alphanumeric string
     */
    public static boolean isAlphanumeric(String value) {
        if (value == null || value.isEmpty()) {
            return false;
        }
        return ALPHANUMERIC_PATTERN.matcher(value).matches();
    }

    /**
     * Validate numeric string
     */
    public static boolean isNumeric(String value) {
        if (value == null || value.isEmpty()) {
            return false;
        }
        return NUMERIC_PATTERN.matcher(value).matches();
    }

    /**
     * Validate UUID format
     */
    public static boolean isValidUuid(String uuid) {
        if (uuid == null || uuid.trim().isEmpty()) {
            return false;
        }
        return UUID_PATTERN.matcher(uuid.trim()).matches();
    }

    /**
     * Validate IPv4 address
     */
    public static boolean isValidIpV4(String ip) {
        if (ip == null || ip.trim().isEmpty()) {
            return false;
        }
        return IP_V4_PATTERN.matcher(ip.trim()).matches();
    }

    /**
     * Validate credit card number format (Luhn algorithm)
     */
    public static boolean isValidCreditCard(String cardNumber) {
        if (cardNumber == null || cardNumber.isEmpty()) {
            return false;
        }

        String normalized = cardNumber.replaceAll("\\s+", "");

        if (!CREDIT_CARD_PATTERN.matcher(normalized).matches()) {
            return false;
        }

        return isValidLuhn(normalized);
    }

    /**
     * Luhn algorithm for credit card validation
     */
    private static boolean isValidLuhn(String number) {
        int sum = 0;
        boolean alternate = false;

        for (int i = number.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(number.charAt(i));

            if (alternate) {
                digit *= 2;
                if (digit > 9) {
                    digit -= 9;
                }
            }

            sum += digit;
            alternate = !alternate;
        }

        return (sum % 10 == 0);
    }

    /**
     * Check if password is weak/common
     */
    public static boolean isWeakPassword(String password) {
        if (password == null) {
            return true;
        }

        String lowerPassword = password.toLowerCase();
        return WEAK_PASSWORDS.stream()
                .anyMatch(weak -> lowerPassword.contains(weak));
    }

    /**
     * Calculate password strength score (0-100)
     */
    public static int calculatePasswordStrength(String password) {
        if (password == null || password.isEmpty()) {
            return 0;
        }

        int score = 0;

        // Length bonus
        score += Math.min(password.length() * 4, 40);

        // Character variety bonuses
        if (password.matches(".*[a-z].*")) score += 10; // lowercase
        if (password.matches(".*[A-Z].*")) score += 10; // uppercase
        if (password.matches(".*\\d.*")) score += 10;   // digit
        if (password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) score += 15; // special char

        // Penalties
        if (isWeakPassword(password)) score -= 30;
        if (password.matches("^(.)\\1+$")) score -= 20; // all same character

        return Math.max(0, Math.min(100, score));
    }

    /**
     * Validate date is not in the past
     */
    public static boolean isNotPastDate(LocalDate date) {
        if (date == null) {
            return false;
        }
        return !date.isBefore(LocalDate.now());
    }

    /**
     * Validate date is not in the future
     */
    public static boolean isNotFutureDate(LocalDate date) {
        if (date == null) {
            return false;
        }
        return !date.isAfter(LocalDate.now());
    }

    /**
     * Validate date is within range
     */
    public static boolean isDateInRange(LocalDate date, LocalDate min, LocalDate max) {
        if (date == null) {
            return false;
        }

        boolean afterMin = min == null || !date.isBefore(min);
        boolean beforeMax = max == null || !date.isAfter(max);

        return afterMin && beforeMax;
    }

    /**
     * Validate datetime is within range
     */
    public static boolean isDateTimeInRange(LocalDateTime dateTime, LocalDateTime min, LocalDateTime max) {
        if (dateTime == null) {
            return false;
        }

        boolean afterMin = min == null || !dateTime.isBefore(min);
        boolean beforeMax = max == null || !dateTime.isAfter(max);

        return afterMin && beforeMax;
    }

    /**
     * Validate string length
     */
    public static boolean isLengthInRange(String value, int min, int max) {
        if (value == null) {
            return false;
        }
        int length = value.length();
        return length >= min && length <= max;
    }

    /**
     * Validate string contains only allowed characters
     */
    public static boolean containsOnlyAllowedChars(String value, String allowedChars) {
        if (value == null || allowedChars == null) {
            return false;
        }

        for (char c : value.toCharArray()) {
            if (allowedChars.indexOf(c) == -1) {
                return false;
            }
        }

        return true;
    }

    /**
     * Validate email domain
     */
    public static boolean isValidEmailDomain(String email, List<String> allowedDomains) {
        if (email == null || !email.contains("@") || allowedDomains == null || allowedDomains.isEmpty()) {
            return false;
        }

        String domain = email.substring(email.lastIndexOf('@') + 1).toLowerCase();
        return allowedDomains.stream()
                .anyMatch(allowed -> domain.equalsIgnoreCase(allowed));
    }

    /**
     * Validate minimum age
     */
    public static boolean isMinimumAge(LocalDate birthDate, int minimumAge) {
        if (birthDate == null) {
            return false;
        }

        LocalDate minimumBirthDate = LocalDate.now().minusYears(minimumAge);
        return !birthDate.isAfter(minimumBirthDate);
    }

    /**
     * Validate file extension
     */
    public static boolean hasAllowedExtension(String filename, List<String> allowedExtensions) {
        if (filename == null || allowedExtensions == null || allowedExtensions.isEmpty()) {
            return false;
        }

        String extension = getFileExtension(filename);
        return allowedExtensions.stream()
                .anyMatch(allowed -> allowed.equalsIgnoreCase(extension));
    }

    /**
     * Get file extension
     */
    private static String getFileExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        if (lastDot == -1 || lastDot == filename.length() - 1) {
            return "";
        }
        return filename.substring(lastDot + 1);
    }

    /**
     * Sanitize input string (remove dangerous characters)
     */
    public static String sanitize(String input) {
        if (input == null) {
            return null;
        }

        // Remove potential XSS/SQL injection characters
        return input
                .replaceAll("[<>\"']", "")
                .replaceAll("(?i)(script|javascript|onerror|onclick)", "")
                .trim();
    }

    /**
     * Validate no SQL injection patterns
     */
    public static boolean isSqlInjectionFree(String input) {
        if (input == null) {
            return true;
        }

        String lower = input.toLowerCase();
        String[] sqlKeywords = {
                "select", "insert", "update", "delete", "drop", "create",
                "alter", "exec", "execute", "union", "script", "--", "/*", "*/"
        };

        for (String keyword : sqlKeywords) {
            if (lower.contains(keyword)) {
                log.warn("Potential SQL injection detected: {}", input);
                return false;
            }
        }

        return true;
    }
}