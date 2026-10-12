package com.example.demo.common.base.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Base class for all domain DTO responses
 * 
 * Provides common audit fields for domain entities
 * 
 * @author Architecture Team
 * @version 1.0
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Base DTO with audit fields")
public abstract class BaseDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(
        description = "Unique identifier",
        example = "550e8400-e29b-41d4-a716-446655440000"
    )
    protected UUID id;

    @Schema(
        description = "Creation timestamp",
        example = "2024-01-15T10:30:00"
    )
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    protected LocalDateTime createdAt;

    @Schema(
        description = "Last update timestamp",
        example = "2024-01-15T14:45:00"
    )
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    protected LocalDateTime updatedAt;

    @Schema(
        description = "User who created this record",
        example = "admin@example.com"
    )
    protected String createdBy;

    @Schema(
        description = "User who last updated this record",
        example = "user@example.com"
    )
    protected String updatedBy;

    @Schema(
        description = "Version number for optimistic locking",
        example = "1"
    )
    protected Long version;

    @Schema(
        description = "Soft delete flag",
        example = "false"
    )
    protected Boolean deleted;

    @Schema(
        description = "Deletion timestamp",
        example = "2024-01-20T16:00:00"
    )
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    protected LocalDateTime deletedAt;

    @Schema(
        description = "User who deleted this record",
        example = "admin@example.com"
    )
    protected String deletedBy;

    /**
     * Check if this entity is deleted
     */
    public boolean isDeleted() {
        return Boolean.TRUE.equals(deleted);
    }

    /**
     * Check if this entity is active (not deleted)
     */
    public boolean isActive() {
        return !isDeleted();
    }

    /**
     * Check if this entity has been modified
     */
    public boolean isModified() {
        return updatedAt != null && !updatedAt.equals(createdAt);
    }

    /**
     * Get the last modifier (updatedBy or createdBy)
     */
    public String getLastModifier() {
        return updatedBy != null ? updatedBy : createdBy;
    }

    /**
     * Get the last modification timestamp
     */
    public LocalDateTime getLastModifiedAt() {
        return updatedAt != null ? updatedAt : createdAt;
    }
}