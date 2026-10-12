package com.example.demo.modules.product.application.command;

import com.example.demo.common.base.service.command.AbstractCommandService;
import com.example.demo.common.event.EntityUpdatedEvent;
import com.example.demo.common.exception.EntityNotFoundException;
import com.example.demo.common.normalization.NormalizationService;
import com.example.demo.modules.product.application.dto.request.AddProductAttributeRequest;
import com.example.demo.modules.product.application.dto.request.UpdateProductAttributeRequest;
import com.example.demo.modules.product.application.mapper.ProductAttributeMapper;
import com.example.demo.modules.product.domain.model.Product;
import com.example.demo.modules.product.domain.model.ProductAttribute;
import com.example.demo.modules.product.domain.repository.ProductAttributeRepository;
import com.example.demo.modules.product.domain.repository.ProductRepository;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Product attribute command service
 * 
 * Handles attribute-specific operations on products
 * Extends AbstractCommandService for ProductAttribute entity
 * 
 * @author PIM Team
 * @version 1.0
 */
@Slf4j
@Service
@Transactional
public class ProductAttributeCommandService extends AbstractCommandService<ProductAttribute, UUID> {

    private final ProductAttributeRepository attributeRepository;
    private final ProductRepository productRepository;
    private final ProductAttributeMapper attributeMapper;

    public ProductAttributeCommandService(
            ProductAttributeRepository attributeRepository,
            ProductRepository productRepository,
            ProductAttributeMapper attributeMapper,
            CacheManager cacheManager,
            ApplicationEventPublisher eventPublisher,
            Validator validator,
            NormalizationService normalizationService) {
        super(attributeRepository, cacheManager, eventPublisher, validator, normalizationService);
        this.attributeRepository = attributeRepository;
        this.productRepository = productRepository;
        this.attributeMapper = attributeMapper;
    }

    // ====================================
    // Abstract Method Implementations
    // ====================================

    @Override
    protected List<String> getCacheNames() {
        return List.of("products", "product-attributes");
    }

    @Override
    protected Class<ProductAttribute> getEntityClass() {
        return ProductAttribute.class;
    }

    // ====================================
    // Attribute Creation
    // ====================================

    /**
     * Add attribute to product
     */
    public ProductAttribute addAttribute(UUID productId, AddProductAttributeRequest request) {
        log.info("Adding attribute '{}' to product: {}", request.getKey(), productId);

        ProductAttribute entity = buildAttributeFromAddRequest(productId, request);
        return create(entity);
    }

    private ProductAttribute buildAttributeFromAddRequest(UUID productId, AddProductAttributeRequest request) {
        // 1. Pre-process
        request.preProcess();
        
        // 2. Normalize
        request.normalize();
        
        // 3. Validate request
        validateRequest(request).throwIfInvalid();
        
        // 4. Get product
        Product product = findProductOrThrow(productId);
        
        // 5. Check if attribute already exists
        if (product.hasAttribute(request.getKey())) {
            throw new IllegalArgumentException(
                String.format("Attribute '%s' already exists on product", request.getKey()));
        }
        
        // 6. Map to entity
        ProductAttribute attribute = attributeMapper.fromAddRequest(request);
        
        // 7. Add to product (fires domain events)
        product.addAttribute(attribute);
        
        // 8. Save product to persist the relationship
        productRepository.save(product);
        
        // 9. Evict cache
        evictProductCache(productId);
        
        // 10. Post-process
        request.postProcess();
        
        return attribute;
    }

    // ====================================
    // Attribute Updates
    // ====================================

    /**
     * Update attribute value
     */
    public ProductAttribute updateAttribute(UUID productId, String key, UpdateProductAttributeRequest request) {
        log.info("Updating attribute '{}' for product: {}", key, productId);

        // Find the attribute first to get its ID
        Product product = findProductOrThrow(productId);
        ProductAttribute attribute = product.getAttribute(key);
        
        if (attribute == null) {
            throw new EntityNotFoundException(ProductAttribute.class, key);
        }

        // Build updated entity
        ProductAttribute entity = buildAttributeFromUpdateRequest(productId, key, attribute.getId(), request);
        
        return update(attribute.getId(), entity);
    }

    private ProductAttribute buildAttributeFromUpdateRequest(
            UUID productId, 
            String key, 
            UUID attributeId, 
            UpdateProductAttributeRequest request) {
        
        // 1. Pre-process
        request.preProcess();
        
        // 2. Normalize
        request.normalize();
        
        // 3. Validate request
        validateRequest(request).throwIfInvalid();
        
        // 4. Get product
        Product product = findProductOrThrow(productId);
        
        // 5. Get existing attribute
        ProductAttribute existing = product.getAttribute(key);
        if (existing == null) {
            throw new EntityNotFoundException(ProductAttribute.class, key);
        }
        
        // 6. Update through product domain logic
        product.updateAttribute(key, request.getValue());
        
        // 7. Update display order if provided
        if (request.getDisplayOrder() != null) {
            existing.setDisplayOrder(request.getDisplayOrder());
        }
        
        // 8. Save product
        productRepository.save(product);
        publishDomainEvents(existing);
        
        // 9. Evict cache
        evictProductCache(productId);
        
        // 10. Post-process
        request.postProcess();
        
        return existing;
    }

    // ====================================
    // Attribute Deletion
    // ====================================

    /**
     * Remove attribute from product
     */
    public void removeAttribute(UUID productId, String key) {
        log.info("Removing attribute '{}' from product: {}", key, productId);

        // Get product
        Product product = findProductOrThrow(productId);

        // Get the attribute to get its ID
        ProductAttribute attribute = product.getAttribute(key);
        if (attribute == null) {
            throw new EntityNotFoundException(ProductAttribute.class, key);
        }

        // Remove through domain logic
        product.removeAttribute(key);

        // Save product
        productRepository.save(product);
        publishDomainEvents(attribute);
        
        // Delete the attribute entity
        delete(attribute.getId());
        
        // Evict cache
        evictProductCache(productId);

        log.info("Attribute '{}' removed from product: {}", key, productId);
    }

    // ====================================
    // Bulk Operations
    // ====================================

    /**
     * Bulk update attributes
     */
    public void bulkUpdateAttributes(UUID productId, Map<String, String> attributes) {
        log.info("Bulk updating {} attributes for product: {}", attributes.size(), productId);

        Product product = findProductOrThrow(productId);

        attributes.forEach((key, value) -> {
            if (product.hasAttribute(key)) {
                // Update existing attribute
                product.updateAttribute(key, value);
                
                ProductAttribute attribute = product.getAttribute(key);
                publishDomainEvents(attribute);
                eventPublisher.publishEvent(new EntityUpdatedEvent(attribute));
                
            } else {
                // Create new attribute
                AddProductAttributeRequest request = buildAddAttributeRequest(key, value);
                
                // Validate
                validateRequest(request).throwIfInvalid();
                
                ProductAttribute attribute = attributeMapper.fromAddRequest(request);
                product.addAttribute(attribute);
                publishDomainEvents(attribute);
            }
        });

        productRepository.save(product);
        evictProductCache(productId);

        log.info("Bulk attribute update completed for product: {}", productId);
    }

    /**
     * Bulk add attributes to product
     */
    public List<ProductAttribute> bulkAddAttributes(UUID productId, List<AddProductAttributeRequest> requests) {
        log.info("Bulk adding {} attributes to product: {}", requests.size(), productId);

        Product product = findProductOrThrow(productId);
        
        // Build all entities
        List<ProductAttribute> attributes = requests.stream()
                .map(request -> {
                    // Validate each request
                    request.normalize();
                    validateRequest(request).throwIfInvalid();
                    
                    // Check for duplicates
                    if (product.hasAttribute(request.getKey())) {
                        throw new IllegalArgumentException(
                            String.format("Attribute '%s' already exists", request.getKey()));
                    }
                    
                    // Map to entity
                    ProductAttribute attribute = attributeMapper.fromAddRequest(request);
                    product.addAttribute(attribute);
                    
                    return attribute;
                })
                .toList();
        
        // Save product once
        productRepository.save(product);
        
        // Publish events
        attributes.forEach(this::publishDomainEvents);
        
        evictProductCache(productId);
        evictAllCaches();

        log.info("Bulk added {} attributes to product: {}", attributes.size(), productId);
        return attributes;
    }

    // ====================================
    // Lifecycle Hooks
    // ====================================

    @Override
    protected void beforeCreate(ProductAttribute entity) {
        log.debug("Preparing to create attribute: {}", entity.getKey());
    }

    @Override
    protected void afterCreate(ProductAttribute entity) {
        log.debug("Attribute created successfully: {}", entity.getKey());
    }

    @Override
    protected void beforeUpdate(ProductAttribute existing, ProductAttribute updated) {
        log.debug("Preparing to update attribute: {}", existing.getKey());
    }

    @Override
    protected void afterUpdate(ProductAttribute entity) {
        log.debug("Attribute updated successfully: {}", entity.getKey());
    }

    @Override
    protected void beforeDelete(ProductAttribute entity) {
        log.debug("Preparing to delete attribute: {}", entity.getKey());
        // Add any pre-delete validation or cleanup here
    }

    @Override
    protected void afterDelete(ProductAttribute entity) {
        log.debug("Attribute deleted successfully: {}", entity.getKey());
    }

    // ====================================
    // Helper Methods
    // ====================================

    private Product findProductOrThrow(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Product.class, id));
    }

    private void evictProductCache(UUID productId) {
        var cache = cacheManager.getCache("products");
        if (cache != null) {
            cache.evict("id:" + productId);
        }
    }

    private AddProductAttributeRequest buildAddAttributeRequest(String key, String value) {
        AddProductAttributeRequest request = new AddProductAttributeRequest();
        request.setKey(key);
        request.setValue(value);
        request.setRequired(false);
        request.normalize();
        return request;
    }
}