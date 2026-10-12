package com.example.demo.modules.kitchen.application.query;

import com.example.demo.common.base.service.query.AbstractQueryService;
import com.example.demo.common.exception.EntityNotFoundException;
import com.example.demo.common.normalization.NormalizationService;
import com.example.demo.modules.kitchen.domain.model.KitchenFinishGroup;
import com.example.demo.modules.kitchen.domain.model.KitchenRange;
import com.example.demo.modules.kitchen.domain.repository.KitchenFinishGroupRepository;
import com.example.demo.modules.kitchen.domain.repository.KitchenRangeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Kitchen Range Query Service
 *
 * Handles all read operations for KitchenRange and KitchenFinishGroup.
 */
@Slf4j
@Service
@Transactional(readOnly = true)
public class KitchenRangeQueryService extends AbstractQueryService<KitchenRange, UUID> {

    private final KitchenRangeRepository rangeRepository;
    private final KitchenFinishGroupRepository finishGroupRepository;

    public KitchenRangeQueryService(
            KitchenRangeRepository rangeRepository,
            KitchenFinishGroupRepository finishGroupRepository,
            NormalizationService normalizationService) {
        super(rangeRepository, normalizationService);
        this.rangeRepository = rangeRepository;
        this.finishGroupRepository = finishGroupRepository;
    }

    // ====================================
    // Range Queries
    // ====================================

    @Cacheable(cacheNames = "kitchen-ranges", key = "'active-all'")
    public List<KitchenRange> findAllActive() {
        return rangeRepository.findAllActive();
    }

    @Cacheable(cacheNames = "kitchen-ranges", key = "'page:' + #pageable.pageNumber")
    public Page<KitchenRange> findAll(Pageable pageable) {
        return rangeRepository.findAll(pageable);
    }

    @Cacheable(cacheNames = "kitchen-ranges", key = "'slug:' + #slug")
    public Optional<KitchenRange> findBySlug(String slug) {
        return rangeRepository.findBySlug(slug);
    }

    @Cacheable(cacheNames = "kitchen-ranges", key = "'slug-full:' + #slug")
    public KitchenRange findBySlugWithCollections(String slug) {
        return rangeRepository.findBySlugWithCollections(slug)
            .orElseThrow(() -> new EntityNotFoundException(KitchenRange.class, slug));
    }

    @Cacheable(cacheNames = "kitchen-ranges", key = "'id-full:' + #id")
    public KitchenRange findByIdWithAll(UUID id) {
        return rangeRepository.findByIdWithAll(id)
            .orElseThrow(() -> new EntityNotFoundException(KitchenRange.class, id));
    }

    public Page<KitchenRange> search(String searchTerm, Pageable pageable) {
        return rangeRepository.search(searchTerm, pageable);
    }

    public boolean existsByCode(String code) {
        return rangeRepository.existsByCode(code);
    }

    public boolean existsBySlug(String slug) {
        return rangeRepository.existsBySlug(slug);
    }

    // ====================================
    // Finish Group Queries
    // ====================================

    @Cacheable(cacheNames = "kitchen-finish-groups", key = "'range:' + #rangeId")
    public List<KitchenFinishGroup> findActiveFinishGroupsByRange(UUID rangeId) {
        return finishGroupRepository.findActiveByRangeId(rangeId);
    }

    @Cacheable(cacheNames = "kitchen-finish-groups", key = "'range-detail:' + #rangeId")
    public List<KitchenFinishGroup> findFinishGroupsByRangeWithDetails(UUID rangeId) {
        return finishGroupRepository.findByRangeIdWithDetails(rangeId);
    }

    public KitchenFinishGroup findFinishGroupById(UUID finishGroupId) {
        return finishGroupRepository.findById(finishGroupId)
            .orElseThrow(() -> new EntityNotFoundException(KitchenFinishGroup.class, finishGroupId));
    }

    public KitchenFinishGroup findFinishGroupWithCollections(UUID finishGroupId) {
        return finishGroupRepository.findByIdWithCollections(finishGroupId)
            .orElseThrow(() -> new EntityNotFoundException(KitchenFinishGroup.class, finishGroupId));
    }

    // ====================================
    // Statistics
    // ====================================

    public long countActiveRanges() {
        return rangeRepository.countActive();
    }

    public long countFinishGroupsByRange(UUID rangeId) {
        return rangeRepository.countFinishGroupsByRangeId(rangeId);
    }

    public long countCollectionsByRange(UUID rangeId) {
        return rangeRepository.countCollectionsByRangeId(rangeId);
    }

    // ====================================
    // AbstractQueryService
    // ====================================

    @Override
    protected String getEntityCacheName() {
        return "kitchen-ranges";
    }

    @Override
    protected Class<KitchenRange> getEntityClass() {
        return KitchenRange.class;
    }
}