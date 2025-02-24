package com.example.demo.productCatalog.service;

import com.example.demo.productCatalog.dto.CategoryDTO;
import com.example.demo.productCatalog.entity.Category;
import com.example.demo.productCatalog.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    // Convert Entity to DTO
    private CategoryDTO mapToDTO(Category category) {
        return CategoryDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .parentCategoryId(category.getParentCategory() != null ? category.getParentCategory().getId() : null)
                .subcategories(category.getSubcategories().stream().map(this::mapToDTO).collect(Collectors.toList()))
                .build();
    }

    // Convert DTO to Entity
    private Category mapToEntity(CategoryDTO dto) {
        Category category = new Category();
        category.setId(dto.getId());
        category.setName(dto.getName());
        category.setDescription(dto.getDescription());
        return category;
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


    public List<CategoryDTO> getAllCategories() {
        return categoryRepository.findAll()
                .stream().map(this::mapToDTO)
                .collect(Collectors.toList());
    }


    public CategoryDTO getCategoryById(Long id) {
        return categoryRepository.findById(id).map(this::mapToDTO)
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
