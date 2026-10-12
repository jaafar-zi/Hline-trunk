package com.example.demo.modules.user.application.mapper;

import com.example.demo.common.mapper.BaseMapper;
import com.example.demo.modules.user.application.dto.request.CreatePermissionRequest;
import com.example.demo.modules.user.application.dto.request.UpdatePermissionRequest;
import com.example.demo.modules.user.application.dto.response.PermissionResponse;
import com.example.demo.modules.user.domain.model.Permission;
import org.mapstruct.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * MapStruct mapper for Permission entity <-> DTOs
 *
 * Features:
 * - Bidirectional mapping (Entity ↔ DTO)
 * - Permission name formatting (RESOURCE:ACTION)
 * - Role count calculation
 * - Category grouping
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
public abstract class PermissionMapper implements BaseMapper<Permission, PermissionResponse> {

    // ====================================
    // Entity to Response Mappings
    // ====================================

    /**
     * Map Permission entity to PermissionResponse (summary view)
     * Implementation of BaseMapper.toDto()
     */
    @Override
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", expression = "java(permission.getName())")
    @Mapping(target = "resource", expression = "java(extractResource(permission.getName()))")
    @Mapping(target = "action", expression = "java(extractAction(permission.getName()))")
    @Mapping(target = "description", expression = "java(normalizePermissionDescription(permission.getDescription()))")
    @Mapping(target = "category", expression = "java(permission.getCategory())")
    @Mapping(target = "systemPermission", expression = "java(permission.isSystemPermission())")
    @Mapping(target = "displayOrder", expression = "java(permission.getDisplayOrder())")
    @Mapping(target = "roleCount", expression = "java(getRoleCount(permission))")
    public abstract PermissionResponse toDto(Permission permission);

    /**
     * Alias for toDto() - for better readability in services
     */
    public PermissionResponse toResponse(Permission permission) {
        return toDto(permission);
    }

    /**
     * Map list of Permission entities to list of PermissionResponses
     */
    public List<PermissionResponse> toResponseList(List<Permission> permissions) {
        return toDtoList(permissions);
    }

    /**
     * Map set of Permission entities to set of PermissionResponses
     */
    public Set<PermissionResponse> toResponseSet(Set<Permission> permissions) {
        return toDtoSet(permissions);
    }

    // ====================================
    // Request to Entity Mappings
    // ====================================

    /**
     * Map CreatePermissionRequest to Permission entity
     */
    @Mapping(target = "name", expression = "java(buildPermissionName(request.getResource(), request.getAction()))")
    @Mapping(target = "resource", qualifiedByName = "normalizePermissionResource", source = "resource")
    @Mapping(target = "action", qualifiedByName = "normalizePermissionAction", source = "action")
    @Mapping(target = "description", qualifiedByName = "normalizePermissionDescription", source = "description")
    @Mapping(target = "category", qualifiedByName = "normalizePermissionCategory", source = "category")
    @Mapping(target = "displayOrder", source = "displayOrder")
    @Mapping(target = "systemPermission", constant = "false")
    @Mapping(target = "roles", ignore = true)
    public abstract Permission fromCreateRequest(CreatePermissionRequest request);

    /**
     * Implementation of BaseMapper.toEntity(PermissionResponse)
     * Not supported - cannot create entity from response DTO
     */
    @Override
    public Permission toEntity(PermissionResponse dto) {
        throw new UnsupportedOperationException("Cannot create Permission entity from PermissionResponse. Use fromCreateRequest() instead.");
    }

    // ====================================
    // Update Mappings
    // ====================================

    /**
     * Update existing Permission entity from UpdatePermissionRequest (PATCH)
     * Implementation of BaseMapper.updateEntityFromDto
     */
    @BeanMapping(
            nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
            nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS
    )
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "name", ignore = true) // Name is immutable (RESOURCE:ACTION)
    @Mapping(target = "resource", ignore = true) // Resource is immutable
    @Mapping(target = "action", ignore = true) // Action is immutable
    @Mapping(target = "description", qualifiedByName = "normalizePermissionDescription")
    @Mapping(target = "category", qualifiedByName = "normalizePermissionCategory")
    @Mapping(target = "systemPermission", ignore = true) // System permission flag is immutable
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    public abstract Permission updateEntityFromDto(UpdatePermissionRequest dto, @MappingTarget Permission entity);

    /**
     * Update permission with description only
     */
    public Permission withDescription(Permission permission, String description) {
        if (permission == null) {
            return null;
        }
        Permission updated = toShallowCopy(permission);
        updated.setDescription(normalizePermissionDescription(description));
        return updated;
    }

    /**
     * Update permission with category only
     */
    public Permission withCategory(Permission permission, String category) {
        if (permission == null) {
            return null;
        }
        Permission updated = toShallowCopy(permission);
        updated.setCategory(normalizePermissionCategory(category));
        return updated;
    }

    /**
     * Update permission with display order only
     */
    public Permission withDisplayOrder(Permission permission, Integer displayOrder) {
        if (permission == null) {
            return null;
        }
        Permission updated = toShallowCopy(permission);
        updated.setDisplayOrder(displayOrder);
        return updated;
    }

    // ====================================
    // Helper Methods (MapStruct-visible)
    // ====================================

    /**
     * Get role count
     */
    protected Integer getRoleCount(Permission permission) {
        if (permission == null) {
            return 0;
        }
        return permission.getRoleCount();
    }

    /**
     * Build permission name from resource and action
     */
    protected String buildPermissionName(String resource, String action) {
        if (resource == null || resource.isBlank()) {
            throw new IllegalArgumentException("Resource cannot be null or empty");
        }
        if (action == null || action.isBlank()) {
            throw new IllegalArgumentException("Action cannot be null or empty");
        }

        String normalizedResource = normalizePermissionResource(resource);
        String normalizedAction = normalizePermissionAction(action);

        return normalizedResource + ":" + normalizedAction;
    }

    // ====================================
    // Normalization Methods (with @Named)
    // ====================================

    /**
     * Normalize resource name (uppercase)
     * Used exclusively for Permission.resource field
     */
    @Named("normalizePermissionResource")
    protected String normalizePermissionResource(String resource) {
        if (resource == null || resource.isBlank()) {
            throw new IllegalArgumentException("Resource cannot be null or empty");
        }
        return resource.trim().toUpperCase();
    }

    /**
     * Normalize action name (uppercase)
     * Used exclusively for Permission.action field
     */
    @Named("normalizePermissionAction")
    protected String normalizePermissionAction(String action) {
        if (action == null || action.isBlank()) {
            throw new IllegalArgumentException("Action cannot be null or empty");
        }
        return action.trim().toUpperCase();
    }

    /**
     * Normalize permission description (trimmed, preserve case)
     * Used exclusively for Permission.description field
     */
    @Named("normalizePermissionDescription")
    protected String normalizePermissionDescription(String description) {
        if (description == null) {
            return null;
        }
        String trimmed = description.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    /**
     * Normalize permission category (uppercase)
     * Used exclusively for Permission.category field
     */
    @Named("normalizePermissionCategory")
    protected String normalizePermissionCategory(String category) {
        if (category == null) {
            return null;
        }
        String trimmed = category.trim().toUpperCase();
        return trimmed.isEmpty() ? null : trimmed;
    }

    // ====================================
    // Utility Methods (Private - NOT visible to MapStruct)
    // ====================================

    /**
     * Parse permission name to extract resource
     * Private utility method - NOT used by MapStruct
     */
    private String extractResourceInternal(String permissionName) {
        if (permissionName == null || !permissionName.contains(":")) {
            return null;
        }
        return permissionName.split(":")[0];
    }

    /**
     * Parse permission name to extract action
     * Private utility method - NOT used by MapStruct
     */
    private String extractActionInternal(String permissionName) {
        if (permissionName == null || !permissionName.contains(":")) {
            return null;
        }
        String[] parts = permissionName.split(":");
        return parts.length > 1 ? parts[1] : null;
    }

    /**
     * Public API for extracting resource from permission name
     */
    public String extractResource(String permissionName) {
        return extractResourceInternal(permissionName);
    }

    /**
     * Public API for extracting action from permission name
     */
    public String extractAction(String permissionName) {
        return extractActionInternal(permissionName);
    }

    /**
     * Create shallow copy of permission (for immutable updates)
     */
    private Permission toShallowCopy(Permission permission) {
        return Permission.builder()
                .name(permission.getName())
                .resource(permission.getResource())
                .action(permission.getAction())
                .description(permission.getDescription())
                .category(permission.getCategory())
                .systemPermission(permission.getSystemPermission())
                .displayOrder(permission.getDisplayOrder())
                .build();
    }

    // ====================================
    // Mapping to Nested DTOs
    // ====================================

    /**
     * Map Permission to minimal info (name only)
     */
    public PermissionMinimalInfo toMinimalInfo(Permission permission) {
        if (permission == null) {
            return null;
        }

        return PermissionMinimalInfo.builder()
                .id(permission.getId())
                .name(permission.getName())
                .resource(permission.getResource())
                .action(permission.getAction())
                .category(permission.getCategory())
                .systemPermission(permission.getSystemPermission())
                .build();
    }

    /**
     * Map list to minimal info list
     */
    public List<PermissionMinimalInfo> toMinimalInfoList(List<Permission> permissions) {
        if (permissions == null || permissions.isEmpty()) {
            return Collections.emptyList();
        }

        return permissions.stream()
                .map(this::toMinimalInfo)
                .collect(Collectors.toList());
    }

    // ====================================
    // Nested DTOs
    // ====================================

    /**
     * Minimal permission information DTO
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class PermissionMinimalInfo implements java.io.Serializable {
        private static final long serialVersionUID = 1L;

        private UUID id;
        private String name;
        private String resource;
        private String action;
        private String category;
        private Boolean systemPermission;
    }

    // ====================================
    // Grouping and Organization
    // ====================================

    /**
     * Group permissions by category
     */
    public Map<String, List<PermissionResponse>> groupByCategory(List<Permission> permissions) {
        if (permissions == null || permissions.isEmpty()) {
            return Collections.emptyMap();
        }

        return permissions.stream()
                .collect(Collectors.groupingBy(
                        p -> Optional.ofNullable(p.getCategory()).orElse("UNCATEGORIZED"),
                        Collectors.mapping(this::toResponse, Collectors.toList())
                ));
    }

    /**
     * Group permissions by resource
     */
    public Map<String, List<PermissionResponse>> groupByResource(List<Permission> permissions) {
        if (permissions == null || permissions.isEmpty()) {
            return Collections.emptyMap();
        }

        return permissions.stream()
                .collect(Collectors.groupingBy(
                        Permission::getResource,
                        Collectors.mapping(this::toResponse, Collectors.toList())
                ));
    }

    /**
     * Group permissions by action
     */
    public Map<String, List<PermissionResponse>> groupByAction(List<Permission> permissions) {
        if (permissions == null || permissions.isEmpty()) {
            return Collections.emptyMap();
        }

        return permissions.stream()
                .collect(Collectors.groupingBy(
                        Permission::getAction,
                        Collectors.mapping(this::toResponse, Collectors.toList())
                ));
    }

    /**
     * Create hierarchical structure (Category -> Resource -> Permissions)
     */
    public Map<String, Map<String, List<PermissionResponse>>> createHierarchy(List<Permission> permissions) {
        if (permissions == null || permissions.isEmpty()) {
            return Collections.emptyMap();
        }

        return permissions.stream()
                .collect(Collectors.groupingBy(
                        p -> Optional.ofNullable(p.getCategory()).orElse("UNCATEGORIZED"),
                        Collectors.groupingBy(
                                Permission::getResource,
                                Collectors.mapping(this::toResponse, Collectors.toList())
                        )
                ));
    }

    // ====================================
    // Collection Utilities
    // ====================================

    /**
     * Extract permission IDs from collection
     */
    public Set<UUID> extractPermissionIds(Set<Permission> permissions) {
        if (permissions == null || permissions.isEmpty()) {
            return Collections.emptySet();
        }

        return permissions.stream()
                .map(Permission::getId)
                .collect(Collectors.toSet());
    }

    /**
     * Extract permission names from collection
     */
    public Set<String> extractPermissionNames(Set<Permission> permissions) {
        if (permissions == null || permissions.isEmpty()) {
            return Collections.emptySet();
        }

        return permissions.stream()
                .map(Permission::getName)
                .collect(Collectors.toSet());
    }

    /**
     * Filter system permissions
     */
    public List<PermissionResponse> filterSystemPermissions(List<Permission> permissions) {
        if (permissions == null || permissions.isEmpty()) {
            return Collections.emptyList();
        }

        return permissions.stream()
                .filter(p -> Boolean.TRUE.equals(p.getSystemPermission()))
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Filter non-system permissions
     */
    public List<PermissionResponse> filterNonSystemPermissions(List<Permission> permissions) {
        if (permissions == null || permissions.isEmpty()) {
            return Collections.emptyList();
        }

        return permissions.stream()
                .filter(p -> !Boolean.TRUE.equals(p.getSystemPermission()))
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Filter permissions by resource
     */
    public List<PermissionResponse> filterByResource(List<Permission> permissions, String resource) {
        if (permissions == null || permissions.isEmpty() || resource == null) {
            return Collections.emptyList();
        }

        String normalizedResource = resource.toUpperCase();
        return permissions.stream()
                .filter(p -> normalizedResource.equals(p.getResource()))
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Filter permissions by action
     */
    public List<PermissionResponse> filterByAction(List<Permission> permissions, String action) {
        if (permissions == null || permissions.isEmpty() || action == null) {
            return Collections.emptyList();
        }

        String normalizedAction = action.toUpperCase();
        return permissions.stream()
                .filter(p -> normalizedAction.equals(p.getAction()))
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Filter permissions by category
     */
    public List<PermissionResponse> filterByCategory(List<Permission> permissions, String category) {
        if (permissions == null || permissions.isEmpty()) {
            return Collections.emptyList();
        }

        return permissions.stream()
                .filter(p -> category == null ? p.getCategory() == null : category.equals(p.getCategory()))
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Sort permissions by display order
     */
    public List<PermissionResponse> sortByDisplayOrder(List<Permission> permissions) {
        if (permissions == null || permissions.isEmpty()) {
            return Collections.emptyList();
        }

        return permissions.stream()
                .sorted(Comparator
                        .comparing((Permission p) -> Optional.ofNullable(p.getDisplayOrder()).orElse(Integer.MAX_VALUE))
                        .thenComparing(Permission::getName))
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Sort permissions by name
     */
    public List<PermissionResponse> sortByName(List<Permission> permissions) {
        if (permissions == null || permissions.isEmpty()) {
            return Collections.emptyList();
        }

        return permissions.stream()
                .sorted(Comparator.comparing(Permission::getName))
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Sort permissions by resource then action
     */
    public List<PermissionResponse> sortByResourceAndAction(List<Permission> permissions) {
        if (permissions == null || permissions.isEmpty()) {
            return Collections.emptyList();
        }

        return permissions.stream()
                .sorted(Comparator
                        .comparing(Permission::getResource)
                        .thenComparing(Permission::getAction))
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ====================================
    // Validation Helpers
    // ====================================

    /**
     * Validate permission name format (RESOURCE:ACTION)
     */
    public boolean isValidPermissionName(String name) {
        if (name == null || name.isBlank()) {
            return false;
        }

        // Must be in format RESOURCE:ACTION
        if (!name.contains(":")) {
            return false;
        }

        String[] parts = name.split(":");
        if (parts.length != 2) {
            return false;
        }

        // Both parts must be uppercase letters/numbers/underscores
        return parts[0].matches("^[A-Z0-9_]+$") && parts[1].matches("^[A-Z0-9_]+$");
    }

    /**
     * Check if permission can be modified
     */
    public boolean canModifyPermission(Permission permission) {
        if (permission == null) {
            return false;
        }

        // System permissions cannot be modified
        return !Boolean.TRUE.equals(permission.getSystemPermission());
    }

    /**
     * Check if permission can be deleted
     */
    public boolean canDeletePermission(Permission permission) {
        if (permission == null) {
            return false;
        }

        // Cannot delete system permissions
        if (Boolean.TRUE.equals(permission.getSystemPermission())) {
            return false;
        }

        // Cannot delete if assigned to roles
        return permission.getRoleCount() == 0;
    }

    // ====================================
    // Utility Methods for UI/Display
    // ====================================

    /**
     * Get all unique resources from permissions
     */
    public List<String> getUniqueResources(List<Permission> permissions) {
        if (permissions == null || permissions.isEmpty()) {
            return Collections.emptyList();
        }

        return permissions.stream()
                .map(Permission::getResource)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * Get all unique actions from permissions
     */
    public List<String> getUniqueActions(List<Permission> permissions) {
        if (permissions == null || permissions.isEmpty()) {
            return Collections.emptyList();
        }

        return permissions.stream()
                .map(Permission::getAction)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * Get all unique categories from permissions
     */
    public List<String> getUniqueCategories(List<Permission> permissions) {
        if (permissions == null || permissions.isEmpty()) {
            return Collections.emptyList();
        }

        return permissions.stream()
                .map(p -> Optional.ofNullable(p.getCategory()).orElse("UNCATEGORIZED"))
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * Create permission matrix (Resource x Action)
     */
    public Map<String, Map<String, PermissionResponse>> createPermissionMatrix(List<Permission> permissions) {
        if (permissions == null || permissions.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, Map<String, PermissionResponse>> matrix = new HashMap<>();

        permissions.forEach(permission -> {
            matrix.computeIfAbsent(permission.getResource(), k -> new HashMap<>())
                    .put(permission.getAction(), toResponse(permission));
        });

        return matrix;
    }

    /**
     * Generate suggested permissions for a resource
     * (CREATE, READ, UPDATE, DELETE)
     */
    public List<String> generateCrudPermissions(String resource) {
        if (resource == null || resource.isBlank()) {
            return Collections.emptyList();
        }

        String normalizedResource = normalizePermissionResource(resource);

        return List.of(
                buildPermissionName(normalizedResource, "CREATE"),
                buildPermissionName(normalizedResource, "READ"),
                buildPermissionName(normalizedResource, "UPDATE"),
                buildPermissionName(normalizedResource, "DELETE")
        );
    }
}