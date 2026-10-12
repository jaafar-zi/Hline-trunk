package com.example.demo.modules.product.application.query;

import com.example.demo.common.annotation.ReadOnly;
import com.example.demo.common.base.service.query.AbstractQueryService;
import com.example.demo.common.normalization.NormalizationService;
import com.example.demo.modules.product.application.dto.filter.ProductPriceFilter;
import com.example.demo.modules.product.application.dto.response.ProductPriceResponse;
import com.example.demo.modules.product.application.mapper.ProductMapper;
import com.example.demo.modules.product.application.mapper.ProductPriceMapper;
import com.example.demo.modules.product.domain.model.ProductPrice;
import com.example.demo.modules.product.domain.repository.ProductPriceRepository;
import com.example.demo.modules.product.domain.specification.ProductPriceSpecification;

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
 * Product price query service
 * 
 * Handles all read operations for product prices
 * 
 * @author PIM Team
 * @version 3.0
 */
@Slf4j
@Service
@Transactional(readOnly = true)
public class ProductPriceQueryService extends AbstractQueryService<ProductPrice, UUID> {

    private static final Set<String> VALID_SORT_FIELDS = Set.of(
        "priority", "amount", "validFrom", "validTo", "createdAt", "priceType"
    );

    private final ProductPriceRepository priceRepository;
    private final ProductPriceMapper priceMapper;

    public ProductPriceQueryService(
            ProductPriceRepository priceRepository,
            NormalizationService normalizationService,
            ProductPriceMapper priceMapper) {
        super(priceRepository, normalizationService);
        this.priceRepository = priceRepository;
        this.priceMapper = priceMapper;
    }

    @Override
    protected String getEntityCacheName() {
        return "product-prices";
    }

    @Override
    protected Class<ProductPrice> getEntityClass() {
        return ProductPrice.class;
    }

    @Override
    protected boolean isValidSortField(String field) {
        return VALID_SORT_FIELDS.contains(field);
    }

    @Override
    protected String getDefaultSortField() {
        return "priority";
    }

    // ====================================
    // Filter-Based Queries
    // ====================================

    /**
     * Search prices with filter
     */
    @ReadOnly
    public Page<ProductPriceResponse> searchPrices(ProductPriceFilter filter) {
        log.debug("Searching product prices with filter");

        Specification<ProductPrice> spec = ProductPriceSpecification.fromFilter(filter);
        Page<ProductPrice> prices = findWithFilter(filter, spec);

        return prices.map(priceMapper::toResponse);
    }

    /**
     * Find all prices with filter
     */
    @ReadOnly
    public List<ProductPriceResponse> findAllPrices(ProductPriceFilter filter) {
        log.debug("Finding all product prices with filter");

        Specification<ProductPrice> spec = ProductPriceSpecification.fromFilter(filter);
        List<ProductPrice> prices = findAllWithFilter(filter, spec);

        return prices.stream()
                .map(priceMapper::toResponse)
                .collect(Collectors.toList());
    }

    // ====================================
    // Specific Queries
    // ====================================

    /**
     * Get price by ID
     */
    @ReadOnly
    public ProductPriceResponse getPriceById(UUID id) {
        log.debug("Getting price by ID: {}", id);

        ProductPrice price = findById(id);
        return priceMapper.toResponse(price);
    }

    /**
     * Get prices for product
     */
    @ReadOnly
    public List<ProductPriceResponse> getPricesForProduct(UUID productId) {
        log.debug("Getting prices for product: {}", productId);

        ProductPriceFilter filter = ProductPriceFilter.builder()
                .productId(productId)
                .excludeDeleted(true)
                .build();

        return findAllPrices(filter);
    }

    /**
     * Get active prices for product
     */
    @ReadOnly
    public List<ProductPriceResponse> getActivePricesForProduct(UUID productId) {
        log.debug("Getting active prices for product: {}", productId);

        ProductPriceFilter filter = ProductPriceFilter.builder()
                .productId(productId)
                .activeOnly(true)
                .excludeDeleted(true)
                .build();

        return findAllPrices(filter);
    }

    /**
     * Get current valid price for product
     */
    @ReadOnly
    public ProductPriceResponse getCurrentPriceForProduct(UUID productId, String priceType) {
        log.debug("Getting current {} price for product: {}", priceType, productId);

        ProductPriceFilter filter = ProductPriceFilter.builder()
                .productId(productId)
                .priceType(priceType)
                .currentlyValid(true)
                .activeOnly(true)
                .build();

        List<ProductPriceResponse> prices = findAllPrices(filter);

        return prices.stream()
                .findFirst()
                .orElse(null);
    }

    /**
     * Get promotional prices
     */
    @ReadOnly
    public List<ProductPriceResponse> getPromotionalPrices(UUID productId) {
        log.debug("Getting promotional prices for product: {}", productId);

        ProductPriceFilter filter = ProductPriceFilter.builder()
                .productId(productId)
                .priceType("PROMOTIONAL")
                .currentlyValid(true)
                .activeOnly(true)
                .build();

        return findAllPrices(filter);
    }

    /**
     * Get prices by currency
     */
    @ReadOnly
    public List<ProductPriceResponse> getPricesByCurrency(UUID productId, String currency) {
        log.debug("Getting prices in {} for product: {}", currency, productId);

        ProductPriceFilter filter = ProductPriceFilter.builder()
                .productId(productId)
                .currency(currency)
                .activeOnly(true)
                .build();

        return findAllPrices(filter);
    }

    /**
     * Get scheduled prices (future)
     */
    @ReadOnly
    public List<ProductPriceResponse> getScheduledPrices(UUID productId) {
        log.debug("Getting scheduled prices for product: {}", productId);

        ProductPriceFilter filter = ProductPriceFilter.builder()
                .productId(productId)
                .scheduled(true)
                .build();

        return findAllPrices(filter);
    }

    /**
     * Get expired prices
     */
    @ReadOnly
    public List<ProductPriceResponse> getExpiredPrices(UUID productId) {
        log.debug("Getting expired prices for product: {}", productId);

        ProductPriceFilter filter = ProductPriceFilter.builder()
                .productId(productId)
                .expired(true)
                .build();

        return findAllPrices(filter);
    }

    /**
     * Count prices for product
     */
    @ReadOnly
    public long countPricesForProduct(UUID productId) {
        ProductPriceFilter filter = ProductPriceFilter.builder()
                .productId(productId)
                .excludeDeleted(true)
                .build();

        Specification<ProductPrice> spec = ProductPriceSpecification.fromFilter(filter);
        return countWithFilter(filter, spec);
    }
}