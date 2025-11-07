package com.example.demo.domain.productManagementDomain.controller;

import com.example.demo.domain.productManagementDomain.dto.ProductAttributeDTO;
import com.example.demo.domain.productManagementDomain.service.ProductAttributeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/product-attributes")
public class ProductAttributeController {

    @Autowired
    private ProductAttributeService productAttributeService;

    // ───────────────────────────────────────────────
    //  CREATE
    // ───────────────────────────────────────────────

    /**
     * Create a new ProductAttribute
     */
    @PostMapping
    public ResponseEntity<ProductAttributeDTO> createProductAttribute(@RequestBody ProductAttributeDTO productAttributeDTO) {
        ProductAttributeDTO created = productAttributeService.createProductAttribute(productAttributeDTO);
        return ResponseEntity.ok(created);
    }

    // ───────────────────────────────────────────────
    //  READ
    // ───────────────────────────────────────────────

    /**
     * Get all ProductAttributes with pagination
     */
    @GetMapping
    public ResponseEntity<Page<ProductAttributeDTO>> getAllProductAttributes(
            @PageableDefault(size = 10, sort = "attributeName") Pageable pageable) {
        return ResponseEntity.ok(productAttributeService.getAllProductAttributes(pageable));
    }

    /**
     * Get a ProductAttribute by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductAttributeDTO> getProductAttributeById(@PathVariable Long id) {
        return ResponseEntity.ok(productAttributeService.getById(id));
    }

    /**
     * Get ProductAttributes assigned to a product
     */
    @GetMapping("/by-product/{productId}")
    public ResponseEntity<List<ProductAttributeDTO>> getAttributesByProductId(@PathVariable Long productId) {
        return ResponseEntity.ok(productAttributeService.getAttributesByProductId(productId));
    }

    /**
     * Get ProductAttributes NOT assigned to a product (paginated)
     */
    @GetMapping("/not-assigned-to-product/{productId}")
    public ResponseEntity<Page<ProductAttributeDTO>> getAttributesNotAssignedToProduct(
            @PathVariable Long productId,
            @PageableDefault(size = 10, sort = "attributeName") Pageable pageable) {
        return ResponseEntity.ok(productAttributeService.getAttributesNotAssignedToProduct(productId, pageable));
    }

    /**
     * Find ProductAttributes by attribute name
     */
    @GetMapping("/by-name/{attributeName}")
    public ResponseEntity<List<ProductAttributeDTO>> getAttributesByAttributeName(@PathVariable String attributeName) {
        return ResponseEntity.ok(productAttributeService.getAttributesByAttributeName(attributeName));
    }

    /**
     * Find by attribute name and value
     */
    @GetMapping("/by-name/{attributeName}/value/{attributeValue}")
    public ResponseEntity<List<ProductAttributeDTO>> getAttributesByAttributeNameAndValue(
            @PathVariable String attributeName,
            @PathVariable String attributeValue) {
        return ResponseEntity.ok(productAttributeService.getAttributesByAttributeNameAndValue(attributeName, attributeValue));
    }

    /**
     * Find by product ID and attribute name
     */
    @GetMapping("/by-product/{productId}/name/{attributeName}")
    public ResponseEntity<List<ProductAttributeDTO>> getAttributesByProductIdAndAttributeName(
            @PathVariable Long productId,
            @PathVariable String attributeName) {
        return ResponseEntity.ok(productAttributeService.getAttributesByProductIdAndAttributeName(productId, attributeName));
    }

    // ───────────────────────────────────────────────
    //  UPDATE
    // ───────────────────────────────────────────────

    @PutMapping("/{id}")
    public ResponseEntity<ProductAttributeDTO> updateProductAttribute(
            @PathVariable Long id,
            @RequestBody ProductAttributeDTO updatedDto) {
        return ResponseEntity.ok(productAttributeService.updateProductAttribute(id, updatedDto));
    }

    // ───────────────────────────────────────────────
    //  DELETE
    // ───────────────────────────────────────────────

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProductAttribute(@PathVariable Long id) {
        productAttributeService.deleteProductAttribute(id);
        return ResponseEntity.noContent().build();
    }
}
