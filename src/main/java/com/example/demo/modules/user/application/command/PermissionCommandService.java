package com.example.demo.modules.user.application.command;

import com.example.demo.common.base.service.command.AbstractCommandService;
import com.example.demo.common.constant.CacheConstants;
import com.example.demo.common.event.EventPublisher;
import com.example.demo.common.exception.BusinessException;
import com.example.demo.common.exception.EntityNotFoundException;
import com.example.demo.common.normalization.NormalizationService;
import com.example.demo.modules.user.application.dto.request.CreatePermissionRequest;
import com.example.demo.modules.user.application.dto.request.UpdatePermissionRequest;
import com.example.demo.modules.user.application.dto.response.PermissionResponse;
import com.example.demo.modules.user.application.mapper.PermissionMapper;
import com.example.demo.modules.user.domain.model.Permission;
import com.example.demo.modules.user.domain.model.Role;
import com.example.demo.modules.user.domain.repository.PermissionRepository;
import com.example.demo.modules.user.domain.repository.RoleRepository;
import com.example.demo.modules.user.domain.repository.UserRepository;

import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;

import org.hibernate.validator.internal.metadata.facets.Validatable;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;


/**
 * Command service for Permission aggregate
 *
 * Responsibilities:
 * - Permission lifecycle management (create, update, delete)
 * - Cache invalidation
 * - Security validation
 * - Impact analysis (affected roles and users)
 *
 * Event Publishing Strategy:
 * - Domain events published automatically by AbstractCommandService
 * - Entity domain methods publish specific events (if implemented)
 *
 * NOTE: Permission changes trigger cascade effects:
 * - Role permission cache invalidation
 * - User permission recalculation
 * - Security policy updates
 */
@Slf4j
@Service
@Transactional
public class PermissionCommandService extends AbstractCommandService<Permission, UUID> {

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PermissionMapper permissionMapper;
    private final EventPublisher eventPublisher;

    public PermissionCommandService(
            PermissionRepository permissionRepository,
            RoleRepository roleRepository,
            UserRepository userRepository,
            PermissionMapper permissionMapper,
            CacheManager cacheManager,
            ApplicationEventPublisher applicationEventPublisher,
            Validator validator,
            NormalizationService normalizationService,
            EventPublisher eventPublisher) {
        super(permissionRepository, cacheManager, applicationEventPublisher,validator, normalizationService);
        this.permissionRepository = permissionRepository;
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.permissionMapper = permissionMapper;
        this.eventPublisher = eventPublisher;
    }

    // ====================================
    // Framework Configuration
    // ====================================

    @Override
    protected List<String> getCacheNames() {
        return Arrays.asList(
                CacheConstants.CACHE_USER_PERMISSIONS,
                CacheConstants.CACHE_USER_ROLES,
                "permissions",
                "permissionDetails",
                "permissionsByResource"
        );
    }

    @Override
    protected Class<Permission> getEntityClass() {
        return Permission.class;
    }

    // ====================================
    // Permission Lifecycle Management
    // ====================================

    /**
     * Create new permission
     *
     * Flow:
     * 1. Validate resource:action uniqueness
     * 2. Build permission entity using factory method
     * 3. Persist (AbstractCommandService publishes EntityCreatedEvent)
     *
     * Events Published (by domain):
     * - EntityCreatedEvent → via AbstractCommandService
     * - PermissionCreatedEvent → via Permission.create() (if implemented)
     *
     * Example:
     * Resource: USER, Action: READ → Permission: USER:READ
     */
    public PermissionResponse createPermission(CreatePermissionRequest request) {
        log.info("Creating permission: {}:{}", request.getResource(), request.getAction());

        // Enforce uniqueness on resource:action combination
        if (permissionRepository.findByResourceAndAction(request.getResource(), request.getAction()).isPresent()) {
            throw new BusinessException(String.format("Permission already exists: %s:%s",
                    request.getResource(), request.getAction()));
        }

        // Create permission using factory method (publishes event if implemented)
        Permission permission = Permission.create(
                request.getResource(),
                request.getAction(),
                request.getDescription()
        );

        // Set optional fields
        if (request.getCategory() != null) {
            permission.setCategory(request.getCategory());
        }

        if (request.getDisplayOrder() != null) {
            permission.setDisplayOrder(request.getDisplayOrder());
        }

        // Persist (domain events published automatically)
        Permission createdPermission = create(permission);

        log.info("Permission created: {} ({}:{})",
                createdPermission.getId(),
                createdPermission.getResource(),
                createdPermission.getAction());

        return permissionMapper.toResponse(createdPermission);
    }

    /**
     * Update existing permission
     *
     * Flow:
     * 1. Load existing permission
     * 2. Validate not system permission
     * 3. Update using domain method (publishes events)
     * 4. Persist (AbstractCommandService publishes EntityUpdatedEvent)
     *
     * Events Published (by domain):
     * - EntityUpdatedEvent → via AbstractCommandService
     * - PermissionUpdatedEvent → via Permission.update() (if implemented)
     *
     * NOTE: Cannot update resource or action (immutable for integrity)
     */
    public PermissionResponse updatePermission(UUID id, UpdatePermissionRequest request) {
        log.info("Updating permission: {}", id);

        // Load existing permission
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(getEntityClass(), id));

        // Prevent updating system permissions
        if (permission.getSystemPermission()) {
            throw new BusinessException("Cannot update system permission");
        }

        // Update using domain method (publishes event if implemented)
        permission.update(
                request.getDescription(),
                request.getCategory(),
                request.getDisplayOrder()
        );

        // Calculate impact for logging
        ImpactAnalysis impact = analyzePermissionImpact(id);

        // Persist (domain events published automatically)
        Permission updatedPermission = update(id, permission);

        log.info("Permission updated: {} - Affected roles: {}, users: {}",
                id, impact.affectedRoleCount, impact.affectedUserCount);

        return permissionMapper.toResponse(updatedPermission);
    }

    /**
     * Delete permission
     *
     * Flow:
     * 1. Load permission
     * 2. Validate deletion (not system permission, can be removed)
     * 3. Execute deletion (AbstractCommandService handles)
     *
     * Events Published (by entity):
     * - No soft delete event (Permission uses AuditableAggregateRoot, not SoftDeletableAggregateRoot)
     * - Hard delete performed
     *
     * IMPORTANT: This will remove permission from all roles that have it (via cascade)
     */
    public void deletePermission(UUID id) {
        log.info("Deleting permission: {}", id);

        // Load permission
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(getEntityClass(), id));

        // Validate deletion using domain method (throws exception if invalid)
        permission.validateDeletion();

        // Analyze impact for logging
        ImpactAnalysis impact = analyzePermissionImpact(id);

        // Execute hard delete using parent method
        delete(id);

        log.warn("Permission deleted: {} ({}:{}) - Affected roles: {}, users: {}",
                id,
                permission.getResource(),
                permission.getAction(),
                impact.affectedRoleCount,
                impact.affectedUserCount);
    }

    // ====================================
    // Impact Analysis
    // ====================================

    /**
     * Analyze impact of permission change or deletion
     *
     * Calculates:
     * - Roles that have this permission
     * - Users affected through those roles
     *
     * Used for logging and validation
     */
    private ImpactAnalysis analyzePermissionImpact(UUID permissionId) {
        // Find all roles that have this permission
        List<Role> affectedRoles = roleRepository.findByPermissionId(permissionId);

        // Count affected users (users with any of these roles)
        Set<UUID> roleIds = affectedRoles.stream()
                .map(Role::getId)
                .collect(Collectors.toSet());

        long affectedUserCount = 0;
        if (!roleIds.isEmpty()) {
            affectedUserCount = userRepository.countByRoleIds(roleIds);
        }

        return new ImpactAnalysis(
                affectedRoles,
                (long) affectedRoles.size(),
                affectedUserCount
        );
    }

    /**
     * Impact analysis result holder
     */
    private static class ImpactAnalysis {
        final List<Role> affectedRoles;
        final Long affectedRoleCount;
        final Long affectedUserCount;

        ImpactAnalysis(List<Role> affectedRoles, Long affectedRoleCount, Long affectedUserCount) {
            this.affectedRoles = affectedRoles;
            this.affectedRoleCount = affectedRoleCount;
            this.affectedUserCount = affectedUserCount;
        }
    }

    // ====================================
    // Lifecycle Hooks
    // ====================================

    @Override
    protected void beforeCreate(Permission permission) {
        log.debug("Before create permission: {}:{}", permission.getResource(), permission.getAction());
    }

    @Override
    protected void afterCreate(Permission permission) {
        log.debug("After create permission: {}", permission.getId());
    }

    @Override
    protected void beforeUpdate(Permission existing, Permission updated) {
        log.debug("Before update permission: {}", existing.getId());
    }

    @Override
    protected void afterUpdate(Permission permission) {
        log.debug("After update permission: {}", permission.getId());
    }

    @Override
    protected void beforeDelete(Permission permission) {
        log.debug("Before delete permission: {}", permission.getId());

        // Additional validation before deletion
        if (permission.getSystemPermission()) {
            throw new BusinessException("Cannot delete system permission");
        }
    }

    @Override
    protected void afterDelete(Permission permission) {
        log.debug("After delete permission: {}", permission.getId());
    }

    // ====================================
    // Helper Methods
    // ====================================

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

    // ====================================
    // Bulk Operations
    // ====================================

    /**
     * Create multiple permissions in batch
     *
     * Useful for:
     * - Initial system setup
     * - Module registration
     * - Permission seeding
     *
     * Events Published (by domain):
     * - EntityCreatedEvent → for each created permission (via AbstractCommandService)
     */
    @Transactional
    public List<PermissionResponse> createPermissionsBatch(List<CreatePermissionRequest> requests) {
        log.info("Creating {} permissions in batch", requests.size());

        List<PermissionResponse> createdPermissions = new ArrayList<>();

        for (CreatePermissionRequest request : requests) {
            try {
                PermissionResponse created = createPermission(request);
                createdPermissions.add(created);
            } catch (BusinessException e) {
                log.warn("Failed to create permission {}:{} - {}",
                        request.getResource(), request.getAction(), e.getMessage());
                // Continue with next permission
            }
        }

        log.info("Batch creation completed: {} of {} permissions created",
                createdPermissions.size(), requests.size());

        return createdPermissions;
    }

    /**
     * Synchronize permissions for a resource
     *
     * Use case:
     * - Module updates that add/remove permissions
     * - Ensures resource has exactly the specified permissions
     *
     * Flow:
     * 1. Load existing permissions for resource
     * 2. Determine which to add/remove
     * 3. Execute changes (domain events published automatically)
     */
    @Transactional
    public void synchronizePermissionsForResource(String resource, Set<String> actions) {
        log.info("Synchronizing permissions for resource: {} with {} actions", resource, actions.size());

        // Load existing permissions for this resource
        List<Permission> existingPermissions = permissionRepository.findByResource(resource);
        Set<String> existingActions = existingPermissions.stream()
                .map(Permission::getAction)
                .collect(Collectors.toSet());

        // Determine actions to add
        Set<String> actionsToAdd = actions.stream()
                .filter(action -> !existingActions.contains(action))
                .collect(Collectors.toSet());

        // Determine permissions to remove
        List<Permission> permissionsToRemove = existingPermissions.stream()
                .filter(p -> !actions.contains(p.getAction()))
                .filter(p -> !p.getSystemPermission()) // Don't remove system permissions
                .collect(Collectors.toList());

        // Add new permissions (domain events published automatically)
        for (String action : actionsToAdd) {
            CreatePermissionRequest request = new CreatePermissionRequest();
            request.setResource(resource);
            request.setAction(action);
            request.setDescription(String.format("Permission for %s:%s", resource, action));

            createPermission(request);
        }

        // Remove obsolete permissions (domain events published automatically)
        for (Permission permission : permissionsToRemove) {
            deletePermission(permission.getId());
        }

        log.info("Synchronization completed for resource: {} - Added: {}, Removed: {}",
                resource, actionsToAdd.size(), permissionsToRemove.size());
    }
}