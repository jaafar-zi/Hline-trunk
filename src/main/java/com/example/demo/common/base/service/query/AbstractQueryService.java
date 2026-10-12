package com.example.demo.common.base.service.query;

import com.example.demo.common.annotation.ReadOnly;
import com.example.demo.common.base.dto.BaseFilter;
import com.example.demo.common.exception.EntityNotFoundException;
import com.example.demo.common.normalization.NormalizationService;
import com.example.demo.common.validation.result.ValidationResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

/**
 * Abstract query service implementation with validation support
 *
 * Features:
 * - Read operations with caching
 * - Specification-based queries
 * - Pagination support
 * - Filter validation and normalization
 * - Routes to read replica database
 *
 * @param <T> Entity type
 * @param <ID> ID type
 *
 * @author Architecture Team
 * @version 1.0
 */
@Slf4j
public abstract class AbstractQueryService<T, ID> implements QueryService<T, ID> {

    protected final JpaRepository<T, ID> repository;
    protected final NormalizationService normalizationService;

    protected AbstractQueryService(
            JpaRepository<T, ID> repository,
            NormalizationService normalizationService) {
        this.repository = repository;
        this.normalizationService = normalizationService;
    }

    // ====================================
    // Abstract Methods
    // ====================================

    /**
     * Get cache name for entity caching
     */
    protected abstract String getEntityCacheName();

    /**
     * Get entity class for logging and error handling
     */
    protected abstract Class<T> getEntityClass();

    /**
     * Get cache TTL in seconds
     */
    protected long getCacheTtlSeconds() {
        return 1800; // 30 minutes default
    }

    // ====================================
    // Cached Queries
    // ====================================

    @Override
    @ReadOnly
    @Cacheable(
            cacheNames = "#{target.entityCacheName}",
            key = "'id:' + #id",
            unless = "#result == null"
    )
    public T findById(ID id) {
        String entityName = getEntityClass().getSimpleName();
        log.debug("Finding {} by ID: {} (cacheable)", entityName, id);

        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(getEntityClass(), id));
    }

    @Override
    @ReadOnly
    @Cacheable(
            cacheNames = "#{target.entityCacheName}",
            key = "'all'",
            unless = "#result == null || #result.isEmpty()"
    )
    public List<T> findAll() {
        String entityName = getEntityClass().getSimpleName();
        log.debug("Finding all {} (cacheable)", entityName);

        return repository.findAll();
    }

    @Override
    @ReadOnly
    @Cacheable(
            cacheNames = "#{target.entityCacheName}",
            key = "'page:' + #pageable.pageNumber + ':' + #pageable.pageSize + ':' + #pageable.sort.toString()",
            unless = "#result == null || #result.isEmpty()"
    )
    public Page<T> findAll(Pageable pageable) {
        String entityName = getEntityClass().getSimpleName();
        log.debug("Finding paginated {} (cacheable)", entityName);

        return repository.findAll(pageable);
    }

    // ====================================
    // Non-Cached Queries
    // ====================================

    @Override
    @ReadOnly
    public Optional<T> findByIdOptional(ID id) {
        String entityName = getEntityClass().getSimpleName();
        log.debug("Finding {} by ID: {} (non-cached)", entityName, id);

        return repository.findById(id);
    }

    @Override
    @ReadOnly
    public boolean existsById(ID id) {
        return repository.existsById(id);
    }

    @Override
    @ReadOnly
    public long count() {
        return repository.count();
    }

    @Override
    @ReadOnly
    public List<T> findAllById(List<ID> ids) {
        String entityName = getEntityClass().getSimpleName();
        log.debug("Finding {} entities by {} IDs", entityName, ids.size());

        return repository.findAllById(ids);
    }

    // ====================================
    // Specification Queries
    // ====================================

    @ReadOnly
    @SuppressWarnings("unchecked")
    public List<T> findAll(Specification<T> spec) {
        if (!(repository instanceof JpaSpecificationExecutor)) {
            throw new UnsupportedOperationException(
                    "Repository must extend JpaSpecificationExecutor for specification queries"
            );
        }
        return ((JpaSpecificationExecutor<T>) repository).findAll(spec);
    }

    @ReadOnly
    @SuppressWarnings("unchecked")
    public Page<T> findAll(Specification<T> spec, Pageable pageable) {
        if (!(repository instanceof JpaSpecificationExecutor)) {
            throw new UnsupportedOperationException(
                    "Repository must extend JpaSpecificationExecutor for specification queries"
            );
        }
        return ((JpaSpecificationExecutor<T>) repository).findAll(spec, pageable);
    }

    @ReadOnly
    @SuppressWarnings("unchecked")
    public long count(Specification<T> spec) {
        if (!(repository instanceof JpaSpecificationExecutor)) {
            throw new UnsupportedOperationException(
                    "Repository must extend JpaSpecificationExecutor for specification queries"
            );
        }
        return ((JpaSpecificationExecutor<T>) repository).count(spec);
    }

    @ReadOnly
    @SuppressWarnings("unchecked")
    public Optional<T> findOne(Specification<T> spec) {
        if (!(repository instanceof JpaSpecificationExecutor)) {
            throw new UnsupportedOperationException(
                    "Repository must extend JpaSpecificationExecutor for specification queries"
            );
        }
        return ((JpaSpecificationExecutor<T>) repository).findOne(spec);
    }

    // ====================================
    // Filter-Based Queries (NEW)
    // ====================================

    /**
     * Find with filter - normalizes and validates filter before querying
     *
     * @param filter Filter criteria
     * @param spec Specification builder from filter
     * @return Page of results
     */
    @ReadOnly
    @SuppressWarnings("unchecked")
    protected <F extends BaseFilter> Page<T> findWithFilter(F filter, Specification<T> spec) {
        String entityName = getEntityClass().getSimpleName();
        log.debug("Finding {} with filter: {}", entityName, filter.getClass().getSimpleName());

        // 1. Normalize filter
        filter.normalize();

        // 2. Validate filter (optional - can be lenient for queries)
        ValidationResult validationResult = validateFilter(filter);
        if (!validationResult.isValid()) {
            log.warn("Filter validation failed: {}", validationResult.getErrorMessages());
            // For queries, we can be lenient and continue with warnings
            validationResult.getWarningMessages().forEach(warning -> 
                log.debug("Filter warning: {}", warning)
            );
        }

        // 3. Build pageable
        Pageable pageable = buildPageable(filter);

        // 4. Execute query
        if (!(repository instanceof JpaSpecificationExecutor)) {
            throw new UnsupportedOperationException(
                    "Repository must extend JpaSpecificationExecutor for filter queries"
            );
        }

        return ((JpaSpecificationExecutor<T>) repository).findAll(spec, pageable);
    }

    /**
     * Find all with filter (no pagination)
     */
    @ReadOnly
    @SuppressWarnings("unchecked")
    protected List<T> findAllWithFilter(BaseFilter filter, Specification<T> spec) {
        String entityName = getEntityClass().getSimpleName();
        log.debug("Finding all {} with filter: {}", entityName, filter.getClass().getSimpleName());

        // Normalize and validate
        filter.normalize();
        ValidationResult validationResult = validateFilter(filter);
        
        if (!validationResult.isValid()) {
            log.warn("Filter validation failed: {}", validationResult.getErrorMessages());
        }

        // Build sort
        Sort sort = buildSort(filter);

        if (!(repository instanceof JpaSpecificationExecutor)) {
            throw new UnsupportedOperationException(
                    "Repository must extend JpaSpecificationExecutor for filter queries"
            );
        }

        return ((JpaSpecificationExecutor<T>) repository).findAll(spec, sort);
    }

    /**
     * Count with filter
     */
    @ReadOnly
    @SuppressWarnings("unchecked")
    protected long countWithFilter(BaseFilter filter, Specification<T> spec) {
        filter.normalize();
        
        if (!(repository instanceof JpaSpecificationExecutor)) {
            throw new UnsupportedOperationException(
                    "Repository must extend JpaSpecificationExecutor for filter queries"
            );
        }

        return ((JpaSpecificationExecutor<T>) repository).count(spec);
    }

    // ====================================
    // Filter Validation & Building (NEW)
    // ====================================

    /**
     * Validate filter (override for custom validation)
     * Default implementation is lenient (warnings only)
     */
    protected ValidationResult validateFilter(BaseFilter filter) {
        // Default: always valid for queries
        return ValidationResult.success("Filter validation");
    }

    /**
     * Build Pageable from filter
     */
    protected Pageable buildPageable(BaseFilter filter) {
        Sort sort = buildSort(filter);
        
        int page = filter.getEffectivePage();
        int size = filter.getEffectiveSize();

        return PageRequest.of(page, size, sort);
    }

    /**
     * Build Sort from filter
     */
    protected Sort buildSort(BaseFilter filter) {
        String sortBy = filter.getSortBy();
        String sortDirection = filter.getSortDirection();

        if (sortBy == null || sortBy.isBlank()) {
            return Sort.unsorted();
        }

        // Validate sort field (override in subclass for entity-specific fields)
        if (!isValidSortField(sortBy)) {
            log.warn("Invalid sort field: {}, using default", sortBy);
            sortBy = getDefaultSortField();
        }

        Sort.Direction direction = "ASC".equalsIgnoreCase(sortDirection)
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        return Sort.by(direction, sortBy);
    }

    /**
     * Check if sort field is valid (override in subclass)
     */
    protected boolean isValidSortField(String field) {
        // Default: allow any field
        return true;
    }

    /**
     * Get default sort field (override in subclass)
     */
    protected String getDefaultSortField() {
        return "id";
    }

    // ====================================
    // Helper Methods
    // ====================================

    /**
     * Find by ID or throw exception
     */
    @ReadOnly
    protected T findByIdOrThrow(ID id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(getEntityClass(), id));
    }

    /**
     * Check if entity exists or throw exception
     */
    protected void ensureExists(ID id) {
        if (!repository.existsById(id)) {
            throw new EntityNotFoundException(getEntityClass(), id);
        }
    }

    /**
     * Normalize filter using normalization service
     */
    protected <F extends BaseFilter> F normalizeFilter(F filter) {
        filter.normalize();
        return filter;
    }
}