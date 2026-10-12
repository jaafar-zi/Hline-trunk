package com.example.demo.modules.product.application.command;

import com.example.demo.common.base.service.command.AbstractCommandService;
import com.example.demo.common.exception.EntityNotFoundException;
import com.example.demo.common.normalization.NormalizationService;
import com.example.demo.modules.product.application.dto.request.BulkUpdateInventoryRequest;
import com.example.demo.modules.product.application.dto.request.UpdateInventoryRequest;
import com.example.demo.modules.product.domain.model.Product;
import com.example.demo.modules.product.domain.model.ProductInventory;
import com.example.demo.modules.product.domain.repository.ProductInventoryRepository;
import com.example.demo.modules.product.domain.repository.ProductRepository;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Product inventory command service
 * 
 * Handles inventory-specific operations
 * 
 * @author PIM Team
 * @version 1.0
 */
@Slf4j
@Service
@Transactional
public class ProductInventoryCommandService extends AbstractCommandService<ProductInventory, UUID> {

    private final ProductRepository productRepository;
    private final ProductInventoryRepository inventoryRepository;

    public ProductInventoryCommandService(
            ProductInventoryRepository inventoryRepository,
            ProductRepository productRepository,
            CacheManager cacheManager,
            ApplicationEventPublisher eventPublisher,
            Validator validator,
            NormalizationService normalizationService) {
        super(inventoryRepository, cacheManager, eventPublisher, validator, normalizationService);
        this.inventoryRepository = inventoryRepository;
        this.productRepository = productRepository;
    }

    // ====================================
    // AbstractCommandService Implementation
    // ====================================

    @Override
    protected List<String> getCacheNames() {
        return List.of("products", "productInventories");
    }

    @Override
    protected Class<ProductInventory> getEntityClass() {
        return ProductInventory.class;
    }

    // ====================================
    // Inventory Operations
    // ====================================

    /**
     * Update inventory
     */
    public ProductInventory updateInventory(UUID productId, UpdateInventoryRequest request) {
        log.info("Updating inventory for product: {}", productId);

        // Normalize and validate using parent's validation
        request.preProcess();
        request.normalize();
        validateRequest(request).throwIfInvalid();

        // Get product with inventory
        Product product = findProductOrThrow(productId);

        // Update stock through aggregate
        product.updateStock(request.getQuantity(), request.getReason());

        ProductInventory inventory = product.getInventory();
        
        // Update inventory settings
        if (request.getLowStockThreshold() != null) {
            inventory.setLowStockThreshold(request.getLowStockThreshold());
        }
        if (request.getReorderPoint() != null) {
            inventory.setReorderPoint(request.getReorderPoint());
        }
        if (request.getReorderQuantity() != null) {
            inventory.setReorderQuantity(request.getReorderQuantity());
        }

        // Save product (cascade will save inventory)
        productRepository.save(product);
        
        // Publish events and evict caches using parent methods
        publishDomainEvents(inventory);
        evictAllCaches();
        evictProductCache(productId);

        request.postProcess();
        log.info("Inventory updated for product: {}", productId);
        return inventory;
    }

    /**
     * Bulk update inventory
     */
    public List<ProductInventory> bulkUpdateInventory(BulkUpdateInventoryRequest request) {
        log.info("Bulk updating inventory for {} products", request.getUpdates().size());

        // Normalize and validate
        request.preProcess();
        request.normalize();
        validateRequest(request).throwIfInvalid();

        List<ProductInventory> updatedInventories = new ArrayList<>();

        for (BulkUpdateInventoryRequest.InventoryUpdateItem item : request.getUpdates()) {
            Product product = findProductByIdOrSku(item);

            // Update stock through aggregate
            product.updateStock(item.getQuantity(), request.getReason());
            
            productRepository.save(product);
            publishDomainEvents(product.getInventory());
            evictProductCache(product.getId());

            updatedInventories.add(product.getInventory());
        }

        evictAllCaches();
        request.postProcess();

        log.info("Bulk inventory update completed for {} products", updatedInventories.size());
        return updatedInventories;
    }

    /**
     * Adjust stock (relative change)
     */
    public ProductInventory adjustStock(UUID productId, int adjustment, String reason) {
        log.info("Adjusting stock for product: {} by {}", productId, adjustment);

        Product product = findProductOrThrow(productId);
        
        if (product.getInventory() == null) {
            throw new IllegalStateException("Product has no inventory tracking");
        }

        int currentQuantity = product.getInventory().getStockLevel().getQuantity();
        int newQuantity = currentQuantity + adjustment;

        if (newQuantity < 0) {
            throw new IllegalArgumentException(
                    String.format("Stock adjustment would result in negative quantity: %d", newQuantity));
        }

        product.updateStock(newQuantity, reason);

        productRepository.save(product);
        publishDomainEvents(product.getInventory());
        evictAllCaches();
        evictProductCache(productId);

        log.info("Stock adjusted for product: {}", productId);
        return product.getInventory();
    }

    /**
     * Reserve stock
     */
    public void reserveStock(UUID productId, int quantity) {
        log.info("Reserving {} units for product: {}", quantity, productId);

        Product product = findProductOrThrow(productId);
        
        if (product.getInventory() == null) {
            throw new IllegalStateException("Product has no inventory tracking");
        }

        product.getInventory().reserve(quantity);

        productRepository.save(product);
        publishDomainEvents(product.getInventory());
        evictAllCaches();
        evictProductCache(productId);

        log.info("Stock reserved for product: {}", productId);
    }

    /**
     * Release reserved stock
     */
    public void releaseStock(UUID productId, int quantity) {
        log.info("Releasing {} reserved units for product: {}", quantity, productId);

        Product product = findProductOrThrow(productId);
        
        if (product.getInventory() == null) {
            throw new IllegalStateException("Product has no inventory tracking");
        }

        product.getInventory().releaseReservation(quantity);

        productRepository.save(product);
        publishDomainEvents(product.getInventory());
        evictAllCaches();
        evictProductCache(productId);

        log.info("Reserved stock released for product: {}", productId);
    }

    // ====================================
    // Helper Methods
    // ====================================

    private Product findProductOrThrow(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Product.class, id));
    }

    private Product findProductByIdOrSku(BulkUpdateInventoryRequest.InventoryUpdateItem item) {
        if (item.getProductId() != null) {
            return findProductOrThrow(item.getProductId());
        } else if (item.getSku() != null) {
            return productRepository.findBySku(item.getSku())
                    .orElseThrow(() -> EntityNotFoundException.byField(Product.class, "SKU", item.getSku()));
        } else {
            throw new IllegalArgumentException("Either productId or SKU must be provided");
        }
    }

    private void evictProductCache(UUID productId) {
        var productsCache = cacheManager.getCache("products");
        if (productsCache != null) {
            productsCache.evict("id:" + productId);
        }
    }
}