package com.example.demo.modules.product.application.command;

import com.example.demo.common.base.service.command.AbstractCommandService;
import com.example.demo.common.exception.EntityNotFoundException;
import com.example.demo.common.normalization.NormalizationService;
import com.example.demo.common.validation.result.ValidationResult;
import com.example.demo.modules.product.application.dto.request.*;
import com.example.demo.modules.product.application.mapper.CategoryMapper;
import com.example.demo.modules.product.application.validator.CategoryValidator;
import com.example.demo.modules.product.domain.model.Category;
import com.example.demo.modules.product.domain.repository.CategoryRepository;
import com.example.demo.modules.product.domain.repository.ProductRepository;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Category command service
 * 
 * Handles all write operations for categories
 * 
 * @author PIM Team
 * @version 1.0
 */
@Slf4j
@Service
@Transactional
public class CategoryCommandService extends AbstractCommandService<Category, UUID> {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final CategoryMapper categoryMapper;
    private final CategoryValidator categoryValidator;

    public CategoryCommandService(
            CategoryRepository categoryRepository,
            ProductRepository productRepository,
            CacheManager cacheManager,
            ApplicationEventPublisher eventPublisher,
            Validator validator,
            NormalizationService normalizationService,
            CategoryMapper categoryMapper,
            CategoryValidator categoryValidator) {
        super(categoryRepository, cacheManager, eventPublisher, validator, normalizationService);
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.categoryMapper = categoryMapper;
        this.categoryValidator = categoryValidator;
    }

    @Override
    protected List<String> getCacheNames() {
        return List.of("categories", "category-tree", "products", "product-list");
    }

    @Override
    protected Class<Category> getEntityClass() {
        return Category.class;
    }

    // ====================================
    // Category Creation
    // ====================================

    public Category createCategory(CreateCategoryRequest request) {
        log.info("Creating category: {}", request.getName());

        return createFromRequest(request, req -> {
            // Check for duplicate code
            if (categoryRepository.existsByCode(req.getCode())) {
                throw new IllegalArgumentException(
                    String.format("Category with code '%s' already exists", req.getCode()));
            }

            // Map to entity first
            Category category = categoryMapper.fromCreateRequest(req);

            // Set parent if specified
            if (req.getParentId() != null) {
                Category parent = categoryRepository.findById(req.getParentId())
                        .orElseThrow(() -> new EntityNotFoundException(Category.class, req.getParentId()));
                
                category.setParent(parent);

                // Validate hierarchy
                ValidationResult hierarchyValidation = categoryValidator.validateHierarchy(category);
                hierarchyValidation.throwIfInvalid();
            }

            return category;
        });
    }

    // ====================================
    // Category Updates
    // ====================================

    public Category updateCategory(UUID id, UpdateCategoryRequest request) {
        log.info("Updating category: {}", id);
        
        // Use the same pattern as createCategory
        Category entity = buildCategoryFromUpdateRequest(id, request);
        
        return update(id, entity);
    }

    /**
     * Build category entity from update request
     * Same pattern as create - validates, maps, and sets relationships
     */
    private Category buildCategoryFromUpdateRequest(UUID id, UpdateCategoryRequest request) {
        // 1. Pre-process
        request.preProcess();
        
        // 2. Normalize
        request.normalize();
        
        // 3. Validate request
        ValidationResult validationResult = validateRequest(request);
        validationResult.throwIfInvalid();
        
        // 4. Find existing entity
        Category existing = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Category.class, id));
        
        // 5. Map request to existing entity (updates in place)
        categoryMapper.updateEntityFromDto(request, existing);
        
        // 6. Handle parent relationship
        updateParentRelationship(request, existing);
        
        // 7. Domain validation
        ValidationResult hierarchyValidation = categoryValidator.validateHierarchy(existing);
        hierarchyValidation.throwIfInvalid();
        
        // 8. Post-process
        request.postProcess();
        
        return existing;
    }

    /**
     * Update parent relationship if changed
     */
    private void updateParentRelationship(UpdateCategoryRequest request, Category existing) {
        UUID currentParentId = existing.getParent() != null ? existing.getParent().getId() : null;
        UUID newParentId = request.getParentId();
        
        // Check if parent changed
        if (!java.util.Objects.equals(currentParentId, newParentId)) {
            if (newParentId != null) {
                // Prevent moving to itself
                if (existing.getId().equals(newParentId)) {
                    throw new IllegalArgumentException("Cannot set category as its own parent");
                }
                
                Category newParent = categoryRepository.findById(newParentId)
                        .orElseThrow(() -> new EntityNotFoundException(Category.class, newParentId));
                
                // Prevent circular reference
                if (isDescendant(newParent, existing)) {
                    throw new IllegalArgumentException("Cannot move category to its own descendant");
                }
                
                existing.setParent(newParent);
            } else {
                // Remove parent (move to root)
                existing.setParent(null);
            }
        }
    }

    // ====================================
    // Category Operations
    // ====================================

    /**
     * Move category to different parent
     */
    public void moveCategory(UUID id, MoveCategoryRequest request) {
        log.info("Moving category: {} to parent: {}", id, request.getNewParentId());

        // Normalize and validate request
        request.normalize();
        validateRequest(request).throwIfInvalid();

        Category category = findByIdOrThrow(id);

        // Prevent moving to itself
        if (id.equals(request.getNewParentId())) {
            throw new IllegalArgumentException("Cannot move category to itself");
        }

        // Get new parent
        Category newParent = null;
        if (request.getNewParentId() != null) {
            newParent = findByIdOrThrow(request.getNewParentId());

            // Prevent circular reference (moving to own descendant)
            if (isDescendant(newParent, category)) {
                throw new IllegalArgumentException("Cannot move category to its own descendant");
            }
        }

        // Move category
        category.setParent(newParent);
        
        if (request.getDisplayOrder() != null) {
            category.setDisplayOrder(request.getDisplayOrder());
        }

        // Validate hierarchy
        ValidationResult hierarchyValidation = categoryValidator.validateHierarchy(category);
        hierarchyValidation.throwIfInvalid();

        categoryRepository.save(category);
        publishDomainEvents(category);
        evictAllCaches();

        log.info("Category moved successfully: {}", id);
    }

    /**
     * Toggle category active status
     */
    public void toggleCategoryStatus(UUID id, ToggleCategoryStatusRequest request) {
        log.info("Toggling category status: {} to {}", id, request.getActive());

        request.normalize();
        validateRequest(request).throwIfInvalid();

        Category category = findByIdOrThrow(id);

        category.setActive(request.getActive());

        // Apply to children if requested
        if (Boolean.TRUE.equals(request.getApplyToChildren())) {
            applyStatusToChildren(category, request.getActive());
        }

        categoryRepository.save(category);
        publishDomainEvents(category);
        evictAllCaches();

        log.info("Category status toggled: {}", id);
    }

    /**
     * Reorder categories
     */
    public void reorderCategories(ReorderCategoriesRequest request) {
        log.info("Reordering {} categories", request.getCategoryOrders().size());

        request.normalize();
        validateRequest(request).throwIfInvalid();

        List<Category> categoriesToUpdate = request.getCategoryOrders().stream()
                .map(order -> {
                    Category category = findByIdOrThrow(order.getCategoryId());
                    category.setDisplayOrder(order.getDisplayOrder());
                    return category;
                })
                .toList();

        // Bulk save for better performance
        categoryRepository.saveAll(categoriesToUpdate);
        evictAllCaches();

        log.info("Categories reordered successfully");
    }

    // ====================================
    // Category Deletion
    // ====================================

    @Override
    protected void beforeDelete(Category category) {
        // Check if category has products
        boolean hasProducts = productRepository.existsByCategoryId(category.getId());
        
        // Check if category has children
        boolean hasChildren = categoryRepository.existsByParentId(category.getId());

        // Validate deletion
        ValidationResult deletionValidation = categoryValidator.validateDeletion(
                category, hasProducts, hasChildren);
        deletionValidation.throwIfInvalid();
    }

    /**
     * Soft delete category
     */
    public void softDelete(UUID id) {
        log.info("Soft deleting category: {}", id);

        Category category = findByIdOrThrow(id);

        // Check if category has products
        boolean hasProducts = productRepository.existsByCategoryId(id);
        boolean hasChildren = categoryRepository.existsByParentId(id);

        // Validate deletion
        ValidationResult deletionValidation = categoryValidator.validateDeletion(
                category, hasProducts, hasChildren);
        deletionValidation.throwIfInvalid();

        // Soft delete
        category.delete();

        categoryRepository.save(category);
        publishDomainEvents(category);
        evictAllCaches();

        log.info("Category soft deleted: {}", id);
    }

    /**
     * Restore soft-deleted category
     */
    public void restoreCategory(UUID id) {
        log.info("Restoring category: {}", id);

        Category category = findByIdOrThrow(id);

        if (!category.isDeleted()) {
            throw new IllegalStateException("Category is not deleted");
        }

        category.restore();

        categoryRepository.save(category);
        publishDomainEvents(category);
        evictAllCaches();

        log.info("Category restored: {}", id);
    }

    // ====================================
    // Helper Methods
    // ====================================

    private Category findByIdOrThrow(UUID id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Category.class, id));
    }

    /**
     * Check if potential parent is a descendant of category
     */
    private boolean isDescendant(Category potentialParent, Category category) {
        Category current = potentialParent;
        while (current != null) {
            if (current.getId().equals(category.getId())) {
                return true;
            }
            current = current.getParent();
        }
        return false;
    }

    /**
     * Apply status to all children recursively
     */
    private void applyStatusToChildren(Category category, Boolean active) {
        List<Category> children = categoryRepository.findByParentId(category.getId());
        children.forEach(child -> {
            child.setActive(active);
            applyStatusToChildren(child, active); // Recurse first
        });
        
        // Batch save for better performance
        if (!children.isEmpty()) {
            categoryRepository.saveAll(children);
        }
    }

    @Override
    protected ValidationResult validateDomain(Category category) {
        return categoryValidator.validate(category);
    }
}