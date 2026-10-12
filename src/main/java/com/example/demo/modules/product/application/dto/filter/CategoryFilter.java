package com.example.demo.modules.product.application.dto.filter;

import com.example.demo.common.base.dto.BaseFilter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Filter DTO for Category queries
 *
 * @author PIM Team
 * @version 1.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Category filter criteria")
public class CategoryFilter extends BaseFilter {

    private static final long serialVersionUID = 1L;

    @Schema(
        description = "Search keyword (searches in name, code, description)",
        example = "electronics"
    )
    private String keyword;

    @Schema(
        description = "Category code",
        example = "ELECTRONICS"
    )
    private String code;

    @Schema(
        description = "Category name (partial match)",
        example = "Electronics"
    )
    private String name;

    @Schema(
        description = "Parent category ID (use null for root categories)",
        example = "550e8400-e29b-41d4-a716-446655440000"
    )
    private UUID parentId;

    @Schema(
        description = "Root categories only (no parent)",
        example = "true"
    )
    private Boolean rootOnly;

    @Schema(
        description = "Leaf categories only (no children)",
        example = "false"
    )
    private Boolean leafOnly;

    @Schema(
        description = "Active categories only",
        example = "true"
    )
    private Boolean activeOnly;

    @Schema(
        description = "Has children",
        example = "true"
    )
    private Boolean hasChildren;

    @Schema(
        description = "Created after date",
        example = "2024-01-01T00:00:00"
    )
    private LocalDateTime createdAfter;

    @Schema(
        description = "Created before date",
        example = "2024-12-31T23:59:59"
    )
    private LocalDateTime createdBefore;

    // Constructor for builder pattern compatibility
    @Builder
    public CategoryFilter(
            // BaseFilter fields
            Integer page,
            Integer size,
            String sortBy,
            String sortDirection,
            Boolean excludeDeleted,
            // CategoryFilter specific fields
            String keyword,
            String code,
            String name,
            UUID parentId,
            Boolean rootOnly,
            Boolean leafOnly,
            Boolean activeOnly,
            Boolean hasChildren,
            LocalDateTime createdAfter,
            LocalDateTime createdBefore
    ) {
        // Initialize parent fields
        this.page = page != null ? page : 0;
        this.size = size != null ? size : 50; // Category-specific default
        this.sortBy = sortBy != null ? sortBy : "displayOrder"; // Category-specific default
        this.sortDirection = sortDirection != null ? sortDirection : "ASC"; // Category-specific default
        this.excludeDeleted = excludeDeleted != null ? excludeDeleted : true;
        
        // Initialize category-specific fields
        this.keyword = keyword;
        this.code = code;
        this.name = name;
        this.parentId = parentId;
        this.rootOnly = rootOnly;
        this.leafOnly = leafOnly;
        this.activeOnly = activeOnly;
        this.hasChildren = hasChildren;
        this.createdAfter = createdAfter;
        this.createdBefore = createdBefore;
    }

    @Override
    public void normalize() {
        super.normalize(); // Call parent normalization
        
        // Normalize keyword
        if (keyword != null) {
            keyword = keyword.trim();
            if (keyword.isEmpty()) {
                keyword = null;
            }
        }
        
        // Normalize code
        if (code != null) {
            code = code.trim().toUpperCase();
            if (code.isEmpty()) {
                code = null;
            }
        }
        
        // Normalize name
        if (name != null) {
            name = name.trim();
            if (name.isEmpty()) {
                name = null;
            }
        }
        
        // Normalize sortBy to allowed values
        if (sortBy != null) {
            String normalizedSortBy = sortBy.trim().toLowerCase();
            // Validate against allowed values
            if (!isValidSortField(normalizedSortBy)) {
                sortBy = "displayOrder"; // Default fallback
            } else {
                sortBy = normalizedSortBy;
            }
        }
        
        // Validate date range
        if (createdAfter != null && createdBefore != null && createdAfter.isAfter(createdBefore)) {
            LocalDateTime temp = createdAfter;
            createdAfter = createdBefore;
            createdBefore = temp;
        }
        
        // Business logic: rootOnly and parentId are mutually exclusive
        if (Boolean.TRUE.equals(rootOnly) && parentId != null) {
            parentId = null; // rootOnly takes precedence
        }
    }

    @Override
    public boolean hasFilters() {
        return keyword != null 
            || code != null 
            || name != null
            || parentId != null 
            || rootOnly != null 
            || leafOnly != null
            || activeOnly != null 
            || hasChildren != null
            || createdAfter != null
            || createdBefore != null;
    }

    @Override
    protected int getMaxPageSize() {
        return 100; // Category-specific max page size
    }

    /**
     * Validate sort field against allowed values
     */
    private boolean isValidSortField(String field) {
        return field != null && (
            field.equals("name") ||
            field.equals("code") ||
            field.equals("displayorder") ||
            field.equals("createdat") ||
            field.equals("updatedat")
        );
    }

    /**
     * Check if filtering by parent
     */
    public boolean isFilteringByParent() {
        return parentId != null || Boolean.TRUE.equals(rootOnly);
    }

    /**
     * Check if filtering by hierarchy level
     */
    public boolean isFilteringByHierarchy() {
        return Boolean.TRUE.equals(rootOnly) || Boolean.TRUE.equals(leafOnly);
    }

    /**
     * Check if filtering by activity status
     */
    public boolean isFilteringByStatus() {
        return Boolean.TRUE.equals(activeOnly);
    }

    /**
     * Check if filtering by date range
     */
    public boolean hasDateRangeFilter() {
        return createdAfter != null || createdBefore != null;
    }

    /**
     * Get effective page size with category-specific default
     */
    @Override
    public int getEffectiveSize() {
        return size != null ? size : 50;
    }
}