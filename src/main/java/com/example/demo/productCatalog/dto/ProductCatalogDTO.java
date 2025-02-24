package com.example.demo.productCatalog.dto;

import lombok.*;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductCatalogDTO {
    private long id;
    private String name;
    private Integer stockQuantity;
    private ProductCategoryDTO productCategory;
    private ProductPricingDTO productPricing;

    public ProductCatalogDTO(long id,String name,Integer stockQuantity,ProductCategoryDTO productCategory){
        this.id = id;
        this.name = name;
        this.stockQuantity = stockQuantity;
        this.productCategory = productCategory;
    }
}
