package com.example.demo.domain.productManagementDomain.mapper;

import com.example.demo.common.mapper.AbstractBaseMapper;
import com.example.demo.domain.productManagementDomain.dto.ProductPricingDTO;
import com.example.demo.domain.productManagementDomain.entity.Pricing;
import com.example.demo.domain.productManagementDomain.entity.Product;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class PricingMapper extends AbstractBaseMapper<Pricing, ProductPricingDTO> {

    @Override
    public ProductPricingDTO toDto(Pricing entity) {
        if (entity == null) return null;

        return ProductPricingDTO.builder()
                .id(entity.getId())
                .productIds(entity.getProducts() != null
                        ? entity.getProducts().stream()
                        .map(Product::getId)
                        .collect(Collectors.toList())
                        : null)
                .currency(entity.getCurrency())
                .oldPrice(entity.getOldPrice())
                .price(entity.getPrice())
                .promoFlag(entity.isPromoFlag())
                .build();
    }

    @Override
    public Pricing toEntity(ProductPricingDTO dto) {
        if (dto == null) return null;

        Pricing entity = new Pricing();
        entity.setId(dto.getId());
        entity.setCurrency(dto.getCurrency());
        entity.setOldPrice(dto.getOldPrice());
        entity.setPrice(dto.getPrice());
        entity.setPromoFlag(dto.isPromoFlag());
        // ⚠️ Product relations should be handled separately in the service layer
        return entity;
    }

    @Override
    public void updateEntityFromDto(ProductPricingDTO dto, Pricing entity) {
        if (dto.getCurrency() != null) entity.setCurrency(dto.getCurrency());
        if (dto.getOldPrice() != null) entity.setOldPrice(dto.getOldPrice());
        if (dto.getPrice() != null) entity.setPrice(dto.getPrice());
        entity.setPromoFlag(dto.isPromoFlag());
    }
}
