package com.example.demo.domain.productManagementDomain.service;

import com.example.demo.domain.productManagementDomain.dto.AssetDTO;
import com.example.demo.domain.productManagementDomain.entity.Asset;
import com.example.demo.domain.productManagementDomain.entity.Product;
import com.example.demo.domain.productManagementDomain.repository.AssetRepository;
import com.example.demo.domain.productManagementDomain.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AssetService {

    private final AssetRepository assetRepository;
    private final ProductRepository productRepository;

    /**
     * Upload a single file and associate it with a product.
     *
     * @param file      The file to upload.
     * @param productId The associated product ID.
     * @return Saved AssetDTO.
     * @throws IOException if file read fails.
     */
    @Transactional
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

        Asset savedAsset = assetRepository.save(asset);
        return convertToDTO(savedAsset);
    }

    /**
     * Upload multiple files and associate each with the same product.
     *
     * @param files     The files to upload.
     * @param productId The associated product ID.
     * @return List of saved AssetDTOs.
     * @throws IOException if any file read fails.
     */
    @Transactional
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
        return assetRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get all assets by product ID.
     */
    public List<AssetDTO> findAllAssetsByProductId(Long productId) {
        return assetRepository.findAssetsByProductId(productId);
    }

    /**
     * Get an asset by ID as DTO.
     */
    public Optional<AssetDTO> getAssetById(Long id) {
        return assetRepository.findById(id).map(this::convertToDTO);
    }

    /**
     * Get the latest uploaded assets with pagination.
     */
    public Page<AssetDTO> getLatestAssets(int limit) {
        return assetRepository.findLatestAssets(PageRequest.of(0, limit));
    }

    /**
     * Delete an asset by ID.
     */
    public void deleteAsset(Long id) {
        assetRepository.deleteById(id);
    }

    /**
     * Validate the uploaded file.
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }
        if (file.getOriginalFilename() == null || !file.getOriginalFilename().contains(".")) {
            throw new IllegalArgumentException("File must have a valid name with an extension");
        }
    }

    /**
     * Convert Asset entity to DTO.
     */
    private AssetDTO convertToDTO(Asset asset) {
        return new AssetDTO(
                asset.getId(),
                asset.getName(),
                asset.getContentType(),
                asset.getSize(),
                asset.getProduct().getId()
        );
    }
}
