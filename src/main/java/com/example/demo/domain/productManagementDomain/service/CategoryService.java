package com.example.demo.domain.productManagementDomain.service;

import com.example.demo.common.base.service.AbstractBaseService;
import com.example.demo.domain.productManagementDomain.dto.CategoryDTO;
import com.example.demo.domain.productManagementDomain.entity.Category;
import com.example.demo.domain.productManagementDomain.mapper.CategoryMapper;
import com.example.demo.domain.productManagementDomain.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class CategoryService extends AbstractBaseService<Category, Long> {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    public CategoryService(CategoryRepository repository,CategoryMapper categoryMapper) {
        super(repository);
        this.categoryRepository = repository;
        this.categoryMapper = categoryMapper;
    }

    @Override
    protected String getCacheName() {
        return "categories";
    }

    @Override
    protected Class<?> getEntityClass() {
        return Category.class;
    }

    // -----------------------
    // DOMAIN LOGIC
    // -----------------------

    @Transactional
    public CategoryDTO createCategory(CategoryDTO dto) {
        Category category = categoryMapper.toEntity(dto);
        if (dto.getParentCategoryId() != null) {
            category.setParentCategory(categoryRepository.findById(dto.getParentCategoryId()).orElse(null));
        }
        Category saved = save(category);
        return categoryMapper.toDto(saved);
    }

    public CategoryDTO getCategoryById(Long id) {
        return categoryMapper.toDto(findByIdOrThrow(id));
    }

    public CategoryDTO getCategoryByName(String name) {
        return categoryRepository.findByName(name)
                .map(category -> categoryMapper.toDto(category))
                .orElseThrow(() -> new RuntimeException("Category not found"));
    }

    public List<CategoryDTO> getRootCategories() {
        return categoryRepository.findByParentCategoryIsNull()
                .stream().map(category -> categoryMapper.toDto(category))
                .collect(Collectors.toList());
    }

    public List<CategoryDTO> getSubcategories(Long parentId) {
        return categoryRepository.findByParentCategoryId(parentId)
                .stream().map(category -> categoryMapper.toDto(category))
                .collect(Collectors.toList());
    }

    public List<CategoryDTO> getAllCategories() {
        return getCategoryDTOs(false, null, true);
    }

    public Map<Long, CategoryDTO> getCategoryDTOMapByIds(List<Long> categoryIds, boolean onlyRoots) {
        Map<Long, CategoryDTO> categoryMap = new HashMap<>();
        List<CategoryDTO> categories = categoryRepository.getCategoryDTOs(onlyRoots, categoryIds);
        categories.forEach(category -> categoryMap.putIfAbsent(category.getId(), category));
        return categoryMap;
    }

    public List<CategoryDTO> getCategoryDTOs(boolean onlyRoots, List<Long> categoryIds, boolean nested) {
        List<CategoryDTO> flatList = categoryRepository.getCategoryDTOs(onlyRoots, categoryIds);
        return nested ? buildCategoryHierarchy(flatList) : flatList;
    }

    private List<CategoryDTO> buildCategoryHierarchy(List<CategoryDTO> flatList) {
        Map<Long, CategoryDTO> idToCategoryMap = flatList.stream()
                .collect(Collectors.toMap(CategoryDTO::getId, Function.identity()));

        List<CategoryDTO> roots = new ArrayList<>();

        for (CategoryDTO dto : flatList) {
            Long parentId = dto.getParentCategoryId();
            if (parentId != null) {
                CategoryDTO parent = idToCategoryMap.get(parentId);
                if (parent != null) {
                    parent.getSubcategories().add(dto);
                }
            } else {
                roots.add(dto);
            }
        }
        return roots;
    }

    @Transactional
    public CategoryDTO updateCategory(Long id, CategoryDTO dto) {
        Category existing = findByIdOrThrow(id);
        existing.setName(dto.getName());
        existing.setDescription(dto.getDescription());

        if (dto.getParentCategoryId() != null) {
            existing.setParentCategory(categoryRepository.findById(dto.getParentCategoryId()).orElse(null));
        }

        Category updated = update(id, existing);
        return categoryMapper.toDto(updated);
    }

    @Transactional
    public void deleteCategory(Long id) {
        delete(id);
    }
}

