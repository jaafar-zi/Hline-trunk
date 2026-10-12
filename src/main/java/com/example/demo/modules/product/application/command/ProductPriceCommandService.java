package com.example.demo.modules.product.application.command;

import com.example.demo.common.base.service.command.AbstractCommandService;
import com.example.demo.common.exception.EntityNotFoundException;
import com.example.demo.common.normalization.NormalizationService;
import com.example.demo.modules.product.application.dto.request.AddProductPriceRequest;
import com.example.demo.modules.product.application.dto.request.UpdatePriceRequest;
import com.example.demo.modules.product.application.mapper.ProductPriceMapper;
import com.example.demo.modules.product.domain.model.Product;
import com.example.demo.modules.product.domain.model.ProductPrice;
import com.example.demo.modules.product.domain.repository.ProductPriceRepository;
import com.example.demo.modules.product.domain.repository.ProductRepository;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Product price command service
 * 
 * Handles price-specific operations on products
 * Extends AbstractCommandService for ProductPrice entity
 * 
 * @author PIM Team
 * @version 3.0
 */
@Slf4j
@Service
@Transactional
public class ProductPriceCommandService extends AbstractCommandService<ProductPrice, UUID> {

    private final ProductRepository productRepository;
    private final ProductPriceMapper priceMapper;

    public ProductPriceCommandService(
            ProductPriceRepository priceRepository,
            ProductRepository productRepository,
            ProductPriceMapper priceMapper,
            CacheManager cacheManager,
            ApplicationEventPublisher eventPublisher,
            Validator validator,
            NormalizationService normalizationService) {
        super(priceRepository, cacheManager, eventPublisher, validator, normalizationService);
        this.productRepository = productRepository;
        this.priceMapper = priceMapper;
    }

    // ====================================
    // Abstract Method Implementations
    // ====================================

    @Override
    protected List<String> getCacheNames() {
        return List.of("products", "product-prices");
    }

    @Override
    protected Class<ProductPrice> getEntityClass() {
        return ProductPrice.class;
    }

    // ====================================
    // Price-Specific Operations
    // ====================================

    /**
     * Add price to product
     */
    public ProductPrice addPrice(UUID productId, AddProductPriceRequest request) {
        log.info("Adding price to product: {}", productId);

        // Get product
        Product product = findProductOrThrow(productId);

        // Create price using parent's createFromRequest method
        return createFromRequest(request, req -> {
            ProductPrice price = priceMapper.fromAddRequest(req);
            
            // Add to product (fires domain events)
            product.addPrice(price);

            // Save product to persist the relationship
            productRepository.save(product);
            publishDomainEvents(price);
            evictProductCache(productId);

            log.info("Price added to product: {} (amount: {}, currency: {})", 
                    productId, price.getPrice().getAmount(), price.getPrice().getCurrency());
            return price;
        });
    }

    /**
     * Update price
     */
    public ProductPrice updatePrice(UUID priceId, UpdatePriceRequest request) {
        log.info("Updating price: {}", priceId);

        ProductPrice entity = buildPriceFromUpdateRequest(priceId, request);
        return update(priceId, entity);
    }

    private ProductPrice buildPriceFromUpdateRequest(UUID priceId, UpdatePriceRequest request) {
        // 1. Pre-process
        request.preProcess();
        
        // 2. Normalize
        request.normalize();
        
        // 3. Validate request
        validateRequest(request).throwIfInvalid();
        
        // 4. Find existing price
        ProductPrice existing = findPriceOrThrow(priceId);
        
        // 5. Update price amount
        existing.getPrice().setAmount(request.getAmount());
        
        // 6. Update validity period
        if (request.getValidFrom() != null) {
            existing.setValidFrom(request.getValidFrom());
        }
        if (request.getValidTo() != null) {
            existing.setValidTo(request.getValidTo());
        }
        
        // 7. Update priority
        if (request.getPriority() != null) {
            existing.setPriority(request.getPriority());
        }
        
        // 8. Update tax rate
        if (request.getTaxRate() != null) {
            existing.setTaxRate(request.getTaxRate());
        }
        
        // 9. Update active status
        if (request.getActive() != null) {
            existing.setActive(request.getActive());
        }

        // 10. Evict product cache
        evictProductCache(existing.getProduct().getId());
        
        // 11. Post-process
        request.postProcess();

        log.info("Price updated: {} (new amount: {})", priceId, request.getAmount());
        return existing;
    }

    /**
     * Delete price (soft delete)
     */
    @Override
    public void delete(UUID priceId) {
        log.info("Deleting price: {}", priceId);

        ProductPrice price = findPriceOrThrow(priceId);
        UUID productId = price.getProduct().getId();

        // Soft delete through domain logic
        price.delete();
        
        // Save the soft-deleted entity
        repository.save(price);
        
        evictProductCache(productId);
        evictAllCaches();

        log.info("Price deleted: {}", priceId);
    }

    /**
     * Activate price
     */
    public ProductPrice activatePrice(UUID priceId) {
        log.info("Activating price: {}", priceId);

        ProductPrice price = findPriceOrThrow(priceId);
        
        // Validate price can be activated
        validatePriceActivation(price);
        
        price.setActive(true);

        ProductPrice updated = update(priceId, price);
        evictProductCache(price.getProduct().getId());

        log.info("Price activated: {} for product: {}", priceId, price.getProduct().getId());
        return updated;
    }

    /**
     * Deactivate price
     */
    public ProductPrice deactivatePrice(UUID priceId) {
        log.info("Deactivating price: {}", priceId);

        ProductPrice price = findPriceOrThrow(priceId);
        price.setActive(false);

        ProductPrice updated = update(priceId, price);
        evictProductCache(price.getProduct().getId());

        log.info("Price deactivated: {} for product: {}", priceId, price.getProduct().getId());
        return updated;
    }

    // ====================================
    // Lifecycle Hooks
    // ====================================

    @Override
    protected void beforeCreate(ProductPrice entity) {
        log.debug("Preparing to create price for product: {}", entity.getProduct().getId());
        
        // Validate price belongs to a product
        if (entity.getProduct() == null) {
            throw new IllegalArgumentException("Price must be associated with a product");
        }
        
        // Validate price amount
        if (entity.getPrice() == null || entity.getPrice().getAmount() == null) {
            throw new IllegalArgumentException("Price amount is required");
        }
        
        if (entity.getPrice().getAmount().signum() < 0) {
            throw new IllegalArgumentException("Price amount cannot be negative");
        }
        
        // Validate date range
        validateDateRange(entity);
    }

    @Override
    protected void afterCreate(ProductPrice entity) {
        log.debug("Price created successfully: {} for product: {}", 
                entity.getId(), entity.getProduct().getId());
        
        // Log price details
        log.info("New price: {} {} (valid from: {}, valid to: {})", 
                entity.getPrice().getAmount(), 
                entity.getPrice().getCurrency(),
                entity.getValidFrom(),
                entity.getValidTo());
    }

    @Override
    protected void beforeUpdate(ProductPrice existing, ProductPrice updated) {
        log.debug("Preparing to update price: {}", existing.getId());
        
        // Validate date range
        validateDateRange(updated);
        
        // Log price changes
        if (!existing.getPrice().equals(updated.getPrice())) {
            log.info("Price change: {} -> {} for product: {}", 
                    existing.getPrice().getAmount(), 
                    updated.getPrice().getAmount(),
                    existing.getProduct().getId());
        }
    }

    @Override
    protected void afterUpdate(ProductPrice entity) {
        log.debug("Price updated successfully: {}", entity.getId());
        
        // Check for expired prices
        if (entity.getValidTo() != null && entity.isExpired()) {
            log.warn("Price {} is expiring soon (valid to: {})", 
                    entity.getId(), entity.getValidTo());
        }
    }

    @Override
    protected void beforeDelete(ProductPrice entity) {
        log.debug("Preparing to delete price: {}", entity.getId());
        
        // Warn if deleting active price
        if (entity.isActive()) {
            log.warn("Deleting active price: {} for product: {}", 
                    entity.getId(), entity.getProduct().getId());
        }
    }

    @Override
    protected void afterDelete(ProductPrice entity) {
        log.debug("Price deleted successfully: {} for product: {}", 
                entity.getId(), entity.getProduct().getId());
    }

    // ====================================
    // Validation Methods
    // ====================================

    /**
     * Validate price date range
     */
    private void validateDateRange(ProductPrice price) {
        if (price.getValidFrom() != null && price.getValidTo() != null) {
            if (price.getValidTo().isBefore(price.getValidFrom())) {
                throw new IllegalArgumentException(
                        "Valid-to date must be after valid-from date");
            }
        }
    }

    /**
     * Validate price can be activated
     */
    private void validatePriceActivation(ProductPrice price) {
        // Check if price is already deleted
        if (price.isDeleted()) {
            throw new IllegalStateException(
                    String.format("Cannot activate deleted price: %s", price.getId()));
        }
        
        // Check if price is expired
        if (price.getValidTo() != null && price.getValidTo().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException(
                    String.format("Cannot activate expired price: %s (valid to: %s)", 
                            price.getId(), price.getValidTo()));
        }
        
        // Check if price is not yet valid
        if (price.getValidFrom() != null && price.getValidFrom().isAfter(LocalDateTime.now())) {
            log.warn("Activating future price: {} (valid from: {})", 
                    price.getId(), price.getValidFrom());
        }
    }

    // ====================================
    // Helper Methods
    // ====================================

    private Product findProductOrThrow(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Product.class, id));
    }

    private ProductPrice findPriceOrThrow(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ProductPrice.class, id));
    }

    private void evictProductCache(UUID productId) {
        var cache = cacheManager.getCache("products");
        if (cache != null) {
            cache.evict("id:" + productId);
        }
    }
}