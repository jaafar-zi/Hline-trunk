package com.example.demo.common.validation.result;

import lombok.Getter;

import java.util.List;

/**
 * Validation exception
 * 
 * Thrown when validation fails and exceptions are preferred over result objects
 * 
 * @author Architecture Team
 * @version 1.0
 */
@Getter
public class ValidationException extends RuntimeException {

    private final ValidationResult validationResult;

    public ValidationException(ValidationResult validationResult) {
        super(buildMessage(validationResult));
        this.validationResult = validationResult;
    }

    public ValidationException(String message, ValidationResult validationResult) {
        super(message);
        this.validationResult = validationResult;
    }

    public ValidationException(String message) {
        super(message);
        this.validationResult = ValidationResult.failure("global", message);
    }

    private static String buildMessage(ValidationResult result) {
        if (result == null || !result.hasErrors()) {
            return "Validation failed";
        }

        List<ValidationError> errors = result.getErrors();
        if (errors.size() == 1) {
            return errors.get(0).toString();
        }

        return String.format("Validation failed with %d error(s): %s",
                errors.size(),
                errors.stream()
                        .map(ValidationError::toString)
                        .reduce((a, b) -> a + "; " + b)
                        .orElse(""));
    }

    public List<ValidationError> getErrors() {
        return validationResult != null ? validationResult.getErrors() : List.of();
    }

    public List<ValidationWarning> getWarnings() {
        return validationResult != null ? validationResult.getWarnings() : List.of();
    }
}