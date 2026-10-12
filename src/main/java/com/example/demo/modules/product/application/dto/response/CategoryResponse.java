package com.example.demo.modules.product.application.dto.response;

import com.example.demo.common.base.dto.BaseDTO;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

/**
 * Lean Category Response - Performance optimized
 *
 * @author PIM Team
 * @version 1.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Category response - optimized")
public class CategoryResponse extends BaseDTO {

    private static final long serialVersionUID = 1L;

    @Schema(description = "Category code", example = "CAT-001", required = true)
    private String code;

    @Schema(description = "Category name", example = "Category A", required = true)
    private String name;

    @Schema(description = "Description")
    private String description;

    @Schema(description = "Parent category ID (use CategoryService for details)")
    private UUID parentId;

    @Schema(description = "Category path", example = "Root > Category A")
    private String path;

    @Schema(description = "Category level", example = "1")
    private Integer level;

    @Schema(description = "Display order", example = "1")
    private Integer displayOrder;

    @Schema(description = "Is active", example = "true")
    private Boolean active;

    @Schema(description = "Image URL")
    private String imageUrl;

    @Schema(description = "URL slug", example = "category-a")
    private String slug;

    @Schema(description = "Product count", example = "50")
    private Long productCount;

    @Schema(description = "Children count", example = "5")
    private Integer childrenCount;

    // Helper methods
    public boolean isRoot() {
        return parentId == null || level == 0;
    }

    public boolean isLeaf() {
        return childrenCount == null || childrenCount == 0;
    }

    public boolean hasProducts() {
        return productCount != null && productCount > 0;
    }

    public boolean isFullyActive() {
        return Boolean.TRUE.equals(active) && isActive();
    }

    @Override
    public String toString() {
        return String.format("CategoryResponse[id=%s, code=%s, name=%s]", id, code, name);
    }
}