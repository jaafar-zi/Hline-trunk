package com.example.demo.modules.kitchen.domain.specification;

import com.example.demo.modules.kitchen.application.dto.filter.KitchenProductFilter;
import com.example.demo.modules.kitchen.domain.model.KitchenProduct;
import com.example.demo.modules.kitchen.domain.model.KitchenProductVariant;
import com.example.demo.modules.kitchen.domain.model.vo.AssemblyType;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Kitchen Product Specifications
 */
@Component
public class KitchenProductSpecifications {

    public static Specification<KitchenProduct> fromFilter(KitchenProductFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("deleted"), false));

            if (filter.getKeyword() != null && !filter.getKeyword().isBlank()) {
                String keyword = "%" + filter.getKeyword().toLowerCase() + "%";
                predicates.add(cb.or(
                    cb.like(cb.lower(root.get("name")), keyword),
                    cb.like(cb.lower(root.get("sku")), keyword),
                    cb.like(cb.lower(root.get("shortDescription")), keyword)
                ));
            }

            if (filter.getCollectionId() != null) {
                predicates.add(cb.equal(root.get("collection").get("id"),
                    filter.getCollectionId()));
            }

            if (filter.getUnitCategoryId() != null) {
                predicates.add(cb.equal(root.get("unitCategory").get("id"),
                    filter.getUnitCategoryId()));
            }

            if (filter.getUnitCategoryCode() != null
                    && !filter.getUnitCategoryCode().isBlank()) {
                predicates.add(cb.equal(root.get("unitCategory").get("code"),
                    filter.getUnitCategoryCode()));
            }

            if (filter.getActive() != null) {
                predicates.add(cb.equal(root.get("active"), filter.getActive()));
            }

            if (Boolean.TRUE.equals(filter.getPopularOnly())) {
                predicates.add(cb.equal(root.get("isPopular"), true));
            }

            if (filter.getMinPrice() != null || filter.getMaxPrice() != null) {
                if (filter.getMinPrice() != null) {
                    predicates.add(cb.greaterThanOrEqualTo(
                        root.get("basePrice").get("amount"), filter.getMinPrice()));
                }
                if (filter.getMaxPrice() != null) {
                    predicates.add(cb.lessThanOrEqualTo(
                        root.get("basePrice").get("amount"), filter.getMaxPrice()));
                }
            }

            if (filter.getMinWidthMm() != null) {
                predicates.add(cb.greaterThanOrEqualTo(
                    root.get("widthMm"), filter.getMinWidthMm()));
            }

            if (filter.getMaxWidthMm() != null) {
                predicates.add(cb.lessThanOrEqualTo(
                    root.get("widthMm"), filter.getMaxWidthMm()));
            }

            // Filter by cabinet colour (via variants subquery)
            if (filter.getCabinetColourId() != null) {
                Subquery<Long> variantSubquery = query.subquery(Long.class);
                Root<KitchenProductVariant> variantRoot =
                    variantSubquery.from(KitchenProductVariant.class);
                variantSubquery.select(cb.count(variantRoot));
                variantSubquery.where(
                    cb.equal(variantRoot.get("product"), root),
                    cb.equal(variantRoot.get("cabinetColour").get("id"),
                        filter.getCabinetColourId()),
                    cb.equal(variantRoot.get("active"), true)
                );
                predicates.add(cb.greaterThan(variantSubquery, 0L));
            }

            // Filter by assembly type (via variants subquery)
            if (filter.getAssemblyType() != null) {
                Subquery<Long> variantSubquery = query.subquery(Long.class);
                Root<KitchenProductVariant> variantRoot =
                    variantSubquery.from(KitchenProductVariant.class);
                variantSubquery.select(cb.count(variantRoot));
                variantSubquery.where(
                    cb.equal(variantRoot.get("product"), root),
                    cb.equal(variantRoot.get("assemblyType"), filter.getAssemblyType()),
                    cb.equal(variantRoot.get("active"), true)
                );
                predicates.add(cb.greaterThan(variantSubquery, 0L));
            }

            // Filter in stock (via variants subquery)
            if (Boolean.TRUE.equals(filter.getInStockOnly())) {
                Subquery<Long> variantSubquery = query.subquery(Long.class);
                Root<KitchenProductVariant> variantRoot =
                    variantSubquery.from(KitchenProductVariant.class);
                variantSubquery.select(cb.count(variantRoot));
                variantSubquery.where(
                    cb.equal(variantRoot.get("product"), root),
                    cb.equal(variantRoot.get("inStock"), true),
                    cb.equal(variantRoot.get("active"), true)
                );
                predicates.add(cb.greaterThan(variantSubquery, 0L));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<KitchenProduct> isActive() {
        return (root, query, cb) -> cb.and(
            cb.equal(root.get("active"), true),
            cb.equal(root.get("deleted"), false)
        );
    }

    public static Specification<KitchenProduct> inCollection(UUID collectionId) {
        return (root, query, cb) -> {
            if (collectionId == null) return cb.conjunction();
            return cb.equal(root.get("collection").get("id"), collectionId);
        };
    }

    public static Specification<KitchenProduct> inCategory(UUID categoryId) {
        return (root, query, cb) -> {
            if (categoryId == null) return cb.conjunction();
            return cb.equal(root.get("unitCategory").get("id"), categoryId);
        };
    }

    public static Specification<KitchenProduct> isPopular() {
        return (root, query, cb) -> cb.equal(root.get("isPopular"), true);
    }

    public static Specification<KitchenProduct> hasPriceBetween(
            BigDecimal min, BigDecimal max) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (min != null) {
                predicates.add(cb.greaterThanOrEqualTo(
                    root.get("basePrice").get("amount"), min));
            }
            if (max != null) {
                predicates.add(cb.lessThanOrEqualTo(
                    root.get("basePrice").get("amount"), max));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<KitchenProduct> hasVariantWithColour(UUID colourId) {
        return (root, query, cb) -> {
            if (colourId == null) return cb.conjunction();
            var variantsJoin = root.join("variants", JoinType.LEFT);
            return cb.and(
                cb.equal(variantsJoin.get("cabinetColour").get("id"), colourId),
                cb.equal(variantsJoin.get("active"), true)
            );
        };
    }

    public static Specification<KitchenProduct> hasVariantWithAssemblyType(
            AssemblyType assemblyType) {
        return (root, query, cb) -> {
            if (assemblyType == null) return cb.conjunction();
            var variantsJoin = root.join("variants", JoinType.LEFT);
            return cb.and(
                cb.equal(variantsJoin.get("assemblyType"), assemblyType),
                cb.equal(variantsJoin.get("active"), true)
            );
        };
    }

    public static Specification<KitchenProduct> searchByKeyword(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) return cb.conjunction();
            String pattern = "%" + keyword.toLowerCase() + "%";
            return cb.or(
                cb.like(cb.lower(root.get("name")), pattern),
                cb.like(cb.lower(root.get("sku")), pattern),
                cb.like(cb.lower(root.get("shortDescription")), pattern)
            );
        };
    }

    @SafeVarargs
    public static Specification<KitchenProduct> combine(
            Specification<KitchenProduct>... specs) {
        Specification<KitchenProduct> result = Specification.where(null);
        for (Specification<KitchenProduct> spec : specs) {
            if (spec != null) result = result.and(spec);
        }
        return result;
    }
}