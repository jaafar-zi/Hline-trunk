package com.example.demo.modules.product.application.dto.request;

import com.example.demo.common.base.dto.BaseRequest;
import com.example.demo.common.normalization.Normalize;
import com.example.demo.common.validator.constraints.ValidImageUrl;
import com.example.demo.common.validator.constraints.ValidSlug;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Request DTO for creating a new category
 *
 * @author PIM Team
 * @version 2.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Create category request")
public class CreateCategoryRequest extends BaseRequest {

    private static final long serialVersionUID = 1L;

    @Schema(
        description = "Category code (unique identifier)",
        example = "ELECTRONICS",
        required = true
    )
    @NotBlank(message = "Category code is required")
    @Size(min = 2, max = 50, message = "Category code must be between 2 and 50 characters")
    @Pattern(
        regexp = "^[A-Z0-9_-]+$",
        message = "Category code must contain only uppercase letters, numbers, underscores, and hyphens"
    )
    @Normalize(Normalize.Strategy.UPPERCASE)
    private String code;

    @Schema(
        description = "Category name",
        example = "Electronics",
        required = true
    )
    @NotBlank(message = "Category name is required")
    @Size(min = 2, max = 255, message = "Category name must be between 2 and 255 characters")
    @Normalize(Normalize.Strategy.TRIM)
    private String name;

    @Schema(
        description = "Category description",
        example = "Electronic devices and accessories"
    )
    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    @Normalize(Normalize.Strategy.TRIM)
    private String description;

    @Schema(
        description = "Parent category ID (null for root categories)",
        example = "550e8400-e29b-41d4-a716-446655440000"
    )
    private UUID parentId;

    @Schema(
        description = "Display order",
        example = "1"
    )
    private Integer displayOrder = 0;

    @Schema(
        description = "Category image URL",
        example = "https://cdn.example.com/categories/electronics.jpg"
    )
    @ValidImageUrl(allowedExtensions = {"jpg", "jpeg", "png", "webp", "gif", "svg"})
    @Normalize(Normalize.Strategy.TRIM)
    private String imageUrl;

    @Schema(
        description = "Category icon (CSS class or icon identifier)",
        example = "fas fa-laptop"
    )
    @Size(max = 100, message = "Icon cannot exceed 100 characters")
    @Normalize(Normalize.Strategy.TRIM)
    private String icon;

    @Schema(
        description = "Is category active",
        example = "true",
        defaultValue = "true"
    )
    private Boolean active = true;

    @Schema(
        description = "SEO meta title",
        example = "Buy Electronics Online | Best Prices"
    )
    @Size(max = 255, message = "Meta title cannot exceed 255 characters")
    @Normalize(Normalize.Strategy.TRIM)
    private String metaTitle;

    @Schema(
        description = "SEO meta description",
        example = "Shop the latest electronics at competitive prices..."
    )
    @Size(max = 500, message = "Meta description cannot exceed 500 characters")
    @Normalize(Normalize.Strategy.TRIM)
    private String metaDescription;

    @Schema(
        description = "SEO keywords",
        example = "electronics, gadgets, devices"
    )
    @Size(max = 500, message = "SEO keywords cannot exceed 500 characters")
    @Normalize(Normalize.Strategy.TRIM)
    private String seoKeywords;

    @Schema(
        description = "URL slug (auto-generated from name if not provided)",
        example = "electronics"
    )
    @ValidSlug(allowNull = true)
    @Normalize(Normalize.Strategy.SLUG)
    private String urlSlug;

    @Override
    public void normalize() {
        super.normalize();

        // Auto-generate URL slug from name if not provided
        if ((urlSlug == null || urlSlug.isBlank()) && name != null && !name.isBlank()) {
            urlSlug = generateSlug(name);
        }
    }

    private String generateSlug(String name) {
        return name.toLowerCase()
            .replaceAll("[^a-z0-9\\s-]", "")
            .replaceAll("\\s+", "-")
            .replaceAll("-+", "-")
            .replaceAll("^-|-$", "")
            .trim();
    }
}