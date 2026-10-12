package com.example.demo.common.base.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * Base class for all filter DTOs
 * 
 * Provides common filtering and pagination capabilities
 * 
 * @author Architecture Team
 * @version 2.0
 */
@Data
@Schema(description = "Base filter DTO")
public abstract class BaseFilter implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "Page number (0-based)", example = "0")
    protected Integer page = 0;

    @Schema(description = "Page size", example = "20")
    protected Integer size = 20;

    @Schema(description = "Sort field", example = "createdAt")
    protected String sortBy;

    @Schema(description = "Sort direction", example = "DESC", allowableValues = {"ASC", "DESC"})
    protected String sortDirection = "DESC";

    @Schema(description = "Exclude deleted records", example = "true")
    protected Boolean excludeDeleted = true;

    /**
     * Normalize filter values
     * Called BEFORE applying the filter
     */
    public void normalize() {
        // Normalize sort direction
        if (sortDirection != null) {
            sortDirection = sortDirection.trim().toUpperCase();
        }

        // Normalize sort field
        if (sortBy != null) {
            sortBy = sortBy.trim();
        }

        // Ensure valid pagination
        if (page == null || page < 0) {
            page = 0;
        }

        if (size == null || size < 1) {
            size = 20;
        }

        // Cap page size
        if (size > getMaxPageSize()) {
            size = getMaxPageSize();
        }
    }

    /**
     * Check if any filters are applied (beyond pagination)
     */
    public abstract boolean hasFilters();

    /**
     * Get maximum allowed page size
     */
    protected int getMaxPageSize() {
        return 100;
    }

    /**
     * Get effective page number
     */
    public int getEffectivePage() {
        return page != null ? page : 0;
    }

    /**
     * Get effective page size
     */
    public int getEffectiveSize() {
        return size != null ? size : 20;
    }
}