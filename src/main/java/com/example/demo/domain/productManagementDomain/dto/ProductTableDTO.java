package com.example.demo.domain.productManagementDomain.dto;

import lombok.*;

@Getter
@Setter
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductTableDTO {
    private long id;
    private String name;
    private long assetId;
    private String status;
    private int stockQuantity;
    private int reorderQuantity;
    private int reservedQuantity;

    private CategoryDTO productCategory;
}
