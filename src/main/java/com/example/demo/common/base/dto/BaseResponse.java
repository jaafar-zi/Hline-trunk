package com.example.demo.common.base.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Standard API response wrapper
 *
 * Provides consistent response structure across all endpoints
 *
 * @param <T> Response data type
 * @author Architecture Team
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Standard API response wrapper")
public class BaseResponse<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "Indicates if the request was successful", example = "true")
    private Boolean success;

    @Schema(description = "Response message", example = "Operation completed successfully")
    private String message;

    @Schema(description = "Response data payload")
    private T data;

    @Schema(description = "Response timestamp", example = "2024-01-24T10:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    @Schema(description = "Request ID for tracing", example = "550e8400-e29b-41d4-a716-446655440000")
    private String requestId;

    @Schema(description = "Additional metadata")
    private Object metadata;

    @Schema(description = "Error code (only present on errors)", example = "USER_NOT_FOUND")
    private String errorCode;

    // ====================================
    // Factory Methods - Success
    // ====================================

    public static <T> BaseResponse<T> success(T data) {
        return BaseResponse.<T>builder()
                .success(true)
                .message("Success")
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> BaseResponse<T> success(T data, String message) {
        return BaseResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> BaseResponse<T> success(T data, String message, Object metadata) {
        return BaseResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .metadata(metadata)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static BaseResponse<Void> success() {
        return BaseResponse.<Void>builder()
                .success(true)
                .message("Success")
                .timestamp(LocalDateTime.now())
                .build();
    }

    // ====================================
    // Factory Methods - Error
    // ====================================

    public static <T> BaseResponse<T> error(String message) {
        return BaseResponse.<T>builder()
                .success(false)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> BaseResponse<T> error(String errorCode, String message) {
        return BaseResponse.<T>builder()
                .success(false)
                .errorCode(errorCode)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> BaseResponse<T> error(String errorCode, String message, T data) {
        return BaseResponse.<T>builder()
                .success(false)
                .errorCode(errorCode)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    // ====================================
    // Builder Helpers
    // ====================================

    public BaseResponse<T> withRequestId(String requestId) {
        this.requestId = requestId;
        return this;
    }

    public BaseResponse<T> withMetadata(Object metadata) {
        this.metadata = metadata;
        return this;
    }
}