package com.example.demo.domain.productManagementDomain.dto;

import lombok.*;

@Getter
@Setter
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductAttributeDTO {

    private Long id;
    private Long productId;
    private String attributeName;
    private String attributeValue;
    private String lang;

}

