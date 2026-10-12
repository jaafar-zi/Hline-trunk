package com.example.demo.modules.product.domain.model;

import com.example.demo.common.base.entity.SoftDeletableAggregateRoot;
import com.example.demo.common.event.DomainEvent;
import com.example.demo.common.event.EntityUpdatedEvent;
import com.example.demo.common.util.ChangeTracker;
import com.example.demo.modules.product.domain.model.vo.ImageType;
import jakarta.persistence.*;
import lombok.*;

/**
 * Product Image Entity
 *
 * Represents product images and media assets
 * Part of Product aggregate
 *
 * Business Rules:
 * - Each product must have at most one PRIMARY image
 * - Image URLs must be valid
 * - Display order controls image carousel sequence
 * - Alt text required for accessibility
 *
 * Events Published:
 * - EntityUpdatedEvent<ProductImage> (with change tracking)
 *
 * @author PIM Team
 * @version 1.0
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "product_images",
        indexes = {
                @Index(name = "idx_product_image_product", columnList = "product_id"),
                @Index(name = "idx_product_image_type", columnList = "product_id, image_type"),
                @Index(name = "idx_product_image_order", columnList = "display_order")
        }
)
public class ProductImage extends SoftDeletableAggregateRoot {

    // ====================================
    // Core Information
    // ====================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Column(name = "original_file_name", length = 255)
    private String originalFileName;

    @Column(name = "url", nullable = false, length = 1000)
    private String url;

    @Column(name = "thumbnail_url", length = 1000)
    private String thumbnailUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "image_type", nullable = false, length = 50)
    @Builder.Default
    private ImageType imageType = ImageType.GALLERY;

    @Column(name = "display_order")
    @Builder.Default
    private Integer displayOrder = 0;

    // ====================================
    // Image Metadata
    // ====================================

    @Column(name = "alt_text", length = 500)
    private String altText;

    @Column(name = "title", length = 255)
    private String title;

    @Column(name = "caption", length = 1000)
    private String caption;

    @Column(name = "mime_type", length = 100)
    private String mimeType;

    @Column(name = "file_size")
    private Long fileSize; // in bytes

    @Column(name = "width")
    private Integer width;

    @Column(name = "height")
    private Integer height;

    // ====================================
    // Storage Information
    // ====================================

    @Column(name = "storage_path", length = 1000)
    private String storagePath;

    @Column(name = "storage_provider", length = 50)
    private String storageProvider; // e.g., "S3", "local", "cloudinary"

    @Column(name = "cdn_url", length = 1000)
    private String cdnUrl;

    // ====================================
    // Factory Methods
    // ====================================

    /**
     * Create product image
     *
     * Events Published:
     * - None (event published by Product aggregate when image is added)
     */
    public static ProductImage create(
            String fileName,
            String url,
            ImageType imageType
    ) {
        validateFileName(fileName);
        validateUrl(url);

        return ProductImage.builder()
                .fileName(fileName)
                .originalFileName(fileName)
                .url(url)
                .imageType(imageType != null ? imageType : ImageType.GALLERY)
                .displayOrder(0)
                .build();
    }

    /**
     * Create with full metadata
     */
    public static ProductImage createWithMetadata(
            String fileName,
            String url,
            String altText,
            ImageType imageType,
            Integer width,
            Integer height,
            Long fileSize
    ) {
        ProductImage image = create(fileName, url, imageType);
        image.altText = altText;
        image.width = width;
        image.height = height;
        image.fileSize = fileSize;
        return image;
    }

    // ====================================
    // Domain Logic - Metadata Updates
    // ====================================

    /**
     * Update image metadata
     *
     * Events Published:
     * - EntityUpdatedEvent<ProductImage> (with change tracking)
     */
    public void updateMetadata(String altText, String title, String caption) {
        ChangeTracker tracker = new ChangeTracker();

        if (altText != null && !altText.equals(this.altText)) {
            tracker.track("altText", this.altText, altText);
            this.altText = altText;
        }

        if (title != null && !title.equals(this.title)) {
            tracker.track("title", this.title, title);
            this.title = title;
        }

        if (caption != null && !caption.equals(this.caption)) {
            tracker.track("caption", this.caption, caption);
            this.caption = caption;
        }

        if (tracker.hasChanges()) {
            registerEvent(new EntityUpdatedEvent<>(this, tracker.getChanges()));
        }
    }

    /**
     * Update image type
     */
    public void updateImageType(ImageType newType) {
        if (newType == null) {
            throw new IllegalArgumentException("Image type cannot be null");
        }

        if (this.imageType != newType) {
            ChangeTracker tracker = new ChangeTracker();
            tracker.track("imageType", this.imageType, newType);

            this.imageType = newType;

            registerEvent(new EntityUpdatedEvent<>(this, tracker.getChanges()));
        }
    }

    /**
     * Update display order
     */
    public void updateDisplayOrder(Integer displayOrder) {
        if (displayOrder != null && !displayOrder.equals(this.displayOrder)) {
            ChangeTracker tracker = new ChangeTracker();
            tracker.track("displayOrder", this.displayOrder, displayOrder);

            this.displayOrder = displayOrder;

            registerEvent(new EntityUpdatedEvent<>(this, tracker.getChanges()));
        }
    }

    /**
     * Update URLs
     */
    public void updateUrls(String url, String thumbnailUrl, String cdnUrl) {
        ChangeTracker tracker = new ChangeTracker();

        if (url != null && !url.equals(this.url)) {
            validateUrl(url);
            tracker.track("url", this.url, url);
            this.url = url;
        }

        if (thumbnailUrl != null && !thumbnailUrl.equals(this.thumbnailUrl)) {
            tracker.track("thumbnailUrl", this.thumbnailUrl, thumbnailUrl);
            this.thumbnailUrl = thumbnailUrl;
        }

        if (cdnUrl != null && !cdnUrl.equals(this.cdnUrl)) {
            tracker.track("cdnUrl", this.cdnUrl, cdnUrl);
            this.cdnUrl = cdnUrl;
        }

        if (tracker.hasChanges()) {
            registerEvent(new EntityUpdatedEvent<>(this, tracker.getChanges()));
        }
    }

    /**
     * Update dimensions
     */
    public void updateDimensions(Integer width, Integer height) {
        ChangeTracker tracker = new ChangeTracker();

        if (width != null && !width.equals(this.width)) {
            if (width <= 0) {
                throw new IllegalArgumentException("Width must be positive");
            }
            tracker.track("width", this.width, width);
            this.width = width;
        }

        if (height != null && !height.equals(this.height)) {
            if (height <= 0) {
                throw new IllegalArgumentException("Height must be positive");
            }
            tracker.track("height", this.height, height);
            this.height = height;
        }

        if (tracker.hasChanges()) {
            registerEvent(new EntityUpdatedEvent<>(this, tracker.getChanges()));
        }
    }

    /**
     * Update storage information
     */
    public void updateStorageInfo(String storagePath, String storageProvider, String mimeType, Long fileSize) {
        ChangeTracker tracker = new ChangeTracker();

        tracker.track("storagePath", this.storagePath, storagePath);
        tracker.track("storageProvider", this.storageProvider, storageProvider);
        tracker.track("mimeType", this.mimeType, mimeType);

        if (fileSize != null && fileSize > 0) {
            tracker.track("fileSize", this.fileSize, fileSize);
            this.fileSize = fileSize;
        }

        this.storagePath = storagePath;
        this.storageProvider = storageProvider;
        this.mimeType = mimeType;

        if (tracker.hasChanges()) {
            registerEvent(new EntityUpdatedEvent<>(this, tracker.getChanges()));
        }
    }

    // ====================================
    // Query Methods
    // ====================================

    /**
     * Check if this is the primary image
     */
    @Transient
    public boolean isPrimary() {
        return imageType == ImageType.PRIMARY;
    }

    /**
     * Check if image has thumbnail
     */
    @Transient
    public boolean hasThumbnail() {
        return thumbnailUrl != null && !thumbnailUrl.isBlank();
    }

    /**
     * Get effective URL (CDN if available, otherwise regular URL)
     */
    @Transient
    public String getEffectiveUrl() {
        return (cdnUrl != null && !cdnUrl.isBlank()) ? cdnUrl : url;
    }

    /**
     * Get aspect ratio
     */
    @Transient
    public Double getAspectRatio() {
        if (width != null && height != null && height > 0) {
            return (double) width / height;
        }
        return null;
    }

    /**
     * Get file size in KB
     */
    @Transient
    public Double getFileSizeInKB() {
        if (fileSize != null) {
            return fileSize / 1024.0;
        }
        return null;
    }

    /**
     * Get file size in MB
     */
    @Transient
    public Double getFileSizeInMB() {
        if (fileSize != null) {
            return fileSize / (1024.0 * 1024.0);
        }
        return null;
    }

    /**
     * Check if image is landscape
     */
    @Transient
    public boolean isLandscape() {
        return width != null && height != null && width > height;
    }

    /**
     * Check if image is portrait
     */
    @Transient
    public boolean isPortrait() {
        return width != null && height != null && height > width;
    }

    /**
     * Check if image is square
     */
    @Transient
    public boolean isSquare() {
        return width != null && height != null && width.equals(height);
    }

    /**
     * Get formatted file size
     */
    @Transient
    public String getFormattedFileSize() {
        if (fileSize == null) {
            return "Unknown";
        }

        if (fileSize < 1024) {
            return fileSize + " B";
        } else if (fileSize < 1024 * 1024) {
            return String.format("%.2f KB", fileSize / 1024.0);
        } else {
            return String.format("%.2f MB", fileSize / (1024.0 * 1024.0));
        }
    }

    // ====================================
    // Validation
    // ====================================

    private static void validateFileName(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            throw new IllegalArgumentException("File name cannot be null or empty");
        }
        if (fileName.length() > 255) {
            throw new IllegalArgumentException("File name cannot exceed 255 characters");
        }
    }

    private static void validateUrl(String url) {
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("Image URL cannot be null or empty");
        }
        if (url.length() > 1000) {
            throw new IllegalArgumentException("Image URL cannot exceed 1000 characters");
        }
        // Basic URL validation
        if (!url.matches("^(https?|ftp)://.*$")) {
            throw new IllegalArgumentException("Invalid URL format");
        }
    }

    /**
     * Validate image meets minimum requirements
     */
    public void validateQuality() {
        if (width != null && width < 800) {
            throw new IllegalArgumentException("Image width must be at least 800px for product images");
        }
        if (height != null && height < 600) {
            throw new IllegalArgumentException("Image height must be at least 600px for product images");
        }
        if (fileSize != null && fileSize > 5 * 1024 * 1024) { // 5MB
            throw new IllegalArgumentException("Image file size cannot exceed 5MB");
        }
        if (altText == null || altText.isBlank()) {
            throw new IllegalArgumentException("Alt text is required for accessibility");
        }
    }

    // ====================================
    // Helper Methods
    // ====================================

    /**
     * Clone image for variant or duplication
     */
    public ProductImage clone() {
        return ProductImage.builder()
                .fileName(this.fileName)
                .originalFileName(this.originalFileName)
                .url(this.url)
                .thumbnailUrl(this.thumbnailUrl)
                .imageType(this.imageType)
                .displayOrder(this.displayOrder)
                .altText(this.altText)
                .title(this.title)
                .caption(this.caption)
                .mimeType(this.mimeType)
                .fileSize(this.fileSize)
                .width(this.width)
                .height(this.height)
                .storagePath(this.storagePath)
                .storageProvider(this.storageProvider)
                .cdnUrl(this.cdnUrl)
                .build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProductImage)) return false;
        ProductImage that = (ProductImage) o;
        return getId() != null && getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    protected DomainEvent createDeletedEvent(String deletedBy) {
        return null;
    }

    @Override
    protected DomainEvent createRestoredEvent(String restoredBy, String previousDeletedBy) {
        return null;
    }

    @Override
    public String toString() {
        return String.format("ProductImage[id=%s, fileName=%s, type=%s, order=%d]",
                getId(), fileName, imageType, displayOrder);
    }
}