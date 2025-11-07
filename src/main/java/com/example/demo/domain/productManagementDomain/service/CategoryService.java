package com.example.demo.domain.productManagementDomain.service;

import com.example.demo.domain.productManagementDomain.dto.CategoryDTO;
import com.example.demo.domain.productManagementDomain.entity.Category;
import com.example.demo.domain.productManagementDomain.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    // Convert Entity to DTO (used for simple fetches like findById, etc.)
    public CategoryDTO mapToDTO(Category category) {
        return CategoryDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .parentCategoryId(category.getParentCategory() != null ? category.getParentCategory().getId() : null)
                .subcategories(category.getSubcategories().stream()
                        .map(this::mapToDTO)
                        .collect(Collectors.toList()))
                .build();
    }

    public Category mapToEntity(CategoryDTO dto) {
        Category category = new Category();
        category.setId(dto.getId());
        category.setName(dto.getName());
        category.setDescription(dto.getDescription());
        return category;
    }

    public List<CategoryDTO> getAllCategories() {
        // Only fetch root categories and recursively build subcategory tree
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
                    if (parent.getSubcategories() == null) {
                        parent.setSubcategories(new ArrayList<>());
                    }
                    parent.getSubcategories().add(dto);
                }
            } else {
                roots.add(dto);
            }
        }

        return roots;
    }

    @Transactional
    public CategoryDTO createCategory(CategoryDTO dto) {
        Category category = mapToEntity(dto);
        if (dto.getParentCategoryId() != null) {
            category.setParentCategory(categoryRepository.findById(dto.getParentCategoryId()).orElse(null));
        }
        Category savedCategory = categoryRepository.save(category);
        return mapToDTO(savedCategory);
    }

    public CategoryDTO getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .map(this::mapToDTO)
                .orElseThrow(() -> new RuntimeException("Category not found"));
    }

    public CategoryDTO getCategoryByName(String name) {
        return categoryRepository.findByName(name)
                .map(this::mapToDTO)
                .orElseThrow(() -> new RuntimeException("Category not found"));
    }

    public List<CategoryDTO> getRootCategories() {
        return categoryRepository.findByParentCategoryIsNull()
                .stream().map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<CategoryDTO> getSubcategories(Long parentId) {
        return categoryRepository.findByParentCategoryId(parentId)
                .stream().map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public CategoryDTO updateCategory(Long id, CategoryDTO dto) {
        Category existingCategory = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        existingCategory.setName(dto.getName());
        existingCategory.setDescription(dto.getDescription());

        if (dto.getParentCategoryId() != null) {
            existingCategory.setParentCategory(categoryRepository.findById(dto.getParentCategoryId()).orElse(null));
        }

        Category updatedCategory = categoryRepository.save(existingCategory);
        return mapToDTO(updatedCategory);
    }

    @Transactional
    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new RuntimeException("Category not found");
        }
        categoryRepository.deleteById(id);
    }
}
