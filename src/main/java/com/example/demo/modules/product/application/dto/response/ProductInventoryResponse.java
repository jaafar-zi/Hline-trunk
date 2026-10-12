package com.example.demo.modules.product.application.dto.response;

import com.example.demo.common.base.dto.BaseDTO;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Lean Product Inventory Response - Performance optimized
 *
 * @author PIM Team
 * @version 1.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Product inventory response - optimized")
public class ProductInventoryResponse extends BaseDTO {

    private static final long serialVersionUID = 1L;

    // ====================================
    // Reference
    // ====================================

    @Schema(description = "Product ID reference")
    private UUID productId;

    @Schema(description = "Variant ID reference (if variant-specific)")
    private UUID variantId;

    // ====================================
    // Stock Levels
    // ====================================

    @Schema(description = "Total quantity", example = "100")
    private Integer quantity;

    @Schema(description = "Available quantity", example = "85")
    private Integer available;

    @Schema(description = "Reserved quantity", example = "10")
    private Integer reserved;

    @Schema(description = "Committed quantity", example = "5")
    private Integer committed;

    // ====================================
    // Status
    // ====================================

    @Schema(description = "Stock status", example = "IN_STOCK")
    private String stockStatus;

    @Schema(description = "Track inventory", example = "true")
    private Boolean trackInventory;

    @Schema(description = "Allow backorder", example = "false")
    private Boolean allowBackorder;

    // ====================================
    // Thresholds
    // ====================================

    @Schema(description = "Low stock threshold", example = "10")
    private Integer lowStockThreshold;

    @Schema(description = "Reorder point", example = "20")
    private Integer reorderPoint;

    // ====================================
    // Location
    // ====================================

    @Schema(description = "Warehouse location", example = "WH-A")
    private String warehouseLocation;

    // ====================================
    // Dates
    // ====================================

    @Schema(description = "Last restock date")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime lastRestockDate;

    @Schema(description = "Next expected restock date")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime nextRestockDate;

    // ====================================
    // Helper Methods
    // ====================================

    public boolean isInStock() {
        return "IN_STOCK".equals(stockStatus) 
            && available != null && available > 0;
    }

    public boolean isLowStock() {
        return "LOW_STOCK".equals(stockStatus) 
            || (available != null && lowStockThreshold != null 
                && available <= lowStockThreshold);
    }

    public boolean needsReorder() {
        return available != null && reorderPoint != null 
            && available <= reorderPoint;
    }

    public boolean isAvailable() {
        return isInStock() 
            || (Boolean.TRUE.equals(allowBackorder) && "BACKORDER".equals(stockStatus));
    }

    @Override
    public String toString() {
        return String.format("ProductInventoryResponse[id=%s, status=%s, available=%d]", 
            id, stockStatus, available);
    }
}