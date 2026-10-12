package com.example.demo.modules.kitchen.domain.specification;

import com.example.demo.modules.kitchen.application.dto.filter.KitchenCollectionFilter;
import com.example.demo.modules.kitchen.domain.model.KitchenCollection;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Kitchen Collection Specifications
 */
@Component
public class KitchenCollectionSpecifications {

    public static Specification<KitchenCollection> fromFilter(KitchenCollectionFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("deleted"), false));

            if (filter.getKeyword() != null && !filter.getKeyword().isBlank()) {
                String keyword = "%" + filter.getKeyword().toLowerCase() + "%";
                var colourJoin = root.join("colour", JoinType.LEFT);
                predicates.add(cb.or(
                    cb.like(cb.lower(root.get("name")), keyword),
                    cb.like(cb.lower(colourJoin.get("name")), keyword),
                    cb.like(cb.lower(root.get("shortDescription")), keyword)
                ));
            }

            if (filter.getRangeId() != null) {
                var finishGroupJoin = root.join("finishGroup", JoinType.LEFT);
                predicates.add(cb.equal(finishGroupJoin.get("range").get("id"),
                    filter.getRangeId()));
            }

            if (filter.getFinishGroupId() != null) {
                predicates.add(cb.equal(root.get("finishGroup").get("id"),
                    filter.getFinishGroupId()));
            }

            if (filter.getColourId() != null) {
                predicates.add(cb.equal(root.get("colour").get("id"),
                    filter.getColourId()));
            }

            if (filter.getActive() != null) {
                predicates.add(cb.equal(root.get("active"), filter.getActive()));
            }

            if (filter.getFinishTypeCode() != null && !filter.getFinishTypeCode().isBlank()) {
                var finishGroupJoin = root.join("finishGroup", JoinType.LEFT);
                var finishTypeJoin = finishGroupJoin.join("finishType", JoinType.LEFT);
                predicates.add(cb.equal(finishTypeJoin.get("code"),
                    filter.getFinishTypeCode()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<KitchenCollection> isActive() {
        return (root, query, cb) -> cb.and(
            cb.equal(root.get("active"), true),
            cb.equal(root.get("deleted"), false)
        );
    }

    public static Specification<KitchenCollection> inRange(UUID rangeId) {
        return (root, query, cb) -> {
            if (rangeId == null) return cb.conjunction();
            var fgJoin = root.join("finishGroup", JoinType.LEFT);
            return cb.equal(fgJoin.get("range").get("id"), rangeId);
        };
    }

    public static Specification<KitchenCollection> hasColour(UUID colourId) {
        return (root, query, cb) -> {
            if (colourId == null) return cb.conjunction();
            return cb.equal(root.get("colour").get("id"), colourId);
        };
    }
}