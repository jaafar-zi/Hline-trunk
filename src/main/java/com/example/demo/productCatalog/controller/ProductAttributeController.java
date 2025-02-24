package com.example.demo.productCatalog.controller;

import com.example.demo.productCatalog.entity.ProductAttribute;
import com.example.demo.productCatalog.service.ProductAttributeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/product-attributes")
public class ProductAttributeController {

    @Autowired
    private ProductAttributeService productAttributeService;

    // Create a new product attribute
    @PostMapping
    public ResponseEntity<ProductAttribute> createProductAttribute(@RequestBody ProductAttribute productAttribute) {
        ProductAttribute createdProductAttribute = productAttributeService.createProductAttribute(productAttribute);
        return ResponseEntity.ok(createdProductAttribute);
    }

    // Get all product attributes
    @GetMapping
    public ResponseEntity<List<ProductAttribute>> getAllProductAttributes() {
        List<ProductAttribute> productAttributes = productAttributeService.getAllProductAttributes();
        return ResponseEntity.ok(productAttributes);
    }


    @GetMapping("/{id}")
    public ResponseEntity<ProductAttribute> getProductAttributeById(@PathVariable Long id) {
        productAttributeService.getAllProductAttributes();
        return productAttribute.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }


    @PutMapping("/{id}")
    public ResponseEntity<ProductAttribute> updateProductAttribute(
            @PathVariable Long id,
            @RequestBody ProductAttribute updatedProductAttribute) {
        ProductAttribute productAttribute = productAttributeService.updateProductAttribute(id, updatedProductAttribute);
        return ResponseEntity.ok(productAttribute);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProductAttribute(@PathVariable Long id) {
        productAttributeService.deleteProductAttribute(id);
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/product/{productId}")
    public ResponseEntity<List<ProductAttribute>> getAttributesByProductId(@PathVariable Long productId) {
        List<ProductAttribute> productAttributes = productAttributeService.getAttributesByProductId(productId);
        return ResponseEntity.ok(productAttributes);
    }

    // Find attributes by attribute name
    @GetMapping("/attribute-name/{attributeName}")
    public ResponseEntity<List<ProductAttribute>> getAttributesByAttributeName(@PathVariable String attributeName) {
        List<ProductAttribute> productAttributes = productAttributeService.getAttributesByAttributeName(attributeName);
        return ResponseEntity.ok(productAttributes);
    }


    @GetMapping("/attribute-name/{attributeName}/attribute-value/{attributeValue}")
    public ResponseEntity<List<ProductAttribute>> getAttributesByAttributeNameAndValue(
            @PathVariable String attributeName,
            @PathVariable String attributeValue) {
        List<ProductAttribute> productAttributes = productAttributeService.getAttributesByAttributeNameAndValue(attributeName, attributeValue);
        return ResponseEntity.ok(productAttributes);
    }


    @GetMapping("/product/{productId}/attribute-name/{attributeName}")
    public ResponseEntity<List<ProductAttribute>> getAttributesByProductIdAndAttributeName(
            @PathVariable Long productId,
            @PathVariable String attributeName) {
        List<ProductAttribute> productAttributes = productAttributeService.getAttributesByProductIdAndAttributeName(productId, attributeName);
        return ResponseEntity.ok(productAttributes);
    }
}