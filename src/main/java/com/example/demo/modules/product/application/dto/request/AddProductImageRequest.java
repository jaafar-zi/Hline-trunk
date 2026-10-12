package com.example.demo.modules.product.application.dto.request;

import com.example.demo.common.base.dto.BaseRequest;
import com.example.demo.common.normalization.Normalize;
import com.example.demo.common.validator.constraints.ValidEnum;
import com.example.demo.common.validator.constraints.ValidImageUrl;
import com.example.demo.modules.product.domain.model.vo.ImageType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Request DTO for adding a product image
 *
 * @author PIM Team
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Add product image request")
public class AddProductImageRequest extends BaseRequest {

    private static final long serialVersionUID = 1L;

    @Schema(
        description = "Image URL",
        example = "https://cdn.example.com/products/headphones-001.jpg",
        required = true
    )
    @NotBlank(message = "Image URL is required")
    @ValidImageUrl(requireHttps = false, allowedExtensions = {"jpg", "jpeg", "png", "webp", "gif"})
    @Normalize(Normalize.Strategy.TRIM)
    private String url;

    @Schema(
        description = "Image file path (if stored locally)",
        example = "/uploads/products/2024/01/headphones-001.jpg"
    )
    @Size(max = 1000, message = "File path cannot exceed 1000 characters")
    @Normalize(Normalize.Strategy.TRIM)
    private String filePath;

    @Schema(
        description = "Image file name",
        example = "headphones-001.jpg"
    )
    @Size(max = 255, message = "File name cannot exceed 255 characters")
    @Normalize(Normalize.Strategy.TRIM)
    private String fileName;

    @Schema(
        description = "Image type",
        example = "PRIMARY",
        allowableValues = {"PRIMARY", "GALLERY", "THUMBNAIL", "DETAIL"},
        required = true
    )
    @NotBlank(message = "Image type is required")
    @ValidEnum(enumClass = ImageType.class, message = "Invalid image type")
    @Normalize(Normalize.Strategy.UPPERCASE)
    private String imageType;

    @Schema(
        description = "Alt text for SEO and accessibility",
        example = "Premium wireless headphones in black color"
    )
    @Size(max = 500, message = "Alt text cannot exceed 500 characters")
    @Normalize(Normalize.Strategy.TRIM)
    private String altText;

    @Schema(
        description = "Image title",
        example = "Premium Wireless Headphones - Front View"
    )
    @Size(max = 255, message = "Image title cannot exceed 255 characters")
    @Normalize(Normalize.Strategy.TRIM)
    private String title;

    @Schema(
        description = "Display order",
        example = "1"
    )
    @Min(value = 0, message = "Display order cannot be negative")
    private Integer displayOrder = 0;

    @Schema(
        description = "Image width in pixels",
        example = "1920"
    )
    @Min(value = 1, message = "Width must be positive")
    @Max(value = 10000, message = "Width cannot exceed 10000 pixels")
    private Integer width;

    @Schema(
        description = "Image height in pixels",
        example = "1080"
    )
    @Min(value = 1, message = "Height must be positive")
    @Max(value = 10000, message = "Height cannot exceed 10000 pixels")
    private Integer height;

    @Schema(
        description = "File size in bytes",
        example = "524288"
    )
    @Min(value = 0, message = "File size cannot be negative")
    private Long fileSize;

    @Schema(
        description = "MIME type",
        example = "image/jpeg"
    )
    @Size(max = 100, message = "MIME type cannot exceed 100 characters")
    @Normalize(Normalize.Strategy.TRIM)
    private String mimeType;

    @Override
    public void normalize() {
        super.normalize();

        // Auto-generate alt text from title if not provided
        if ((altText == null || altText.isBlank()) && title != null && !title.isBlank()) {
            altText = title;
        }
    }
}