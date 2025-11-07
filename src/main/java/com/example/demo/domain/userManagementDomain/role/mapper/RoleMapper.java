package com.example.demo.domain.userManagementDomain.role.mapper;

import com.example.demo.domain.userManagementDomain.role.dto.RoleCreateDto;
import com.example.demo.domain.userManagementDomain.role.dto.RoleDTO;
import com.example.demo.domain.userManagementDomain.role.entity.Permission;
import com.example.demo.domain.userManagementDomain.role.entity.Role;
import com.example.demo.domain.userManagementDomain.role.service.PermissionService;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public abstract class RoleMapper {

    @Autowired
    protected PermissionService permissionService;

    // === Entity → DTO ===
    @Mapping(target = "permissionIds", expression = "java(mapPermissionIds(role.getPermissions()))")
    public abstract RoleDTO toDto(Role role);

    // === CreateDto → Entity ===
    @Mapping(target = "permissions", expression = "java(resolvePermissions(dto.getPermissionIds()))")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    public abstract Role toEntity(RoleCreateDto dto);

    // --- Custom mapping helpers ---
    protected Set<Long> mapPermissionIds(Set<Permission> permissions) {
        return permissions == null
                ? null
                : permissions.stream().map(Permission::getId).collect(Collectors.toSet());
    }

    protected Set<Permission> resolvePermissions(Set<Long> permissionIds) {
        if (permissionIds == null || permissionIds.isEmpty()) {
            return Set.of();
        }
        return permissionIds.stream()
                .map(permissionService::findByIdOrThrow) // your PermissionService should provide this
                .collect(Collectors.toSet());
    }
}
