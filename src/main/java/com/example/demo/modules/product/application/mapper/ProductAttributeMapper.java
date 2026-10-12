package com.example.demo.modules.product.application.mapper;

import com.example.demo.common.mapper.BaseMapper;
import com.example.demo.modules.product.application.dto.request.AddProductAttributeRequest;
import com.example.demo.modules.product.application.dto.request.UpdateProductAttributeRequest;
import com.example.demo.modules.product.application.dto.response.ProductAttributeResponse;
import com.example.demo.modules.product.domain.model.ProductAttribute;
import org.mapstruct.*;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Lean MapStruct mapper for ProductAttribute entity <-> DTOs
 * 
 * Performance optimized
 *
 * @author PIM Team
 * @version 1.0
 */
@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
    injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
@Component
public abstract class ProductAttributeMapper implements BaseMapper<ProductAttribute, ProductAttributeResponse> {

    // ====================================
    // Entity to Response
    // ====================================

    /**
     * Map ProductAttribute entity to ProductAttributeResponse
     */
    @Override
    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "categoryAttributeId", source = "categoryAttribute.id")
    @Mapping(target = "attributeType", source = "attributeType")
    @Mapping(target = "formattedValue", expression = "java(attribute.getFormattedValue())")
    public abstract ProductAttributeResponse toDto(ProductAttribute attribute);

    /**
     * Alias for better readability
     */
    public ProductAttributeResponse toResponse(ProductAttribute attribute) {
        return attribute != null ? toDto(attribute) : null;
    }

    /**
     * Map list of attributes to responses
     */
    @Override
    public List<ProductAttributeResponse> toDtoList(List<ProductAttribute> attributes) {
        if (attributes == null || attributes.isEmpty()) {
            return Collections.emptyList();
        }
        return attributes.stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    /**
     * Map set of attributes to sorted list
     */
    public List<ProductAttributeResponse> toSortedList(Set<ProductAttribute> attributes) {
        if (attributes == null || attributes.isEmpty()) {
            return Collections.emptyList();
        }

        return attributes.stream()
            .map(this::toResponse)
            .sorted(Comparator.comparing(ProductAttributeResponse::getDisplayOrder)
                .thenComparing(ProductAttributeResponse::getKey))
            .collect(Collectors.toList());
    }

    /**
     * Map set to map (key -> value)
     */
    public Map<String, String> toAttributeMap(Set<ProductAttribute> attributes) {
        if (attributes == null || attributes.isEmpty()) {
            return Collections.emptyMap();
        }

        return attributes.stream()
            .filter(ProductAttribute::hasValue)
            .collect(Collectors.toMap(
                ProductAttribute::getKey,
                ProductAttribute::getValue,
                (v1, v2) -> v1, // In case of duplicates, keep first
                LinkedHashMap::new // Preserve order
            ));
    }

    /**
     * Map set to grouped map (groupName -> list of attributes)
     */
    public Map<String, List<ProductAttributeResponse>> toGroupedAttributes(Set<ProductAttribute> attributes) {
        if (attributes == null || attributes.isEmpty()) {
            return Collections.emptyMap();
        }

        return attributes.stream()
            .map(this::toResponse)
            .collect(Collectors.groupingBy(
                attr -> attr.getGroupName() != null ? attr.getGroupName() : "General",
                LinkedHashMap::new,
                Collectors.collectingAndThen(
                    Collectors.toList(),
                    list -> list.stream()
                        .sorted(Comparator.comparing(ProductAttributeResponse::getDisplayOrder)
                            .thenComparing(ProductAttributeResponse::getKey))
                        .collect(Collectors.toList())
                )
            ));
    }

    // ====================================
    // Request to Entity - CREATE
    // ====================================

    /**
     * Map AddProductAttributeRequest to ProductAttribute entity
     */
    @Mapping(target = "product", ignore = true) // Set by service layer
    @Mapping(target = "categoryAttribute", ignore = true) // Set by service layer if from template
    @Mapping(target = "key", source = "key")
    @Mapping(target = "value", source = "value")
    @Mapping(target = "required", source = "required")
    @Mapping(target = "displayOrder", source = "displayOrder")
    // Default values for fields not in request
    @Mapping(target = "label", expression = "java(generateLabelFromKey(request.getKey()))")
    @Mapping(target = "attributeType", expression = "java(com.example.demo.modules.product.domain.model.vo.AttributeType.TEXT)")
    @Mapping(target = "searchable", constant = "false")
    @Mapping(target = "filterable", constant = "false")
    @Mapping(target = "groupName", ignore = true)
    @Mapping(target = "unit", ignore = true)
    @Mapping(target = "helpText", ignore = true)
    public abstract ProductAttribute fromAddRequest(AddProductAttributeRequest request);

    /**
     * Generate a human-readable label from attribute key
     * Converts "product_color" to "Product Color"
     */
    protected String generateLabelFromKey(String key) {
        if (key == null || key.isBlank()) {
            return null;
        }
        
        // Convert snake_case or camelCase to Title Case
        return Arrays.stream(key.split("[_\\s]+"))
            .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase())
            .collect(Collectors.joining(" "));
    }

    /**
     * Not supported
     */
    @Override
    public ProductAttribute toEntity(ProductAttributeResponse dto) {
        throw new UnsupportedOperationException(
            "Cannot create entity from response DTO. Use fromAddRequest() instead."
        );
    }

    // ====================================
    // Request to Entity - UPDATE
    // ====================================

    /**
     * Update existing ProductAttribute from UpdateProductAttributeRequest
     */
    @BeanMapping(
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS
    )
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "categoryAttribute", ignore = true)
    @Mapping(target = "key", ignore = true) // Immutable
    @Mapping(target = "attributeType", ignore = true) // Type cannot be changed
    @Mapping(target = "required", ignore = true) // Set by template or system
    public abstract void updateEntityFromRequest(
        UpdateProductAttributeRequest request,
        @MappingTarget ProductAttribute entity
    );

    // ====================================
    // Utility Methods
    // ====================================

    /**
     * Create minimal summary (key-value pair)
     */
    public AttributeSummary toSummary(ProductAttribute attribute) {
        if (attribute == null) {
            return null;
        }

        return AttributeSummary.builder()
            .key(attribute.getKey())
            .label(attribute.getLabel())
            .value(attribute.getValue())
            .formattedValue(attribute.getFormattedValue())
            .build();
    }

    /**
     * Map to summary list
     */
    public List<AttributeSummary> toSummaryList(Set<ProductAttribute> attributes) {
        if (attributes == null || attributes.isEmpty()) {
            return Collections.emptyList();
        }

        return attributes.stream()
            .filter(ProductAttribute::hasValue)
            .map(this::toSummary)
            .sorted(Comparator.comparing(AttributeSummary::getKey))
            .collect(Collectors.toList());
    }

    // ====================================
    // Nested DTO
    // ====================================

    /**
     * Lightweight attribute summary for simple displays
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class AttributeSummary {
        private String key;
        private String label;
        private String value;
        private String formattedValue;

        public String getDisplayValue() {
            return formattedValue != null ? formattedValue : value;
        }
    }
}