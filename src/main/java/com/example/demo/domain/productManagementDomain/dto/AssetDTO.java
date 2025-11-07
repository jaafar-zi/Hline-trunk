package com.example.demo.domain.productManagementDomain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AssetDTO {
    private Long id;
    private String name;
    private String contentType;
    private long size;
    private Long productId;
}
