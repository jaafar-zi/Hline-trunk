package com.example.demo.domain.userManagementDomain.role.service;

import com.example.demo.domain.userManagementDomain.role.dto.RoleCreateDto;
import com.example.demo.domain.userManagementDomain.role.dto.RoleDTO;
import com.example.demo.domain.userManagementDomain.role.entity.Permission;
import com.example.demo.domain.userManagementDomain.role.entity.Role;
import com.example.demo.domain.userManagementDomain.role.exception.permission.PermissionNotFoundException;
import com.example.demo.domain.userManagementDomain.role.exception.role.DuplicateRoleNameException;
import com.example.demo.domain.userManagementDomain.role.exception.role.InvalidRoleException;
import com.example.demo.domain.userManagementDomain.role.exception.role.RoleHasUsersException;
import com.example.demo.domain.userManagementDomain.role.exception.role.RoleNotFoundException;
import com.example.demo.domain.userManagementDomain.role.respository.PermissionRepository;
import com.example.demo.domain.userManagementDomain.role.respository.RoleRepository;
import com.example.demo.domain.userManagementDomain.user.dto.UserDTO;
import com.example.demo.domain.userManagementDomain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepo;
    private final PermissionRepository permissionRepo;
    private final UserService userService;

    public Page<RoleDTO> findAll(Pageable pageable) {
        return roleRepo.findAll(pageable)
                .map(this::toDto);
    }

    public List<RoleDTO> findAll() {
        return roleRepo.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public RoleDTO findById(Long id) {
        Role role = roleRepo.findById(id)
                .orElseThrow(() -> new RoleNotFoundException(id));
        return toDto(role);
    }

    public List<RoleDTO> findByPermission(String permissionName) {
        return roleRepo.findAll().stream()
                .filter(r -> r.getPermissions().stream()
                        .anyMatch(p -> p.getName().equals(permissionName)))
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<UserDTO> getUsersByRole(Long roleId) {
        Role role = roleRepo.findById(roleId)
                .orElseThrow(() -> new RoleNotFoundException(roleId));
        return role.getUsers().stream()
                .map(userService::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public RoleDTO create(RoleCreateDto dto) {
        validateRoleName(dto.getName());
        if (roleRepo.findByName(dto.getName()).isPresent()) {
            throw new DuplicateRoleNameException(dto.getName());
        }
        Role role = toEntity(dto);
        Role saved = roleRepo.save(role);
        return toDto(saved);
    }

    @Transactional
    public RoleDTO update(Long id, RoleDTO dto) {
        Role existing = roleRepo.findById(id)
                .orElseThrow(() -> new RoleNotFoundException(id));
        validateRoleName(dto.getName());
        if (roleRepo.findByName(dto.getName()).isPresent() && !existing.getName().equals(dto.getName())) {
            throw new DuplicateRoleNameException(dto.getName());
        }
        existing.setName(dto.getName());
        existing.setPermissions(resolvePermissions(dto.getPermissionIds()));
        Role updated = roleRepo.save(existing);
        return toDto(updated);
    }

    @Transactional
    public void delete(Long id) {
        Role role = roleRepo.findById(id)
                .orElseThrow(() -> new RoleNotFoundException(id));
        if (!role.getUsers().isEmpty()) {
            throw new RoleHasUsersException(id);
        }
        roleRepo.deleteById(id);
    }

    // -- Private helpers -------------------------------------------------------

    private void validateRoleName(String name) {
        if (name == null || name.isBlank()) {
            throw new InvalidRoleException("Role name must not be blank");
        }
    }

    private RoleDTO toDto(Role role) {
        return RoleDTO.builder()
                .id(role.getId())
                .name(role.getName())
                .permissionIds(
                        role.getPermissions().stream()
                                .map(Permission::getId)
                                .collect(Collectors.toSet())
                )
                .createdDate(role.getCreatedDate())
                .lastModifiedDate(role.getLastModifiedDate())
                .build();
    }

    private Role toEntity(RoleCreateDto dto) {
        return Role.builder()
                .name(dto.getName())
                .permissions(resolvePermissions(dto.getPermissionIds()))
                .build();
    }

    private Set<Permission> resolvePermissions(Set<Long> ids) {
        return ids.stream()
                .map(pid -> permissionRepo.findById(pid)
                        .orElseThrow(() -> new PermissionNotFoundException(pid)))
                .collect(Collectors.toSet());
    }
}
