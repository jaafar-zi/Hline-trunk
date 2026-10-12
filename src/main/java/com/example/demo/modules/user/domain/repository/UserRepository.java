package com.example.demo.modules.user.domain.repository;

import com.example.demo.common.base.repository.BaseRepository;
import com.example.demo.modules.user.domain.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * User Repository
 *
 * Repository for User aggregate root
 * Contains custom query methods for user-specific operations
 *
 * Features:
 * - Email-based queries (with Value Object support)
 * - Full name search capabilities
 * - Role and permission queries
 * - User group management
 * - Status and verification queries
 * - Comprehensive search functionality
 * - Statistics and analytics
 * - Eager loading optimizations
 *
 * @author Architecture Team
 * @version 1.0
 */
@Repository
public interface UserRepository extends BaseRepository<User, UUID> {

    // ====================================
    // Find by Email (Value Object)
    // ====================================

    /**
     * Find user by email
     * Most common query - used for login and authentication
     *
     * @param email the email address to search for
     * @return Optional containing the user if found
     */
    @Query("SELECT u FROM User u WHERE u.email.value = :email")
    Optional<User> findByEmail(@Param("email") String email);

    /**
     * Find user by email with roles eagerly loaded
     * Optimized for authentication/authorization flows
     * Prevents N+1 queries
     *
     * @param email the email address
     * @return Optional containing user with roles loaded
     */
    @EntityGraph(attributePaths = {"roles", "roles.permissions", "userGroup", "userGroup.roles"})
    @Query("SELECT u FROM User u WHERE u.email.value = :email")
    Optional<User> findByEmailWithRoles(@Param("email") String email);

    /**
     * Check if email exists
     *
     * @param email the email to check
     * @return true if email exists, false otherwise
     */
    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u WHERE u.email.value = :email")
    boolean existsByEmail(@Param("email") String email);

    /**
     * Check if email exists for a different user
     * Useful for update operations to ensure uniqueness
     *
     * @param email  the email to check
     * @param userId the user ID to exclude from the check
     * @return true if email exists for another user
     */
    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u " +
            "WHERE u.email.value = :email AND u.id <> :userId")
    boolean existsByEmailAndIdNot(@Param("email") String email, @Param("userId") UUID userId);

    // ====================================
    // Find by Name - Basic
    // ====================================

    /**
     * Find users by first name (exact match, case-insensitive)
     *
     * @param firstName the first name to search for
     * @return list of users with matching first name
     */
    @Query("SELECT u FROM User u WHERE LOWER(u.firstName) = LOWER(:firstName)")
    List<User> findByFirstName(@Param("firstName") String firstName);

    /**
     * Find users by last name (exact match, case-insensitive)
     *
     * @param lastName the last name to search for
     * @return list of users with matching last name
     */
    @Query("SELECT u FROM User u WHERE LOWER(u.lastName) = LOWER(:lastName)")
    List<User> findByLastName(@Param("lastName") String lastName);

    /**
     * Find users by first name and last name (exact match)
     *
     * @param firstName the first name
     * @param lastName  the last name
     * @return list of matching users
     */
    @Query("SELECT u FROM User u WHERE LOWER(u.firstName) = LOWER(:firstName) " +
            "AND LOWER(u.lastName) = LOWER(:lastName)")
    List<User> findByFirstNameAndLastName(@Param("firstName") String firstName,
                                           @Param("lastName") String lastName);

    /**
     * Find users by first name containing (partial match)
     *
     * @param firstName partial first name
     * @return list of matching users
     */
    @Query("SELECT u FROM User u WHERE LOWER(u.firstName) LIKE LOWER(CONCAT('%', :firstName, '%'))")
    List<User> findByFirstNameContaining(@Param("firstName") String firstName);

    /**
     * Find users by last name containing (partial match)
     *
     * @param lastName partial last name
     * @return list of matching users
     */
    @Query("SELECT u FROM User u WHERE LOWER(u.lastName) LIKE LOWER(CONCAT('%', :lastName, '%'))")
    List<User> findByLastNameContaining(@Param("lastName") String lastName);

    // ====================================
    // Find by Full Name
    // ====================================

    /**
     * Find user by exact full name
     * Searches for "FirstName LastName" as concatenated string
     *
     * @param fullName the full name (e.g., "John Doe")
     * @return Optional containing the user if found
     */
    @Query("SELECT u FROM User u WHERE " +
            "LOWER(CONCAT(u.firstName, ' ', u.lastName)) = LOWER(:fullName)")
    Optional<User> findByFullName(@Param("fullName") String fullName);

    /**
     * Find all users by exact full name
     * Returns list in case of duplicates
     *
     * @param fullName the full name
     * @return list of users with matching full name
     */
    @Query("SELECT u FROM User u WHERE " +
            "LOWER(CONCAT(u.firstName, ' ', u.lastName)) = LOWER(:fullName)")
    List<User> findAllByFullName(@Param("fullName") String fullName);

    /**
     * Find users by full name containing (partial match)
     *
     * @param searchTerm partial name to search for
     * @return list of matching users
     */
    @Query("SELECT u FROM User u WHERE " +
            "LOWER(CONCAT(u.firstName, ' ', u.lastName)) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<User> findByFullNameContaining(@Param("searchTerm") String searchTerm);

    /**
     * Find users by full name containing with pagination
     *
     * @param searchTerm partial name to search for
     * @param pageable   pagination parameters
     * @return page of matching users
     */
    @Query("SELECT u FROM User u WHERE " +
            "LOWER(CONCAT(u.firstName, ' ', u.lastName)) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<User> findByFullNameContaining(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Find users where full name starts with given prefix
     *
     * @param prefix the prefix to search for
     * @return list of matching users
     */
    @Query("SELECT u FROM User u WHERE " +
            "LOWER(CONCAT(u.firstName, ' ', u.lastName)) LIKE LOWER(CONCAT(:prefix, '%'))")
    List<User> findByFullNameStartingWith(@Param("prefix") String prefix);

    /**
     * Find users where full name ends with given suffix
     *
     * @param suffix the suffix to search for
     * @return list of matching users
     */
    @Query("SELECT u FROM User u WHERE " +
            "LOWER(CONCAT(u.firstName, ' ', u.lastName)) LIKE LOWER(CONCAT('%', :suffix))")
    List<User> findByFullNameEndingWith(@Param("suffix") String suffix);

    // ====================================
    // Advanced Name Queries
    // ====================================

    /**
     * Search by first name OR last name containing
     *
     * @param searchTerm the search term
     * @return list of matching users
     */
    @Query("SELECT u FROM User u WHERE " +
            "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<User> searchByName(@Param("searchTerm") String searchTerm);

    /**
     * Search by first name OR last name containing with pagination
     *
     * @param searchTerm the search term
     * @param pageable   pagination parameters
     * @return page of matching users
     */
    @Query("SELECT u FROM User u WHERE " +
            "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<User> searchByName(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Find users by initials
     * Example: "JD" finds "John Doe"
     *
     * @param initials the initials (e.g., "JD")
     * @return list of users with matching initials
     */
    @Query("SELECT u FROM User u WHERE " +
            "UPPER(CONCAT(SUBSTRING(u.firstName, 1, 1), SUBSTRING(u.lastName, 1, 1))) = UPPER(:initials)")
    List<User> findByInitials(@Param("initials") String initials);

    // ====================================
    // Find by Display Name (UserProfile)
    // ====================================

    /**
     * Find users by profile display name (exact match)
     *
     * @param displayName the display name
     * @return list of matching users
     */
    @Query("SELECT u FROM User u WHERE u.profile IS NOT NULL " +
            "AND LOWER(u.profile.displayName) = LOWER(:displayName)")
    List<User> findByDisplayName(@Param("displayName") String displayName);

    /**
     * Find users by profile display name containing
     *
     * @param displayName partial display name
     * @return list of matching users
     */
    @Query("SELECT u FROM User u WHERE u.profile IS NOT NULL " +
            "AND LOWER(u.profile.displayName) LIKE LOWER(CONCAT('%', :displayName, '%'))")
    List<User> findByDisplayNameContaining(@Param("displayName") String displayName);

    // ====================================
    // Comprehensive Search
    // ====================================

    /**
     * Search users by name (first, last, full) or email
     * Most comprehensive search for general user lookup
     *
     * @param searchTerm the search term
     * @param pageable   pagination parameters
     * @return page of matching users
     */
    @Query("SELECT u FROM User u WHERE " +
            "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(CONCAT(u.firstName, ' ', u.lastName)) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(u.email.value) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<User> searchUsers(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Search users by name, email, or display name
     * Extended search including profile display name
     *
     * @param searchTerm the search term
     * @param pageable   pagination parameters
     * @return page of matching users
     */
    @Query("SELECT u FROM User u WHERE " +
            "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(CONCAT(u.firstName, ' ', u.lastName)) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(u.email.value) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "(u.profile IS NOT NULL AND LOWER(u.profile.displayName) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<User> searchUsersExtended(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Search enabled users only
     *
     * @param searchTerm the search term
     * @param pageable   pagination parameters
     * @return page of matching enabled users
     */
    @Query("SELECT u FROM User u WHERE u.enabled = true AND (" +
            "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(CONCAT(u.firstName, ' ', u.lastName)) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(u.email.value) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<User> searchEnabledUsers(@Param("searchTerm") String searchTerm, Pageable pageable);

    // ====================================
    // Find by Status
    // ====================================

    /**
     * Find all enabled users
     *
     * @return list of enabled users
     */
    @Query("SELECT u FROM User u WHERE u.enabled = true")
    List<User> findAllEnabled();

    /**
     * Find all enabled users with pagination
     *
     * @param pageable pagination parameters
     * @return page of enabled users
     */
    @Query("SELECT u FROM User u WHERE u.enabled = true")
    Page<User> findAllEnabled(Pageable pageable);

    /**
     * Find all locked users
     *
     * @return list of locked users
     */
    @Query("SELECT u FROM User u WHERE u.accountLocked = true")
    List<User> findAllLocked();

    /**
     * Find all locked users with pagination
     *
     * @param pageable pagination parameters
     * @return page of locked users
     */
    @Query("SELECT u FROM User u WHERE u.accountLocked = true")
    Page<User> findAllLocked(Pageable pageable);

    /**
     * Find all users pending email verification
     *
     * @return list of users pending verification
     */
    @Query("SELECT u FROM User u WHERE u.emailVerified = false")
    List<User> findAllPendingVerification();

    /**
     * Find all users pending email verification with pagination
     *
     * @param pageable pagination parameters
     * @return page of users pending verification
     */
    @Query("SELECT u FROM User u WHERE u.emailVerified = false")
    Page<User> findAllPendingVerification(Pageable pageable);

    /**
     * Find all users with expired passwords
     *
     * @param now current timestamp
     * @return list of users with expired passwords
     */
    @Query("SELECT u FROM User u WHERE u.passwordExpiresAt IS NOT NULL AND u.passwordExpiresAt < :now")
    List<User> findAllWithExpiredPasswords(@Param("now") LocalDateTime now);

    /**
     * Find all users with temporary locks that have expired
     *
     * @param now current timestamp
     * @return list of users with expired temporary locks
     */
    @Query("SELECT u FROM User u WHERE u.accountLocked = true " +
            "AND u.lockedUntil IS NOT NULL AND u.lockedUntil < :now")
    List<User> findAllWithExpiredLocks(@Param("now") LocalDateTime now);

    /**
     * Find all disabled users
     *
     * @return list of disabled users
     */
    @Query("SELECT u FROM User u WHERE u.enabled = false")
    List<User> findAllDisabled();

    /**
     * Find all active users (enabled, not locked, email verified)
     *
     * @return list of active users
     */
    @Query("SELECT u FROM User u WHERE u.enabled = true " +
            "AND u.accountLocked = false AND u.emailVerified = true")
    List<User> findAllActive();

    /**
     * Find all active users with pagination
     *
     * @param pageable pagination parameters
     * @return page of active users
     */
    @Query("SELECT u FROM User u WHERE u.enabled = true " +
            "AND u.accountLocked = false AND u.emailVerified = true")
    Page<User> findAllActive(Pageable pageable);

    // ====================================
    // Find by Role
    // ====================================

    /**
     * Find users by role name
     *
     * @param roleName the role name
     * @return list of users with the specified role
     */
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName")
    List<User> findByRoleName(@Param("roleName") String roleName);

    /**
     * Find user IDs by role ID
     * Optimized query that returns only IDs
     *
     * @param roleId the role ID
     * @return set of user IDs
     */
    @Query("SELECT u.id FROM User u JOIN u.roles r WHERE r.id = :roleId")
    Set<UUID> findUserIdsByRoleId(@Param("roleId") UUID roleId);

    /**
     * Find users by role name with pagination
     *
     * @param roleName the role name
     * @param pageable pagination parameters
     * @return page of users with the specified role
     */
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName")
    Page<User> findByRoleName(@Param("roleName") String roleName, Pageable pageable);

    /**
     * Find users with any of the specified roles
     *
     * @param roleNames list of role names
     * @return list of users with any of the specified roles
     */
    @Query("SELECT DISTINCT u FROM User u JOIN u.roles r WHERE r.name IN :roleNames")
    List<User> findByRoleNameIn(@Param("roleNames") List<String> roleNames);

    /**
     * Find users with any of the specified roles with pagination
     *
     * @param roleNames list of role names
     * @param pageable  pagination parameters
     * @return page of users with any of the specified roles
     */
    @Query("SELECT DISTINCT u FROM User u JOIN u.roles r WHERE r.name IN :roleNames")
    Page<User> findByRoleNameIn(@Param("roleNames") List<String> roleNames, Pageable pageable);

    /**
     * Find users with specific permission
     *
     * @param permissionName the permission name
     * @return list of users with the specified permission
     */
    @Query("SELECT DISTINCT u FROM User u JOIN u.roles r JOIN r.permissions p WHERE p.name = :permissionName")
    List<User> findByPermission(@Param("permissionName") String permissionName);

    /**
     * Find users with any of the specified permissions
     *
     * @param permissionNames list of permission names
     * @return list of users with any of the specified permissions
     */
    @Query("SELECT DISTINCT u FROM User u JOIN u.roles r JOIN r.permissions p WHERE p.name IN :permissionNames")
    List<User> findByPermissionIn(@Param("permissionNames") List<String> permissionNames);

    // ====================================
    // Find by User Group
    // ====================================

    /**
     * Find users by user group ID
     *
     * @param groupId the user group ID
     * @return list of users in the group
     */
    @Query("SELECT u FROM User u WHERE u.userGroup.id = :groupId")
    List<User> findByUserGroupId(@Param("groupId") UUID groupId);

    /**
     * Find users by user group ID with pagination
     *
     * @param groupId  the user group ID
     * @param pageable pagination parameters
     * @return page of users in the group
     */
    @Query("SELECT u FROM User u WHERE u.userGroup.id = :groupId")
    Page<User> findByUserGroupId(@Param("groupId") UUID groupId, Pageable pageable);

    /**
     * Find users without a user group
     *
     * @return list of users without a group
     */
    @Query("SELECT u FROM User u WHERE u.userGroup IS NULL")
    List<User> findUsersWithoutGroup();

    /**
     * Find users without a user group with pagination
     *
     * @param pageable pagination parameters
     * @return page of users without a group
     */
    @Query("SELECT u FROM User u WHERE u.userGroup IS NULL")
    Page<User> findUsersWithoutGroup(Pageable pageable);

    // ====================================
    // Find by Date Range
    // ====================================

    /**
     * Find users created between dates
     *
     * @param startDate start date
     * @param endDate   end date
     * @return list of users created in the date range
     */
    @Query("SELECT u FROM User u WHERE u.createdAt BETWEEN :startDate AND :endDate")
    List<User> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate,
                                      @Param("endDate") LocalDateTime endDate);

    /**
     * Find users who logged in after specific date
     *
     * @param since the date threshold
     * @return list of users who logged in after the date
     */
    @Query("SELECT u FROM User u WHERE u.lastLoginAt > :since")
    List<User> findByLastLoginAfter(@Param("since") LocalDateTime since);

    /**
     * Find users who never logged in
     *
     * @return list of users who never logged in
     */
    @Query("SELECT u FROM User u WHERE u.lastLoginAt IS NULL AND u.enabled = true")
    List<User> findNeverLoggedIn();

    /**
     * Find inactive users (no login since specific date)
     *
     * @param since the date threshold
     * @return list of inactive users
     */
    @Query("SELECT u FROM User u WHERE u.lastLoginAt < :since OR (u.lastLoginAt IS NULL AND u.createdAt < :since)")
    List<User> findInactiveUsers(@Param("since") LocalDateTime since);

    /**
     * Find users by date of birth
     *
     * @param dateOfBirth the date of birth
     * @return list of users with matching date of birth
     */
    @Query("SELECT u FROM User u WHERE u.dateOfBirth = :dateOfBirth")
    List<User> findByDateOfBirth(@Param("dateOfBirth") LocalDate dateOfBirth);

    /**
     * Find users with birthdays in current month
     *
     * @return list of users with birthdays this month
     */
    @Query("SELECT u FROM User u WHERE MONTH(u.dateOfBirth) = MONTH(CURRENT_DATE)")
    List<User> findUsersWithBirthdayThisMonth();

    /**
     * Find users with birthday today
     *
     * @return list of users with birthdays today
     */
    @Query("SELECT u FROM User u WHERE MONTH(u.dateOfBirth) = MONTH(CURRENT_DATE) " +
            "AND DAY(u.dateOfBirth) = DAY(CURRENT_DATE)")
    List<User> findUsersWithBirthdayToday();

    /**
     * Find users with upcoming birthdays (within next N days)
     *
     * @param days number of days to look ahead
     * @return list of users with upcoming birthdays
     */
@Query("SELECT u FROM User u WHERE u.dateOfBirth IS NOT NULL " +
       "AND (MONTH(u.dateOfBirth) * 100 + DAY(u.dateOfBirth)) " +
       "BETWEEN (MONTH(:startDate) * 100 + DAY(:startDate)) " +
       "AND (MONTH(:endDate) * 100 + DAY(:endDate))")
List<User> findUsersWithUpcomingBirthdays(@Param("startDate") LocalDate startDate,
                                          @Param("endDate") LocalDate endDate);

    // ====================================
    // Statistics
    // ====================================

    /**
     * Count enabled users
     *
     * @return count of enabled users
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.enabled = true")
    long countEnabled();

    /**
     * Count locked users
     *
     * @return count of locked users
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.accountLocked = true")
    long countLocked();

    /**
     * Count users pending verification
     *
     * @return count of users pending verification
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.emailVerified = false")
    long countPendingVerification();

    /**
     * Count users by role name
     *
     * @param roleName the role name
     * @return count of users with the role
     */
    @Query("SELECT COUNT(DISTINCT u) FROM User u JOIN u.roles r WHERE r.name = :roleName")
    long countByRoleName(@Param("roleName") String roleName);

    /**
     * Count users by role ID
     *
     * @param roleId the role ID
     * @return count of users with the role
     */
    @Query("SELECT COUNT(DISTINCT u) FROM User u JOIN u.roles r WHERE r.id = :roleId")
    long countByRoleId(@Param("roleId") UUID roleId);

    /**
     * Count users by user group ID
     *
     * @param groupId the user group ID
     * @return count of users in the group
     */
    @Query("SELECT COUNT(DISTINCT u) FROM User u WHERE u.userGroup.id = :groupId")
    long countByUserGroupId(@Param("groupId") UUID groupId);

    /**
     * Count users by multiple role IDs
     *
     * @param roleIds set of role IDs
     * @return count of users with any of the roles
     */
    @Query("SELECT COUNT(DISTINCT u) FROM User u JOIN u.roles r WHERE r.id IN :roleIds")
    long countByRoleIds(@Param("roleIds") Set<UUID> roleIds);

    /**
     * Count users created today
     *
     * @return count of users created today
     */
    @Query("SELECT COUNT(u) FROM User u WHERE FUNCTION('DATE', u.createdAt) = CURRENT_DATE")
    long countCreatedToday();

    /**
     * Count users logged in today
     *
     * @return count of users logged in today
     */
    @Query("SELECT COUNT(u) FROM User u WHERE FUNCTION('DATE', u.lastLoginAt) = CURRENT_DATE")
    long countLoggedInToday();

    /**
     * Count active users (enabled, not locked, email verified)
     *
     * @return count of active users
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.enabled = true " +
            "AND u.accountLocked = false AND u.emailVerified = true")
    long countActive();

    // ====================================
    // Verification
    // ====================================

    /**
     * Find user by email verification token
     *
     * @param token the verification token
     * @return Optional containing the user if found
     */
    @Query("SELECT u FROM User u WHERE u.emailVerificationToken = :token")
    Optional<User> findByEmailVerificationToken(@Param("token") String token);

    // ====================================
    // Bulk Operations with Eager Loading
    // ====================================

    /**
     * Find users by IDs with roles eagerly loaded
     * Optimized for bulk operations
     *
     * @param ids list of user IDs
     * @return list of users with roles loaded
     */
    @EntityGraph(attributePaths = {"roles", "userGroup"})
    @Query("SELECT u FROM User u WHERE u.id IN :ids")
    List<User> findByIdInWithRoles(@Param("ids") List<UUID> ids);

    /**
     * Find all users with roles eagerly loaded
     * Warning: Use with caution - can be expensive for large datasets
     *
     * @return list of all users with roles loaded
     */
    @EntityGraph(attributePaths = {"roles", "roles.permissions", "userGroup"})
    @Query("SELECT DISTINCT u FROM User u")
    List<User> findAllWithRoles();

    /**
     * Find all users with roles and pagination
     * Recommended over findAllWithRoles() for better performance
     *
     * @param pageable pagination parameters
     * @return page of users with roles loaded
     */
    @EntityGraph(attributePaths = {"roles", "roles.permissions", "userGroup"})
    @Query("SELECT DISTINCT u FROM User u")
    Page<User> findAllWithRoles(Pageable pageable);

    /**
     * Find users by IDs with full relationship graph
     * Includes roles, permissions, and user group
     *
     * @param ids list of user IDs
     * @return list of users with full graph loaded
     */
    @EntityGraph(attributePaths = {"roles", "roles.permissions", "userGroup", "userGroup.roles"})
    @Query("SELECT u FROM User u WHERE u.id IN :ids")
    List<User> findByIdInWithFullGraph(@Param("ids") List<UUID> ids);

    /**
     * Find enabled users with roles
     *
     * @param pageable pagination parameters
     * @return page of enabled users with roles loaded
     */
    @EntityGraph(attributePaths = {"roles", "userGroup"})
    @Query("SELECT u FROM User u WHERE u.enabled = true")
    Page<User> findEnabledUsersWithRoles(Pageable pageable);

    /**
     * Find users by role with full graph loaded
     *
     * @param roleName the role name
     * @param pageable pagination parameters
     * @return page of users with full graph loaded
     */
    @EntityGraph(attributePaths = {"roles", "roles.permissions", "userGroup"})
    @Query("SELECT DISTINCT u FROM User u JOIN u.roles r WHERE r.name = :roleName")
    Page<User> findByRoleNameWithFullGraph(@Param("roleName") String roleName, Pageable pageable);
}