package com.example.demo.modules.user.application.command;

import com.example.demo.common.base.service.command.AbstractCommandService;
import com.example.demo.common.constant.CacheConstants;
import com.example.demo.common.event.EventPublisher;
import com.example.demo.common.exception.BusinessException;
import com.example.demo.common.exception.EntityNotFoundException;
import com.example.demo.common.normalization.NormalizationService;
import com.example.demo.modules.user.application.dto.request.CreateRoleRequest;
import com.example.demo.modules.user.application.dto.request.UpdateRoleRequest;
import com.example.demo.modules.user.application.dto.response.RoleResponse;
import com.example.demo.modules.user.application.mapper.RoleMapper;
import com.example.demo.modules.user.domain.model.Permission;
import com.example.demo.modules.user.domain.model.Role;
import com.example.demo.modules.user.domain.repository.PermissionRepository;
import com.example.demo.modules.user.domain.repository.RoleRepository;
import com.example.demo.modules.user.domain.repository.UserRepository;

import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Command service for Role aggregate
 *
 * Responsibilities:
 * - Role lifecycle management (create, update, delete)
 * - Permission assignment to roles
 * - Cache invalidation
 * - Security validation
 *
 * Event Publishing Strategy:
 * - Domain events published automatically by AbstractCommandService
 * - Entity domain methods publish specific events (RoleCreated, PermissionAssigned, etc.)
 */
@Slf4j
@Service
@Transactional
public class RoleCommandService extends AbstractCommandService<Role, UUID> {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UserRepository userRepository;
    private final RoleMapper roleMapper;
    private final EventPublisher eventPublisher;

    public RoleCommandService(
            RoleRepository roleRepository,
            PermissionRepository permissionRepository,
            UserRepository userRepository,
            RoleMapper roleMapper,
            CacheManager cacheManager,
            Validator validator,
            NormalizationService normalizationService,
            ApplicationEventPublisher applicationEventPublisher,
            EventPublisher eventPublisher) {
        super(roleRepository, cacheManager, applicationEventPublisher, validator, normalizationService);
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.userRepository = userRepository;
        this.roleMapper = roleMapper;
        this.eventPublisher = eventPublisher;
    }

    // ====================================
    // Framework Configuration
    // ====================================

    @Override
    protected List<String> getCacheNames() {
        return Arrays.asList(
                CacheConstants.CACHE_USER_ROLES,
                CacheConstants.CACHE_USER_PERMISSIONS,
                "roles",
                "roleDetails",
                "rolesByName"
        );
    }

    @Override
    protected Class<Role> getEntityClass() {
        return Role.class;
    }

    // ====================================
    // Role Lifecycle Management
    // ====================================

    /**
     * Create new role
     *
     * Flow:
     * 1. Validate role name uniqueness
     * 2. Build role entity using factory method
     * 3. Assign permissions if specified
     * 4. Persist (AbstractCommandService publishes EntityCreatedEvent)
     *
     * Events Published (by domain):
     * - EntityCreatedEvent → via AbstractCommandService
     * - RoleCreatedEvent → via Role.create() factory method (if implemented)
     */
    public RoleResponse createRole(CreateRoleRequest request) {
        log.info("Creating role: {}", request.getName());

        // Enforce name uniqueness
        if (roleRepository.existsByName(request.getName())) {
            throw new BusinessException("Role with name " + request.getName() + " already exists");
        }

        // Build role entity using factory method (publishes RoleCreatedEvent)
        Role role = Role.create(
                request.getName(),
                request.getDisplayName(),
                request.getDescription()
        );

        role.setPriority(request.getPriority() != null ? request.getPriority() : 0);

        // Assign initial permissions (each publishes PermissionAssignedEvent via domain)
        if (request.getPermissionIds() != null && !request.getPermissionIds().isEmpty()) {
            List<Permission> permissions = loadAndValidatePermissions(
                    request.getPermissionIds().stream()
                            .map(UUID::fromString)
                            .collect(Collectors.toSet())
            );
            permissions.forEach(role::addPermission);
        }

        // Persist (domain events published automatically)
        Role createdRole = create(role);

        log.info("Role created: {} with {} permissions",
                createdRole.getId(), createdRole.getPermissions().size());

        return roleMapper.toResponse(createdRole);
    }

    /**
     * Update existing role
     *
     * Flow:
     * 1. Load existing role
     * 2. Validate not system role
     * 3. Update using domain method (publishes events)
     * 4. Persist (AbstractCommandService publishes EntityUpdatedEvent)
     *
     * Events Published (by domain):
     * - EntityUpdatedEvent → via AbstractCommandService
     * - RoleUpdatedEvent → via Role.update() (if implemented)
     * - PermissionAssignedEvent → via Role.addPermission()
     * - PermissionRemovedEvent → via Role.removePermission()
     */
    public RoleResponse updateRole(UUID id, UpdateRoleRequest request) {
        log.info("Updating role: {}", id);

        // Load existing role
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Role.class, id));

        // Prevent updating system roles
        if (role.getSystemRole()) {
            throw new BusinessException("Cannot update system role");
        }

        // Track permission changes for logging
        Set<Permission> oldPermissions = new HashSet<>(role.getPermissions());

        // Update basic fields using domain method (publishes event if implemented)
        role.update(
                request.getDisplayName(),
                request.getDescription(),
                request.getPriority()
        );

        // Update permissions if specified
        if (request.getPermissionIds() != null) {
            List<Permission> newPermissions = loadAndValidatePermissions(request.getPermissionIds());
            Set<Permission> newPermissionSet = new HashSet<>(newPermissions);

            // Remove old permissions (each publishes PermissionRemovedEvent)
            new HashSet<>(role.getPermissions()).stream()
                    .filter(p -> !newPermissionSet.contains(p))
                    .forEach(role::removePermission);

            // Add new permissions (each publishes PermissionAssignedEvent)
            newPermissionSet.stream()
                    .filter(p -> !oldPermissions.contains(p))
                    .forEach(role::addPermission);
        }

        // Persist (domain events published automatically)
        Role updatedRole = update(id, role);

        log.info("Role updated: {}", id);

        return roleMapper.toResponse(updatedRole);
    }

    // ====================================
    // Permission Assignment
    // ====================================

    /**
     * Assign permissions to role (replace existing)
     *
     * Flow:
     * 1. Load role
     * 2. Validate not system role
     * 3. Replace permissions using domain methods
     * 4. Persist
     *
     * Events Published (by domain):
     * - PermissionRemovedEvent → via Role.removePermission()
     * - PermissionAssignedEvent → via Role.addPermission()
     */
    public RoleResponse assignPermissions(UUID roleId, Set<UUID> permissionIds) {
        log.info("Assigning {} permissions to role: {}", permissionIds.size(), roleId);

        // Load role
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new EntityNotFoundException(Role.class, roleId));

        // Prevent modifying system roles
        if (role.getSystemRole()) {
            throw new BusinessException("Cannot modify system role permissions");
        }

        // Load and validate new permissions
        List<Permission> newPermissions = loadAndValidatePermissions(permissionIds);
        Set<Permission> newPermissionSet = new HashSet<>(newPermissions);

        // Remove old permissions (publishes events)
        new HashSet<>(role.getPermissions()).stream()
                .filter(p -> !newPermissionSet.contains(p))
                .forEach(role::removePermission);

        // Add new permissions (publishes events)
        newPermissionSet.stream()
                .filter(p -> !role.getPermissions().contains(p))
                .forEach(role::addPermission);

        // Persist (domain events published automatically)
        Role updatedRole = roleRepository.save(role);
        evictAllCaches();
        publishDomainEvents(updatedRole);

        log.info("Permissions assigned to role: {}", roleId);

        return roleMapper.toResponse(updatedRole);
    }

    /**
     * Remove single permission from role
     *
     * Flow:
     * 1. Load role and permission
     * 2. Validate not system role
     * 3. Remove using domain method (publishes event)
     * 4. Persist
     *
     * Events Published (by domain):
     * - PermissionRemovedEvent → via Role.removePermission()
     */
    public RoleResponse removePermission(UUID roleId, UUID permissionId) {
        log.info("Removing permission {} from role: {}", permissionId, roleId);

        // Load role
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new EntityNotFoundException(Role.class, roleId));

        // Prevent modifying system roles
        if (role.getSystemRole()) {
            throw new BusinessException("Cannot modify system role permissions");
        }

        // Load permission
        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new EntityNotFoundException(Permission.class, permissionId));

        // Ensure role has this permission
        if (!role.getPermissions().contains(permission)) {
            throw new BusinessException("Role does not have permission: " + permission.getName());
        }

        // Remove permission (publishes event)
        role.removePermission(permission);

        // Persist (domain events published automatically)
        Role updatedRole = roleRepository.save(role);
        evictAllCaches();
        publishDomainEvents(updatedRole);

        log.info("Permission {} removed from role: {}", permissionId, roleId);

        return roleMapper.toResponse(updatedRole);
    }

    /**
     * Add single permission to role
     *
     * Flow:
     * 1. Load role and permission
     * 2. Validate not system role
     * 3. Add using domain method (publishes event)
     * 4. Persist
     *
     * Events Published (by domain):
     * - PermissionAssignedEvent → via Role.addPermission()
     */
    public RoleResponse addPermission(UUID roleId, UUID permissionId) {
        log.info("Adding permission {} to role: {}", permissionId, roleId);

        // Load role
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new EntityNotFoundException(Role.class, roleId));

        // Prevent modifying system roles
        if (role.getSystemRole()) {
            throw new BusinessException("Cannot modify system role permissions");
        }

        // Load permission
        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new EntityNotFoundException(Permission.class, permissionId));

        // Check if already assigned
        if (role.getPermissions().contains(permission)) {
            throw new BusinessException("Role already has permission: " + permission.getName());
        }

        // Add permission (publishes event)
        role.addPermission(permission);

        // Persist (domain events published automatically)
        Role updatedRole = roleRepository.save(role);
        evictAllCaches();
        publishDomainEvents(updatedRole);

        log.info("Permission {} added to role: {}", permissionId, roleId);

        return roleMapper.toResponse(updatedRole);
    }

    /**
     * Delete role
     *
     * Flow:
     * 1. Load role
     * 2. Validate deletion (not system role, no users)
     * 3. Execute deletion (AbstractCommandService handles)
     *
     * Events Published (by entity):
     * - EntityDeletedEvent → via AbstractCommandService (soft delete)
     * OR
     * - Hard delete (no event if hard deleted)
     */
    public void deleteRole(UUID id) {
        log.info("Deleting role: {}", id);

        // Load role
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Role.class, id));

        // Validate deletion using domain method (throws exception if invalid)
        role.validateDeletion();

        // Execute deletion using parent method
        // (publishes EntityDeletedEvent if soft delete, or performs hard delete)
        delete(id);

        log.warn("Role deleted: {}", id);
    }

    // ====================================
    // Lifecycle Hooks
    // ====================================

    @Override
    protected void beforeCreate(Role role) {
        log.debug("Before create role: {}", role.getName());
    }

    @Override
    protected void afterCreate(Role role) {
        log.debug("After create role: {}", role.getId());
    }

    @Override
    protected void beforeUpdate(Role existing, Role updated) {
        log.debug("Before update role: {}", existing.getId());
    }

    @Override
    protected void afterUpdate(Role role) {
        log.debug("After update role: {}", role.getId());
    }

    @Override
    protected void beforeDelete(Role role) {
        log.debug("Before delete role: {}", role.getId());

        // Additional validation before deletion
        if (role.getSystemRole()) {
            throw new BusinessException("Cannot delete system role");
        }
    }

    @Override
    protected void afterDelete(Role role) {
        log.debug("After delete role: {}", role.getId());
    }

    protected void validate(Role role) {
        // Domain validation
        if (role.getName() == null || role.getName().isBlank()) {
            throw new BusinessException("Role name is required");
        }

        // Enforce naming convention for roles
        if (!role.getName().startsWith("ROLE_")) {
            throw new BusinessException("Role name must start with 'ROLE_'");
        }
    }

    // ====================================
    // Helper Methods
    // ====================================

    /**
     * Load and validate permissions by IDs
     */
    private List<Permission> loadAndValidatePermissions(Set<UUID> permissionIds) {
        List<Permission> permissions = permissionRepository.findAllById(permissionIds);

        if (permissions.size() != permissionIds.size()) {
            Set<UUID> foundIds = permissions.stream()
                    .map(Permission::getId)
                    .collect(Collectors.toSet());

            Set<UUID> missingIds = permissionIds.stream()
                    .filter(id -> !foundIds.contains(id))
                    .collect(Collectors.toSet());

            throw new BusinessException("Permissions not found: " + missingIds);
        }

        return permissions;
    }

    /**
     * Get current authenticated username
     */
    private String getCurrentUsername() {
        try {
            org.springframework.security.core.Authentication authentication =
                    org.springframework.security.core.context.SecurityContextHolder
                            .getContext()
                            .getAuthentication();

            if (authentication != null && authentication.isAuthenticated()) {
                return authentication.getName();
            }
        } catch (Exception e) {
            log.debug("Could not get current username", e);
        }
        return "system";
    }
}