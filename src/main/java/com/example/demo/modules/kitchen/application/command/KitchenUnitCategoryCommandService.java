package com.example.demo.modules.kitchen.application.command;

import com.example.demo.common.base.service.command.AbstractCommandService;
import com.example.demo.common.exception.EntityNotFoundException;
import com.example.demo.common.normalization.NormalizationService;
import com.example.demo.modules.kitchen.domain.model.KitchenUnitCategory;
import com.example.demo.modules.kitchen.domain.repository.KitchenUnitCategoryRepository;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@Transactional
public class KitchenUnitCategoryCommandService
        extends AbstractCommandService<KitchenUnitCategory, UUID> {

    private final KitchenUnitCategoryRepository unitCategoryRepository;

    public KitchenUnitCategoryCommandService(
            KitchenUnitCategoryRepository unitCategoryRepository,
            CacheManager cacheManager,
            ApplicationEventPublisher eventPublisher,
            Validator validator,
            NormalizationService normalizationService) {
        super(unitCategoryRepository, cacheManager, eventPublisher, validator, normalizationService);
        this.unitCategoryRepository = unitCategoryRepository;
    }

    public KitchenUnitCategory createCategory(
            String code, String name, String description, Integer sortOrder) {

        log.debug("Creating unit category: code={}, name={}", code, name);

        if (unitCategoryRepository.existsByCode(code.toUpperCase())) {
            throw new IllegalStateException("Unit category code already exists: " + code);
        }

        KitchenUnitCategory category = KitchenUnitCategory.create(code, name);

        if (description != null) {
            category.updateDetails(name, description, null, null);
        }
        if (sortOrder != null) {
            category.updateSortOrder(sortOrder);
        }

        return create(category);
    }

    public KitchenUnitCategory updateCategory(
            UUID id, String name, String description, String icon, String imageUrl) {

        KitchenUnitCategory category = unitCategoryRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(KitchenUnitCategory.class, id));

        category.updateDetails(name, description, icon, imageUrl);
        return update(id, category);
    }

    public KitchenUnitCategory activate(UUID id) {
        KitchenUnitCategory category = unitCategoryRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(KitchenUnitCategory.class, id));

        category.activate();
        return update(id, category);
    }

    public KitchenUnitCategory deactivate(UUID id) {
        KitchenUnitCategory category = unitCategoryRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(KitchenUnitCategory.class, id));

        category.deactivate();
        return update(id, category);
    }

    public void softDeleteCategory(UUID id, String deletedBy) {
        KitchenUnitCategory category = unitCategoryRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(KitchenUnitCategory.class, id));

        category.delete(deletedBy);
        update(id, category);
    }

    @Override
    protected List<String> getCacheNames() {
        return List.of("kitchen-unit-categories");
    }

    @Override
    protected Class<KitchenUnitCategory> getEntityClass() {
        return KitchenUnitCategory.class;
    }
}