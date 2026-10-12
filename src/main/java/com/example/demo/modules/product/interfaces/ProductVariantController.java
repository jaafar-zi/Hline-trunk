package com.example.demo.modules.product.interfaces;

import com.example.demo.modules.product.application.command.ProductVariantCommandService;
import com.example.demo.modules.product.application.dto.filter.ProductVariantFilter;
import com.example.demo.modules.product.application.dto.request.CreateProductVariantRequest;
import com.example.demo.modules.product.application.dto.request.UpdateProductVariantRequest;
import com.example.demo.modules.product.application.dto.response.ProductVariantResponse;
import com.example.demo.modules.product.application.query.ProductVariantQueryService;
import com.example.demo.modules.product.domain.model.vo.SKU;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Product Variant REST Controller
 * 
 * Handles all HTTP requests for product variant operations
 * Implements CQRS pattern with separate command and query services
 * 
 * @author PIM Team
 * @version 1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/products/{productId}/variants")
@RequiredArgsConstructor
@Validated
@Tag(name = "Product Variants", description = "Product variant management endpoints")
public class ProductVariantController {

    private final ProductVariantCommandService commandService;
    private final ProductVariantQueryService queryService;

    // ====================================
    // Command Operations (Writes)
    // ====================================

    @PostMapping
    @Operation(
        summary = "Create product variant",
        description = "Creates a new variant for the specified product"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Variant created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request or SKU already exists"),
        @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<ProductVariantResponse> createVariant(
            @Parameter(description = "Product ID")
            @PathVariable UUID productId,
            @Valid @RequestBody CreateProductVariantRequest request) {
        
        log.info("REST: Creating variant for product: {}", productId);
        
        var variant = commandService.createVariant(productId, request);
        var response = queryService.getVariantById(variant.getId());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{variantId}")
    @Operation(
        summary = "Update product variant",
        description = "Updates an existing product variant"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Variant updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "404", description = "Variant not found")
    })
    public ResponseEntity<ProductVariantResponse> updateVariant(
            @Parameter(description = "Product ID")
            @PathVariable UUID productId,
            @Parameter(description = "Variant ID")
            @PathVariable UUID variantId,
            @Valid @RequestBody UpdateProductVariantRequest request) {
        
        log.info("REST: Updating variant: {} for product: {}", variantId, productId);
        
        var variant = commandService.updateVariant(variantId, request);
        var response = queryService.getVariantById(variant.getId());
        
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{variantId}")
    @Operation(
        summary = "Delete product variant",
        description = "Deletes a variant from the product"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Variant deleted successfully"),
        @ApiResponse(responseCode = "400", description = "Variant does not belong to product"),
        @ApiResponse(responseCode = "404", description = "Product or variant not found")
    })
    public ResponseEntity<Void> deleteVariant(
            @Parameter(description = "Product ID")
            @PathVariable UUID productId,
            @Parameter(description = "Variant ID")
            @PathVariable UUID variantId) {
        
        log.info("REST: Deleting variant: {} from product: {}", variantId, productId);
        
        commandService.deleteVariant(productId, variantId);
        
        return ResponseEntity.noContent().build();
    }

    // ====================================
    // Query Operations (Reads)
    // ====================================

    @GetMapping
    @Operation(
        summary = "Get all variants for product",
        description = "Returns all variants for the specified product"
    )
    @ApiResponse(responseCode = "200", description = "Variants retrieved successfully")
    public ResponseEntity<List<ProductVariantResponse>> getVariantsForProduct(
            @Parameter(description = "Product ID")
            @PathVariable UUID productId) {
        
        log.info("REST: Getting variants for product: {}", productId);
        
        return ResponseEntity.ok(queryService.getVariantsForProduct(productId));
    }

    @GetMapping("/{variantId}")
    @Operation(
        summary = "Get variant by ID",
        description = "Returns a specific variant by its ID"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Variant found"),
        @ApiResponse(responseCode = "404", description = "Variant not found")
    })
    public ResponseEntity<ProductVariantResponse> getVariantById(
            @Parameter(description = "Product ID")
            @PathVariable UUID productId,
            @Parameter(description = "Variant ID")
            @PathVariable UUID variantId) {
        
        log.info("REST: Getting variant: {} for product: {}", variantId, productId);
        
        return ResponseEntity.ok(queryService.getVariantById(variantId));
    }

    @GetMapping("/sku/{sku}")
    @Operation(
        summary = "Get variant by SKU",
        description = "Returns a variant by its SKU"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Variant found"),
        @ApiResponse(responseCode = "404", description = "Variant not found")
    })
    public ResponseEntity<ProductVariantResponse> getVariantBySku(
            @Parameter(description = "Product ID")
            @PathVariable UUID productId,
            @Parameter(description = "Variant SKU")
            @PathVariable String sku) {
        
        log.info("REST: Getting variant by SKU: {} for product: {}", sku, productId);
        
        return ResponseEntity.ok(queryService.getVariantBySku(SKU.of(sku)));
    }

    @GetMapping("/active")
    @Operation(
        summary = "Get active variants",
        description = "Returns all active variants for the product"
    )
    @ApiResponse(responseCode = "200", description = "Active variants retrieved successfully")
    public ResponseEntity<List<ProductVariantResponse>> getActiveVariants(
            @Parameter(description = "Product ID")
            @PathVariable UUID productId) {
        
        log.info("REST: Getting active variants for product: {}", productId);
        
        return ResponseEntity.ok(queryService.getActiveVariantsForProduct(productId));
    }

    @GetMapping("/default")
    @Operation(
        summary = "Get default variant",
        description = "Returns the default variant for the product"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Default variant found"),
        @ApiResponse(responseCode = "404", description = "No default variant set")
    })
    public ResponseEntity<ProductVariantResponse> getDefaultVariant(
            @Parameter(description = "Product ID")
            @PathVariable UUID productId) {
        
        log.info("REST: Getting default variant for product: {}", productId);
        
        return ResponseEntity.ok(queryService.getDefaultVariant(productId));
    }

    @GetMapping("/in-stock")
    @Operation(
        summary = "Get in-stock variants",
        description = "Returns all variants that are currently in stock"
    )
    @ApiResponse(responseCode = "200", description = "In-stock variants retrieved successfully")
    public ResponseEntity<List<ProductVariantResponse>> getInStockVariants(
            @Parameter(description = "Product ID")
            @PathVariable UUID productId) {
        
        log.info("REST: Getting in-stock variants for product: {}", productId);
        
        return ResponseEntity.ok(queryService.getInStockVariants(productId));
    }

    @GetMapping("/by-attributes")
    @Operation(
        summary = "Get variants by attributes",
        description = "Returns variants matching the specified attribute values"
    )
    @ApiResponse(responseCode = "200", description = "Variants retrieved successfully")
    public ResponseEntity<List<ProductVariantResponse>> getVariantsByAttributes(
            @Parameter(description = "Product ID")
            @PathVariable UUID productId,
            @Parameter(description = "Attribute key-value pairs (e.g., color=red, size=L)")
            @RequestParam Map<String, String> attributes) {
        
        log.info("REST: Getting variants by attributes for product: {}", productId);
        
        return ResponseEntity.ok(queryService.getVariantsByAttributes(productId, attributes));
    }

    @GetMapping("/search")
    @Operation(
        summary = "Search variants",
        description = "Search and filter variants with pagination"
    )
    @ApiResponse(responseCode = "200", description = "Variants retrieved successfully")
    public ResponseEntity<Page<ProductVariantResponse>> searchVariants(
            @Parameter(description = "Product ID")
            @PathVariable UUID productId,
            @Parameter(description = "Variant filter criteria")
            @ModelAttribute ProductVariantFilter filter) {
        
        log.info("REST: Searching variants for product: {}", productId);
        
        // Ensure filter is scoped to the product
        filter.setProductId(productId);
        
        return ResponseEntity.ok(queryService.searchVariants(filter));
    }

    @GetMapping("/count")
    @Operation(
        summary = "Count variants",
        description = "Returns the total number of variants for the product"
    )
    @ApiResponse(responseCode = "200", description = "Count retrieved successfully")
    public ResponseEntity<Long> countVariants(
            @Parameter(description = "Product ID")
            @PathVariable UUID productId) {
        
        log.info("REST: Counting variants for product: {}", productId);
        
        return ResponseEntity.ok(queryService.countVariantsForProduct(productId));
    }

    @GetMapping("/sku-exists/{sku}")
    @Operation(
        summary = "Check SKU existence",
        description = "Checks if a SKU already exists in the system"
    )
    @ApiResponse(responseCode = "200", description = "Check completed successfully")
    public ResponseEntity<Boolean> skuExists(
            @Parameter(description = "Product ID")
            @PathVariable UUID productId,
            @Parameter(description = "SKU to check")
            @PathVariable String sku) {
        
        log.info("REST: Checking if SKU exists: {}", sku);
        
        return ResponseEntity.ok(queryService.skuExists(sku));
    }
}