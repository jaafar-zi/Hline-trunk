package com.example.demo.modules.kitchen.application.command;

import com.example.demo.common.base.service.command.AbstractCommandService;
import com.example.demo.common.exception.EntityNotFoundException;
import com.example.demo.common.normalization.NormalizationService;
import com.example.demo.modules.kitchen.domain.model.*;
import com.example.demo.modules.kitchen.domain.model.vo.AssemblyType;
import com.example.demo.modules.kitchen.domain.repository.*;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Kitchen Configuration Command Service
 */
@Slf4j
@Service
@Transactional
public class KitchenConfigurationCommandService
        extends AbstractCommandService<KitchenConfiguration, UUID> {

    private final KitchenConfigurationRepository configurationRepository;
    private final KitchenConfigurationItemRepository itemRepository;
    private final KitchenCollectionRepository collectionRepository;
    private final KitchenProductRepository productRepository;
    private final KitchenProductVariantRepository variantRepository;
    private final KitchenColourRepository colourRepository;

    public KitchenConfigurationCommandService(
            KitchenConfigurationRepository configurationRepository,
            KitchenConfigurationItemRepository itemRepository,
            KitchenCollectionRepository collectionRepository,
            KitchenProductRepository productRepository,
            KitchenProductVariantRepository variantRepository,
            KitchenColourRepository colourRepository,
            CacheManager cacheManager,
            ApplicationEventPublisher eventPublisher,
            Validator validator,
            NormalizationService normalizationService) {
        super(configurationRepository, cacheManager, eventPublisher,
              validator, normalizationService);
        this.configurationRepository = configurationRepository;
        this.itemRepository = itemRepository;
        this.collectionRepository = collectionRepository;
        this.productRepository = productRepository;
        this.variantRepository = variantRepository;
        this.colourRepository = colourRepository;
    }

    // ====================================
    // Create Configuration
    // ====================================

    public KitchenConfiguration createConfiguration(
            UUID collectionId,
            String name,
            UUID customerId,
            String sessionId) {

        log.debug("Creating kitchen configuration: collectionId={}, customerId={}",
            collectionId, customerId);

        KitchenCollection collection = collectionRepository
            .findById(collectionId)
            .orElseThrow(() -> new EntityNotFoundException(
                KitchenCollection.class, collectionId));

        KitchenConfiguration configuration = KitchenConfiguration.create(
            collection, name, customerId, sessionId
        );

        return create(configuration);
    }

    // ====================================
    // Update Name
    // ====================================

    public KitchenConfiguration updateName(UUID configId, String name) {
        KitchenConfiguration configuration = configurationRepository
            .findById(configId)
            .orElseThrow(() -> new EntityNotFoundException(
                KitchenConfiguration.class, configId));

        configuration.setName(name);
        return update(configId, configuration);
    }

    // ====================================
    // Global Colour Change (Key Feature)
    // ====================================

    /**
     * Change the global cabinet colour for the entire configuration.
     *
     * Steps:
     * 1. Validate new colour is available for this collection
     * 2. Update selectedCabinetColour on configuration
     * 3. Publish KitchenColourChangedEvent
     * 4. For each non-overridden item: find matching variant (same assembly, new colour)
     * 5. Update item variant and unit price
     * 6. Save all changes
     */
    public KitchenConfiguration changeGlobalColour(UUID configId, UUID newColourId) {
        log.debug("Changing global colour: configId={}, newColourId={}",
            configId, newColourId);

        KitchenConfiguration configuration = configurationRepository
            .findByIdWithAll(configId)
            .orElseThrow(() -> new EntityNotFoundException(
                KitchenConfiguration.class, configId));

        KitchenColour newColour = colourRepository.findById(newColourId)
            .orElseThrow(() -> new EntityNotFoundException(
                KitchenColour.class, newColourId));

        // Change global colour on configuration (validates + fires event)
        configuration.changeGlobalCabinetColour(newColour);

        // Re-resolve variants for all non-overridden items
        List<KitchenConfigurationItem> nonOverriddenItems =
            itemRepository.findNonOverriddenByConfigurationId(configId);

        nonOverriddenItems.forEach(item ->
            resolveAndUpdateVariant(item, newColourId));

        // Save updated items
        itemRepository.saveAll(nonOverriddenItems);

        // Save and return updated configuration
        return update(configId, configuration);
    }

    /**
     * Resolve and update variant for a single item.
     * Finds variant matching: same product + new colour + same assembly type.
     * Updates item in-place (no new save needed, saveAll called after).
     */
    private void resolveAndUpdateVariant(
            KitchenConfigurationItem item,
            UUID newColourId) {

        if (item.getVariant() == null || item.getProduct() == null) {
            log.warn("Item {} missing variant or product, skipping", item.getId());
            return;
        }

        AssemblyType currentAssembly = item.getVariant().getAssemblyType();
        UUID productId = item.getProduct().getId();

        variantRepository
            .findByProductIdAndCabinetColourIdAndAssemblyType(
                productId, newColourId, currentAssembly)
            .ifPresentOrElse(
                newVariant -> {
                    log.debug("Resolved variant for item {}: {} → {}",
                        item.getId(),
                        item.getVariant().getSku(),
                        newVariant.getSku());
                    item.resetToGlobalColour(newVariant);
                },
                () -> log.warn(
                    "No matching variant found for product={}, colour={}, " +
                    "assembly={}. Item {} not updated.",
                    productId, newColourId, currentAssembly, item.getId()
                )
            );
    }

    // ====================================
    // Add Item
    // ====================================

    public KitchenConfigurationItem addItem(
            UUID configId,
            UUID productId,
            UUID variantId,
            int quantity) {

        log.debug("Adding item: configId={}, productId={}, qty={}",
            configId, productId, quantity);

        KitchenConfiguration configuration = configurationRepository
            .findByIdWithAll(configId)
            .orElseThrow(() -> new EntityNotFoundException(
                KitchenConfiguration.class, configId));

        KitchenProduct product = productRepository.findByIdWithAll(productId)
            .orElseThrow(() -> new EntityNotFoundException(
                KitchenProduct.class, productId));

        KitchenProductVariant variant = variantRepository.findById(variantId)
            .orElseThrow(() -> new EntityNotFoundException(
                KitchenProductVariant.class, variantId));

        if (!variant.isSellable()) {
            throw new IllegalStateException(
                "Variant is not available: " + variant.getSku());
        }

        KitchenConfigurationItem item =
            configuration.addItem(product, variant, quantity);

        KitchenConfigurationItem saved = itemRepository.save(item);
        update(configId, configuration);

        return saved;
    }

    // ====================================
    // Remove Item
    // ====================================

    public void removeItem(UUID configId, UUID itemId) {
        log.debug("Removing item: configId={}, itemId={}", configId, itemId);

        KitchenConfiguration configuration = configurationRepository
            .findByIdWithAll(configId)
            .orElseThrow(() -> new EntityNotFoundException(
                KitchenConfiguration.class, configId));

        configuration.removeItem(itemId);
        update(configId, configuration);
    }

    // ====================================
    // Update Item Quantity
    // ====================================

    public KitchenConfigurationItem updateItemQuantity(
            UUID configId,
            UUID itemId,
            int newQuantity) {

        KitchenConfiguration configuration = configurationRepository
            .findById(configId)
            .orElseThrow(() -> new EntityNotFoundException(
                KitchenConfiguration.class, configId));

        configuration.updateItemQuantity(itemId, newQuantity);

        KitchenConfigurationItem item = itemRepository.findById(itemId)
            .orElseThrow(() -> new EntityNotFoundException(
                KitchenConfigurationItem.class, itemId));

        KitchenConfigurationItem updated = itemRepository.save(item);
        update(configId, configuration);

        return updated;
    }

    // ====================================
    // Override Item Colour
    // ====================================

    public KitchenConfigurationItem overrideItemColour(
            UUID configId,
            UUID itemId,
            UUID newVariantId) {

        log.debug("Overriding item colour: configId={}, itemId={}", configId, itemId);

        KitchenConfiguration configuration = configurationRepository
            .findById(configId)
            .orElseThrow(() -> new EntityNotFoundException(
                KitchenConfiguration.class, configId));

        KitchenProductVariant newVariant = variantRepository.findById(newVariantId)
            .orElseThrow(() -> new EntityNotFoundException(
                KitchenProductVariant.class, newVariantId));

        configuration.overrideItemColour(itemId, newVariant);

        KitchenConfigurationItem item = itemRepository.findById(itemId)
            .orElseThrow(() -> new EntityNotFoundException(
                KitchenConfigurationItem.class, itemId));

        KitchenConfigurationItem updated = itemRepository.save(item);
        update(configId, configuration);

        return updated;
    }

    // ====================================
    // Update Item Label
    // ====================================

    public KitchenConfigurationItem updateItemLabel(
            UUID configId,
            UUID itemId,
            String label) {

        KitchenConfigurationItem item = itemRepository.findById(itemId)
            .orElseThrow(() -> new EntityNotFoundException(
                KitchenConfigurationItem.class, itemId));

        item.updateLabel(label);
        return itemRepository.save(item);
    }

    // ====================================
    // Status Transitions
    // ====================================

    public KitchenConfiguration markAsQuoted(UUID configId) {
        KitchenConfiguration configuration = configurationRepository
            .findById(configId)
            .orElseThrow(() -> new EntityNotFoundException(
                KitchenConfiguration.class, configId));

        configuration.markAsQuoted();
        return update(configId, configuration);
    }

    public KitchenConfiguration markAsOrdered(UUID configId) {
        KitchenConfiguration configuration = configurationRepository
            .findById(configId)
            .orElseThrow(() -> new EntityNotFoundException(
                KitchenConfiguration.class, configId));

        configuration.markAsOrdered();
        return update(configId, configuration);
    }

    public KitchenConfiguration archive(UUID configId) {
        KitchenConfiguration configuration = configurationRepository
            .findById(configId)
            .orElseThrow(() -> new EntityNotFoundException(
                KitchenConfiguration.class, configId));

        configuration.archive();
        return update(configId, configuration);
    }

    // ====================================
    // AbstractCommandService
    // ====================================

    @Override
    protected List<String> getCacheNames() {
        return List.of("kitchen-configurations");
    }

    @Override
    protected Class<KitchenConfiguration> getEntityClass() {
        return KitchenConfiguration.class;
    }
}