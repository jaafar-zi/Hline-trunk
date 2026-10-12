package com.example.demo.modules.product.interfaces;

import com.example.demo.modules.product.application.command.ProductPriceCommandService;
import com.example.demo.modules.product.application.dto.filter.ProductPriceFilter;
import com.example.demo.modules.product.application.dto.request.AddProductPriceRequest;
import com.example.demo.modules.product.application.dto.request.UpdatePriceRequest;
import com.example.demo.modules.product.application.dto.response.ProductPriceResponse;
import com.example.demo.modules.product.application.query.ProductPriceQueryService;
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
import java.util.UUID;

/**
 * Product Price REST Controller
 * 
 * Handles all HTTP requests for product price operations
 * Implements CQRS pattern with separate command and query services
 * 
 * @author PIM Team
 * @version 1.0
 */
@Slf4j
@RestController
@RequestMapping("/products/{productId}/prices")
@RequiredArgsConstructor
@Validated
@Tag(name = "Product Prices", description = "Product price management endpoints")
public class ProductPriceController {

    private final ProductPriceCommandService commandService;
    private final ProductPriceQueryService queryService;

    // ====================================
    // Command Operations (Writes)
    // ====================================

    @PostMapping
    @Operation(
        summary = "Add price to product",
        description = "Adds a new price entry to the specified product"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Price added successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<ProductPriceResponse> addPrice(
            @Parameter(description = "Product ID")
            @PathVariable UUID productId,
            @Valid @RequestBody AddProductPriceRequest request) {
        
        log.info("REST: Adding price to product: {}", productId);
        
        var price = commandService.addPrice(productId, request);
        var response = queryService.getPriceById(price.getId());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{priceId}")
    @Operation(
        summary = "Update price",
        description = "Updates an existing price entry"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Price updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "404", description = "Price not found")
    })
    public ResponseEntity<ProductPriceResponse> updatePrice(
            @Parameter(description = "Product ID")
            @PathVariable UUID productId,
            @Parameter(description = "Price ID")
            @PathVariable UUID priceId,
            @Valid @RequestBody UpdatePriceRequest request) {
        
        log.info("REST: Updating price: {} for product: {}", priceId, productId);
        
        var price = commandService.updatePrice(priceId, request);
        var response = queryService.getPriceById(price.getId());
        
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{priceId}")
    @Operation(
        summary = "Delete price",
        description = "Soft deletes a price entry"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Price deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Price not found")
    })
    public ResponseEntity<Void> deletePrice(
            @Parameter(description = "Product ID")
            @PathVariable UUID productId,
            @Parameter(description = "Price ID")
            @PathVariable UUID priceId) {
        
        log.info("REST: Deleting price: {} from product: {}", priceId, productId);
        
        commandService.delete(priceId);
        
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{priceId}/activate")
    @Operation(
        summary = "Activate price",
        description = "Activates a price entry, making it available for use"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Price activated successfully"),
        @ApiResponse(responseCode = "400", description = "Price cannot be activated (expired or deleted)"),
        @ApiResponse(responseCode = "404", description = "Price not found")
    })
    public ResponseEntity<ProductPriceResponse> activatePrice(
            @Parameter(description = "Product ID")
            @PathVariable UUID productId,
            @Parameter(description = "Price ID")
            @PathVariable UUID priceId) {
        
        log.info("REST: Activating price: {} for product: {}", priceId, productId);
        
        var price = commandService.activatePrice(priceId);
        var response = queryService.getPriceById(price.getId());
        
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{priceId}/deactivate")
    @Operation(
        summary = "Deactivate price",
        description = "Deactivates a price entry, making it unavailable for use"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Price deactivated successfully"),
        @ApiResponse(responseCode = "404", description = "Price not found")
    })
    public ResponseEntity<ProductPriceResponse> deactivatePrice(
            @Parameter(description = "Product ID")
            @PathVariable UUID productId,
            @Parameter(description = "Price ID")
            @PathVariable UUID priceId) {
        
        log.info("REST: Deactivating price: {} for product: {}", priceId, productId);
        
        var price = commandService.deactivatePrice(priceId);
        var response = queryService.getPriceById(price.getId());
        
        return ResponseEntity.ok(response);
    }

    // ====================================
    // Query Operations (Reads)
    // ====================================

    @GetMapping
    @Operation(
        summary = "Get all prices for product",
        description = "Returns all price entries for the specified product"
    )
    @ApiResponse(responseCode = "200", description = "Prices retrieved successfully")
    public ResponseEntity<List<ProductPriceResponse>> getPricesForProduct(
            @Parameter(description = "Product ID")
            @PathVariable UUID productId) {
        
        log.info("REST: Getting prices for product: {}", productId);
        
        return ResponseEntity.ok(queryService.getPricesForProduct(productId));
    }

    @GetMapping("/{priceId}")
    @Operation(
        summary = "Get price by ID",
        description = "Returns a specific price entry by its ID"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Price found"),
        @ApiResponse(responseCode = "404", description = "Price not found")
    })
    public ResponseEntity<ProductPriceResponse> getPriceById(
            @Parameter(description = "Product ID")
            @PathVariable UUID productId,
            @Parameter(description = "Price ID")
            @PathVariable UUID priceId) {
        
        log.info("REST: Getting price: {} for product: {}", priceId, productId);
        
        return ResponseEntity.ok(queryService.getPriceById(priceId));
    }

    @GetMapping("/active")
    @Operation(
        summary = "Get active prices",
        description = "Returns all active price entries for the product"
    )
    @ApiResponse(responseCode = "200", description = "Active prices retrieved successfully")
    public ResponseEntity<List<ProductPriceResponse>> getActivePrices(
            @Parameter(description = "Product ID")
            @PathVariable UUID productId) {
        
        log.info("REST: Getting active prices for product: {}", productId);
        
        return ResponseEntity.ok(queryService.getActivePricesForProduct(productId));
    }

    @GetMapping("/current")
    @Operation(
        summary = "Get current price",
        description = "Returns the currently valid price for the product by type"
    )
    @ApiResponse(responseCode = "200", description = "Current price retrieved successfully")
    public ResponseEntity<ProductPriceResponse> getCurrentPrice(
            @Parameter(description = "Product ID")
            @PathVariable UUID productId,
            @Parameter(description = "Price type (e.g., REGULAR, PROMOTIONAL)")
            @RequestParam(defaultValue = "REGULAR") String priceType) {
        
        log.info("REST: Getting current {} price for product: {}", priceType, productId);
        
        return ResponseEntity.ok(queryService.getCurrentPriceForProduct(productId, priceType));
    }

    @GetMapping("/promotional")
    @Operation(
        summary = "Get promotional prices",
        description = "Returns all currently valid promotional prices for the product"
    )
    @ApiResponse(responseCode = "200", description = "Promotional prices retrieved successfully")
    public ResponseEntity<List<ProductPriceResponse>> getPromotionalPrices(
            @Parameter(description = "Product ID")
            @PathVariable UUID productId) {
        
        log.info("REST: Getting promotional prices for product: {}", productId);
        
        return ResponseEntity.ok(queryService.getPromotionalPrices(productId));
    }

    @GetMapping("/currency/{currency}")
    @Operation(
        summary = "Get prices by currency",
        description = "Returns all prices in the specified currency for the product"
    )
    @ApiResponse(responseCode = "200", description = "Prices retrieved successfully")
    public ResponseEntity<List<ProductPriceResponse>> getPricesByCurrency(
            @Parameter(description = "Product ID")
            @PathVariable UUID productId,
            @Parameter(description = "Currency code (e.g., USD, EUR)")
            @PathVariable String currency) {
        
        log.info("REST: Getting {} prices for product: {}", currency, productId);
        
        return ResponseEntity.ok(queryService.getPricesByCurrency(productId, currency));
    }

    @GetMapping("/scheduled")
    @Operation(
        summary = "Get scheduled prices",
        description = "Returns all future/scheduled prices for the product"
    )
    @ApiResponse(responseCode = "200", description = "Scheduled prices retrieved successfully")
    public ResponseEntity<List<ProductPriceResponse>> getScheduledPrices(
            @Parameter(description = "Product ID")
            @PathVariable UUID productId) {
        
        log.info("REST: Getting scheduled prices for product: {}", productId);
        
        return ResponseEntity.ok(queryService.getScheduledPrices(productId));
    }

    @GetMapping("/expired")
    @Operation(
        summary = "Get expired prices",
        description = "Returns all expired prices for the product"
    )
    @ApiResponse(responseCode = "200", description = "Expired prices retrieved successfully")
    public ResponseEntity<List<ProductPriceResponse>> getExpiredPrices(
            @Parameter(description = "Product ID")
            @PathVariable UUID productId) {
        
        log.info("REST: Getting expired prices for product: {}", productId);
        
        return ResponseEntity.ok(queryService.getExpiredPrices(productId));
    }

    @GetMapping("/search")
    @Operation(
        summary = "Search prices",
        description = "Search and filter prices with pagination"
    )
    @ApiResponse(responseCode = "200", description = "Prices retrieved successfully")
    public ResponseEntity<Page<ProductPriceResponse>> searchPrices(
            @Parameter(description = "Product ID")
            @PathVariable UUID productId,
            @Parameter(description = "Price filter criteria")
            @ModelAttribute ProductPriceFilter filter) {
        
        log.info("REST: Searching prices for product: {}", productId);
        
        // Ensure filter is scoped to the product
        filter.setProductId(productId);
        
        return ResponseEntity.ok(queryService.searchPrices(filter));
    }

    @GetMapping("/count")
    @Operation(
        summary = "Count prices",
        description = "Returns the total number of prices for the product"
    )
    @ApiResponse(responseCode = "200", description = "Count retrieved successfully")
    public ResponseEntity<Long> countPrices(
            @Parameter(description = "Product ID")
            @PathVariable UUID productId) {
        
        log.info("REST: Counting prices for product: {}", productId);
        
        return ResponseEntity.ok(queryService.countPricesForProduct(productId));
    }
}