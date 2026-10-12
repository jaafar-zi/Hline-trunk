package com.example.demo.modules.product.application.query;

import com.example.demo.common.annotation.ReadOnly;
import com.example.demo.common.base.service.query.AbstractQueryService;
import com.example.demo.common.normalization.NormalizationService;
import com.example.demo.modules.product.application.dto.response.ProductImageResponse;
import com.example.demo.modules.product.application.mapper.ProductImageMapper;
import com.example.demo.modules.product.application.mapper.ProductMapper;
import com.example.demo.modules.product.domain.model.ProductImage;
import com.example.demo.modules.product.domain.model.vo.ImageType;
import com.example.demo.modules.product.domain.repository.ProductImageRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Product image query service
 * 
 * Handles all read operations for product images
 * 
 * @author PIM Team
 * @version 3.0
 */
@Slf4j
@Service
@Transactional(readOnly = true)
public class ProductImageQueryService extends AbstractQueryService<ProductImage, UUID> {

    private static final Set<String> VALID_SORT_FIELDS = Set.of(
        "displayOrder", "createdAt", "updatedAt", "imageType"
    );

    private final ProductImageRepository imageRepository;
    private final ProductImageMapper imageMapper;

    public ProductImageQueryService(
            ProductImageRepository imageRepository,
            NormalizationService normalizationService,
            ProductImageMapper imageMapper) {
        super(imageRepository, normalizationService);
        this.imageRepository = imageRepository;
        this.imageMapper = imageMapper;
    }

    @Override
    protected String getEntityCacheName() {
        return "product-images";
    }

    @Override
    protected Class<ProductImage> getEntityClass() {
        return ProductImage.class;
    }

    @Override
    protected boolean isValidSortField(String field) {
        return VALID_SORT_FIELDS.contains(field);
    }

    @Override
    protected String getDefaultSortField() {
        return "displayOrder";
    }

    // ====================================
    // Specific Queries
    // ====================================

    /**
     * Get image by ID
     */
    @ReadOnly
    public ProductImageResponse getImageById(UUID id) {
        log.debug("Getting image by ID: {}", id);

        ProductImage image = findById(id);
        return imageMapper.toResponse(image);
    }

    /**
     * Get all images for product
     */
    @ReadOnly
    public List<ProductImageResponse> getImagesForProduct(UUID productId) {
        log.debug("Getting images for product: {}", productId);

        List<ProductImage> images = imageRepository.findByProductIdOrderByDisplayOrderAsc(productId);

        return images.stream()
                .map(imageMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get primary image for product
     */
    @ReadOnly
    public ProductImageResponse getPrimaryImage(UUID productId) {
        log.debug("Getting primary image for product: {}", productId);

        ProductImage image = imageRepository.findByProductIdAndImageType(productId, ImageType.PRIMARY)
                .get(0);

        return image != null ? imageMapper.toResponse(image) : null;
    }

    /**
     * Get gallery images for product
     */
    @ReadOnly
    public List<ProductImageResponse> getGalleryImages(UUID productId) {
        log.debug("Getting gallery images for product: {}", productId);

        List<ProductImage> images = imageRepository.findByProductIdAndImageType(productId, ImageType.GALLERY);

        return images.stream()
                .sorted((a, b) -> Integer.compare(a.getDisplayOrder(), b.getDisplayOrder()))
                .map(imageMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get thumbnail images for product
     */
    @ReadOnly
    public List<ProductImageResponse> getThumbnailImages(UUID productId) {
        log.debug("Getting thumbnail images for product: {}", productId);

        List<ProductImage> images = imageRepository.findByProductIdAndImageType(productId, ImageType.THUMBNAIL);

        return images.stream()
                .map(imageMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get images by type
     */
    @ReadOnly
    public List<ProductImageResponse> getImagesByType(UUID productId, ImageType imageType) {
        log.debug("Getting {} images for product: {}", imageType, productId);

        List<ProductImage> images = imageRepository.findByProductIdAndImageType(productId, imageType);

        return images.stream()
                .map(imageMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get images by type (string)
     */
    @ReadOnly
    public List<ProductImageResponse> getImagesByType(UUID productId, String imageType) {
        log.debug("Getting {} images for product: {}", imageType, productId);

        try {
            ImageType type = ImageType.valueOf(imageType.toUpperCase());
            return getImagesByType(productId, type);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid image type: {}", imageType);
            return List.of();
        }
    }

    /**
     * Get first N images for product
     */
    @ReadOnly
    public List<ProductImageResponse> getFirstImages(UUID productId, int limit) {
        log.debug("Getting first {} images for product: {}", limit, productId);

        List<ProductImage> images = imageRepository.findByProductIdOrderByDisplayOrderAsc(productId);

        return images.stream()
                .limit(limit)
                .map(imageMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get image by URL
     */
    @ReadOnly
    public ProductImageResponse getImageByUrl(UUID productId, String url) {
        log.debug("Getting image by URL for product: {}", productId);

        ProductImage image = imageRepository.findByProductIdAndUrl(productId, url).get();

        return image != null ? imageMapper.toResponse(image) : null;
    }

    /**
     * Check if product has images
     */
    @ReadOnly
    public boolean hasImages(UUID productId) {
        log.debug("Checking if product has images: {}", productId);

        return imageRepository.existsByProductId(productId);
    }

    /**
     * Check if product has primary image
     */
    @ReadOnly
    public boolean hasPrimaryImage(UUID productId) {
        log.debug("Checking if product has primary image: {}", productId);

        return imageRepository.existsByProductIdAndImageType(productId, ImageType.PRIMARY);
    }

    /**
     * Count images for product
     */
    @ReadOnly
    public long countImagesForProduct(UUID productId) {
        log.debug("Counting images for product: {}", productId);

        return imageRepository.countByProductId(productId);
    }

    /**
     * Count images by type for product
     */
    @ReadOnly
    public long countImagesByType(UUID productId, ImageType imageType) {
        log.debug("Counting {} images for product: {}", imageType, productId);

        return imageRepository.countByProductIdAndImageType(productId, imageType);
    }

    /**
     * Get image statistics for product
     */
    @ReadOnly
    public ImageStatistics getImageStatistics(UUID productId) {
        log.debug("Getting image statistics for product: {}", productId);

        long total = imageRepository.countByProductId(productId);
        long primary = imageRepository.countByProductIdAndImageType(productId, ImageType.PRIMARY);
        long gallery = imageRepository.countByProductIdAndImageType(productId, ImageType.GALLERY);
        long thumbnail = imageRepository.countByProductIdAndImageType(productId, ImageType.THUMBNAIL);
        long detail = imageRepository.countByProductIdAndImageType(productId, ImageType.DETAIL);

        return ImageStatistics.builder()
                .totalImages(total)
                .primaryImages(primary)
                .galleryImages(gallery)
                .thumbnailImages(thumbnail)
                .detailImages(detail)
                .hasPrimaryImage(primary > 0)
                .build();
    }

    /**
     * Get all images (across all products)
     */
    @ReadOnly
    public List<ProductImageResponse> getAllImages() {
        log.debug("Getting all product images");

        List<ProductImage> images = findAll();

        return images.stream()
                .map(imageMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Search images by file name
     */
    @ReadOnly
    public List<ProductImageResponse> searchByFileName(String fileName) {
        log.debug("Searching images by file name: {}", fileName);

        List<ProductImage> images = imageRepository.findByFileNameContainingIgnoreCase(fileName);

        return images.stream()
                .map(imageMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Search images by alt text
     */
    @ReadOnly
    public List<ProductImageResponse> searchByAltText(String altText) {
        log.debug("Searching images by alt text: {}", altText);

        List<ProductImage> images = imageRepository.findByAltTextContainingIgnoreCase(altText);

        return images.stream()
                .map(imageMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get images with missing alt text
     */
    @ReadOnly
    public List<ProductImageResponse> getImagesWithMissingAltText(UUID productId) {
        log.debug("Getting images with missing alt text for product: {}", productId);

        List<ProductImage> images = imageRepository.findByProductIdOrderByDisplayOrderAsc(productId);

        return images.stream()
                .filter(img -> img.getAltText() == null || img.getAltText().isBlank())
                .map(imageMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get large images (by file size)
     */
    @ReadOnly
    public List<ProductImageResponse> getLargeImages(UUID productId, long minFileSize) {
        log.debug("Getting large images (> {} bytes) for product: {}", minFileSize, productId);

        List<ProductImage> images = imageRepository.findByProductIdAndFileSizeGreaterThan(productId, minFileSize);

        return images.stream()
                .map(imageMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get images by dimensions
     */
    @ReadOnly
    public List<ProductImageResponse> getImagesByDimensions(UUID productId, Integer minWidth, Integer minHeight) {
        log.debug("Getting images with dimensions >= {}x{} for product: {}", minWidth, minHeight, productId);

        List<ProductImage> images = imageRepository.findByProductIdOrderByDisplayOrderAsc(productId);

        return images.stream()
                .filter(img -> {
                    boolean widthMatch = minWidth == null || (img.getWidth() != null && img.getWidth() >= minWidth);
                    boolean heightMatch = minHeight == null || (img.getHeight() != null && img.getHeight() >= minHeight);
                    return widthMatch && heightMatch;
                })
                .map(imageMapper::toResponse)
                .collect(Collectors.toList());
    }

    // ====================================
    // Statistics DTO
    // ====================================

    @lombok.Data
    @lombok.Builder
    public static class ImageStatistics {
        private long totalImages;
        private long primaryImages;
        private long galleryImages;
        private long thumbnailImages;
        private long detailImages;
        private boolean hasPrimaryImage;
    }
}