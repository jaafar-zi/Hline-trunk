package com.example.demo.domain.productManagementDomain.mapper;

import com.example.demo.common.mapper.AbstractBaseMapper;
import com.example.demo.domain.productManagementDomain.dto.ProductAttributeDTO;
import com.example.demo.domain.productManagementDomain.entity.ProductAttribute;
import com.example.demo.domain.productManagementDomain.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductAttributeMapper extends AbstractBaseMapper<ProductAttribute, ProductAttributeDTO> {

    private final ProductRepository productRepository;

    @Override
    public ProductAttributeDTO toDto(ProductAttribute entity) {
        if (entity == null) return null;

        Long productId = entity.getProducts() == null || entity.getProducts().isEmpty()
                ? null
                : entity.getProducts().get(0).getId();

        ProductAttributeDTO dto = new ProductAttributeDTO();
        dto.setId(entity.getId());
        dto.setProductId(productId);
        dto.setAttributeName(entity.getAttributeName());
        dto.setAttributeValue(entity.getAttributeValue());
        dto.setLang(entity.getLang());
        return dto;
    }

    @Override
    public ProductAttribute toEntity(ProductAttributeDTO dto) {
        if (dto == null) return null;

        ProductAttribute entity = new ProductAttribute();
        entity.setId(dto.getId());
        entity.setAttributeName(dto.getAttributeName());
        entity.setAttributeValue(dto.getAttributeValue());
        entity.setLang(dto.getLang());

        if (dto.getProductId() != null) {
            productRepository.findById(dto.getProductId())
                    .ifPresent(entity::setProduct);
        }

        return entity;
    }

    @Override
    public void updateEntityFromDto(ProductAttributeDTO dto, ProductAttribute entity) {
        if (dto.getAttributeName() != null) entity.setAttributeName(dto.getAttributeName());
        if (dto.getAttributeValue() != null) entity.setAttributeValue(dto.getAttributeValue());
        if (dto.getLang() != null) entity.setLang(dto.getLang());

        if (dto.getProductId() != null) {
            productRepository.findById(dto.getProductId())
                    .ifPresent(entity::setProduct);
        }
    }
}
