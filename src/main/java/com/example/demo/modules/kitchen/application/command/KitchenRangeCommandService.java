package com.example.demo.modules.kitchen.application.command;

import com.example.demo.common.base.service.command.AbstractCommandService;
import com.example.demo.common.exception.EntityNotFoundException;
import com.example.demo.common.normalization.NormalizationService;
import com.example.demo.modules.kitchen.domain.model.KitchenFinishGroup;
import com.example.demo.modules.kitchen.domain.model.KitchenFinishType;
import com.example.demo.modules.kitchen.domain.model.KitchenRange;
import com.example.demo.modules.kitchen.domain.repository.KitchenFinishGroupRepository;
import com.example.demo.modules.kitchen.domain.repository.KitchenFinishTypeRepository;
import com.example.demo.modules.kitchen.domain.repository.KitchenRangeRepository;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Kitchen Range Command Service
 *
 * Handles all write operations for KitchenRange and KitchenFinishGroup.
 */
@Slf4j
@Service
@Transactional
public class KitchenRangeCommandService extends AbstractCommandService<KitchenRange, UUID> {

    private final KitchenRangeRepository rangeRepository;
    private final KitchenFinishGroupRepository finishGroupRepository;
    private final KitchenFinishTypeRepository finishTypeRepository;

    public KitchenRangeCommandService(
            KitchenRangeRepository rangeRepository,
            KitchenFinishGroupRepository finishGroupRepository,
            KitchenFinishTypeRepository finishTypeRepository,
            CacheManager cacheManager,
            ApplicationEventPublisher eventPublisher,
            Validator validator,
            NormalizationService normalizationService) {
        super(rangeRepository, cacheManager, eventPublisher, validator, normalizationService);
        this.rangeRepository = rangeRepository;
        this.finishGroupRepository = finishGroupRepository;
        this.finishTypeRepository = finishTypeRepository;
    }

    // ====================================
    // Range - Create
    // ====================================

    public KitchenRange createRange(String code, String name) {
        log.debug("Creating kitchen range: code={}, name={}", code, name);

        if (rangeRepository.existsByCode(code)) {
            throw new IllegalStateException("Range code already exists: " + code);
        }

        KitchenRange range = KitchenRange.create(code, name);
        return create(range);
    }

    // ====================================
    // Range - Update
    // ====================================

    public KitchenRange updateRangeInfo(
            UUID id,
            String name,
            String shortDescription,
            String description) {

        KitchenRange range = rangeRepository.findByIdWithFinishGroups(id)
            .orElseThrow(() -> new EntityNotFoundException(KitchenRange.class, id));

        range.updateBasicInfo(name, shortDescription, description);
        return update(id, range);
    }

    public KitchenRange updateHeroImage(UUID id, String heroImageUrl) {
        KitchenRange range = rangeRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(KitchenRange.class, id));

        range.updateHeroImage(heroImageUrl);
        return update(id, range);
    }

    public KitchenRange updateSEO(UUID id, String metaTitle, String metaDescription) {
        KitchenRange range = rangeRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(KitchenRange.class, id));

        range.updateSEO(metaTitle, metaDescription);
        return update(id, range);
    }

    // ====================================
    // Range - Activation
    // ====================================

    public KitchenRange activateRange(UUID id) {
        KitchenRange range = rangeRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(KitchenRange.class, id));

        range.activate();
        return update(id, range);
    }

    public KitchenRange deactivateRange(UUID id, String reason) {
        KitchenRange range = rangeRepository.findByIdWithAll(id)
            .orElseThrow(() -> new EntityNotFoundException(KitchenRange.class, id));

        range.deactivate(reason);
        return update(id, range);
    }

    // ====================================
    // Range - Delete
    // ====================================

    public void softDeleteRange(UUID id, String deletedBy) {
        KitchenRange range = rangeRepository.findByIdWithAll(id)
            .orElseThrow(() -> new EntityNotFoundException(KitchenRange.class, id));

        range.delete(deletedBy);
        update(id, range);
    }

    // ====================================
    // FinishGroup - Create
    // ====================================

    public KitchenFinishGroup addFinishGroupToRange(
            UUID rangeId,
            UUID finishTypeId,
            String name) {

        log.debug("Adding finish group to range: rangeId={}, name={}", rangeId, name);

        KitchenRange range = rangeRepository.findById(rangeId)
            .orElseThrow(() -> new EntityNotFoundException(KitchenRange.class, rangeId));

        KitchenFinishType finishType = finishTypeRepository.findById(finishTypeId)
            .orElseThrow(() -> new EntityNotFoundException(KitchenFinishType.class, finishTypeId));

        if (finishGroupRepository.existsByRangeIdAndFinishTypeId(rangeId, finishTypeId)) {
            throw new IllegalStateException(
                "Finish group with this type already exists in range"
            );
        }

        KitchenFinishGroup finishGroup = KitchenFinishGroup.create(range, finishType, name);
        range.addFinishGroup(finishGroup);

        KitchenFinishGroup saved = finishGroupRepository.save(finishGroup);
        publishDomainEvents(range);
        evictAllCaches();

        return saved;
    }

    // ====================================
    // FinishGroup - Update
    // ====================================

    public KitchenFinishGroup updateFinishGroup(UUID finishGroupId, String name, String description) {
        KitchenFinishGroup finishGroup = finishGroupRepository.findById(finishGroupId)
            .orElseThrow(() -> new EntityNotFoundException(KitchenFinishGroup.class, finishGroupId));

        finishGroup.updateDetails(name, description);
        KitchenFinishGroup updated = finishGroupRepository.save(finishGroup);
        evictAllCaches();

        return updated;
    }

    public KitchenFinishGroup changeFinishGroupType(UUID finishGroupId, UUID newFinishTypeId) {
        KitchenFinishGroup finishGroup = finishGroupRepository.findById(finishGroupId)
            .orElseThrow(() -> new EntityNotFoundException(KitchenFinishGroup.class, finishGroupId));

        KitchenFinishType newFinishType = finishTypeRepository.findById(newFinishTypeId)
            .orElseThrow(() -> new EntityNotFoundException(KitchenFinishType.class, newFinishTypeId));

        finishGroup.changeFinishType(newFinishType);
        KitchenFinishGroup updated = finishGroupRepository.save(finishGroup);
        evictAllCaches();

        return updated;
    }

    // ====================================
    // FinishGroup - Activation
    // ====================================

    public KitchenFinishGroup activateFinishGroup(UUID finishGroupId) {
        KitchenFinishGroup finishGroup = finishGroupRepository.findById(finishGroupId)
            .orElseThrow(() -> new EntityNotFoundException(KitchenFinishGroup.class, finishGroupId));

        finishGroup.activate();
        KitchenFinishGroup updated = finishGroupRepository.save(finishGroup);
        evictAllCaches();

        return updated;
    }

    public KitchenFinishGroup deactivateFinishGroup(UUID finishGroupId, String reason) {
        KitchenFinishGroup finishGroup = finishGroupRepository
            .findByIdWithCollections(finishGroupId)
            .orElseThrow(() -> new EntityNotFoundException(KitchenFinishGroup.class, finishGroupId));

        finishGroup.deactivate(reason);
        KitchenFinishGroup updated = finishGroupRepository.save(finishGroup);
        evictAllCaches();

        return updated;
    }

    // ====================================
    // AbstractCommandService
    // ====================================

    @Override
    protected List<String> getCacheNames() {
        return List.of("kitchen-ranges", "kitchen-finish-groups", "kitchen-collections");
    }

    @Override
    protected Class<KitchenRange> getEntityClass() {
        return KitchenRange.class;
    }
}