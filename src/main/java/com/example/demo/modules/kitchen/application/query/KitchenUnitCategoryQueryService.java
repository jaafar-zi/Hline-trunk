package com.example.demo.modules.kitchen.application.query;

import com.example.demo.common.base.service.query.AbstractQueryService;
import com.example.demo.common.exception.EntityNotFoundException;
import com.example.demo.common.normalization.NormalizationService;
import com.example.demo.modules.kitchen.domain.model.KitchenUnitCategory;
import com.example.demo.modules.kitchen.domain.repository.KitchenUnitCategoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Kitchen Unit Category Query Service
 */
@Slf4j
@Service
@Transactional(readOnly = true)
public class KitchenUnitCategoryQueryService
        extends AbstractQueryService<KitchenUnitCategory, UUID> {

    private final KitchenUnitCategoryRepository unitCategoryRepository;

    public KitchenUnitCategoryQueryService(
            KitchenUnitCategoryRepository unitCategoryRepository,
            NormalizationService normalizationService) {
        super(unitCategoryRepository, normalizationService);
        this.unitCategoryRepository = unitCategoryRepository;
    }

    @Cacheable(cacheNames = "kitchen-unit-categories", key = "'active-all'")
    public List<KitchenUnitCategory> findAllActive() {
        return unitCategoryRepository.findAllActive();
    }

    @Cacheable(cacheNames = "kitchen-unit-categories", key = "'code:' + #code")
    public Optional<KitchenUnitCategory> findByCode(String code) {
        return unitCategoryRepository.findByCode(code);
    }

    @Cacheable(cacheNames = "kitchen-unit-categories",
               key = "'collection-cats:' + #collectionId")
    public List<KitchenUnitCategory> findCategoriesWithProductsByCollection(
            UUID collectionId) {
        return unitCategoryRepository
            .findCategoriesWithProductsByCollectionId(collectionId);
    }

    public KitchenUnitCategory findByCodeOrThrow(String code) {
        return unitCategoryRepository.findByCode(code)
            .orElseThrow(() -> new EntityNotFoundException(
                KitchenUnitCategory.class, code));
    }

    @Override
    protected String getEntityCacheName() {
        return "kitchen-unit-categories";
    }

    @Override
    protected Class<KitchenUnitCategory> getEntityClass() {
        return KitchenUnitCategory.class;
    }
}