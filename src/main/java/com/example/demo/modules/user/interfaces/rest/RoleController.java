package com.example.demo.modules.user.interfaces.rest;

import com.example.demo.common.base.dto.BaseResponse;
import com.example.demo.common.base.dto.PageResponse;
import com.example.demo.modules.user.application.command.RoleCommandService;
import com.example.demo.modules.user.application.dto.request.AssignPermissionsRequest;
import com.example.demo.modules.user.application.dto.request.CreateRoleRequest;
import com.example.demo.modules.user.application.dto.request.UpdateRoleRequest;
import com.example.demo.modules.user.application.dto.response.RoleResponse;
import com.example.demo.modules.user.application.query.RoleQueryService;
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

import java.util.List;
import java.util.UUID;

/**
 * Role REST Controller
 *
 * @author Architecture Team
 * @version 1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
@Tag(name = "Roles", description = "Role management APIs")
public class RoleController {

    private final RoleCommandService roleCommandService;
    private final RoleQueryService roleQueryService;

    @GetMapping
    @PreAuthorize("hasPermission('ROLE', 'READ')")
    @Operation(summary = "Get all roles")
    public ResponseEntity<BaseResponse<PageResponse<RoleResponse>>> getAllRoles(
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {

        log.info("GET /api/v1/roles");

        PageResponse<RoleResponse> roles = roleQueryService.getAllRoles(pageable);

        return ResponseEntity.ok(
                BaseResponse.success(roles, "Roles retrieved successfully")
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasPermission('ROLE', 'READ')")
    @Operation(summary = "Get role by ID")
    public ResponseEntity<BaseResponse<RoleResponse>> getRoleById(@PathVariable UUID id) {

        log.info("GET /api/v1/roles/{}", id);

        RoleResponse role = roleQueryService.findRoleById(id);

        return ResponseEntity.ok(
                BaseResponse.success(role, "Role retrieved successfully")
        );
    }

    @GetMapping("/name/{name}")
    @PreAuthorize("hasPermission('ROLE', 'READ')")
    @Operation(summary = "Get role by name")
    public ResponseEntity<BaseResponse<RoleResponse>> getRoleByName(@PathVariable String name) {

        log.info("GET /api/v1/roles/name/{}", name);

        RoleResponse role = roleQueryService.findByName(name);

        return ResponseEntity.ok(
                BaseResponse.success(role, "Role retrieved successfully")
        );
    }

    @GetMapping("/system")
    @PreAuthorize("hasPermission('ROLE', 'READ')")
    @Operation(summary = "Get all system roles")
    public ResponseEntity<BaseResponse<List<RoleResponse>>> getSystemRoles() {

        log.info("GET /api/v1/roles/system");

        List<RoleResponse> roles = roleQueryService.getSystemRoles();

        return ResponseEntity.ok(
                BaseResponse.success(roles, "System roles retrieved successfully")
        );
    }

    @PostMapping
    @PreAuthorize("hasPermission('ROLE', 'CREATE')")
    @Operation(summary = "Create role")
    public ResponseEntity<BaseResponse<RoleResponse>> createRole(
            @Valid @RequestBody CreateRoleRequest request) {

        log.info("POST /api/v1/roles - name: {}", request.getName());

        RoleResponse role = roleCommandService.createRole(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(BaseResponse.success(role, "Role created successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasPermission('ROLE', 'UPDATE')")
    @Operation(summary = "Update role")
    public ResponseEntity<BaseResponse<RoleResponse>> updateRole(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateRoleRequest request) {

        log.info("PUT /api/v1/roles/{}", id);

        RoleResponse role = roleCommandService.updateRole(id, request);

        return ResponseEntity.ok(
                BaseResponse.success(role, "Role updated successfully")
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasPermission('ROLE', 'DELETE')")
    @Operation(summary = "Delete role")
    public ResponseEntity<BaseResponse<Void>> deleteRole(@PathVariable UUID id) {

        log.info("DELETE /api/v1/roles/{}", id);

        roleCommandService.deleteRole(id);

        return ResponseEntity.ok(
                BaseResponse.success(null, "Role deleted successfully")
        );
    }

    @PostMapping("/{id}/permissions")
    @PreAuthorize("hasPermission('ROLE', 'UPDATE')")
    @Operation(summary = "Assign permissions to role")
    public ResponseEntity<BaseResponse<RoleResponse>> assignPermissions(
            @PathVariable UUID id,
            @Valid @RequestBody AssignPermissionsRequest request) {

        log.info("POST /api/v1/roles/{}/permissions", id);

        RoleResponse role = roleCommandService.assignPermissions(id, request.getPermissionIds());

        return ResponseEntity.ok(
                BaseResponse.success(role, "Permissions assigned successfully")
        );
    }

    @DeleteMapping("/{roleId}/permissions/{permissionId}")
    @PreAuthorize("hasPermission('ROLE', 'UPDATE')")
    @Operation(summary = "Remove permission from role")
    public ResponseEntity<BaseResponse<RoleResponse>> removePermission(
            @PathVariable UUID roleId,
            @PathVariable UUID permissionId) {

        log.info("DELETE /api/v1/roles/{}/permissions/{}", roleId, permissionId);

        RoleResponse role = roleCommandService.removePermission(roleId, permissionId);

        return ResponseEntity.ok(
                BaseResponse.success(role, "Permission removed successfully")
        );
    }
}