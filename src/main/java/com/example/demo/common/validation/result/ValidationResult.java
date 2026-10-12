package com.example.demo.common.validation.result;

import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import java.util.*;

/**
 * Unified validation result
 * 
 * Supports both functional (return result) and exception-based approaches
 * 
 * @author Architecture Team
 * @version 1.0
 */
@Data
@Builder
public class ValidationResult {

    /**
     * Overall validation status
     */
    @Builder.Default
    private boolean valid = true;

    /**
     * Validation errors (blocking issues)
     */
    @Singular
    private List<ValidationError> errors;

    /**
     * Validation warnings (non-blocking issues)
     */
    @Singular
    private List<ValidationWarning> warnings;

    /**
     * Additional metadata
     */
    @Singular("metadatum")
    private Map<String, Object> metadata;

    /**
     * Validation context
     */
    private String context;

    /**
     * Timestamp
     */
    @Builder.Default
    private long timestamp = System.currentTimeMillis();

    // ====================================
    // Factory Methods
    // ====================================

    public static ValidationResult success() {
        return ValidationResult.builder()
                .valid(true)
                .build();
    }

    public static ValidationResult success(String context) {
        return ValidationResult.builder()
                .valid(true)
                .context(context)
                .build();
    }

    public static ValidationResult failure(ValidationError error) {
        return ValidationResult.builder()
                .valid(false)
                .error(error)
                .build();
    }

    public static ValidationResult failure(List<ValidationError> errors) {
        return ValidationResult.builder()
                .valid(false)
                .errors(errors)
                .build();
    }

    public static ValidationResult failure(String field, String message) {
        return failure(ValidationError.of(field, message));
    }

    // ====================================
    // Helper Methods
    // ====================================

    public boolean hasErrors() {
        return errors != null && !errors.isEmpty();
    }

    public boolean hasWarnings() {
        return warnings != null && !warnings.isEmpty();
    }

    public int getErrorCount() {
        return errors != null ? errors.size() : 0;
    }

    public int getWarningCount() {
        return warnings != null ? warnings.size() : 0;
    }

    /**
     * Add error and mark as invalid
     */
    public void addError(ValidationError error) {
        if (this.errors == null) {
            this.errors = new ArrayList<>();
        }
        this.errors.add(error);
        this.valid = false;
    }

    public void addError(String field, String message) {
        addError(ValidationError.of(field, message));
    }

    public void addError(String field, String message, Object rejectedValue) {
        addError(ValidationError.of(field, message, rejectedValue));
    }

    /**
     * Add warning (doesn't affect validity)
     */
    public void addWarning(ValidationWarning warning) {
        if (this.warnings == null) {
            this.warnings = new ArrayList<>();
        }
        this.warnings.add(warning);
    }

    public void addWarning(String field, String message) {
        addWarning(ValidationWarning.of(field, message));
    }

    /**
     * Merge another validation result
     */
    public void merge(ValidationResult other) {
        if (other == null) {
            return;
        }

        if (other.hasErrors()) {
            if (this.errors == null) {
                this.errors = new ArrayList<>();
            }
            this.errors.addAll(other.getErrors());
            this.valid = false;
        }

        if (other.hasWarnings()) {
            if (this.warnings == null) {
                this.warnings = new ArrayList<>();
            }
            this.warnings.addAll(other.getWarnings());
        }

        if (other.getMetadata() != null && !other.getMetadata().isEmpty()) {
            if (this.metadata == null) {
                this.metadata = new HashMap<>();
            }
            this.metadata.putAll(other.getMetadata());
        }
    }

    /**
     * Throw exception if invalid
     */
    public void throwIfInvalid() {
        if (!valid) {
            throw new ValidationException(this);
        }
    }

    /**
     * Throw exception if invalid, with custom message
     */
    public void throwIfInvalid(String message) {
        if (!valid) {
            throw new ValidationException(message, this);
        }
    }

    /**
     * Get all error messages as list
     */
    public List<String> getErrorMessages() {
        if (errors == null || errors.isEmpty()) {
            return Collections.emptyList();
        }
        return errors.stream()
                .map(ValidationError::getMessage)
                .toList();
    }

    /**
     * Get all warning messages as list
     */
    public List<String> getWarningMessages() {
        if (warnings == null || warnings.isEmpty()) {
            return Collections.emptyList();
        }
        return warnings.stream()
                .map(ValidationWarning::getMessage)
                .toList();
    }

    /**
     * Get errors by field
     */
    public List<ValidationError> getErrorsForField(String field) {
        if (errors == null || errors.isEmpty()) {
            return Collections.emptyList();
        }
        return errors.stream()
                .filter(e -> field.equals(e.getField()))
                .toList();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("ValidationResult{");
        sb.append("valid=").append(valid);
        
        if (hasErrors()) {
            sb.append(", errors=").append(errors.size());
        }
        
        if (hasWarnings()) {
            sb.append(", warnings=").append(warnings.size());
        }
        
        if (context != null) {
            sb.append(", context='").append(context).append('\'');
        }
        
        sb.append('}');
        return sb.toString();
    }
}