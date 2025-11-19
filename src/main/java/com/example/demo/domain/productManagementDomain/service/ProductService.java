package com.example.demo.domain.productManagementDomain.service;

import com.example.demo.common.base.service.AbstractBaseService;
import com.example.demo.domain.productManagementDomain.dto.*;
import com.example.demo.domain.productManagementDomain.entity.*;
import com.example.demo.domain.productManagementDomain.mapper.CategoryMapper;
import com.example.demo.domain.productManagementDomain.mapper.ProductMapper;
import com.example.demo.domain.productManagementDomain.repository.*;
import com.example.demo.domain.productManagementDomain.handler.ProductEntityHandler;
import com.example.demo.domain.productManagementDomain.projection.ProductProjection;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class ProductService extends AbstractBaseService<Product, Long> {

    private final ProductRepository productRepository;
    private final CategoryService categoryService;
    private final CategoryMapper categoryMapper;
    private final ProductAttributeService attributeService;
    private final PricingService pricingService;
    private final AssetService assetService;
    private final ProductMapper productMapper;

    protected ProductService(ProductRepository productRepository, CategoryService categoryService, CategoryMapper categoryMapper, ProductAttributeService attributeService, PricingService pricingService, AssetService assetService, ProductMapper productMapper) {
        super(productRepository);
        this.productRepository = productRepository;
        this.categoryService = categoryService;
        this.categoryMapper = categoryMapper;
        this.attributeService = attributeService;
        this.pricingService = pricingService;
        this.assetService = assetService;
        this.productMapper = productMapper;
    }

    @Override
    protected String getCacheName() {
        return "products";
    }

    @Override
    protected Class<?> getEntityClass() {
        return Product.class;
    }

    // ───────────────────────────────
    // CREATE
    // ───────────────────────────────

    public ProductDTO createProduct(ProductDTO dto) {
        if (productRepository.existsByName(dto.getName())) {
            throw new DuplicateEntityException("Product with name '" + dto.getName() + "' already exists");
        }

        CategoryDTO categoryDTO = categoryService.getCategoryById(dto.getCategoryId());
        Product entity = productMapper.toEntity(dto);
        entity.setCategory(categoryMapper.toEntity(categoryDTO));

        Product saved = save(entity);
        log.info("Created product: {}", saved.getName());
        return productMapper.toDto(saved);
    }

    // ───────────────────────────────
    // DELETE
    // ───────────────────────────────

    public void deleteProduct(Long productId, boolean deleteAssets, boolean keepAttributes) {
        Product product = findByIdOrThrow(productId);

        if (deleteAssets) {
            assetService.findAllAssetsByProductId(productId).forEach(asset ->  assetService.delete(asset.getId()));
        }

        // Pricing cleanup
        pricingService.getPricingsByProductId(productId)
                .forEach(pricing -> pricingService.delete(pricing.getId()));

        // Attributes cleanup
        if (!keepAttributes) {
            attributeService.findAllByProductAndLang(productId, "en")
                    .forEach(attr -> attributeService.delete(attr.getId()));
        }

        delete(productId);
        log.info("🗑️ Product {} deleted (deleteAssets={}, keepAttributes={})", productId, deleteAssets, keepAttributes);
    }

    // ───────────────────────────────
    // PUBLISH
    // ───────────────────────────────

    public ProductDTO publishProduct(Long productId) {
        Product product = findByIdOrThrow(productId);

        if (!product.canBePublished()) {
            throw new InvalidStateException("Product cannot be published. Missing attributes, pricing, or assets.");
        }

        product.setPublished(true);
        Product saved = save(product);
        log.info("📦 Product published: {}", saved.getName());
        return productMapper.toDto(saved);
    }

    // ───────────────────────────────
    // SEARCH & RETRIEVE
    // ───────────────────────────────

    @Transactional(readOnly = true)
    public Page<ProductProjection> searchProductsByName(String name, Pageable pageable) {
        String namePattern = "%" + name.chars()
                .mapToObj(c -> "%" + (char) c + "%")
                .collect(Collectors.joining());
        return productRepository.findByNameLikeIgnoreCase(namePattern, pageable);
    }

    @Transactional(readOnly = true)
    public ProductDetailsDTO getProductDetails(Long id) {
        ProductProjection projection = productRepository.findProductProjectionById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with ID " + id));

        CategoryDTO category = categoryService.getCategoryById(projection.getCategoryId());
        ProductPricingDTO pricing = pricingService
                .getPricingByProductIdAndCurrency(projection.getId(), "EUR")
                .orElseThrow(() -> new EntityNotFoundException("Pricing not found for product " + id));
        List<ProductAttributeDTO> attributes = attributeService.findAllByProductAndLang(id, "en");
        List<AssetDTO> assets = assetService.findAllAssetsByProductId(id);

        return ProductDetailsDTO.builder()
                .id(projection.getId())
                .name(projection.getName())
                .stockQuantity(projection.getStockQuantity())
                .productCategory(category)
                .productPricing(pricing)
                .productAttributes(attributes)
                .productAssets(assets)
                .build();
    }

    @Transactional(readOnly = true)
    public Page<ProductCatalogDTO> getProducts(Pageable pageable) {
        Page<ProductProjection> projections = productRepository.findAllProductProjection(pageable);
        List<Long> productIds = projections.stream().map(ProductProjection::getId).toList();

        Map<Long, ProductPricingDTO> pricingMap = pricingService.getPricingByProductIds(productIds, "EUR");
        Map<Long, CategoryDTO> categoryMap = categoryService.getCategoryDTOMapByIds(productIds, true);

        List<ProductCatalogDTO> dtos = (List<ProductCatalogDTO>) projections.map(p ->
                ProductCatalogDTO.builder()
                        .id(p.getId())
                        .name(p.getName())
                        .stockQuantity(p.getStockQuantity())
                        .productCategory(categoryMap.get(p.getId()))
                        .productPricing(pricingMap.get(p.getId()))
                        .build()
        ).toList();

        return new PageImpl<>(dtos, pageable, projections.getTotalElements());
    }

    // ───────────────────────────────
    // ENTITY ASSIGNMENT HELPERS
    // ───────────────────────────────

    @Transactional
    public <T> void assignEntitiesToProduct(Long productId, List<Long> entityIds, ProductEntityHandler<T> handler) {
        Product product = findByIdOrThrow(productId);
        List<T> entities = handler.getRepository().findAllById(entityIds);
        if (entities.isEmpty()) throw new EntityNotFoundException("No entities found for provided IDs");

        Collection<T> collection = handler.getCollection(product);
        entities.forEach(entity -> {
            if (!collection.contains(entity)) handler.addToCollection(product, entity);
        });

        save(product);
    }

    @Transactional
    public <T> void unassignEntitiesFromProduct(Long productId, List<Long> entityIds, ProductEntityHandler<T> handler) {
        Product product = findByIdOrThrow(productId);
        List<T> entities = handler.getRepository().findAllById(entityIds);
        if (entities.isEmpty()) throw new EntityNotFoundException("No entities found for provided IDs");

        Collection<T> collection = handler.getCollection(product);
        entities.forEach(entity -> {
            if (collection.contains(entity)) handler.removeFromCollection(product, entity);
        });

        save(product);
        handler.getRepository().saveAll(entities);
    }

    // ───────────────────────────────
    // HOOKS
    // ───────────────────────────────

    @Override
    protected void onAfterSave(Product entity) {
        log.debug("Product {} saved (published={})", entity.getName(), entity.isPublished());
    }

    @Override
    protected void onAfterDelete(Long id) {
        log.debug("Product {} deleted from system", id);
    }
}

