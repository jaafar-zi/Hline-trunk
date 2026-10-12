package com.example.demo.modules.product.application.query;

import com.example.demo.common.annotation.ReadOnly;
import com.example.demo.common.base.service.query.AbstractQueryService;
import com.example.demo.common.exception.EntityNotFoundException;
import com.example.demo.common.normalization.NormalizationService;
import com.example.demo.modules.product.application.dto.filter.CategoryFilter;
import com.example.demo.modules.product.application.dto.response.CategoryResponse;
import com.example.demo.modules.product.application.mapper.CategoryMapper;
import com.example.demo.modules.product.domain.model.Category;
import com.example.demo.modules.product.domain.repository.CategoryRepository;
import com.example.demo.modules.product.domain.specification.CategorySpecifications;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Category query service
 * 
 * @author PIM Team
 * @version 3.0
 */
@Slf4j
@Service
@Transactional(readOnly = true)
public class CategoryQueryService extends AbstractQueryService<Category, UUID> {

    private static final Set<String> VALID_SORT_FIELDS = Set.of(
        "name", "code", "displayOrder", "createdAt", "updatedAt"
    );

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final CategorySpecifications  categorySpecification;

    public CategoryQueryService(
            CategoryRepository categoryRepository,
            NormalizationService normalizationService,
            CategoryMapper categoryMapper) {
        super(categoryRepository, normalizationService);
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
        this.categorySpecification = new CategorySpecifications();
    }

    @Override
    protected String getEntityCacheName() {
        return "categories";
    }

    @Override
    protected Class<Category> getEntityClass() {
        return Category.class;
    }

    @Override
    protected boolean isValidSortField(String field) {
        return VALID_SORT_FIELDS.contains(field);
    }

    @Override
    protected String getDefaultSortField() {
        return "displayOrder";
    }

    // ====================================
    // Filter-Based Queries
    // ====================================

    @ReadOnly
    public Page<CategoryResponse> searchCategories(CategoryFilter filter) {
        log.debug("Searching categories with filter");

        Specification<Category> spec = categorySpecification.fromFilter(filter);
        Page<Category> categories = findWithFilter(filter, spec);

        return categories.map(categoryMapper::toResponse);
    }

    @ReadOnly
    public List<CategoryResponse> findAllCategories(CategoryFilter filter) {
        log.debug("Finding all categories with filter");

        Specification<Category> spec = categorySpecification.fromFilter(filter);
        List<Category> categories = findAllWithFilter(filter, spec);

        return categories.stream()
                .map(categoryMapper::toResponse)
                .collect(Collectors.toList());
    }

    // ====================================
    // Specific Queries
    // ====================================

    @ReadOnly
    public CategoryResponse getCategoryById(UUID id) {
        Category category = findById(id);
        return categoryMapper.toResponse(category);
    }

    @ReadOnly
    public CategoryResponse getCategoryByCode(String code) {
        Category category = categoryRepository.findByCode(code)
                .orElseThrow(() -> new EntityNotFoundException(
                        Category.class, "code"));
        return categoryMapper.toResponse(category);
    }

    @ReadOnly
    public List<CategoryResponse> getRootCategories() {
        log.debug("Getting root categories");

        CategoryFilter filter = CategoryFilter.builder()
                .rootOnly(true)
                .activeOnly(true)
                .build();

        return findAllCategories(filter);
    }

    @ReadOnly
    public List<CategoryResponse> getChildCategories(UUID parentId) {
        log.debug("Getting child categories of: {}", parentId);

        CategoryFilter filter = CategoryFilter.builder()
                .parentId(parentId)
                .activeOnly(true)
                .build();

        return findAllCategories(filter);
    }

    @ReadOnly
    public boolean codeExists(String code) {
        return categoryRepository.existsByCode(code);
    }
}