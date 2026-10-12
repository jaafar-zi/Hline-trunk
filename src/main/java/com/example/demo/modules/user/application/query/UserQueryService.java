package com.example.demo.modules.user.application.query;

import com.example.demo.common.annotation.ReadOnly;
import com.example.demo.common.base.dto.PageResponse;
import com.example.demo.common.base.service.query.AbstractQueryService;
import com.example.demo.common.exception.EntityNotFoundException;
import com.example.demo.common.normalization.NormalizationService;
import com.example.demo.modules.user.application.dto.filter.UserFilterRequest;
import com.example.demo.modules.user.application.dto.response.*;
import com.example.demo.modules.user.application.mapper.UserMapper;
import com.example.demo.modules.user.domain.model.User;
import com.example.demo.modules.user.domain.repository.UserRepository;
import com.example.demo.modules.user.domain.specification.UserSpecifications;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Query service for User aggregate
 * Handles all read operations with caching and optimizations
 *
 * Features:
 * - Cached queries
 * - Advanced filtering with specifications
 * - Role/permission-based queries
 * - Statistics and analytics
 * - Read replica routing via @ReadOnly
 *
 * @author Architecture Team
 * @version 1.0
 */
@Slf4j
@Service
@Transactional(readOnly = true)
public class UserQueryService extends AbstractQueryService<User, UUID> {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserQueryService(UserRepository userRepository, NormalizationService normalizationService, UserMapper userMapper) {
        super(userRepository, normalizationService);
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    // ====================================
    // Abstract Method Implementations
    // ====================================

    @Override
    protected String getEntityCacheName() {
        return "users";
    }

    @Override
    protected Class<User> getEntityClass() {
        return User.class;
    }

    @Override
    protected long getCacheTtlSeconds() {
        return 1800; // 30 minutes
    }

    // ====================================
    // Basic Queries (with DTO mapping)
    // ====================================

    /**
     * Find user by ID and return detailed response
     */
    @ReadOnly
    @Cacheable(cacheNames = "userDetails", key = "#id", unless = "#result == null")
    public UserDetailResponse findUserDetailById(UUID id) {
        log.debug("Finding user detail by ID: {}", id);

        User user = findById(id); // Uses parent's cached method
        return userMapper.toDetailResponse(user);
    }

    /**
     * Find user by ID and return summary response
     */
    @ReadOnly
    @Cacheable(cacheNames = "users", key = "'summary:' + #id", unless = "#result == null")
    public UserResponse findUserSummaryById(UUID id) {
        log.debug("Finding user summary by ID: {}", id);

        User user = findById(id);
        return userMapper.toResponse(user);
    }

    /**
     * Find user by email
     */
    @ReadOnly
    @Cacheable(cacheNames = "usersByEmail", key = "#email", unless = "#result == null")
    public UserDetailResponse findByEmail(String email) {
        log.debug("Finding user by email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found with email: " + email));

        return userMapper.toDetailResponse(user);
    }

    /**
     * Find user by email (optional)
     */
    @ReadOnly
    public Optional<UserDetailResponse> findByEmailOptional(String email) {
        log.debug("Finding user by email (optional): {}", email);

        return userRepository.findByEmail(email)
                .map(userMapper::toDetailResponse);
    }

    /**
     * Find user by email with roles (for authentication)
     */
    @ReadOnly
    @Cacheable(cacheNames = "usersByEmailWithRoles", key = "#email", unless = "#result == null")
    public UserDetailResponse findByEmailWithRoles(String email) {
        log.debug("Finding user by email with roles: {}", email);

        User user = userRepository.findByEmailWithRoles(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found with email: " + email));

        return userMapper.toDetailResponse(user);
    }

    /**
     * Check if email exists
     */
    @ReadOnly
    @Cacheable(cacheNames = "userEmailExists", key = "#email")
    public boolean existsByEmail(String email) {
        log.debug("Checking if email exists: {}", email);
        return userRepository.existsByEmail(email);
    }

    // ====================================
    // Advanced Filtering & Search
    // ====================================

    /**
     * Search users with filters and pagination
     */
    @ReadOnly
    public PageResponse<UserResponse> searchUsers(UserFilterRequest filter) {
        log.debug("Searching users with filter: {}", filter);

        Pageable pageable = filter.toPageable();
        Page<User> usersPage;

        if (filter.hasFilters()) {
            // Build specification from filter
            Specification<User> spec = buildSpecificationFromFilter(filter);
            usersPage = findAll(spec, pageable);
        } else {
            // Simple pagination without filters
            usersPage = findAll(pageable);
        }

        // Convert to PageResponse with DTO mapping
        return PageResponse.from(usersPage, userMapper::toResponse);
    }

    /**
     * Search users and return detailed responses
     */
    @ReadOnly
    public PageResponse<UserDetailResponse> searchUsersDetailed(UserFilterRequest filter) {
        log.debug("Searching users (detailed) with filter: {}", filter);

        Specification<User> spec = buildSpecificationFromFilter(filter);
        Page<User> usersPage = findAll(spec, filter.toPageable());

        return PageResponse.from(usersPage, userMapper::toDetailResponse);
    }

    /**
     * Search users by keyword
     */
    @ReadOnly
    public PageResponse<UserResponse> searchByKeyword(String keyword, Pageable pageable) {
        log.debug("Searching users by keyword: {}", keyword);

        Specification<User> spec = UserSpecifications.searchByKeyword(keyword);
        Page<User> usersPage = findAll(spec, pageable);

        return PageResponse.from(usersPage, userMapper::toResponse);
    }

    /**
     * Get all users with pagination (summary)
     */
    @ReadOnly
    public PageResponse<UserResponse> getAllUsers(Pageable pageable) {
        log.debug("Getting all users with pagination");

        Page<User> usersPage = findAll(pageable);
        return PageResponse.from(usersPage, userMapper::toResponse);
    }

    /**
     * Get all users (detailed) with pagination
     */
    @ReadOnly
    public PageResponse<UserDetailResponse> getAllUsersDetailed(Pageable pageable) {
        log.debug("Getting all users (detailed) with pagination");

        Page<User> usersPage = userRepository.findAllWithRoles(pageable);
        return PageResponse.from(usersPage, userMapper::toDetailResponse);
    }

    // ====================================
    // Role-Based Queries
    // ====================================

    /**
     * Find users by role name
     */
    @ReadOnly
    @Cacheable(cacheNames = "usersByRole", key = "#roleName")
    public List<UserResponse> findByRole(String roleName) {
        log.debug("Finding users by role: {}", roleName);

        List<User> users = userRepository.findByRoleName(roleName);
        return users.stream()
                .map(userMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Find users by role name with pagination
     */
    @ReadOnly
    public PageResponse<UserResponse> findByRole(String roleName, Pageable pageable) {
        log.debug("Finding users by role (paginated): {}", roleName);

        Page<User> usersPage = userRepository.findByRoleName(roleName, pageable);
        return PageResponse.from(usersPage, userMapper::toResponse);
    }

    /**
     * Find users with any of the specified roles
     */
    @ReadOnly
    public PageResponse<UserResponse> findByAnyRole(Set<String> roleNames, Pageable pageable) {
        log.debug("Finding users by any of roles: {}", roleNames);

        Specification<User> spec = UserSpecifications.hasAnyRole(new ArrayList<>(roleNames));
        Page<User> usersPage = findAll(spec, pageable);

        return PageResponse.from(usersPage, userMapper::toResponse);
    }

    /**
     * Find users with specific permission
     */
    @ReadOnly
    public List<UserResponse> findByPermission(String permissionName) {
        log.debug("Finding users with permission: {}", permissionName);

        List<User> users = userRepository.findByPermission(permissionName);
        return users.stream()
                .map(userMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Count users by role
     */
    @ReadOnly
    @Cacheable(cacheNames = "userCountByRole", key = "#roleName")
    public long countByRole(String roleName) {
        log.debug("Counting users by role: {}", roleName);
        Specification<User> spec = UserSpecifications.hasRole(roleName);
        return count(spec);
    }

    // ====================================
    // User Group Queries
    // ====================================

    /**
     * Find users in user group
     */
    @ReadOnly
    @Cacheable(cacheNames = "usersByGroup", key = "#userGroupId")
    public List<UserResponse> findByUserGroup(UUID userGroupId) {
        log.debug("Finding users in group: {}", userGroupId);

        List<User> users = userRepository.findByUserGroupId(userGroupId);
        return users.stream()
                .map(userMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Find users in user group with pagination
     */
    @ReadOnly
    public PageResponse<UserResponse> findByUserGroup(UUID userGroupId, Pageable pageable) {
        log.debug("Finding users in group (paginated): {}", userGroupId);

        Page<User> usersPage = userRepository.findByUserGroupId(userGroupId, pageable);
        return PageResponse.from(usersPage, userMapper::toResponse);
    }

    /**
     * Find users without a group
     */
    @ReadOnly
    public List<UserResponse> findUsersWithoutGroup() {
        log.debug("Finding users without group");

        List<User> users = userRepository.findUsersWithoutGroup();
        return users.stream()
                .map(userMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Count users in user group
     */
    @ReadOnly
    public long countByUserGroup(UUID userGroupId) {
        log.debug("Counting users in group: {}", userGroupId);
        Specification<User> spec = UserSpecifications.inUserGroup(userGroupId);
        return count(spec);
    }

    // ====================================
    // Status-Based Queries
    // ====================================

    /**
     * Find all active users
     */
    @ReadOnly
    @Cacheable(cacheNames = "activeUsers", unless = "#result.isEmpty()")
    public List<UserResponse> findActiveUsers() {
        log.debug("Finding all active users");

        Specification<User> spec = UserSpecifications.combine(
                UserSpecifications.isEnabled(true),
                UserSpecifications.isLocked(false),
                UserSpecifications.isEmailVerified(true)
        );

        List<User> users = findAll(spec);
        return users.stream()
                .map(userMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Find active users with pagination
     */
    @ReadOnly
    public PageResponse<UserResponse> findActiveUsers(Pageable pageable) {
        log.debug("Finding active users (paginated)");

        Specification<User> spec = UserSpecifications.combine(
                UserSpecifications.isEnabled(true),
                UserSpecifications.isLocked(false),
                UserSpecifications.isEmailVerified(true)
        );

        Page<User> usersPage = findAll(spec, pageable);
        return PageResponse.from(usersPage, userMapper::toResponse);
    }

    /**
     * Find enabled users
     */
    @ReadOnly
    public PageResponse<UserResponse> findEnabledUsers(Pageable pageable) {
        log.debug("Finding enabled users");

        Page<User> usersPage = userRepository.findAllEnabled(pageable);
        return PageResponse.from(usersPage, userMapper::toResponse);
    }

    /**
     * Find locked users
     */
    @ReadOnly
    public PageResponse<UserResponse> findLockedUsers(Pageable pageable) {
        log.debug("Finding locked users");

        Specification<User> spec = UserSpecifications.isLocked(true);
        Page<User> usersPage = findAll(spec, pageable);

        return PageResponse.from(usersPage, userMapper::toResponse);
    }

    /**
     * Find users with unverified email
     */
    @ReadOnly
    public PageResponse<UserResponse> findUnverifiedUsers(Pageable pageable) {
        log.debug("Finding users with unverified email");

        Specification<User> spec = UserSpecifications.isEmailVerified(false);
        Page<User> usersPage = findAll(spec, pageable);

        return PageResponse.from(usersPage, userMapper::toResponse);
    }

    /**
     * Find users who never logged in
     */
    @ReadOnly
    public PageResponse<UserResponse> findNeverLoggedInUsers(Pageable pageable) {
        log.debug("Finding users who never logged in");

        Specification<User> spec = UserSpecifications.neverLoggedIn();
        Page<User> usersPage = findAll(spec, pageable);

        return PageResponse.from(usersPage, userMapper::toResponse);
    }

    /**
     * Find users with expired passwords
     */
    @ReadOnly
    public List<UserResponse> findUsersWithExpiredPasswords() {
        log.debug("Finding users with expired passwords");

        List<User> users = userRepository.findAllWithExpiredPasswords(LocalDateTime.now());
        return users.stream()
                .map(userMapper::toResponse)
                .collect(Collectors.toList());
    }

    // ====================================
    // Time-Based Queries
    // ====================================

    /**
     * Find users created in date range
     */
    @ReadOnly
    public PageResponse<UserResponse> findUsersCreatedBetween(
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable) {
        log.debug("Finding users created between {} and {}", startDate, endDate);

        List<User> users = userRepository.findByCreatedAtBetween(startDate, endDate);

        // Manual pagination
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), users.size());

        List<UserResponse> pageContent = users.subList(start, end)
                .stream()
                .map(userMapper::toResponse)
                .collect(Collectors.toList());

        Page<UserResponse> page = new org.springframework.data.domain.PageImpl<>(
                pageContent, pageable, users.size());

        return PageResponse.from(page);
    }

    /**
     * Find recently created users
     */
    @ReadOnly
    @Cacheable(cacheNames = "recentUsers", key = "#days")
    public List<UserResponse> findRecentlyCreatedUsers(int days) {
        log.debug("Finding users created in last {} days", days);

        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(days);
        Specification<User> spec = UserSpecifications.createdAfter(cutoffDate);
        List<User> users = findAll(spec);

        return users.stream()
                .map(userMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Find recently active users (based on last login)
     */
    @ReadOnly
    public List<UserResponse> findRecentlyActiveUsers(int days) {
        log.debug("Finding users active in last {} days", days);

        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(days);
        List<User> users = userRepository.findByLastLoginAfter(cutoffDate);

        return users.stream()
                .map(userMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Find inactive users (not logged in for specified days)
     */
    @ReadOnly
    public PageResponse<UserResponse> findInactiveUsers(int days, Pageable pageable) {
        log.debug("Finding users inactive for {} days", days);

        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(days);
        List<User> users = userRepository.findInactiveUsers(cutoffDate);

        // Manual pagination
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), users.size());

        List<UserResponse> pageContent = users.subList(start, end)
                .stream()
                .map(userMapper::toResponse)
                .collect(Collectors.toList());

        Page<UserResponse> page = new org.springframework.data.domain.PageImpl<>(
                pageContent, pageable, users.size());

        return PageResponse.from(page);
    }

    // ====================================
    // Statistics & Analytics
    // ====================================

    /**
     * Get user statistics
     */
    @ReadOnly
    @Cacheable(cacheNames = "userStats", unless = "#result == null")
    public UserStatistics getUserStatistics() {
        log.debug("Getting user statistics");

        long totalUsers = count();
        long enabledUsers = userRepository.countEnabled();
        long lockedUsers = userRepository.countLocked();
        long unverifiedUsers = userRepository.countPendingVerification();

        // Calculate active users (enabled, not locked, verified)
        Specification<User> activeSpec = UserSpecifications.combine(
                UserSpecifications.isEnabled(true),
                UserSpecifications.isLocked(false),
                UserSpecifications.isEmailVerified(true)
        );
        long activeUsers = count(activeSpec);

        long neverLoggedIn = count(UserSpecifications.neverLoggedIn());

        return UserStatistics.builder()
                .totalUsers(totalUsers)
                .activeUsers(activeUsers)
                .enabledUsers(enabledUsers)
                .lockedUsers(lockedUsers)
                .unverifiedUsers(unverifiedUsers)
                .verifiedUsers(totalUsers - unverifiedUsers)
                .neverLoggedInUsers(neverLoggedIn)
                .inactiveUsers(totalUsers - activeUsers)
                .createdToday(userRepository.countCreatedToday())
                .loggedInToday(userRepository.countLoggedInToday())
                .build();
    }

    /**
     * Get user activity summary
     */
    @ReadOnly
    public UserActivitySummary getUserActivitySummary(int days) {
        log.debug("Getting user activity summary for {} days", days);

        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(days);

        long total = count();
        long activeInPeriod = count(UserSpecifications.loggedInAfter(cutoffDate));
        long createdInPeriod = count(UserSpecifications.createdAfter(cutoffDate));

        return UserActivitySummary.builder()
                .totalUsers(total)
                .activeUsers(activeInPeriod)
                .newUsers(createdInPeriod)
                .inactiveUsers(total - activeInPeriod)
                .periodDays(days)
                .build();
    }

    /**
     * Get user counts by status
     */
    @ReadOnly
    @Cacheable(cacheNames = "userCountsByStatus")
    public Map<String, Long> getUserCountsByStatus() {
        log.debug("Getting user counts by status");

        Map<String, Long> counts = new HashMap<>();
        counts.put("TOTAL", count());
        counts.put("ENABLED", userRepository.countEnabled());
        counts.put("LOCKED", userRepository.countLocked());
        counts.put("PENDING_VERIFICATION", userRepository.countPendingVerification());
        counts.put("NEVER_LOGGED_IN", count(UserSpecifications.neverLoggedIn()));

        return counts;
    }

    // ====================================
    // Utility Methods
    // ====================================

    /**
     * Find user entity by ID (for internal use)
     */
    @ReadOnly
    public User findUserEntityById(UUID id) {
        return findById(id);
    }

    /**
     * Find user entity by email (for internal use)
     */
    @ReadOnly
    public Optional<User> findUserEntityByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Find user entity by email with roles (for authentication)
     */
    @ReadOnly
    public Optional<User> findUserEntityByEmailWithRoles(String email) {
        return userRepository.findByEmailWithRoles(email);
    }

    /**
     * Validate user exists
     */
    @ReadOnly
    public void ensureUserExists(UUID userId) {
        ensureExists(userId);
    }

    /**
     * Find users by IDs
     */
    @ReadOnly
    public List<UserResponse> findByIds(List<UUID> ids) {
        log.debug("Finding users by IDs: {}", ids.size());

        List<User> users = userRepository.findByIdInWithRoles(ids);
        return users.stream()
                .map(userMapper::toResponse)
                .collect(Collectors.toList());
    }

    // ====================================
    // Helper Methods
    // ====================================

    /**
     * Build specification from filter request
     */
    private Specification<User> buildSpecificationFromFilter(UserFilterRequest filter) {
        List<Specification<User>> specs = new ArrayList<>();

        // Keyword search
        if (filter.getKeyword() != null && !filter.getKeyword().isBlank()) {
            specs.add(UserSpecifications.searchByKeyword(filter.getKeyword()));
        }

        // Email filter
        if (filter.getEmail() != null && !filter.getEmail().isBlank()) {
            specs.add(UserSpecifications.hasEmail(filter.getEmail()));
        }

        // First name filter
        if (filter.getFirstName() != null && !filter.getFirstName().isBlank()) {
            specs.add(UserSpecifications.hasFirstNameLike(filter.getFirstName()));
        }

        // Last name filter
        if (filter.getLastName() != null && !filter.getLastName().isBlank()) {
            specs.add(UserSpecifications.hasLastNameLike(filter.getLastName()));
        }

        // Enabled filter
        if (filter.getEnabled() != null) {
            specs.add(UserSpecifications.isEnabled(filter.getEnabled()));
        }

        // Locked filter
        if (filter.getLocked() != null) {
            specs.add(UserSpecifications.isLocked(filter.getLocked()));
        }

        // Email verified filter
        if (filter.getEmailVerified() != null) {
            specs.add(UserSpecifications.isEmailVerified(filter.getEmailVerified()));
        }

        // Status filter
        if (filter.getStatus() != null) {
            specs.add(UserSpecifications.hasStatus(filter.getStatus()));
        }

        // Role filter
        if (filter.getRoleNames() != null && !filter.getRoleNames().isEmpty()) {
            specs.add(UserSpecifications.hasAnyRole(new ArrayList<>(filter.getRoleNames())));
        }

        // User group filter
        if (filter.getUserGroupId() != null) {
            specs.add(UserSpecifications.inUserGroup(UUID.fromString(filter.getUserGroupId())));
        }

        // Created after
        if (filter.getCreatedAfter() != null) {
            specs.add(UserSpecifications.createdAfter(filter.getCreatedAfter()));
        }

        // Created before
        if (filter.getCreatedBefore() != null) {
            specs.add(UserSpecifications.createdBefore(filter.getCreatedBefore()));
        }

        // Last login after
        if (filter.getLastLoginAfter() != null) {
            specs.add(UserSpecifications.loggedInAfter(filter.getLastLoginAfter()));
        }

        // Never logged in
        if (Boolean.TRUE.equals(filter.getNeverLoggedIn())) {
            specs.add(UserSpecifications.neverLoggedIn());
        }

        // Combine all specifications with AND
        return UserSpecifications.combine(specs.toArray(new Specification[0]));
    }

    // ====================================
    // Statistics DTOs
    // ====================================

    /**
     * User statistics response
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class UserStatistics {
        private Long totalUsers;
        private Long activeUsers;
        private Long inactiveUsers;
        private Long enabledUsers;
        private Long lockedUsers;
        private Long verifiedUsers;
        private Long unverifiedUsers;
        private Long neverLoggedInUsers;
        private Long createdToday;
        private Long loggedInToday;
    }

    /**
     * User activity summary
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class UserActivitySummary {
        private Long totalUsers;
        private Long activeUsers;
        private Long inactiveUsers;
        private Long newUsers;
        private Integer periodDays;
    }
}