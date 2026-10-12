package com.example.demo.modules.kitchen.interfaces;

import com.example.demo.common.base.controller.ApiResponse;
import com.example.demo.common.base.controller.BaseController;
import com.example.demo.modules.kitchen.application.command.KitchenRangeCommandService;
import com.example.demo.modules.kitchen.application.dto.filter.KitchenRangeFilter;
import com.example.demo.modules.kitchen.application.dto.request.*;
import com.example.demo.modules.kitchen.application.dto.response.*;
import com.example.demo.modules.kitchen.application.mapper.*;
import com.example.demo.modules.kitchen.application.query.KitchenRangeQueryService;
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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Kitchen Range REST Controller
 *
 * Public:  GET endpoints (browsing)
 * Admin:   POST/PUT/DELETE endpoints (management)
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/kitchen-ranges")
@RequiredArgsConstructor
@Validated
@Tag(name = "Kitchen Ranges", description = "Kitchen range management APIs")
public class KitchenRangeController extends BaseController {

    private final KitchenRangeCommandService rangeCommandService;
    private final KitchenRangeQueryService rangeQueryService;
    private final KitchenRangeMapper rangeMapper;
    private final KitchenFinishGroupMapper finishGroupMapper;
    private final KitchenCollectionMapper collectionMapper;

    // ====================================
    // Public - Browse
    // ====================================

    @GetMapping
    @Operation(summary = "Get all active kitchen ranges",
               description = "Returns all active kitchen ranges for the storefront")
    public ResponseEntity<ApiResponse<List<KitchenRangeResponse>>> getAllActiveRanges() {
        log.info("Getting all active kitchen ranges");

        List<KitchenRange> ranges = rangeQueryService.findAllActive();
        List<KitchenRangeResponse> response = ranges.stream()
            .map(rangeMapper::toDto)
            .collect(Collectors.toList());

        return success(response);
    }

    @GetMapping("/{slug}")
    @Operation(summary = "Get kitchen range by slug",
               description = "Get range with finish groups - used on /kitchens/{range-slug}")
    public ResponseEntity<ApiResponse<KitchenRangeResponse>> getRangeBySlug(
            @Parameter(description = "Range slug", example = "j-pull", required = true)
            @PathVariable String slug) {

        log.info("Getting kitchen range by slug: {}", slug);

        KitchenRange range = rangeQueryService.findBySlugWithCollections(slug);
        KitchenRangeResponse response = rangeMapper.toDto(range);

        // Set finish groups manually (avoids MapStruct lazy-load issues)
        List<KitchenFinishGroupResponse> finishGroups = range.getFinishGroups()
            .stream()
            .filter(fg -> Boolean.TRUE.equals(fg.getActive()))
            .map(fg -> {
                KitchenFinishGroupResponse fgResponse = finishGroupMapper.toDto(fg);
                fgResponse.setCollections(
                    fg.getCollections().stream()
                        .filter(c -> Boolean.TRUE.equals(c.getActive()))
                        .map(collectionMapper::toSummary)
                        .collect(Collectors.toList())
                );
                return fgResponse;
            })
            .collect(Collectors.toList());

        response.setFinishGroups(finishGroups);
        response.setFinishGroupCount((long) finishGroups.size());

        return success(response);
    }

    // ====================================
    // Admin - Management
    // ====================================

    @GetMapping("/admin")
    @Operation(summary = "Get all ranges (admin)",
               description = "Get all ranges with pagination and filtering")
    // @PreAuthorize("hasRole('ADMIN') or hasRole('KITCHEN_MANAGER')")
    public ResponseEntity<ApiResponse<Page<KitchenRangeResponse>>> getAllRanges(
            @ModelAttribute KitchenRangeFilter filter,
            Pageable pageable) {

        log.info("Admin: getting all kitchen ranges");

        Page<KitchenRange> page = rangeQueryService.findAll(pageable);
        Page<KitchenRangeResponse> response = page.map(rangeMapper::toDto);

        return success(response);
    }

    @GetMapping("/admin/{id}")
    @Operation(summary = "Get range by ID (admin)")
    // @PreAuthorize("hasRole('ADMIN') or hasRole('KITCHEN_MANAGER')")
    public ResponseEntity<ApiResponse<KitchenRangeResponse>> getRangeById(
            @PathVariable UUID id) {

        log.info("Admin: getting kitchen range by ID: {}", id);

        KitchenRange range = rangeQueryService.findByIdWithAll(id);
        KitchenRangeResponse response = rangeMapper.toDto(range);
        response.setFinishGroupCount(rangeQueryService.countFinishGroupsByRange(id));
        response.setCollectionCount(rangeQueryService.countCollectionsByRange(id));

        return success(response);
    }

    @PostMapping
    @Operation(summary = "Create kitchen range")
    // @PreAuthorize("hasRole('ADMIN') or hasRole('KITCHEN_MANAGER')")
    public ResponseEntity<ApiResponse<KitchenRangeResponse>> createRange(
            @Valid @RequestBody CreateKitchenRangeRequest request) {

        log.info("Creating kitchen range: {}", request.getName());
        validateAndNormalize(request);

        KitchenRange range = rangeCommandService.createRange(
            request.getCode(),
            request.getName()
        );

        if (request.getShortDescription() != null
                || request.getDescription() != null) {
            rangeCommandService.updateRangeInfo(
                range.getId(),
                request.getName(),
                request.getShortDescription(),
                request.getDescription()
            );
        }

        KitchenRange created = rangeQueryService.findById(range.getId());
        return created(rangeMapper.toDto(created));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update kitchen range")
    // @PreAuthorize("hasRole('ADMIN') or hasRole('KITCHEN_MANAGER')")
    public ResponseEntity<ApiResponse<KitchenRangeResponse>> updateRange(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateKitchenRangeRequest request) {

        log.info("Updating kitchen range: {}", id);
        validateAndNormalize(request);

        KitchenRange range = rangeCommandService.updateRangeInfo(
            id,
            request.getName(),
            request.getShortDescription(),
            request.getDescription()
        );

        return success(rangeMapper.toDto(range), "Range updated successfully");
    }

    @PutMapping("/{id}/seo")
    @Operation(summary = "Update range SEO")
    // @PreAuthorize("hasRole('ADMIN') or hasRole('KITCHEN_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> updateRangeSEO(
            @PathVariable UUID id,
            @RequestBody UpdateKitchenRangeRequest request) {

        rangeCommandService.updateSEO(
            id,
            request.getMetaTitle(),
            request.getMetaDescription()
        );
        return success(null, "SEO updated successfully");
    }

    @PostMapping("/{id}/activate")
    @Operation(summary = "Activate kitchen range")
    // @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> activateRange(@PathVariable UUID id) {
        log.info("Activating kitchen range: {}", id);
        rangeCommandService.activateRange(id);
        return success(null, "Range activated successfully");
    }

    @PostMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate kitchen range")
    // @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deactivateRange(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "Deactivated by admin") String reason) {

        log.info("Deactivating kitchen range: {}", id);
        rangeCommandService.deactivateRange(id, reason);
        return success(null, "Range deactivated successfully");
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft delete kitchen range")
    // @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteRange(@PathVariable UUID id) {
        log.info("Deleting kitchen range: {}", id);
        rangeCommandService.softDeleteRange(id, getCurrentUsername());
        return success(null, "Range deleted successfully");
    }

    // ====================================
    // Finish Group endpoints
    // ====================================

    @GetMapping("/{rangeId}/finish-groups")
    @Operation(summary = "Get finish groups for a range")
    public ResponseEntity<ApiResponse<List<KitchenFinishGroupResponse>>> getFinishGroups(
            @PathVariable UUID rangeId) {

        log.info("Getting finish groups for range: {}", rangeId);

        List<KitchenFinishGroup> groups =
            rangeQueryService.findActiveFinishGroupsByRange(rangeId);

        return success(finishGroupMapper.toDtoList(groups));
    }

    @PostMapping("/{rangeId}/finish-groups")
    @Operation(summary = "Add finish group to range")
    // @PreAuthorize("hasRole('ADMIN') or hasRole('KITCHEN_MANAGER')")
    public ResponseEntity<ApiResponse<KitchenFinishGroupResponse>> addFinishGroup(
            @PathVariable UUID rangeId,
            @RequestParam UUID finishTypeId,
            @RequestParam String name) {

        log.info("Adding finish group to range: {}", rangeId);

        KitchenFinishGroup group = rangeCommandService.addFinishGroupToRange(
            rangeId, finishTypeId, name
        );

        return created(finishGroupMapper.toDto(group));
    }

    @PutMapping("/finish-groups/{finishGroupId}")
    @Operation(summary = "Update finish group")
    // @PreAuthorize("hasRole('ADMIN') or hasRole('KITCHEN_MANAGER')")
    public ResponseEntity<ApiResponse<KitchenFinishGroupResponse>> updateFinishGroup(
            @PathVariable UUID finishGroupId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String description) {

        KitchenFinishGroup group = rangeCommandService.updateFinishGroup(
            finishGroupId, name, description
        );
        return success(finishGroupMapper.toDto(group), "Finish group updated");
    }

    @PostMapping("/finish-groups/{finishGroupId}/activate")
    @Operation(summary = "Activate finish group")
    // @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> activateFinishGroup(
            @PathVariable UUID finishGroupId) {

        rangeCommandService.activateFinishGroup(finishGroupId);
        return success(null, "Finish group activated");
    }

    @PostMapping("/finish-groups/{finishGroupId}/deactivate")
    @Operation(summary = "Deactivate finish group")
    // @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deactivateFinishGroup(
            @PathVariable UUID finishGroupId,
            @RequestParam(defaultValue = "Deactivated by admin") String reason) {

        rangeCommandService.deactivateFinishGroup(finishGroupId, reason);
        return success(null, "Finish group deactivated");
    }

    // ====================================
    // Statistics
    // ====================================

    @GetMapping("/statistics")
    @Operation(summary = "Get range statistics")
    // @PreAuthorize("hasRole('ADMIN') or hasRole('KITCHEN_MANAGER')")
    public ResponseEntity<ApiResponse<RangeStatistics>> getStatistics() {
        return success(RangeStatistics.builder()
            .totalActiveRanges(rangeQueryService.countActiveRanges())
            .build());
    }

    // ====================================
    // Helper
    // ====================================

    private String getCurrentUsername() {
        try {
            var auth = org.springframework.security.core.context
                .SecurityContextHolder.getContext().getAuthentication();
            return auth != null ? auth.getName() : "system";
        } catch (Exception e) {
            return "system";
        }
    }

    @lombok.Data
    @lombok.Builder
    public static class RangeStatistics {
        private Long totalActiveRanges;
    }
}