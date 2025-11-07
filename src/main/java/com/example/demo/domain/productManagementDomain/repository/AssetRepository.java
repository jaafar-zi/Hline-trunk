package com.example.demo.domain.productManagementDomain.repository;

import com.example.demo.domain.productManagementDomain.dto.AssetDTO;
import com.example.demo.domain.productManagementDomain.entity.Asset;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AssetRepository extends JpaRepository<Asset, Long> {

    /**
     * Fetches the count of assets by the given asset ID.
     *
     * @param assetId The ID of the asset to search for.
     * @return The count of assets matching the given asset ID.
     */
    @Query("SELECT COUNT(a) FROM Asset a WHERE a.id = :assetId")
    int countByAssetId(@Param("assetId") Long assetId);

    /**
     * Fetches all assets from the database and maps them to AssetDTO.
     * The AssetDTO contains id, name, contentType, size, and productId.
     *
     * @return A list of AssetDTO objects.
     */
    @Query("""
        SELECT new com.example.demo.productManagementDomain.dto.AssetDTO(
            a.id, a.name, a.contentType, a.size, a.product.id
        )
        FROM Asset a
    """)
    List<AssetDTO> findAllAssets();

    /**
     * Fetches all assets associated with a specific product ID and maps them to AssetDTO.
     *
     * @param productId The ID of the product.
     * @return A list of AssetDTOs related to the product.
     */
    @Query("""
        SELECT new com.example.demo.productManagementDomain.dto.AssetDTO(
            a.id, a.name, a.contentType, a.size, a.product.id
        )
        FROM Asset a
        WHERE a.product.id = :productId
    """)
    List<AssetDTO> findAssetsByProductId(@Param("productId") Long productId);

    /**
     * Fetches the most recent assets ordered by creation date in descending order.
     * Pagination is supported through Pageable.
     *
     * @param pageable The pagination settings.
     * @return A page of AssetDTOs.
     */
    @Query("""
        SELECT new com.example.demo.productManagementDomain.dto.AssetDTO(
            a.id, a.name, a.contentType, a.size, a.product.id
        )
        FROM Asset a
        ORDER BY a.createdAt DESC
    """)
    Page<AssetDTO> findLatestAssets(Pageable pageable);
}
