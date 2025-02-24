package com.example.demo.productCatalog.repository;

import com.example.demo.productCatalog.dto.AssetDTO;
import com.example.demo.productCatalog.entity.Asset;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AssetRepository extends JpaRepository<Asset, Long> {

    // Fetch a single Asset by ID and map it to AssetDTO
    @Query("SELECT COUNT(a) FROM Asset a WHERE a.id = :assetId")
    int countByAssetId(@Param("assetId") Long assetId);

    // Fetch all Assets mapped to AssetDTO
    @Query("SELECT new com.example.demo.productCatalog.dto.AssetDTO(a.id, a.assetUrl) FROM Asset a")
    List<AssetDTO> findAllAssets();

    // Fetch Assets associated with a product
    @Query("SELECT new com.example.demo.productCatalog.dto.AssetDTO(a.id, a.assetUrl) FROM Asset a WHERE a.product.id = :productId")
    List<AssetDTO> findAssetsByProductId(@Param("productId") Long productId);

    @Query("SELECT new com.example.demo.productCatalog.dto.AssetDTO(a.id, a.assetUrl) FROM Asset a ORDER BY a.createdAt DESC")
    Page<AssetDTO> findLatestAssets(Pageable pageable);

}
