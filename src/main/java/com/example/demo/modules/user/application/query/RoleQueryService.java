package com.example.demo.modules.user.application.query;

import com.example.demo.common.annotation.ReadOnly;
import com.example.demo.common.base.dto.PageResponse;
import com.example.demo.common.base.service.query.AbstractQueryService;
import com.example.demo.common.constant.CacheConstants;
import com.example.demo.common.exception.EntityNotFoundException;
import com.example.demo.common.normalization.NormalizationService;
import com.example.demo.modules.user.application.dto.response.RoleResponse;
import com.example.demo.modules.user.application.mapper.RoleMapper;
import com.example.demo.modules.user.domain.model.Role;
import com.example.demo.modules.user.domain.repository.RoleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Query service for Role aggregate
 *
 * @author Architecture Team
 * @version 1.0
 */
@Slf4j
@Service
@Transactional(readOnly = true)
public class RoleQueryService extends AbstractQueryService<Role, UUID> {

    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;

    public RoleQueryService(RoleRepository roleRepository, NormalizationService normalizationService, RoleMapper roleMapper) {
        super(roleRepository, normalizationService);
        this.roleRepository = roleRepository;
        this.roleMapper = roleMapper;
    }

    @Override
    protected String getEntityCacheName() {
        return CacheConstants.CACHE_USER_ROLES;
    }

    @Override
    protected Class<Role> getEntityClass() {
        return Role.class;
    }

    /**
     * Find role by ID
     */
    @ReadOnly
    @Cacheable(cacheNames = CacheConstants.CACHE_USER_ROLES, key = "#id", unless = "#result == null")
    public RoleResponse findRoleById(UUID id) {
        log.debug("Finding role by ID: {}", id);

        Role role = findById(id);
        return roleMapper.toResponse(role);
    }

    /**
     * Find role by name
     */
    @ReadOnly
    @Cacheable(cacheNames = CacheConstants.CACHE_USER_ROLES, key = "'name:' + #name", unless = "#result == null")
    public RoleResponse findByName(String name) {
        log.debug("Finding role by name: {}", name);

        Role role = roleRepository.findByName(name)
                .orElseThrow(() -> new EntityNotFoundException("Role not found with name: " + name));

        return roleMapper.toResponse(role);
    }

    /**
     * Get all roles
     */
    @ReadOnly
    public PageResponse<RoleResponse> getAllRoles(Pageable pageable) {
        log.debug("Getting all roles with pagination");

        Page<Role> roles = findAll(pageable);
        return PageResponse.from(roles, roleMapper::toResponse);
    }

    /**
     * Get all system roles
     */
    @ReadOnly
    @Cacheable(cacheNames = CacheConstants.CACHE_USER_ROLES, key = "'system'")
    public List<RoleResponse> getSystemRoles() {
        log.debug("Getting all system roles");

        List<Role> roles = roleRepository.findAllSystemRoles();
        return roles.stream()
                .map(roleMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get all non-system roles
     */
    @ReadOnly
    public List<RoleResponse> getNonSystemRoles() {
        log.debug("Getting all non-system roles");

        List<Role> roles = roleRepository.findAllNonSystemRoles();
        return roles.stream()
                .map(roleMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get roles with specific permission
     */
    @ReadOnly
    public List<RoleResponse> getRolesWithPermission(String permissionName) {
        log.debug("Getting roles with permission: {}", permissionName);

        List<Role> roles = roleRepository.findByPermissionName(permissionName);
        return roles.stream()
                .map(roleMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Count users with role
     */
    @ReadOnly
    public long countUsersByRole(UUID roleId) {
        log.debug("Counting users with role: {}", roleId);
        return roleRepository.countUsersByRoleId(roleId);
    }
}