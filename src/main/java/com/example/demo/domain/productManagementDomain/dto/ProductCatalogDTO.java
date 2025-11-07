package com.example.demo.domain.productManagementDomain.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class ProductCatalogDTO {
    private long id;
    private String name;
    private Integer stockQuantity;
    private CategoryDTO productCategory;
    private ProductPricingDTO productPricing;
}
