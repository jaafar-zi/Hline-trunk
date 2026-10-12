package com.example.demo.modules.product.application.query;

import com.example.demo.common.annotation.ReadOnly;
import com.example.demo.common.base.service.query.AbstractQueryService;
import com.example.demo.common.normalization.NormalizationService;
import com.example.demo.modules.product.application.dto.response.ProductAttributeResponse;
import com.example.demo.modules.product.application.mapper.ProductAttributeMapper;
import com.example.demo.modules.product.domain.model.ProductAttribute;
import com.example.demo.modules.product.domain.repository.ProductAttributeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Product attribute query service
 * 
 * Handles all read operations for product attributes
 * 
 * @author PIM Team
 * @version 3.0
 */
@Slf4j
@Service
@Transactional(readOnly = true)
public class ProductAttributeQueryService extends AbstractQueryService<ProductAttribute, UUID> {

    private static final Set<String> VALID_SORT_FIELDS = Set.of(
        "key", "displayOrder", "createdAt", "updatedAt"
    );

    private final ProductAttributeRepository attributeRepository;
    private final ProductAttributeMapper attributeMapper;

    public ProductAttributeQueryService(
            ProductAttributeRepository attributeRepository,
            NormalizationService normalizationService,
            ProductAttributeMapper attributeMapper) {
        super(attributeRepository, normalizationService);
        this.attributeRepository = attributeRepository;
        this.attributeMapper = attributeMapper;
    }

    @Override
    protected String getEntityCacheName() {
        return "product-attributes";
    }

    @Override
    protected Class<ProductAttribute> getEntityClass() {
        return ProductAttribute.class;
    }

    @Override
    protected boolean isValidSortField(String field) {
        return VALID_SORT_FIELDS.contains(field);
    }

    @Override
    protected String getDefaultSortField() {
        return "displayOrder";
    }

    // ====================================
    // Specific Queries
    // ====================================

    /**
     * Get attribute by ID
     */
    @ReadOnly
    public ProductAttributeResponse getAttributeById(UUID id) {
        log.debug("Getting attribute by ID: {}", id);

        ProductAttribute attribute = findById(id);
        return attributeMapper.toResponse(attribute);
    }

    /**
     * Get all attributes for product
     */
    @ReadOnly
    public List<ProductAttributeResponse> getAttributesForProduct(UUID productId) {
        log.debug("Getting attributes for product: {}", productId);

        List<ProductAttribute> attributes = attributeRepository.findByProductId(productId);

        return attributes.stream()
                .map(attributeMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get attribute by product and key
     */
    @ReadOnly
    public ProductAttributeResponse getAttributeByProductAndKey(UUID productId, String key) {
        log.debug("Getting attribute '{}' for product: {}", key, productId);

        ProductAttribute attribute = attributeRepository.findByProductIdAndKey(productId, key)
                .orElse(null);

        return attribute != null ? attributeMapper.toResponse(attribute) : null;
    }

    /**
     * Get required attributes for product
     */
    @ReadOnly
    public List<ProductAttributeResponse> getRequiredAttributes(UUID productId) {
        log.debug("Getting required attributes for product: {}", productId);

        List<ProductAttribute> attributes = attributeRepository.findByProductIdAndRequired(productId, true);

        return attributes.stream()
                .map(attributeMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get optional attributes for product
     */
    @ReadOnly
    public List<ProductAttributeResponse> getOptionalAttributes(UUID productId) {
        log.debug("Getting optional attributes for product: {}", productId);

        List<ProductAttribute> attributes = attributeRepository.findByProductIdAndRequired(productId, false);

        return attributes.stream()
                .map(attributeMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get missing required attributes for product
     */
    @ReadOnly
    public List<ProductAttributeResponse> getMissingRequiredAttributes(UUID productId) {
        log.debug("Getting missing required attributes for product: {}", productId);

        List<ProductAttribute> attributes = attributeRepository.findByProductIdAndRequired(productId, true);

        List<ProductAttribute> missing = attributes.stream()
                .filter(attr -> attr.getValue() == null || attr.getValue().isBlank())
                .collect(Collectors.toList());

        return missing.stream()
                .map(attributeMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get attributes by key (across all products)
     */
    @ReadOnly
    public List<ProductAttributeResponse> getAttributesByKey(String key) {
        log.debug("Getting attributes with key: {}", key);

        List<ProductAttribute> attributes = attributeRepository.findByKey(key);

        return attributes.stream()
                .map(attributeMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get all unique attribute keys
     */
    @ReadOnly
    public Set<String> getAllAttributeKeys() {
        log.debug("Getting all unique attribute keys");

        return attributeRepository.findDistinctKeys();
    }

    /**
     * Get all unique attribute keys for product
     */
    @ReadOnly
    public Set<String> getAttributeKeysForProduct(UUID productId) {
        log.debug("Getting attribute keys for product: {}", productId);

        List<ProductAttribute> attributes = attributeRepository.findByProductId(productId);

        return attributes.stream()
                .map(ProductAttribute::getKey)
                .collect(Collectors.toSet());
    }

    /**
     * Get attributes as map (key-value pairs)
     */
    @ReadOnly
    public Map<String, String> getAttributesAsMap(UUID productId) {
        log.debug("Getting attributes as map for product: {}", productId);

        List<ProductAttribute> attributes = attributeRepository.findByProductId(productId);

        return attributes.stream()
                .filter(attr -> attr.getValue() != null)
                .collect(Collectors.toMap(
                    ProductAttribute::getKey,
                    ProductAttribute::getValue,
                    (existing, replacement) -> replacement // Keep last value if duplicate keys
                ));
    }

    /**
     * Get attribute value
     */
    @ReadOnly
    public String getAttributeValue(UUID productId, String key) {
        log.debug("Getting value for attribute '{}' on product: {}", key, productId);

        return attributeRepository.findByProductIdAndKey(productId, key)
                .map(ProductAttribute::getValue)
                .orElse(null);
    }

    /**
     * Check if attribute exists
     */
    @ReadOnly
    public boolean attributeExists(UUID productId, String key) {
        log.debug("Checking if attribute '{}' exists for product: {}", key, productId);

        return attributeRepository.existsByProductIdAndKey(productId, key);
    }

    /**
     * Count attributes for product
     */
    @ReadOnly
    public long countAttributesForProduct(UUID productId) {
        log.debug("Counting attributes for product: {}", productId);

        return attributeRepository.countByProductId(productId);
    }

    /**
     * Count required attributes for product
     */
    @ReadOnly
    public long countRequiredAttributesForProduct(UUID productId) {
        log.debug("Counting required attributes for product: {}", productId);

        return attributeRepository.countByProductIdAndRequired(productId, true);
    }

    /**
     * Count filled attributes for product
     */
    @ReadOnly
    public long countFilledAttributesForProduct(UUID productId) {
        log.debug("Counting filled attributes for product: {}", productId);

        List<ProductAttribute> attributes = attributeRepository.findByProductId(productId);

        return attributes.stream()
                .filter(attr -> attr.getValue() != null && !attr.getValue().isBlank())
                .count();
    }

    /**
     * Get attribute statistics for product
     */
    @ReadOnly
    public AttributeStatistics getAttributeStatistics(UUID productId) {
        log.debug("Getting attribute statistics for product: {}", productId);

        List<ProductAttribute> attributes = attributeRepository.findByProductId(productId);

        long total = attributes.size();
        long required = attributes.stream().filter(attr -> Boolean.TRUE.equals(attr.getRequired())).count();
        long filled = attributes.stream().filter(attr -> attr.getValue() != null && !attr.getValue().isBlank()).count();
        long missing = attributes.stream()
                .filter(attr -> Boolean.TRUE.equals(attr.getRequired()))
                .filter(attr -> attr.getValue() == null || attr.getValue().isBlank())
                .count();

        return AttributeStatistics.builder()
                .totalAttributes(total)
                .requiredAttributes(required)
                .filledAttributes(filled)
                .missingRequiredAttributes(missing)
                .completionPercentage(total > 0 ? (filled * 100.0) / total : 0.0)
                .build();
    }

    /**
     * Search attributes by value
     */
    @ReadOnly
    public List<ProductAttributeResponse> searchAttributesByValue(String value) {
        log.debug("Searching attributes by value: {}", value);

        List<ProductAttribute> attributes = attributeRepository.findByValueContainingIgnoreCase(value);

        return attributes.stream()
                .map(attributeMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get products with specific attribute
     */
    @ReadOnly
    public List<UUID> getProductsWithAttribute(String key, String value) {
        log.debug("Getting products with attribute '{}' = '{}'", key, value);

        return attributeRepository.findProductIdsByKeyAndValue(key, value).stream()
                .collect(Collectors.toList());
    }

    // ====================================
    // Statistics DTO
    // ====================================

    @lombok.Data
    @lombok.Builder
    public static class AttributeStatistics {
        private long totalAttributes;
        private long requiredAttributes;
        private long filledAttributes;
        private long missingRequiredAttributes;
        private double completionPercentage;
    }
}