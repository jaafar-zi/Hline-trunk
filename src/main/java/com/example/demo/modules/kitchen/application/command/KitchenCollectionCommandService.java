package com.example.demo.modules.kitchen.application.command;

import com.example.demo.common.base.service.command.AbstractCommandService;
import com.example.demo.common.exception.EntityNotFoundException;
import com.example.demo.common.normalization.NormalizationService;
import com.example.demo.modules.kitchen.domain.model.KitchenCollection;
import com.example.demo.modules.kitchen.domain.model.KitchenColour;
import com.example.demo.modules.kitchen.domain.model.KitchenFinishGroup;
import com.example.demo.modules.kitchen.domain.repository.KitchenCollectionRepository;
import com.example.demo.modules.kitchen.domain.repository.KitchenColourRepository;
import com.example.demo.modules.kitchen.domain.repository.KitchenFinishGroupRepository;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Kitchen Collection Command Service
 *
 * Handles all write operations for KitchenCollection.
 */
@Slf4j
@Service
@Transactional
public class KitchenCollectionCommandService
        extends AbstractCommandService<KitchenCollection, UUID> {

    private final KitchenCollectionRepository collectionRepository;
    private final KitchenFinishGroupRepository finishGroupRepository;
    private final KitchenColourRepository colourRepository;

    public KitchenCollectionCommandService(
            KitchenCollectionRepository collectionRepository,
            KitchenFinishGroupRepository finishGroupRepository,
            KitchenColourRepository colourRepository,
            CacheManager cacheManager,
            ApplicationEventPublisher eventPublisher,
            Validator validator,
            NormalizationService normalizationService) {
        super(collectionRepository, cacheManager, eventPublisher, validator, normalizationService);
        this.collectionRepository = collectionRepository;
        this.finishGroupRepository = finishGroupRepository;
        this.colourRepository = colourRepository;
    }

    // ====================================
    // Create
    // ====================================

    public KitchenCollection createCollection(
            UUID finishGroupId,
            UUID colourId,
            String name) {

        log.debug("Creating collection: finishGroupId={}, name={}", finishGroupId, name);

        KitchenFinishGroup finishGroup = finishGroupRepository.findById(finishGroupId)
            .orElseThrow(() -> new EntityNotFoundException(KitchenFinishGroup.class, finishGroupId));

        KitchenColour colour = colourRepository.findById(colourId)
            .orElseThrow(() -> new EntityNotFoundException(KitchenColour.class, colourId));

        if (collectionRepository.existsBySlug(generateSlug(name))) {
            throw new IllegalStateException("Collection with this name already exists: " + name);
        }

        KitchenCollection collection = KitchenCollection.create(finishGroup, colour, name);
        finishGroup.addCollection(collection);

        return create(collection);
    }

    // ====================================
    // Update - Basic Info
    // ====================================

    public KitchenCollection updateBasicInfo(
            UUID id,
            String name,
            String shortDescription,
            String description) {

        KitchenCollection collection = collectionRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(KitchenCollection.class, id));

        if (name != null && !name.equals(collection.getName())) {
            String newSlug = generateSlug(name);
            if (collectionRepository.existsBySlugAndIdNot(newSlug, id)) {
                throw new IllegalStateException("Collection name already taken: " + name);
            }
        }

        collection.updateBasicInfo(name, shortDescription, description);
        return update(id, collection);
    }

    public KitchenCollection updateHeroImage(UUID id, String heroImageUrl) {
        KitchenCollection collection = collectionRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(KitchenCollection.class, id));

        collection.updateHeroImage(heroImageUrl);
        return update(id, collection);
    }

    public KitchenCollection updateLeadTime(UUID id, Integer leadTimeDays) {
        KitchenCollection collection = collectionRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(KitchenCollection.class, id));

        collection.updateLeadTime(leadTimeDays);
        return update(id, collection);
    }

    // ====================================
    // Update - Cabinet Colours
    // ====================================

    public KitchenCollection setDefaultCabinetColour(UUID collectionId, UUID colourId) {
        KitchenCollection collection = collectionRepository
            .findBySlugWithDetails(collectionId.toString())
            .orElseGet(() -> collectionRepository.findById(collectionId)
                .orElseThrow(() -> new EntityNotFoundException(
                    KitchenCollection.class, collectionId)));

        KitchenColour colour = colourRepository.findById(colourId)
            .orElseThrow(() -> new EntityNotFoundException(KitchenColour.class, colourId));

        collection.setDefaultCabinetColour(colour);
        return update(collectionId, collection);
    }

    public KitchenCollection addAvailableCabinetColour(UUID collectionId, UUID colourId) {
        KitchenCollection collection = collectionRepository.findById(collectionId)
            .orElseThrow(() -> new EntityNotFoundException(KitchenCollection.class, collectionId));

        KitchenColour colour = colourRepository.findById(colourId)
            .orElseThrow(() -> new EntityNotFoundException(KitchenColour.class, colourId));

        collection.addAvailableCabinetColour(colour);
        return update(collectionId, collection);
    }

    public KitchenCollection removeAvailableCabinetColour(UUID collectionId, UUID colourId) {
        KitchenCollection collection = collectionRepository.findById(collectionId)
            .orElseThrow(() -> new EntityNotFoundException(KitchenCollection.class, collectionId));

        KitchenColour colour = colourRepository.findById(colourId)
            .orElseThrow(() -> new EntityNotFoundException(KitchenColour.class, colourId));

        collection.removeAvailableCabinetColour(colour);
        return update(collectionId, collection);
    }

    // ====================================
    // Update - Gallery & Features
    // ====================================

    public KitchenCollection addGalleryImage(UUID collectionId, String imageUrl) {
        KitchenCollection collection = collectionRepository.findById(collectionId)
            .orElseThrow(() -> new EntityNotFoundException(KitchenCollection.class, collectionId));

        collection.addGalleryImage(imageUrl);
        return update(collectionId, collection);
    }

    public KitchenCollection removeGalleryImage(UUID collectionId, String imageUrl) {
        KitchenCollection collection = collectionRepository.findById(collectionId)
            .orElseThrow(() -> new EntityNotFoundException(KitchenCollection.class, collectionId));

        collection.removeGalleryImage(imageUrl);
        return update(collectionId, collection);
    }

    public KitchenCollection addFeature(UUID collectionId, String feature) {
        KitchenCollection collection = collectionRepository.findById(collectionId)
            .orElseThrow(() -> new EntityNotFoundException(KitchenCollection.class, collectionId));

        collection.addFeature(feature);
        return update(collectionId, collection);
    }

    public KitchenCollection removeFeature(UUID collectionId, String feature) {
        KitchenCollection collection = collectionRepository.findById(collectionId)
            .orElseThrow(() -> new EntityNotFoundException(KitchenCollection.class, collectionId));

        collection.removeFeature(feature);
        return update(collectionId, collection);
    }

    // ====================================
    // Activation
    // ====================================

    public KitchenCollection activate(UUID id) {
        KitchenCollection collection = collectionRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(KitchenCollection.class, id));

        collection.activate();
        return update(id, collection);
    }

    public KitchenCollection deactivate(UUID id, String reason) {
        KitchenCollection collection = collectionRepository.findBySlugWithProducts(
                collectionRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException(KitchenCollection.class, id))
                    .getSlug()
            ).orElseThrow(() -> new EntityNotFoundException(KitchenCollection.class, id));

        collection.deactivate(reason);
        return update(id, collection);
    }

    // ====================================
    // Delete
    // ====================================

    public void softDeleteCollection(UUID id, String deletedBy) {
        KitchenCollection collection = collectionRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(KitchenCollection.class, id));

        collection.delete(deletedBy);
        update(id, collection);
    }

    // ====================================
    // Helper
    // ====================================

    private String generateSlug(String name) {
        return name.toLowerCase()
            .replaceAll("[^a-z0-9\\s-]", "")
            .replaceAll("\\s+", "-")
            .replaceAll("-+", "-")
            .trim();
    }

    // ====================================
    // AbstractCommandService
    // ====================================

    @Override
    protected List<String> getCacheNames() {
        return List.of("kitchen-collections", "kitchen-ranges");
    }

    @Override
    protected Class<KitchenCollection> getEntityClass() {
        return KitchenCollection.class;
    }
}