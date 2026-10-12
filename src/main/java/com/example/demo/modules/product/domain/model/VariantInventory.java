package com.example.demo.modules.product.domain.model;

import com.example.demo.common.base.entity.AuditableAggregateRoot;
import com.example.demo.modules.product.domain.model.vo.StockLevel;
import com.example.demo.modules.product.domain.model.vo.StockStatus;
import jakarta.persistence.*;
import lombok.*;

/**
 * Variant Inventory Entity
 * 
 * Manages inventory for product variants
 * Simpler than ProductInventory as variants typically don't need all features
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "variant_inventory")
public class VariantInventory extends AuditableAggregateRoot {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id", nullable = false, unique = true)
    private ProductVariant variant;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "quantity", column = @Column(name = "quantity_on_hand")),
        @AttributeOverride(name = "reserved", column = @Column(name = "quantity_reserved")),
        @AttributeOverride(name = "available", column = @Column(name = "quantity_available"))
    })
    @Builder.Default
    private StockLevel stockLevel = StockLevel.zero();

    @Enumerated(EnumType.STRING)
    @Column(name = "stock_status", nullable = false)
    @Builder.Default
    private StockStatus stockStatus = StockStatus.OUT_OF_STOCK;

    public boolean isInStock() {
        return stockLevel.hasStock();
    }

    public int getAvailableQuantity() {
        return stockLevel.getAvailable();
    }
}