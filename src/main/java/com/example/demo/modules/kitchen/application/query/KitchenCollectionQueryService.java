package com.example.demo.modules.kitchen.application.query;

import com.example.demo.common.base.service.query.AbstractQueryService;
import com.example.demo.common.exception.EntityNotFoundException;
import com.example.demo.common.normalization.NormalizationService;
import com.example.demo.modules.kitchen.domain.model.KitchenCollection;
import com.example.demo.modules.kitchen.domain.repository.KitchenCollectionRepository;
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
 * Kitchen Collection Query Service
 */
@Slf4j
@Service
@Transactional(readOnly = true)
public class KitchenCollectionQueryService
        extends AbstractQueryService<KitchenCollection, UUID> {

    private final KitchenCollectionRepository collectionRepository;

    public KitchenCollectionQueryService(
            KitchenCollectionRepository collectionRepository,
            NormalizationService normalizationService) {
        super(collectionRepository, normalizationService);
        this.collectionRepository = collectionRepository;
    }

    // ====================================
    // Find by ID
    // ====================================

    /**
     * Find by ID with full details (joins slug lookup internally)
     */
    @Cacheable(cacheNames = "kitchen-collections", key = "'id-detail:' + #id")
    public KitchenCollection findByIdWithDetails(UUID id) {
        KitchenCollection collection = collectionRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(
                KitchenCollection.class, id));

        return collectionRepository.findBySlugWithDetails(collection.getSlug())
            .orElseThrow(() -> new EntityNotFoundException(
                KitchenCollection.class, id));
    }

    /**
     * Find by ID with products
     */
    @Cacheable(cacheNames = "kitchen-collections", key = "'id-products:' + #id")
    public KitchenCollection findByIdWithProducts(UUID id) {
        KitchenCollection collection = collectionRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(
                KitchenCollection.class, id));

        return collectionRepository.findBySlugWithProducts(collection.getSlug())
            .orElseThrow(() -> new EntityNotFoundException(
                KitchenCollection.class, id));
    }

    // ====================================
    // Find by Slug
    // ====================================

    @Cacheable(cacheNames = "kitchen-collections", key = "'slug:' + #slug")
    public Optional<KitchenCollection> findBySlug(String slug) {
        return collectionRepository.findBySlug(slug);
    }

    @Cacheable(cacheNames = "kitchen-collections", key = "'slug-detail:' + #slug")
    public KitchenCollection findBySlugWithDetails(String slug) {
        return collectionRepository.findBySlugWithDetails(slug)
            .orElseThrow(() -> new EntityNotFoundException(
                KitchenCollection.class, slug));
    }

    @Cacheable(cacheNames = "kitchen-collections", key = "'slug-products:' + #slug")
    public KitchenCollection findBySlugWithProducts(String slug) {
        return collectionRepository.findBySlugWithProducts(slug)
            .orElseThrow(() -> new EntityNotFoundException(
                KitchenCollection.class, slug));
    }

    /**
     * Full URL lookup: /kitchens/{rangeSlug}/{collectionSlug}
     */
    @Cacheable(cacheNames = "kitchen-collections",
               key = "'path:' + #rangeSlug + ':' + #collectionSlug")
    public KitchenCollection findByPath(String rangeSlug, String collectionSlug) {
        return collectionRepository
            .findByRangeSlugAndCollectionSlug(rangeSlug, collectionSlug)
            .orElseThrow(() -> new EntityNotFoundException(
                KitchenCollection.class,
                rangeSlug + "/" + collectionSlug
            ));
    }

    // ====================================
    // Find by Range / FinishGroup
    // ====================================

    @Cacheable(cacheNames = "kitchen-collections", key = "'range:' + #rangeId")
    public List<KitchenCollection> findActiveByRange(UUID rangeId) {
        return collectionRepository.findActiveByRangeId(rangeId);
    }

    @Cacheable(cacheNames = "kitchen-collections",
               key = "'finish-group:' + #finishGroupId")
    public List<KitchenCollection> findActiveByFinishGroup(UUID finishGroupId) {
        return collectionRepository.findActiveByFinishGroupId(finishGroupId);
    }

    @Cacheable(cacheNames = "kitchen-collections",
               key = "'finish-group-detail:' + #finishGroupId")
    public List<KitchenCollection> findByFinishGroupWithDetails(UUID finishGroupId) {
        return collectionRepository.findByFinishGroupIdWithDetails(finishGroupId);
    }

    // ====================================
    // Sibling Collections (colour switcher)
    // ====================================

    @Cacheable(cacheNames = "kitchen-collections",
               key = "'siblings:' + #rangeId + ':' + #collectionId")
    public List<KitchenCollection> findSiblingCollections(
            UUID rangeId, UUID collectionId) {
        return collectionRepository.findSiblingsByRangeId(rangeId, collectionId);
    }

    // ====================================
    // Pagination
    // ====================================

    @Cacheable(cacheNames = "kitchen-collections",
               key = "'page:' + #pageable.pageNumber")
    public Page<KitchenCollection> findAll(Pageable pageable) {
        return collectionRepository.findAll(pageable);
    }

    // ====================================
    // Search
    // ====================================

    public Page<KitchenCollection> search(String searchTerm, Pageable pageable) {
        return collectionRepository.search(searchTerm, pageable);
    }

    // ====================================
    // Statistics
    // ====================================

    public long countActive() {
        return collectionRepository.countActive();
    }

    public long countByRange(UUID rangeId) {
        return collectionRepository.countByRangeId(rangeId);
    }

    // ====================================
    // AbstractQueryService
    // ====================================

    @Override
    protected String getEntityCacheName() {
        return "kitchen-collections";
    }

    @Override
    protected Class<KitchenCollection> getEntityClass() {
        return KitchenCollection.class;
    }
}