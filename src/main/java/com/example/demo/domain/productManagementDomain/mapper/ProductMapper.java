package com.example.demo.domain.productManagementDomain.mapper;

import com.example.demo.common.mapper.AbstractBaseMapper;
import com.example.demo.domain.productManagementDomain.dto.ProductDTO;
import com.example.demo.domain.productManagementDomain.entity.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper extends AbstractBaseMapper<Product, ProductDTO> {

    @Override
    public ProductDTO toDto(Product entity) {
        if (entity == null) return null;

        return new ProductDTO(
                entity.getName(),
                entity.getStockQuantity(),
                entity.getCategory() != null ? entity.getCategory().getId() : null
        );
    }

    @Override
    public Product toEntity(ProductDTO dto) {
        if (dto == null) return null;

        Product entity = new Product();
        entity.setName(dto.getName());
        entity.setStockQuantity(dto.getStockQuantity());
        // Category should be set separately in the service if needed
        return entity;
    }

    @Override
    public void updateEntityFromDto(ProductDTO dto, Product entity) {
        if (dto.getName() != null) entity.setName(dto.getName());
        if (dto.getStockQuantity() != null) entity.setStockQuantity(dto.getStockQuantity());
        // Category updates can be handled separately if required
    }
}
