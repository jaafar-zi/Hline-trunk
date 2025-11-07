package com.example.demo.domain.userManagementDomain.user.service;


import com.example.demo.domain.userManagementDomain.role.dto.RoleDTO;
import com.example.demo.domain.userManagementDomain.role.entity.Role;
import com.example.demo.domain.userManagementDomain.role.respository.RoleRepository;
import com.example.demo.domain.userManagementDomain.user.dto.CreateOrUpdateUserGroupDto;
import com.example.demo.domain.userManagementDomain.user.dto.UserDTO;
import com.example.demo.domain.userManagementDomain.user.dto.UserGroupDTO;
import com.example.demo.domain.userManagementDomain.user.entity.User;
import com.example.demo.domain.userManagementDomain.user.entity.UserGroup;
import com.example.demo.domain.userManagementDomain.user.repository.UserGroupRepository;
import com.example.demo.domain.userManagementDomain.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserGroupService {

    private final UserGroupRepository userGroupRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    @Autowired
    public UserGroupService(UserGroupRepository userGroupRepository,
                            RoleRepository roleRepository,
                            UserRepository userRepository) {
        this.userGroupRepository = userGroupRepository;
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
    }

    /*** Guard Methods ***/

    private void assertExists(Long id) {
        if (!userGroupRepository.existsById(id)) {
            throw new RuntimeException("UserGroup not found with id: " + id);
        }
    }

    private UserGroup fetch(Long id) {
        return userGroupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("UserGroup not found with id: " + id));
    }

    /*** Public API Methods ***/

    public Page<UserGroupDTO> findAll(Pageable pageable) {
        return userGroupRepository.findAll(pageable)
                .map(this::toDto);
    }

    public UserGroupDTO findById(Long id) {
        return toDto(fetch(id));
    }

    public UserGroupDTO findByName(String name) {
        return userGroupRepository.findByName(name)
                .map(this::toDto)
                .orElseThrow(() -> new RuntimeException("UserGroup not found with name: " + name));
    }

    public UserGroupDTO create(CreateOrUpdateUserGroupDto dto) {
        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new IllegalArgumentException("UserGroup name must not be blank");
        }
        if (userGroupRepository.existsByName(dto.getName())) {
            throw new IllegalArgumentException("UserGroup already exists with name: " + dto.getName());
        }

        UserGroup group = toEntity(dto);
        return toDto(userGroupRepository.save(group));
    }

    public UserGroupDTO update(Long id, CreateOrUpdateUserGroupDto dto) {
        UserGroup existing = fetch(id);
        existing.setName(dto.getName());
        existing.setRoles(resolveRoles(dto.getRoleIds()));
        existing.setUsers(resolveUsers(dto.getUserIds()));
        return toDto(userGroupRepository.save(existing));
    }

    public void deleteById(Long id) {
        assertExists(id);
        userGroupRepository.deleteById(id);
    }

    public void deleteByName(String name) {
        userGroupRepository.deleteByName(name);
    }

    public List<UserGroupDTO> findByRole(Role role) {
        return userGroupRepository.findByRolesContaining(role).stream()
                .map(this::toDto)
                .toList();
    }

    public List<UserGroupDTO> findByUser(User user) {
        return userGroupRepository.findByUsersContaining(user).stream()
                .map(this::toDto)
                .toList();
    }

    /*** Mapper Methods ***/

    private UserGroupDTO toDto(UserGroup entity) {
        UserGroupDTO dto = new UserGroupDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());

        dto.setRoles(entity.getRoles().stream()
                .map(role -> {
                    RoleDTO r = new RoleDTO();
                    r.setId(role.getId());
                    r.setName(role.getName());
                    r.setCreatedDate(role.getCreatedDate());
                    r.setLastModifiedDate(role.getLastModifiedDate());
                    return r;
                })
                .collect(Collectors.toSet()));

        dto.setUsers(entity.getUsers().stream()
                .map(user -> {
                    UserDTO u = new UserDTO();
                    u.setId(user.getId());
                    u.setFirstname(user.getFirstname());
                    u.setLastname(user.getLastname());
                    u.setEmail(user.getEmail());
                    u.setEnabled(user.isEnabled());
                    u.setAccountLocked(user.isAccountLocked());
                    return u;
                })
                .collect(Collectors.toSet()));

        return dto;
    }

    private UserGroup toEntity(CreateOrUpdateUserGroupDto dto) {
        UserGroup group = new UserGroup();
        group.setName(dto.getName());
        group.setRoles(resolveRoles(dto.getRoleIds()));
        group.setUsers(resolveUsers(dto.getUserIds()));
        return group;
    }

    private Set<Role> resolveRoles(Set<Long> roleIds) {
        return roleIds == null ? Set.of() : roleIds.stream()
                .map(id -> roleRepository.findById(id)
                        .orElseThrow(() -> new RuntimeException("Role not found with id: " + id)))
                .collect(Collectors.toSet());
    }

    private Set<User> resolveUsers(Set<Integer> userIds) {
        return userIds == null ? Set.of() : userIds.stream()
                .map(id -> userRepository.findById(id)
                        .orElseThrow(() -> new RuntimeException("User not found with id: " + id)))
                .collect(Collectors.toSet());
    }
}


