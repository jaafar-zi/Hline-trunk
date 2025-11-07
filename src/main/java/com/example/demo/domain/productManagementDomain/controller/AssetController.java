package com.example.demo.domain.productManagementDomain.controller;

import com.example.demo.domain.productManagementDomain.dto.AssetDTO;
import com.example.demo.domain.productManagementDomain.repository.AssetRepository;
import com.example.demo.domain.productManagementDomain.service.AssetService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/assets")
@RequiredArgsConstructor
public class AssetController {

    private final AssetService assetService;
    private final AssetRepository assetRepository;

    @PostMapping("/upload")
    public ResponseEntity<AssetDTO> uploadAsset(
            @RequestParam("file") MultipartFile file,
            @RequestParam("productId") Long productId
    ) throws IOException {
        return ResponseEntity.ok(assetService.uploadAsset(file, productId));
    }

    @PostMapping("/upload/multiple")
    public ResponseEntity<List<AssetDTO>> uploadMultipleAssets(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam("productId") Long productId
    ) throws IOException {
        List<AssetDTO> uploadedAssets = new ArrayList<>();
        for (MultipartFile file : files) {
            uploadedAssets.add(assetService.uploadAsset(file, productId));
        }
        return ResponseEntity.ok(uploadedAssets);
    }

    @GetMapping
    public ResponseEntity<List<AssetDTO>> getAllAssets() {
        return ResponseEntity.ok(assetService.getAllAssets());
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<AssetDTO>> getAssetsByProductId(@PathVariable Long productId) {
        return ResponseEntity.ok(assetService.findAllAssetsByProductId(productId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AssetDTO> getAssetById(@PathVariable Long id) {
        return assetService.getAssetById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadAsset(@PathVariable Long id) {
        return assetRepository.findById(id)
                .map(asset -> {
                    Resource resource = new ByteArrayResource(asset.getData());

                    String safeFilename = asset.getName()
                            .replaceAll("[^\\x20-\\x7E]", "")
                            .replaceAll("\\s+", "_");

                    MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
                    if (asset.getContentType() != null) {
                        mediaType = MediaType.parseMediaType(asset.getContentType());
                    }

                    ResponseEntity<Resource> body = ResponseEntity.ok()
                            .contentType(mediaType)
                            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + safeFilename + "\"")
                            .contentLength(asset.getSize())
                            .body(resource);
                    return body;
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAsset(@PathVariable Long id) {
        assetService.deleteAsset(id);
        return ResponseEntity.noContent().build();
    }
}
