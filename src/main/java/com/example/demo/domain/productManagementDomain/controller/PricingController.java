package com.example.demo.domain.productManagementDomain.controller;

import com.example.demo.domain.productManagementDomain.dto.ProductPricingDTO;
import com.example.demo.domain.productManagementDomain.entity.Pricing;
import com.example.demo.domain.productManagementDomain.service.PricingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/pricings")
public class PricingController {

    @Autowired
    private PricingService pricingService;

    // ─────────────────────────────
    // CREATE
    // ─────────────────────────────

    /**
     * Create a new pricing entry.
     */
    @PostMapping
    public ResponseEntity<ProductPricingDTO> createPricing(@RequestBody Pricing pricing) {
        ProductPricingDTO createdPricing = pricingService.createPricing(pricing);
        return ResponseEntity.ok(createdPricing);
    }

    // ─────────────────────────────
    // READ
    // ─────────────────────────────

    /**
     * Get a pricing entry by its ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductPricingDTO> getPricingById(@PathVariable Long id) {
        Optional<ProductPricingDTO> pricing = pricingService.getPricingById(id);
        return pricing.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Get all pricings paginated.
     */
    @GetMapping
    public ResponseEntity<Page<ProductPricingDTO>> getAllPricings(
            @PageableDefault(size = 10, sort = "currency") Pageable pageable) {
        Page<ProductPricingDTO> page = pricingService.getAllPricings(pageable);
        return ResponseEntity.ok(page);
    }

    /**
     * Get pricings NOT assigned to a given product ID (paginated).
     */
    @GetMapping("/not-assigned-to-product/{productId}")
    public ResponseEntity<Page<ProductPricingDTO>> getPricingsNotAssignedToProduct(
            @PathVariable Long productId,
            @PageableDefault(size = 10, sort = "currency") Pageable pageable) {
        Page<ProductPricingDTO> page = pricingService.getPricingsNotAssignedToProduct(productId, pageable);
        return ResponseEntity.ok(page);
    }

    /**
     * Find all pricing entries for a specific product.
     */
    @GetMapping("/product/{productId}")
    public ResponseEntity<List<ProductPricingDTO>> getPricingsByProductId(@PathVariable Long productId) {
        List<ProductPricingDTO> pricings = pricingService.getPricingsByProductId(productId);
        return ResponseEntity.ok(pricings);
    }

    /**
     * Find pricing entries by currency.
     */
    @GetMapping("/currency/{currency}")
    public ResponseEntity<List<ProductPricingDTO>> getPricingsByCurrency(@PathVariable String currency) {
        List<ProductPricingDTO> pricings = pricingService.getPricingsByCurrency(currency);
        return ResponseEntity.ok(pricings);
    }

    /**
     * Find pricing entries by product ID and currency.
     */
    @GetMapping("/product/{productId}/currency/{currency}")
    public ResponseEntity<ProductPricingDTO> getPricingsByProductIdAndCurrency(
            @PathVariable Long productId,
            @PathVariable String currency) {
        return ResponseEntity.ok(pricingService.getPricingByProductIdAndCurrency(productId, currency).get());
    }

    // ─────────────────────────────
    // UPDATE
    // ─────────────────────────────

    /**
     * Update a pricing entry by ID.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ProductPricingDTO> updatePricing(@PathVariable Long id, @RequestBody Pricing updatedPricing) {
        ProductPricingDTO pricing = pricingService.updatePricing(id, updatedPricing);
        return ResponseEntity.ok(pricing);
    }

    // ─────────────────────────────
    // DELETE
    // ─────────────────────────────

    /**
     * Delete a pricing entry by ID.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePricing(@PathVariable Long id) {
        pricingService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
