package com.example.demo.modules.user.application.command;

import com.example.demo.common.base.service.command.AbstractCommandService;
import com.example.demo.common.event.EventPublisher;
import com.example.demo.common.exception.BusinessException;
import com.example.demo.common.exception.DuplicateEntityException;
import com.example.demo.common.exception.EntityNotFoundException;
import com.example.demo.common.normalization.NormalizationService;
import com.example.demo.modules.user.application.dto.request.*;
import com.example.demo.modules.user.application.dto.response.UserDetailResponse;
import com.example.demo.modules.user.application.mapper.UserMapper;
import com.example.demo.modules.user.domain.event.*;
import com.example.demo.modules.user.domain.model.Role;
import com.example.demo.modules.user.domain.model.User;
import com.example.demo.modules.user.domain.model.UserGroup;
import com.example.demo.modules.user.domain.model.vo.Email;
import com.example.demo.modules.user.domain.repository.RoleRepository;
import com.example.demo.modules.user.domain.repository.UserGroupRepository;
import com.example.demo.modules.user.domain.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Command service for User aggregate
 *
 * Responsibilities:
 * - All write operations (Create, Update, Delete)
 * - Transaction management
 * - Cache invalidation
 * - Business rule enforcement
 *
 * Event Publishing Strategy:
 * - Domain events published automatically by AbstractCommandService
 * - Only business-specific events published here (password reset, logout, etc.)
 */
@Slf4j
@Service
@Transactional
public class UserCommandService extends AbstractCommandService<User, UUID> {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserGroupRepository userGroupRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final EventPublisher eventPublisher;

    public UserCommandService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            UserGroupRepository userGroupRepository,
            PasswordEncoder passwordEncoder,
            UserMapper userMapper,
            CacheManager cacheManager,
            Validator validator,
            NormalizationService normalizationService,
            ApplicationEventPublisher applicationEventPublisher,
            EventPublisher eventPublisher) {
        super(userRepository, cacheManager, applicationEventPublisher, validator, normalizationService);
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userGroupRepository = userGroupRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
        this.eventPublisher = eventPublisher;
    }

    // ====================================
    // Framework Configuration
    // ====================================

    @Override
    protected List<String> getCacheNames() {
        return Arrays.asList("users", "userDetails", "usersByEmail");
    }

    @Override
    protected Class<User> getEntityClass() {
        return User.class;
    }

    // ====================================
    // User Creation
    // ====================================

    /**
     * Create new user account
     *
     * Flow:
     * 1. Validate request
     * 2. Check email uniqueness
     * 3. Build user entity with encoded password
     * 4. Assign default or specified roles
     * 5. Persist user (AbstractCommandService publishes EntityCreatedEvent)
     *
     * Events Published (by entity):
     * - EntityCreatedEvent → via AbstractCommandService
     */
    public UserDetailResponse createUser(CreateUserRequest request) {
        log.info("Creating user: {}", request.getEmail());

        // Validate request data
        request.validate();

        // Enforce email uniqueness
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEntityException("User with email " + request.getEmail() + " already exists");
        }

        // Build user entity
        User user = buildUserFromRequest(request);

        // Assign roles (default ROLE_USER if none specified)
        assignRolesToUser(user, request.getRoleIds());

        // Assign to user group if specified
        if (request.getUserGroupId() != null) {
            assignUserGroup(user, UUID.fromString(request.getUserGroupId()));
        }

        // Persist using parent's create method (handles events, transaction, cache)
        User createdUser = create(user);

        return userMapper.toDetailResponse(createdUser);
    }

    // ====================================
    // User Profile Updates
    // ====================================

    /**
     * Update user (admin operation)
     *
     * Flow:
     * 1. Load existing user
     * 2. Validate email uniqueness if changed
     * 3. Update entity
     * 4. Persist (AbstractCommandService publishes EntityUpdatedEvent)
     *
     * Events Published (by entity):
     * - EntityUpdatedEvent → via AbstractCommandService
     */
    public UserDetailResponse updateUser(UUID id, UpdateUserRequest request) {
        log.info("Updating user: {}", id);

        // Validate request
        request.validate();

        // Load existing user
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(User.class, id));

        // Check email change
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail().getValue())) {
            if (userRepository.existsByEmailAndIdNot(request.getEmail(), id)) {
                throw new DuplicateEntityException("Email already in use");
            }
        }

        // Apply updates using mapper
        userMapper.updateEntityFromDto(request, user);

        // Update user group if specified
        if (request.getUserGroupId() != null) {
            UUID newGroupId = UUID.fromString(request.getUserGroupId());
            UUID oldGroupId = user.getUserGroup() != null ? user.getUserGroup().getId() : null;

            if (!Objects.equals(oldGroupId, newGroupId)) {
                assignUserGroup(user, newGroupId);
            }
        }

        // Persist using parent's update method (handles events)
        User updatedUser = update(id, user);

        return userMapper.toDetailResponse(updatedUser);
    }

    /**
     * Update user profile (self-service operation)
     *
     * Flow:
     * 1. Load user
     * 2. Update profile fields using domain method
     * 3. Persist (AbstractCommandService publishes EntityUpdatedEvent)
     *
     * Events Published (by entity):
     * - EntityUpdatedEvent → via AbstractCommandService
     * - UserProfileUpdatedEvent → via domain method (if implemented)
     */
    public UserDetailResponse updateUserProfile(UUID userId, UpdateUserProfileRequest request) {
        log.info("Updating user profile (self-service): {}", userId);

        // Load user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(User.class, userId));

        // Update profile using domain method
        user.updateProfile(
                request.getFirstName() != null ? request.getFirstName() : user.getFirstName(),
                request.getLastName() != null ? request.getLastName() : user.getLastName(),
                userMapper.toUserProfile(request)
        );

        // Persist (domain events published automatically)
        User updatedUser = update(userId, user);

        return userMapper.toDetailResponse(updatedUser);
    }

    // ====================================
    // User Deletion
    // ====================================

    /**
     * Hard delete user (permanent deletion)
     *
     * Flow:
     * 1. Load user
     * 2. Execute hard delete (AbstractCommandService handles)
     *
     * Events Published (by entity):
     * - EntityDeletedEvent → via AbstractCommandService (if soft delete)
     */
    public void hardDeleteUser(UUID id) {
        log.warn("Hard deleting user: {}", id);

        // Execute hard delete using parent method
        delete(id);

        log.info("User hard deleted: {}", id);
    }

    // ====================================
    // Password Management
    // ====================================

    /**
     * Change password (authenticated user)
     *
     * Flow:
     * 1. Verify current password
     * 2. Encode and set new password using domain method
     * 3. Persist (publishes UserPasswordChangedEvent via domain)
     * 4. Publish UserLogoutEvent to invalidate sessions
     *
     * Events Published:
     * - UserPasswordChangedEvent → via domain method
     * - UserLogoutEvent → service-level (session management)
     */
    public void changePassword(UUID userId, ChangePasswordRequest request) {
        log.info("Changing password for user: {}", userId);

        // Validate request
        request.validate();

        // Load user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(User.class, userId));

        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new BusinessException("Current password is incorrect");
        }

        // Change password using domain method (publishes UserPasswordChangedEvent)
        String encodedPassword = passwordEncoder.encode(request.getNewPassword());
        user.changePassword(encodedPassword);

        // Persist (domain events published automatically)
        userRepository.save(user);
        evictAllCaches();
        publishDomainEvents(user);

        // Publish logout event to kill all sessions (service-level concern)
        publishLogoutEvent(user, "password_changed", true);

        log.info("Password changed successfully for user: {}", userId);
    }

    /**
     * Reset password (forgot password flow)
     *
     * Flow:
     * 1. Validate reset token
     * 2. Set new password using domain method
     * 3. Clear reset token
     * 4. Publish PasswordResetCompletedEvent (service-level)
     * 5. Publish UserLogoutEvent to kill sessions
     *
     * Events Published:
     * - UserPasswordChangedEvent → via domain method
     * - PasswordResetCompletedEvent → service-level
     * - UserLogoutEvent → service-level (session management)
     */
    public void resetPassword(String token, String newPassword) {
        log.info("Resetting password with token");

        // Find user by reset token
        User user = userRepository.findByEmailVerificationToken(token)
                .orElseThrow(() -> new BusinessException("Invalid or expired reset token"));

        // Set new password (publishes UserPasswordChangedEvent)
        String encodedPassword = passwordEncoder.encode(newPassword);
        user.changePassword(encodedPassword);

        // Clear reset token
        user.setEmailVerificationToken(null);

        // Persist
        userRepository.save(user);
        evictAllCaches();
        publishDomainEvents(user);

        // Publish password reset completed event (service-level)
        publishPasswordResetCompletedEvent(user, token);

        // Publish logout event to kill all sessions
        publishLogoutEvent(user, "password_reset", true);

        log.info("Password reset completed for user: {}", user.getId());
    }

    /**
     * Publish PasswordResetCompletedEvent (service-level event)
     */
    private void publishPasswordResetCompletedEvent(User user, String resetToken) {
        PasswordResetCompletedEvent event = PasswordResetCompletedEvent.builder()
                .userId(user.getId())
                .email(user.getEmail().getValue())
                .resetToken(resetToken)
                .terminateAllSessions(true)
                .ipAddress(getCurrentIpAddress())
                .source("demo-app")
                .build();

        eventPublisher.publish(event);
        log.debug("Published PasswordResetCompletedEvent for user: {}", user.getId());
    }

    /**
     * Publish UserLogoutEvent (service-level event for session management)
     */
    private void publishLogoutEvent(User user, String logoutType, boolean terminateAll) {
        UserLogoutEvent event = UserLogoutEvent.builder()
                .userId(user.getId())
                .email(user.getEmail().getValue())
                .sessionId(null) // All sessions
                .logoutType(logoutType)
                .forced(true)
                .terminateAllSessions(terminateAll)
                .source("demo-app")
                .build();

        eventPublisher.publish(event);
        log.debug("Published UserLogoutEvent for user: {} (type={})", user.getId(), logoutType);
    }

    // ====================================
    // Account Status Management
    // ====================================

    /**
     * Activate user account
     *
     * Flow:
     * 1. Load user
     * 2. Call domain method activate() (publishes UserActivatedEvent)
     * 3. Persist
     *
     * Events Published (by domain):
     * - UserActivatedEvent → via domain method
     */
    public void activateUser(UUID userId) {
        log.info("Activating user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(User.class, userId));

        // Activate using domain method (publishes event)
        user.activate();

        // Persist (domain events published automatically)
        userRepository.save(user);
        evictAllCaches();
        publishDomainEvents(user);

        log.info("User activated: {}", userId);
    }

    /**
     * Lock user account
     *
     * Flow:
     * 1. Load user
     * 2. Call domain method lock(reason) (publishes UserLockedEvent)
     * 3. Persist
     *
     * Events Published (by domain):
     * - UserLockedEvent → via domain method
     */
    public void lockUser(UUID userId, String reason) {
        log.info("Locking user: {} - Reason: {}", userId, reason);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(User.class, userId));

        // Lock using domain method (publishes event)
        user.lock(reason);

        // Persist
        userRepository.save(user);
        evictAllCaches();
        publishDomainEvents(user);

        log.warn("User locked: {} - Reason: {}", userId, reason);
    }

    /**
     * Unlock user account
     *
     * Flow:
     * 1. Load user
     * 2. Call domain method unlock() (publishes UserUnlockedEvent)
     * 3. Persist
     *
     * Events Published (by domain):
     * - UserUnlockedEvent → via domain method
     */
    public void unlockUser(UUID userId) {
        log.info("Unlocking user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(User.class, userId));

        // Unlock using domain method (publishes event)
        user.unlock();

        // Persist
        userRepository.save(user);
        evictAllCaches();
        publishDomainEvents(user);

        log.info("User unlocked: {}", userId);
    }

    /**
     * Enable user account
     *
     * Flow:
     * 1. Load user
     * 2. Set enabled=true
     * 3. Publish UserActivatedEvent (service-level, since no domain method)
     *
     * Events Published:
     * - UserActivatedEvent → service-level
     */
    public void enableUser(UUID userId) {
        log.info("Enabling user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(User.class, userId));

        user.setEnabled(true);

        userRepository.save(user);
        evictAllCaches();

        // Publish activation event (service-level, no domain method for this)
        UserActivatedEvent event = UserActivatedEvent.builder()
                .userId(user.getId())
                .email(user.getEmail().getValue())
                .activatedBy(getCurrentUsername())
                .activationMethod("admin_action")
                .source("demo-app")
                .build();

        eventPublisher.publish(event);

        log.info("User enabled: {}", userId);
    }

    /**
     * Disable user account
     *
     * Flow:
     * 1. Load user
     * 2. Set enabled=false
     * 3. Publish UserDeactivatedEvent (service-level, since no domain method)
     *
     * Events Published:
     * - UserDeactivatedEvent → service-level
     */
    public void disableUser(UUID userId) {
        log.info("Disabling user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(User.class, userId));

        user.setEnabled(false);

        userRepository.save(user);
        evictAllCaches();

        // Publish deactivation event (service-level, no domain method for this)
        UserDesactivatedEvent event = UserDesactivatedEvent.builder()
                .userId(user.getId())
                .email(user.getEmail().getValue())
                .deactivatedBy(getCurrentUsername())
                .reason("Admin deactivation")
                .selfService(false)
                .temporary(false)
                .retainData(true)
                .source("demo-app")
                .build();

        eventPublisher.publish(event);

        log.info("User disabled: {}", userId);
    }

    // ====================================
    // Role Management
    // ====================================

    /**
     * Assign roles to user
     *
     * Flow:
     * 1. Load user and roles
     * 2. Remove existing roles if replaceExisting=true
     * 3. Assign new roles using domain methods (publishes events)
     * 4. Persist
     *
     * Events Published (by domain):
     * - UserRoleRemovedEvent → via domain method removeRole()
     * - UserRoleAssignedEvent → via domain method assignRole()
     */
    public UserDetailResponse assignRoles(UUID userId, AssignRoleRequest request) {
        log.info("Assigning roles to user: {}", userId);

        // Load user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(User.class, userId));

        // Parse role IDs
        Set<UUID> roleIds = request.getRoleIds().stream()
                .map(UUID::fromString)
                .collect(Collectors.toSet());

        if (request.getReplaceExisting()) {
            // Remove all existing roles (domain publishes events)
            new HashSet<>(user.getRoles()).forEach(user::removeRole);
        }

        // Load and assign new roles
        List<Role> newRoles = roleRepository.findAllById(roleIds);

        if (newRoles.size() != roleIds.size()) {
            throw new BusinessException("Some role IDs are invalid");
        }

        // Assign roles (domain publishes events)
        newRoles.forEach(user::assignRole);

        // Persist (domain events published automatically)
        User updatedUser = userRepository.save(user);
        evictAllCaches();
        publishDomainEvents(user);

        log.info("Roles updated for user: {}", userId);

        return userMapper.toDetailResponse(updatedUser);
    }

    /**
     * Remove single role from user
     *
     * Flow:
     * 1. Load user and role
     * 2. Remove role from user using domain method (publishes UserRoleRemovedEvent)
     * 3. Persist
     *
     * Events Published (by domain):
     * - UserRoleRemovedEvent → via domain method
     */
    public UserDetailResponse removeRole(UUID userId, UUID roleId) {
        log.info("Removing role {} from user: {}", roleId, userId);

        // Load user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(User.class, userId));

        // Load role
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new EntityNotFoundException(Role.class, roleId));

        // Ensure user has this role
        if (!user.getRoles().contains(role)) {
            throw new BusinessException("User does not have role: " + role.getName());
        }

        // Remove role (domain publishes event)
        user.removeRole(role);

        // Persist
        User updatedUser = userRepository.save(user);
        evictAllCaches();
        publishDomainEvents(user);

        log.info("Role {} removed from user: {}", roleId, userId);

        return userMapper.toDetailResponse(updatedUser);
    }

    // ====================================
    // Lifecycle Hooks (Template Method Pattern)
    // ====================================

    @Override
    protected void beforeCreate(User user) {
        log.debug("Before create user: {}", user.getEmail().getValue());
    }

    @Override
    protected void afterCreate(User user) {
        log.debug("After create user: {}", user.getId());
    }

    @Override
    protected void beforeUpdate(User existing, User updated) {
        log.debug("Before update user: {}", existing.getId());
    }

    @Override
    protected void afterUpdate(User user) {
        log.debug("After update user: {}", user.getId());
    }

    @Override
    protected void beforeDelete(User user) {
        log.debug("Before delete user: {}", user.getId());
    }

    @Override
    protected void afterDelete(User user) {
        log.debug("After delete user: {}", user.getId());
    }

    // ====================================
    // Helper Methods
    // ====================================

    private User buildUserFromRequest(CreateUserRequest request) {
        return User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(Email.of(request.getEmail()))
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .dateOfBirth(request.getDateOfBirth())
                .profile(userMapper.toUserProfile(request))
                .enabled(false)
                .emailVerified(false)
                .accountLocked(false)
                .failedLoginAttempts(0)
                .emailVerificationToken(UUID.randomUUID().toString())
                .build();
    }

    private Set<String> assignRolesToUser(User user, Set<String> roleIds) {
        if (roleIds != null && !roleIds.isEmpty()) {
            Set<UUID> uuidRoleIds = roleIds.stream()
                    .map(UUID::fromString)
                    .collect(Collectors.toSet());

            List<Role> roles = roleRepository.findAllById(uuidRoleIds);

            if (roles.size() != roleIds.size()) {
                throw new BusinessException("Some role IDs are invalid");
            }

            roles.forEach(user::assignRole);

            return roles.stream()
                    .map(Role::getName)
                    .collect(Collectors.toSet());
        } else {
            Optional<Role> defaultRole = roleRepository.findByName("ROLE_USER");
            if (defaultRole.isPresent()) {
                user.assignRole(defaultRole.get());
                return Set.of("ROLE_USER");
            }
            return Collections.emptySet();
        }
    }

    private void assignUserGroup(User user, UUID userGroupId) {
        UserGroup userGroup = userGroupRepository.findById(userGroupId)
                .orElseThrow(() -> new EntityNotFoundException(UserGroup.class, userGroupId));
        user.setUserGroup(userGroup);
    }

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

    private String getCurrentIpAddress() {
        try {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();

                String ipAddress = request.getHeader("X-Forwarded-For");
                if (ipAddress == null || ipAddress.isEmpty()) {
                    ipAddress = request.getHeader("X-Real-IP");
                }
                if (ipAddress == null || ipAddress.isEmpty()) {
                    ipAddress = request.getRemoteAddr();
                }

                if (ipAddress != null && ipAddress.contains(",")) {
                    ipAddress = ipAddress.split(",")[0].trim();
                }

                return ipAddress != null ? ipAddress : "unknown";
            }
        } catch (Exception e) {
            log.debug("Could not get IP address", e);
        }
        return "unknown";
    }
}