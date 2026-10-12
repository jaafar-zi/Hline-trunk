package com.example.demo.modules.product.domain.specification;

import com.example.demo.modules.product.application.dto.filter.ProductInventoryFilter;
import com.example.demo.modules.product.domain.model.ProductInventory;
import com.example.demo.modules.product.domain.model.vo.StockStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * Product inventory specification builder
 * 
 * @author PIM Team
 * @version 1.0
 */
public class ProductInventorySpecification {

    public static Specification<ProductInventory> fromFilter(ProductInventoryFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Stock status filter
            if (filter.getStockStatus() != null && !filter.getStockStatus().isBlank()) {
                try {
                    StockStatus status = StockStatus.valueOf(filter.getStockStatus().toUpperCase());
                    predicates.add(cb.equal(root.get("stockLevel").get("status"), status));
                } catch (IllegalArgumentException e) {
                    // Invalid status - ignore
                }
            }

            // Stock statuses filter
            if (filter.getStockStatuses() != null && !filter.getStockStatuses().isEmpty()) {
                List<StockStatus> statuses = new ArrayList<>();
                filter.getStockStatuses().forEach(s -> {
                    try {
                        statuses.add(StockStatus.valueOf(s.toUpperCase()));
                    } catch (IllegalArgumentException e) {
                        // Invalid status - ignore
                    }
                });
                if (!statuses.isEmpty()) {
                    predicates.add(root.get("stockLevel").get("status").in(statuses));
                }
            }

            // Track inventory only
            if (Boolean.TRUE.equals(filter.getTrackInventory())) {
                predicates.add(cb.equal(root.get("trackInventory"), true));
            }

            // In stock
            if (Boolean.TRUE.equals(filter.getInStock())) {
                predicates.add(cb.greaterThan(root.get("stockLevel").get("available"), 0));
            }

            // Low stock
            if (Boolean.TRUE.equals(filter.getLowStock())) {
                predicates.add(cb.and(
                    cb.greaterThan(root.get("stockLevel").get("available"), 0),
                    cb.lessThanOrEqualTo(
                        root.get("stockLevel").get("available"),
                        root.get("lowStockThreshold")
                    )
                ));
            }

            // Out of stock
            if (Boolean.TRUE.equals(filter.getOutOfStock())) {
                predicates.add(cb.equal(root.get("stockLevel").get("available"), 0));
            }

            // Needs reorder
            if (Boolean.TRUE.equals(filter.getNeedsReorder())) {
                predicates.add(cb.lessThanOrEqualTo(
                    root.get("stockLevel").get("quantity"),
                    root.get("reorderPoint")
                ));
            }

            // Quantity range
            if (filter.getMinQuantity() != null) {
                predicates.add(cb.greaterThanOrEqualTo(
                    root.get("stockLevel").get("quantity"),
                    filter.getMinQuantity()
                ));
            }
            if (filter.getMaxQuantity() != null) {
                predicates.add(cb.lessThanOrEqualTo(
                    root.get("stockLevel").get("quantity"),
                    filter.getMaxQuantity()
                ));
            }

            // Reserved quantity
            if (filter.getMinReserved() != null) {
                predicates.add(cb.greaterThanOrEqualTo(
                    root.get("stockLevel").get("reserved"),
                    filter.getMinReserved()
                ));
            }

            // Allow backorder
            if (filter.getAllowBackorder() != null) {
                predicates.add(cb.equal(root.get("allowBackorder"), filter.getAllowBackorder()));
            }

            // Warehouse location
            if (filter.getWarehouseLocation() != null && !filter.getWarehouseLocation().isBlank()) {
                predicates.add(cb.equal(root.get("warehouseLocation"), filter.getWarehouseLocation()));
            }

            // Restocked after
            if (filter.getRestockedAfter() != null) {
                predicates.add(cb.greaterThanOrEqualTo(
                    root.get("lastRestockDate"),
                    filter.getRestockedAfter()
                ));
            }

            // Stock check before
            if (filter.getStockCheckBefore() != null) {
                predicates.add(cb.lessThanOrEqualTo(
                    root.get("lastStockCheck"),
                    filter.getStockCheckBefore()
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}