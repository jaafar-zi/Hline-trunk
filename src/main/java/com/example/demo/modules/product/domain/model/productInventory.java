package com.example.demo.modules.product.domain.model;

import com.example.demo.common.base.entity.AuditableAggregateRoot;
import com.example.demo.common.event.EntityUpdatedEvent;
import com.example.demo.common.util.ChangeTracker;
import com.example.demo.modules.product.domain.event.LowStockEvent;          
import com.example.demo.modules.product.domain.event.OutOfStockEvent;       
import com.example.demo.modules.product.domain.event.StockLevelChangedEvent; 
import com.example.demo.modules.product.domain.model.vo.StockLevel;
import com.example.demo.modules.product.domain.model.vo.StockStatus;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;                                                

/**
 * Product Inventory Entity
 * 
 * Manages stock levels and inventory tracking for products
 * 
 * Business Rules:
 * - Stock quantity cannot be negative
 * - Reserved quantity cannot exceed available quantity
 * - Low stock threshold must be positive
 * - Backorder allowed only if explicitly enabled
 * 
 * Events Published:
 * - EntityUpdatedEvent<ProductInventory> (for all inventory changes)
 * - StockLevelChangedEvent (business-critical)
 * - LowStockEvent (alert)
 * - OutOfStockEvent (alert)
 * 
 * @author PIM Team
 * @version 1.0
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
    name = "product_inventory",
    indexes = {
        @Index(name = "idx_inventory_product", columnList = "product_id"),
        @Index(name = "idx_inventory_sku", columnList = "sku"),
        @Index(name = "idx_inventory_stock_status", columnList = "stock_status")
    }
)
public class ProductInventory extends AuditableAggregateRoot {

    // ====================================
    // Relationships
    // ====================================

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false, unique = true)
    private Product product;

    @Column(name = "sku", nullable = false, length = 50)
    private String sku;

    // ====================================
    // Stock Levels
    // ====================================

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "quantity", column = @Column(name = "quantity_on_hand")),
        @AttributeOverride(name = "reserved", column = @Column(name = "quantity_reserved")),
        @AttributeOverride(name = "available", column = @Column(name = "quantity_available"))
    })
    @Builder.Default
    private StockLevel stockLevel = StockLevel.zero();

    @Column(name = "low_stock_threshold")
    @Builder.Default
    private Integer lowStockThreshold = 10;

    @Column(name = "reorder_point")
    private Integer reorderPoint;

    @Column(name = "reorder_quantity")
    private Integer reorderQuantity;

    // ====================================
    // Inventory Settings
    // ====================================

    @Enumerated(EnumType.STRING)
    @Column(name = "stock_status", nullable = false, length = 50)
    @Builder.Default
    private StockStatus stockStatus = StockStatus.OUT_OF_STOCK;

    @Column(name = "track_inventory", nullable = false)
    @Builder.Default
    private Boolean trackInventory = true;

    @Column(name = "allow_backorder", nullable = false)
    @Builder.Default
    private Boolean allowBackorder = false;

    @Column(name = "backorder_limit")
    private Integer backorderLimit;

    // ====================================
    // Location & Tracking
    // ====================================

    @Column(name = "warehouse_location", length = 100)
    private String warehouseLocation;

    @Column(name = "bin_location", length = 50)
    private String binLocation;

    @Column(name = "supplier_sku", length = 100)
    private String supplierSku;

    // ====================================
    // Timestamps
    // ====================================

    @Column(name = "last_stock_check")
    private LocalDateTime lastStockCheck;

    @Column(name = "last_restock_date")
    private LocalDateTime lastRestockDate;

    @Column(name = "next_restock_date")
    private LocalDateTime nextRestockDate;

    // ====================================
    // Factory Methods
    // ====================================

    /**
     * Create inventory for product
     */
    public static ProductInventory createFor(Product product, int initialQuantity) {
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null");
        }

        ProductInventory inventory = ProductInventory.builder()
            .product(product)
            .sku(product.getSku().getValue())
            .stockLevel(StockLevel.of(initialQuantity))
            .trackInventory(true)
            .allowBackorder(false)
            .build();

        inventory.updateStockStatus();
        return inventory;
    }

    /**
     * Create inventory without tracking
     */
    public static ProductInventory createUntracked(Product product) {
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null");
        }

        return ProductInventory.builder()
            .product(product)
            .sku(product.getSku().getValue())
            .stockLevel(StockLevel.unlimited())
            .trackInventory(false)
            .stockStatus(StockStatus.IN_STOCK)
            .build();
    }

    // ====================================
    // Domain Logic - Stock Management
    // ====================================

    /**
     * Set quantity (replaces existing)
     *
     * Events Published:
     * - StockLevelChangedEvent
     */
    public void setQuantity(int newQuantity) {
        if (!trackInventory) {
            throw new IllegalStateException("Cannot set quantity for untracked inventory");
        }

        int oldQuantity = stockLevel.getQuantity();
        int oldAvailable = stockLevel.getAvailable();

        stockLevel = stockLevel.setQuantity(newQuantity);
        updateStockStatus();

        // Register stock level changed event
        registerEvent(StockLevelChangedEvent.builder()
            .productId(product.getId())
            .sku(sku)
            .previousQuantity(oldQuantity)
            .newQuantity(newQuantity)
            .previousAvailable(oldAvailable)
            .newAvailable(stockLevel.getAvailable())
            .changeType(newQuantity > oldQuantity ? "INCREASE" : "DECREASE")
            .changedBy(getCurrentUsername())
            .occurredAt(LocalDateTime.now())
            .build());

        // Check for low stock or out of stock
        checkStockAlerts();
    }

    /**
     * Add stock (incremental)
     */
    public void addStock(int quantity, String reason) {
        if (!trackInventory) {
            throw new IllegalStateException("Cannot add stock to untracked inventory");
        }

        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity to add must be positive");
        }

        int oldQuantity = stockLevel.getQuantity();
        stockLevel = stockLevel.add(quantity);
        updateStockStatus();

        this.lastRestockDate = LocalDateTime.now();

        ChangeTracker tracker = new ChangeTracker();
        tracker.track("quantity", oldQuantity, stockLevel.getQuantity());
        tracker.track("reason", null, "Stock added: " + reason);

        registerEvent(new EntityUpdatedEvent<>(this, tracker.getChanges()));

        registerEvent(StockLevelChangedEvent.builder()
            .productId(product.getId())
            .sku(sku)
            .previousQuantity(oldQuantity)
            .newQuantity(stockLevel.getQuantity())
            .quantityChange(quantity)
            .changeType("RESTOCK")
            .reason(reason)
            .changedBy(getCurrentUsername())
            .occurredAt(LocalDateTime.now())
            .build());
    }

    /**
     * Remove stock (incremental)
     */
    public void removeStock(int quantity, String reason) {
        if (!trackInventory) {
            throw new IllegalStateException("Cannot remove stock from untracked inventory");
        }

        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity to remove must be positive");
        }

        int oldQuantity = stockLevel.getQuantity();
        stockLevel = stockLevel.subtract(quantity);
        updateStockStatus();

        ChangeTracker tracker = new ChangeTracker();
        tracker.track("quantity", oldQuantity, stockLevel.getQuantity());
        tracker.track("reason", null, "Stock removed: " + reason);

        registerEvent(new EntityUpdatedEvent<>(this, tracker.getChanges()));

        registerEvent(StockLevelChangedEvent.builder()
            .productId(product.getId())
            .sku(sku)
            .previousQuantity(oldQuantity)
            .newQuantity(stockLevel.getQuantity())
            .quantityChange(-quantity)
            .changeType("REMOVAL")
            .reason(reason)
            .changedBy(getCurrentUsername())
            .occurredAt(LocalDateTime.now())
            .build());

        checkStockAlerts();
    }

    /**
     * Reserve stock for order
     */
    public void reserve(int quantity) {
        if (!trackInventory) {
            throw new IllegalStateException("Cannot reserve untracked inventory");
        }

        if (quantity <= 0) {
            throw new IllegalArgumentException("Reservation quantity must be positive");
        }

        int oldReserved = stockLevel.getReserved();
        stockLevel = stockLevel.reserve(quantity);
        updateStockStatus();

        ChangeTracker tracker = new ChangeTracker();
        tracker.track("reserved", oldReserved, stockLevel.getReserved());

        registerEvent(new EntityUpdatedEvent<>(this, tracker.getChanges()));
    }

    /**
     * Release reserved stock
     */
    public void releaseReservation(int quantity) {
        if (!trackInventory) {
            return;
        }

        if (quantity <= 0) {
            throw new IllegalArgumentException("Release quantity must be positive");
        }

        int oldReserved = stockLevel.getReserved();
        stockLevel = stockLevel.release(quantity);
        updateStockStatus();

        ChangeTracker tracker = new ChangeTracker();
        tracker.track("reserved", oldReserved, stockLevel.getReserved());

        registerEvent(new EntityUpdatedEvent<>(this, tracker.getChanges()));
    }

    /**
     * Fulfill reservation (convert reserved to sold)
     */
    public void fulfillReservation(int quantity) {
        if (!trackInventory) {
            return;
        }

        if (quantity <= 0) {
            throw new IllegalArgumentException("Fulfillment quantity must be positive");
        }

        stockLevel = stockLevel.fulfill(quantity);
        updateStockStatus();

        checkStockAlerts();
    }

    // ====================================
    // Domain Logic - Stock Status
    // ====================================

    /**
     * Update stock status based on current levels
     */
    private void updateStockStatus() {
        if (!trackInventory) {
            this.stockStatus = StockStatus.IN_STOCK;
            return;
        }

        StockStatus oldStatus = this.stockStatus;
        StockStatus newStatus;

        int available = stockLevel.getAvailable();

        if (available <= 0) {
            newStatus = allowBackorder ? StockStatus.BACKORDER : StockStatus.OUT_OF_STOCK;
        } else if (lowStockThreshold != null && available <= lowStockThreshold) {
            newStatus = StockStatus.LOW_STOCK;
        } else {
            newStatus = StockStatus.IN_STOCK;
        }

        if (oldStatus != newStatus) {
            this.stockStatus = newStatus;

            ChangeTracker tracker = new ChangeTracker();
            tracker.track("stockStatus", oldStatus, newStatus);
            registerEvent(new EntityUpdatedEvent<>(this, tracker.getChanges()));
        }
    }

    /**
     * Check and raise stock alerts
     */
    private void checkStockAlerts() {
        int available = stockLevel.getAvailable();

        // Out of stock alert
        if (available <= 0 && !allowBackorder) {
            registerEvent(OutOfStockEvent.builder()
                .productId(product.getId())
                .sku(sku)
                .productName(product.getName())
                .occurredAt(LocalDateTime.now())
                .build());
        }

        // Low stock alert
        if (lowStockThreshold != null && 
            available > 0 && 
            available <= lowStockThreshold) {
            registerEvent(LowStockEvent.builder()
                .productId(product.getId())
                .sku(sku)
                .productName(product.getName())
                .currentQuantity(available)
                .threshold(lowStockThreshold)
                .occurredAt(LocalDateTime.now())
                .build());
        }
    }

    // ====================================
    // Domain Logic - Settings
    // ====================================

    /**
     * Enable/disable inventory tracking
     */
    public void setTrackInventory(boolean track) {
        if (this.trackInventory == track) {
            return;
        }

        ChangeTracker tracker = new ChangeTracker();
        tracker.track("trackInventory", this.trackInventory, track);

        this.trackInventory = track;

        if (!track) {
            this.stockLevel = StockLevel.unlimited();
            this.stockStatus = StockStatus.IN_STOCK;
        } else {
            this.stockLevel = StockLevel.zero();
            updateStockStatus();
        }

        registerEvent(new EntityUpdatedEvent<>(this, tracker.getChanges()));
    }

    /**
     * Enable/disable backorders
     */
    public void setAllowBackorder(boolean allow, Integer limit) {
        ChangeTracker tracker = new ChangeTracker();
        tracker.track("allowBackorder", this.allowBackorder, allow);
        tracker.track("backorderLimit", this.backorderLimit, limit);

        this.allowBackorder = allow;
        this.backorderLimit = limit;
        updateStockStatus();

        registerEvent(new EntityUpdatedEvent<>(this, tracker.getChanges()));
    }

    /**
     * Update low stock threshold
     */
    public void setLowStockThreshold(Integer threshold) {
        if (threshold != null && threshold < 0) {
            throw new IllegalArgumentException("Low stock threshold cannot be negative");
        }

        ChangeTracker tracker = new ChangeTracker();
        tracker.track("lowStockThreshold", this.lowStockThreshold, threshold);

        this.lowStockThreshold = threshold;
        updateStockStatus();

        registerEvent(new EntityUpdatedEvent<>(this, tracker.getChanges()));
    }

    /**
     * Set reorder point and quantity
     */
    public void setReorderSettings(Integer reorderPoint, Integer reorderQuantity) {
        if (reorderPoint != null && reorderPoint < 0) {
            throw new IllegalArgumentException("Reorder point cannot be negative");
        }
        if (reorderQuantity != null && reorderQuantity <= 0) {
            throw new IllegalArgumentException("Reorder quantity must be positive");
        }

        ChangeTracker tracker = new ChangeTracker();
        tracker.track("reorderPoint", this.reorderPoint, reorderPoint);
        tracker.track("reorderQuantity", this.reorderQuantity, reorderQuantity);

        this.reorderPoint = reorderPoint;
        this.reorderQuantity = reorderQuantity;

        registerEvent(new EntityUpdatedEvent<>(this, tracker.getChanges()));
    }

    // ====================================
    // Query Methods
    // ====================================

    public boolean isInStock() {
        if (!trackInventory) {
            return true;
        }
        return stockLevel.getAvailable() > 0 || allowBackorder;
    }

    public boolean isLowStock() {
        if (!trackInventory) {
            return false;
        }
        return stockStatus == StockStatus.LOW_STOCK;
    }

    public boolean isOutOfStock() {
        if (!trackInventory) {
            return false;
        }
        return stockLevel.getAvailable() <= 0;
    }

    public boolean needsReorder() {
        if (!trackInventory || reorderPoint == null) {
            return false;
        }
        return stockLevel.getAvailable() <= reorderPoint;
    }

    public int getAvailableQuantity() {
        if (!trackInventory) {
            return Integer.MAX_VALUE;
        }
        return stockLevel.getAvailable();
    }

    public boolean canFulfill(int requestedQuantity) {
        if (!trackInventory) {
            return true;
        }

        int available = stockLevel.getAvailable();

        if (available >= requestedQuantity) {
            return true;
        }

        if (allowBackorder) {
            if (backorderLimit == null) {
                return true;
            }
            return requestedQuantity <= backorderLimit;
        }

        return false;
    }

    // ====================================
    // Helper Methods
    // ====================================

    private String getCurrentUsername() {
        try {
            org.springframework.security.core.Authentication authentication = 
                org.springframework.security.core.context.SecurityContextHolder
                    .getContext()
                    .getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                return authentication.getName();
            }
        } catch (Exception e) {
            // Ignore
        }
        return "system";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProductInventory)) return false;
        ProductInventory that = (ProductInventory) o;
        return getId() != null && getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return String.format(
            "ProductInventory[id=%s, sku=%s, quantity=%d, available=%d, status=%s]",
            getId(), sku, 
            stockLevel.getQuantity(), 
            stockLevel.getAvailable(), 
            stockStatus
        );
    }
}