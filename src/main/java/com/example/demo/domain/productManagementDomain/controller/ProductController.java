package com.example.demo.domain.productManagementDomain.controller;

import com.example.demo.domain.productManagementDomain.dto.ProductCatalogDTO;
import com.example.demo.domain.productManagementDomain.dto.ProductDTO;
import com.example.demo.domain.productManagementDomain.dto.ProductDetailsDTO;
import com.example.demo.domain.productManagementDomain.dto.ProductTableDTO;
import com.example.demo.domain.productManagementDomain.entity.Product;
import com.example.demo.domain.productManagementDomain.handler.PricingHandler;
import com.example.demo.domain.productManagementDomain.handler.ProductAttributeHandler;
import com.example.demo.domain.productManagementDomain.projection.ProductProjection;
import com.example.demo.domain.productManagementDomain.service.ProductDataTableService;
import com.example.demo.domain.productManagementDomain.service.ProductService;
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
    @Autowired
    private ProductDataTableService productDataTableService;

    @GetMapping("/erp-table")
    public ResponseEntity<Page<ProductTableDTO>> getErpTable(Pageable pageable) {
        return ResponseEntity.ok(productDataTableService.getPagedProductTable(pageable));
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ProductDetailsDTO> getProductById(@PathVariable Long productId) {
        return ResponseEntity.ok(productService.getProductDetails(productId));
    }

    @GetMapping
    public ResponseEntity<Page<ProductCatalogDTO>> getProducts(Pageable pageable) {
        Page<ProductCatalogDTO> productCatalog = productService.getProducts(pageable);
        return ResponseEntity.ok(productCatalog);
    }

    @GetMapping("/search/name")
    public Page<ProductProjection> searchByName(@RequestParam String name, Pageable pageable) {
        return productService.searchProductsByAnyCharacterInName(name,pageable);
    }


    @GetMapping("/search/category")
    public Page<Product> searchByCategoryId(@RequestParam Long categoryId, Pageable pageable) {
        return null;
    }

    @PostMapping("")
    public ResponseEntity<ProductDTO> createProduct(@RequestBody Product product) {
        ProductDTO savedProduct = productService.createProduct(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedProduct);
    }

    @PostMapping("/{productId}/pricing")
    public ResponseEntity<String> assignPricingToProduct(@PathVariable Long productId, @RequestBody List<Long> pricingIds) {
        productService.assignEntitiesToProduct(productId, pricingIds,new PricingHandler());
        return ResponseEntity.ok("");
    }

    @PostMapping("/{productId}/product-attribute")
    public ResponseEntity<String> assignProductAttributeToProduct(@PathVariable Long productId, @RequestBody List<Long> productAttributeIds) {
        productService.assignEntitiesToProduct(productId, productAttributeIds,new ProductAttributeHandler());
        return ResponseEntity.ok("");
    }

    @DeleteMapping("/{productId}/pricing")
    public ResponseEntity<String> unassignPricingToProduct(@RequestParam Long productId, @RequestBody List<Long> pricingIds) {
        productService.unassignEntitiesFromProduct(productId, pricingIds,new PricingHandler());
        return ResponseEntity.ok("");
    }

    @DeleteMapping("/{productId}/product-attribute")
    public ResponseEntity<String> unassignProductAttributeToProduct(@RequestParam Long productId, @RequestBody List<Long> productAttributeIds) {
        productService.unassignEntitiesFromProduct(productId, productAttributeIds,new ProductAttributeHandler());
        return ResponseEntity.ok("");
    }


    @RequestMapping(value = "/products/{productId}", method = RequestMethod.DELETE)
    public ResponseEntity<String> forceDeleteProduct(@RequestParam Long productId,@RequestBody boolean deleteAssets) {
        productService.deleteProductWithConditionalAssets(productId,deleteAssets);
        return ResponseEntity.ok("");
    }

    @RequestMapping(value = "/products/{productId}", method = RequestMethod.DELETE, params = "force=true")
    public ResponseEntity<String> DeleteProduct(@RequestParam Long productId, @RequestParam boolean force) {
        productService.deleteProductKeepAssetsAndAttributes(productId);
        return  null;
    }

}
