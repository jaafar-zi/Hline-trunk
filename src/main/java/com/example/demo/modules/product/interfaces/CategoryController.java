package com.example.demo.modules.product.interfaces;

import com.example.demo.common.base.controller.ApiResponse;
import com.example.demo.common.base.controller.BaseController;
import com.example.demo.common.validation.groups.Create;
import com.example.demo.common.validation.groups.Update;
import com.example.demo.modules.product.application.command.CategoryCommandService;
import com.example.demo.modules.product.application.dto.filter.CategoryFilter;
import com.example.demo.modules.product.application.dto.request.*;
import com.example.demo.modules.product.application.dto.response.CategoryResponse;
import com.example.demo.modules.product.application.query.CategoryQueryService;
import com.example.demo.modules.product.domain.model.Category;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Category REST controller
 * 
 * @author PIM Team
 * @version 3.0
 */
@Slf4j
@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
@Validated
@Tag(name = "Categories", description = "Category management APIs")
public class CategoryController extends BaseController {

    private final CategoryCommandService categoryCommandService;
    private final CategoryQueryService categoryQueryService;

    // ====================================
    // Query Operations
    // ====================================

    @GetMapping
    @Operation(summary = "Search categories", description = "Search categories with filters and pagination")
    public ResponseEntity<ApiResponse<Page<CategoryResponse>>> searchCategories(
            @Parameter(description = "Category filter criteria")
            @ModelAttribute CategoryFilter filter) {
        
        log.info("Searching categories with filter");
        
        Page<CategoryResponse> categories = categoryQueryService.searchCategories(filter);
        
        return success(categories);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get category by ID", description = "Get category details by ID")
    public ResponseEntity<ApiResponse<CategoryResponse>> getCategoryById(
            @Parameter(description = "Category ID", required = true)
            @PathVariable UUID id) {
        
        log.info("Getting category by ID: {}", id);
        
        CategoryResponse category = categoryQueryService.getCategoryById(id);
        
        return success(category);
    }

    @GetMapping("/code/{code}")
    @Operation(summary = "Get category by code", description = "Get category details by code")
    public ResponseEntity<ApiResponse<CategoryResponse>> getCategoryByCode(
            @Parameter(description = "Category code", required = true)
            @PathVariable String code) {
        
        log.info("Getting category by code: {}", code);
        
        CategoryResponse category = categoryQueryService.getCategoryByCode(code);
        
        return success(category);
    }

    @GetMapping("/root")
    @Operation(summary = "Get root categories", description = "Get all root (top-level) categories")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getRootCategories() {
        
        log.info("Getting root categories");
        
        List<CategoryResponse> categories = categoryQueryService.getRootCategories();
        
        return success(categories);
    }

    @GetMapping("/{id}/children")
    @Operation(summary = "Get child categories", description = "Get all child categories of a parent")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getChildCategories(
            @Parameter(description = "Parent category ID", required = true)
            @PathVariable UUID id) {
        
        log.info("Getting child categories for: {}", id);
        
        List<CategoryResponse> categories = categoryQueryService.getChildCategories(id);
        
        return success(categories);
    }

    @GetMapping("/all")
    @Operation(summary = "Get all categories", description = "Get all categories without pagination")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getAllCategories(
            @Parameter(description = "Category filter criteria")
            @ModelAttribute CategoryFilter filter) {
        
        log.info("Getting all categories");
        
        List<CategoryResponse> categories = categoryQueryService.findAllCategories(filter);
        
        return success(categories);
    }

    @GetMapping("/check-code/{code}")
    @Operation(summary = "Check if code exists", description = "Check if a category code already exists")
    public ResponseEntity<ApiResponse<Boolean>> checkCodeExists(
            @Parameter(description = "Category code to check", required = true)
            @PathVariable String code) {
        
        log.info("Checking if category code exists: {}", code);
        
        boolean exists = categoryQueryService.codeExists(code);
        
        return success(exists);
    }

    // ====================================
    // Command Operations
    // ====================================

    @PostMapping
    @Operation(summary = "Create category", description = "Create a new category")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CATEGORY_MANAGER')")
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(
            @Parameter(description = "Category creation request", required = true)
            @Valid @RequestBody CreateCategoryRequest request) {
        
        log.info("Creating category: {}", request.getName());
        
        // Validate with Create group
        validateAndNormalize(request, Create.class);
        
        Category category = categoryCommandService.createCategory(request);
        CategoryResponse response = categoryQueryService.getCategoryById(category.getId());
        
        return created(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update category", description = "Update an existing category")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CATEGORY_MANAGER')")
    public ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(
            @Parameter(description = "Category ID", required = true)
            @PathVariable UUID id,
            @Parameter(description = "Category update request", required = true)
            @Valid @RequestBody UpdateCategoryRequest request) {
        
        log.info("Updating category: {}", id);
        
        // Validate with Update group
        validateAndNormalize(request, Update.class);
        
        Category category = categoryCommandService.updateCategory(id, request);
        CategoryResponse response = categoryQueryService.getCategoryById(category.getId());
        
        return success(response, "Category updated successfully");
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete category", description = "Delete a category (hard delete)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(
            @Parameter(description = "Category ID", required = true)
            @PathVariable UUID id) {
        
        log.info("Deleting category: {}", id);
        
        categoryCommandService.delete(id);
        
        return success(null, "Category deleted successfully");
    }

    @PostMapping("/{id}/soft-delete")
    @Operation(summary = "Soft delete category", description = "Soft delete a category")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CATEGORY_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> softDeleteCategory(
            @Parameter(description = "Category ID", required = true)
            @PathVariable UUID id) {
        
        log.info("Soft deleting category: {}", id);
        
        categoryCommandService.softDelete(id);
        
        return success(null, "Category soft deleted successfully");
    }

    @PostMapping("/{id}/restore")
    @Operation(summary = "Restore category", description = "Restore a soft-deleted category")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CATEGORY_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> restoreCategory(
            @Parameter(description = "Category ID", required = true)
            @PathVariable UUID id) {
        
        log.info("Restoring category: {}", id);
        
        categoryCommandService.restoreCategory(id);
        
        return success(null, "Category restored successfully");
    }

    // ====================================
    // Category Operations
    // ====================================

    @PutMapping("/{id}/move")
    @Operation(summary = "Move category", description = "Move category to a different parent")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CATEGORY_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> moveCategory(
            @Parameter(description = "Category ID", required = true)
            @PathVariable UUID id,
            @Parameter(description = "Move category request", required = true)
            @Valid @RequestBody MoveCategoryRequest request) {
        
        log.info("Moving category: {} to parent: {}", id, request.getNewParentId());
        
        validateAndNormalize(request);
        
        categoryCommandService.moveCategory(id, request);
        
        return success(null, "Category moved successfully");
    }

    @PutMapping("/{id}/toggle-status")
    @Operation(summary = "Toggle category status", description = "Activate or deactivate a category")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CATEGORY_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> toggleCategoryStatus(
            @Parameter(description = "Category ID", required = true)
            @PathVariable UUID id,
            @Parameter(description = "Toggle status request", required = true)
            @Valid @RequestBody ToggleCategoryStatusRequest request) {
        
        log.info("Toggling category status: {} to {}", id, request.getActive());
        
        validateAndNormalize(request);
        
        categoryCommandService.toggleCategoryStatus(id, request);
        
        return success(null, "Category status updated successfully");
    }

    @PutMapping("/reorder")
    @Operation(summary = "Reorder categories", description = "Update display order of multiple categories")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CATEGORY_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> reorderCategories(
            @Parameter(description = "Reorder request", required = true)
            @Valid @RequestBody ReorderCategoriesRequest request) {
        
        log.info("Reordering {} categories", request.getCategoryOrders().size());
        
        validateAndNormalize(request);
        
        categoryCommandService.reorderCategories(request);
        
        return success(null, "Categories reordered successfully");
    }
}