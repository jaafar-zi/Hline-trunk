package com.example.demo.modules.kitchen.interfaces;

import com.example.demo.common.base.controller.ApiResponse;
import com.example.demo.common.base.controller.BaseController;
import com.example.demo.modules.kitchen.application.command.KitchenCollectionCommandService;
import com.example.demo.modules.kitchen.application.dto.request.*;
import com.example.demo.modules.kitchen.application.dto.response.*;
import com.example.demo.modules.kitchen.application.mapper.*;
import com.example.demo.modules.kitchen.application.query.KitchenCollectionQueryService;
import com.example.demo.modules.kitchen.application.query.KitchenProductQueryService;
import com.example.demo.modules.kitchen.application.query.KitchenUnitCategoryQueryService;
import com.example.demo.modules.kitchen.domain.model.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Kitchen Collection REST Controller
 *
 * URL pattern: /kitchens/{rangeSlug}/{collectionSlug}
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@Validated
@Tag(name = "Kitchen Collections", description = "Kitchen collection APIs")
public class KitchenCollectionController extends BaseController {

    private final KitchenCollectionCommandService collectionCommandService;
    private final KitchenCollectionQueryService collectionQueryService;
    private final KitchenProductQueryService productQueryService;
    private final KitchenUnitCategoryQueryService unitCategoryQueryService;
    private final KitchenCollectionMapper collectionMapper;
    private final KitchenColourMapper colourMapper;
    private final KitchenProductMapper productMapper;

    // ====================================
    // Public Storefront - URL-based lookups
    // ====================================

    /**
     * PAGE 3: Collection detail page
     * URL: /kitchens/{rangeSlug}/{collectionSlug}
     */
    @GetMapping("/api/v1/kitchens/{rangeSlug}/{collectionSlug}")
    @Operation(summary = "Get collection detail page",
               description = "Full collection data for /kitchens/{range}/{collection}")
    public ResponseEntity<ApiResponse<KitchenCollectionResponse>> getCollectionPage(
            @Parameter(description = "Range slug", example = "j-pull", required = true)
            @PathVariable String rangeSlug,
            @Parameter(description = "Collection slug",
                       example = "supergloss-cashmere", required = true)
            @PathVariable String collectionSlug) {

        log.info("Getting collection page: {}/{}", rangeSlug, collectionSlug);

        KitchenCollection collection =
            collectionQueryService.findByPath(rangeSlug, collectionSlug);

        KitchenCollectionResponse response = buildFullCollectionResponse(collection);
        return success(response);
    }

    /**
     * Build full collection response with all nested data
     */
    private KitchenCollectionResponse buildFullCollectionResponse(
            KitchenCollection collection) {

        KitchenCollectionResponse response = collectionMapper.toDto(collection);

        // Set default cabinet colour
        if (collection.getDefaultCabinetColour() != null) {
            response.setDefaultCabinetColour(
                colourMapper.toDto(collection.getDefaultCabinetColour()));
        }

        // Set available cabinet colours
        if (collection.getAvailableCabinetColours() != null) {
            response.setAvailableCabinetColours(
                collection.getAvailableCabinetColours().stream()
                    .map(colourMapper::toDto)
                    .collect(Collectors.toList())
            );
        }

        // Set sibling collections (colour switcher)
        if (collection.getRange() != null) {
            List<KitchenCollectionSummary> siblings =
                collectionQueryService.findSiblingCollections(
                    collection.getRange().getId(),
                    collection.getId()
                ).stream()
                .map(s -> collectionMapper.toSummaryWithSelected(s, false))
                .collect(Collectors.toList());

            // Mark current as selected
            KitchenCollectionSummary current =
                collectionMapper.toSummaryWithSelected(collection, true);
            siblings.add(0, current);

            response.setSiblingCollections(siblings);
        }

        // Set products grouped by unit category
        List<KitchenUnitCategory> categories =
            unitCategoryQueryService.findCategoriesWithProductsByCollection(
                collection.getId());

        List<KitchenUnitCategoryWithProductsResponse> unitCategories =
            categories.stream().map(category -> {
                List<KitchenProduct> products =
                    productQueryService.findByCollectionAndCategory(
                        collection.getId(), category.getId());

                return KitchenUnitCategoryWithProductsResponse.builder()
                    .id(category.getId())
                    .code(category.getCode())
                    .name(category.getName())
                    .slug(category.getSlug())
                    .sortOrder(category.getSortOrder())
                    .products(productMapper.toSummaryList(products))
                    .productCount(products.size())
                    .build();
            }).collect(Collectors.toList());

        response.setUnitCategories(unitCategories);

        // Set starting from price
        response.setStartingFromPrice(
            productQueryService.findMinPriceByCollection(collection.getId()));

        // Set product count
        response.setProductCount(
            productQueryService.countActiveByCollection(collection.getId()));

        return response;
    }

    // ====================================
    // Public - Collections by Range
    // ====================================

    @GetMapping("/api/v1/kitchen-ranges/{rangeId}/collections")
    @Operation(summary = "Get collections by range ID")
    public ResponseEntity<ApiResponse<List<KitchenCollectionSummary>>>
            getCollectionsByRange(@PathVariable UUID rangeId) {

        log.info("Getting collections for range: {}", rangeId);

        List<KitchenCollection> collections =
            collectionQueryService.findActiveByRange(rangeId);

        return success(collectionMapper.toSummaryList(collections));
    }

    @GetMapping("/api/v1/kitchen-ranges/{rangeId}/collections/{collectionId}/siblings")
    @Operation(summary = "Get sibling collections (colour switcher)")
    public ResponseEntity<ApiResponse<List<KitchenCollectionSummary>>> getSiblings(
            @PathVariable UUID rangeId,
            @PathVariable UUID collectionId) {

        List<KitchenCollection> siblings =
            collectionQueryService.findSiblingCollections(rangeId, collectionId);

        return success(collectionMapper.toSummaryList(siblings));
    }

    // ====================================
    // Admin - Management
    // ====================================

    @GetMapping("/api/v1/admin/kitchen-collections")
    @Operation(summary = "Get all collections (admin)")
    // @PreAuthorize("hasRole('ADMIN') or hasRole('KITCHEN_MANAGER')")
    public ResponseEntity<ApiResponse<Page<KitchenCollectionResponse>>> getAllCollections(
            Pageable pageable) {

        Page<KitchenCollection> page = collectionQueryService.findAll(pageable);
        Page<KitchenCollectionResponse> response = page.map(collectionMapper::toDto);
        return success(response);
    }

    @GetMapping("/api/v1/admin/kitchen-collections/{id}")
    @Operation(summary = "Get collection by ID (admin)")
    // @PreAuthorize("hasRole('ADMIN') or hasRole('KITCHEN_MANAGER')")
    public ResponseEntity<ApiResponse<KitchenCollectionResponse>> getCollectionById(
            @PathVariable UUID id) {

        log.info("Admin: getting kitchen collection by ID: {}", id);

        KitchenCollection collection = collectionQueryService.findByIdWithDetails(id);
        return success(buildFullCollectionResponse(collection));
    }

    @PostMapping("/api/v1/admin/kitchen-collections")
    @Operation(summary = "Create kitchen collection")
    // @PreAuthorize("hasRole('ADMIN') or hasRole('KITCHEN_MANAGER')")
    public ResponseEntity<ApiResponse<KitchenCollectionResponse>> createCollection(
            @Valid @RequestBody CreateKitchenCollectionRequest request) {

        log.info("Creating kitchen collection: {}", request.getName());
        validateAndNormalize(request);

        KitchenCollection collection = collectionCommandService.createCollection(
            request.getFinishGroupId(),
            request.getColourId(),
            request.getName()
        );

        if (request.getDefaultCabinetColourId() != null) {
            collectionCommandService.setDefaultCabinetColour(
                collection.getId(),
                request.getDefaultCabinetColourId()
            );
        }

        if (request.getLeadTimeDays() != null) {
            collectionCommandService.updateLeadTime(
                collection.getId(), request.getLeadTimeDays());
        }

        KitchenCollection created =
            collectionQueryService.findByIdWithDetails(collection.getId());

        return created(collectionMapper.toDto(created));
    }

    @PutMapping("/api/v1/admin/kitchen-collections/{id}")
    @Operation(summary = "Update kitchen collection")
    // @PreAuthorize("hasRole('ADMIN') or hasRole('KITCHEN_MANAGER')")
    public ResponseEntity<ApiResponse<KitchenCollectionResponse>> updateCollection(
            @PathVariable UUID id,
            @Valid @RequestBody CreateKitchenCollectionRequest request) {

        log.info("Updating kitchen collection: {}", id);
        validateAndNormalize(request);

        KitchenCollection collection = collectionCommandService.updateBasicInfo(
            id,
            request.getName(),
            request.getShortDescription(),
            request.getDescription()
        );

        return success(collectionMapper.toDto(collection), "Collection updated");
    }

    @PutMapping("/api/v1/admin/kitchen-collections/{id}/default-colour")
    @Operation(summary = "Set default cabinet colour")
    // @PreAuthorize("hasRole('ADMIN') or hasRole('KITCHEN_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> setDefaultCabinetColour(
            @PathVariable UUID id,
            @RequestParam UUID colourId) {

        collectionCommandService.setDefaultCabinetColour(id, colourId);
        return success(null, "Default cabinet colour set");
    }

    @PostMapping("/api/v1/admin/kitchen-collections/{id}/cabinet-colours")
    @Operation(summary = "Add available cabinet colour")
    // @PreAuthorize("hasRole('ADMIN') or hasRole('KITCHEN_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> addCabinetColour(
            @PathVariable UUID id,
            @RequestParam UUID colourId) {

        collectionCommandService.addAvailableCabinetColour(id, colourId);
        return success(null, "Cabinet colour added");
    }

    @DeleteMapping("/api/v1/admin/kitchen-collections/{id}/cabinet-colours/{colourId}")
    @Operation(summary = "Remove available cabinet colour")
    // @PreAuthorize("hasRole('ADMIN') or hasRole('KITCHEN_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> removeCabinetColour(
            @PathVariable UUID id,
            @PathVariable UUID colourId) {

        collectionCommandService.removeAvailableCabinetColour(id, colourId);
        return success(null, "Cabinet colour removed");
    }

    @PostMapping("/api/v1/admin/kitchen-collections/{id}/gallery-images")
    @Operation(summary = "Add gallery image")
    // @PreAuthorize("hasRole('ADMIN') or hasRole('KITCHEN_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> addGalleryImage(
            @PathVariable UUID id,
            @RequestParam String imageUrl) {

        collectionCommandService.addGalleryImage(id, imageUrl);
        return success(null, "Gallery image added");
    }

    @PostMapping("/api/v1/admin/kitchen-collections/{id}/features")
    @Operation(summary = "Add feature")
    // @PreAuthorize("hasRole('ADMIN') or hasRole('KITCHEN_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> addFeature(
            @PathVariable UUID id,
            @RequestParam String feature) {

        collectionCommandService.addFeature(id, feature);
        return success(null, "Feature added");
    }

    @PostMapping("/api/v1/admin/kitchen-collections/{id}/activate")
    @Operation(summary = "Activate collection")
    // @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> activate(@PathVariable UUID id) {
        collectionCommandService.activate(id);
        return success(null, "Collection activated");
    }

    @PostMapping("/api/v1/admin/kitchen-collections/{id}/deactivate")
    @Operation(summary = "Deactivate collection")
    // @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deactivate(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "Deactivated by admin") String reason) {

        collectionCommandService.deactivate(id, reason);
        return success(null, "Collection deactivated");
    }

    @DeleteMapping("/api/v1/admin/kitchen-collections/{id}")
    @Operation(summary = "Delete collection")
    // @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteCollection(@PathVariable UUID id) {
        collectionCommandService.softDeleteCollection(id, getCurrentUsername());
        return success(null, "Collection deleted");
    }

    private String getCurrentUsername() {
        try {
            var auth = org.springframework.security.core.context
                .SecurityContextHolder.getContext().getAuthentication();
            return auth != null ? auth.getName() : "system";
        } catch (Exception e) {
            return "system";
        }
    }
}