package com.example.demo.modules.product.application.query;

import com.example.demo.common.annotation.ReadOnly;
import com.example.demo.common.base.service.query.AbstractQueryService;
import com.example.demo.common.exception.EntityNotFoundException;
import com.example.demo.common.normalization.NormalizationService;
import com.example.demo.common.validation.result.ValidationResult;
import com.example.demo.modules.product.application.dto.filter.ProductFilter;
import com.example.demo.modules.product.application.dto.response.ProductResponse;
import com.example.demo.modules.product.application.mapper.ProductMapper;
import com.example.demo.modules.product.domain.model.Product;
import com.example.demo.modules.product.domain.model.vo.SKU;
import com.example.demo.modules.product.domain.repository.ProductRepository;
import com.example.demo.modules.product.domain.specification.ProductSpecifications;

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
 * Product query service
 * 
 * Handles all read operations for products
 * 
 * @author PIM Team
 * @version 3.0
 */
@Slf4j
@Service
public class ProductQueryService extends AbstractQueryService<Product, UUID> {

    private static final Set<String> VALID_SORT_FIELDS = Set.of(
        "name", "sku", "createdAt", "updatedAt", "publishedAt", 
        "status", "brand", "category.name"
    );

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final ProductSpecifications productSpecifications;

    public ProductQueryService(
            ProductRepository productRepository,
            NormalizationService normalizationService,
            ProductMapper productMapper,
            ProductSpecifications productSpecifications) {
        super(productRepository, normalizationService);
        this.productRepository = productRepository;
        this.productMapper = productMapper;
        this.productSpecifications = productSpecifications;
    }

    @Override
    protected String getEntityCacheName() {
        return "products";
    }

    @Override
    protected Class<Product> getEntityClass() {
        return Product.class;
    }

    @Override
    protected boolean isValidSortField(String field) {
        return VALID_SORT_FIELDS.contains(field);
    }

    @Override
    protected String getDefaultSortField() {
        return "createdAt";
    }

    // ====================================
    // Filter-Based Queries
    // ====================================

    /**
     * Search products with filter
     */
    @ReadOnly
    public Page<ProductResponse> searchProducts(ProductFilter filter) {
        log.debug("Searching products with filter");

        // Build specification from filter
        Specification<Product> spec = productSpecifications.fromFilter(filter);

        // Execute query with filter validation
        Page<Product> products = findWithFilter(filter, spec);

        // Map to response DTOs
        return products.map(productMapper::toResponse);
    }

    /**
     * Find all products with filter (no pagination)
     */
    @ReadOnly
    public List<ProductResponse> findAllProducts(ProductFilter filter) {
        log.debug("Finding all products with filter");

        Specification<Product> spec = productSpecifications.fromFilter(filter);
        List<Product> products = findAllWithFilter(filter, spec);

        return products.stream()
                .map(productMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Count products matching filter
     */
    @ReadOnly
    public long countProducts(ProductFilter filter) {
        log.debug("Counting products with filter");

        Specification<Product> spec = productSpecifications.fromFilter(filter);
        return countWithFilter(filter, spec);
    }

    // ====================================
    // Specific Queries
    // ====================================

    /**
     * Find product by ID (returns response DTO)
     */
    @ReadOnly
    public ProductResponse getProductById(UUID id) {
        log.debug("Getting product by ID: {}", id);

        Product product = findById(id);
        return productMapper.toResponse(product);
    }

    /**
     * Find product by SKU
     */
    @ReadOnly
    public ProductResponse getProductBySku(SKU sku) {
        log.debug("Getting product by SKU: {}", sku);

        Product product = productRepository.findBySku(sku)
                .orElseThrow(() -> EntityNotFoundException.byField(
                        Product.class,"sku" ,sku));

        return productMapper.toResponse(product);
    }

    /**
     * Find products by category
     */
    @ReadOnly
    public Page<ProductResponse> getProductsByCategory(UUID categoryId, ProductFilter filter) {
        log.debug("Getting products by category: {}", categoryId);

        // Override category filter
        filter.setCategoryId(categoryId);

        return searchProducts(filter);
    }

    /**
     * Find products by brand
     */
    @ReadOnly
    public Page<ProductResponse> getProductsByBrand(String brand, ProductFilter filter) {
        log.debug("Getting products by brand: {}", brand);

        filter.setBrand(brand);

        return searchProducts(filter);
    }

    /**
     * Find published products only
     */
    @ReadOnly
    public Page<ProductResponse> getPublishedProducts(ProductFilter filter) {
        log.debug("Getting published products");

        filter.setPublishedOnly(true);

        return searchProducts(filter);
    }

    /**
     * Find products ready to publish
     */
    @ReadOnly
    public List<ProductResponse> getProductsReadyToPublish() {
        log.debug("Getting products ready to publish");

        List<Product> products = productRepository.findAll().stream()
                .filter(Product::canBePublished)
                .collect(Collectors.toList());

        return products.stream()
                .map(productMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Find incomplete products (missing required data)
     */
    @ReadOnly
    public List<ProductResponse> getIncompleteProducts() {
        log.debug("Getting incomplete products");

        List<Product> products = productRepository.findAll().stream()
                .filter(p -> !p.canBePublished())
                .collect(Collectors.toList());

        return products.stream()
                .map(productMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Find products with low stock
     */
    @ReadOnly
    public List<ProductResponse> getLowStockProducts() {
        log.debug("Getting low stock products");

        List<Product> products = productRepository.findAll().stream()
                .filter(Product::isLowStock)
                .collect(Collectors.toList());

        return products.stream()
                .map(productMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Search products by keyword
     */
    @ReadOnly
    public Page<ProductResponse> searchByKeyword(String keyword, ProductFilter filter) {
        log.debug("Searching products by keyword: {}", keyword);

        filter.setKeyword(keyword);

        return searchProducts(filter);
    }

    // ====================================
    // Validation Override
    // ====================================

    protected ValidationResult validateFilter(ProductFilter filter) {
        ValidationResult result = ValidationResult.success("Product Filter Validation");

        // Validate price range
        if (filter.getMinPrice() != null && filter.getMaxPrice() != null) {
            if (filter.getMinPrice().compareTo(filter.getMaxPrice()) > 0) {
                result.addWarning("price", 
                    "Minimum price is greater than maximum price - results may be empty");
            }
        }

        // Validate date ranges
        if (filter.getCreatedAfter() != null && filter.getCreatedBefore() != null) {
            if (filter.getCreatedAfter().isAfter(filter.getCreatedBefore())) {
                result.addWarning("createdDate",
                    "Created after date is after created before date - results may be empty");
            }
        }

        // Validate quantity range
        if (filter.getMinQuantity() != null && filter.getMaxQuantity() != null) {
            if (filter.getMinQuantity() > filter.getMaxQuantity()) {
                result.addWarning("quantity",
                    "Minimum quantity is greater than maximum quantity - results may be empty");
            }
        }

        // Validate contradictory filters
        if (Boolean.TRUE.equals(filter.getInStock()) && Boolean.TRUE.equals(filter.getOutOfStock())) {
            result.addWarning("stock",
                "Both inStock and outOfStock are true - no results will be returned");
        }

        // Validate sort field
        if (filter.getSortBy() != null && !isValidSortField(filter.getSortBy())) {
            result.addWarning("sortBy",
                String.format("Invalid sort field '%s', using default '%s'", 
                    filter.getSortBy(), getDefaultSortField()));
        }

        return result;
    }

    // ====================================
    // Statistics & Analytics
    // ====================================

    /**
     * Get product statistics
     */
    @ReadOnly
    public ProductStatistics getStatistics() {
        log.debug("Getting product statistics");

        long totalProducts = count();
        long publishedProducts = countProducts(ProductFilter.builder()
                .publishedOnly(true)
                .build());
        long draftProducts = countProducts(ProductFilter.builder()
                .status("DRAFT")
                .build());
        long outOfStock = countProducts(ProductFilter.builder()
                .outOfStock(true)
                .build());

        return ProductStatistics.builder()
                .totalProducts(totalProducts)
                .publishedProducts(publishedProducts)
                .draftProducts(draftProducts)
                .outOfStockProducts(outOfStock)
                .build();
    }

    /**
     * Check if SKU exists
     */
    @ReadOnly
    public boolean skuExists(String sku) {
        return productRepository.existsBySku(sku);
    }

    /**
     * Check if product name exists in category
     */
    @ReadOnly
    public boolean nameExistsInCategory(String name, UUID categoryId) {
        return productRepository.existsByNameAndCategoryId(name, categoryId);
    }

    // ====================================
    // Statistics DTO
    // ====================================

    @lombok.Data
    @lombok.Builder
    public static class ProductStatistics {
        private long totalProducts;
        private long publishedProducts;
        private long draftProducts;
        private long outOfStockProducts;
    }
}
