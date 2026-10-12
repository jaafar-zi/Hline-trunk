package com.example.demo.modules.product.application.dto.request;

import com.example.demo.common.base.dto.BaseRequest;
import com.example.demo.common.normalization.Normalize;
import com.example.demo.common.validator.constraints.ValidEnum;
import com.example.demo.modules.product.domain.model.vo.ImageType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating a product image
 *
 * @author PIM Team
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Update product image request")
public class UpdateProductImageRequest extends BaseRequest {

    private static final long serialVersionUID = 1L;

    @Schema(
        description = "Image type",
        example = "GALLERY",
        allowableValues = {"PRIMARY", "GALLERY", "THUMBNAIL", "LIFESTYLE", "DETAIL", "SPIN", "SIZE_CHART", "PACKAGING"}
    )
    @ValidEnum(enumClass = ImageType.class, message = "Invalid image type")
    @Normalize(Normalize.Strategy.UPPERCASE)
    private String imageType;

    @Schema(
        description = "Alt text for SEO and accessibility",
        example = "Premium wireless headphones in black color - side view"
    )
    @Size(max = 500, message = "Alt text cannot exceed 500 characters")
    @Normalize(Normalize.Strategy.TRIM)
    private String altText;

    @Schema(
        description = "Image title",
        example = "Premium Wireless Headphones - Side View"
    )
    @Size(max = 255, message = "Image title cannot exceed 255 characters")
    @Normalize(Normalize.Strategy.TRIM)
    private String title;

    @Schema(
        description = "Image caption",
        example = "High-quality wireless headphones with noise cancellation technology"
    )
    @Size(max = 1000, message = "Caption cannot exceed 1000 characters")
    @Normalize(Normalize.Strategy.TRIM)
    private String caption;

    @Schema(
        description = "Display order in gallery",
        example = "2"
    )
    @Min(value = 0, message = "Display order cannot be negative")
    @Max(value = 999, message = "Display order cannot exceed 999")
    private Integer displayOrder;

    @Schema(
        description = "Whether this image is active/visible",
        example = "true"
    )
    private Boolean active;

    @Schema(
        description = "Image tags for categorization",
        example = "front-view,black,premium"
    )
    @Size(max = 500, message = "Tags cannot exceed 500 characters")
    @Normalize(Normalize.Strategy.TRIM)
    private String tags;

    @Schema(
        description = "Image description for internal use",
        example = "Product shot taken in studio with professional lighting"
    )
    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    @Normalize(Normalize.Strategy.TRIM)
    private String description;

    @Schema(
        description = "Copyright information",
        example = "© 2024 Company Name. All rights reserved."
    )
    @Size(max = 255, message = "Copyright cannot exceed 255 characters")
    @Normalize(Normalize.Strategy.TRIM)
    private String copyright;

    @Schema(
        description = "Photographer/source credit",
        example = "John Doe Photography"
    )
    @Size(max = 255, message = "Credit cannot exceed 255 characters")
    @Normalize(Normalize.Strategy.TRIM)
    private String credit;

    // ====================================
    // Validation Groups
    // ====================================

    /**
     * Validation group for metadata updates
     */
    public interface MetadataUpdate {}

    /**
     * Validation group for display order updates
     */
    public interface DisplayOrderUpdate {}

    /**
     * Validation group for type changes
     */
    public interface TypeUpdate {}

    // ====================================
    // Custom Validation
    // ====================================

    @Override
    public void normalize() {
        super.normalize();

        // Auto-generate alt text from title if alt text is being cleared but title exists
        if ((altText == null || altText.isBlank()) && title != null && !title.isBlank()) {
            altText = title;
        }

        // Clean up tags format
        if (tags != null && !tags.isBlank()) {
            tags = tags.toLowerCase()
                      .replaceAll("\\s+", "-")  // Replace spaces with hyphens
                      .replaceAll("[^a-z0-9,-]", "")  // Remove invalid characters
                      .replaceAll(",+", ",")  // Remove duplicate commas
                      .replaceAll("^,|,$", "");  // Remove leading/trailing commas
        }
    }

    // ====================================
    // Helper Methods
    // ====================================

    /**
     * Check if this is a metadata-only update
     */
    public boolean isMetadataOnlyUpdate() {
        return imageType == null && displayOrder == null;
    }

    /**
     * Check if this is a display order update
     */
    public boolean isDisplayOrderUpdate() {
        return displayOrder != null;
    }

    /**
     * Check if this is a type change
     */
    public boolean isTypeChange() {
        return imageType != null;
    }

    /**
     * Check if any field is being updated
     */
    public boolean hasUpdates() {
        return imageType != null ||
               altText != null ||
               title != null ||
               caption != null ||
               displayOrder != null ||
               active != null ||
               tags != null ||
               description != null ||
               copyright != null ||
               credit != null;
    }

    /**
     * Get tags as array
     */
    public String[] getTagsArray() {
        if (tags == null || tags.isBlank()) {
            return new String[0];
        }
        return tags.split(",");
    }

    /**
     * Set tags from array
     */
    public void setTagsFromArray(String[] tagsArray) {
        if (tagsArray == null || tagsArray.length == 0) {
            this.tags = null;
        } else {
            this.tags = String.join(",", tagsArray);
        }
    }

    /**
     * Add tag
     */
    public void addTag(String tag) {
        if (tag == null || tag.isBlank()) {
            return;
        }

        String cleanTag = tag.trim().toLowerCase().replaceAll("\\s+", "-");
        
        if (this.tags == null || this.tags.isBlank()) {
            this.tags = cleanTag;
        } else if (!this.tags.contains(cleanTag)) {
            this.tags += "," + cleanTag;
        }
    }

    /**
     * Remove tag
     */
    public void removeTag(String tag) {
        if (tag == null || tag.isBlank() || this.tags == null) {
            return;
        }

        String cleanTag = tag.trim().toLowerCase().replaceAll("\\s+", "-");
        String[] currentTags = getTagsArray();
        
        StringBuilder newTags = new StringBuilder();
        for (String currentTag : currentTags) {
            if (!currentTag.equals(cleanTag)) {
                if (newTags.length() > 0) {
                    newTags.append(",");
                }
                newTags.append(currentTag);
            }
        }
        
        this.tags = newTags.length() > 0 ? newTags.toString() : null;
    }

    /**
     * Check if has specific tag
     */
    public boolean hasTag(String tag) {
        if (tag == null || tag.isBlank() || this.tags == null) {
            return false;
        }

        String cleanTag = tag.trim().toLowerCase().replaceAll("\\s+", "-");
        String[] currentTags = getTagsArray();
        
        for (String currentTag : currentTags) {
            if (currentTag.equals(cleanTag)) {
                return true;
            }
        }
        return false;
    }
}