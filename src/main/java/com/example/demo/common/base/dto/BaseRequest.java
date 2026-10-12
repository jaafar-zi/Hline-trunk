package com.example.demo.common.base.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serializable;
import java.util.UUID;

/**
 * Enhanced base class for all request DTOs
 *
 * Now separates normalization from validation
 *
 * @author Architecture Team
 * @version 2.0
 */
@Data
@Schema(description = "Base request DTO")
public abstract class BaseRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(
            description = "Request ID for tracing",
            example = "550e8400-e29b-41d4-a716-446655440000"
    )
    private String requestId;

    @Schema(
            description = "Client request timestamp",
            example = "1706097600000"
    )
    private Long timestamp;

    @Schema(
            description = "Idempotency key for duplicate prevention",
            example = "idm_550e8400e29b41d4a716446655440000"
    )
    private String idempotencyKey;

    @Schema(
            description = "Client application identifier",
            example = "mobile-app-v1.0"
    )
    private String clientId;

    @Schema(
            description = "Client application version",
            example = "1.0.5"
    )
    private String clientVersion;

    @JsonIgnore
    private String clientIp;

    @JsonIgnore
    private String userAgent;

    // ====================================
    // Lifecycle Methods (Template Pattern)
    // ====================================

    /**
     * Normalize request data
     * Called BEFORE validation
     * Should NOT throw exceptions
     */
    public void normalize() {
        // Default implementation - override in subclasses
        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString();
        }
        if (timestamp == null) {
            timestamp = System.currentTimeMillis();
        }
    }

    /**
     * Validate request (override in subclasses for custom validation)
     * Called AFTER normalization
     * 
     * @deprecated Use declarative validation (annotations) instead
     * This method will be removed in future versions
     * Keep for backward compatibility only
     */
    @Deprecated(since = "2.0", forRemoval = true)
    public void validate() {
        // Legacy support - do nothing by default
        // Validation should be done via annotations and validators
    }

    /**
     * Pre-process hook called before normalization
     */
    public void preProcess() {
        // Override in subclasses if needed
    }

    /**
     * Post-process hook called after validation
     */
    public void postProcess() {
        // Override in subclasses if needed
    }

    // ====================================
    // Helper Methods
    // ====================================

    @JsonIgnore
    public boolean isFresh(long maxAgeSeconds) {
        if (timestamp == null) {
            return false;
        }
        long currentTime = System.currentTimeMillis();
        long ageSeconds = (currentTime - timestamp) / 1000;
        return ageSeconds <= maxAgeSeconds;
    }

    @JsonIgnore
    public long getAgeSeconds() {
        if (timestamp == null) {
            return -1;
        }
        return (System.currentTimeMillis() - timestamp) / 1000;
    }
}