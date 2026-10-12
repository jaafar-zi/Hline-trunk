package com.example.demo.modules.product.application.dto.request;

import com.example.demo.common.base.dto.BaseRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Request DTO for updating an existing category
 *
 * All fields are optional - only provided fields will be updated
 *
 * @author PIM Team
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Update category request")
public class UpdateCategoryRequest extends BaseRequest {

    private static final long serialVersionUID = 1L;

    @Schema(
        description = "Category name",
        example = "Electronics & Gadgets"
    )
    @Size(min = 2, max = 255, message = "Category name must be between 2 and 255 characters")
    private String name;

    @Schema(
        description = "Category description",
        example = "Electronic devices, gadgets, and accessories"
    )
    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    @Schema(
        description = "Parent category ID (null to make root category)",
        example = "550e8400-e29b-41d4-a716-446655440000"
    )
    private UUID parentId;

    @Schema(
        description = "Display order",
        example = "2"
    )
    private Integer displayOrder;

    @Schema(
        description = "Category image URL",
        example = "https://cdn.example.com/categories/electronics-updated.jpg"
    )
    @Size(max = 2048, message = "Image URL cannot exceed 2048 characters")
    @Pattern(
        regexp = "^(https?://)?.*\\.(jpg|jpeg|png|webp|gif|svg)$",
        message = "Invalid image URL format",
        flags = Pattern.Flag.CASE_INSENSITIVE
    )
    private String imageUrl;

    @Schema(
        description = "Category icon",
        example = "fas fa-microchip"
    )
    @Size(max = 100, message = "Icon cannot exceed 100 characters")
    private String icon;

    @Schema(
        description = "Is category active",
        example = "true"
    )
    private Boolean active;

    @Schema(
        description = "SEO meta title",
        example = "Buy Electronics & Gadgets Online | Best Prices"
    )
    @Size(max = 255, message = "Meta title cannot exceed 255 characters")
    private String metaTitle;

    @Schema(
        description = "SEO meta description",
        example = "Discover the latest electronics and gadgets at unbeatable prices..."
    )
    @Size(max = 500, message = "Meta description cannot exceed 500 characters")
    private String metaDescription;

    @Schema(
        description = "SEO keywords",
        example = "electronics, gadgets, devices, technology"
    )
    @Size(max = 500, message = "SEO keywords cannot exceed 500 characters")
    private String seoKeywords;

    @Schema(
        description = "URL slug",
        example = "electronics-gadgets"
    )
    @Size(max = 255, message = "URL slug cannot exceed 255 characters")
    @Pattern(
        regexp = "^[a-z0-9-]*$",
        message = "URL slug must contain only lowercase letters, numbers, and hyphens"
    )
    private String urlSlug;

    @Override
    public void validate() {
        super.validate();

        // Trim string fields
        if (name != null) {
            name = name.trim();
        }

        if (description != null) {
            description = description.trim();
        }

        if (imageUrl != null) {
            imageUrl = imageUrl.trim();
        }

        if (icon != null) {
            icon = icon.trim();
        }

        if (metaTitle != null) {
            metaTitle = metaTitle.trim();
        }

        if (metaDescription != null) {
            metaDescription = metaDescription.trim();
        }

        if (seoKeywords != null) {
            seoKeywords = seoKeywords.trim();
        }

        // Normalize URL slug to lowercase
        if (urlSlug != null && !urlSlug.isBlank()) {
            urlSlug = urlSlug.trim().toLowerCase();
        }
    }
}