package com.example.demo.common.base.service.command;

import com.example.demo.common.base.dto.BaseRequest;
import com.example.demo.common.base.entity.AbstractAggregateRoot;
import com.example.demo.common.event.*;
import com.example.demo.common.exception.EntityNotFoundException;
import com.example.demo.common.normalization.NormalizationService;
import com.example.demo.common.validation.result.ValidationException;
import com.example.demo.common.validation.result.ValidationResult;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Slf4j
@Transactional
public abstract class AbstractCommandService<T, ID> implements CommandService<T, ID> {

    protected final JpaRepository<T, ID> repository;
    protected final CacheManager cacheManager;
    protected final ApplicationEventPublisher eventPublisher;
    protected final Validator validator;
    protected final NormalizationService normalizationService;

    protected AbstractCommandService(
            JpaRepository<T, ID> repository,
            CacheManager cacheManager,
            ApplicationEventPublisher eventPublisher,
            Validator validator,
            NormalizationService normalizationService) {
        this.repository = repository;
        this.cacheManager = cacheManager;
        this.eventPublisher = eventPublisher;
        this.validator = validator;
        this.normalizationService = normalizationService;
    }

    // ====================================
    // Abstract Methods
    // ====================================

    protected abstract List<String> getCacheNames();
    protected abstract Class<T> getEntityClass();

    // ====================================
    // CRUD Operations
    // ====================================

    @Override
    public T create(T entity) {
        String entityName = getEntityClass().getSimpleName();
        log.debug("Creating new {}", entityName);

        beforeCreate(entity);
        validateEntity(entity);

        T created = repository.save(entity);

        afterCreate(created);
        publishDomainEvents(created);

        // ── Replica sync ──────────────────────────────────────
        eventPublisher.publishEvent(new EntityCreatedEvent<>(created));
        log.debug("📤 EntityCreatedEvent published for {} id={}",
                entityName, extractId(created));
        // ─────────────────────────────────────────────────────

        evictAllCaches();
        log.info("Created {} with ID: {}", entityName, extractId(created));
        return created;
    }

    public <R extends BaseRequest> T createFromRequest(R request, RequestToEntityMapper<R, T> mapper) {
        String entityName = getEntityClass().getSimpleName();
        log.debug("Creating new {} from request", entityName);

        request.preProcess();
        request.normalize();

        ValidationResult validationResult = validateRequest(request);
        validationResult.throwIfInvalid();

        T entity = mapper.map(request);
        validateEntity(entity);

        beforeCreate(entity);
        T created = repository.save(entity);
        afterCreate(created);

        request.postProcess();

        publishDomainEvents(created);

        // ── Replica sync ──────────────────────────────────────
        eventPublisher.publishEvent(new EntityCreatedEvent<>(created));
        log.debug("📤 EntityCreatedEvent published for {} id={}",
                entityName, extractId(created));
        // ─────────────────────────────────────────────────────

        evictAllCaches();
        log.info("Created {} with ID: {} from request", entityName, extractId(created));
        return created;
    }

    @Override
    public T update(ID id, T entity) {
        String entityName = getEntityClass().getSimpleName();
        log.debug("Updating {} with ID: {}", entityName, id);

        T existing = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(getEntityClass(), id));

        beforeUpdate(existing, entity);
        validateEntity(entity);

        T updated = repository.save(entity);

        afterUpdate(updated);
        publishDomainEvents(updated);

        // ── Replica sync ──────────────────────────────────────
        eventPublisher.publishEvent(new EntityUpdatedEvent<>(updated));
        log.debug("📤 EntityUpdatedEvent published for {} id={}", entityName, id);
        // ─────────────────────────────────────────────────────

        evictAllCaches();
        evictSpecificCache(id);
        log.info("Updated {} with ID: {}", entityName, id);
        return updated;
    }

    public <R extends BaseRequest> T updateFromRequest(ID id, R request, RequestToEntityMapper<R, T> mapper) {
        String entityName = getEntityClass().getSimpleName();
        log.debug("Updating {} with ID: {} from request", entityName, id);

        T existing = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(getEntityClass(), id));

        request.preProcess();
        request.normalize();

        ValidationResult validationResult = validateRequest(request);
        validationResult.throwIfInvalid();

        T entity = mapper.map(request, existing);

        if (entity != existing) {
            log.warn("Mapper returned a different instance. This may cause issues with JPA.");
        }

        validateEntity(entity);

        beforeUpdate(existing, entity);
        T updated = repository.save(entity);
        afterUpdate(updated);

        request.postProcess();

        publishDomainEvents(updated);

        // ── Replica sync ──────────────────────────────────────
        eventPublisher.publishEvent(new EntityUpdatedEvent<>(updated));
        log.debug("📤 EntityUpdatedEvent published for {} id={}", entityName, id);
        // ─────────────────────────────────────────────────────

        evictAllCaches();
        evictSpecificCache(id);
        log.info("Updated {} with ID: {} from request", entityName, id);
        return updated;
    }

    @Override
    public void delete(ID id) {
        String entityName = getEntityClass().getSimpleName();
        log.debug("Deleting {} with ID: {}", entityName, id);

        T entity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(getEntityClass(), id));

        beforeDelete(entity);
        repository.deleteById(id);
        afterDelete(entity);

        // ── Replica sync ──────────────────────────────────────
        eventPublisher.publishEvent(
            new EntityDeletedEvent(getEntityClass(), id, getCurrentUsername(), false)
        );
        log.debug("📤 EntityDeletedEvent published for {} id={}", entityName, id);
        // ─────────────────────────────────────────────────────

        evictAllCaches();
        evictSpecificCache(id);
        publishDeleteEvent(id, entity);
        log.info("Deleted {} with ID: {}", entityName, id);
    }

    // ====================================
    // Bulk Operations
    // ====================================

    @Override
    public List<T> createAll(List<T> entities) {
        String entityName = getEntityClass().getSimpleName();
        log.debug("Bulk creating {} {} entities", entities.size(), entityName);

        entities.forEach(this::beforeCreate);
        entities.forEach(this::validateEntity);

        List<T> created = repository.saveAll(entities);

        created.forEach(this::afterCreate);
        created.forEach(this::publishDomainEvents);

        // ── Replica sync ──────────────────────────────────────
        eventPublisher.publishEvent(new EntityBulkCreatedEvent<>(created));
        log.debug("📤 EntityBulkCreatedEvent published for {} count={}",
                entityName, created.size());
        // ─────────────────────────────────────────────────────

        evictAllCaches();
        log.info("Bulk created {} {} entities", created.size(), entityName);
        return created;
    }

    @Override
    public List<T> updateAll(List<T> entities) {
        String entityName = getEntityClass().getSimpleName();
        log.debug("Bulk updating {} {} entities", entities.size(), entityName);

        entities.forEach(this::validateEntity);

        List<T> updated = repository.saveAll(entities);

        updated.forEach(this::afterUpdate);
        updated.forEach(this::publishDomainEvents);

        // ── Replica sync ──────────────────────────────────────
        eventPublisher.publishEvent(new EntityBulkUpdatedEvent<>(updated));
        log.debug("📤 EntityBulkUpdatedEvent published for {} count={}",
                entityName, updated.size());
        // ─────────────────────────────────────────────────────

        evictAllCaches();
        log.info("Bulk updated {} {} entities", updated.size(), entityName);
        return updated;
    }

    @Override
    public void deleteAll(List<ID> ids) {
        String entityName = getEntityClass().getSimpleName();
        log.debug("Bulk deleting {} {} entities", ids.size(), entityName);

        List<T> entities = repository.findAllById(ids);

        if (entities.size() != ids.size()) {
            throw new IllegalArgumentException(
                    String.format("Expected %d entities but found %d",
                            ids.size(), entities.size())
            );
        }

        entities.forEach(this::beforeDelete);
        repository.deleteAllById(ids);
        entities.forEach(this::afterDelete);

        // ── Replica sync ──────────────────────────────────────
        List<Object> objectIds = ids.stream()
                .map(id -> (Object) id)
                .toList();
        eventPublisher.publishEvent(
            new EntityBulkDeletedEvent(getEntityClass(), objectIds)
        );
        log.debug("📤 EntityBulkDeletedEvent published for {} count={}",
                entityName, ids.size());
        // ─────────────────────────────────────────────────────

        evictAllCaches();
        ids.forEach(this::evictSpecificCache);
        log.info("Bulk deleted {} {} entities", ids.size(), entityName);
    }

    // ====================================
    // Validation
    // ====================================

    protected <R extends BaseRequest> ValidationResult validateRequest(R request) {
        return validateRequest(request, new Class<?>[0]);
    }

    protected <R extends BaseRequest> ValidationResult validateRequest(R request, Class<?>... groups) {
        Set<ConstraintViolation<R>> violations = validator.validate(request, groups);

        if (violations.isEmpty()) {
            return ValidationResult.success("Request validation");
        }

        ValidationResult result = ValidationResult.builder()
                .valid(false)
                .context("Request Validation")
                .build();

        violations.forEach(violation -> {
            String field = violation.getPropertyPath().toString();
            String message = violation.getMessage();
            Object invalidValue = violation.getInvalidValue();
            result.addError(field, message, invalidValue);
        });

        return result;
    }

    protected void validateEntity(T entity) {
        Set<ConstraintViolation<T>> violations = validator.validate(entity);

        if (!violations.isEmpty()) {
            ValidationResult result = ValidationResult.builder()
                    .valid(false)
                    .context("Entity Validation")
                    .build();

            violations.forEach(violation -> {
                String field = violation.getPropertyPath().toString();
                String message = violation.getMessage();
                result.addError(field, message);
            });

            throw new ValidationException(result);
        }
    }

    protected ValidationResult validateDomain(T entity) {
        return ValidationResult.success("Domain validation");
    }

    // ====================================
    // Lifecycle Hooks
    // ====================================

    protected void beforeCreate(T entity) {}
    protected void afterCreate(T entity)  {}
    protected void beforeUpdate(T existing, T updated) {}
    protected void afterUpdate(T entity)  {}
    protected void beforeDelete(T entity) {}
    protected void afterDelete(T entity)  {}

    // ====================================
    // Cache Management
    // ====================================

    protected void evictAllCaches() {
        getCacheNames().forEach(cacheName -> {
            var cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                log.trace("Evicting cache: {}", cacheName);
                cache.clear();
            }
        });
    }

    protected void evictSpecificCache(ID id) {
        getCacheNames().forEach(cacheName -> {
            var cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                String key = "id:" + id;
                log.trace("Evicting cache key: {} from cache: {}", key, cacheName);
                cache.evict(key);
            }
        });
    }

    // ====================================
    // Domain Event Publishing
    // ====================================

    protected void publishDomainEvents(T entity) {
        if (entity instanceof AbstractAggregateRoot aggregate) {
            List<DomainEvent> events = aggregate.getDomainEvents();

            if (events.isEmpty()) {
                log.trace("No domain events to publish for: {}",
                        entity.getClass().getSimpleName());
                return;
            }

            events.forEach(event -> {
                log.debug("Publishing domain event: {}", event.getClass().getSimpleName());
                eventPublisher.publishEvent(event);
            });

            aggregate.clearDomainEvents();
        }
    }

    protected void publishDeleteEvent(ID id, T entity) {
        log.trace("Entity deleted: {} with ID: {}", getEntityClass().getSimpleName(), id);
    }

    // ====================================
    // Utility
    // ====================================

    protected Object extractId(T entity) {
        try {
            var idField = entity.getClass().getSuperclass().getDeclaredField("id");
            idField.setAccessible(true);
            return idField.get(entity);
        } catch (Exception e) {
            log.warn("Could not extract ID from entity", e);
            return "unknown";
        }
    }

    private String getCurrentUsername() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            return auth != null ? auth.getName() : "system";
        } catch (Exception e) {
            return "system";
        }
    }

    // ====================================
    // Mapper Interface
    // ====================================

    @FunctionalInterface
    public interface RequestToEntityMapper<R, E> {
        E map(R request);

        default E map(R request, E existing) {
            return map(request);
        }
    }
}