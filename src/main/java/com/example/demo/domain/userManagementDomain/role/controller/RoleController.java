package com.example.demo.domain.userManagementDomain.role.controller;

import com.example.demo.domain.userManagementDomain.role.dto.CreatePermissionDto;
import com.example.demo.domain.userManagementDomain.role.dto.PermissionDTO;
import com.example.demo.domain.userManagementDomain.role.dto.RoleCreateDto;
import com.example.demo.domain.userManagementDomain.role.dto.RoleDTO;
import com.example.demo.domain.userManagementDomain.role.entity.DefaultPermission;
import com.example.demo.domain.userManagementDomain.role.entity.Permission;
import com.example.demo.domain.userManagementDomain.role.service.PermissionService;
import com.example.demo.domain.userManagementDomain.role.service.RoleService;
import com.example.demo.domain.userManagementDomain.user.dto.UserDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;

@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
@Validated
public class RoleController {

    private final RoleService roleService;
    private final PermissionService permissionService;

    // -------------------- Role Endpoints --------------------

    @PostMapping
    public ResponseEntity<RoleDTO> createRole(@Valid @RequestBody RoleCreateDto roleDto) {
        RoleDTO created = roleService.create(roleDto);
        return ResponseEntity.ok(created);
    }

    @GetMapping
    public ResponseEntity<Page<RoleDTO>> getAllRoles(Pageable pageable) {
        Page<RoleDTO> roles = roleService.findAll(pageable);
        return ResponseEntity.ok(roles);
    }

    @GetMapping("/all")
    public ResponseEntity<List<RoleDTO>> getAllRolesList() {
        List<RoleDTO> roles = roleService.findAll();
        return ResponseEntity.ok(roles);
    }

    @GetMapping("/{roleId}")
    public ResponseEntity<RoleDTO> getRoleById(@PathVariable Long roleId) {
        RoleDTO dto = roleService.findById(roleId);
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/{roleId}")
    public ResponseEntity<RoleDTO> updateRole(
            @PathVariable Long roleId,
            @Valid @RequestBody RoleDTO roleDto) {
        RoleDTO updated = roleService.update(roleId, roleDto);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/{roleId}/permissions")
    public ResponseEntity<RoleDTO> assignPermissionsToRole(
            @PathVariable Long roleId,
            @RequestBody List<Long> permissionIds) {
        RoleDTO dto = roleService.findById(roleId);
        dto.setPermissionIds(new HashSet<>(permissionIds));
        RoleDTO updated = roleService.update(roleId, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{roleId}")
    public ResponseEntity<Void> deleteRole(@PathVariable Long roleId) {
        roleService.delete(roleId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{roleId}/users")
    public ResponseEntity<List<UserDTO>> getUsersByRole(@PathVariable Long roleId) {
        List<UserDTO> users = roleService.getUsersByRole(roleId);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/permissions/{permissionName}/roles")
    public ResponseEntity<List<RoleDTO>> findRolesByPermission(@PathVariable String permissionName) {
        List<RoleDTO> roles = roleService.findByPermission(permissionName);
        return ResponseEntity.ok(roles);
    }

    // -------------------- Permission Endpoints --------------------

    // Create a permission
    @PostMapping("/permissions")
    public ResponseEntity<PermissionDTO> createPermission(@RequestBody CreatePermissionDto permission) {
        PermissionDTO created = permissionService.createPermission(permission.getName());
        return ResponseEntity.ok(created);
    }

    // Get a permission by name
    @GetMapping("/permissions/{permissionName}")
    public ResponseEntity<PermissionDTO> getPermissionByName(@PathVariable String permissionName) {
        PermissionDTO dto = permissionService.getPermissionByName(permissionName);
        return ResponseEntity.ok(dto);
    }

    // Get all permissions
    @GetMapping("/permissions")
    public ResponseEntity<List<PermissionDTO>> getAllPermissions() {
        List<PermissionDTO> permissions = permissionService.getAllPermissions();
        return ResponseEntity.ok(permissions);
    }

    // Initialize default permissions
    @PostMapping("/permissions/init")
    public ResponseEntity<?> initializeDefaultPermissions() {
        try {
            List<Permission> defaults = DefaultPermission.mapToPermissionEntities();
            for (Permission p : defaults) {
                if (!permissionService.existsByName(p.getName())) {
                    permissionService.createPermission(p.getName());
                }
            }
            return ResponseEntity.ok("All default permissions initialized successfully.");
        } catch (Exception ex) {
            Throwable root = getRootCause(ex);
            return ResponseEntity.internalServerError()
                    .body("Permission initialization failed: " + root.getMessage());
        }
    }

    // Bulk initialize a custom list of permissions
    @PostMapping("/permissions/init-custom")
    public ResponseEntity<?> initializePermissions(@RequestBody List<String> permissionNames) {
        try {
            permissionService.initializePermissions(permissionNames);
            return ResponseEntity.ok("Custom permissions initialized successfully.");
        } catch (Exception ex) {
            Throwable root = getRootCause(ex);
            return ResponseEntity.internalServerError()
                    .body("Custom permission initialization failed: " + root.getMessage());
        }
    }

    // Check if a permission exists by name (optional endpoint)
    @GetMapping("/permissions/exists/{permissionName}")
    public ResponseEntity<Boolean> permissionExists(@PathVariable String permissionName) {
        boolean exists = permissionService.existsByName(permissionName);
        return ResponseEntity.ok(exists);
    }

    // -------------------- Utility --------------------

    private Throwable getRootCause(Throwable t) {
        Throwable cause = t;
        while (cause.getCause() != null && cause != cause.getCause()) {
            cause = cause.getCause();
        }
        return cause;
    }
}


