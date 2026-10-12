package com.example.demo.modules.product.domain.specification;

import com.example.demo.modules.product.application.dto.filter.CategoryFilter;
import com.example.demo.modules.product.domain.model.Category;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Category Specifications for dynamic query building
 *
 * @author PIM Team
 * @version 1.0
 */
public class CategorySpecifications {

        // ====================================
    // Filter-to-Specification Conversion
    // ====================================

    /**
     * Build a composite Specification from a CategoryFilter DTO.
     *
     * Maps each non-null filter field to the corresponding specification
     * and combines them with AND logic.
     *
     * @param filter the category filter criteria
     * @return combined specification matching all provided criteria
     */
    public Specification<Category> fromFilter(CategoryFilter filter) {
        Specification<Category> spec = Specification.where(null);

        if (filter == null) {
            return spec;
        }

        // Soft-delete exclusion (default: true)
        if (Boolean.TRUE.equals(filter.getExcludeDeleted())) {
            spec = spec.and(isNotDeleted());
        }

        // Keyword search (spans name, code, description)
        if (filter.getKeyword() != null && !filter.getKeyword().isBlank()) {
            spec = spec.and(searchByKeyword(filter.getKeyword()));
        }

        // Exact code match
        if (filter.getCode() != null && !filter.getCode().isBlank()) {
            spec = spec.and(hasCode(filter.getCode()));
        }

        // Name like match
        if (filter.getName() != null && !filter.getName().isBlank()) {
            spec = spec.and(hasNameLike(filter.getName()));
        }

        // Active only
        if (Boolean.TRUE.equals(filter.getActiveOnly())) {
            spec = spec.and(isActive());
        }

        // Root categories only (no parent)
        if (Boolean.TRUE.equals(filter.getRootOnly())) {
            spec = spec.and(isRoot());
        }

        // Leaf categories only (no children)
        if (Boolean.TRUE.equals(filter.getLeafOnly())) {
            spec = spec.and(isLeaf());
        }

        // Parent ID filter
        if (filter.getParentId() != null) {
            spec = spec.and(hasParent(filter.getParentId()));
        }

        // Has children
        if (Boolean.TRUE.equals(filter.getHasChildren())) {
            spec = spec.and(hasChildren());
        }

        // Date range filters
        if (filter.getCreatedAfter() != null) {
            spec = spec.and(createdAfter(filter.getCreatedAfter()));
        }

        if (filter.getCreatedBefore() != null) {
            spec = spec.and(createdBefore(filter.getCreatedBefore()));
        }

        return spec;
    }
    
    /**
     * Find by code (case-insensitive)
     */
    public static Specification<Category> hasCode(String code) {
        return (root, query, cb) -> {
            if (code == null || code.isBlank()) {
                return cb.conjunction();
            }
            return cb.equal(cb.lower(root.get("code")), code.toLowerCase());
        };
    }

    /**
     * Find by name (case-insensitive, like)
     */
    public static Specification<Category> hasNameLike(String name) {
        return (root, query, cb) -> {
            if (name == null || name.isBlank()) {
                return cb.conjunction();
            }
            return cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
        };
    }

    /**
     * Find active categories
     */
    public static Specification<Category> isActive() {
        return (root, query, cb) -> cb.equal(root.get("active"), true);
    }

    /**
     * Find root categories (no parent)
     */
    public static Specification<Category> isRoot() {
        return (root, query, cb) -> cb.isNull(root.get("parent"));
    }

    /**
     * Find categories by parent ID
     */
    public static Specification<Category> hasParent(UUID parentId) {
        return (root, query, cb) -> {
            if (parentId == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("parent").get("id"), parentId);
        };
    }

    /**
     * Find categories with children
     */
    public static Specification<Category> hasChildren() {
        return (root, query, cb) -> cb.isNotEmpty(root.get("children"));
    }

    /**
     * Find leaf categories (no children)
     */
    public static Specification<Category> isLeaf() {
        return (root, query, cb) -> cb.isEmpty(root.get("children"));
    }

    /**
     * Find not deleted categories
     */
    public static Specification<Category> isNotDeleted() {
        return (root, query, cb) -> cb.equal(root.get("deleted"), false);
    }

    /**
     * Search by keyword
     */
    public static Specification<Category> searchByKeyword(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) {
                return cb.conjunction();
            }

            String likePattern = "%" + keyword.toLowerCase() + "%";

            return cb.or(
                cb.like(cb.lower(root.get("name")), likePattern),
                cb.like(cb.lower(root.get("code")), likePattern),
                cb.like(cb.lower(root.get("description")), likePattern)
            );
        };
    }

    /**
     * Find categories created after a specific date
     */
    public static Specification<Category> createdAfter(LocalDateTime dateTime) {
        return (root, query, cb) -> {
            if (dateTime == null) {
                return cb.conjunction();
            }
            return cb.greaterThanOrEqualTo(root.get("createdAt"), dateTime);
        };
    }

    /**
     * Find categories created before a specific date
     */
    public static Specification<Category> createdBefore(LocalDateTime dateTime) {
        return (root, query, cb) -> {
            if (dateTime == null) {
                return cb.conjunction();
            }
            return cb.lessThanOrEqualTo(root.get("createdAt"), dateTime);
        };
    }

    /**
     * Combine multiple specifications with AND
     */
    public static Specification<Category> combine(Specification<Category>... specs) {
        Specification<Category> result = Specification.where(null);
        for (Specification<Category> spec : specs) {
            if (spec != null) {
                result = result.and(spec);
            }
        }
        return result;
    }
}