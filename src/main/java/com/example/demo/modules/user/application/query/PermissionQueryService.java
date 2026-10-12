package com.example.demo.modules.user.application.query;

import com.example.demo.common.annotation.ReadOnly;
import com.example.demo.common.base.dto.PageResponse;
import com.example.demo.common.base.service.query.AbstractQueryService;
import com.example.demo.common.constant.CacheConstants;
import com.example.demo.common.exception.EntityNotFoundException;
import com.example.demo.common.normalization.NormalizationService;
import com.example.demo.modules.user.application.dto.response.PermissionResponse;
import com.example.demo.modules.user.application.mapper.PermissionMapper;
import com.example.demo.modules.user.domain.model.Permission;
import com.example.demo.modules.user.domain.repository.PermissionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Query service for Permission aggregate
 *
 * @author Architecture Team
 * @version 1.0
 */
@Slf4j
@Service
@Transactional(readOnly = true)
public class PermissionQueryService extends AbstractQueryService<Permission, UUID> {

    private final PermissionRepository permissionRepository;
    private final PermissionMapper permissionMapper;

    public PermissionQueryService(PermissionRepository permissionRepository,
        NormalizationService normalizationService,
        PermissionMapper permissionMapper) {
        super(permissionRepository, normalizationService);
        this.permissionRepository = permissionRepository;
        this.permissionMapper = permissionMapper;
    }

    @Override
    protected String getEntityCacheName() {
        return CacheConstants.CACHE_USER_PERMISSIONS;
    }

    @Override
    protected Class<Permission> getEntityClass() {
        return Permission.class;
    }

    /**
     * Find permission by ID
     */
    @ReadOnly
    @Cacheable(cacheNames = CacheConstants.CACHE_USER_PERMISSIONS, key = "#id", unless = "#result == null")
    public PermissionResponse findPermissionById(UUID id) {
        log.debug("Finding permission by ID: {}", id);

        Permission permission = findById(id);
        return permissionMapper.toResponse(permission);
    }

    /**
     * Find permission by name
     */
    @ReadOnly
    @Cacheable(cacheNames = CacheConstants.CACHE_USER_PERMISSIONS, key = "'name:' + #name", unless = "#result == null")
    public PermissionResponse findByName(String name) {
        log.debug("Finding permission by name: {}", name);

        Permission permission = permissionRepository.findByName(name)
                .orElseThrow(() -> new EntityNotFoundException(getEntityClass(),name));

        return permissionMapper.toResponse(permission);
    }

    /**
     * Get all permissions
     */
    @ReadOnly
    public PageResponse<PermissionResponse> getAllPermissions(Pageable pageable) {
        log.debug("Getting all permissions with pagination");

        Page<Permission> permissions = findAll(pageable);
        return PageResponse.from(permissions, permissionMapper::toResponse);
    }

    /**
     * Get all system permissions
     */
    @ReadOnly
    @Cacheable(cacheNames = CacheConstants.CACHE_USER_PERMISSIONS, key = "'system'")
    public List<PermissionResponse> getSystemPermissions() {
        log.debug("Getting all system permissions");

        List<Permission> permissions = permissionRepository.findAllSystemPermissions();
        return permissions.stream()
                .map(permissionMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get permissions by resource
     */
    @ReadOnly
    @Cacheable(cacheNames = CacheConstants.CACHE_USER_PERMISSIONS, key = "'resource:' + #resource")
    public List<PermissionResponse> getPermissionsByResource(String resource) {
        log.debug("Getting permissions by resource: {}", resource);

        List<Permission> permissions = permissionRepository.findByResource(resource);
        return permissions.stream()
                .map(permissionMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get permissions by category
     */
    @ReadOnly
    @Cacheable(cacheNames = CacheConstants.CACHE_USER_PERMISSIONS, key = "'category:' + #category")
    public List<PermissionResponse> getPermissionsByCategory(String category) {
        log.debug("Getting permissions by category: {}", category);

        List<Permission> permissions = permissionRepository.findByCategory(category);
        return permissions.stream()
                .map(permissionMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get all resources
     */
    @ReadOnly
    @Cacheable(cacheNames = CacheConstants.CACHE_USER_PERMISSIONS, key = "'resources'")
    public List<String> getAllResources() {
        log.debug("Getting all resources");
        return permissionRepository.findAllResources();
    }

    /**
     * Get all actions
     */
    @ReadOnly
    @Cacheable(cacheNames = CacheConstants.CACHE_USER_PERMISSIONS, key = "'actions'")
    public List<String> getAllActions() {
        log.debug("Getting all actions");
        return permissionRepository.findAllActions();
    }

    /**
     * Get all categories
     */
    @ReadOnly
    @Cacheable(cacheNames = CacheConstants.CACHE_USER_PERMISSIONS, key = "'categories'")
    public List<String> getAllCategories() {
        log.debug("Getting all categories");
        return permissionRepository.findAllCategories();
    }

    /**
     * Group permissions by category
     */
    @ReadOnly
    @Cacheable(cacheNames = CacheConstants.CACHE_USER_PERMISSIONS, key = "'grouped'")
    public Map<String, List<PermissionResponse>> getPermissionsGroupedByCategory() {
        log.debug("Getting permissions grouped by category");

        List<Permission> allPermissions = findAll();

        return allPermissions.stream()
                .collect(Collectors.groupingBy(
                        p -> p.getCategory() != null ? p.getCategory() : "UNCATEGORIZED",
                        Collectors.mapping(permissionMapper::toResponse, Collectors.toList())
                ));
    }

    /**
     * Group permissions by resource
     */
    @ReadOnly
    public Map<String, List<PermissionResponse>> getPermissionsGroupedByResource() {
        log.debug("Getting permissions grouped by resource");

        List<Permission> allPermissions = findAll();

        return allPermissions.stream()
                .collect(Collectors.groupingBy(
                        Permission::getResource,
                        Collectors.mapping(permissionMapper::toResponse, Collectors.toList())
                ));
    }
}