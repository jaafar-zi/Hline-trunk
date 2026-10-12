package com.example.demo.modules.user.application.mapper;

import com.example.demo.common.mapper.BaseMapper;
import com.example.demo.modules.user.application.dto.request.CreateRoleRequest;
import com.example.demo.modules.user.application.dto.request.UpdateRoleRequest;
import com.example.demo.modules.user.application.dto.response.RoleResponse;
import com.example.demo.modules.user.domain.model.Permission;
import com.example.demo.modules.user.domain.model.Role;
import org.mapstruct.*;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * MapStruct mapper for Role entity <-> DTOs
 *
 * Features:
 * - Bidirectional mapping (Entity ↔ DTO)
 * - Permission name extraction
 * - User count calculation
 * - Partial updates (PATCH operations)
 * - Null-safe operations
 *
 * @author Architecture Team
 * @version 1.0
 */
@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
public abstract class RoleMapper implements BaseMapper<Role, RoleResponse> {

    // ====================================
    // Entity to Response Mappings
    // ====================================

    /**
     * Map Role entity to RoleResponse (summary view)
     * Implementation of BaseMapper.toDto()
     */
    @Override
    @Mapping(target = "permissionCount", expression = "java(getPermissionCount(role))")
    @Mapping(target = "userCount", expression = "java(getUserCount(role))")
    @Mapping(target = "permissions", expression = "java(extractPermissionNames(role))")
    public abstract RoleResponse toDto(Role role);

    /**
     * Alias for toDto() - for better readability in services
     */
    public RoleResponse toResponse(Role role) {
        return toDto(role);
    }

    /**
     * Map list of Role entities to list of RoleResponses
     */
    public java.util.List<RoleResponse> toResponseList(java.util.List<Role> roles) {
        return toDtoList(roles);
    }

    // ====================================
    // Request to Entity Mappings
    // ====================================

    /**
     * Map CreateRoleRequest to Role entity
     */
    @Mapping(target = "name", expression = "java(normalizeRoleName(request.getName()))")
    @Mapping(target = "displayName", source = "displayName")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "priority", source = "priority")
    @Mapping(target = "systemRole", constant = "false")
    @Mapping(target = "permissions", ignore = true)
    @Mapping(target = "users", ignore = true)
    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "childRoles", ignore = true)
    public abstract Role fromCreateRequest(CreateRoleRequest request);

    /**
     * Implementation of BaseMapper.toEntity(RoleResponse)
     * Not supported - cannot create entity from response DTO
     */
    @Override
    public Role toEntity(RoleResponse dto) {
        throw new UnsupportedOperationException("Cannot create Role entity from RoleResponse. Use fromCreateRequest() instead.");
    }

    // ====================================
    // Update Mappings
    // ====================================

    /**
     * Update existing Role entity from UpdateRoleRequest (PATCH)
     * Implementation of BaseMapper.updateEntityFromDto
     */
    @BeanMapping(
            nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
            nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS
    )
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "name", ignore = true) // Role name is immutable
    @Mapping(target = "systemRole", ignore = true) // System role flag is immutable
    @Mapping(target = "permissions", ignore = true) // Handled separately in service
    @Mapping(target = "users", ignore = true)
    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "childRoles", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    public abstract Role updateEntityFromDto(UpdateRoleRequest dto, @MappingTarget Role entity);

    /**
     * Update role with display name only
     */
    public Role withDisplayName(Role role, String displayName) {
        if (role == null) {
            return null;
        }
        Role updated = toShallowCopy(role);
        updated.setDisplayName(displayName);
        return updated;
    }

    /**
     * Update role with description only
     */
    public Role withDescription(Role role, String description) {
        if (role == null) {
            return null;
        }
        Role updated = toShallowCopy(role);
        updated.setDescription(description);
        return updated;
    }

    /**
     * Update role with priority only
     */
    public Role withPriority(Role role, Integer priority) {
        if (role == null) {
            return null;
        }
        Role updated = toShallowCopy(role);
        updated.setPriority(priority);
        return updated;
    }

    // ====================================
    // Helper Methods
    // ====================================

    /**
     * Extract permission names from role
     */
    protected Set<String> extractPermissionNames(Role role) {
        if (role == null || role.getPermissions() == null || role.getPermissions().isEmpty()) {
            return Collections.emptySet();
        }

        return role.getPermissions().stream()
                .map(Permission::getName)
                .collect(Collectors.toSet());
    }

    /**
     * Get permission count
     */
    protected Integer getPermissionCount(Role role) {
        if (role == null || role.getPermissions() == null) {
            return 0;
        }
        return role.getPermissions().size();
    }

    /**
     * Get user count
     */
    protected Integer getUserCount(Role role) {
        if (role == null) {
            return 0;
        }
        return role.getUserCount();
    }

    /**
     * Normalize role name (ensure ROLE_ prefix)
     */
    protected String normalizeRoleName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Role name cannot be null or empty");
        }

        String normalized = name.trim().toUpperCase();

        if (!normalized.startsWith("ROLE_")) {
            normalized = "ROLE_" + normalized;
        }

        return normalized;
    }

    /**
     * Create shallow copy of role (for immutable updates)
     */
    private Role toShallowCopy(Role role) {
        return Role.builder()
                .name(role.getName())
                .displayName(role.getDisplayName())
                .description(role.getDescription())
                .systemRole(role.getSystemRole())
                .priority(role.getPriority())
                .parent(role.getParent())
                .build();
    }

    // ====================================
    // Mapping to Nested DTOs
    // ====================================

    /**
     * Map Role to nested RoleResponse (for UserDetailResponse)
     */
    @Mapping(target = "permissions", ignore = true)
    @Mapping(target = "permissionCount", expression = "java(getPermissionCount(role))")
    @Mapping(target = "userCount", expression = "java(getUserCount(role))")
    public abstract RoleResponse toNestedRoleResponse(Role role);

    /**
     * Map set of Roles to set of nested RoleResponses
     */
    public Set<RoleResponse> toNestedRoleResponseSet(Set<Role> roles) {
        if (roles == null || roles.isEmpty()) {
            return Collections.emptySet();
        }

        return roles.stream()
                .map(this::toNestedRoleResponse)
                .collect(Collectors.toSet());
    }

    // ====================================
    // Query Methods
    // ====================================

    /**
     * Map to minimal role info (ID and name only)
     */
    public RoleMinimalInfo toMinimalInfo(Role role) {
        if (role == null) {
            return null;
        }

        return RoleMinimalInfo.builder()
                .id(role.getId())
                .name(role.getName())
                .displayName(role.getDisplayName())
                .systemRole(role.getSystemRole())
                .priority(role.getPriority())
                .build();
    }

    /**
     * Map list to minimal info list
     */
    public java.util.List<RoleMinimalInfo> toMinimalInfoList(java.util.List<Role> roles) {
        if (roles == null || roles.isEmpty()) {
            return Collections.emptyList();
        }

        return roles.stream()
                .map(this::toMinimalInfo)
                .collect(Collectors.toList());
    }

    // ====================================
    // Nested DTOs
    // ====================================

    /**
     * Minimal role information DTO
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class RoleMinimalInfo implements java.io.Serializable {
        private static final long serialVersionUID = 1L;

        private java.util.UUID id;
        private String name;
        private String displayName;
        private Boolean systemRole;
        private Integer priority;
    }

    // ====================================
    // Validation Helpers
    // ====================================

    /**
     * Validate role name format
     */
    protected boolean isValidRoleName(String name) {
        if (name == null || name.isBlank()) {
            return false;
        }

        String normalized = name.toUpperCase();

        // Must start with ROLE_ and contain only uppercase letters, numbers, and underscores
        return normalized.matches("^ROLE_[A-Z0-9_]+$");
    }

    /**
     * Check if role can be modified
     */
    protected boolean canModifyRole(Role role) {
        if (role == null) {
            return false;
        }

        // System roles cannot be modified
        return !Boolean.TRUE.equals(role.getSystemRole());
    }

    // ====================================
    // Collection Utilities
    // ====================================

    /**
     * Extract role IDs from collection
     */
    public Set<java.util.UUID> extractRoleIds(Set<Role> roles) {
        if (roles == null || roles.isEmpty()) {
            return Collections.emptySet();
        }

        return roles.stream()
                .map(Role::getId)
                .collect(Collectors.toSet());
    }

    /**
     * Extract role names from collection
     */
    public Set<String> extractRoleNames(Set<Role> roles) {
        if (roles == null || roles.isEmpty()) {
            return Collections.emptySet();
        }

        return roles.stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
    }

    /**
     * Group roles by priority
     */
    public java.util.Map<Integer, java.util.List<RoleResponse>> groupByPriority(java.util.List<Role> roles) {
        if (roles == null || roles.isEmpty()) {
            return Collections.emptyMap();
        }

        return roles.stream()
                .collect(Collectors.groupingBy(
                        role -> Optional.ofNullable(role.getPriority()).orElse(0),
                        Collectors.mapping(this::toResponse, Collectors.toList())
                ));
    }

    /**
     * Filter system roles
     */
    public java.util.List<RoleResponse> filterSystemRoles(java.util.List<Role> roles) {
        if (roles == null || roles.isEmpty()) {
            return Collections.emptyList();
        }

        return roles.stream()
                .filter(role -> Boolean.TRUE.equals(role.getSystemRole()))
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Filter non-system roles
     */
    public java.util.List<RoleResponse> filterNonSystemRoles(java.util.List<Role> roles) {
        if (roles == null || roles.isEmpty()) {
            return Collections.emptyList();
        }

        return roles.stream()
                .filter(role -> !Boolean.TRUE.equals(role.getSystemRole()))
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Sort roles by priority (descending)
     */
    public java.util.List<RoleResponse> sortByPriority(java.util.List<Role> roles) {
        if (roles == null || roles.isEmpty()) {
            return Collections.emptyList();
        }

        return roles.stream()
                .sorted((r1, r2) -> {
                    Integer p1 = Optional.ofNullable(r1.getPriority()).orElse(0);
                    Integer p2 = Optional.ofNullable(r2.getPriority()).orElse(0);
                    return p2.compareTo(p1); // Descending
                })
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}