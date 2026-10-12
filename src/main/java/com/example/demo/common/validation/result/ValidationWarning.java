package com.example.demo.common.validation.result;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Validation warning (non-blocking issue)
 * 
 * @author Architecture Team
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidationWarning {

    /**
     * Field name
     */
    private String field;

    /**
     * Warning code (for i18n lookup)
     */
    private String code;

    /**
     * Human-readable warning message
     */
    private String message;

    /**
     * Warning severity
     */
    @Builder.Default
    private WarningSeverity severity = WarningSeverity.MEDIUM;

    /**
     * Suggested action
     */
    private String suggestedAction;

    // ====================================
    // Factory Methods
    // ====================================

    public static ValidationWarning of(String field, String message) {
        return ValidationWarning.builder()
                .field(field)
                .message(message)
                .build();
    }

    public static ValidationWarning of(String field, String code, String message) {
        return ValidationWarning.builder()
                .field(field)
                .code(code)
                .message(message)
                .build();
    }

    public enum WarningSeverity {
        LOW,
        MEDIUM,
        HIGH
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[WARNING] ");
        if (field != null) {
            sb.append(field).append(": ");
        }
        sb.append(message);
        if (suggestedAction != null) {
            sb.append(" (Suggestion: ").append(suggestedAction).append(")");
        }
        return sb.toString();
    }
}