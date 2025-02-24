package com.example.demo.productCatalog.service;

import com.example.demo.productCatalog.dto.AssetDTO;
import com.example.demo.productCatalog.entity.Asset;
import com.example.demo.productCatalog.repository.AssetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AssetService {

    private static final String UPLOAD_DIR = "uploads/";
    private final AssetRepository assetRepository;

    // Upload a new asset
    public AssetDTO uploadAsset(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        String extension = getFileExtension(file.getOriginalFilename());
        String filename = UUID.randomUUID() + "." + extension;

        // Extract byte array reading into a separate method
        byte[] fileBytes = readFileBytes(file);

        File uploadDir = new File(UPLOAD_DIR);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        Path filePath = Path.of(UPLOAD_DIR + filename);
        Files.write(filePath, fileBytes);

        // Extract URL formatting into a separate method
        String assetUrl = formatAssetUrl(filename);

        Asset asset = Asset.builder()
                .name(file.getOriginalFilename())
                .assetUrl(assetUrl)
                .build();

        Asset savedAsset = assetRepository.save(asset);
        return convertToDTO(savedAsset);
    }

    // Get asset by ID
    public Optional<AssetDTO> getAssetById(Long id) {
        return assetRepository.findById(id).map(this::convertToDTO);
    }

    // Get all assets
    public List<AssetDTO> getAllAssets() {
        return assetRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }


    // Get latest uploaded assets (limit N)
    public Page<AssetDTO> getLatestAssets(int limit) {
        return assetRepository.findLatestAssets(PageRequest.of(0, limit));
    }

    // Delete asset by ID
    public void deleteAsset(Long id) {
        assetRepository.deleteById(id);
    }

    private byte[] readFileBytes(MultipartFile file) throws IOException {
        return file.getBytes();
    }

    private String formatAssetUrl(String filename) {
        return "/assets/" + filename;
    }

    private String getFileExtension(String filename) {
        return filename.substring(filename.lastIndexOf(".") + 1);
    }

    private AssetDTO convertToDTO(Asset asset) {
        return new AssetDTO(asset.getId(),asset.getAssetUrl());
    }
}
