package com.example.demo.modules.kitchen.application.mapper;

import com.example.demo.common.mapper.BaseMapper;
import com.example.demo.modules.kitchen.application.dto.response.*;
import com.example.demo.modules.kitchen.domain.model.*;
import org.mapstruct.*;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Kitchen Configuration Mapper
 *
 * Fix: No uses = {}. All nested objects resolved via helper methods.
 */
@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
    injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
public abstract class KitchenConfigurationMapper
        implements BaseMapper<KitchenConfiguration, KitchenConfigurationResponse> {

    // ====================================
    // Helper Methods
    // ====================================

    protected UUID extractCollectionId(KitchenConfiguration c) {
        return c.getCollection() != null ? c.getCollection().getId() : null;
    }

    protected String extractCollectionName(KitchenConfiguration c) {
        return c.getCollection() != null ? c.getCollection().getName() : null;
    }

    protected String extractCollectionSlug(KitchenConfiguration c) {
        return c.getCollection() != null ? c.getCollection().getSlug() : null;
    }

    protected String extractRangeName(KitchenConfiguration c) {
        return c.getCollection() != null && c.getCollection().getRange() != null
            ? c.getCollection().getRange().getName() : null;
    }

    protected String extractStatusName(KitchenConfiguration c) {
        return c.getStatus() != null ? c.getStatus().name() : null;
    }

    // Item helpers
    protected UUID extractItemProductId(KitchenConfigurationItem item) {
        return item.getProduct() != null ? item.getProduct().getId() : null;
    }

    protected String extractItemProductName(KitchenConfigurationItem item) {
        return item.getProduct() != null ? item.getProduct().getName() : null;
    }

    protected String extractItemProductSlug(KitchenConfigurationItem item) {
        return item.getProduct() != null ? item.getProduct().getSlug() : null;
    }

    protected String extractItemUnitCategoryName(KitchenConfigurationItem item) {
        return item.getProduct() != null && item.getProduct().getUnitCategory() != null
            ? item.getProduct().getUnitCategory().getName() : null;
    }

    protected String extractItemDimensions(KitchenConfigurationItem item) {
        return item.getProduct() != null
            ? item.getProduct().getFormattedDimensions() : null;
    }

    protected UUID extractItemVariantId(KitchenConfigurationItem item) {
        return item.getVariant() != null ? item.getVariant().getId() : null;
    }

    protected String extractItemVariantSku(KitchenConfigurationItem item) {
        return item.getVariant() != null ? item.getVariant().getSku() : null;
    }

    protected String extractItemColourName(KitchenConfigurationItem item) {
        return item.getVariant() != null
            && item.getVariant().getCabinetColour() != null
            ? item.getVariant().getCabinetColour().getName() : null;
    }

    protected String extractItemColourHex(KitchenConfigurationItem item) {
        return item.getVariant() != null
            && item.getVariant().getCabinetColour() != null
            ? item.getVariant().getCabinetColour().getHexValue() : null;
    }

    protected String extractItemAssemblyType(KitchenConfigurationItem item) {
        return item.getVariant() != null
            && item.getVariant().getAssemblyType() != null
            ? item.getVariant().getAssemblyType().getDisplayName() : null;
    }

    protected BigDecimal extractItemUnitPrice(KitchenConfigurationItem item) {
        return item.getUnitPrice() != null ? item.getUnitPrice().getAmount() : null;
    }

    protected BigDecimal extractItemLineTotal(KitchenConfigurationItem item) {
        return item.getLineTotal() != null ? item.getLineTotal().getAmount() : null;
    }

    protected String extractItemCurrency(KitchenConfigurationItem item) {
        return item.getUnitPrice() != null ? item.getUnitPrice().getCurrency() : "GBP";
    }

    // ====================================
    // Configuration → Response
    // ====================================

    @Override
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "customerId", source = "customerId")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "updatedAt", source = "updatedAt")
    @Mapping(target = "createdBy", source = "createdBy")
    @Mapping(target = "updatedBy", source = "updatedBy")
    @Mapping(target = "version", source = "version")
    @Mapping(target = "status",
             expression = "java(extractStatusName(config))")
    @Mapping(target = "collectionId",
             expression = "java(extractCollectionId(config))")
    @Mapping(target = "collectionName",
             expression = "java(extractCollectionName(config))")
    @Mapping(target = "collectionSlug",
             expression = "java(extractCollectionSlug(config))")
    @Mapping(target = "rangeName",
             expression = "java(extractRangeName(config))")
    @Mapping(target = "selectedCabinetColour", ignore = true)
    @Mapping(target = "availableCabinetColours", ignore = true)
    @Mapping(target = "items", ignore = true)
    @Mapping(target = "totalItems", ignore = true)
    @Mapping(target = "totalUnits", ignore = true)
    @Mapping(target = "subtotal", ignore = true)
    @Mapping(target = "currency", ignore = true)
    public abstract KitchenConfigurationResponse toDto(KitchenConfiguration config);

    // ====================================
    // Item → Response
    // ====================================

    @Mapping(target = "id", source = "id")
    @Mapping(target = "position", source = "position")
    @Mapping(target = "label", source = "label")
    @Mapping(target = "quantity", source = "quantity")
    @Mapping(target = "colourOverridden", source = "colourOverridden")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "updatedAt", source = "updatedAt")
    @Mapping(target = "productId",
             expression = "java(extractItemProductId(item))")
    @Mapping(target = "productName",
             expression = "java(extractItemProductName(item))")
    @Mapping(target = "productSlug",
             expression = "java(extractItemProductSlug(item))")
    @Mapping(target = "unitCategoryName",
             expression = "java(extractItemUnitCategoryName(item))")
    @Mapping(target = "dimensions",
             expression = "java(extractItemDimensions(item))")
    @Mapping(target = "variantId",
             expression = "java(extractItemVariantId(item))")
    @Mapping(target = "variantSku",
             expression = "java(extractItemVariantSku(item))")
    @Mapping(target = "cabinetColourName",
             expression = "java(extractItemColourName(item))")
    @Mapping(target = "cabinetColourHex",
             expression = "java(extractItemColourHex(item))")
    @Mapping(target = "assemblyType",
             expression = "java(extractItemAssemblyType(item))")
    @Mapping(target = "unitPrice",
             expression = "java(extractItemUnitPrice(item))")
    @Mapping(target = "lineTotal",
             expression = "java(extractItemLineTotal(item))")
    @Mapping(target = "currency",
             expression = "java(extractItemCurrency(item))")
    @Mapping(target = "productImageUrl", ignore = true)
    public abstract KitchenConfigurationItemResponse toItemDto(
        KitchenConfigurationItem item);

    // ====================================
    // Collection methods
    // ====================================

    public List<KitchenConfigurationItemResponse> toItemDtoList(
            List<KitchenConfigurationItem> items) {
        if (items == null || items.isEmpty()) return Collections.emptyList();
        return items.stream().map(this::toItemDto).collect(Collectors.toList());
    }

    @Override
    public KitchenConfiguration toEntity(KitchenConfigurationResponse dto) {
        throw new UnsupportedOperationException(
            "Use factory methods on KitchenConfiguration");
    }

    @Override
    public List<KitchenConfigurationResponse> toDtoList(
            List<KitchenConfiguration> configs) {
        if (configs == null || configs.isEmpty()) return Collections.emptyList();
        return configs.stream().map(this::toDto).collect(Collectors.toList());
    }
}