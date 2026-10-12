package com.example.demo.common.base.controller;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Standard API response wrapper
 * 
 * @author Architecture Team
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "API response wrapper")
public class ApiResponse<T> {

    @Schema(description = "Response status", example = "true")
    private boolean success;

    @Schema(description = "Response message", example = "Operation completed successfully")
    private String message;

    @Schema(description = "Response data")
    private T data;

    @Schema(description = "Error details")
    private ErrorDetails errors;

    @Schema(description = "Response metadata")
    private Map<String, Object> metadata;

    @Schema(description = "Timestamp", example = "2024-01-20T10:30:00")
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    // ====================================
    // Factory Methods
    // ====================================

    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message("Success")
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .build();
    }

    public static <T> ApiResponse<T> error(String message, ErrorDetails errors) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .errors(errors)
                .build();
    }

    public static <T> ApiResponse<T> validationError(List<FieldError> fieldErrors) {
        return ApiResponse.<T>builder()
                .success(false)
                .message("Validation failed")
                .errors(ErrorDetails.builder()
                        .fieldErrors(fieldErrors)
                        .build())
                .build();
    }

    // ====================================
    // Error Details
    // ====================================

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "Error details")
    public static class ErrorDetails {
        
        @Schema(description = "Field-level errors")
        private List<FieldError> fieldErrors;

        @Schema(description = "Global errors")
        private List<String> globalErrors;

        @Schema(description = "Error code", example = "VALIDATION_ERROR")
        private String errorCode;

        @Schema(description = "Additional error details")
        private Map<String, Object> details;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Field error")
    public static class FieldError {
        
        @Schema(description = "Field name", example = "name")
        private String field;

        @Schema(description = "Error message", example = "Product name is required")
        private String message;

        @Schema(description = "Rejected value")
        private Object rejectedValue;

        @Schema(description = "Error code", example = "NotBlank")
        private String code;
    }
}