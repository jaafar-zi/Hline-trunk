package com.example.demo.modules.product.application.dto.response;

import com.example.demo.common.base.dto.BaseDTO;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

/**
 * Lean Product Image Response - Performance optimized
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
@Schema(description = "Product image response - optimized")
public class ProductImageResponse extends BaseDTO {

    private static final long serialVersionUID = 1L;

    // ====================================
    // Reference
    // ====================================

    @Schema(description = "Product ID reference")
    private UUID productId;

    @Schema(description = "Variant ID reference (if variant-specific)")
    private UUID variantId;

    // ====================================
    // Core Image Data
    // ====================================

    @Schema(description = "Image URL", required = true)
    private String url;

    @Schema(description = "Thumbnail URL")
    private String thumbnailUrl;

    @Schema(description = "Image type", example = "PRIMARY")
    private String imageType;

    @Schema(description = "Alt text for accessibility")
    private String altText;

    @Schema(description = "Image title")
    private String title;

    @Schema(description = "Display order", example = "1")
    private Integer displayOrder;

    // ====================================
    // Basic Metadata
    // ====================================

    @Schema(description = "MIME type", example = "image/jpeg")
    private String mimeType;

    @Schema(description = "File size in bytes", example = "2048576")
    private Long fileSize;

    @Schema(description = "Image width", example = "1200")
    private Integer width;

    @Schema(description = "Image height", example = "800")
    private Integer height;

    // ====================================
    // Helper Methods
    // ====================================

    public boolean isPrimary() {
        return "PRIMARY".equals(imageType);
    }

    public boolean hasValidDimensions() {
        return width != null && height != null && width > 0 && height > 0;
    }

    public String getDisplayUrl() {
        return url != null ? url : thumbnailUrl;
    }

    @Override
    public String toString() {
        return String.format("ProductImageResponse[id=%s, type=%s, url=%s]", 
            id, imageType, url);
    }
}