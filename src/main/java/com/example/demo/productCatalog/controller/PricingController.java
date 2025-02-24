package com.example.demo.productCatalog.controller;

import com.example.demo.productCatalog.entity.Pricing;
import com.example.demo.productCatalog.service.PricingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/pricings")
public class PricingController {

    @Autowired
    private PricingService pricingService;

    // Create a new pricing entry
    @PostMapping
    public ResponseEntity<Pricing> createPricing(@RequestBody Pricing pricing) {
        Pricing createdPricing = pricingService.createPricing(pricing);
        return ResponseEntity.ok(createdPricing);
    }


    // Get a pricing entry by ID
    @GetMapping("/{id}")
    public ResponseEntity<Pricing> getPricingById(@PathVariable Long id) {
        Optional<Pricing> pricing = pricingService.getPricingById(id);
        return pricing.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Update a pricing entry
    @PutMapping("/{id}")
    public ResponseEntity<Pricing> updatePricing(@PathVariable Long id, @RequestBody Pricing updatedPricing) {
        Pricing pricing = pricingService.updatePricing(id, updatedPricing);
        return ResponseEntity.ok(pricing);
    }

    // Delete a pricing entry by ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePricing(@PathVariable Long id) {
        pricingService.deletePricing(id);
        return ResponseEntity.noContent().build();
    }

    // Find all pricing entries for a specific product
    @GetMapping("/product/{productId}")
    public ResponseEntity<List<Pricing>> getPricingsByProductId(@PathVariable Long productId) {
        List<Pricing> pricings = pricingService.getPricingsByProductId(productId);
        return ResponseEntity.ok(pricings);
    }

    // Find pricing entries by currency
    @GetMapping("/currency/{currency}")
    public ResponseEntity<List<Pricing>> getPricingsByCurrency(@PathVariable String currency) {
        List<Pricing> pricings = pricingService.getPricingsByCurrency(currency);
        return ResponseEntity.ok(pricings);
    }

    // Find pricing entries by product ID and currency
    @GetMapping("/product/{productId}/currency/{currency}")
    public ResponseEntity<List<Pricing>> getPricingsByProductIdAndCurrency(
            @PathVariable Long productId,
            @PathVariable String currency) {
        List<Pricing> pricings = pricingService.getPricingsByProductIdAndCurrency(productId, currency);
        return ResponseEntity.ok(pricings);
    }
}