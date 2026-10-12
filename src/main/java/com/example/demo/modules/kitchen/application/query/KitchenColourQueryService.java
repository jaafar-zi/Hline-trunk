package com.example.demo.modules.kitchen.application.query;

import com.example.demo.common.base.service.query.AbstractQueryService;
import com.example.demo.common.normalization.NormalizationService;
import com.example.demo.modules.kitchen.domain.model.KitchenColour;
import com.example.demo.modules.kitchen.domain.repository.KitchenColourRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Kitchen Colour Query Service
 *
 * Handles all read operations for KitchenColour.
 */
@Slf4j
@Service
@Transactional(readOnly = true)
public class KitchenColourQueryService extends AbstractQueryService<KitchenColour, UUID> {

    private final KitchenColourRepository colourRepository;

    public KitchenColourQueryService(
            KitchenColourRepository colourRepository,
            NormalizationService normalizationService) {
        super(colourRepository, normalizationService);
        this.colourRepository = colourRepository;
    }

    // ====================================
    // Find Methods
    // ====================================

    @Cacheable(cacheNames = "kitchen-colours", key = "'active-all'")
    public List<KitchenColour> findAllActive() {
        return colourRepository.findAllActive();
    }

    @Cacheable(cacheNames = "kitchen-colours", key = "'code:' + #code")
    public Optional<KitchenColour> findByCode(String code) {
        return colourRepository.findByCode(code);
    }

    public List<KitchenColour> findAvailableCabinetColoursByCollection(UUID collectionId) {
        return colourRepository.findAvailableCabinetColoursByCollectionId(collectionId);
    }

    public boolean existsByCode(String code) {
        return colourRepository.existsByCode(code);
    }

    // ====================================
    // AbstractQueryService
    // ====================================

    @Override
    protected String getEntityCacheName() {
        return "kitchen-colours";
    }

    @Override
    protected Class<KitchenColour> getEntityClass() {
        return KitchenColour.class;
    }
}