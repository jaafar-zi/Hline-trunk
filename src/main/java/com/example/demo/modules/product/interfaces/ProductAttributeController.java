package com.example.demo.modules.product.interfaces;

import com.example.demo.common.base.controller.ApiResponse;
import com.example.demo.common.base.controller.BaseController;
import com.example.demo.modules.product.application.dto.response.ProductAttributeResponse;
import com.example.demo.modules.product.application.query.ProductAttributeQueryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Product attribute query controller
 * 
 * @author PIM Team
 * @version 3.0
 */
@Slf4j
@RestController
@RequestMapping("/products/{productId}/attributes")
@RequiredArgsConstructor
@Tag(name = "Product Attributes", description = "Product attribute query APIs")
public class ProductAttributeController extends BaseController {

    private final ProductAttributeQueryService attributeQueryService;

    @GetMapping
    @Operation(summary = "Get all attributes for product", description = "Get all attributes of a product")
    public ResponseEntity<ApiResponse<List<ProductAttributeResponse>>> getAttributesForProduct(
            @Parameter(description = "Product ID", required = true)
            @PathVariable UUID productId) {
        
        log.info("Getting attributes for product: {}", productId);
        
        List<ProductAttributeResponse> attributes = attributeQueryService.getAttributesForProduct(productId);
        
        return success(attributes);
    }

    @GetMapping("/{key}")
    @Operation(summary = "Get attribute by key", description = "Get a specific attribute by key")
    public ResponseEntity<ApiResponse<ProductAttributeResponse>> getAttributeByKey(
            @Parameter(description = "Product ID", required = true)
            @PathVariable UUID productId,
            @Parameter(description = "Attribute key", required = true)
            @PathVariable String key) {
        
        log.info("Getting attribute '{}' for product: {}", key, productId);
        
        ProductAttributeResponse attribute = attributeQueryService.getAttributeByProductAndKey(productId, key);
        
        return success(attribute);
    }

    @GetMapping("/required")
    @Operation(summary = "Get required attributes", description = "Get all required attributes")
    public ResponseEntity<ApiResponse<List<ProductAttributeResponse>>> getRequiredAttributes(
            @Parameter(description = "Product ID", required = true)
            @PathVariable UUID productId) {
        
        log.info("Getting required attributes for product: {}", productId);
        
        List<ProductAttributeResponse> attributes = attributeQueryService.getRequiredAttributes(productId);
        
        return success(attributes);
    }

    @GetMapping("/optional")
    @Operation(summary = "Get optional attributes", description = "Get all optional attributes")
    public ResponseEntity<ApiResponse<List<ProductAttributeResponse>>> getOptionalAttributes(
            @Parameter(description = "Product ID", required = true)
            @PathVariable UUID productId) {
        
        log.info("Getting optional attributes for product: {}", productId);
        
        List<ProductAttributeResponse> attributes = attributeQueryService.getOptionalAttributes(productId);
        
        return success(attributes);
    }

    @GetMapping("/missing")
    @Operation(summary = "Get missing required attributes", description = "Get required attributes that are not filled")
    public ResponseEntity<ApiResponse<List<ProductAttributeResponse>>> getMissingRequiredAttributes(
            @Parameter(description = "Product ID", required = true)
            @PathVariable UUID productId) {
        
        log.info("Getting missing required attributes for product: {}", productId);
        
        List<ProductAttributeResponse> attributes = attributeQueryService.getMissingRequiredAttributes(productId);
        
        return success(attributes);
    }

    @GetMapping("/map")
    @Operation(summary = "Get attributes as map", description = "Get all attributes as key-value map")
    public ResponseEntity<ApiResponse<Map<String, String>>> getAttributesAsMap(
            @Parameter(description = "Product ID", required = true)
            @PathVariable UUID productId) {
        
        log.info("Getting attributes as map for product: {}", productId);
        
        Map<String, String> attributes = attributeQueryService.getAttributesAsMap(productId);
        
        return success(attributes);
    }

    @GetMapping("/keys")
    @Operation(summary = "Get attribute keys", description = "Get all attribute keys for product")
    public ResponseEntity<ApiResponse<Set<String>>> getAttributeKeys(
            @Parameter(description = "Product ID", required = true)
            @PathVariable UUID productId) {
        
        log.info("Getting attribute keys for product: {}", productId);
        
        Set<String> keys = attributeQueryService.getAttributeKeysForProduct(productId);
        
        return success(keys);
    }

    @GetMapping("/statistics")
    @Operation(summary = "Get attribute statistics", description = "Get attribute statistics for product")
    public ResponseEntity<ApiResponse<ProductAttributeQueryService.AttributeStatistics>> getAttributeStatistics(
            @Parameter(description = "Product ID", required = true)
            @PathVariable UUID productId) {
        
        log.info("Getting attribute statistics for product: {}", productId);
        
        ProductAttributeQueryService.AttributeStatistics statistics = 
            attributeQueryService.getAttributeStatistics(productId);
        
        return success(statistics);
    }
}