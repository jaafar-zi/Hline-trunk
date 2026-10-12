package com.example.demo.modules.kitchen.application.command;

import com.example.demo.common.base.service.command.AbstractCommandService;
import com.example.demo.common.normalization.NormalizationService;
import com.example.demo.modules.kitchen.domain.model.KitchenColour;
import com.example.demo.modules.kitchen.domain.repository.KitchenColourRepository;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Kitchen Colour Command Service
 *
 * Handles all write operations for KitchenColour.
 */
@Slf4j
@Service
@Transactional
public class KitchenColourCommandService extends AbstractCommandService<KitchenColour, UUID> {

    private final KitchenColourRepository colourRepository;

    public KitchenColourCommandService(
            KitchenColourRepository colourRepository,
            CacheManager cacheManager,
            ApplicationEventPublisher eventPublisher,
            Validator validator,
            NormalizationService normalizationService) {
        super(colourRepository, cacheManager, eventPublisher, validator, normalizationService);
        this.colourRepository = colourRepository;
    }

    // ====================================
    // Create
    // ====================================

    public KitchenColour createColour(String code, String name, String hexValue) {
        log.debug("Creating kitchen colour: code={}, name={}", code, name);

        if (colourRepository.existsByCode(code)) {
            throw new IllegalStateException("Colour code already exists: " + code);
        }

        KitchenColour colour = KitchenColour.create(code, name, hexValue);
        return create(colour);
    }

    // ====================================
    // Update
    // ====================================

    public KitchenColour updateColour(
            UUID id,
            String name,
            String hexValue,
            String swatchImageUrl,
            String description) {

        KitchenColour colour = colourRepository.findById(id)
            .orElseThrow(() -> new com.example.demo.common.exception
                .EntityNotFoundException(KitchenColour.class, id));

        colour.updateDetails(name, hexValue, swatchImageUrl, description);
        return update(id, colour);
    }

    public KitchenColour updateSortOrder(UUID id, Integer sortOrder) {
        KitchenColour colour = colourRepository.findById(id)
            .orElseThrow(() -> new com.example.demo.common.exception
                .EntityNotFoundException(KitchenColour.class, id));

        colour.setSortOrder(sortOrder);
        return update(id, colour);
    }

    // ====================================
    // Activation
    // ====================================

    public KitchenColour activate(UUID id) {
        KitchenColour colour = colourRepository.findById(id)
            .orElseThrow(() -> new com.example.demo.common.exception
                .EntityNotFoundException(KitchenColour.class, id));

        colour.activate();
        return update(id, colour);
    }

    public KitchenColour deactivate(UUID id) {
        KitchenColour colour = colourRepository.findById(id)
            .orElseThrow(() -> new com.example.demo.common.exception
                .EntityNotFoundException(KitchenColour.class, id));

        colour.deactivate();
        return update(id, colour);
    }

    // ====================================
    // Delete
    // ====================================

    public void softDeleteColour(UUID id, String deletedBy) {
        KitchenColour colour = colourRepository.findById(id)
            .orElseThrow(() -> new com.example.demo.common.exception
                .EntityNotFoundException(KitchenColour.class, id));

        colour.delete(deletedBy);
        update(id, colour);
    }

    // ====================================
    // AbstractCommandService
    // ====================================

    @Override
    protected List<String> getCacheNames() {
        return List.of("kitchen-colours", "kitchen-collections");
    }

    @Override
    protected Class<KitchenColour> getEntityClass() {
        return KitchenColour.class;
    }
}