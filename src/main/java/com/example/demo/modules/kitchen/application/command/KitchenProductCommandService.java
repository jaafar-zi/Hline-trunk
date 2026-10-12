package com.example.demo.modules.kitchen.application.command;

import com.example.demo.common.base.service.command.AbstractCommandService;
import com.example.demo.common.exception.EntityNotFoundException;
import com.example.demo.common.normalization.NormalizationService;
import com.example.demo.modules.kitchen.domain.model.*;
import com.example.demo.modules.kitchen.domain.model.vo.AssemblyType;
import com.example.demo.modules.kitchen.domain.repository.*;
import com.example.demo.modules.product.domain.model.vo.Money;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Kitchen Product Command Service
 *
 * Handles all write operations for KitchenProduct and KitchenProductVariant.
 */
@Slf4j
@Service
@Transactional
public class KitchenProductCommandService extends AbstractCommandService<KitchenProduct, UUID> {

    private final KitchenProductRepository productRepository;
    private final KitchenProductVariantRepository variantRepository;
    private final KitchenCollectionRepository collectionRepository;
    private final KitchenUnitCategoryRepository unitCategoryRepository;
    private final KitchenColourRepository colourRepository;

    public KitchenProductCommandService(
            KitchenProductRepository productRepository,
            KitchenProductVariantRepository variantRepository,
            KitchenCollectionRepository collectionRepository,
            KitchenUnitCategoryRepository unitCategoryRepository,
            KitchenColourRepository colourRepository,
            CacheManager cacheManager,
            ApplicationEventPublisher eventPublisher,
            Validator validator,
            NormalizationService normalizationService) {
        super(productRepository, cacheManager, eventPublisher, validator, normalizationService);
        this.productRepository = productRepository;
        this.variantRepository = variantRepository;
        this.collectionRepository = collectionRepository;
        this.unitCategoryRepository = unitCategoryRepository;
        this.colourRepository = colourRepository;
    }

    // ====================================
    // Product - Create
    // ====================================

    public KitchenProduct createProduct(
            UUID collectionId,
            UUID unitCategoryId,
            String sku,
            String name,
            Integer widthMm,
            Integer heightMm,
            Integer depthMm,
            Money basePrice) {

        log.debug("Creating kitchen product: sku={}, name={}", sku, name);

        KitchenCollection collection = collectionRepository.findById(collectionId)
            .orElseThrow(() -> new EntityNotFoundException(KitchenCollection.class, collectionId));

        KitchenUnitCategory unitCategory = unitCategoryRepository.findById(unitCategoryId)
            .orElseThrow(() -> new EntityNotFoundException(
                KitchenUnitCategory.class, unitCategoryId));

        if (productRepository.existsBySku(sku)) {
            throw new IllegalStateException("Product SKU already exists: " + sku);
        }

        KitchenProduct product = KitchenProduct.create(
            collection, unitCategory, sku, name,
            widthMm, heightMm, depthMm, basePrice
        );

        return create(product);
    }

    // ====================================
    // Product - Update
    // ====================================

    public KitchenProduct updateBasicInfo(
            UUID id,
            String name,
            String shortDescription,
            String description) {

        KitchenProduct product = productRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(KitchenProduct.class, id));

        product.updateBasicInfo(name, shortDescription, description);
        return update(id, product);
    }

    public KitchenProduct updateDimensions(
            UUID id,
            Integer widthMm,
            Integer heightMm,
            Integer depthMm) {

        KitchenProduct product = productRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(KitchenProduct.class, id));

        product.updateDimensions(widthMm, heightMm, depthMm);
        return update(id, product);
    }

    public KitchenProduct updateBasePrice(UUID id, Money newPrice) {
        KitchenProduct product = productRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(KitchenProduct.class, id));

        product.updateBasePrice(newPrice);
        return update(id, product);
    }

    public KitchenProduct markAsPopular(UUID id) {
        KitchenProduct product = productRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(KitchenProduct.class, id));

        product.markAsPopular();
        return update(id, product);
    }

    public KitchenProduct unmarkAsPopular(UUID id) {
        KitchenProduct product = productRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(KitchenProduct.class, id));

        product.unmarkAsPopular();
        return update(id, product);
    }

    // ====================================
    // Product - Images
    // ====================================

    public KitchenProduct addImage(UUID productId, String url, String altText, boolean isPrimary) {
        KitchenProduct product = productRepository.findById(productId)
            .orElseThrow(() -> new EntityNotFoundException(KitchenProduct.class, productId));

        KitchenProductImage image = KitchenProductImage.create(url, altText, isPrimary);
        product.addImage(image);
        return update(productId, product);
    }

    public KitchenProduct removeImage(UUID productId, UUID imageId) {
        KitchenProduct product = productRepository.findByIdWithAll(productId)
            .orElseThrow(() -> new EntityNotFoundException(KitchenProduct.class, productId));

        product.removeImage(imageId);
        return update(productId, product);
    }

    // ====================================
    // Product - Activation
    // ====================================

    public KitchenProduct activateProduct(UUID id) {
        KitchenProduct product = productRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(KitchenProduct.class, id));

        product.activate();
        return update(id, product);
    }

    public KitchenProduct deactivateProduct(UUID id) {
        KitchenProduct product = productRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(KitchenProduct.class, id));

        product.deactivate();
        return update(id, product);
    }

    // ====================================
    // Product - Delete
    // ====================================

    public void softDeleteProduct(UUID id, String deletedBy) {
        KitchenProduct product = productRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(KitchenProduct.class, id));

        product.delete(deletedBy);
        update(id, product);
    }

    // ====================================
    // Variant - Create
    // ====================================

    public KitchenProductVariant addVariant(
            UUID productId,
            String sku,
            UUID cabinetColourId,
            AssemblyType assemblyType,
            Money price) {

        log.debug("Adding variant: productId={}, sku={}", productId, sku);

        KitchenProduct product = productRepository.findByIdWithAll(productId)
            .orElseThrow(() -> new EntityNotFoundException(KitchenProduct.class, productId));

        KitchenColour cabinetColour = colourRepository.findById(cabinetColourId)
            .orElseThrow(() -> new EntityNotFoundException(KitchenColour.class, cabinetColourId));

        if (variantRepository.existsBySku(sku)) {
            throw new IllegalStateException("Variant SKU already exists: " + sku);
        }

        KitchenProductVariant variant = KitchenProductVariant.create(
            product, sku, cabinetColour, assemblyType, price
        );

        product.addVariant(variant);

        KitchenProductVariant saved = variantRepository.save(variant);
        publishDomainEvents(product);
        evictAllCaches();

        return saved;
    }

    // ====================================
    // Variant - Update
    // ====================================

    public KitchenProductVariant updateVariantPrice(UUID variantId, Money newPrice) {
        KitchenProductVariant variant = variantRepository.findById(variantId)
            .orElseThrow(() -> new EntityNotFoundException(KitchenProductVariant.class, variantId));

        variant.updatePrice(newPrice);
        KitchenProductVariant updated = variantRepository.save(variant);
        evictAllCaches();

        return updated;
    }

    public KitchenProductVariant updateVariantPremiums(
            UUID variantId,
            Money cabinetColourPremium,
            Money assemblyPremium) {

        KitchenProductVariant variant = variantRepository.findById(variantId)
            .orElseThrow(() -> new EntityNotFoundException(KitchenProductVariant.class, variantId));

        variant.updatePremiums(cabinetColourPremium, assemblyPremium);
        KitchenProductVariant updated = variantRepository.save(variant);
        evictAllCaches();

        return updated;
    }

    public KitchenProductVariant updateVariantStock(
            UUID variantId,
            boolean inStock,
            Integer quantity) {

        KitchenProductVariant variant = variantRepository.findById(variantId)
            .orElseThrow(() -> new EntityNotFoundException(KitchenProductVariant.class, variantId));

        variant.updateStock(inStock, quantity);
        KitchenProductVariant updated = variantRepository.save(variant);
        evictAllCaches();

        return updated;
    }

    public KitchenProductVariant markVariantAsDefault(UUID variantId) {
        KitchenProductVariant variant = variantRepository.findByIdWithDetails(variantId)
            .orElseThrow(() -> new EntityNotFoundException(KitchenProductVariant.class, variantId));

        variant.markAsDefault();
        KitchenProductVariant updated = variantRepository.save(variant);
        evictAllCaches();

        return updated;
    }

    // ====================================
    // Variant - Activation
    // ====================================

    public KitchenProductVariant activateVariant(UUID variantId) {
        KitchenProductVariant variant = variantRepository.findByIdWithDetails(variantId)
            .orElseThrow(() -> new EntityNotFoundException(KitchenProductVariant.class, variantId));

        variant.activate();
        KitchenProductVariant updated = variantRepository.save(variant);
        evictAllCaches();

        return updated;
    }

    public KitchenProductVariant deactivateVariant(UUID variantId) {
        KitchenProductVariant variant = variantRepository.findById(variantId)
            .orElseThrow(() -> new EntityNotFoundException(KitchenProductVariant.class, variantId));

        variant.deactivate();
        KitchenProductVariant updated = variantRepository.save(variant);
        evictAllCaches();

        return updated;
    }

    // ====================================
    // AbstractCommandService
    // ====================================

    @Override
    protected List<String> getCacheNames() {
        return List.of("kitchen-products", "kitchen-product-variants", "kitchen-collections");
    }

    @Override
    protected Class<KitchenProduct> getEntityClass() {
        return KitchenProduct.class;
    }
}