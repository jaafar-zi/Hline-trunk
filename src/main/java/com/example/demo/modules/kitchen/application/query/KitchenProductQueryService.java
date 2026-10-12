package com.example.demo.modules.kitchen.application.query;

import com.example.demo.common.base.service.query.AbstractQueryService;
import com.example.demo.common.exception.EntityNotFoundException;
import com.example.demo.common.normalization.NormalizationService;
import com.example.demo.modules.kitchen.domain.model.KitchenProduct;
import com.example.demo.modules.kitchen.domain.model.KitchenProductVariant;
import com.example.demo.modules.kitchen.domain.model.vo.AssemblyType;
import com.example.demo.modules.kitchen.domain.repository.KitchenProductRepository;
import com.example.demo.modules.kitchen.domain.repository.KitchenProductVariantRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Kitchen Product Query Service
 */
@Slf4j
@Service
@Transactional(readOnly = true)
public class KitchenProductQueryService extends AbstractQueryService<KitchenProduct, UUID> {

    private final KitchenProductRepository productRepository;
    private final KitchenProductVariantRepository variantRepository;

    public KitchenProductQueryService(
            KitchenProductRepository productRepository,
            KitchenProductVariantRepository variantRepository,
            NormalizationService normalizationService) {
        super(productRepository, normalizationService);
        this.productRepository = productRepository;
        this.variantRepository = variantRepository;
    }

    // ====================================
    // Product - Find by URL path
    // ====================================

    /**
     * Full URL lookup: /kitchens/{rangeSlug}/{collectionSlug}/{productSlug}
     */
    @Cacheable(cacheNames = "kitchen-products",
               key = "'path:' + #rangeSlug + ':' + #collectionSlug + ':' + #productSlug")
    public KitchenProduct findByPath(
            String rangeSlug,
            String collectionSlug,
            String productSlug) {

        return productRepository.findByFullPathWithAll(rangeSlug, collectionSlug, productSlug)
            .orElseThrow(() -> new EntityNotFoundException(
                KitchenProduct.class,
                rangeSlug + "/" + collectionSlug + "/" + productSlug
            ));
    }

    // ====================================
    // Product - Find by Collection
    // ====================================

    @Cacheable(cacheNames = "kitchen-products", key = "'collection:' + #collectionId")
    public List<KitchenProduct> findActiveByCollection(UUID collectionId) {
        return productRepository.findActiveByCollectionId(collectionId);
    }

    @Cacheable(cacheNames = "kitchen-products",
               key = "'collection-variants:' + #collectionId")
    public List<KitchenProduct> findActiveByCollectionWithVariants(UUID collectionId) {
        return productRepository.findActiveByCollectionIdWithVariants(collectionId);
    }

    @Cacheable(cacheNames = "kitchen-products",
               key = "'collection-cat:' + #collectionId + ':' + #categoryId")
    public List<KitchenProduct> findByCollectionAndCategory(
            UUID collectionId,
            UUID categoryId) {
        return productRepository.findByCollectionIdAndCategoryId(collectionId, categoryId);
    }

    // ====================================
    // Product - Find Popular
    // ====================================

    @Cacheable(cacheNames = "kitchen-products", key = "'popular:' + #collectionId")
    public List<KitchenProduct> findPopularByCollection(UUID collectionId) {
        return productRepository.findPopularByCollectionId(collectionId);
    }

    @Cacheable(cacheNames = "kitchen-products",
               key = "'popular-cat:' + #collectionId + ':' + #categoryId")
    public List<KitchenProduct> findPopularByCollectionAndCategory(
            UUID collectionId,
            UUID categoryId) {
        return productRepository.findPopularByCollectionIdAndCategoryId(collectionId, categoryId);
    }

    // ====================================
    // Product - Find by SKU
    // ====================================

    @Cacheable(cacheNames = "kitchen-products", key = "'sku:' + #sku")
    public Optional<KitchenProduct> findBySku(String sku) {
        return productRepository.findBySku(sku);
    }

    @Cacheable(cacheNames = "kitchen-products", key = "'full:' + #id")
    public KitchenProduct findByIdWithAll(UUID id) {
        return productRepository.findByIdWithAll(id)
            .orElseThrow(() -> new EntityNotFoundException(KitchenProduct.class, id));
    }

    // ====================================
    // Product - Search
    // ====================================

    public Page<KitchenProduct> searchByCollection(
            UUID collectionId,
            String searchTerm,
            Pageable pageable) {
        return productRepository.searchByCollection(collectionId, searchTerm, pageable);
    }

    // ====================================
    // Variant - Colour Resolver (Key Feature)
    // ====================================

    /**
     * Resolve variants for a product given a cabinet colour.
     * This is called when the global colour changes.
     */
    @Cacheable(cacheNames = "kitchen-product-variants",
               key = "'resolve:' + #productId + ':' + #colourId")
    public List<KitchenProductVariant> resolveVariantsByColour(
            UUID productId,
            UUID colourId) {
        return variantRepository.findByProductIdAndCabinetColourId(productId, colourId);
    }

    /**
     * Resolve exact variant for product + colour + assembly type.
     */
    @Cacheable(cacheNames = "kitchen-product-variants",
               key = "'resolve-exact:' + #productId + ':' + #colourId + ':' + #assemblyType")
    public Optional<KitchenProductVariant> resolveExactVariant(
            UUID productId,
            UUID colourId,
            AssemblyType assemblyType) {
        return variantRepository.findByProductIdAndCabinetColourIdAndAssemblyType(
            productId, colourId, assemblyType
        );
    }

    // ====================================
    // Variant - Find by Product
    // ====================================

    @Cacheable(cacheNames = "kitchen-product-variants", key = "'product:' + #productId")
    public List<KitchenProductVariant> findActiveVariantsByProduct(UUID productId) {
        return variantRepository.findActiveByProductId(productId);
    }

    @Cacheable(cacheNames = "kitchen-product-variants",
               key = "'product-colour:' + #productId")
    public List<KitchenProductVariant> findVariantsWithColour(UUID productId) {
        return variantRepository.findByProductIdWithColour(productId);
    }

    public Optional<KitchenProductVariant> findDefaultVariant(UUID productId) {
        return variantRepository.findDefaultByProductId(productId);
    }

    // ====================================
    // Statistics
    // ====================================

    public long countActiveByCollection(UUID collectionId) {
        return productRepository.countActiveByCollectionId(collectionId);
    }

    public long countByCollectionAndCategory(UUID collectionId, UUID categoryId) {
        return productRepository.countByCollectionIdAndCategoryId(collectionId, categoryId);
    }

    public java.math.BigDecimal findMinPriceByCollection(UUID collectionId) {
        return productRepository.findMinPriceByCollectionId(collectionId);
    }

    // ====================================
    // AbstractQueryService
    // ====================================

    @Override
    protected String getEntityCacheName() {
        return "kitchen-products";
    }

    @Override
    protected Class<KitchenProduct> getEntityClass() {
        return KitchenProduct.class;
    }
}