package com.example.demo.productCatalog.controller;

import com.example.demo.productCatalog.dto.ProductCatalogDTO;
import com.example.demo.productCatalog.dto.ProductDTO;
import com.example.demo.productCatalog.entity.Product;
import com.example.demo.productCatalog.handler.PricingHandler;
import com.example.demo.productCatalog.handler.ProductAttributeHandler;
import com.example.demo.productCatalog.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping
    public ResponseEntity<ProductDTO> getProductById(@RequestParam("id") Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    /**
     * Get a paginated list of products.
     *
     * @param pageable Pageable object for pagination parameters
     * @return A paginated list of product catalog DTOs
     */
    @GetMapping
    public ResponseEntity<Page<ProductCatalogDTO>> getProducts(Pageable pageable) {
        Page<ProductCatalogDTO> productCatalog = productService.getProducts(pageable);
        return ResponseEntity.ok(productCatalog);
    }

    @GetMapping("/search/name")
    public Page<Product> searchByName(@RequestParam String name, Pageable pageable) {
        return null;
    }


    @GetMapping("/search/category")
    public Page<Product> searchByCategoryId(@RequestParam Long categoryId, Pageable pageable) {
        return null;
    }

    @PostMapping("/products")
    public ResponseEntity<ProductDTO> createProduct(@RequestBody Product product) {
        ProductDTO savedProduct = productService.createProduct(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedProduct);
    }

    @PostMapping("/products/{productId}/pricing")
    public ResponseEntity<String> assignPricingToProduct(@PathVariable Long productId, @RequestBody List<Long> pricingIds) {
        productService.assignEntitiesToProduct(productId, pricingIds,new PricingHandler());
        return ResponseEntity.ok("");
    }

    @PostMapping("/products/{productId}/product-attribute")
    public ResponseEntity<String> assignProductAttributeToProduct(@PathVariable Long productId, @RequestBody List<Long> productAttributeIds) {
        productService.assignEntitiesToProduct(productId, productAttributeIds,new ProductAttributeHandler());
        return ResponseEntity.ok("");
    }

    @DeleteMapping("/products/{productId}/pricing")
    public ResponseEntity<String> unassignPricingToProduct(@RequestParam Long productId, @RequestBody List<Long> pricingIds) {
        productService.assignEntitiesToProduct(productId, pricingIds,new PricingHandler());
        return ResponseEntity.ok("");
    }

    @DeleteMapping("/products/{productId}/product-attribute")
    public ResponseEntity<String> unassignProductAttributeToProduct(@RequestParam Long productId, @RequestBody List<Long> productAttributeIds) {
        productService.assignEntitiesToProduct(productId, productAttributeIds,new ProductAttributeHandler());
        return ResponseEntity.ok("");
    }

}
