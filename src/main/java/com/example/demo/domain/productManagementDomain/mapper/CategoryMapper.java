package com.example.demo.domain.productManagementDomain.mapper;

import com.example.demo.common.mapper.AbstractBaseMapper;
import com.example.demo.domain.productManagementDomain.dto.CategoryDTO;
import com.example.demo.domain.productManagementDomain.entity.Category;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.stream.Collectors;

@Component
public class CategoryMapper extends AbstractBaseMapper<Category, CategoryDTO> {

    @Override
    public CategoryDTO toDto(Category entity) {
        if (entity == null) return null;

        CategoryDTO.CategoryDTOBuilder builder = CategoryDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .parentCategoryId(entity.getParentCategory() != null ? entity.getParentCategory().getId() : null)
                .subcategories(entity.getSubcategories() != null
                        ? entity.getSubcategories().stream()
                        .map(this::toDto)
                        .collect(Collectors.toList())
                        : new ArrayList<>());

        return builder.build();
    }

    @Override
    public Category toEntity(CategoryDTO dto) {
        if (dto == null) return null;

        Category entity = new Category();
        entity.setId(dto.getId());
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        return entity;
    }

    @Override
    public void updateEntityFromDto(CategoryDTO dto, Category entity) {
        if (dto.getName() != null) entity.setName(dto.getName());
        if (dto.getDescription() != null) entity.setDescription(dto.getDescription());
    }
}
