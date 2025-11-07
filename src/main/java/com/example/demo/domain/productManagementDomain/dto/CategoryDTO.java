package com.example.demo.domain.productManagementDomain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CategoryDTO {
    private Long id;
    private String name;
    private String description;
    private Long parentCategoryId; // Reference to parent category
    private List<CategoryDTO> subcategories; // Nested subcategories

    public CategoryDTO(Long id, String name, String description, Long parentCategoryId) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.parentCategoryId = parentCategoryId;
        this.subcategories = null; // safely ignored in projection
    }
}

