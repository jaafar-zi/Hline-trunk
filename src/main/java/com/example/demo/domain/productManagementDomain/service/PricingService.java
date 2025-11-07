package com.example.demo.domain.productManagementDomain.service;

import com.example.demo.domain.productManagementDomain.dto.ProductPricingDTO;
import com.example.demo.domain.productManagementDomain.entity.Pricing;
import com.example.demo.domain.productManagementDomain.entity.Product;
import com.example.demo.domain.productManagementDomain.repository.PricingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class PricingService {

    @Autowired
    private PricingRepository pricingRepository;

    public Optional<ProductPricingDTO> getPricingById(Long id) {
        return pricingRepository.findByIdWithProducts(id)
                .map(this::mapToDto);
    }

    public List<ProductPricingDTO> getPricingsByProductId(Long productId) {
        return pricingRepository.findByProductIdWithProducts(productId)
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    public List<ProductPricingDTO> getPricingsByCurrency(String currency) {
        return pricingRepository.findByCurrencyWithProducts(currency)
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    public Optional<ProductPricingDTO> getPricingsByProductIdAndCurrency(Long productId, String currency) {
        return pricingRepository.findByProductIdAndCurrencyWithProducts(productId, currency)
                .map(this::mapToDto);
    }

    public Map<Long, ProductPricingDTO> getPricingByProductIds(List<Long> productIds, String currency) {
        return pricingRepository.findByProductIdsAndCurrencyWithProducts(productIds, currency)
                .stream()
                .map(this::mapToDto)
                .collect(HashMap::new, (map, dto) -> dto.getProductIds()
                        .forEach(pid -> map.putIfAbsent(pid, dto)), HashMap::putAll);
    }

    public Page<ProductPricingDTO> getAllPricings(Pageable pageable) {
        return pricingRepository.findAll(pageable)
                .map(this::mapToDto);
    }

    public Page<ProductPricingDTO> getPricingsNotAssignedToProduct(Long productId, Pageable pageable) {
        return pricingRepository.findPricingsNotAssignedToProduct(productId, pageable)
                .map(this::mapToDto);
    }

    public Pricing createPricing(Pricing pricing) {
        return pricingRepository.save(pricing);
    }

    public Pricing updatePricing(Long id, Pricing updatedPricing) {
        int rowsAffected = pricingRepository.updatePricingById(
                id,
                updatedPricing.getCurrency(),
                updatedPricing.getOldPrice(),
                updatedPricing.getPrice(),
                updatedPricing.isPromoFlag()
        );
        if (rowsAffected == 0) {
            throw new RuntimeException("Pricing not found with id: " + id);
        }
        return pricingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pricing not found with id: " + id));
    }

    public void deletePricing(Long id) {
        if (!pricingRepository.existsById(id)) {
            throw new RuntimeException("Pricing not found with id: " + id);
        }
        pricingRepository.deleteById(id);
    }

    private ProductPricingDTO mapToDto(Pricing pricing) {
        return ProductPricingDTO.builder()
                .id(pricing.getId())
                .productIds(
                        pricing.getProducts()
                                .stream()
                                .map(Product::getId)
                                .toList()
                )
                .currency(pricing.getCurrency())
                .oldPrice(pricing.getOldPrice())
                .price(pricing.getPrice())
                .promoFlag(pricing.isPromoFlag())
                .build();
    }
}

