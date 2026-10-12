package com.example.demo.modules.product.application.query;

import com.example.demo.common.annotation.ReadOnly;
import com.example.demo.common.base.service.query.AbstractQueryService;
import com.example.demo.common.exception.EntityNotFoundException;
import com.example.demo.common.normalization.NormalizationService;
import com.example.demo.modules.product.application.dto.filter.ProductVariantFilter;
import com.example.demo.modules.product.application.dto.response.ProductVariantResponse;
import com.example.demo.modules.product.application.mapper.ProductVariantMapper;
import com.example.demo.modules.product.domain.model.ProductVariant;
import com.example.demo.modules.product.domain.model.vo.SKU;
import com.example.demo.modules.product.domain.repository.ProductVariantRepository;
import com.example.demo.modules.product.domain.specification.ProductVariantSpecification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Product variant query service
 * 
 * Handles all read operations for product variants
 * 
 * @author PIM Team
 * @version 3.0
 */
@Slf4j
@Service
@Transactional(readOnly = true)
public class ProductVariantQueryService extends AbstractQueryService<ProductVariant, UUID> {

    private static final Set<String> VALID_SORT_FIELDS = Set.of(
        "name", "sku", "displayOrder", "createdAt", "price"
    );

    private final ProductVariantRepository variantRepository;
    private final ProductVariantMapper variantMapper;

    public ProductVariantQueryService(
            ProductVariantRepository variantRepository,
            NormalizationService normalizationService,
            ProductVariantMapper variantMapper) {
        super(variantRepository, normalizationService);
        this.variantRepository = variantRepository;
        this.variantMapper = variantMapper;
    }

    @Override
    protected String getEntityCacheName() {
        return "product-variants";
    }

    @Override
    protected Class<ProductVariant> getEntityClass() {
        return ProductVariant.class;
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
    // Filter-Based Queries
    // ====================================

    /**
     * Search variants with filter
     */
    @ReadOnly
    public Page<ProductVariantResponse> searchVariants(ProductVariantFilter filter) {
        log.debug("Searching product variants with filter");

        Specification<ProductVariant> spec = ProductVariantSpecification.fromFilter(filter);
        Page<ProductVariant> variants = findWithFilter(filter, spec);

        return variants.map(variantMapper::toResponse);
    }

    /**
     * Find all variants with filter
     */
    @ReadOnly
    public List<ProductVariantResponse> findAllVariants(ProductVariantFilter filter) {
        log.debug("Finding all product variants with filter");

        Specification<ProductVariant> spec = ProductVariantSpecification.fromFilter(filter);
        List<ProductVariant> variants = findAllWithFilter(filter, spec);

        return variants.stream()
                .map(variantMapper::toResponse)
                .collect(Collectors.toList());
    }

    // ====================================
    // Specific Queries
    // ====================================

    /**
     * Get variant by ID
     */
    @ReadOnly
    public ProductVariantResponse getVariantById(UUID id) {
        log.debug("Getting variant by ID: {}", id);

        ProductVariant variant = findById(id);
        return variantMapper.toResponse(variant);
    }

    /**
     * Get variant by SKU
     */
    @ReadOnly
    public ProductVariantResponse getVariantBySku(SKU sku) {
        log.debug("Getting variant by SKU: {}", sku);

        ProductVariant variant = variantRepository.findBySku(sku)
                .orElseThrow(() -> EntityNotFoundException.byField(ProductVariant.class, "SKU", sku));

        return variantMapper.toResponse(variant);
    }

    /**
     * Get variants for product
     */
    @ReadOnly
    public List<ProductVariantResponse> getVariantsForProduct(UUID productId) {
        log.debug("Getting variants for product: {}", productId);

        ProductVariantFilter filter = ProductVariantFilter.builder()
                .productId(productId)
                .excludeDeleted(true)
                .build();

        return findAllVariants(filter);
    }

    /**
     * Get active variants for product
     */
    @ReadOnly
    public List<ProductVariantResponse> getActiveVariantsForProduct(UUID productId) {
        log.debug("Getting active variants for product: {}", productId);

        ProductVariantFilter filter = ProductVariantFilter.builder()
                .productId(productId)
                .activeOnly(true)
                .excludeDeleted(true)
                .build();

        return findAllVariants(filter);
    }

    /**
     * Get default variant for product
     */
    @ReadOnly
    public ProductVariantResponse getDefaultVariant(UUID productId) {
        log.debug("Getting default variant for product: {}", productId);

        ProductVariantFilter filter = ProductVariantFilter.builder()
                .productId(productId)
                .defaultOnly(true)
                .build();

        List<ProductVariantResponse> variants = findAllVariants(filter);

        return variants.stream()
                .findFirst()
                .orElse(null);
    }

    /**
     * Get in-stock variants for product
     */
    @ReadOnly
    public List<ProductVariantResponse> getInStockVariants(UUID productId) {
        log.debug("Getting in-stock variants for product: {}", productId);

        ProductVariantFilter filter = ProductVariantFilter.builder()
                .productId(productId)
                .inStock(true)
                .build();

        return findAllVariants(filter);
    }

    /**
     * Get variants by attributes
     */
    @ReadOnly
    public List<ProductVariantResponse> getVariantsByAttributes(
            UUID productId, 
            java.util.Map<String, String> attributes) {
        log.debug("Getting variants by attributes for product: {}", productId);

        ProductVariantFilter filter = ProductVariantFilter.builder()
                .productId(productId)
                .attributes(attributes)
                .build();

        return findAllVariants(filter);
    }

    /**
     * Count variants for product
     */
    @ReadOnly
    public long countVariantsForProduct(UUID productId) {
        ProductVariantFilter filter = ProductVariantFilter.builder()
                .productId(productId)
                .excludeDeleted(true)
                .build();

        Specification<ProductVariant> spec = ProductVariantSpecification.fromFilter(filter);
        return countWithFilter(filter, spec);
    }

    /**
     * Check if SKU exists
     */
    @ReadOnly
    public boolean skuExists(String sku) {
        return variantRepository.existsBySku(sku);
    }
}