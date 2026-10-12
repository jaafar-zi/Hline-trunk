package com.example.demo.modules.product.interfaces;

import com.example.demo.modules.product.application.command.ProductImageCommandService;
import com.example.demo.modules.product.application.dto.request.AddProductImageRequest;
import com.example.demo.modules.product.application.dto.response.ProductImageResponse;
import com.example.demo.modules.product.application.query.ProductImageQueryService;
import com.example.demo.modules.product.domain.model.vo.ImageType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Product Image REST Controller
 * 
 * Handles all HTTP requests for product image operations
 * Implements CQRS pattern with separate command and query services
 * 
 * @author PIM Team
 * @version 1.0
 */
@Slf4j
@RestController
@RequestMapping("/products/{productId}/images")
@RequiredArgsConstructor
@Validated
@Tag(name = "Product Images", description = "Product image management endpoints")
public class ProductImageController {

    private final ProductImageCommandService commandService;
    private final ProductImageQueryService queryService;

    // ====================================
    // Command Operations (Writes)
    // ====================================

    @PostMapping
    @Operation(
        summary = "Add image to product",
        description = "Adds a new image to the specified product"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Image added successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<ProductImageResponse> addImage(
            @Parameter(description = "Product ID") 
            @PathVariable UUID productId,
            @Valid @RequestBody AddProductImageRequest request) {
        
        log.info("REST: Adding image to product: {}", productId);
        
        var image = commandService.addImage(productId, request);
        var response = queryService.getImageById(image.getId());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{imageId}")
    @Operation(
        summary = "Remove image from product",
        description = "Removes the specified image from the product"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Image removed successfully"),
        @ApiResponse(responseCode = "404", description = "Product or image not found"),
        @ApiResponse(responseCode = "400", description = "Image does not belong to product")
    })
    public ResponseEntity<Void> removeImage(
            @Parameter(description = "Product ID") 
            @PathVariable UUID productId,
            @Parameter(description = "Image ID") 
            @PathVariable UUID imageId) {
        
        log.info("REST: Removing image {} from product: {}", imageId, productId);
        
        commandService.removeImage(productId, imageId);
        
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{imageId}/primary")
    @Operation(
        summary = "Set primary image",
        description = "Sets the specified image as the primary image for the product"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Primary image set successfully"),
        @ApiResponse(responseCode = "404", description = "Product or image not found"),
        @ApiResponse(responseCode = "400", description = "Image does not belong to product")
    })
    public ResponseEntity<Void> setPrimaryImage(
            @Parameter(description = "Product ID") 
            @PathVariable UUID productId,
            @Parameter(description = "Image ID") 
            @PathVariable UUID imageId) {
        
        log.info("REST: Setting primary image {} for product: {}", imageId, productId);
        
        commandService.setPrimaryImage(productId, imageId);
        
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{imageId}/display-order")
    @Operation(
        summary = "Update image display order",
        description = "Updates the display order of the specified image"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Display order updated successfully"),
        @ApiResponse(responseCode = "404", description = "Product or image not found"),
        @ApiResponse(responseCode = "400", description = "Invalid display order or image does not belong to product")
    })
    public ResponseEntity<Void> updateImageOrder(
            @Parameter(description = "Product ID") 
            @PathVariable UUID productId,
            @Parameter(description = "Image ID") 
            @PathVariable UUID imageId,
            @Parameter(description = "Display order (0-based)") 
            @RequestParam @Min(0) int displayOrder) {
        
        log.info("REST: Updating display order for image {} on product: {}", imageId, productId);
        
        commandService.updateImageOrder(productId, imageId, displayOrder);
        
        return ResponseEntity.noContent().build();
    }

    // ====================================
    // Query Operations (Reads)
    // ====================================

    @GetMapping
    @Operation(
        summary = "Get all images for product",
        description = "Returns all images for the specified product, ordered by display order"
    )
    @ApiResponse(responseCode = "200", description = "Images retrieved successfully")
    public ResponseEntity<List<ProductImageResponse>> getImagesForProduct(
            @Parameter(description = "Product ID") 
            @PathVariable UUID productId) {
        
        log.info("REST: Getting images for product: {}", productId);
        
        return ResponseEntity.ok(queryService.getImagesForProduct(productId));
    }

    @GetMapping("/{imageId}")
    @Operation(
        summary = "Get image by ID",
        description = "Returns a specific image by its ID"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Image found"),
        @ApiResponse(responseCode = "404", description = "Image not found")
    })
    public ResponseEntity<ProductImageResponse> getImageById(
            @Parameter(description = "Product ID") 
            @PathVariable UUID productId,
            @Parameter(description = "Image ID") 
            @PathVariable UUID imageId) {
        
        log.info("REST: Getting image {} for product: {}", imageId, productId);
        
        return ResponseEntity.ok(queryService.getImageById(imageId));
    }

    @GetMapping("/primary")
    @Operation(
        summary = "Get primary image",
        description = "Returns the primary image for the product"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Primary image found"),
        @ApiResponse(responseCode = "404", description = "Primary image not found")
    })
    public ResponseEntity<ProductImageResponse> getPrimaryImage(
            @Parameter(description = "Product ID") 
            @PathVariable UUID productId) {
        
        log.info("REST: Getting primary image for product: {}", productId);
        
        return ResponseEntity.ok(queryService.getPrimaryImage(productId));
    }

    @GetMapping("/gallery")
    @Operation(
        summary = "Get gallery images",
        description = "Returns all gallery images for the product"
    )
    @ApiResponse(responseCode = "200", description = "Gallery images retrieved successfully")
    public ResponseEntity<List<ProductImageResponse>> getGalleryImages(
            @Parameter(description = "Product ID") 
            @PathVariable UUID productId) {
        
        log.info("REST: Getting gallery images for product: {}", productId);
        
        return ResponseEntity.ok(queryService.getGalleryImages(productId));
    }

    @GetMapping("/thumbnails")
    @Operation(
        summary = "Get thumbnail images",
        description = "Returns all thumbnail images for the product"
    )
    @ApiResponse(responseCode = "200", description = "Thumbnail images retrieved successfully")
    public ResponseEntity<List<ProductImageResponse>> getThumbnailImages(
            @Parameter(description = "Product ID") 
            @PathVariable UUID productId) {
        
        log.info("REST: Getting thumbnail images for product: {}", productId);
        
        return ResponseEntity.ok(queryService.getThumbnailImages(productId));
    }

    @GetMapping("/type/{imageType}")
    @Operation(
        summary = "Get images by type",
        description = "Returns all images of the specified type for the product"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Images retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid image type")
    })
    public ResponseEntity<List<ProductImageResponse>> getImagesByType(
            @Parameter(description = "Product ID") 
            @PathVariable UUID productId,
            @Parameter(description = "Image type (PRIMARY, GALLERY, THUMBNAIL, DETAIL)") 
            @PathVariable String imageType) {
        
        log.info("REST: Getting {} images for product: {}", imageType, productId);
        
        return ResponseEntity.ok(queryService.getImagesByType(productId, imageType));
    }

    @GetMapping("/first")
    @Operation(
        summary = "Get first N images",
        description = "Returns the first N images for the product"
    )
    @ApiResponse(responseCode = "200", description = "Images retrieved successfully")
    public ResponseEntity<List<ProductImageResponse>> getFirstImages(
            @Parameter(description = "Product ID") 
            @PathVariable UUID productId,
            @Parameter(description = "Number of images to return") 
            @RequestParam(defaultValue = "5") @Min(1) int limit) {
        
        log.info("REST: Getting first {} images for product: {}", limit, productId);
        
        return ResponseEntity.ok(queryService.getFirstImages(productId, limit));
    }

    @GetMapping("/statistics")
    @Operation(
        summary = "Get image statistics",
        description = "Returns statistics about product images"
    )
    @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully")
    public ResponseEntity<ProductImageQueryService.ImageStatistics> getImageStatistics(
            @Parameter(description = "Product ID") 
            @PathVariable UUID productId) {
        
        log.info("REST: Getting image statistics for product: {}", productId);
        
        return ResponseEntity.ok(queryService.getImageStatistics(productId));
    }

    @GetMapping("/missing-alt-text")
    @Operation(
        summary = "Get images with missing alt text",
        description = "Returns all images that don't have alt text for accessibility"
    )
    @ApiResponse(responseCode = "200", description = "Images retrieved successfully")
    public ResponseEntity<List<ProductImageResponse>> getImagesWithMissingAltText(
            @Parameter(description = "Product ID") 
            @PathVariable UUID productId) {
        
        log.info("REST: Getting images with missing alt text for product: {}", productId);
        
        return ResponseEntity.ok(queryService.getImagesWithMissingAltText(productId));
    }

    @GetMapping("/search/filename")
    @Operation(
        summary = "Search images by filename",
        description = "Searches images by filename pattern"
    )
    @ApiResponse(responseCode = "200", description = "Images retrieved successfully")
    public ResponseEntity<List<ProductImageResponse>> searchByFileName(
            @Parameter(description = "Product ID") 
            @PathVariable UUID productId,
            @Parameter(description = "Filename pattern to search") 
            @RequestParam String fileName) {
        
        log.info("REST: Searching images by filename for product: {}", productId);
        
        var images = queryService.searchByFileName(fileName);
        
        // Filter by product ID
        var filtered = images.stream()
                .filter(img -> img.getProductId().equals(productId))
                .toList();
        
        return ResponseEntity.ok(filtered);
    }

    @GetMapping("/large")
    @Operation(
        summary = "Get large images",
        description = "Returns images larger than the specified file size"
    )
    @ApiResponse(responseCode = "200", description = "Images retrieved successfully")
    public ResponseEntity<List<ProductImageResponse>> getLargeImages(
            @Parameter(description = "Product ID") 
            @PathVariable UUID productId,
            @Parameter(description = "Minimum file size in bytes") 
            @RequestParam(defaultValue = "1048576") @Min(0) long minFileSize) {
        
        log.info("REST: Getting large images (> {} bytes) for product: {}", minFileSize, productId);
        
        return ResponseEntity.ok(queryService.getLargeImages(productId, minFileSize));
    }

    @GetMapping("/dimensions")
    @Operation(
        summary = "Get images by dimensions",
        description = "Returns images matching the specified minimum dimensions"
    )
    @ApiResponse(responseCode = "200", description = "Images retrieved successfully")
    public ResponseEntity<List<ProductImageResponse>> getImagesByDimensions(
            @Parameter(description = "Product ID") 
            @PathVariable UUID productId,
            @Parameter(description = "Minimum width in pixels") 
            @RequestParam(required = false) @Min(1) Integer minWidth,
            @Parameter(description = "Minimum height in pixels") 
            @RequestParam(required = false) @Min(1) Integer minHeight) {
        
        log.info("REST: Getting images with dimensions >= {}x{} for product: {}", minWidth, minHeight, productId);
        
        return ResponseEntity.ok(queryService.getImagesByDimensions(productId, minWidth, minHeight));
    }

    // ====================================
    // Utility Endpoints
    // ====================================

    @GetMapping("/exists")
    @Operation(
        summary = "Check if product has images",
        description = "Returns whether the product has any images"
    )
    @ApiResponse(responseCode = "200", description = "Check completed successfully")
    public ResponseEntity<Boolean> hasImages(
            @Parameter(description = "Product ID") 
            @PathVariable UUID productId) {
        
        log.info("REST: Checking if product has images: {}", productId);
        
        return ResponseEntity.ok(queryService.hasImages(productId));
    }

    @GetMapping("/count")
    @Operation(
        summary = "Count product images",
        description = "Returns the total number of images for the product"
    )
    @ApiResponse(responseCode = "200", description = "Count retrieved successfully")
    public ResponseEntity<Long> countImages(
            @Parameter(description = "Product ID") 
            @PathVariable UUID productId) {
        
        log.info("REST: Counting images for product: {}", productId);
        
        return ResponseEntity.ok(queryService.countImagesForProduct(productId));
    }

    @GetMapping("/count/type/{imageType}")
    @Operation(
        summary = "Count images by type",
        description = "Returns the number of images of the specified type"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Count retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid image type")
    })
    public ResponseEntity<Long> countImagesByType(
            @Parameter(description = "Product ID") 
            @PathVariable UUID productId,
            @Parameter(description = "Image type") 
            @PathVariable String imageType) {
        
        log.info("REST: Counting {} images for product: {}", imageType, productId);
        
        ImageType type = ImageType.valueOf(imageType.toUpperCase());
        return ResponseEntity.ok(queryService.countImagesByType(productId, type));
    }
}
