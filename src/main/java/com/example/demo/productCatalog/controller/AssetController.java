package com.example.demo.productCatalog.controller;

import com.example.demo.productCatalog.dto.AssetDTO;
import com.example.demo.productCatalog.service.AssetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/assets")
@RequiredArgsConstructor
public class AssetController {

    private final AssetService assetService;

    @PostMapping("/upload")
    public ResponseEntity<AssetDTO> uploadAsset(@RequestParam("file") MultipartFile file) throws IOException {
        return ResponseEntity.ok(assetService.uploadAsset(file));
    }

    @GetMapping
    public ResponseEntity<List<AssetDTO>> getAllAssets() {
        return ResponseEntity.ok(assetService.getAllAssets());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AssetDTO> getAssetById(@PathVariable Long id) {
        Optional<AssetDTO> asset = assetService.getAssetById(id);
        return asset.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

//    needs to be implemented
//    to check all the latest saved assets
//    @GetMapping("/latest")
//    public ResponseEntity<List<AssetDTO>> getLatestAssets(@RequestParam(defaultValue = "5") int limit) {
//        return ResponseEntity.ok(assetService.getLatestAssets(limit));
//    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAsset(@PathVariable Long id) {
        assetService.deleteAsset(id);
        return ResponseEntity.noContent().build();
    }
}
