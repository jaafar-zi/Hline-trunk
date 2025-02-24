package com.example.demo.productCatalog.dto;

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
}

