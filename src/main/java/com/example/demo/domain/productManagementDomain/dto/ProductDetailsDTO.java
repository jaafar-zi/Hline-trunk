package com.example.demo.domain.productManagementDomain.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@SuperBuilder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ProductDetailsDTO extends ProductCatalogDTO{
    private List<ProductAttributeDTO> productAttributes;
    private List<AssetDTO> productAssets;
}
