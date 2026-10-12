package com.example.demo.modules.kitchen.interfaces;

import com.example.demo.common.base.controller.ApiResponse;
import com.example.demo.common.base.controller.BaseController;
import com.example.demo.modules.kitchen.application.command.KitchenUnitCategoryCommandService;
import com.example.demo.modules.kitchen.application.dto.response.UnitCategoryResponse;
import com.example.demo.modules.kitchen.application.query.KitchenUnitCategoryQueryService;
import com.example.demo.modules.kitchen.domain.model.KitchenUnitCategory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/v1/kitchen-unit-categories")
@RequiredArgsConstructor
@Tag(name = "Kitchen Unit Categories", description = "Kitchen unit category management APIs")
public class KitchenUnitCategoryController extends BaseController {

    private final KitchenUnitCategoryCommandService unitCategoryCommandService;
    private final KitchenUnitCategoryQueryService unitCategoryQueryService;

    @GetMapping
    @Operation(summary = "Get all active unit categories")
    public ResponseEntity<ApiResponse<List<UnitCategoryResponse>>> getAllActive() {
        List<KitchenUnitCategory> categories = unitCategoryQueryService.findAllActive();
        return success(categories.stream().map(this::toResponse).collect(Collectors.toList()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get unit category by ID")
    public ResponseEntity<ApiResponse<UnitCategoryResponse>> getById(@PathVariable UUID id) {
        KitchenUnitCategory category = unitCategoryQueryService.findById(id);
        return success(toResponse(category));
    }

    @GetMapping("/collection/{collectionId}")
    @Operation(summary = "Get categories that have products in a collection")
    public ResponseEntity<ApiResponse<List<UnitCategoryResponse>>> getByCollection(
            @PathVariable UUID collectionId) {
        List<KitchenUnitCategory> categories =
            unitCategoryQueryService.findCategoriesWithProductsByCollection(collectionId);
        return success(categories.stream().map(this::toResponse).collect(Collectors.toList()));
    }

    @PostMapping
    @Operation(summary = "Create unit category")
    @PreAuthorize("hasRole('ADMIN') or hasRole('KITCHEN_MANAGER')")
    public ResponseEntity<ApiResponse<UnitCategoryResponse>> create(
            @RequestParam String code,
            @RequestParam String name,
            @RequestParam(required = false) String description,
            @RequestParam(required = false, defaultValue = "0") Integer sortOrder) {

        log.info("Creating unit category: code={}, name={}", code, name);
        KitchenUnitCategory category = unitCategoryCommandService.createCategory(
            code, name, description, sortOrder);
        return created(toResponse(category));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update unit category")
    @PreAuthorize("hasRole('ADMIN') or hasRole('KITCHEN_MANAGER')")
    public ResponseEntity<ApiResponse<UnitCategoryResponse>> update(
            @PathVariable UUID id,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String icon,
            @RequestParam(required = false) String imageUrl) {

        KitchenUnitCategory category = unitCategoryCommandService.updateCategory(
            id, name, description, icon, imageUrl);
        return success(toResponse(category), "Unit category updated");
    }

    @PostMapping("/{id}/activate")
    @Operation(summary = "Activate unit category")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> activate(@PathVariable UUID id) {
        unitCategoryCommandService.activate(id);
        return success(null, "Unit category activated");
    }

    @PostMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate unit category")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deactivate(@PathVariable UUID id) {
        unitCategoryCommandService.deactivate(id);
        return success(null, "Unit category deactivated");
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete unit category")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        unitCategoryCommandService.softDeleteCategory(id, getCurrentUsername());
        return success(null, "Unit category deleted");
    }

    private UnitCategoryResponse toResponse(KitchenUnitCategory category) {
        return UnitCategoryResponse.builder()
            .id(category.getId())
            .code(category.getCode())
            .name(category.getName())
            .slug(category.getSlug())
            .description(category.getDescription())
            .icon(category.getIcon())
            .imageUrl(category.getImageUrl())
            .active(category.getActive())
            .sortOrder(category.getSortOrder())
            .createdAt(category.getCreatedAt())
            .updatedAt(category.getUpdatedAt())
            .version(category.getVersion())
            .build();
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