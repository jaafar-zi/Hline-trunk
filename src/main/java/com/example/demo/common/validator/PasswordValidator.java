package com.example.demo.common.validator;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Password validation utility
 *
 * Features:
 * - Strength calculation
 * - Common password detection
 * - Character type validation
 * - Pattern detection
 * - Entropy calculation
 *
 * @author Architecture Team
 * @version 1.0
 */
@Slf4j
@UtilityClass
public class PasswordValidator  {

    // Password patterns
    private static final Pattern UPPERCASE_PATTERN = Pattern.compile("[A-Z]");
    private static final Pattern LOWERCASE_PATTERN = Pattern.compile("[a-z]");
    private static final Pattern DIGIT_PATTERN = Pattern.compile("\\d");
    private static final Pattern SPECIAL_CHAR_PATTERN = Pattern.compile("[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]");
    private static final Pattern SEQUENTIAL_CHARS = Pattern.compile("(012|123|234|345|456|567|678|789|890|abc|bcd|cde|def|efg|fgh|ghi|hij|ijk|jkl|klm|lmn|mno|nop|opq|pqr|qrs|rst|stu|tuv|uvw|vwx|wxy|xyz)");
    private static final Pattern REPEATED_CHARS = Pattern.compile("(.)\\1{2,}");

    // Top 100 most common passwords
    private static final Set<String> COMMON_PASSWORDS = new HashSet<>(Arrays.asList(
            "password", "123456", "12345678", "qwerty", "abc123", "monkey", "1234567", "letmein",
            "trustno1", "dragon", "baseball", "111111", "iloveyou", "master", "sunshine", "ashley",
            "bailey", "passw0rd", "shadow", "123123", "654321", "superman", "qazwsx", "michael",
            "football", "welcome", "jesus", "ninja", "mustang", "password1", "123456789", "adobe123",
            "admin", "princess", "solo", "access", "flower", "tigger", "login", "1234567890",
            "starwars", "charlie", "qwertyuiop", "696969", "hottie", "freedom", "aa123456", "whatever",
            "donald", "batman", "zaq1zaq1", "qazwsxedc", "000000", "Password", "password123",
            "P@ssw0rd", "Welcome123", "Admin123", "letmein123", "changeme", "test", "guest"
    ));

    // Keyboard patterns
    private static final Set<String> KEYBOARD_PATTERNS = new HashSet<>(Arrays.asList(
            "qwerty", "asdfgh", "zxcvbn", "qwertyuiop", "asdfghjkl", "zxcvbnm",
            "1qaz2wsx", "qazwsx", "1234qwer", "!qaz@wsx"
    ));

    /**
     * Validate password strength
     *
     * @param password Password to validate
     * @return PasswordStrength object with validation results
     */
    public static PasswordStrength validateStrength(String password) {
        if (password == null || password.isEmpty()) {
            return PasswordStrength.builder()
                    .valid(false)
                    .score(0)
                    .strength(StrengthLevel.VERY_WEAK)
                    .feedback("Password is required")
                    .build();
        }

        PasswordStrength.PasswordStrengthBuilder builder = PasswordStrength.builder();
        int score = 0;
        StringBuilder feedback = new StringBuilder();

        // Length check
        int length = password.length();
        if (length < 8) {
            feedback.append("Password must be at least 8 characters. ");
            builder.valid(false);
        } else if (length >= 8 && length < 12) {
            score += 10;
        } else if (length >= 12 && length < 16) {
            score += 20;
        } else {
            score += 25;
        }

        // Character variety
        boolean hasUppercase = UPPERCASE_PATTERN.matcher(password).find();
        boolean hasLowercase = LOWERCASE_PATTERN.matcher(password).find();
        boolean hasDigit = DIGIT_PATTERN.matcher(password).find();
        boolean hasSpecial = SPECIAL_CHAR_PATTERN.matcher(password).find();

        if (hasUppercase) score += 10;
        else feedback.append("Add uppercase letters. ");

        if (hasLowercase) score += 10;
        else feedback.append("Add lowercase letters. ");

        if (hasDigit) score += 10;
        else feedback.append("Add numbers. ");

        if (hasSpecial) score += 15;
        else feedback.append("Add special characters (!@#$%^&*). ");

        // Common password check
        if (isCommonPassword(password)) {
            score -= 50;
            feedback.append("This is a commonly used password. ");
            builder.valid(false);
        }

        // Keyboard pattern check
        if (containsKeyboardPattern(password)) {
            score -= 20;
            feedback.append("Avoid keyboard patterns. ");
        }

        // Sequential characters check
        if (SEQUENTIAL_CHARS.matcher(password.toLowerCase()).find()) {
            score -= 15;
            feedback.append("Avoid sequential characters. ");
        }

        // Repeated characters check
        if (REPEATED_CHARS.matcher(password).find()) {
            score -= 15;
            feedback.append("Avoid repeated characters. ");
        }

        // Unique characters count
        int uniqueChars = countUniqueCharacters(password);
        if (uniqueChars < 5) {
            score -= 10;
            feedback.append("Use more unique characters. ");
        } else {
            score += Math.min(uniqueChars, 15);
        }

        // Calculate entropy
        double entropy = calculateEntropy(password);
        if (entropy < 28) {
            score -= 10;
        } else if (entropy > 50) {
            score += 10;
        }

        // Normalize score to 0-100
        score = Math.max(0, Math.min(100, score));

        // Determine strength level
        StrengthLevel strengthLevel = determineStrengthLevel(score);

        // Set valid if score is at least 60 and has all character types
        boolean isValid = score >= 60 &&
                hasUppercase &&
                hasLowercase &&
                hasDigit &&
                hasSpecial &&
                length >= 8;

        if (isValid && feedback.length() == 0) {
            feedback.append("Strong password!");
        }

        return builder
                .valid(isValid)
                .score(score)
                .strength(strengthLevel)
                .feedback(feedback.toString().trim())
                .length(length)
                .hasUppercase(hasUppercase)
                .hasLowercase(hasLowercase)
                .hasDigit(hasDigit)
                .hasSpecialChar(hasSpecial)
                .uniqueCharacters(uniqueChars)
                .entropy(entropy)
                .build();
    }

    /**
     * Check if password meets minimum requirements
     */
    public static boolean meetsMinimumRequirements(
            String password,
            int minLength,
            boolean requireUppercase,
            boolean requireLowercase,
            boolean requireDigit,
            boolean requireSpecial) {

        if (password == null || password.length() < minLength) {
            return false;
        }

        if (requireUppercase && !UPPERCASE_PATTERN.matcher(password).find()) {
            return false;
        }

        if (requireLowercase && !LOWERCASE_PATTERN.matcher(password).find()) {
            return false;
        }

        if (requireDigit && !DIGIT_PATTERN.matcher(password).find()) {
            return false;
        }

        if (requireSpecial && !SPECIAL_CHAR_PATTERN.matcher(password).find()) {
            return false;
        }

        return true;
    }

    /**
     * Check if password is common/weak
     */
    public static boolean isCommonPassword(String password) {
        if (password == null) {
            return false;
        }
        return COMMON_PASSWORDS.contains(password.toLowerCase());
    }

    /**
     * Check if password contains keyboard patterns
     */
    public static boolean containsKeyboardPattern(String password) {
        if (password == null) {
            return false;
        }
        String lower = password.toLowerCase();
        return KEYBOARD_PATTERNS.stream().anyMatch(lower::contains);
    }

    /**
     * Count unique characters in password
     */
    public static int countUniqueCharacters(String password) {
        if (password == null) {
            return 0;
        }
        return (int) password.chars().distinct().count();
    }

    /**
     * Calculate password entropy (in bits)
     */
    public static double calculateEntropy(String password) {
        if (password == null || password.isEmpty()) {
            return 0;
        }

        // Calculate character space
        int charSpace = 0;
        if (LOWERCASE_PATTERN.matcher(password).find()) charSpace += 26;
        if (UPPERCASE_PATTERN.matcher(password).find()) charSpace += 26;
        if (DIGIT_PATTERN.matcher(password).find()) charSpace += 10;
        if (SPECIAL_CHAR_PATTERN.matcher(password).find()) charSpace += 32;

        // Entropy = log2(charSpace^length)
        return password.length() * (Math.log(charSpace) / Math.log(2));
    }

    /**
     * Determine strength level from score
     */
    private static StrengthLevel determineStrengthLevel(int score) {
        if (score < 20) return StrengthLevel.VERY_WEAK;
        if (score < 40) return StrengthLevel.WEAK;
        if (score < 60) return StrengthLevel.MODERATE;
        if (score < 80) return StrengthLevel.STRONG;
        return StrengthLevel.VERY_STRONG;
    }

    /**
     * Generate password suggestions
     */
    public static List<String> generateSuggestions(String password) {
        List<String> suggestions = new java.util.ArrayList<>();

        if (password == null || password.length() < 8) {
            suggestions.add("Use at least 8 characters");
        }

        if (!UPPERCASE_PATTERN.matcher(password).find()) {
            suggestions.add("Add uppercase letters (A-Z)");
        }

        if (!LOWERCASE_PATTERN.matcher(password).find()) {
            suggestions.add("Add lowercase letters (a-z)");
        }

        if (!DIGIT_PATTERN.matcher(password).find()) {
            suggestions.add("Add numbers (0-9)");
        }

        if (!SPECIAL_CHAR_PATTERN.matcher(password).find()) {
            suggestions.add("Add special characters (!@#$%^&*)");
        }

        if (isCommonPassword(password)) {
            suggestions.add("Avoid common passwords");
        }

        if (containsKeyboardPattern(password)) {
            suggestions.add("Avoid keyboard patterns (qwerty, asdf, etc.)");
        }

        return suggestions;
    }

    /**
     * Password strength result
     */
    @lombok.Data
    @lombok.Builder
    public static class PasswordStrength {
        private boolean valid;
        private int score;
        private StrengthLevel strength;
        private String feedback;
        private int length;
        private boolean hasUppercase;
        private boolean hasLowercase;
        private boolean hasDigit;
        private boolean hasSpecialChar;
        private int uniqueCharacters;
        private double entropy;
    }

    /**
     * Password strength levels
     */
    public enum StrengthLevel {
        VERY_WEAK,
        WEAK,
        MODERATE,
        STRONG,
        VERY_STRONG
    }
}