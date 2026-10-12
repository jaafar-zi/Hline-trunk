package com.example.demo.common.validator.validators;

import com.example.demo.common.validator.PasswordValidator;
import com.example.demo.common.validator.constraints.ValidPassword;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;

import java.util.regex.Pattern;

/**
 * Validator for @ValidPassword annotation
 *
 * Validates password strength and complexity
 *
 * @author Architecture Team
 * @version 1.0
 */
@Slf4j
public class ValidPasswordValidator implements ConstraintValidator<ValidPassword, String> {

    private int minLength;
    private int maxLength;
    private boolean requireUppercase;
    private boolean requireLowercase;
    private boolean requireDigit;
    private boolean requireSpecialChar;
    private String allowedSpecialChars;
    private boolean disallowCommon;
    private boolean disallowUsername;
    private int minUniqueChars;
    private ValidPassword.StrengthLevel strengthLevel;

    private Pattern uppercasePattern;
    private Pattern lowercasePattern;
    private Pattern digitPattern;
    private Pattern specialCharPattern;

    @Override
    public void initialize(ValidPassword constraintAnnotation) {
        this.minLength = constraintAnnotation.minLength();
        this.maxLength = constraintAnnotation.maxLength();
        this.requireUppercase = constraintAnnotation.requireUppercase();
        this.requireLowercase = constraintAnnotation.requireLowercase();
        this.requireDigit = constraintAnnotation.requireDigit();
        this.requireSpecialChar = constraintAnnotation.requireSpecialChar();
        this.allowedSpecialChars = constraintAnnotation.allowedSpecialChars();
        this.disallowCommon = constraintAnnotation.disallowCommon();
        this.disallowUsername = constraintAnnotation.disallowUsername();
        this.minUniqueChars = constraintAnnotation.minUniqueChars();
        this.strengthLevel = constraintAnnotation.strength();

        // Compile patterns
        this.uppercasePattern = Pattern.compile("[A-Z]");
        this.lowercasePattern = Pattern.compile("[a-z]");
        this.digitPattern = Pattern.compile("\\d");
        this.specialCharPattern = Pattern.compile("[" + Pattern.quote(allowedSpecialChars) + "]");

        log.debug("ValidPasswordValidator initialized with minLength: {}, strength: {}",
                minLength, strengthLevel);
    }

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null || password.isEmpty()) {
            buildConstraintViolation(context, "Password is required");
            return false;
        }

        // Check length
        if (password.length() < minLength) {
            buildConstraintViolation(context,
                    String.format("Password must be at least %d characters", minLength));
            return false;
        }

        if (password.length() > maxLength) {
            buildConstraintViolation(context,
                    String.format("Password must not exceed %d characters", maxLength));
            return false;
        }

        // Check uppercase requirement
        if (requireUppercase && !uppercasePattern.matcher(password).find()) {
            buildConstraintViolation(context, "Password must contain at least one uppercase letter");
            return false;
        }

        // Check lowercase requirement
        if (requireLowercase && !lowercasePattern.matcher(password).find()) {
            buildConstraintViolation(context, "Password must contain at least one lowercase letter");
            return false;
        }

        // Check digit requirement
        if (requireDigit && !digitPattern.matcher(password).find()) {
            buildConstraintViolation(context, "Password must contain at least one digit");
            return false;
        }

        // Check special character requirement
        if (requireSpecialChar && !specialCharPattern.matcher(password).find()) {
            buildConstraintViolation(context,
                    String.format("Password must contain at least one special character (%s)", allowedSpecialChars));
            return false;
        }

        // Check unique characters
        int uniqueChars = PasswordValidator.countUniqueCharacters(password);
        if (uniqueChars < minUniqueChars) {
            buildConstraintViolation(context,
                    String.format("Password must contain at least %d unique characters", minUniqueChars));
            return false;
        }

        // Check common passwords
        if (disallowCommon && PasswordValidator.isCommonPassword(password)) {
            buildConstraintViolation(context, "This password is too common. Please choose a stronger password");
            return false;
        }

        // Validate strength level
        PasswordValidator.PasswordStrength strength = PasswordValidator.validateStrength(password);
        if (!meetsStrengthLevel(strength)) {
            buildConstraintViolation(context,
                    String.format("Password does not meet %s strength requirements. %s",
                            strengthLevel, strength.getFeedback()));
            return false;
        }

        return true;
    }

    /**
     * Check if password meets required strength level
     */
    private boolean meetsStrengthLevel(PasswordValidator.PasswordStrength strength) {
        int requiredScore = switch (strengthLevel) {
            case WEAK -> 20;
            case MEDIUM -> 40;
            case STRONG -> 60;
            case MAXIMUM -> 80;
        };

        return strength.getScore() >= requiredScore;
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