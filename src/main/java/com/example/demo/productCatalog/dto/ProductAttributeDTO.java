package com.example.demo.productCatalog.dto;

import lombok.*;

@Getter
@Setter
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductAttributeDTO {

    private Long id; // ID of the attribute
    private Long productId;
    private String attributeName; // Name of the attribute (e.g., "Color", "Size")
    private String attributeValue; // Value of the attribute (e.g., "Red", "Large")

}

