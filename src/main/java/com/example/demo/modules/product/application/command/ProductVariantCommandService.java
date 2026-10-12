package com.example.demo.modules.product.application.command;

import com.example.demo.common.base.service.command.AbstractCommandService;
import com.example.demo.common.exception.EntityNotFoundException;
import com.example.demo.common.normalization.NormalizationService;
import com.example.demo.modules.product.application.dto.request.CreateProductVariantRequest;
import com.example.demo.modules.product.application.dto.request.UpdateProductVariantRequest;
import com.example.demo.modules.product.application.mapper.ProductVariantMapper;
import com.example.demo.modules.product.domain.model.Product;
import com.example.demo.modules.product.domain.model.ProductVariant;
import com.example.demo.modules.product.domain.repository.ProductRepository;
import com.example.demo.modules.product.domain.repository.ProductVariantRepository;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Product variant command service
 * 
 * Handles variant-specific operations on products
 * Extends AbstractCommandService for ProductVariant entity
 * 
 * @author PIM Team
 * @version 1.0
 */
@Slf4j
@Service
@Transactional
public class ProductVariantCommandService extends AbstractCommandService<ProductVariant, UUID> {

    private final ProductRepository productRepository;
    private final ProductVariantMapper variantMapper;

    public ProductVariantCommandService(
            ProductVariantRepository variantRepository,
            ProductRepository productRepository,
            ProductVariantMapper variantMapper,
            CacheManager cacheManager,
            ApplicationEventPublisher eventPublisher,
            Validator validator,
            NormalizationService normalizationService) {
        super(variantRepository, cacheManager, eventPublisher, validator, normalizationService);
        this.productRepository = productRepository;
        this.variantMapper = variantMapper;
    }

    // ====================================
    // Abstract Method Implementations
    // ====================================

    @Override
    protected List<String> getCacheNames() {
        return List.of("products", "product-variants");
    }

    @Override
    protected Class<ProductVariant> getEntityClass() {
        return ProductVariant.class;
    }

    // ====================================
    // Variant-Specific Operations
    // ====================================

    /**
     * Create variant for product
     */
    public ProductVariant createVariant(UUID productId, CreateProductVariantRequest request) {
        log.info("Creating variant for product: {}", productId);

        // Get parent product
        Product product = findProductOrThrow(productId);

        // Create variant using parent's createFromRequest method
        return createFromRequest(request, req -> {
            ProductVariant variant = variantMapper.fromCreateRequest(req);
            
            // Add to product (fires domain events)
            product.addVariant(variant);

            // Save product to persist the relationship
            productRepository.save(product);
            publishDomainEvents(variant);
            evictProductCache(productId);

            log.info("Variant created for product: {} with SKU: {}", productId, variant.getSku());
            return variant;
        });
    }

    /**
     * Update variant
     */
    public ProductVariant updateVariant(UUID variantId, UpdateProductVariantRequest request) {
        log.info("Updating variant: {}", variantId);

        ProductVariant entity = buildVariantFromUpdateRequest(variantId, request);
        return update(variantId, entity);
    }

    private ProductVariant buildVariantFromUpdateRequest(UUID variantId, UpdateProductVariantRequest request) {
        // 1. Pre-process
        request.preProcess();
        
        // 2. Normalize
        request.normalize();
        
        // 3. Validate request
        validateRequest(request).throwIfInvalid();
        
        // 4. Find existing variant
        ProductVariant existing = findVariantOrThrow(variantId);
        
        // 5. Update variant fields via mapper
        variantMapper.updateEntityFromRequest(request, existing);

        // 6. Evict product cache
        evictProductCache(existing.getProduct().getId());
        
        // 7. Post-process
        request.postProcess();

        log.info("Variant updated: {} (SKU: {})", variantId, existing.getSku());
        return existing;
    }

    /**
     * Delete variant from product
     */
    public void deleteVariant(UUID productId, UUID variantId) {
        log.info("Deleting variant {} from product: {}", variantId, productId);

        Product product = findProductOrThrow(productId);
        ProductVariant variant = findVariantOrThrow(variantId);
        
        // Verify variant belongs to product
        validateVariantBelongsToProduct(variant, productId);

        // Remove through domain logic
        product.removeVariant(variant.getSku());

        // Save product
        productRepository.save(product);
        publishDomainEvents(variant);

        // Delete the variant entity
        delete(variantId);
        evictProductCache(productId);

        log.info("Variant {} deleted from product: {}", variantId, productId);
    }

    // ====================================
    // Lifecycle Hooks
    // ====================================

    @Override
    protected void beforeCreate(ProductVariant entity) {
        log.debug("Preparing to create variant for product: {}", entity.getProduct().getId());
        
        // Validate variant belongs to a product
        if (entity.getProduct() == null) {
            throw new IllegalArgumentException("Variant must be associated with a product");
        }
        
        // Validate SKU is provided
        if (entity.getSku() == null || entity.getSku().toString().isBlank()) {
            throw new IllegalArgumentException("Variant SKU is required");
        }
        
        // Validate SKU uniqueness
        validateSkuUniqueness(entity);
        
        // Validate variant options
        validateVariantOptions(entity);
    }

    @Override
    protected void afterCreate(ProductVariant entity) {
        log.debug("Variant created successfully: {} (SKU: {})", 
                entity.getId(), entity.getSku());
        
        // Log variant details
        log.info("New variant created - SKU: {}, Product: {}, Options: {}", 
                entity.getSku(), 
                entity.getProduct().getId(),
                entity.getOptionValues().size());
    }

    @Override
    protected void beforeUpdate(ProductVariant existing, ProductVariant updated) {
        log.debug("Preparing to update variant: {} (SKU: {})", 
                existing.getId(), existing.getSku());
        
        // Validate SKU change if applicable
        if (!existing.getSku().equals(updated.getSku())) {
            log.warn("Variant SKU is changing from {} to {}", 
                    existing.getSku(), updated.getSku());
            validateSkuUniqueness(updated);
        }
        
        // Validate variant options
        validateVariantOptions(updated);
    }

    @Override
    protected void afterUpdate(ProductVariant entity) {
        log.debug("Variant updated successfully: {} (SKU: {})", 
                entity.getId(), entity.getSku());
        
        // Check if variant is active
        if (!entity.isActive()) {
            log.info("Variant {} is inactive", entity.getSku());
        }
    }

    @Override
    protected void beforeDelete(ProductVariant entity) {
        log.debug("Preparing to delete variant: {} (SKU: {})", 
                entity.getId(), entity.getSku());
        
        // Warn if deleting active variant
        if (entity.isActive()) {
            log.warn("Deleting active variant: {} from product: {}", 
                    entity.getSku(), entity.getProduct().getId());
        }
        
        // Check if variant has inventory
        if (entity.getInventory() != null && 
            entity.getInventory().getStockLevel() != null &&
            entity.getInventory().getStockLevel().getQuantity() > 0) {
            log.warn("Deleting variant with inventory: {} units", 
                    entity.getInventory().getStockLevel().getQuantity());
        }
    }

    @Override
    protected void afterDelete(ProductVariant entity) {
        log.debug("Variant deleted successfully: {} (SKU: {}) from product: {}", 
                entity.getId(), entity.getSku(), entity.getProduct().getId());
    }

    // ====================================
    // Validation Methods
    // ====================================

    /**
     * Validate variant belongs to product
     */
    private void validateVariantBelongsToProduct(ProductVariant variant, UUID productId) {
        if (!variant.getProduct().getId().equals(productId)) {
            throw new IllegalArgumentException(
                    String.format("Variant %s does not belong to product %s", 
                            variant.getId(), productId));
        }
    }

    /**
     * Validate SKU uniqueness
     */
    private void validateSkuUniqueness(ProductVariant variant) {
        // Check if SKU already exists
        var existingVariant = ((ProductVariantRepository) repository)
                .findBySku(variant.getSku());
        
        if (existingVariant.isPresent() && 
            !existingVariant.get().getId().equals(variant.getId())) {
            throw new IllegalArgumentException(
                    String.format("Variant with SKU '%s' already exists", variant.getSku()));
        }
    }

    /**
     * Validate variant options
     */
    private void validateVariantOptions(ProductVariant variant) {
        if (variant.getOptionValues() == null || variant.getOptionValues().isEmpty()) {
            log.warn("Variant {} has no option values defined", variant.getSku());
        }
        
        // Validate each option value
        variant.getOptionValues().forEach(optionValue -> {
            if (optionValue.getOptionName() == null || optionValue.getOptionName().isBlank()) {
                throw new IllegalArgumentException("Option name is required for all variant options");
            }
            if (optionValue.getValue() == null || optionValue.getValue().isBlank()) {
                throw new IllegalArgumentException(
                        String.format("Option value is required for option '%s'", 
                                optionValue.getOptionName()));
            }
        });
    }

    // ====================================
    // Helper Methods
    // ====================================

    private Product findProductOrThrow(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Product.class, id));
    }

    private ProductVariant findVariantOrThrow(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ProductVariant.class, id));
    }

    private void evictProductCache(UUID productId) {
        var cache = cacheManager.getCache("products");
        if (cache != null) {
            cache.evict("id:" + productId);
        }
    }
}