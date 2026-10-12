package com.example.demo.modules.product.domain.repository;

import com.example.demo.common.base.repository.BaseRepository;
import com.example.demo.modules.product.domain.model.ProductImage;
import com.example.demo.modules.product.domain.model.vo.ImageType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Product Image Repository
 *
 * Repository for ProductImage entity
 * Optimized for media management operations
 *
 * @author PIM Team
 * @version 1.0
 */
@Repository
public interface ProductImageRepository extends BaseRepository<ProductImage, UUID> {

    // ====================================
    // Find by Product
    // ====================================

    /**
     * Find all images for a product
     */
    @Query("SELECT i FROM ProductImage i WHERE i.product.id = :productId ORDER BY i.displayOrder, i.createdAt")
    List<ProductImage> findByProductId(@Param("productId") UUID productId);

    /**
     * Find images for product with pagination
     */
    @Query("SELECT i FROM ProductImage i WHERE i.product.id = :productId ORDER BY i.displayOrder, i.createdAt")
    Page<ProductImage> findByProductId(@Param("productId") UUID productId, Pageable pageable);

    /**
     * Count images for product
     */
    @Query("SELECT COUNT(i) FROM ProductImage i WHERE i.product.id = :productId")
    long countByProductId(@Param("productId") UUID productId);

    // ====================================
    // Find by Image Type
    // ====================================

    /**
     * Find images by product and type
     */
    @Query("SELECT i FROM ProductImage i WHERE i.product.id = :productId AND i.imageType = :imageType ORDER BY i.displayOrder")
    List<ProductImage> findByProductIdAndType(
        @Param("productId") UUID productId,
        @Param("imageType") ImageType imageType
    );

    /**
     * Find primary image for product
     */
    @Query("SELECT i FROM ProductImage i WHERE i.product.id = :productId AND i.imageType = 'PRIMARY'")
    Optional<ProductImage> findPrimaryByProductId(@Param("productId") UUID productId);

    /**
     * Find gallery images for product
     */
    @Query("SELECT i FROM ProductImage i WHERE i.product.id = :productId AND i.imageType = 'GALLERY' ORDER BY i.displayOrder")
    List<ProductImage> findGalleryByProductId(@Param("productId") UUID productId);

    /**
     * Find thumbnail for product
     */
    @Query("SELECT i FROM ProductImage i WHERE i.product.id = :productId AND i.imageType = 'THUMBNAIL'")
    Optional<ProductImage> findThumbnailByProductId(@Param("productId") UUID productId);

    /**
     * Find images by type
     */
    @Query("SELECT i FROM ProductImage i WHERE i.imageType = :imageType ORDER BY i.displayOrder")
    List<ProductImage> findByImageType(@Param("imageType") ImageType imageType);

    // ====================================
    // Find by File Information
    // ====================================

    /**
     * Find image by file path
     */
    @Query("SELECT i FROM ProductImage i WHERE i.storagePath = :filePath")
    Optional<ProductImage> findByFilePath(@Param("filePath") String filePath);

    /**
     * Find image by URL
     */
    @Query("SELECT i FROM ProductImage i WHERE i.url = :url")
    Optional<ProductImage> findByUrl(@Param("url") String url);

    /**
     * Find images by file name pattern
     */
    @Query("SELECT i FROM ProductImage i WHERE i.fileName LIKE :pattern")
    List<ProductImage> findByFileNameLike(@Param("pattern") String pattern);

    /**
     * Check if file path exists
     */
    @Query("SELECT CASE WHEN COUNT(i) > 0 THEN true ELSE false END FROM ProductImage i WHERE i.storagePath = :filePath")
    boolean existsByFilePath(@Param("filePath") String filePath);

    // ====================================
    // Find by Upload Date
    // ====================================

    /**
     * Find images uploaded between dates
     */
    @Query("SELECT i FROM ProductImage i WHERE i.createdAt BETWEEN :startDate AND :endDate")
    List<ProductImage> findByUploadedBetween(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    /**
     * Find recently uploaded images
     */
    @Query("SELECT i FROM ProductImage i WHERE i.createdAt >= :since ORDER BY i.createdAt DESC")
    List<ProductImage> findRecentlyUploaded(@Param("since") LocalDateTime since);

    // ====================================
    // Validation
    // ====================================

    /**
     * Check if product has primary image
     */
    @Query("SELECT CASE WHEN COUNT(i) > 0 THEN true ELSE false END " +
           "FROM ProductImage i WHERE i.product.id = :productId AND i.imageType = 'PRIMARY'")
    boolean hasPrimaryImage(@Param("productId") UUID productId);

    /**
     * Check if product has any images
     */
    @Query("SELECT CASE WHEN COUNT(i) > 0 THEN true ELSE false END " +
           "FROM ProductImage i WHERE i.product.id = :productId")
    boolean hasImages(@Param("productId") UUID productId);

    /**
     * Find products without images
     */
    @Query("SELECT p.id FROM Product p WHERE p.id NOT IN (SELECT DISTINCT i.product.id FROM ProductImage i)")
    Set<UUID> findProductIdsWithoutImages();

    /**
     * Find products without primary image
     */
    @Query("SELECT DISTINCT i.product.id FROM ProductImage i " +
           "WHERE i.product.id NOT IN (SELECT pi.product.id FROM ProductImage pi WHERE pi.imageType = 'PRIMARY')")
    Set<UUID> findProductIdsWithoutPrimaryImage();

    // ====================================
    // Statistics
    // ====================================

    /**
     * Count total images
     */
    @Query("SELECT COUNT(i) FROM ProductImage i")
    long countAll();

    /**
     * Count by image type
     */
    @Query("SELECT COUNT(i) FROM ProductImage i WHERE i.imageType = :imageType")
    long countByImageType(@Param("imageType") ImageType imageType);

    /**
     * Count primary images
     */
    @Query("SELECT COUNT(i) FROM ProductImage i WHERE i.imageType = 'PRIMARY'")
    long countPrimaryImages();

    /**
     * Get total file size
     */
    @Query("SELECT SUM(i.fileSize) FROM ProductImage i")
    Long getTotalFileSize();

    /**
     * Get total file size by product
     */
    @Query("SELECT SUM(i.fileSize) FROM ProductImage i WHERE i.product.id = :productId")
    Long getTotalFileSizeByProductId(@Param("productId") UUID productId);

    // ====================================
    // Bulk Operations
    // ====================================

    /**
     * Find images by product IDs
     */
    @Query("SELECT i FROM ProductImage i WHERE i.product.id IN :productIds ORDER BY i.displayOrder")
    List<ProductImage> findByProductIdIn(@Param("productIds") List<UUID> productIds);

    /**
     * Find primary images for multiple products
     */
    @Query("SELECT i FROM ProductImage i WHERE i.product.id IN :productIds AND i.imageType = 'PRIMARY'")
    List<ProductImage> findPrimaryByProductIdIn(@Param("productIds") List<UUID> productIds);

    /**
     * Delete images by product ID
     */
    @Query("DELETE FROM ProductImage i WHERE i.product.id = :productId")
    void deleteByProductId(@Param("productId") UUID productId);

    /**
     * Delete images by file path
     */
    @Query("DELETE FROM ProductImage i WHERE i.storagePath IN :filePaths")
    void deleteByFilePathIn(@Param("filePaths") List<String> filePaths);

    // ====================================
    // Storage Management
    // ====================================

    /**
     * Find orphaned images (images without product)
     */
    @Query("SELECT i FROM ProductImage i WHERE i.product IS NULL")
    List<ProductImage> findOrphanedImages();

    /**
     * Find duplicate images by file path
     */
    @Query("SELECT i.storagePath, COUNT(i) FROM ProductImage i GROUP BY i.storagePath HAVING COUNT(i) > 1")
    List<Object[]> findDuplicateFilePaths();

    /**
     * Find large images (above size threshold)
     */
    @Query("SELECT i FROM ProductImage i WHERE i.fileSize > :sizeThreshold ORDER BY i.fileSize DESC")
    List<ProductImage> findLargeImages(@Param("sizeThreshold") Long sizeThreshold);

    /**
     * Find images by product ID ordered by display order
     */
    @Query("SELECT i FROM ProductImage i WHERE i.product.id = :productId ORDER BY i.displayOrder ASC")
    List<ProductImage> findByProductIdOrderByDisplayOrderAsc(UUID productId);

    /**
     * Find images by product ID and image type
     */
    @Query("SELECT i FROM ProductImage i WHERE i.product.id = :productId AND i.imageType = :imageType")
    List<ProductImage> findByProductIdAndImageType(UUID productId, ImageType imageType);
    
    /**
     * Find image by product ID and URL
     */
    @Query("SELECT i FROM ProductImage i WHERE i.product.id = :productId AND i.url = :url")
    Optional<ProductImage> findByProductIdAndUrl(UUID productId, String url);

    /**
     * Check if product has images of a specific type
     */
    @Query("SELECT CASE WHEN COUNT(i) > 0 THEN true ELSE false END " +
           "FROM ProductImage i WHERE i.product.id = :productId AND i.imageType = :imageType")
    boolean existsByProductId(UUID productId);

    /**
     * Check if product has images of a specific type
     */
    @Query("SELECT CASE WHEN COUNT(i) > 0 THEN true ELSE false END " +
           "FROM ProductImage i WHERE i.product.id = :productId AND i.imageType = :imageType")
    boolean existsByProductIdAndImageType(UUID productId, ImageType imageType);

    /**
     * Count images by product ID and image type
     */
    @Query("SELECT COUNT(i) FROM ProductImage i WHERE i.product.id = :productId AND i.imageType = :imageType")
    long countByProductIdAndImageType(UUID productId, ImageType imageType);

    /**
     * Find images by file name containing text
     */
    @Query("SELECT i FROM ProductImage i WHERE LOWER(i.fileName) LIKE LOWER(CONCAT('%', :fileName, '%'))")
    List<ProductImage> findByFileNameContainingIgnoreCase(String fileName);

    /**
     * Find images by product ID and file size greater than
     */
    @Query("SELECT i FROM ProductImage i WHERE i.product.id = :productId AND i.fileSize > :minFileSize")
    List<ProductImage> findByProductIdAndFileSizeGreaterThan(UUID productId, long minFileSize);

    /**
     * Find images by alt text containing text
     */
    @Query("SELECT i FROM ProductImage i WHERE LOWER(i.altText) LIKE LOWER(CONCAT('%', :altText, '%'))")
    List<ProductImage> findByAltTextContainingIgnoreCase(String altText);
}