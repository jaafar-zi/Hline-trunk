package com.example.demo.modules.product.application.command;

import com.example.demo.common.base.service.command.AbstractCommandService;
import com.example.demo.common.normalization.NormalizationService;
import com.example.demo.common.validation.result.ValidationResult;
import com.example.demo.common.validation.validator.ValidationContext;
import com.example.demo.modules.product.application.dto.request.*;
import com.example.demo.modules.product.application.mapper.ProductAttributeMapper;
import com.example.demo.modules.product.application.mapper.ProductImageMapper;
import com.example.demo.modules.product.application.mapper.ProductMapper;
import com.example.demo.modules.product.application.mapper.ProductPriceMapper;
import com.example.demo.modules.product.application.validator.ProductValidator;
import com.example.demo.modules.product.domain.model.Category;
import com.example.demo.modules.product.domain.model.Product;
import com.example.demo.modules.product.domain.repository.CategoryRepository;
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
 * Product command service
 * 
 * Handles all write operations for products
 * 
 * @author PIM Team
 * @version 1.0
 */
@Slf4j
@Service
@Transactional
public class ProductCommandService extends AbstractCommandService<Product, UUID> {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;
    private final ProductAttributeMapper attributeMapper;
    private final ProductPriceMapper priceMapper;
    private final ProductImageMapper productImageMapper;
    private final ProductValidator productValidator;

    public ProductCommandService(
            ProductRepository productRepository,
            CategoryRepository categoryRepository,
            CacheManager cacheManager,
            ApplicationEventPublisher eventPublisher,
            Validator validator,
            NormalizationService normalizationService,
            ProductImageMapper productImageMapper,
            ProductMapper productMapper,
            ProductAttributeMapper attributeMapper,
            ProductPriceMapper priceMapper,
            ProductValidator productValidator) {
        super(productRepository, cacheManager, eventPublisher, validator, normalizationService);
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.productMapper = productMapper;
        this.attributeMapper = attributeMapper;
        this.priceMapper = priceMapper;
        this.productImageMapper = productImageMapper;
        this.productValidator = productValidator;
    }

    @Override
    protected List<String> getCacheNames() {
        return List.of("products", "product-list", "product-search");
    }

    @Override
    protected Class<Product> getEntityClass() {
        return Product.class;
    }

    // ====================================
    // Product Creation
    // ====================================

    public Product createProduct(CreateProductRequest request) {
        log.info("Creating product: {}", request.getName());

        return createFromRequest(request, req -> {
            // Map request to entity
            Category category = categoryRepository.findById(req.getCategoryId())
                    .orElseThrow(() -> new IllegalArgumentException("Category not found"));

            Product product = productMapper.fromCreateRequest(req);
            
            return product;
        });
    }

    // ====================================
    // Product Updates
    // ====================================

    public Product updateProduct(UUID id, UpdateProductRequest request) {
        log.info("Updating product: {}", id);
        
        Product entity = buildProductFromUpdateRequest(id, request);
        return update(id, entity);
    }

    private Product buildProductFromUpdateRequest(UUID id, UpdateProductRequest request) {
        // 1. Pre-process
        request.preProcess();
        
        // 2. Normalize
        request.normalize();
        
        // 3. Validate request
        validateRequest(request).throwIfInvalid();
        
        // 4. Find existing product
        Product existing = findByIdOrThrow(id);
        
        // 5. Update entity from request
        productMapper.updateEntityFromRequest(request, existing);
        
        // 6. Update category if changed
        if (request.getCategoryId() != null && !request.getCategoryId().equals(existing.getCategory().getId())) {
            Category newCategory = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new IllegalArgumentException("Category not found"));
            existing.changeCategory(newCategory);
        }
        
        // 7. Post-process
        request.postProcess();
        
        return existing;
    }

    // ====================================
    // Publishing Operations
    // ====================================

    public void publishProduct(UUID id, PublishProductRequest request) {
        log.info("Publishing product: {}", id);

        // 1. Normalize & validate request
        request.normalize();
        ValidationResult requestValidation = validateRequest(request);
        requestValidation.throwIfInvalid();

        // 2. Find product
        Product product = findByIdOrThrow(id);

        // 3. Validate product for publishing (domain validation)
        ValidationContext context = ValidationContext.builder()
                .operation("PUBLISH")
                .build();
        
        ValidationResult domainValidation = productValidator.validate(product, context);
        
        if (!request.getForcePublish()) {
            domainValidation.throwIfInvalid();
        } else if (domainValidation.hasErrors()) {
            log.warn("Force publishing product {} with validation errors: {}", 
                    id, domainValidation.getErrorMessages());
        }

        // 4. Publish
        if (request.getAvailableFrom() != null || request.getAvailableUntil() != null) {
            product.setAvailabilityPeriod(request.getAvailableFrom(), request.getAvailableUntil());
        }
        
        product.publish();

        // 5. Save and evict cache
        productRepository.save(product);
        publishDomainEvents(product);
        evictAllCaches();
        evictSpecificCache(id);

        log.info("Product published: {}", id);
    }

    public void unpublishProduct(UUID id, UnpublishProductRequest request) {
        log.info("Unpublishing product: {}", id);

        request.normalize();
        validateRequest(request).throwIfInvalid();

        Product product = findByIdOrThrow(id);
        product.unpublish(request.getReason());

        productRepository.save(product);
        publishDomainEvents(product);
        evictAllCaches();
        evictSpecificCache(id);

        log.info("Product unpublished: {}", id);
    }

    public void discontinueProduct(UUID id, DiscontinueProductRequest request) {
        log.info("Discontinuing product: {}", id);

        request.normalize();
        validateRequest(request).throwIfInvalid();

        Product product = findByIdOrThrow(id);
        product.discontinue(request.getReason());

        productRepository.save(product);
        publishDomainEvents(product);
        evictAllCaches();
        evictSpecificCache(id);

        log.info("Product discontinued: {}", id);
    }

    // ====================================
    // Component Operations
    // ====================================

    public void addAttribute(UUID productId, AddProductAttributeRequest request) {
        log.info("Adding attribute to product: {}", productId);

        request.normalize();
        validateRequest(request).throwIfInvalid();

        Product product = findByIdOrThrow(productId);
        
        var attribute = attributeMapper.fromAddRequest(request);
        product.addAttribute(attribute);

        productRepository.save(product);
        publishDomainEvents(product);
        evictAllCaches();
        evictSpecificCache(productId);
    }

    public void addPrice(UUID productId, AddProductPriceRequest request) {
        log.info("Adding price to product: {}", productId);

        request.normalize();
        validateRequest(request).throwIfInvalid();

        Product product = findByIdOrThrow(productId);
        
        var price = priceMapper.fromAddRequest(request);
        product.addPrice(price);

        productRepository.save(product);
        publishDomainEvents(product);
        evictAllCaches();
        evictSpecificCache(productId);
    }

    public void addImage(UUID productId, AddProductImageRequest request) {
        log.info("Adding image to product: {}", productId);

        request.normalize();
        validateRequest(request).throwIfInvalid();

        Product product = findByIdOrThrow(productId);
        
        var image = productImageMapper.fromAddRequest(request);
        product.addImage(image);

        productRepository.save(product);
        publishDomainEvents(product);
        evictAllCaches();
        evictSpecificCache(productId);
    }

    // ====================================
    // Helper Methods
    // ====================================

    private Product findByIdOrThrow(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new com.example.demo.common.exception.EntityNotFoundException(Product.class, id));
    }

    @Override
    protected ValidationResult validateDomain(Product product) {
        return productValidator.validate(product);
    }
}