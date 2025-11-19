package com.example.demo.domain.productManagementDomain.mapper;

import com.example.demo.common.mapper.AbstractBaseMapper;
import com.example.demo.domain.productManagementDomain.dto.AssetDTO;
import com.example.demo.domain.productManagementDomain.entity.Asset;
import org.springframework.stereotype.Component;

@Component
public class AssetMapper extends AbstractBaseMapper<Asset, AssetDTO> {

    @Override
    public AssetDTO toDto(Asset entity) {
        if (entity == null) return null;

        AssetDTO dto = new AssetDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setContentType(entity.getContentType());
        dto.setSize(entity.getSize());
        dto.setProductId(entity.getProduct() != null ? entity.getProduct().getId() : null);

        return dto;
    }

    @Override
    public Asset toEntity(AssetDTO dto) {
        if (dto == null) return null;

        Asset entity = new Asset();
        entity.setId(dto.getId());
        entity.setName(dto.getName());
        entity.setContentType(dto.getContentType());
        entity.setSize(dto.getSize());
        // ⚠️ Do not set `Product` directly — handled in service via repository fetch
        return entity;
    }

    @Override
    public void updateEntityFromDto(AssetDTO dto, Asset entity) {
        if (dto.getName() != null) entity.setName(dto.getName());
        if (dto.getContentType() != null) entity.setContentType(dto.getContentType());
        if (dto.getSize() != 0) entity.setSize(dto.getSize());
    }
}
