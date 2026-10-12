package com.example.demo.modules.product.domain.specification;

import com.example.demo.modules.product.application.dto.filter.ProductFilter;
import com.example.demo.modules.product.domain.model.Category;
import com.example.demo.modules.product.domain.model.Product;
import com.example.demo.modules.product.domain.model.ProductAttribute;
import com.example.demo.modules.product.domain.model.ProductImage;
import com.example.demo.modules.product.domain.model.ProductInventory;
import com.example.demo.modules.product.domain.model.ProductPrice;
import com.example.demo.modules.product.domain.model.ProductVariant;
import com.example.demo.modules.product.domain.model.vo.ImageType;
import com.example.demo.modules.product.domain.model.vo.PriceType;
import com.example.demo.modules.product.domain.model.vo.ProductStatus;
import com.example.demo.modules.product.domain.model.vo.StockStatus;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class ProductSpecifications {

    public static Specification<Product> fromFilter(ProductFilter filter) {
        return (root, query, cb) -> extracted(filter, root, query, cb);
    }

    private static Predicate extracted(ProductFilter filter, Root<Product> root,
            CriteriaQuery<?> query, CriteriaBuilder cb) {
        List<Predicate> predicates = new ArrayList<>();

        if (filter.getKeyword() != null && !filter.getKeyword().isBlank()) {
            String keyword = "%" + filter.getKeyword().toLowerCase() + "%";
            predicates.add(cb.or(
                cb.like(cb.lower(root.get("name")), keyword),
                cb.like(cb.lower(root.get("sku").get("value")), keyword),
                cb.like(cb.lower(root.get("shortDescription")), keyword),
                cb.like(cb.lower(root.get("brand")), keyword)
            ));
        }

        if (filter.getSku() != null && !filter.getSku().isBlank()) {
            predicates.add(cb.equal(root.get("sku").get("value"), filter.getSku()));
        }

        if (filter.getName() != null && !filter.getName().isBlank()) {
            predicates.add(cb.like(cb.lower(root.get("name")),
                "%" + filter.getName().toLowerCase() + "%"));
        }

        if (filter.getIds() != null && !filter.getIds().isEmpty()) {
            predicates.add(root.get("id").in(filter.getIds()));
        }

        if (filter.getStatus() != null && !filter.getStatus().isBlank()) {
            try {
                ProductStatus status = ProductStatus.valueOf(filter.getStatus().toUpperCase());
                predicates.add(cb.equal(root.get("status"), status));
            } catch (IllegalArgumentException ignored) {}
        }

        if (filter.getStatuses() != null && !filter.getStatuses().isEmpty()) {
            List<ProductStatus> statuses = new ArrayList<>();
            filter.getStatuses().forEach(s -> {
                try {
                    statuses.add(ProductStatus.valueOf(s.toUpperCase()));
                } catch (IllegalArgumentException ignored) {}
            });
            if (!statuses.isEmpty()) {
                predicates.add(root.get("status").in(statuses));
            }
        }

        if (Boolean.TRUE.equals(filter.getPublishedOnly())) {
            predicates.add(cb.equal(root.get("status"), ProductStatus.PUBLISHED));
        }

        if (Boolean.TRUE.equals(filter.getExcludeDeleted())) {
            predicates.add(cb.isNull(root.get("deletedAt")));
        }

        if (filter.getCategoryId() != null) {
            Join<Product, Category> categoryJoin = root.join("category");
            predicates.add(cb.equal(categoryJoin.get("id"), filter.getCategoryId()));
        }

        if (filter.getCategoryIds() != null && !filter.getCategoryIds().isEmpty()) {
            Join<Product, Category> categoryJoin = root.join("category");
            predicates.add(categoryJoin.get("id").in(filter.getCategoryIds()));
        }

        if (filter.getCategoryCode() != null && !filter.getCategoryCode().isBlank()) {
            Join<Product, Category> categoryJoin = root.join("category");
            predicates.add(cb.equal(categoryJoin.get("code"), filter.getCategoryCode()));
        }

        if (filter.getBrand() != null && !filter.getBrand().isBlank()) {
            predicates.add(cb.equal(root.get("brand"), filter.getBrand()));
        }

        if (filter.getBrandLike() != null && !filter.getBrandLike().isBlank()) {
            predicates.add(cb.like(cb.lower(root.get("brand")),
                "%" + filter.getBrandLike().toLowerCase() + "%"));
        }

        if (filter.getBrands() != null && !filter.getBrands().isEmpty()) {
            predicates.add(root.get("brand").in(filter.getBrands()));
        }

        if (filter.getManufacturer() != null && !filter.getManufacturer().isBlank()) {
            predicates.add(cb.equal(root.get("manufacturer"), filter.getManufacturer()));
        }

        // Price filters — use PriceType enum, not String
        if (filter.getMinPrice() != null || filter.getMaxPrice() != null) {
            Subquery<Double> priceSubquery = query.subquery(Double.class);
            Root<ProductPrice> priceRoot = priceSubquery.from(ProductPrice.class);
            priceSubquery.select(priceRoot.get("price").get("amount"));
            priceSubquery.where(
                cb.equal(priceRoot.get("product"), root),
                cb.equal(priceRoot.get("priceType"), PriceType.BASE),  
                cb.equal(priceRoot.get("active"), true)
            );
            if (filter.getMinPrice() != null) {
                predicates.add(cb.greaterThanOrEqualTo(priceSubquery,
                    filter.getMinPrice().doubleValue()));
            }
            if (filter.getMaxPrice() != null) {
                predicates.add(cb.lessThanOrEqualTo(priceSubquery,
                    filter.getMaxPrice().doubleValue()));
            }
        }

        if (Boolean.TRUE.equals(filter.getHasActivePrice())) {
            Subquery<Long> priceSubquery = query.subquery(Long.class);
            Root<ProductPrice> priceRoot = priceSubquery.from(ProductPrice.class);
            priceSubquery.select(cb.count(priceRoot));
            priceSubquery.where(
                cb.equal(priceRoot.get("product"), root),
                cb.equal(priceRoot.get("active"), true)
            );
            predicates.add(cb.greaterThan(priceSubquery, 0L));
        }

        if (filter.getInStock() != null || filter.getOutOfStock() != null
                || filter.getLowStock() != null) {
            Join<Product, ProductInventory> inventoryJoin =
                root.join("inventory", JoinType.LEFT);

            if (Boolean.TRUE.equals(filter.getInStock())) {
                predicates.add(cb.greaterThan(
                    inventoryJoin.get("stockLevel").get("available"), 0));
            }
            if (Boolean.TRUE.equals(filter.getOutOfStock())) {
                predicates.add(cb.or(
                    cb.isNull(inventoryJoin.get("id")),
                    cb.equal(inventoryJoin.get("stockLevel").get("available"), 0)
                ));
            }
            if (Boolean.TRUE.equals(filter.getLowStock())) {
                predicates.add(cb.and(
                    cb.greaterThan(inventoryJoin.get("stockLevel").get("available"), 0),
                    cb.lessThanOrEqualTo(
                        inventoryJoin.get("stockLevel").get("available"),
                        inventoryJoin.get("lowStockThreshold")
                    )
                ));
            }
        }

        if (filter.getMinQuantity() != null || filter.getMaxQuantity() != null) {
            Join<Product, ProductInventory> inventoryJoin =
                root.join("inventory", JoinType.LEFT);
            if (filter.getMinQuantity() != null) {
                predicates.add(cb.greaterThanOrEqualTo(
                    inventoryJoin.get("stockLevel").get("quantity"),
                    filter.getMinQuantity()));
            }
            if (filter.getMaxQuantity() != null) {
                predicates.add(cb.lessThanOrEqualTo(
                    inventoryJoin.get("stockLevel").get("quantity"),
                    filter.getMaxQuantity()));
            }
        }

        if (filter.getAttributesAsMap() != null && !filter.getAttributesAsMap().isEmpty()) {
            for (Map.Entry<String, String> entry : filter.getAttributesAsMap().entrySet()) {
                Subquery<Long> attrSubquery = query.subquery(Long.class);
                Root<ProductAttribute> attrRoot = attrSubquery.from(ProductAttribute.class);
                attrSubquery.select(cb.count(attrRoot));
                attrSubquery.where(
                    cb.equal(attrRoot.get("product"), root),
                    cb.equal(cb.lower(attrRoot.get("key")), entry.getKey().toLowerCase()),
                    cb.equal(attrRoot.get("value"), entry.getValue())
                );
                predicates.add(cb.greaterThan(attrSubquery, 0L));
            }
        }

        if (Boolean.TRUE.equals(filter.getHasAllRequiredAttributes())) {
            Subquery<Long> attrSubquery = query.subquery(Long.class);
            Root<ProductAttribute> attrRoot = attrSubquery.from(ProductAttribute.class);
            attrSubquery.select(cb.count(attrRoot));
            attrSubquery.where(
                cb.equal(attrRoot.get("product"), root),
                cb.equal(attrRoot.get("required"), true),
                cb.or(
                    cb.isNull(attrRoot.get("value")),
                    cb.equal(attrRoot.get("value"), "")
                )
            );
            predicates.add(cb.equal(attrSubquery, 0L));
        }

        if (filter.getHasVariants() != null) {
            Subquery<Long> variantSubquery = query.subquery(Long.class);
            Root<ProductVariant> variantRoot = variantSubquery.from(ProductVariant.class);
            variantSubquery.select(cb.count(variantRoot));
            variantSubquery.where(cb.equal(variantRoot.get("product"), root));
            if (Boolean.TRUE.equals(filter.getHasVariants())) {
                predicates.add(cb.greaterThan(variantSubquery, 0L));
            } else {
                predicates.add(cb.equal(variantSubquery, 0L));
            }
        }

        if (filter.getHasImages() != null) {
            Subquery<Long> imageSubquery = query.subquery(Long.class);
            Root<ProductImage> imageRoot = imageSubquery.from(ProductImage.class);
            imageSubquery.select(cb.count(imageRoot));
            imageSubquery.where(cb.equal(imageRoot.get("product"), root));
            if (Boolean.TRUE.equals(filter.getHasImages())) {
                predicates.add(cb.greaterThan(imageSubquery, 0L));
            } else {
                predicates.add(cb.equal(imageSubquery, 0L));
            }
        }

        if (filter.getCreatedAfter() != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"),
                filter.getCreatedAfter()));
        }
        if (filter.getCreatedBefore() != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"),
                filter.getCreatedBefore()));
        }
        if (filter.getPublishedAfter() != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("publishedAt"),
                filter.getPublishedAfter()));
        }
        if (filter.getPublishedBefore() != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("publishedAt"),
                filter.getPublishedBefore()));
        }
        if (filter.getUpdatedAfter() != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("updatedAt"),
                filter.getUpdatedAfter()));
        }
        if (filter.getUpdatedBefore() != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("updatedAt"),
                filter.getUpdatedBefore()));
        }

        if (Boolean.TRUE.equals(filter.getInStock())) {
            LocalDateTime now = LocalDateTime.now();
            predicates.add(cb.and(
                cb.or(cb.isNull(root.get("availableFrom")),
                    cb.lessThanOrEqualTo(root.get("availableFrom"), now)),
                cb.or(cb.isNull(root.get("availableUntil")),
                    cb.greaterThanOrEqualTo(root.get("availableUntil"), now))
            ));
        }

        return cb.and(predicates.toArray(new Predicate[0]));
    }

    public static Specification<Product> hasSku(String sku) {
        return (root, query, cb) -> {
            if (sku == null || sku.isBlank()) return cb.conjunction();
            return cb.equal(cb.lower(root.get("sku").get("value")), sku.toLowerCase());
        };
    }

    public static Specification<Product> hasNameLike(String name) {
        return (root, query, cb) -> {
            if (name == null || name.isBlank()) return cb.conjunction();
            return cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
        };
    }

    public static Specification<Product> hasStatus(ProductStatus status) {
        return (root, query, cb) -> {
            if (status == null) return cb.conjunction();
            return cb.equal(root.get("status"), status);
        };
    }

    public static Specification<Product> hasStatusIn(List<ProductStatus> statuses) {
        return (root, query, cb) -> {
            if (statuses == null || statuses.isEmpty()) return cb.conjunction();
            return root.get("status").in(statuses);
        };
    }

    public static Specification<Product> isPublished() {
        return (root, query, cb) ->
            cb.equal(root.get("status"), ProductStatus.PUBLISHED);
    }

    public static Specification<Product> isNotDeleted() {
        return (root, query, cb) -> cb.equal(root.get("deleted"), false);
    }

    public static Specification<Product> inCategory(UUID categoryId) {
        return (root, query, cb) -> {
            if (categoryId == null) return cb.conjunction();
            return cb.equal(root.get("category").get("id"), categoryId);
        };
    }

    public static Specification<Product> inCategories(List<UUID> categoryIds) {
        return (root, query, cb) -> {
            if (categoryIds == null || categoryIds.isEmpty()) return cb.conjunction();
            return root.get("category").get("id").in(categoryIds);
        };
    }

    public static Specification<Product> inCategoryCode(String categoryCode) {
        return (root, query, cb) -> {
            if (categoryCode == null || categoryCode.isBlank()) return cb.conjunction();
            return cb.equal(root.get("category").get("code"), categoryCode);
        };
    }

    public static Specification<Product> hasBrand(String brand) {
        return (root, query, cb) -> {
            if (brand == null || brand.isBlank()) return cb.conjunction();
            return cb.equal(cb.lower(root.get("brand")), brand.toLowerCase());
        };
    }

    public static Specification<Product> hasBrandLike(String brand) {
        return (root, query, cb) -> {
            if (brand == null || brand.isBlank()) return cb.conjunction();
            return cb.like(cb.lower(root.get("brand")), "%" + brand.toLowerCase() + "%");
        };
    }

    public static Specification<Product> hasManufacturer(String manufacturer) {
        return (root, query, cb) -> {
            if (manufacturer == null || manufacturer.isBlank()) return cb.conjunction();
            return cb.equal(cb.lower(root.get("manufacturer")), manufacturer.toLowerCase());
        };
    }

    public static Specification<Product> hasBrandIn(List<String> brands) {
        return (root, query, cb) -> {
            if (brands == null || brands.isEmpty()) return cb.conjunction();
            return cb.lower(root.get("brand")).in(
                brands.stream().map(String::toLowerCase).toList());
        };
    }

    // Price filters — all using PriceType enum
    public static Specification<Product> hasPriceBetween(BigDecimal minPrice, BigDecimal maxPrice) {
        return (root, query, cb) -> {
            if (minPrice == null && maxPrice == null) return cb.conjunction();
            var pricesJoin = root.join("prices", JoinType.LEFT);
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(pricesJoin.get("priceType"), PriceType.BASE)); 
            predicates.add(cb.equal(pricesJoin.get("active"), true));
            if (minPrice != null)
                predicates.add(cb.greaterThanOrEqualTo(
                    pricesJoin.get("price").get("amount"), minPrice));
            if (maxPrice != null)
                predicates.add(cb.lessThanOrEqualTo(
                    pricesJoin.get("price").get("amount"), maxPrice));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Product> hasPriceLessThan(BigDecimal maxPrice) {
        return hasPriceBetween(null, maxPrice);
    }

    public static Specification<Product> hasPriceGreaterThan(BigDecimal minPrice) {
        return hasPriceBetween(minPrice, null);
    }

    public static Specification<Product> hasActivePrice() {
        return (root, query, cb) -> {
            var pricesJoin = root.join("prices", JoinType.LEFT);
            return cb.equal(pricesJoin.get("active"), true);
        };
    }

    public static Specification<Product> hasPromotionalPrice() {
        return (root, query, cb) -> {
            var pricesJoin = root.join("prices", JoinType.LEFT);
            return cb.and(
                cb.equal(pricesJoin.get("priceType"), PriceType.PROMOTIONAL),  
                cb.equal(pricesJoin.get("active"), true)
            );
        };
    }

    // Inventory filters — all using StockStatus enum
    public static Specification<Product> isInStock() {
        return (root, query, cb) -> {
            var inventoryJoin = root.join("inventory", JoinType.LEFT);
            return cb.equal(inventoryJoin.get("stockStatus"), StockStatus.IN_STOCK);  
        };
    }

    public static Specification<Product> isOutOfStock() {
        return (root, query, cb) -> {
            var inventoryJoin = root.join("inventory", JoinType.LEFT);
            return cb.equal(inventoryJoin.get("stockStatus"), StockStatus.OUT_OF_STOCK);  
        };
    }

    public static Specification<Product> isLowStock() {
        return (root, query, cb) -> {
            var inventoryJoin = root.join("inventory", JoinType.LEFT);
            return cb.equal(inventoryJoin.get("stockStatus"), StockStatus.LOW_STOCK);  
        };
    }

    public static Specification<Product> hasMinimumQuantity(Integer minQuantity) {
        return (root, query, cb) -> {
            if (minQuantity == null) return cb.conjunction();
            var inventoryJoin = root.join("inventory", JoinType.LEFT);
            return cb.greaterThanOrEqualTo(
                inventoryJoin.get("stockLevel").get("available"), minQuantity);
        };
    }

    public static Specification<Product> needsReorder() {
        return (root, query, cb) -> {
            var inventoryJoin = root.join("inventory", JoinType.LEFT);
            return cb.and(
                cb.isNotNull(inventoryJoin.get("reorderPoint")),
                cb.lessThanOrEqualTo(
                    inventoryJoin.get("stockLevel").get("available"),
                    inventoryJoin.get("reorderPoint")
                )
            );
        };
    }

    public static Specification<Product> createdAfter(LocalDateTime date) {
        return (root, query, cb) -> {
            if (date == null) return cb.conjunction();
            return cb.greaterThanOrEqualTo(root.get("createdAt"), date);
        };
    }

    public static Specification<Product> createdBefore(LocalDateTime date) {
        return (root, query, cb) -> {
            if (date == null) return cb.conjunction();
            return cb.lessThanOrEqualTo(root.get("createdAt"), date);
        };
    }

    public static Specification<Product> createdBetween(LocalDateTime startDate,
            LocalDateTime endDate) {
        return (root, query, cb) -> {
            if (startDate == null && endDate == null) return cb.conjunction();
            List<Predicate> predicates = new ArrayList<>();
            if (startDate != null)
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), startDate));
            if (endDate != null)
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), endDate));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Product> publishedAfter(LocalDateTime date) {
        return (root, query, cb) -> {
            if (date == null) return cb.conjunction();
            return cb.greaterThanOrEqualTo(root.get("publishedAt"), date);
        };
    }

    public static Specification<Product> publishedBetween(LocalDateTime startDate,
            LocalDateTime endDate) {
        return (root, query, cb) -> {
            if (startDate == null && endDate == null) return cb.conjunction();
            List<Predicate> predicates = new ArrayList<>();
            if (startDate != null)
                predicates.add(cb.greaterThanOrEqualTo(root.get("publishedAt"), startDate));
            if (endDate != null)
                predicates.add(cb.lessThanOrEqualTo(root.get("publishedAt"), endDate));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Product> isAvailableNow() {
        return (root, query, cb) -> {
            LocalDateTime now = LocalDateTime.now();
            return cb.and(
                cb.equal(root.get("status"), ProductStatus.PUBLISHED),
                cb.or(cb.isNull(root.get("availableFrom")),
                    cb.lessThanOrEqualTo(root.get("availableFrom"), now)),
                cb.or(cb.isNull(root.get("availableUntil")),
                    cb.greaterThanOrEqualTo(root.get("availableUntil"), now))
            );
        };
    }

    public static Specification<Product> isScheduledForFuture() {
        return (root, query, cb) -> {
            LocalDateTime now = LocalDateTime.now();
            return cb.greaterThan(root.get("availableFrom"), now);
        };
    }

    public static Specification<Product> isExpired() {
        return (root, query, cb) -> {
            LocalDateTime now = LocalDateTime.now();
            return cb.lessThan(root.get("availableUntil"), now);
        };
    }

    public static Specification<Product> hasAttribute(String key, String value) {
        return (root, query, cb) -> {
            if (key == null || key.isBlank()) return cb.conjunction();
            var attributesJoin = root.join("attributes", JoinType.LEFT);
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(attributesJoin.get("key"), key));
            if (value != null && !value.isBlank())
                predicates.add(cb.equal(attributesJoin.get("value"), value));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Product> hasAttributeKey(String key) {
        return hasAttribute(key, null);
    }

    public static Specification<Product> hasMissingRequiredAttributes() {
        return (root, query, cb) -> {
            var attributesJoin = root.join("attributes", JoinType.LEFT);
            return cb.and(
                cb.equal(attributesJoin.get("required"), true),
                cb.or(
                    cb.isNull(attributesJoin.get("value")),
                    cb.equal(attributesJoin.get("value"), "")
                )
            );
        };
    }

    public static Specification<Product> hasVariants() {
        return (root, query, cb) -> cb.isNotEmpty(root.get("variants"));
    }

    public static Specification<Product> hasNoVariants() {
        return (root, query, cb) -> cb.isEmpty(root.get("variants"));
    }

    public static Specification<Product> hasMinimumVariants(int minCount) {
        return (root, query, cb) ->
            cb.greaterThanOrEqualTo(cb.size(root.get("variants")), minCount);
    }

    public static Specification<Product> hasImages() {
        return (root, query, cb) -> cb.isNotEmpty(root.get("images"));
    }

    public static Specification<Product> hasNoImages() {
        return (root, query, cb) -> cb.isEmpty(root.get("images"));
    }

    // ImageType enum used here
    public static Specification<Product> hasPrimaryImage() {
        return (root, query, cb) -> {
            var imagesJoin = root.join("images", JoinType.LEFT);
            return cb.equal(imagesJoin.get("imageType"), ImageType.PRIMARY); 
        };
    }

    public static Specification<Product> searchByKeyword(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) return cb.conjunction();
            String likePattern = "%" + keyword.toLowerCase() + "%";
            return cb.or(
                cb.like(cb.lower(root.get("name")), likePattern),
                cb.like(cb.lower(root.get("sku").get("value")), likePattern),
                cb.like(cb.lower(root.get("shortDescription")), likePattern),
                cb.like(cb.lower(root.get("longDescription")), likePattern),
                cb.like(cb.lower(root.get("searchKeywords")), likePattern),
                cb.like(cb.lower(root.get("brand")), likePattern)
            );
        };
    }

    public static Specification<Product> fullTextSearch(String searchTerm) {
        return (root, query, cb) -> {
            if (searchTerm == null || searchTerm.isBlank()) return cb.conjunction();
            String[] terms = searchTerm.toLowerCase().split("\\s+");
            List<Predicate> predicates = new ArrayList<>();
            for (String term : terms) {
                String likePattern = "%" + term + "%";
                predicates.add(cb.or(
                    cb.like(cb.lower(root.get("name")), likePattern),
                    cb.like(cb.lower(root.get("shortDescription")), likePattern),
                    cb.like(cb.lower(root.get("longDescription")), likePattern)
                ));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Product> isReadyToPublish() {
        return (root, query, cb) -> cb.and(
            cb.equal(root.get("status"), ProductStatus.READY),
            cb.isNotEmpty(root.get("images")),
            cb.isNotEmpty(root.get("prices"))
        );
    }

    public static Specification<Product> isIncomplete() {
        return (root, query, cb) -> cb.or(
            cb.isEmpty(root.get("images")),
            cb.isEmpty(root.get("prices")),
            cb.isNull(root.get("shortDescription"))
        );
    }

    @SafeVarargs
    public static Specification<Product> combine(Specification<Product>... specs) {
        Specification<Product> result = Specification.where(null);
        for (Specification<Product> spec : specs) {
            if (spec != null) result = result.and(spec);
        }
        return result;
    }

    public static Specification<Product> not(Specification<Product> spec) {
        return Specification.not(spec);
    }
}