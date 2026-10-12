package com.example.demo.modules.product.interfaces;

import com.example.demo.common.base.controller.ApiResponse;
import com.example.demo.common.base.controller.BaseController;
import com.example.demo.common.validation.groups.Create;
import com.example.demo.common.validation.groups.Publish;
import com.example.demo.common.validation.groups.Update;
import com.example.demo.modules.product.application.command.ProductAttributeCommandService;
import com.example.demo.modules.product.application.command.ProductCommandService;
import com.example.demo.modules.product.application.command.ProductImageCommandService;
import com.example.demo.modules.product.application.command.ProductInventoryCommandService;
import com.example.demo.modules.product.application.command.ProductPriceCommandService;
import com.example.demo.modules.product.application.command.ProductVariantCommandService;
import com.example.demo.modules.product.application.dto.filter.ProductFilter;
import com.example.demo.modules.product.application.dto.request.*;
import com.example.demo.modules.product.application.dto.response.ProductResponse;
import com.example.demo.modules.product.application.query.ProductQueryService;
import com.example.demo.modules.product.domain.model.Product;
import com.example.demo.modules.product.domain.model.vo.SKU;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Product REST controller
 * 
 * @author PIM Team
 * @version 1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Validated
@Tag(name = "Products", description = "Product management APIs")
public class ProductController extends BaseController {

    private final ProductCommandService productCommandService;
    private final ProductQueryService productQueryService;
    private final ProductAttributeCommandService attributeCommandService;
    private final ProductImageCommandService imageCommandService;
    private final ProductPriceCommandService priceCommandService;
    private final ProductInventoryCommandService inventoryCommandService;
    private final ProductVariantCommandService variantCommandService;

    // ====================================
    // Query Operations
    // ====================================

    @GetMapping
    @Operation(summary = "Search products", description = "Search products with filters and pagination")
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> searchProducts(
            @Parameter(description = "Product filter criteria")
            @ModelAttribute ProductFilter filter) {
        
        log.info("Searching products with filter");
        
        Page<ProductResponse> products = productQueryService.searchProducts(filter);
        
        return success(products);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID", description = "Get product details by ID")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductById(
            @Parameter(description = "Product ID", required = true)
            @PathVariable UUID id) {
        
        log.info("Getting product by ID: {}", id);
        
        ProductResponse product = productQueryService.getProductById(id);
        
        return success(product);
    }

    @GetMapping("/sku/{sku}")
    @Operation(summary = "Get product by SKU", description = "Get product details by SKU")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductBySku(
            @Parameter(description = "Product SKU", required = true)
            @PathVariable String sku) {
        
        log.info("Getting product by SKU: {}", sku);
        
        ProductResponse product = productQueryService.getProductBySku(SKU.of(sku));
        
        return success(product);
    }

    @GetMapping("/category/{categoryId}")
    @Operation(summary = "Get products by category", description = "Get all products in a category")
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getProductsByCategory(
            @Parameter(description = "Category ID", required = true)
            @PathVariable UUID categoryId,
            @Parameter(description = "Product filter criteria")
            @ModelAttribute ProductFilter filter) {
        
        log.info("Getting products by category: {}", categoryId);
        
        Page<ProductResponse> products = productQueryService.getProductsByCategory(categoryId, filter);
        
        return success(products);
    }

    @GetMapping("/brand/{brand}")
    @Operation(summary = "Get products by brand", description = "Get all products of a brand")
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getProductsByBrand(
            @Parameter(description = "Brand name", required = true)
            @PathVariable String brand,
            @Parameter(description = "Product filter criteria")
            @ModelAttribute ProductFilter filter) {
        
        log.info("Getting products by brand: {}", brand);
        
        Page<ProductResponse> products = productQueryService.getProductsByBrand(brand, filter);
        
        return success(products);
    }

    @GetMapping("/published")
    @Operation(summary = "Get published products", description = "Get all published products")
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getPublishedProducts(
            @Parameter(description = "Product filter criteria")
            @ModelAttribute ProductFilter filter) {
        
        log.info("Getting published products");
        
        Page<ProductResponse> products = productQueryService.getPublishedProducts(filter);
        
        return success(products);
    }

    @GetMapping("/ready-to-publish")
    @Operation(summary = "Get products ready to publish", description = "Get all products that are ready to be published")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getProductsReadyToPublish() {
        
        log.info("Getting products ready to publish");
        
        List<ProductResponse> products = productQueryService.getProductsReadyToPublish();
        
        return success(products);
    }

    @GetMapping("/incomplete")
    @Operation(summary = "Get incomplete products", description = "Get all products missing required data")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getIncompleteProducts() {
        
        log.info("Getting incomplete products");
        
        List<ProductResponse> products = productQueryService.getIncompleteProducts();
        
        return success(products);
    }

    @GetMapping("/low-stock")
    @Operation(summary = "Get low stock products", description = "Get all products with low stock")
    @PreAuthorize("hasRole('ADMIN') or hasRole('INVENTORY_MANAGER')")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getLowStockProducts() {
        
        log.info("Getting low stock products");
        
        List<ProductResponse> products = productQueryService.getLowStockProducts();
        
        return success(products);
    }

    @GetMapping("/search")
    @Operation(summary = "Search products by keyword", description = "Full-text search across product fields")
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> searchByKeyword(
            @Parameter(description = "Search keyword", required = true)
            @RequestParam String keyword,
            @Parameter(description = "Product filter criteria")
            @ModelAttribute ProductFilter filter) {
        
        log.info("Searching products by keyword: {}", keyword);
        
        Page<ProductResponse> products = productQueryService.searchByKeyword(keyword, filter);
        
        return success(products);
    }

    @GetMapping("/statistics")
    @Operation(summary = "Get product statistics", description = "Get product statistics and counts")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductQueryService.ProductStatistics>> getStatistics() {
        
        log.info("Getting product statistics");
        
        ProductQueryService.ProductStatistics statistics = productQueryService.getStatistics();
        
        return success(statistics);
    }

    // ====================================
    // Command Operations
    // ====================================

    @PostMapping
    @Operation(summary = "Create product", description = "Create a new product")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PRODUCT_MANAGER')")
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
            @Parameter(description = "Product creation request", required = true)
            @Valid @RequestBody CreateProductRequest request) {
        
        log.info("Creating product: {}", request.getName());
        
        // Validate with Create group
        validateAndNormalize(request, Create.class);
        
        Product product = productCommandService.createProduct(request);
        ProductResponse response = productQueryService.getProductById(product.getId());
        
        return created(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update product", description = "Update an existing product")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PRODUCT_MANAGER')")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @Parameter(description = "Product ID", required = true)
            @PathVariable UUID id,
            @Parameter(description = "Product update request", required = true)
            @Valid @RequestBody UpdateProductRequest request) {
        
        log.info("Updating product: {}", id);
        
        // Validate with Update group
        validateAndNormalize(request, Update.class);
        
        Product product = productCommandService.updateProduct(id, request);
        ProductResponse response = productQueryService.getProductById(product.getId());
        
        return success(response, "Product updated successfully");
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete product", description = "Delete a product (soft delete)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(
            @Parameter(description = "Product ID", required = true)
            @PathVariable UUID id) {
        
        log.info("Deleting product: {}", id);
        
        productCommandService.delete(id);
        
        return success(null, "Product deleted successfully");
    }

    // ====================================
    // Publishing Operations
    // ====================================

    @PostMapping("/{id}/publish")
    @Operation(summary = "Publish product", description = "Publish a product to make it visible to customers")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PRODUCT_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> publishProduct(
            @Parameter(description = "Product ID", required = true)
            @PathVariable UUID id,
            @Parameter(description = "Publish request")
            @Valid @RequestBody PublishProductRequest request) {
        
        log.info("Publishing product: {}", id);
        
        validateAndNormalize(request, Publish.class);
        
        productCommandService.publishProduct(id, request);
        
        return success(null, "Product published successfully");
    }

    @PostMapping("/{id}/unpublish")
    @Operation(summary = "Unpublish product", description = "Unpublish a product to hide it from customers")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PRODUCT_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> unpublishProduct(
            @Parameter(description = "Product ID", required = true)
            @PathVariable UUID id,
            @Parameter(description = "Unpublish request", required = true)
            @Valid @RequestBody UnpublishProductRequest request) {
        
        log.info("Unpublishing product: {}", id);
        
        validateAndNormalize(request);
        
        productCommandService.unpublishProduct(id, request);
        
        return success(null, "Product unpublished successfully");
    }

    @PostMapping("/{id}/discontinue")
    @Operation(summary = "Discontinue product", description = "Mark a product as discontinued")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> discontinueProduct(
            @Parameter(description = "Product ID", required = true)
            @PathVariable UUID id,
            @Parameter(description = "Discontinue request", required = true)
            @Valid @RequestBody DiscontinueProductRequest request) {
        
        log.info("Discontinuing product: {}", id);
        
        validateAndNormalize(request);
        
        productCommandService.discontinueProduct(id, request);
        
        return success(null, "Product discontinued successfully");
    }

    // ====================================
    // Attribute Operations
    // ====================================

    @PostMapping("/{id}/attributes")
    @Operation(summary = "Add attribute", description = "Add an attribute to a product")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PRODUCT_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> addAttribute(
            @Parameter(description = "Product ID", required = true)
            @PathVariable UUID id,
            @Parameter(description = "Attribute request", required = true)
            @Valid @RequestBody AddProductAttributeRequest request) {
        
        log.info("Adding attribute to product: {}", id);
        
        validateAndNormalize(request);
        
        attributeCommandService.addAttribute(id, request);
        
        return success(null, "Attribute added successfully");
    }

    @PutMapping("/{id}/attributes/{key}")
    @Operation(summary = "Update attribute", description = "Update a product attribute")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PRODUCT_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> updateAttribute(
            @Parameter(description = "Product ID", required = true)
            @PathVariable UUID id,
            @Parameter(description = "Attribute key", required = true)
            @PathVariable String key,
            @Parameter(description = "Attribute update request", required = true)
            @Valid @RequestBody UpdateProductAttributeRequest request) {
        
        log.info("Updating attribute '{}' for product: {}", key, id);
        
        validateAndNormalize(request);
        
        attributeCommandService.updateAttribute(id, key, request);
        
        return success(null, "Attribute updated successfully");
    }

    @DeleteMapping("/{id}/attributes/{key}")
    @Operation(summary = "Remove attribute", description = "Remove an attribute from a product")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PRODUCT_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> removeAttribute(
            @Parameter(description = "Product ID", required = true)
            @PathVariable UUID id,
            @Parameter(description = "Attribute key", required = true)
            @PathVariable String key) {
        
        log.info("Removing attribute '{}' from product: {}", key, id);
        
        attributeCommandService.removeAttribute(id, key);
        
        return success(null, "Attribute removed successfully");
    }

    // ====================================
    // Image Operations
    // ====================================

    @PostMapping("/{id}/images")
    @Operation(summary = "Add image", description = "Add an image to a product")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PRODUCT_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> addImage(
            @Parameter(description = "Product ID", required = true)
            @PathVariable UUID id,
            @Parameter(description = "Image request", required = true)
            @Valid @RequestBody AddProductImageRequest request) {
        
        log.info("Adding image to product: {}", id);
        
        validateAndNormalize(request);
        
        imageCommandService.addImage(id, request);
        
        return success(null, "Image added successfully");
    }

    @DeleteMapping("/{id}/images/{imageId}")
    @Operation(summary = "Remove image", description = "Remove an image from a product")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PRODUCT_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> removeImage(
            @Parameter(description = "Product ID", required = true)
            @PathVariable UUID id,
            @Parameter(description = "Image ID", required = true)
            @PathVariable UUID imageId) {
        
        log.info("Removing image {} from product: {}", imageId, id);
        
        imageCommandService.removeImage(id, imageId);
        
        return success(null, "Image removed successfully");
    }

    @PutMapping("/{id}/images/{imageId}/primary")
    @Operation(summary = "Set primary image", description = "Set an image as the primary product image")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PRODUCT_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> setPrimaryImage(
            @Parameter(description = "Product ID", required = true)
            @PathVariable UUID id,
            @Parameter(description = "Image ID", required = true)
            @PathVariable UUID imageId) {
        
        log.info("Setting primary image {} for product: {}", imageId, id);
        
        imageCommandService.setPrimaryImage(id, imageId);
        
        return success(null, "Primary image set successfully");
    }

    // ====================================
    // Price Operations
    // ====================================

    @PostMapping("/{id}/prices")
    @Operation(summary = "Add price", description = "Add a price to a product")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PRODUCT_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> addPrice(
            @Parameter(description = "Product ID", required = true)
            @PathVariable UUID id,
            @Parameter(description = "Price request", required = true)
            @Valid @RequestBody AddProductPriceRequest request) {
        
        log.info("Adding price to product: {}", id);
        
        validateAndNormalize(request);
        
        priceCommandService.addPrice(id, request);
        
        return success(null, "Price added successfully");
    }

    @PutMapping("/prices/{priceId}")
    @Operation(summary = "Update price", description = "Update a product price")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PRODUCT_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> updatePrice(
            @Parameter(description = "Price ID", required = true)
            @PathVariable UUID priceId,
            @Parameter(description = "Price update request", required = true)
            @Valid @RequestBody UpdatePriceRequest request) {
        
        log.info("Updating price: {}", priceId);
        
        validateAndNormalize(request);
        
        priceCommandService.updatePrice(priceId, request);
        
        return success(null, "Price updated successfully");
    }

    @DeleteMapping("/prices/{priceId}")
    @Operation(summary = "Delete price", description = "Delete a product price")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deletePrice(
            @Parameter(description = "Price ID", required = true)
            @PathVariable UUID priceId) {
        
        log.info("Deleting price: {}", priceId);
        
        priceCommandService.delete(priceId);
        
        return success(null, "Price deleted successfully");
    }

    // ====================================
    // Inventory Operations
    // ====================================

    @PutMapping("/{id}/inventory")
    @Operation(summary = "Update inventory", description = "Update product inventory")
    @PreAuthorize("hasRole('ADMIN') or hasRole('INVENTORY_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> updateInventory(
            @Parameter(description = "Product ID", required = true)
            @PathVariable UUID id,
            @Parameter(description = "Inventory update request", required = true)
            @Valid @RequestBody UpdateInventoryRequest request) {
        
        log.info("Updating inventory for product: {}", id);
        
        validateAndNormalize(request);
        
        inventoryCommandService.updateInventory(id, request);
        
        return success(null, "Inventory updated successfully");
    }

    @PostMapping("/inventory/bulk-update")
    @Operation(summary = "Bulk update inventory", description = "Update inventory for multiple products")
    @PreAuthorize("hasRole('ADMIN') or hasRole('INVENTORY_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> bulkUpdateInventory(
            @Parameter(description = "Bulk inventory update request", required = true)
            @Valid @RequestBody BulkUpdateInventoryRequest request) {
        
        log.info("Bulk updating inventory for {} products", request.getUpdates().size());
        
        validateAndNormalize(request);
        
        inventoryCommandService.bulkUpdateInventory(request);
        
        return success(null, "Bulk inventory update completed successfully");
    }

    // ====================================
    // Variant Operations
    // ====================================

    @PostMapping("/{id}/variants")
    @Operation(summary = "Create variant", description = "Create a product variant")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PRODUCT_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> createVariant(
            @Parameter(description = "Product ID", required = true)
            @PathVariable UUID id,
            @Parameter(description = "Variant creation request", required = true)
            @Valid @RequestBody CreateProductVariantRequest request) {
        
        log.info("Creating variant for product: {}", id);
        
        validateAndNormalize(request, Create.class);
        
        variantCommandService.createVariant(id, request);
        
        return created(null);
    }

    @PutMapping("/variants/{variantId}")
    @Operation(summary = "Update variant", description = "Update a product variant")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PRODUCT_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> updateVariant(
            @Parameter(description = "Variant ID", required = true)
            @PathVariable UUID variantId,
            @Parameter(description = "Variant update request", required = true)
            @Valid @RequestBody UpdateProductVariantRequest request) {
        
        log.info("Updating variant: {}", variantId);
        
        validateAndNormalize(request, Update.class);
        
        variantCommandService.updateVariant(variantId, request);
        
        return success(null, "Variant updated successfully");
    }

    @DeleteMapping("/{id}/variants/{variantId}")
    @Operation(summary = "Delete variant", description = "Delete a product variant")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteVariant(
            @Parameter(description = "Product ID", required = true)
            @PathVariable UUID id,
            @Parameter(description = "Variant ID", required = true)
            @PathVariable UUID variantId) {
        
        log.info("Deleting variant {} from product: {}", variantId, id);
        
        variantCommandService.deleteVariant(id, variantId);
        
        return success(null, "Variant deleted successfully");
    }

    // ====================================
    // Bulk Operations
    // ====================================

    @PostMapping("/bulk-actions")
    @Operation(summary = "Bulk product actions", description = "Perform bulk actions on multiple products")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> bulkProductActions(
            @Parameter(description = "Bulk action request", required = true)
            @Valid @RequestBody BulkProductActionRequest request) {
        
        log.info("Performing bulk action '{}' on {} products", request.getAction(), request.getProductIds().size());
        
        validateAndNormalize(request);
        
        // Handle different bulk actions
        // This would be implemented based on your business requirements
        
        return success(null, String.format("Bulk action '%s' completed successfully", request.getAction()));
    }
}