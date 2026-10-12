package com.example.demo.modules.kitchen.application.mapper;

import com.example.demo.common.mapper.BaseMapper;
import com.example.demo.modules.kitchen.application.dto.response.KitchenFinishGroupResponse;
import com.example.demo.modules.kitchen.domain.model.KitchenFinishGroup;
import org.mapstruct.*;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS
)
public interface KitchenFinishGroupMapper
        extends BaseMapper<KitchenFinishGroup, KitchenFinishGroupResponse> {

    // ====================================
    // Entity → Response (MapStruct handles)
    // ====================================

    @Override
    @Mapping(target = "id",             source = "id")
    @Mapping(target = "name",           source = "name")
    @Mapping(target = "slug",           source = "slug")
    @Mapping(target = "description",    source = "description")
    @Mapping(target = "active",         source = "active")
    @Mapping(target = "sortOrder",      source = "sortOrder")
    @Mapping(target = "createdAt",      source = "createdAt")
    @Mapping(target = "updatedAt",      source = "updatedAt")
    @Mapping(target = "createdBy",      source = "createdBy")
    @Mapping(target = "updatedBy",      source = "updatedBy")
    @Mapping(target = "version",        source = "version")
    // ✅ Use nested source paths instead of expression + helper methods
    @Mapping(target = "rangeId",        source = "range.id")
    @Mapping(target = "rangeName",      source = "range.name")
    @Mapping(target = "finishTypeId",   source = "finishType.id")
    @Mapping(target = "finishTypeName", source = "finishType.name")
    @Mapping(target = "finishTypeCode", source = "finishType.code")
    @Mapping(target = "collectionCount", ignore = true) // set manually below
    @Mapping(target = "collections",    ignore = true)
    KitchenFinishGroupResponse toDto(KitchenFinishGroup finishGroup);

    // ====================================
    // toEntity — not supported
    // ====================================

    @Override
    default KitchenFinishGroup toEntity(KitchenFinishGroupResponse dto) {
        throw new UnsupportedOperationException(
            "Use factory methods on KitchenFinishGroup"
        );
    }

    // ====================================
    // toDtoList — override for null safety
    // ====================================

    @Override
    default List<KitchenFinishGroupResponse> toDtoList(List<KitchenFinishGroup> groups) {
        if (groups == null || groups.isEmpty()) {
            return Collections.emptyList();
        }
        return groups.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    // ====================================
    // Post-mapping: set computed fields
    // ====================================

    /**
     * Called automatically by MapStruct after toDto() completes.
     * Handles fields that cannot be derived from simple source paths.
     */
    @AfterMapping
    default void setComputedFields(
            KitchenFinishGroup group,
            @MappingTarget KitchenFinishGroupResponse response) {

        // collectionCount — derived from collection size
        if (group.getCollections() != null) {
            response.setCollectionCount((long) group.getCollections().size());
        } else {
            response.setCollectionCount(0L);
        }
    }
}