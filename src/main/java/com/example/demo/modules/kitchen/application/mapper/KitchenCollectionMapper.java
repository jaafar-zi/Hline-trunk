package com.example.demo.modules.kitchen.application.mapper;

import com.example.demo.common.mapper.BaseMapper;
import com.example.demo.modules.kitchen.application.dto.response.*;
import com.example.demo.modules.kitchen.domain.model.*;
import org.mapstruct.*;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Kitchen Collection Mapper
 *
 * Fix: Removed uses = {KitchenColourMapper.class} — caused null element error.
 * All nested object mappings done via explicit helper methods.
 * No @Mapping expressions that reference other mappers directly.
 */
@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
    injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
public abstract class KitchenCollectionMapper
        implements BaseMapper<KitchenCollection, KitchenCollectionResponse> {

    // ====================================
    // Helper Methods — all return primitives/strings/UUIDs only
    // ====================================

    protected UUID extractRangeId(KitchenCollection c) {
        return c.getFinishGroup() != null && c.getFinishGroup().getRange() != null
            ? c.getFinishGroup().getRange().getId() : null;
    }

    protected String extractRangeName(KitchenCollection c) {
        return c.getFinishGroup() != null && c.getFinishGroup().getRange() != null
            ? c.getFinishGroup().getRange().getName() : null;
    }

    protected String extractRangeSlug(KitchenCollection c) {
        return c.getFinishGroup() != null && c.getFinishGroup().getRange() != null
            ? c.getFinishGroup().getRange().getSlug() : null;
    }

    protected UUID extractFinishGroupId(KitchenCollection c) {
        return c.getFinishGroup() != null ? c.getFinishGroup().getId() : null;
    }

    protected String extractFinishGroupName(KitchenCollection c) {
        return c.getFinishGroup() != null ? c.getFinishGroup().getName() : null;
    }

    protected UUID extractColourId(KitchenCollection c) {
        return c.getColour() != null ? c.getColour().getId() : null;
    }

    protected String extractColourName(KitchenCollection c) {
        return c.getColour() != null ? c.getColour().getName() : null;
    }

    protected String extractColourHex(KitchenCollection c) {
        return c.getColour() != null ? c.getColour().getHexValue() : null;
    }

    // ====================================
    // Entity → Response (flat fields only)
    // ====================================

    @Override
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "slug", source = "slug")
    @Mapping(target = "shortDescription", source = "shortDescription")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "heroImageUrl", source = "heroImageUrl")
    @Mapping(target = "galleryImageUrls", source = "galleryImageUrls")
    @Mapping(target = "features", source = "features")
    @Mapping(target = "active", source = "active")
    @Mapping(target = "leadTimeDays", source = "leadTimeDays")
    @Mapping(target = "metaTitle", source = "metaTitle")
    @Mapping(target = "metaDescription", source = "metaDescription")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "updatedAt", source = "updatedAt")
    @Mapping(target = "createdBy", source = "createdBy")
    @Mapping(target = "updatedBy", source = "updatedBy")
    @Mapping(target = "version", source = "version")
    @Mapping(target = "finishGroupId",
             expression = "java(extractFinishGroupId(collection))")
    @Mapping(target = "finishGroupName",
             expression = "java(extractFinishGroupName(collection))")
    @Mapping(target = "rangeId",
             expression = "java(extractRangeId(collection))")
    @Mapping(target = "rangeName",
             expression = "java(extractRangeName(collection))")
    @Mapping(target = "rangeSlug",
             expression = "java(extractRangeSlug(collection))")
    @Mapping(target = "colourId",
             expression = "java(extractColourId(collection))")
    @Mapping(target = "colourName",
             expression = "java(extractColourName(collection))")
    @Mapping(target = "colourHex",
             expression = "java(extractColourHex(collection))")
    @Mapping(target = "siblingCollections", ignore = true)
    @Mapping(target = "unitCategories", ignore = true)
    @Mapping(target = "startingFromPrice", ignore = true)
    @Mapping(target = "productCount", ignore = true)
    @Mapping(target = "defaultCabinetColour", ignore = true)
    @Mapping(target = "availableCabinetColours", ignore = true)
    public abstract KitchenCollectionResponse toDto(KitchenCollection collection);

    // ====================================
    // Summary Mapping (manual, no MapStruct)
    // ====================================

    public KitchenCollectionSummary toSummary(KitchenCollection c) {
        if (c == null) return null;
        return KitchenCollectionSummary.builder()
            .id(c.getId())
            .name(c.getName())
            .slug(c.getSlug())
            .rangeSlug(extractRangeSlug(c))
            .colourName(extractColourName(c))
            .colourHex(extractColourHex(c))
            .heroImageUrl(c.getHeroImageUrl())
            .active(c.getActive())
            .build();
    }

    public KitchenCollectionSummary toSummaryWithSelected(
            KitchenCollection c, boolean selected) {
        KitchenCollectionSummary summary = toSummary(c);
        if (summary != null) summary.setSelected(selected);
        return summary;
    }

    public List<KitchenCollectionSummary> toSummaryList(
            List<KitchenCollection> collections) {
        if (collections == null || collections.isEmpty()) return Collections.emptyList();
        return collections.stream().map(this::toSummary).collect(Collectors.toList());
    }

    @Override
    public KitchenCollection toEntity(KitchenCollectionResponse dto) {
        throw new UnsupportedOperationException(
            "Use factory methods on KitchenCollection");
    }

    @Override
    public List<KitchenCollectionResponse> toDtoList(List<KitchenCollection> list) {
        if (list == null || list.isEmpty()) return Collections.emptyList();
        return list.stream().map(this::toDto).collect(Collectors.toList());
    }
}