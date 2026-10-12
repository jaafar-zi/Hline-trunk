package com.example.demo.modules.product.interfaces;

import com.example.demo.modules.product.application.command.ProductInventoryCommandService;
import com.example.demo.modules.product.application.dto.filter.ProductInventoryFilter;
import com.example.demo.modules.product.application.dto.request.BulkUpdateInventoryRequest;
import com.example.demo.modules.product.application.dto.request.UpdateInventoryRequest;
import com.example.demo.modules.product.application.dto.response.ProductInventoryResponse;
import com.example.demo.modules.product.application.query.ProductInventoryQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Product Inventory REST Controller
 *
 * Handles all HTTP requests for product inventory operations
 * Implements CQRS pattern with separate command and query services
 *
 * Product‑scoped and global inventory operations are exposed
 * through the same controller for convenience.
 *
 * Errors and status mapping are handled by the global exception handler.
 *
 * @author PIM Team
 * @version 1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/products/{productId}/inventory")
@RequiredArgsConstructor
@Validated
@Tag(name = "Product Inventory", description = "Product inventory management endpoints")
public class ProductInventoryController {

    private final ProductInventoryCommandService commandService;
    private final ProductInventoryQueryService queryService;

    // ====================================
    // Product‑Scoped Command Operations
    // ====================================

    @PutMapping
    @Operation(
        summary = "Update product inventory",
        description = "Updates inventory settings and stock level for a specific product"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Inventory updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ProductInventoryResponse updateInventory(
            @Parameter(description = "Product ID")
            @PathVariable UUID productId,
            @Valid @RequestBody UpdateInventoryRequest request) {

        log.info("REST: Updating inventory for product: {}", productId);

        var inventory = commandService.updateInventory(productId, request);
        return queryService.getInventoryById(inventory.getId());
    }

    @PatchMapping("/adjust")
    @Operation(
        summary = "Adjust stock level",
        description = "Adjusts stock by a relative amount (positive or negative)"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Stock adjusted successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid adjustment or would result in negative stock"),
        @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ProductInventoryResponse adjustStock(
            @Parameter(description = "Product ID")
            @PathVariable UUID productId,
            @Parameter(description = "Adjustment amount (positive to add, negative to subtract)")
            @RequestParam int adjustment,
            @Parameter(description = "Reason for adjustment")
            @RequestParam(required = false) String reason) {

        log.info("REST: Adjusting stock for product: {} by {}", productId, adjustment);

        var inventory = commandService.adjustStock(productId, adjustment, reason);
        return queryService.getInventoryById(inventory.getId());
    }

    @PostMapping("/reserve")
    @Operation(
        summary = "Reserve stock",
        description = "Reserves a specified quantity of stock (e.g., for pending orders)"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Stock reserved successfully"),
        @ApiResponse(responseCode = "400", description = "Insufficient stock available"),
        @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public void reserveStock(
            @Parameter(description = "Product ID")
            @PathVariable UUID productId,
            @Parameter(description = "Quantity to reserve")
            @RequestParam @Min(1) int quantity) {

        log.info("REST: Reserving {} units for product: {}", quantity, productId);
        commandService.reserveStock(productId, quantity);
    }

    @PostMapping("/release")
    @Operation(
        summary = "Release reserved stock",
        description = "Releases previously reserved stock back to available inventory"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Stock released successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid quantity or insufficient reserved stock"),
        @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public void releaseStock(
            @Parameter(description = "Product ID")
            @PathVariable UUID productId,
            @Parameter(description = "Quantity to release")
            @RequestParam @Min(1) int quantity) {

        log.info("REST: Releasing {} reserved units for product: {}", quantity, productId);
        commandService.releaseStock(productId, quantity);
    }

    // ====================================
    // Product‑Scoped Query Operations
    // ====================================

    @GetMapping
    @Operation(
        summary = "Get inventory for product",
        description = "Returns inventory information for a specific product"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Inventory found"),
        @ApiResponse(responseCode = "404", description = "Product or inventory not found")
    })
    public ProductInventoryResponse getInventory(
            @Parameter(description = "Product ID")
            @PathVariable UUID productId) {

        log.info("REST: Getting inventory for product: {}", productId);
        return queryService.getInventoryByProductId(productId);
    }

    // ====================================
    // Global / Bulk Operations (same controller)
    // ====================================

    @PostMapping("/bulk-update")
    @Operation(
        summary = "Bulk update inventories",
        description = "Updates inventory for multiple products at once"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Inventories updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    public List<ProductInventoryResponse> bulkUpdateInventory(
            @Valid @RequestBody BulkUpdateInventoryRequest request) {

        // Note: productId path variable is not used here; kept on same controller per requirement
        log.info("REST: Bulk updating inventory for {} products", request.getUpdates().size());

        var inventories = commandService.bulkUpdateInventory(request);
        return inventories.stream()
                .map(inv -> queryService.getInventoryById(inv.getId()))
                .toList();
    }

    @GetMapping("/search")
    @Operation(
        summary = "Search inventories",
        description = "Search and filter product inventories with pagination"
    )
    @ApiResponse(responseCode = "200", description = "Inventories retrieved successfully")
    public Page<ProductInventoryResponse> searchInventories(
            @Parameter(description = "Inventory filter criteria")
            @ModelAttribute ProductInventoryFilter filter) {

        // productId from path is not used here; query is global
        log.info("REST: Searching inventories with filter");
        return queryService.searchInventories(filter);
    }

    @GetMapping("/all")
    @Operation(
        summary = "Get all inventories",
        description = "Get all inventories matching filter criteria (without pagination)"
    )
    @ApiResponse(responseCode = "200", description = "Inventories retrieved successfully")
    public List<ProductInventoryResponse> getAllInventories(
            @Parameter(description = "Inventory filter criteria")
            @ModelAttribute ProductInventoryFilter filter) {

        log.info("REST: Getting all inventories with filter");
        return queryService.findAllInventories(filter);
    }

    @GetMapping("/{inventoryId}")
    @Operation(
        summary = "Get inventory by ID",
        description = "Returns a specific inventory by its ID"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Inventory found"),
        @ApiResponse(responseCode = "404", description = "Inventory not found")
    })
    public ProductInventoryResponse getInventoryById(
            @Parameter(description = "Inventory ID")
            @PathVariable UUID inventoryId,
            @PathVariable UUID productId) { // productId present only to satisfy base mapping

        log.info("REST: Getting inventory by ID: {}", inventoryId);
        return queryService.getInventoryById(inventoryId);
    }

    @GetMapping("/low-stock")
    @Operation(
        summary = "Get low stock inventories",
        description = "Returns all inventories where stock level is below the low stock threshold"
    )
    @ApiResponse(responseCode = "200", description = "Low stock inventories retrieved successfully")
    public List<ProductInventoryResponse> getLowStockInventories(
            @PathVariable UUID productId) {

        log.info("REST: Getting low stock inventories");
        return queryService.getLowStockInventories();
    }

    @GetMapping("/out-of-stock")
    @Operation(
        summary = "Get out of stock inventories",
        description = "Returns all inventories with zero available stock"
    )
    @ApiResponse(responseCode = "200", description = "Out of stock inventories retrieved successfully")
    public List<ProductInventoryResponse> getOutOfStockInventories(
            @PathVariable UUID productId) {

        log.info("REST: Getting out of stock inventories");
        return queryService.getOutOfStockInventories();
    }

    @GetMapping("/needs-reorder")
    @Operation(
        summary = "Get inventories needing reorder",
        description = "Returns all inventories that have reached their reorder point"
    )
    @ApiResponse(responseCode = "200", description = "Inventories needing reorder retrieved successfully")
    public List<ProductInventoryResponse> getInventoriesNeedingReorder(
            @PathVariable UUID productId) {

        log.info("REST: Getting inventories needing reorder");
        return queryService.getInventoriesNeedingReorder();
    }

    @GetMapping("/stock-status/{stockStatus}")
    @Operation(
        summary = "Get inventories by stock status",
        description = "Returns inventories filtered by stock status (IN_STOCK, LOW_STOCK, OUT_OF_STOCK, etc.)"
    )
    @ApiResponse(responseCode = "200", description = "Inventories retrieved successfully")
    public List<ProductInventoryResponse> getInventoriesByStockStatus(
            @Parameter(description = "Stock status")
            @PathVariable String stockStatus,
            @PathVariable UUID productId) {

        log.info("REST: Getting inventories with stock status: {}", stockStatus);
        return queryService.getInventoriesByStockStatus(stockStatus);
    }

    @GetMapping("/warehouse/{warehouseLocation}")
    @Operation(
        summary = "Get inventories by warehouse",
        description = "Returns all inventories for a specific warehouse location"
    )
    @ApiResponse(responseCode = "200", description = "Inventories retrieved successfully")
    public List<ProductInventoryResponse> getInventoriesByWarehouse(
            @Parameter(description = "Warehouse location")
            @PathVariable String warehouseLocation,
            @PathVariable UUID productId) {

        log.info("REST: Getting inventories for warehouse: {}", warehouseLocation);
        return queryService.getInventoriesByWarehouse(warehouseLocation);
    }

    @GetMapping("/statistics")
    @Operation(
        summary = "Get inventory statistics",
        description = "Returns overall inventory statistics including stock levels and alerts"
    )
    @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully")
    public ProductInventoryQueryService.InventoryStatistics getInventoryStatistics(
            @PathVariable UUID productId) {

        log.info("REST: Getting inventory statistics");
        return queryService.getStatistics();
    }
}