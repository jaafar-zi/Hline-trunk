package com.example.demo.productCatalog.service;

import com.example.demo.productCatalog.dto.*;
import com.example.demo.productCatalog.entity.*;
import com.example.demo.productCatalog.handler.ProductEntityHandler;
import com.example.demo.productCatalog.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ProductService {

    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private ProductAttributeService attributeService;
    @Autowired
    private PricingService pricingService;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private AssetRepository assetRepository;
    @Autowired
    private PricingRepository pricingRepository;
    @Autowired
    private ProductAttributeRepository productAttributeRepository;


    public ProductDTO getProductById(Long id) {
        Optional<Product> product = productRepository.findById(id);
        if (product.isPresent()) {
            return mapToDTO(product.get());
        }
        else {
            throw new EntityNotFoundException("Product with id " + id + " not found");
        }
    }

    public ProductDTO createProduct(Product productDTO) {
        // Check if product name already exists (ensure index on 'name' for efficiency)
        if (productRepository.existsByName(productDTO.getName())) {
            throw new RuntimeException("Product with the same name already exists");
        }

        // Retrieve category with optimized error handling and avoid redundant 'get()'
        Category category = categoryRepository.findById(productDTO.getCategory().getId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        // Directly create and save the product, avoid unnecessary intermediate variables
        Product savedProduct = productRepository.save(Product.builder()
                .name(productDTO.getName())
                .stockQuantity(productDTO.getStockQuantity())
                .category(category)
                .build());

        log.info("Created new product: {}", savedProduct);

        // Map to DTO and return
        return mapToDTO(savedProduct);
    }


    public void deleteProductWithConditionalAssets(Long productId, boolean deleteAssets) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (deleteAssets) {
            for (Asset asset : product.getAssets()) {
                    assetRepository.delete(asset);
            }
        }

        for (Pricing pricing : product.getPricingList()) {
            if (pricingRepository.countByProductId(pricing.getId()) == 1) {
                pricingRepository.delete(pricing);
            }
        }

        for (ProductAttribute attribute : product.getAttributes()) {
            if (productAttributeRepository.countByProductId(attribute.getId()) == 1) {
                productAttributeRepository.delete(attribute);
            }
        }

        product.getPricingList().clear();
        product.getAttributes().clear();

        productRepository.delete(product);
    }

    public void deleteProductKeepAssetsAndAttributes(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        for (Pricing pricing : product.getPricingList()) {
            if (pricingRepository.countByProductId(pricing.getId()) == 1) {
                pricingRepository.delete(pricing);
            }
        }

        for (ProductAttribute attribute : product.getAttributes()) {
            if (productAttributeRepository.countByProductId(attribute.getId()) == 1) {
                productAttributeRepository.delete(attribute);
            }
        }

        productRepository.delete(product);
    }



    public ProductDTO publishProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // Check if all necessary attributes, pricing, and assets are set before publishing
        if (product.canBePublished()) {
            product.setPublished(true);
            productRepository.save(product);
            log.info("Product published: {}", product);
        } else {
            throw new RuntimeException("Product cannot be published. Not all attributes, pricing, or assets are set.");
        }

        return mapToDTO(product);
    }


    public Page<ProductProjection> searchProductsByAnyCharacterInName(String name, Pageable pageable) {

        String namePattern = "%" + name.chars()
                .mapToObj(c -> "%" + (char) c + "%")
                .collect(Collectors.joining());

        return productRepository.findByNameLikeIgnoreCase(namePattern, pageable);
    }

    public Page<ProductCatalogDTO> getProducts(Pageable pageable) {
        Page<ProductProjection> products = productRepository.findAllProductProjection(pageable);

        List<Long> productIds = products.stream().map(ProductProjection::getId).toList();

        Map<Long, List<ProductAttributeDTO>> attributesMap = attributeService.getPricingByProductIdsAsMap(productIds, "fr");
        Map<Long, ProductPricingDTO> pricingMap = pricingService.getPricingByProductIds(productIds, "USD");

        List<ProductCatalogDTO> dtos = products.map(projection ->
                ProductCatalogDTO.builder()
                        .id(projection.getId())
                        .name(projection.getName())
                        .stockQuantity(projection.getStockQuantity())
                        .productPricing(pricingMap.get(projection.getId()))
                        .build()
        ).toList();

        return new PageImpl<>(dtos, pageable, products.getTotalElements());
    }

    @Transactional
    public <T> void assignEntitiesToProduct(
            Long productId,
            List<Long> entityIds,
            ProductEntityHandler<T> handler) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        List<T> entities = handler.getRepository().findAllById(entityIds);
        if (entities.isEmpty()) {
            throw new RuntimeException("No entities found");
        }

        Collection<T> productCollection = handler.getCollection(product);
        entities.forEach(entity -> {
            if (!productCollection.contains(entity)) {
                handler.addToCollection(product, entity);
            }
        });

        productRepository.save(product);
    }

    @Transactional
    public <T> void unassignEntitiesFromProduct(
            Long productId,
            List<Long> entityIds,
            ProductEntityHandler<T> handler) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        List<T> entities = handler.getRepository().findAllById(entityIds);
        if (entities.isEmpty()) {
            throw new RuntimeException("No entities found");
        }

        Collection<T> productCollection = handler.getCollection(product);
        entities.forEach(entity -> {
            if (productCollection.contains(entity)) {
                handler.removeFromCollection(product, entity);
            }
        });

        productRepository.save(product);
        handler.getRepository().saveAll(entities);
    }

    private ProductDTO mapToDTO(Product product) {
        return new ProductDTO(
                product.getName(),
                product.getStockQuantity(),
                product.getCategory().getId()
        );
    }


}
