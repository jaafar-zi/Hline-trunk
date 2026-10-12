package com.example.demo.modules.product.application.command;

import com.example.demo.common.base.service.command.AbstractCommandService;
import com.example.demo.common.exception.EntityNotFoundException;
import com.example.demo.common.normalization.NormalizationService;
import com.example.demo.modules.product.application.dto.request.AddProductImageRequest;
import com.example.demo.modules.product.application.mapper.ProductImageMapper;
import com.example.demo.modules.product.domain.model.Product;
import com.example.demo.modules.product.domain.model.ProductImage;
import com.example.demo.modules.product.domain.repository.ProductImageRepository;
import com.example.demo.modules.product.domain.repository.ProductRepository;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Product image command service
 * 
 * Handles image-specific operations on products
 * Extends AbstractCommandService for ProductImage entity
 * 
 * @author PIM Team
 * @version 1.0
 */
@Slf4j
@Service
@Transactional
public class ProductImageCommandService extends AbstractCommandService<ProductImage, UUID> {

    private final ProductRepository productRepository;
    private final ProductImageMapper imageMapper;

    public ProductImageCommandService(
            ProductImageRepository imageRepository,
            ProductRepository productRepository,
            ProductImageMapper imageMapper,
            CacheManager cacheManager,
            ApplicationEventPublisher eventPublisher,
            Validator validator,
            NormalizationService normalizationService) {
        super(imageRepository, cacheManager, eventPublisher, validator, normalizationService);
        this.productRepository = productRepository;
        this.imageMapper = imageMapper;
    }

    // ====================================
    // Abstract Method Implementations
    // ====================================

    @Override
    protected List<String> getCacheNames() {
        return List.of("products", "product-images");
    }

    @Override
    protected Class<ProductImage> getEntityClass() {
        return ProductImage.class;
    }

    // ====================================
    // Image-Specific Operations
    // ====================================

    /**
     * Add image to product
     */
    public ProductImage addImage(UUID productId, AddProductImageRequest request) {
        log.info("Adding image to product: {}", productId);

        // Get product
        Product product = findProductOrThrow(productId);

        // Create image using parent's createFromRequest method
        return createFromRequest(request, req -> {
            ProductImage image = imageMapper.fromAddRequest(req);
            
            // Add to product (fires domain events)
            product.addImage(image);

            // Save product to persist the relationship
            productRepository.save(product);
            publishDomainEvents(image);
            evictProductCache(productId);

            log.info("Image added to product: {}", productId);
            return image;
        });
    }

    /**
     * Remove image from product
     */
    public void removeImage(UUID productId, UUID imageId) {
        log.info("Removing image {} from product: {}", imageId, productId);

        // Get product
        Product product = findProductOrThrow(productId);

        // Verify image belongs to product
        ProductImage image = findImageOrThrow(imageId);
        if (!image.getProduct().getId().equals(productId)) {
            throw new IllegalArgumentException("Image does not belong to this product");
        }

        // Remove through domain logic
        product.removeImage(imageId);

        // Save product
        productRepository.save(product);
        publishDomainEvents(image);

        // Delete the image entity
        delete(imageId);
        evictProductCache(productId);

        log.info("Image removed from product: {}", productId);
    }

    /**
     * Set primary image
     */
    public void setPrimaryImage(UUID productId, UUID imageId) {
        log.info("Setting primary image {} for product: {}", imageId, productId);

        // Get product
        Product product = findProductOrThrow(productId);

        // Verify image belongs to product
        ProductImage image = findImageOrThrow(imageId);
        if (!image.getProduct().getId().equals(productId)) {
            throw new IllegalArgumentException("Image does not belong to this product");
        }

        // Set primary image through domain logic (fires domain events)
        product.setPrimaryImage(imageId);

        // Save
        productRepository.save(product);
        publishDomainEvents(image);
        evictProductCache(productId);

        log.info("Primary image set for product: {}", productId);
    }

    /**
     * Update image display order
     */
    public void updateImageOrder(UUID productId, UUID imageId, int displayOrder) {
        log.info("Updating display order for image {} on product: {}", imageId, productId);

        ProductImage entity = buildImageForDisplayOrderUpdate(productId, imageId, displayOrder);
        update(imageId, entity);
    }

    private ProductImage buildImageForDisplayOrderUpdate(UUID productId, UUID imageId, int displayOrder) {
        // 1. Find existing image
        ProductImage existing = findImageOrThrow(imageId);
        
        // 2. Verify image belongs to product
        if (!existing.getProduct().getId().equals(productId)) {
            throw new IllegalArgumentException("Image does not belong to this product");
        }

        // 3. Update display order
        existing.setDisplayOrder(displayOrder);

        // 4. Evict product cache
        evictProductCache(productId);

        log.info("Image display order updated");
        
        return existing;
    }

    // ====================================
    // Lifecycle Hooks
    // ====================================

    @Override
    protected void beforeCreate(ProductImage entity) {
        log.debug("Preparing to create image for product: {}", entity.getProduct().getId());
        
        // Validate image belongs to a product
        if (entity.getProduct() == null) {
            throw new IllegalArgumentException("Image must be associated with a product");
        }
    }

    @Override
    protected void afterCreate(ProductImage entity) {
        log.debug("Image created successfully with ID: {}", entity.getId());
    }

    @Override
    protected void beforeDelete(ProductImage entity) {
        log.debug("Preparing to delete image: {}", entity.getId());
        
        // Prevent deletion of primary image without unset
        if (entity.isPrimary()) {
            log.warn("Deleting primary image: {}", entity.getId());
        }
    }

    @Override
    protected void afterDelete(ProductImage entity) {
        log.debug("Image deleted successfully: {}", entity.getId());
    }

    @Override
    protected void beforeUpdate(ProductImage existing, ProductImage updated) {
        log.debug("Preparing to update image: {}", existing.getId());
    }

    @Override
    protected void afterUpdate(ProductImage entity) {
        log.debug("Image updated successfully: {}", entity.getId());
    }

    // ====================================
    // Helper Methods
    // ====================================

    private Product findProductOrThrow(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Product.class, id));
    }

    private ProductImage findImageOrThrow(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ProductImage.class, id));
    }

    private void evictProductCache(UUID productId) {
        var cache = cacheManager.getCache("products");
        if (cache != null) {
            cache.evict("id:" + productId);
        }
    }
}