package com.example.demo.common.base.service;

import com.example.demo.common.exception.EntityNotFoundException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Generic base implementation for CRUD services.
 * Includes Spring Cache integration and extension hooks for audit/indexing.
 */
public abstract class AbstractBaseService<T, ID> implements BaseService<T, ID> {

    protected final JpaRepository<T, ID> repository;

    protected AbstractBaseService(JpaRepository<T, ID> repository) {
        this.repository = repository;
    }

    /**
     * Returns the cache name for the entity type (e.g. "products").
     */
    protected abstract String getCacheName();

    /**
     * Returns the entity class type, used for exception messages.
     */
    protected abstract Class<?> getEntityClass();

    /**
     * Hook for post-save logic (audit, index, etc.)
     */
    protected void onAfterSave(T entity) { }

    /**
     * Hook for post-delete logic (audit, index, etc.)
     */
    protected void onAfterDelete(ID id) { }

    // ---- CRUD Methods ---- //

    @Override
    @Transactional
    @CacheEvict(cacheNames = "#root.target.getCacheName()", allEntries = true)
    public T save(T entity) {
        T saved = repository.save(entity);
        onAfterSave(saved);
        return saved;
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = "#root.target.getCacheName()", allEntries = true)
    public T update(ID id, T entity) {
        findByIdOrThrow(id); // ensure exists
        T saved = repository.save(entity);
        onAfterSave(saved);
        return saved;
    }

    @Override
    @Cacheable(cacheNames = "#root.target.getCacheName()", key = "#id")
    public T findByIdOrThrow(ID id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(this.getEntityClass(), id));
    }

    @Override
    @Cacheable(cacheNames = "#root.target.getCacheName()", key = "'all'")
    public List<T> findAll() {
        return repository.findAll();
    }

    @Override
    public Optional<T> findById(ID id) {
        return repository.findById(id);
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = "#root.target.getCacheName()", allEntries = true)
    public void delete(ID id) {
        if (!repository.existsById(id)) {
            throw new EntityNotFoundException(getEntityClass(), id);
        }
        repository.deleteById(id);
        onAfterDelete(id);
    }
}
