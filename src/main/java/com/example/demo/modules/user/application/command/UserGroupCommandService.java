package com.example.demo.modules.user.application.command;

import com.example.demo.common.base.service.command.AbstractCommandService;
import com.example.demo.common.constant.CacheConstants;
import com.example.demo.common.event.EventPublisher;
import com.example.demo.common.exception.BusinessException;
import com.example.demo.common.exception.EntityNotFoundException;
import com.example.demo.common.normalization.NormalizationService;
import com.example.demo.modules.user.application.dto.request.CreateUserGroupRequest;
import com.example.demo.modules.user.application.dto.request.UpdateUserGroupRequest;
import com.example.demo.modules.user.application.dto.response.UserGroupResponse;
import com.example.demo.modules.user.application.mapper.UserGroupMapper;
import com.example.demo.modules.user.domain.event.*;
import com.example.demo.modules.user.domain.model.Role;
import com.example.demo.modules.user.domain.model.User;
import com.example.demo.modules.user.domain.model.UserGroup;
import com.example.demo.modules.user.domain.repository.RoleRepository;
import com.example.demo.modules.user.domain.repository.UserGroupRepository;
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
 * Command service for UserGroup aggregate
 *
 * Responsibilities:
 * - User group lifecycle management (create, update, delete)
 * - Group membership management (add/remove users)
 * - Group role assignment
 * - Cache invalidation
 * - Hierarchy management
 *
 * Event Publishing Strategy:
 * - Domain events published automatically by AbstractCommandService
 * - Entity domain methods publish specific events
 * - Service-level events for membership changes (UserAddedToGroupEvent, UserRemovedFromGroupEvent)
 *
 * NOTE: User group changes can affect:
 * - User permissions (if group has role-based access)
 * - Group hierarchy
 * - Access control policies
 */
@Slf4j
@Service
@Transactional
public class UserGroupCommandService extends AbstractCommandService<UserGroup, UUID> {

    private final UserGroupRepository userGroupRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final UserGroupMapper userGroupMapper;
    private final EventPublisher eventPublisher;

    public UserGroupCommandService(
            UserGroupRepository userGroupRepository,
            RoleRepository roleRepository,
            UserRepository userRepository,
            UserGroupMapper userGroupMapper,
            CacheManager cacheManager,
            Validator  validator,
            NormalizationService normalizationService,
            ApplicationEventPublisher applicationEventPublisher,
            EventPublisher eventPublisher) {
        super(userGroupRepository, cacheManager, applicationEventPublisher, validator, normalizationService);
        this.userGroupRepository = userGroupRepository;
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.userGroupMapper = userGroupMapper;
        this.eventPublisher = eventPublisher;
    }

    // ====================================
    // Framework Configuration
    // ====================================

    @Override
    protected List<String> getCacheNames() {
        return Arrays.asList(
                CacheConstants.CACHE_USER,
                CacheConstants.CACHE_USER_ROLES,
                "userGroups",
                "userGroupDetails",
                "userGroupsByCode"
        );
    }

    @Override
    protected Class<UserGroup> getEntityClass() {
        return UserGroup.class;
    }

    // ====================================
    // User Group Lifecycle Management
    // ====================================

    /**
     * Create new user group
     *
     * Flow:
     * 1. Validate name and code uniqueness
     * 2. Build user group entity using factory method
     * 3. Set parent group if hierarchical
     * 4. Assign roles if specified
     * 5. Persist (AbstractCommandService publishes EntityCreatedEvent)
     *
     * Events Published (by domain):
     * - EntityCreatedEvent → via AbstractCommandService
     * - UserGroupCreatedEvent → via UserGroup.create() (if implemented)
     */
    public UserGroupResponse createUserGroup(CreateUserGroupRequest request) {
        log.info("Creating user group: {}", request.getName());

        // Enforce name uniqueness
        if (userGroupRepository.existsByName(request.getName())) {
            throw new BusinessException("User group with name " + request.getName() + " already exists");
        }

        // Enforce code uniqueness
        if (userGroupRepository.existsByCode(request.getCode())) {
            throw new BusinessException("User group with code " + request.getCode() + " already exists");
        }

        // Create user group using factory method (publishes event if implemented)
        UserGroup userGroup = UserGroup.create(
                request.getName(),
                request.getCode(),
                request.getDescription()
        );

        if (request.getPriority() != null) {
            userGroup.setPriority(request.getPriority());
        }

        // Set parent group if hierarchical structure
        if (request.getParentGroupId() != null) {
            UserGroup parentGroup = userGroupRepository.findById(request.getParentGroupId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            UserGroup.class, request.getParentGroupId()));

            // Validate no circular reference
            validateNoCircularReference(userGroup, parentGroup);

            userGroup.setParentGroup(parentGroup);
        }

        // Assign roles if specified (each publishes event via domain method)
        if (request.getRoleIds() != null && !request.getRoleIds().isEmpty()) {
            List<Role> roles = loadAndValidateRoles(request.getRoleIds());
            roles.forEach(userGroup::addRole);
        }

        // Persist (domain events published automatically)
        UserGroup createdGroup = create(userGroup);

        log.info("User group created: {} with code: {}", createdGroup.getId(), createdGroup.getCode());

        return userGroupMapper.toResponse(createdGroup);
    }

    /**
     * Update user group
     *
     * Flow:
     * 1. Load existing group
     * 2. Update using domain methods (publishes events)
     * 3. Persist (AbstractCommandService publishes EntityUpdatedEvent)
     *
     * Events Published (by domain):
     * - EntityUpdatedEvent → via AbstractCommandService
     * - UserGroupUpdatedEvent → via UserGroup.update() (if implemented)
     * - UserGroupActivatedEvent/DeactivatedEvent → via activate()/deactivate()
     */
    public UserGroupResponse updateUserGroup(UUID id, UpdateUserGroupRequest request) {
        log.info("Updating user group: {}", id);

        // Load existing group
        UserGroup userGroup = userGroupRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(UserGroup.class, id));

        // Update name if changed
        if (request.getName() != null && !request.getName().equals(userGroup.getName())) {
            if (userGroupRepository.existsByName(request.getName())) {
                throw new BusinessException("User group with name " + request.getName() + " already exists");
            }
            userGroup.setName(request.getName());
        }

        // Update basic fields using domain method (publishes event if implemented)
        userGroup.update(
                request.getName(),
                request.getDescription(),
                request.getPriority()
        );

        // Update active status using domain methods (publishes events)
        if (request.getActive() != null && request.getActive() != userGroup.isActive()) {
            if (request.getActive()) {
                userGroup.activate();
            } else {
                userGroup.deactivate();
            }
        }

        // Update parent group
        if (request.getParentGroupId() != null) {
            UUID currentParentId = userGroup.getParentGroup() != null ?
                    userGroup.getParentGroup().getId() : null;

            if (!Objects.equals(currentParentId, request.getParentGroupId())) {
                UserGroup newParentGroup = userGroupRepository.findById(request.getParentGroupId())
                        .orElseThrow(() -> new EntityNotFoundException(
                                UserGroup.class, request.getParentGroupId()));

                validateNoCircularReference(userGroup, newParentGroup);
                userGroup.setParentGroup(newParentGroup);
            }
        }

        // Update roles if specified (domain methods publish events)
        if (request.getRoleIds() != null) {
            // Remove old roles (publishes events)
            new HashSet<>(userGroup.getRoles()).forEach(userGroup::removeRole);

            // Add new roles (publishes events)
            List<Role> newRoles = loadAndValidateRoles(request.getRoleIds());
            newRoles.forEach(userGroup::addRole);
        }

        // Calculate member count for logging
        Long memberCount = userRepository.countByUserGroupId(id);

        // Persist (domain events published automatically)
        UserGroup updatedGroup = update(id, userGroup);

        log.info("User group updated: {} - Members: {}", id, memberCount);

        return userGroupMapper.toResponse(updatedGroup);
    }

    /**
     * Delete user group
     *
     * Flow:
     * 1. Load group
     * 2. Validate deletion (no active members, no child groups)
     * 3. Execute deletion (AbstractCommandService publishes EntityDeletedEvent)
     *
     * Events Published (by entity):
     * - EntityDeletedEvent → via AbstractCommandService (soft delete)
     * - UserRemovedFromGroupEvent → service-level (for each member before delete)
     */
    public void deleteUserGroup(UUID id) {
        log.info("Deleting user group: {}", id);

        // Load group
        UserGroup userGroup = userGroupRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(UserGroup.class, id));

        // Validate deletion using domain method (throws exception if invalid)
        userGroup.validateDeletion();

        // Check for child groups
        long childGroupCount = userGroupRepository.countByParentGroupId(id);
        if (childGroupCount > 0) {
            throw new BusinessException(
                    "Cannot delete user group with child groups. Remove or reassign child groups first.");
        }

        // Get members before deletion for service-level events
        List<User> members = userRepository.findByUserGroupId(id);

        // Publish service-level user removed events (membership management concern)
        String currentUser = getCurrentUsername();
        for (User member : members) {
            publishUserRemovedFromGroupEvent(
                    member,
                    userGroup,
                    currentUser,
                    "Group deleted",
                    true // automatic
            );
        }

        // Execute deletion (publishes EntityDeletedEvent automatically)
        delete(id);

        log.warn("User group deleted: {} - Members affected: {}", id, members.size());
    }

    // ====================================
    // Group Status Management
    // ====================================

    /**
     * Activate user group
     *
     * Flow:
     * 1. Load group
     * 2. Activate using domain method (publishes event)
     * 3. Persist
     *
     * Events Published (by domain):
     * - UserGroupActivatedEvent → via activate() (if implemented)
     */
    public UserGroupResponse activateUserGroup(UUID id) {
        log.info("Activating user group: {}", id);

        UserGroup userGroup = userGroupRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(UserGroup.class, id));

        if (userGroup.isActive()) {
            throw new BusinessException("User group is already active");
        }

        // Activate using domain method (publishes event)
        userGroup.activate();

        UserGroup updatedGroup = userGroupRepository.save(userGroup);
        evictAllCaches();
        publishDomainEvents(updatedGroup);

        log.info("User group activated: {}", id);

        return userGroupMapper.toResponse(updatedGroup);
    }

    /**
     * Deactivate user group
     *
     * Flow:
     * 1. Load group
     * 2. Deactivate using domain method (publishes event)
     * 3. Persist
     *
     * Events Published (by domain):
     * - UserGroupDeactivatedEvent → via deactivate() (if implemented)
     */
    public UserGroupResponse deactivateUserGroup(UUID id) {
        log.info("Deactivating user group: {}", id);

        UserGroup userGroup = userGroupRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(UserGroup.class, id));

        if (!userGroup.isActive()) {
            throw new BusinessException("User group is already inactive");
        }

        // Deactivate using domain method (publishes event)
        userGroup.deactivate();

        UserGroup updatedGroup = userGroupRepository.save(userGroup);
        evictAllCaches();
        publishDomainEvents(updatedGroup);

        log.info("User group deactivated: {}", id);

        return userGroupMapper.toResponse(updatedGroup);
    }

    // ====================================
    // User Membership Management
    // ====================================

    /**
     * Add user to group
     *
     * Flow:
     * 1. Load user and group
     * 2. Validate group is active
     * 3. Assign user to group using domain method
     * 4. Publish UserAddedToGroupEvent (service-level)
     *
     * Events Published:
     * - UserAddedToGroupEvent → service-level (membership management)
     */
    @Transactional
    public void addUserToGroup(UUID userId, UUID groupId) {
        log.info("Adding user {} to group: {}", userId, groupId);

        // Load user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(User.class, userId));

        // Load group
        UserGroup userGroup = userGroupRepository.findById(groupId)
                .orElseThrow(() -> new EntityNotFoundException(UserGroup.class, groupId));

        // Validate group is active
        if (!userGroup.isActive()) {
            throw new BusinessException("Cannot add user to inactive group");
        }

        // Check if user already in group
        if (user.getUserGroup() != null && user.getUserGroup().getId().equals(groupId)) {
            throw new BusinessException("User is already in this group");
        }

        // Assign user to group using domain method (could publish event if implemented)
        userGroup.addUser(user);

        // Persist
        userRepository.save(user);
        userGroupRepository.save(userGroup);
        evictAllCaches();
        publishDomainEvents(userGroup);

        // Calculate new member count
        Long memberCount = userRepository.countByUserGroupId(groupId);

        // Publish service-level membership event
        publishUserAddedToGroupEvent(user, userGroup, getCurrentUsername(), false, memberCount);

        log.info("User {} added to group: {}", userId, groupId);
    }

    /**
     * Remove user from group
     *
     * Flow:
     * 1. Load user
     * 2. Validate user is in a group
     * 3. Remove from group using domain method
     * 4. Publish UserRemovedFromGroupEvent (service-level)
     *
     * Events Published:
     * - UserRemovedFromGroupEvent → service-level (membership management)
     */
    @Transactional
    public void removeUserFromGroup(UUID userId, String reason) {
        log.info("Removing user {} from group - Reason: {}", userId, reason);

        // Load user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(User.class, userId));

        // Check if user is in a group
        if (user.getUserGroup() == null) {
            throw new BusinessException("User is not in any group");
        }

        // Capture group info before removal
        UserGroup userGroup = user.getUserGroup();
        UUID groupId = userGroup.getId();

        // Remove from group using domain method (could publish event if implemented)
        userGroup.removeUser(user);

        // Persist
        userRepository.save(user);
        userGroupRepository.save(userGroup);
        evictAllCaches();
        publishDomainEvents(userGroup);

        // Calculate new member count
        Long memberCount = userRepository.countByUserGroupId(groupId);

        // Publish service-level membership event
        publishUserRemovedFromGroupEvent(
                user,
                userGroup,
                getCurrentUsername(),
                reason,
                false // not automatic
        );

        log.info("User {} removed from group: {}", userId, groupId);
    }

    /**
     * Move user to different group
     *
     * Flow:
     * 1. Remove from current group
     * 2. Add to new group
     *
     * Events Published:
     * - UserRemovedFromGroupEvent → from old group
     * - UserAddedToGroupEvent → to new group
     */
    @Transactional
    public void moveUserToGroup(UUID userId, UUID newGroupId) {
        log.info("Moving user {} to group: {}", userId, newGroupId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(User.class, userId));

        // Remove from current group if in one
        if (user.getUserGroup() != null) {
            removeUserFromGroup(userId, "Moved to another group");
        }

        // Add to new group
        addUserToGroup(userId, newGroupId);

        log.info("User {} moved to group: {}", userId, newGroupId);
    }

    /**
     * Add multiple users to group (bulk operation)
     *
     * Events Published:
     * - UserAddedToGroupEvent → for each user (service-level)
     */
    @Transactional
    public void addUsersToGroup(Set<UUID> userIds, UUID groupId) {
        log.info("Adding {} users to group: {}", userIds.size(), groupId);

        // Load group once
        UserGroup userGroup = userGroupRepository.findById(groupId)
                .orElseThrow(() -> new EntityNotFoundException(UserGroup.class, groupId));

        if (!userGroup.isActive()) {
            throw new BusinessException("Cannot add users to inactive group");
        }

        String currentUser = getCurrentUsername();
        int successCount = 0;

        for (UUID userId : userIds) {
            try {
                User user = userRepository.findById(userId)
                        .orElseThrow(() -> new EntityNotFoundException(User.class, userId));

                // Skip if already in group
                if (user.getUserGroup() != null && user.getUserGroup().getId().equals(groupId)) {
                    log.debug("User {} already in group {}, skipping", userId, groupId);
                    continue;
                }

                // Add user using domain method
                userGroup.addUser(user);
                userRepository.save(user);

                // Calculate member count
                Long memberCount = userRepository.countByUserGroupId(groupId);

                // Publish service-level event
                publishUserAddedToGroupEvent(user, userGroup, currentUser, false, memberCount);

                successCount++;
            } catch (Exception e) {
                log.warn("Failed to add user {} to group: {} - {}", userId, groupId, e.getMessage());
            }
        }

        // Persist group once after all additions
        userGroupRepository.save(userGroup);
        evictAllCaches();
        publishDomainEvents(userGroup);

        log.info("Bulk add completed: {} of {} users added to group: {}",
                successCount, userIds.size(), groupId);
    }

    // ====================================
    // Role Management
    // ====================================

    /**
     * Assign roles to user group (replace existing)
     *
     * Flow:
     * 1. Load group
     * 2. Clear existing roles (domain methods publish events)
     * 3. Assign new roles (domain methods publish events)
     * 4. Persist
     *
     * Events Published (by domain):
     * - RoleRemovedFromGroupEvent → via removeRole() (if implemented)
     * - RoleAssignedToGroupEvent → via addRole() (if implemented)
     */
    public UserGroupResponse assignRoles(UUID groupId, Set<UUID> roleIds) {
        log.info("Assigning {} roles to user group: {}", roleIds.size(), groupId);

        UserGroup userGroup = userGroupRepository.findById(groupId)
                .orElseThrow(() -> new EntityNotFoundException(UserGroup.class, groupId));

        // Clear existing roles (domain publishes events)
        new HashSet<>(userGroup.getRoles()).forEach(userGroup::removeRole);

        // Assign new roles (domain publishes events)
        List<Role> newRoles = loadAndValidateRoles(roleIds);
        newRoles.forEach(userGroup::addRole);

        // Calculate member count for logging
        Long memberCount = userRepository.countByUserGroupId(groupId);

        // Persist (domain events published automatically)
        UserGroup updatedGroup = userGroupRepository.save(userGroup);
        evictAllCaches();
        publishDomainEvents(updatedGroup);

        log.info("Roles assigned to group: {} - Members affected: {}", groupId, memberCount);

        return userGroupMapper.toResponse(updatedGroup);
    }

    /**
     * Remove role from user group
     *
     * Events Published (by domain):
     * - RoleRemovedFromGroupEvent → via removeRole() (if implemented)
     */
    public UserGroupResponse removeRole(UUID groupId, UUID roleId) {
        log.info("Removing role {} from user group: {}", roleId, groupId);

        UserGroup userGroup = userGroupRepository.findById(groupId)
                .orElseThrow(() -> new EntityNotFoundException(UserGroup.class, groupId));

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new EntityNotFoundException(Role.class, roleId));

        if (!userGroup.getRoles().contains(role)) {
            throw new BusinessException("Group does not have role: " + role.getName());
        }

        // Remove role using domain method (publishes event)
        userGroup.removeRole(role);

        // Persist (domain events published automatically)
        UserGroup updatedGroup = userGroupRepository.save(userGroup);
        evictAllCaches();
        publishDomainEvents(updatedGroup);

        log.info("Role {} removed from group: {}", roleId, groupId);

        return userGroupMapper.toResponse(updatedGroup);
    }

    // ====================================
    // Service-Level Event Publishing
    // (Membership Management Events)
    // ====================================

    /**
     * Publish UserAddedToGroupEvent (service-level)
     * This is a cross-aggregate concern (User + UserGroup)
     */
    private void publishUserAddedToGroupEvent(
            User user,
            UserGroup userGroup,
            String addedBy,
            boolean automatic,
            Long groupMemberCount) {

        UserAddedToGroupEvent event = UserAddedToGroupEvent.builder()
                .userId(user.getId())
                .userEmail(user.getEmail().getValue())
                .userGroupId(userGroup.getId())
                .groupName(userGroup.getName())
                .addedBy(addedBy)
                .automatic(automatic)
                .groupMemberCount(groupMemberCount)
                .source("demo-app")
                .build();

        eventPublisher.publish(event);
        log.debug("Published UserAddedToGroupEvent: {} → {}", user.getEmail().getValue(), userGroup.getName());
    }

    /**
     * Publish UserRemovedFromGroupEvent (service-level)
     * This is a cross-aggregate concern (User + UserGroup)
     */
    private void publishUserRemovedFromGroupEvent(
            User user,
            UserGroup userGroup,
            String removedBy,
            String reason,
            boolean automatic) {

        Long groupMemberCount = userRepository.countByUserGroupId(userGroup.getId());

        UserRemovedFromGroupEvent event = UserRemovedFromGroupEvent.builder()
                .userId(user.getId())
                .userEmail(user.getEmail().getValue())
                .userGroupId(userGroup.getId())
                .groupName(userGroup.getName())
                .removedBy(removedBy)
                .reason(reason)
                .automatic(automatic)
                .groupMemberCount(groupMemberCount)
                .source("demo-app")
                .build();

        eventPublisher.publish(event);
        log.debug("Published UserRemovedFromGroupEvent: {} ← {} (reason: {})",
                user.getEmail().getValue(), userGroup.getName(), reason);
    }

    // ====================================
    // Lifecycle Hooks
    // ====================================

    @Override
    protected void beforeCreate(UserGroup userGroup) {
        log.debug("Before create user group: {}", userGroup.getName());
    }

    @Override
    protected void afterCreate(UserGroup userGroup) {
        log.debug("After create user group: {}", userGroup.getId());
    }

    @Override
    protected void beforeUpdate(UserGroup existing, UserGroup updated) {
        log.debug("Before update user group: {}", existing.getId());
    }

    @Override
    protected void afterUpdate(UserGroup userGroup) {
        log.debug("After update user group: {}", userGroup.getId());
    }

    @Override
    protected void beforeDelete(UserGroup userGroup) {
        log.debug("Before delete user group: {}", userGroup.getId());
    }

    @Override
    protected void afterDelete(UserGroup userGroup) {
        log.debug("After delete user group: {}", userGroup.getId());
    }

    protected void validate(UserGroup userGroup) {
        // Domain validation
        if (userGroup.getName() == null || userGroup.getName().isBlank()) {
            throw new BusinessException("User group name is required");
        }
        if (userGroup.getCode() == null || userGroup.getCode().isBlank()) {
            throw new BusinessException("User group code is required");
        }

        // Validate code format (uppercase, alphanumeric + underscore)
        if (!userGroup.getCode().matches("^[A-Z][A-Z0-9_]*$")) {
            throw new BusinessException("User group code must be uppercase alphanumeric with underscores");
        }
    }

    // ====================================
    // Helper Methods
    // ====================================

    /**
     * Load and validate roles by IDs
     */
    private List<Role> loadAndValidateRoles(Set<UUID> roleIds) {
        List<Role> roles = roleRepository.findAllById(roleIds);

        if (roles.size() != roleIds.size()) {
            Set<UUID> foundIds = roles.stream()
                    .map(Role::getId)
                    .collect(Collectors.toSet());

            Set<UUID> missingIds = roleIds.stream()
                    .filter(id -> !foundIds.contains(id))
                    .collect(Collectors.toSet());

            throw new BusinessException("Roles not found: " + missingIds);
        }

        return roles;
    }

    /**
     * Validate no circular reference in group hierarchy
     */
    private void validateNoCircularReference(UserGroup child, UserGroup parent) {
        UserGroup current = parent;
        Set<UUID> visited = new HashSet<>();

        while (current != null) {
            if (visited.contains(current.getId())) {
                throw new BusinessException("Circular reference detected in group hierarchy");
            }

            if (child.getId() != null && current.getId().equals(child.getId())) {
                throw new BusinessException("Cannot set a group as its own parent");
            }

            visited.add(current.getId());
            current = current.getParentGroup();
        }
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