package com.example.demo.modules.user.interfaces.rest;

import com.example.demo.common.base.dto.BaseResponse;
import com.example.demo.common.base.dto.PageResponse;
import com.example.demo.modules.user.application.command.PermissionCommandService;
import com.example.demo.modules.user.application.dto.request.CreatePermissionRequest;
import com.example.demo.modules.user.application.dto.request.UpdatePermissionRequest;
import com.example.demo.modules.user.application.dto.response.PermissionResponse;
import com.example.demo.modules.user.application.query.PermissionQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Permission REST Controller
 *
 * @author Architecture Team
 * @version 1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/permissions")
@RequiredArgsConstructor
@Tag(name = "Permissions", description = "Permission management APIs")
public class PermissionController {

    private final PermissionCommandService permissionCommandService;
    private final PermissionQueryService permissionQueryService;

    @GetMapping
    @PreAuthorize("hasPermission('PERMISSION', 'READ')")
    @Operation(summary = "Get all permissions")
    public ResponseEntity<BaseResponse<PageResponse<PermissionResponse>>> getAllPermissions(
            @PageableDefault(size = 100, sort = "resource") Pageable pageable) {

        log.info("GET /api/v1/permissions");

        PageResponse<PermissionResponse> permissions = permissionQueryService.getAllPermissions(pageable);

        return ResponseEntity.ok(
                BaseResponse.success(permissions, "Permissions retrieved successfully")
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasPermission('PERMISSION', 'READ')")
    @Operation(summary = "Get permission by ID")
    public ResponseEntity<BaseResponse<PermissionResponse>> getPermissionById(@PathVariable UUID id) {

        log.info("GET /api/v1/permissions/{}", id);

        PermissionResponse permission = permissionQueryService.findPermissionById(id);

        return ResponseEntity.ok(
                BaseResponse.success(permission, "Permission retrieved successfully")
        );
    }

    @GetMapping("/name/{name}")
    @PreAuthorize("hasPermission('PERMISSION', 'READ')")
    @Operation(summary = "Get permission by name")
    public ResponseEntity<BaseResponse<PermissionResponse>> getPermissionByName(@PathVariable String name) {

        log.info("GET /api/v1/permissions/name/{}", name);

        PermissionResponse permission = permissionQueryService.findByName(name);

        return ResponseEntity.ok(
                BaseResponse.success(permission, "Permission retrieved successfully")
        );
    }

    @GetMapping("/system")
    @PreAuthorize("hasPermission('PERMISSION', 'READ')")
    @Operation(summary = "Get all system permissions")
    public ResponseEntity<BaseResponse<List<PermissionResponse>>> getSystemPermissions() {

        log.info("GET /api/v1/permissions/system");

        List<PermissionResponse> permissions = permissionQueryService.getSystemPermissions();

        return ResponseEntity.ok(
                BaseResponse.success(permissions, "System permissions retrieved successfully")
        );
    }

    @GetMapping("/resource/{resource}")
    @PreAuthorize("hasPermission('PERMISSION', 'READ')")
    @Operation(summary = "Get permissions by resource")
    public ResponseEntity<BaseResponse<List<PermissionResponse>>> getPermissionsByResource(
            @PathVariable String resource) {

        log.info("GET /api/v1/permissions/resource/{}", resource);

        List<PermissionResponse> permissions = permissionQueryService.getPermissionsByResource(resource);

        return ResponseEntity.ok(
                BaseResponse.success(permissions, "Permissions retrieved successfully")
        );
    }

    @GetMapping("/category/{category}")
    @PreAuthorize("hasPermission('PERMISSION', 'READ')")
    @Operation(summary = "Get permissions by category")
    public ResponseEntity<BaseResponse<List<PermissionResponse>>> getPermissionsByCategory(
            @PathVariable String category) {

        log.info("GET /api/v1/permissions/category/{}", category);

        List<PermissionResponse> permissions = permissionQueryService.getPermissionsByCategory(category);

        return ResponseEntity.ok(
                BaseResponse.success(permissions, "Permissions retrieved successfully")
        );
    }

    @GetMapping("/grouped/category")
    @PreAuthorize("hasPermission('PERMISSION', 'READ')")
    @Operation(summary = "Get permissions grouped by category")
    public ResponseEntity<BaseResponse<Map<String, List<PermissionResponse>>>> getPermissionsGroupedByCategory() {

        log.info("GET /api/v1/permissions/grouped/category");

        Map<String, List<PermissionResponse>> permissions = permissionQueryService.getPermissionsGroupedByCategory();

        return ResponseEntity.ok(
                BaseResponse.success(permissions, "Permissions grouped successfully")
        );
    }

    @GetMapping("/grouped/resource")
    @PreAuthorize("hasPermission('PERMISSION', 'READ')")
    @Operation(summary = "Get permissions grouped by resource")
    public ResponseEntity<BaseResponse<Map<String, List<PermissionResponse>>>> getPermissionsGroupedByResource() {

        log.info("GET /api/v1/permissions/grouped/resource");

        Map<String, List<PermissionResponse>> permissions = permissionQueryService.getPermissionsGroupedByResource();

        return ResponseEntity.ok(
                BaseResponse.success(permissions, "Permissions grouped successfully")
        );
    }

    @GetMapping("/resources")
    @PreAuthorize("hasPermission('PERMISSION', 'READ')")
    @Operation(summary = "Get all resources")
    public ResponseEntity<BaseResponse<List<String>>> getAllResources() {

        log.info("GET /api/v1/permissions/resources");

        List<String> resources = permissionQueryService.getAllResources();

        return ResponseEntity.ok(
                BaseResponse.success(resources, "Resources retrieved successfully")
        );
    }

    @PostMapping
    @PreAuthorize("hasPermission('PERMISSION', 'CREATE')")
    @Operation(summary = "Create permission")
    public ResponseEntity<BaseResponse<PermissionResponse>> createPermission(
            @Valid @RequestBody CreatePermissionRequest request) {

        log.info("POST /api/v1/permissions - {}:{}", request.getResource(), request.getAction());

        PermissionResponse permission = permissionCommandService.createPermission(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(BaseResponse.success(permission, "Permission created successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasPermission('PERMISSION', 'UPDATE')")
    @Operation(summary = "Update permission")
    public ResponseEntity<BaseResponse<PermissionResponse>> updatePermission(
            @PathVariable UUID id,
            @Valid @RequestBody UpdatePermissionRequest request) {

        log.info("PUT /api/v1/permissions/{}", id);

        PermissionResponse permission = permissionCommandService.updatePermission(id, request);

        return ResponseEntity.ok(
                BaseResponse.success(permission, "Permission updated successfully")
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasPermission('PERMISSION', 'DELETE')")
    @Operation(summary = "Delete permission")
    public ResponseEntity<BaseResponse<Void>> deletePermission(@PathVariable UUID id) {

        log.info("DELETE /api/v1/permissions/{}", id);

        permissionCommandService.deletePermission(id);

        return ResponseEntity.ok(
                BaseResponse.success(null, "Permission deleted successfully")
        );
    }
}