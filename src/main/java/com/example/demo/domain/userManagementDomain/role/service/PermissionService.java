package com.example.demo.domain.userManagementDomain.role.service;

import com.example.demo.domain.userManagementDomain.role.dto.PermissionDTO;
import com.example.demo.domain.userManagementDomain.role.entity.Permission;
import com.example.demo.domain.userManagementDomain.role.exception.permission.DuplicatePermissionNameException;
import com.example.demo.domain.userManagementDomain.role.exception.permission.InvalidPermissionException;
import com.example.demo.domain.userManagementDomain.role.exception.permission.PermissionNotFoundException;
import com.example.demo.domain.userManagementDomain.role.respository.PermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PermissionService {

    private final PermissionRepository permissionRepository;

    /**
     * Retrieves a Permission by its ID or throws if not found.
     *
     * @param id the permission ID
     * @return the found Permission entity
     * @throws PermissionNotFoundException if no permission exists with that ID
     */
    public Permission findByIdOrThrow(Long id) {
        return permissionRepository.findById(id)
                .orElseThrow(() ->
                        new PermissionNotFoundException("Permission not found with id: " + id)
                );
    }

    /**
     * Create a new permission by name. Throws if blank or duplicate.
     */
    @Transactional
    public PermissionDTO createPermission(String name) {
        if (name == null || name.isBlank()) {
            throw new InvalidPermissionException("Permission name must not be blank");
        }

        if (permissionRepository.findByName(name).isPresent()) {
            throw new DuplicatePermissionNameException(name);
        }

        Permission saved = permissionRepository.save(new Permission(name));
        return toDto(saved);
    }

    /**
     * Lookup a permission by name. Throws if not found.
     */
    public PermissionDTO getPermissionByName(String name) {
        Permission permission = permissionRepository.findByName(name)
                .orElseThrow(() -> new PermissionNotFoundException(name));
        return toDto(permission);
    }

    /**
     * Return all permissions as DTOs.
     */
    public List<PermissionDTO> getAllPermissions() {
        return permissionRepository.findAll()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Initialize a set of permission names.
     * Existing names are skipped.
     */
    @Transactional
    public void initializePermissions(List<String> permissions) {
        List<String> existing = permissionRepository.findAll()
                .stream()
                .map(Permission::getName)
                .collect(Collectors.toList());

        permissions.stream()
                .filter(name -> !existing.contains(name))
                .forEach(this::createPermission);
    }

    /**
     * Check existence by name.
     */
    public boolean existsByName(String name) {
        return permissionRepository.findByName(name).isPresent();
    }

    // -- Private mapping helpers ------------------------------------------------

    private PermissionDTO toDto(Permission permission) {
        return PermissionDTO.builder()
                .id(permission.getId())
                .name(permission.getName())
                .build();
    }

    private Permission toEntity(PermissionDTO dto) {
        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new InvalidPermissionException("Permission name must not be blank");
        }
        return new Permission(dto.getName());
    }
}
