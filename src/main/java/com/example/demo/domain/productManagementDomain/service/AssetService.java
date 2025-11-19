package com.example.demo.domain.productManagementDomain.service;

import com.example.demo.common.base.service.AbstractBaseService;
import com.example.demo.domain.productManagementDomain.dto.AssetDTO;
import com.example.demo.domain.productManagementDomain.entity.Asset;
import com.example.demo.domain.productManagementDomain.entity.Product;
import com.example.demo.domain.productManagementDomain.mapper.AssetMapper;
import com.example.demo.domain.productManagementDomain.repository.AssetRepository;
import com.example.demo.domain.productManagementDomain.repository.ProductRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AssetService extends AbstractBaseService<Asset, Long> {

    private final ProductRepository productRepository;
    private final AssetMapper assetMapper;

    public AssetService(AssetRepository repository, ProductRepository productRepository, AssetMapper assetMapper) {
        super(repository);
        this.productRepository = productRepository;
        this.assetMapper = assetMapper;
    }

    @Override
    protected String getCacheName() {
        return "assets";
    }

    @Override
    protected Class<?> getEntityClass() {
        return Asset.class;
    }

    // -------------------------
    // Domain-specific methods
    // -------------------------

    /**
     * Upload a single file and associate it with a product.
     */
    @Transactional
    @CacheEvict(cacheNames = "assets", allEntries = true)
    public AssetDTO uploadAsset(MultipartFile file, Long productId) throws IOException {
        validateFile(file);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid product ID: " + productId));

        Asset asset = Asset.builder()
                .name(file.getOriginalFilename())
                .contentType(file.getContentType())
                .size(file.getSize())
                .data(file.getBytes())
                .product(product)
                .build();

        Asset savedAsset = save(asset);
        onAfterSave(savedAsset);
        return assetMapper.toDto(savedAsset);
    }

    /**
     * Upload multiple files and associate each with the same product.
     */
    @Transactional
    @CacheEvict(cacheNames = "assets", allEntries = true)
    public List<AssetDTO> uploadMultipleAssets(MultipartFile[] files, Long productId) throws IOException {
        if (files == null || files.length == 0) {
            throw new IllegalArgumentException("File list cannot be empty");
        }

        List<AssetDTO> results = new ArrayList<>();
        for (MultipartFile file : files) {
            results.add(uploadAsset(file, productId));
        }
        return results;
    }

    /**
     * Get all assets as DTOs.
     */
    public List<AssetDTO> getAllAssets() {
        return findAll().stream()
                .map(asset -> assetMapper.toDto(asset))
                .collect(Collectors.toList());
    }

    /**
     * Get all assets by product ID.
     */
    public List<AssetDTO> findAllAssetsByProductId(Long productId) {
        return ((AssetRepository) repository).findAssetsByProductId(productId);
    }

    /**
     * Get an asset by ID as DTO.
     */
    public Optional<AssetDTO> getAssetById(Long id) {
        return findById(id).map(asset -> assetMapper.toDto(asset));
    }

    /**
     * Get the latest uploaded assets with pagination.
     */
    public Page<AssetDTO> getLatestAssets(int limit) {
        return ((AssetRepository) repository).findLatestAssets(PageRequest.of(0, limit));
    }

    // -------------------------
    // Validation & Helpers
    // -------------------------

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }
        if (file.getOriginalFilename() == null || !file.getOriginalFilename().contains(".")) {
            throw new IllegalArgumentException("File must have a valid name with an extension");
        }
    }

    // Optional: future audit hook
    @Override
    protected void onAfterSave(Asset entity) {
        // e.g. trigger indexing or audit log
    }
}
