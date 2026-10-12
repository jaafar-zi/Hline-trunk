package com.example.demo.modules.product.domain.specification;

import com.example.demo.modules.product.application.dto.filter.ProductVariantFilter;
import com.example.demo.modules.product.domain.model.ProductVariant;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * Product variant specification builder
 * 
 * @author PIM Team
 * @version 1.0
 */
public class ProductVariantSpecification {

    public static Specification<ProductVariant> fromFilter(ProductVariantFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Product ID filter
            if (filter.getProductId() != null) {
                predicates.add(cb.equal(root.get("product").get("id"), filter.getProductId()));
            }

            // SKU filter
            if (filter.getSku() != null && !filter.getSku().isBlank()) {
                predicates.add(cb.equal(root.get("sku").get("value"), filter.getSku()));
            }

            // Name filter
            if (filter.getName() != null && !filter.getName().isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("name")), 
                    "%" + filter.getName().toLowerCase() + "%"));
            }

            // Active only
            if (Boolean.TRUE.equals(filter.getActiveOnly())) {
                predicates.add(cb.equal(root.get("active"), true));
            }

            // In stock
            if (Boolean.TRUE.equals(filter.getInStock())) {
                predicates.add(cb.greaterThan(
                    root.get("inventory").get("stockLevel").get("available"), 0));
            }

            // Default only
            if (Boolean.TRUE.equals(filter.getDefaultOnly())) {
                predicates.add(cb.equal(root.get("isDefault"), true));
            }

            // Price range
            if (filter.getMinPrice() != null) {
                predicates.add(cb.greaterThanOrEqualTo(
                    root.get("price").get("amount"), 
                    filter.getMinPrice()
                ));
            }
            if (filter.getMaxPrice() != null) {
                predicates.add(cb.lessThanOrEqualTo(
                    root.get("price").get("amount"),
                    filter.getMaxPrice()
                ));
            }

            // Has custom price
            if (Boolean.TRUE.equals(filter.getHasCustomPrice())) {
                predicates.add(cb.isNotNull(root.get("price")));
            }

            // Quantity range
            if (filter.getMinQuantity() != null) {
                predicates.add(cb.greaterThanOrEqualTo(
                    root.get("inventory").get("stockLevel").get("quantity"),
                    filter.getMinQuantity()
                ));
            }

            // Exclude deleted
            if (Boolean.TRUE.equals(filter.getExcludeDeleted())) {
                predicates.add(cb.isNull(root.get("deletedAt")));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}