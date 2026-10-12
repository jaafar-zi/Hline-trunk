package com.example.demo.modules.kitchen.domain.specification;

import com.example.demo.modules.kitchen.application.dto.filter.KitchenRangeFilter;
import com.example.demo.modules.kitchen.domain.model.KitchenRange;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Kitchen Range Specifications
 */
@Component
public class KitchenRangeSpecifications {

    public static Specification<KitchenRange> fromFilter(KitchenRangeFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Always exclude deleted
            predicates.add(cb.equal(root.get("deleted"), false));

            if (filter.getKeyword() != null && !filter.getKeyword().isBlank()) {
                String keyword = "%" + filter.getKeyword().toLowerCase() + "%";
                predicates.add(cb.or(
                    cb.like(cb.lower(root.get("name")), keyword),
                    cb.like(cb.lower(root.get("shortDescription")), keyword)
                ));
            }

            if (filter.getActive() != null) {
                predicates.add(cb.equal(root.get("active"), filter.getActive()));
            }

            if (filter.getCode() != null && !filter.getCode().isBlank()) {
                predicates.add(cb.equal(root.get("code"), filter.getCode()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<KitchenRange> isActive() {
        return (root, query, cb) -> cb.and(
            cb.equal(root.get("active"), true),
            cb.equal(root.get("deleted"), false)
        );
    }

    public static Specification<KitchenRange> isNotDeleted() {
        return (root, query, cb) -> cb.equal(root.get("deleted"), false);
    }

    public static Specification<KitchenRange> hasCode(String code) {
        return (root, query, cb) -> {
            if (code == null || code.isBlank()) return cb.conjunction();
            return cb.equal(root.get("code"), code.toUpperCase());
        };
    }

    public static Specification<KitchenRange> hasNameLike(String name) {
        return (root, query, cb) -> {
            if (name == null || name.isBlank()) return cb.conjunction();
            return cb.like(cb.lower(root.get("name")),
                "%" + name.toLowerCase() + "%");
        };
    }
}