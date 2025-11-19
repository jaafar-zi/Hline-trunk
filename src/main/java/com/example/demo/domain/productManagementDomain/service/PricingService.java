package com.example.demo.domain.productManagementDomain.service;

import com.example.demo.common.base.service.AbstractBaseService;
import com.example.demo.domain.productManagementDomain.dto.ProductPricingDTO;
import com.example.demo.domain.productManagementDomain.entity.Pricing;
import com.example.demo.domain.productManagementDomain.entity.Product;
import com.example.demo.domain.productManagementDomain.mapper.PricingMapper;
import com.example.demo.domain.productManagementDomain.repository.PricingRepository;
import com.example.demo.domain.productManagementDomain.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PricingService extends AbstractBaseService<Pricing, Long> {

    private final PricingRepository pricingRepository;
    private final PricingMapper pricingMapper;

    /**
     * Constructor delegation to the base class.
     */
    public PricingService(PricingRepository pricingRepository, PricingMapper pricingMapper) {
        super(pricingRepository);
        this.pricingRepository = pricingRepository;
        this.pricingMapper = pricingMapper;
    }

    // ---- Base Configuration ---- //

    @Override
    protected String getCacheName() {
        return "pricings";
    }

    @Override
    protected Class<?> getEntityClass() {
        return Pricing.class;
    }

    // ---- Custom Queries ---- //

    /**
     * Create a new pricing entry.
     */
    @Transactional
    public ProductPricingDTO createPricing(Pricing pricing) {
        Pricing saved = save(pricing);              // uses AbstractBaseService.save(T)
        return pricingMapper.toDto(saved);
    }

    /**
     * Retrieve pricing by ID.
     */
    public Optional<ProductPricingDTO> getPricingById(Long id) {
        return pricingRepository.findByIdWithProducts(id)
                .map(pricingMapper::toDto);
    }

    /**
     * Retrieve all pricings for a given product.
     */
    public List<ProductPricingDTO> getPricingsByProductId(Long productId) {
        return pricingRepository.findByProductIdWithProducts(productId)
                .stream()
                .map(pricingMapper::toDto)
                .toList();
    }

    /**
     * Retrieve all pricings filtered by currency.
     */
    public List<ProductPricingDTO> getPricingsByCurrency(String currency) {
        return pricingRepository.findByCurrencyWithProducts(currency)
                .stream()
                .map(pricingMapper::toDto)
                .toList();
    }

    /**
     * Retrieve a pricing for a given product and currency.
     */
    public Optional<ProductPricingDTO> getPricingByProductIdAndCurrency(Long productId, String currency) {
        return pricingRepository.findByProductIdAndCurrencyWithProducts(productId, currency)
                .map(pricingMapper::toDto);
    }

    /**
     * Retrieve a pricing map for a list of product IDs with the given currency.
     */
    public Map<Long, ProductPricingDTO> getPricingByProductIds(List<Long> productIds, String currency) {
        return pricingRepository.findByProductIdsAndCurrencyWithProducts(productIds, currency)
                .stream()
                .map(pricingMapper::toDto)
                .collect(HashMap::new,
                        (map, dto) -> dto.getProductIds()
                                .forEach(pid -> map.putIfAbsent(pid, dto)),
                        HashMap::putAll);
    }

    /**
     * Retrieve all pricings (paginated).
     */
    public Page<ProductPricingDTO> getAllPricings(Pageable pageable) {
        return pricingRepository.findAll(pageable)
                .map(pricingMapper::toDto);
    }

    /**
     * Retrieve pricings not assigned to a given product (paginated).
     */
    public Page<ProductPricingDTO> getPricingsNotAssignedToProduct(Long productId, Pageable pageable) {
        return pricingRepository.findPricingsNotAssignedToProduct(productId, pageable)
                .map(pricingMapper::toDto);
    }

    /**
     * Update a pricing by ID.
     */
    @Transactional
    public ProductPricingDTO updatePricing(Long id, Pricing updatedPricing) {
        int updatedRows = pricingRepository.updatePricingById(
                id,
                updatedPricing.getCurrency(),
                updatedPricing.getOldPrice(),
                updatedPricing.getPrice(),
                updatedPricing.isPromoFlag()
        );

        if (updatedRows == 0) {
            throw new NoSuchElementException("Pricing not found with id: " + id);
        }

        Pricing saved = pricingRepository.findByIdWithProducts(id)
                .orElseThrow(() -> new NoSuchElementException("Pricing not found after update: " + id));

        return pricingMapper.toDto(saved);
    }

    // ---- Hooks ---- //

    @Override
    protected void onAfterSave(Pricing entity) {
        log.info("Pricing saved/updated for ID: {}", entity.getId());
    }

    @Override
    protected void onAfterDelete(Long id) {
        log.info("Pricing deleted for ID: {}", id);
    }
}



