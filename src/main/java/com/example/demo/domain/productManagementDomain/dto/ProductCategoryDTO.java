package com.example.demo.domain.productManagementDomain.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
public class ProductCategoryDTO {
    private Long id;
    private String name;
    private String description;
    private Long parentCategoryId;
    private List<Long> subcategoryIds;

    // Constructor with String argument for subcategoryIds
    public ProductCategoryDTO(Long id, String name, String description, Long parentCategoryId, String subcategoryIds) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.parentCategoryId = parentCategoryId;
        this.subcategoryIds = (subcategoryIds == null || subcategoryIds.isEmpty())
                ? Collections.emptyList()
                : Arrays.stream(subcategoryIds.split(","))
                .map(Long::valueOf)
                .collect(Collectors.toList());
    }
}

