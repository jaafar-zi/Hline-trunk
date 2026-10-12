package com.example.demo.modules.kitchen.application.mapper;

import com.example.demo.common.mapper.BaseMapper;
import com.example.demo.modules.kitchen.application.dto.response.*;
import com.example.demo.modules.kitchen.domain.model.*;
import org.mapstruct.*;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Kitchen Range Mapper
 *
 * Fix: Removed uses = {KitchenFinishGroupMapper.class, KitchenCollectionMapper.class}
 * from @Mapper annotation — these caused the "erroneous element null" error
 * because MapStruct tried to auto-wire them into generated mappings.
 * Instead we inject them manually via Spring and call them explicitly.
 */
@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
    injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
public abstract class KitchenRangeMapper
        implements BaseMapper<KitchenRange, KitchenRangeResponse> {

    // ====================================
    // Entity → Response (flat fields only)
    // ====================================

    @Override
    @Mapping(target = "id", source = "id")
    @Mapping(target = "code", source = "code")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "slug", source = "slug")
    @Mapping(target = "shortDescription", source = "shortDescription")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "heroImageUrl", source = "heroImageUrl")
    @Mapping(target = "active", source = "active")
    @Mapping(target = "sortOrder", source = "sortOrder")
    @Mapping(target = "metaTitle", source = "metaTitle")
    @Mapping(target = "metaDescription", source = "metaDescription")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "updatedAt", source = "updatedAt")
    @Mapping(target = "createdBy", source = "createdBy")
    @Mapping(target = "updatedBy", source = "updatedBy")
    @Mapping(target = "version", source = "version")
    @Mapping(target = "finishGroups", ignore = true)
    @Mapping(target = "collectionCount", ignore = true)
    @Mapping(target = "finishGroupCount", ignore = true)
    public abstract KitchenRangeResponse toDto(KitchenRange range);

    @Override
    public KitchenRange toEntity(KitchenRangeResponse dto) {
        throw new UnsupportedOperationException(
            "Use factory methods on KitchenRange");
    }

    @Override
    public List<KitchenRangeResponse> toDtoList(List<KitchenRange> ranges) {
        if (ranges == null || ranges.isEmpty()) return Collections.emptyList();
        return ranges.stream().map(this::toDto).collect(Collectors.toList());
    }
}