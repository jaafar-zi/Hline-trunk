package com.example.demo.modules.kitchen.application.mapper;

import com.example.demo.common.mapper.BaseMapper;
import com.example.demo.modules.kitchen.application.dto.response.*;
import com.example.demo.modules.kitchen.domain.model.*;
import com.example.demo.modules.kitchen.domain.model.vo.AssemblyType;
import org.mapstruct.*;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Kitchen Product Mapper
 *
 * Fix: No uses = {} with other mappers.
 * All nested objects mapped via explicit helper methods.
 */
@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
    injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
public abstract class KitchenProductMapper
        implements BaseMapper<KitchenProduct, KitchenProductResponse> {

    // ====================================
    // Helper Methods
    // ====================================

    protected UUID extractRangeId(KitchenProduct p) {
        return p.getCollection() != null && p.getCollection().getRange() != null
            ? p.getCollection().getRange().getId() : null;
    }

    protected String extractRangeName(KitchenProduct p) {
        return p.getCollection() != null && p.getCollection().getRange() != null
            ? p.getCollection().getRange().getName() : null;
    }

    protected String extractRangeSlug(KitchenProduct p) {
        return p.getCollection() != null && p.getCollection().getRange() != null
            ? p.getCollection().getRange().getSlug() : null;
    }

    protected UUID extractCollectionId(KitchenProduct p) {
        return p.getCollection() != null ? p.getCollection().getId() : null;
    }

    protected String extractCollectionName(KitchenProduct p) {
        return p.getCollection() != null ? p.getCollection().getName() : null;
    }

    protected String extractCollectionSlug(KitchenProduct p) {
        return p.getCollection() != null ? p.getCollection().getSlug() : null;
    }

    protected UUID extractUnitCategoryId(KitchenProduct p) {
        return p.getUnitCategory() != null ? p.getUnitCategory().getId() : null;
    }

    protected String extractUnitCategoryName(KitchenProduct p) {
        return p.getUnitCategory() != null ? p.getUnitCategory().getName() : null;
    }

    protected String extractUnitCategoryCode(KitchenProduct p) {
        return p.getUnitCategory() != null ? p.getUnitCategory().getCode() : null;
    }

    protected BigDecimal extractBasePriceAmount(KitchenProduct p) {
        return p.getBasePrice() != null ? p.getBasePrice().getAmount() : null;
    }

    protected String extractCurrency(KitchenProduct p) {
        return p.getBasePrice() != null ? p.getBasePrice().getCurrency() : "GBP";
    }

    protected String extractFormattedDimensions(KitchenProduct p) {
        return p.getFormattedDimensions();
    }

    protected String extractPrimaryImageUrl(KitchenProduct p) {
        if (p.getImages() == null || p.getImages().isEmpty()) return null;
        return p.getImages().stream()
            .filter(img -> Boolean.TRUE.equals(img.getIsPrimary()))
            .map(KitchenProductImage::getUrl)
            .findFirst()
            .orElse(p.getImages().iterator().next().getUrl());
    }

    // Variant helpers
    protected String mapAssemblyTypeName(AssemblyType t) {
        return t != null ? t.name() : null;
    }

    protected String mapAssemblyTypeDisplayName(AssemblyType t) {
        return t != null ? t.getDisplayName() : null;
    }

    protected UUID extractVariantProductId(KitchenProductVariant v) {
        return v.getProduct() != null ? v.getProduct().getId() : null;
    }

    protected UUID extractVariantColourId(KitchenProductVariant v) {
        return v.getCabinetColour() != null ? v.getCabinetColour().getId() : null;
    }

    protected String extractVariantColourName(KitchenProductVariant v) {
        return v.getCabinetColour() != null ? v.getCabinetColour().getName() : null;
    }

    protected String extractVariantColourHex(KitchenProductVariant v) {
        return v.getCabinetColour() != null ? v.getCabinetColour().getHexValue() : null;
    }

    protected BigDecimal extractVariantPriceAmount(KitchenProductVariant v) {
        return v.getPrice() != null ? v.getPrice().getAmount() : null;
    }

    protected String extractVariantCurrency(KitchenProductVariant v) {
        return v.getPrice() != null ? v.getPrice().getCurrency() : "GBP";
    }

    protected BigDecimal extractColourPremium(KitchenProductVariant v) {
        return v.getCabinetColourPremium() != null
            ? v.getCabinetColourPremium().getAmount() : null;
    }

    protected BigDecimal extractAssemblyPremium(KitchenProductVariant v) {
        return v.getAssemblyPremium() != null
            ? v.getAssemblyPremium().getAmount() : null;
    }

    // ====================================
    // Product → Response
    // ====================================

    @Override
    @Mapping(target = "id", source = "id")
    @Mapping(target = "sku", source = "sku")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "slug", source = "slug")
    @Mapping(target = "shortDescription", source = "shortDescription")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "widthMm", source = "widthMm")
    @Mapping(target = "heightMm", source = "heightMm")
    @Mapping(target = "depthMm", source = "depthMm")
    @Mapping(target = "active", source = "active")
    @Mapping(target = "isPopular", source = "isPopular")
    @Mapping(target = "standaloneOrderable", source = "standaloneOrderable")
    @Mapping(target = "metaTitle", source = "metaTitle")
    @Mapping(target = "metaDescription", source = "metaDescription")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "updatedAt", source = "updatedAt")
    @Mapping(target = "createdBy", source = "createdBy")
    @Mapping(target = "updatedBy", source = "updatedBy")
    @Mapping(target = "version", source = "version")
    @Mapping(target = "collectionId",
             expression = "java(extractCollectionId(product))")
    @Mapping(target = "collectionName",
             expression = "java(extractCollectionName(product))")
    @Mapping(target = "collectionSlug",
             expression = "java(extractCollectionSlug(product))")
    @Mapping(target = "rangeId",
             expression = "java(extractRangeId(product))")
    @Mapping(target = "rangeName",
             expression = "java(extractRangeName(product))")
    @Mapping(target = "rangeSlug",
             expression = "java(extractRangeSlug(product))")
    @Mapping(target = "unitCategoryId",
             expression = "java(extractUnitCategoryId(product))")
    @Mapping(target = "unitCategoryName",
             expression = "java(extractUnitCategoryName(product))")
    @Mapping(target = "unitCategoryCode",
             expression = "java(extractUnitCategoryCode(product))")
    @Mapping(target = "basePrice",
             expression = "java(extractBasePriceAmount(product))")
    @Mapping(target = "currency",
             expression = "java(extractCurrency(product))")
    @Mapping(target = "formattedDimensions",
             expression = "java(extractFormattedDimensions(product))")
    @Mapping(target = "primaryImageUrl",
             expression = "java(extractPrimaryImageUrl(product))")
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "variants", ignore = true)
    @Mapping(target = "defaultVariant", ignore = true)
    @Mapping(target = "availableCabinetColours", ignore = true)
    public abstract KitchenProductResponse toDto(KitchenProduct product);

    // ====================================
    // Variant → Response
    // ====================================

    @Mapping(target = "id", source = "id")
    @Mapping(target = "sku", source = "sku")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "inStock", source = "inStock")
    @Mapping(target = "stockQuantity", source = "stockQuantity")
    @Mapping(target = "leadTimeDays", source = "leadTimeDays")
    @Mapping(target = "active", source = "active")
    @Mapping(target = "isDefault", source = "isDefault")
    @Mapping(target = "sortOrder", source = "sortOrder")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "updatedAt", source = "updatedAt")
    @Mapping(target = "version", source = "version")
    @Mapping(target = "productId",
             expression = "java(extractVariantProductId(variant))")
    @Mapping(target = "cabinetColourId",
             expression = "java(extractVariantColourId(variant))")
    @Mapping(target = "cabinetColourName",
             expression = "java(extractVariantColourName(variant))")
    @Mapping(target = "cabinetColourHex",
             expression = "java(extractVariantColourHex(variant))")
    @Mapping(target = "assemblyType",
             expression = "java(mapAssemblyTypeName(variant.getAssemblyType()))")
    @Mapping(target = "assemblyTypeDisplayName",
             expression = "java(mapAssemblyTypeDisplayName(variant.getAssemblyType()))")
    @Mapping(target = "price",
             expression = "java(extractVariantPriceAmount(variant))")
    @Mapping(target = "currency",
             expression = "java(extractVariantCurrency(variant))")
    @Mapping(target = "cabinetColourPremium",
             expression = "java(extractColourPremium(variant))")
    @Mapping(target = "assemblyPremium",
             expression = "java(extractAssemblyPremium(variant))")
    public abstract KitchenProductVariantResponse toVariantDto(
        KitchenProductVariant variant);

    // ====================================
    // Image → Response (manual, simple)
    // ====================================

    public KitchenProductImageResponse toImageDto(KitchenProductImage image) {
        if (image == null) return null;
        return KitchenProductImageResponse.builder()
            .id(image.getId())
            .url(image.getUrl())
            .thumbnailUrl(image.getThumbnailUrl())
            .altText(image.getAltText())
            .isPrimary(image.getIsPrimary())
            .sortOrder(image.getSortOrder())
            .variantId(image.getVariant() != null ? image.getVariant().getId() : null)
            .build();
    }

    // ====================================
    // Summary Mapping (manual)
    // ====================================

    public KitchenProductSummary toSummary(KitchenProduct p) {
        if (p == null) return null;
        return KitchenProductSummary.builder()
            .id(p.getId())
            .sku(p.getSku())
            .name(p.getName())
            .slug(p.getSlug())
            .widthMm(p.getWidthMm())
            .heightMm(p.getHeightMm())
            .depthMm(p.getDepthMm())
            .formattedDimensions(p.getFormattedDimensions())
            .startingFromPrice(extractBasePriceAmount(p))
            .currency(extractCurrency(p))
            .primaryImageUrl(extractPrimaryImageUrl(p))
            .unitCategoryCode(extractUnitCategoryCode(p))
            .unitCategoryName(extractUnitCategoryName(p))
            .isPopular(p.getIsPopular())
            .hasMultipleVariants(
                p.getVariants() != null && p.getVariants().size() > 1)
            .build();
    }

    public List<KitchenProductSummary> toSummaryList(List<KitchenProduct> products) {
        if (products == null || products.isEmpty()) return Collections.emptyList();
        return products.stream().map(this::toSummary).collect(Collectors.toList());
    }

    public List<KitchenProductVariantResponse> toVariantDtoList(
            List<KitchenProductVariant> variants) {
        if (variants == null || variants.isEmpty()) return Collections.emptyList();
        return variants.stream().map(this::toVariantDto).collect(Collectors.toList());
    }

    public List<KitchenProductImageResponse> toImageDtoList(
            java.util.Set<KitchenProductImage> images) {
        if (images == null || images.isEmpty()) return Collections.emptyList();
        return images.stream()
            .sorted(java.util.Comparator.comparing(
                KitchenProductImage::getSortOrder,
                java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder())))
            .map(this::toImageDto)
            .collect(Collectors.toList());
    }

    @Override
    public KitchenProduct toEntity(KitchenProductResponse dto) {
        throw new UnsupportedOperationException(
            "Use factory methods on KitchenProduct");
    }

    @Override
    public List<KitchenProductResponse> toDtoList(List<KitchenProduct> products) {
        if (products == null || products.isEmpty()) return Collections.emptyList();
        return products.stream().map(this::toDto).collect(Collectors.toList());
    }
}