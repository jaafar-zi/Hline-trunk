package com.example.demo.modules.product.domain.specification;

import com.example.demo.modules.product.application.dto.filter.ProductPriceFilter;
import com.example.demo.modules.product.domain.model.ProductPrice;
import com.example.demo.modules.product.domain.model.vo.PriceType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Product price specification builder
 * 
 * @author PIM Team
 * @version 1.0
 */
public class ProductPriceSpecification {

    public static Specification<ProductPrice> fromFilter(ProductPriceFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Product ID filter
            if (filter.getProductId() != null) {
                predicates.add(cb.equal(root.get("product").get("id"), filter.getProductId()));
            }

            // Price type filter
            if (filter.getPriceType() != null && !filter.getPriceType().isBlank()) {
                try {
                    PriceType priceType = PriceType.valueOf(filter.getPriceType().toUpperCase());
                    predicates.add(cb.equal(root.get("priceType"), priceType));
                } catch (IllegalArgumentException e) {
                    // Invalid price type - ignore
                }
            }

            // Price types filter
            if (filter.getPriceTypes() != null && !filter.getPriceTypes().isEmpty()) {
                List<PriceType> priceTypes = new ArrayList<>();
                filter.getPriceTypes().forEach(pt -> {
                    try {
                        priceTypes.add(PriceType.valueOf(pt.toUpperCase()));
                    } catch (IllegalArgumentException e) {
                        // Invalid price type - ignore
                    }
                });
                if (!priceTypes.isEmpty()) {
                    predicates.add(root.get("priceType").in(priceTypes));
                }
            }

            // Currency filter
            if (filter.getCurrency() != null && !filter.getCurrency().isBlank()) {
                predicates.add(cb.equal(root.get("price").get("currency"), filter.getCurrency()));
            }

            // Active only
            if (Boolean.TRUE.equals(filter.getActiveOnly())) {
                predicates.add(cb.equal(root.get("active"), true));
            }

            // Currently valid
            if (Boolean.TRUE.equals(filter.getCurrentlyValid())) {
                LocalDateTime now = LocalDateTime.now();
                
                Predicate validFromPredicate = cb.or(
                    cb.isNull(root.get("validFrom")),
                    cb.lessThanOrEqualTo(root.get("validFrom"), now)
                );
                
                Predicate validToPredicate = cb.or(
                    cb.isNull(root.get("validTo")),
                    cb.greaterThanOrEqualTo(root.get("validTo"), now)
                );
                
                predicates.add(cb.and(validFromPredicate, validToPredicate));
            }

            // Scheduled (future)
            if (Boolean.TRUE.equals(filter.getScheduled())) {
                LocalDateTime now = LocalDateTime.now();
                predicates.add(cb.greaterThan(root.get("validFrom"), now));
            }

            // Expired
            if (Boolean.TRUE.equals(filter.getExpired())) {
                LocalDateTime now = LocalDateTime.now();
                predicates.add(cb.lessThan(root.get("validTo"), now));
            }

            // Amount range
            if (filter.getMinAmount() != null) {
                predicates.add(cb.greaterThanOrEqualTo(
                    root.get("price").get("amount"), 
                    filter.getMinAmount()
                ));
            }
            if (filter.getMaxAmount() != null) {
                predicates.add(cb.lessThanOrEqualTo(
                    root.get("price").get("amount"),
                    filter.getMaxAmount()
                ));
            }

            // Customer group
            if (filter.getCustomerGroup() != null && !filter.getCustomerGroup().isBlank()) {
                predicates.add(cb.equal(root.get("customerGroup"), filter.getCustomerGroup()));
            }

            // Market
            if (filter.getMarket() != null && !filter.getMarket().isBlank()) {
                predicates.add(cb.equal(root.get("market"), filter.getMarket()));
            }

            // Has discount
            if (Boolean.TRUE.equals(filter.getHasDiscount())) {
                predicates.add(cb.isNotNull(root.get("discountPercentage")));
                predicates.add(cb.greaterThan(root.get("discountPercentage"), 0.0));
            }

            // Valid from/to date range
            if (filter.getValidFromAfter() != null) {
                predicates.add(cb.greaterThanOrEqualTo(
                    root.get("validFrom"), 
                    filter.getValidFromAfter()
                ));
            }
            if (filter.getValidToBefore() != null) {
                predicates.add(cb.lessThanOrEqualTo(
                    root.get("validTo"),
                    filter.getValidToBefore()
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