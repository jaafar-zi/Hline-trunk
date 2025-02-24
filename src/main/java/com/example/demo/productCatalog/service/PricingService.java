package com.example.demo.productCatalog.service;

import com.example.demo.productCatalog.dto.ProductPricingDTO;
import com.example.demo.productCatalog.entity.Pricing;
import com.example.demo.productCatalog.repository.PricingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class PricingService {

    @Autowired
    private PricingRepository pricingRepository;

    // Create a new pricing entry
    public Pricing createPricing(Pricing pricing) {
        return pricingRepository.save(pricing);
    }

    // Get a pricing entry by ID
    public Optional<Pricing> getPricingById(Long id) {
        return pricingRepository.findById(id);
    }

    // Update a pricing entry
    public Pricing updatePricing(Long id, Pricing updatedPricing) {
        return pricingRepository.findById(id)
                .map(pricing -> {
                    pricing.setCurrency(updatedPricing.getCurrency());
                    pricing.setPrice(updatedPricing.getPrice());
                    return pricingRepository.save(pricing);
                })
                .orElseThrow(() -> new RuntimeException("Pricing not found with id: " + id));
    }

    // Delete a pricing entry by ID
    public void deletePricing(Long id) {
        if (!pricingRepository.existsById(id)) {
            throw new RuntimeException("Pricing not found with id: " + id);
        }
        pricingRepository.deleteById(id);
    }

    // Find all pricing entries for a specific product
    public List<Pricing> getPricingsByProductId(Long productId) {
        return pricingRepository.findByProductId(productId);
    }

    // Find pricing entries by currency
    public List<Pricing> getPricingsByCurrency(String currency) {
        return pricingRepository.findByCurrency(currency);
    }

    // Find pricing entries by product ID and currency
    public List<Pricing> getPricingsByProductIdAndCurrency(Long productId, String currency) {
        return pricingRepository.findByProductIdAndCurrency(productId, currency);
    }

    public Map<Long, ProductPricingDTO> getPricingByProductIds(List<Long> productIds, String currency) {
        Map<Long, ProductPricingDTO> productPricingDTOMap = new HashMap<>();
        pricingRepository.getPricingByTheProductIds(productIds, currency).forEach(p ->
                productPricingDTOMap.putIfAbsent(p.getProductId(), p)
        );
        return productPricingDTOMap;
    }
}