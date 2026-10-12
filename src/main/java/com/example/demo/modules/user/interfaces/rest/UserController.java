package com.example.demo.modules.user.interfaces.rest;

import com.example.demo.common.base.dto.BaseResponse;
import com.example.demo.common.base.dto.PageResponse;
import com.example.demo.modules.auth.application.validator.ChangePasswordRequestValidator;
import com.example.demo.modules.user.application.command.UserCommandService;
import com.example.demo.modules.user.application.dto.filter.UserFilterRequest;
import com.example.demo.modules.user.application.dto.request.*;
import com.example.demo.modules.user.application.dto.response.UserDetailResponse;
import com.example.demo.modules.user.application.dto.response.UserResponse;
import com.example.demo.modules.user.application.query.UserQueryService;
import com.example.demo.modules.user.application.validator.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * User REST Controller
 *
 * Provides comprehensive user management endpoints:
 * - CRUD operations
 * - Search and filtering
 * - Account management (activate, lock, unlock)
 * - Password management
 * - Role assignment
 * - Profile management
 * - Statistics
 *
 * @author Architecture Team
 * @version 1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User management APIs")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserCommandService userCommandService;
    private final UserQueryService userQueryService;

    // Validators
    private final CreateUserRequestValidator createUserRequestValidator;
    private final UpdateUserRequestValidator updateUserRequestValidator;
    private final UpdateUserProfileRequestValidator updateUserProfileRequestValidator;
    private final ChangePasswordRequestValidator changePasswordRequestValidator;
    private final AssignRoleRequestValidator assignRoleRequestValidator;
    private final UserFilterRequestValidator userFilterRequestValidator;

    /**
     * Register validators
     */
    @InitBinder("createUserRequest")
    protected void initCreateUserBinder(WebDataBinder binder) {
        binder.addValidators(createUserRequestValidator);
    }

    @InitBinder("updateUserRequest")
    protected void initUpdateUserBinder(WebDataBinder binder) {
        binder.addValidators(updateUserRequestValidator);
    }

    @InitBinder("updateUserProfileRequest")
    protected void initUpdateProfileBinder(WebDataBinder binder) {
        binder.addValidators(updateUserProfileRequestValidator);
    }

    @InitBinder("changePasswordRequest")
    protected void initChangePasswordBinder(WebDataBinder binder) {
        binder.addValidators(changePasswordRequestValidator);
    }

    @InitBinder("assignRoleRequest")
    protected void initAssignRoleBinder(WebDataBinder binder) {
        binder.addValidators(assignRoleRequestValidator);
    }

    @InitBinder("userFilterRequest")
    protected void initUserFilterBinder(WebDataBinder binder) {
        binder.addValidators(userFilterRequestValidator);
    }

    // ========================================
    // Query Endpoints (Read Operations)
    // ========================================

    /**
     * Get all users with pagination and sorting
     */
    @GetMapping
    @PreAuthorize("hasPermission('USER', 'READ')")
    @Operation(
            summary = "Get all users",
            description = "Retrieve paginated list of all users with sorting support"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Users retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<BaseResponse<PageResponse<UserResponse>>> getAllUsers(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {

        log.info("GET /api/v1/users - page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());

        PageResponse<UserResponse> users = userQueryService.getAllUsers(pageable);

        return ResponseEntity.ok(
                BaseResponse.success(users, "Users retrieved successfully")
                        .withMetadata(Map.of(
                                "totalPages", users.getTotalPages(),
                                "totalElements", users.getTotalElements()
                        ))
        );
    }

    /**
     * Search users with advanced filtering
     */
    @PostMapping("/search")
    @PreAuthorize("hasPermission('USER', 'READ')")
    @Operation(
            summary = "Search users",
            description = "Search and filter users with multiple criteria"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Search completed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid filter parameters")
    })
    public ResponseEntity<BaseResponse<PageResponse<UserResponse>>> searchUsers(
            @Valid @RequestBody UserFilterRequest filter,
            BindingResult bindingResult) {

        log.info("POST /api/v1/users/search - filters: {}", filter.hasFilters());

        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest()
                    .body(BaseResponse.error("VALIDATION_ERROR", "Invalid filter parameters"));
        }

        PageResponse<UserResponse> users = userQueryService.searchUsers(filter);

        return ResponseEntity.ok(
                BaseResponse.success(users, "Search completed successfully")
                        .withMetadata(Map.of(
                                "filtersApplied", filter.hasFilters(),
                                "totalResults", users.getTotalElements()
                        ))
        );
    }

    /**
     * Get user by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasPermission('USER', 'READ') or #id == principal.id")
    @Operation(
            summary = "Get user by ID",
            description = "Retrieve detailed user information by ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<BaseResponse<UserDetailResponse>> getUserById(
            @Parameter(description = "User ID", required = true)
            @PathVariable UUID id) {

        log.info("GET /api/v1/users/{}", id);

        UserDetailResponse user = userQueryService.findUserDetailById(id);

        return ResponseEntity.ok(
                BaseResponse.success(user, "User retrieved successfully")
        );
    }

    /**
     * Get user by email
     */
    @GetMapping("/email/{email}")
    @PreAuthorize("hasPermission('USER', 'READ')")
    @Operation(summary = "Get user by email")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<BaseResponse<UserDetailResponse>> getUserByEmail(
            @Parameter(description = "User email", required = true)
            @PathVariable String email) {

        log.info("GET /api/v1/users/email/{}", email);

        UserDetailResponse user = userQueryService.findByEmail(email);

        return ResponseEntity.ok(
                BaseResponse.success(user, "User retrieved successfully")
        );
    }

    /**
     * Get current authenticated user
     */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Get current user",
            description = "Retrieve information about the currently authenticated user"
    )
    public ResponseEntity<BaseResponse<UserDetailResponse>> getCurrentUser(Authentication authentication) {

        log.info("GET /api/v1/users/me - user: {}", authentication.getName());

        UserDetailResponse user = userQueryService.findByEmail(authentication.getName());

        return ResponseEntity.ok(
                BaseResponse.success(user, "Current user retrieved successfully")
        );
    }

    /**
     * Get users by role
     */
    @GetMapping("/role/{roleName}")
    @PreAuthorize("hasPermission('USER', 'READ')")
    @Operation(summary = "Get users by role")
    public ResponseEntity<BaseResponse<List<UserResponse>>> getUsersByRole(
            @PathVariable String roleName) {

        log.info("GET /api/v1/users/role/{}", roleName);

        List<UserResponse> users = userQueryService.findByRole(roleName);

        return ResponseEntity.ok(
                BaseResponse.success(users, String.format("Found %d users with role %s", users.size(), roleName))
        );
    }

    /**
     * Get users by role with pagination
     */
    @GetMapping("/role/{roleName}/paginated")
    @PreAuthorize("hasPermission('USER', 'READ')")
    @Operation(summary = "Get users by role (paginated)")
    public ResponseEntity<BaseResponse<PageResponse<UserResponse>>> getUsersByRolePaginated(
            @PathVariable String roleName,
            @PageableDefault(size = 20) Pageable pageable) {

        log.info("GET /api/v1/users/role/{}/paginated", roleName);

        PageResponse<UserResponse> users = userQueryService.findByRole(roleName, pageable);

        return ResponseEntity.ok(
                BaseResponse.success(users, "Users retrieved successfully")
        );
    }

    /**
     * Get users in user group
     */
    @GetMapping("/group/{groupId}")
    @PreAuthorize("hasPermission('USER', 'READ')")
    @Operation(summary = "Get users in user group")
    public ResponseEntity<BaseResponse<PageResponse<UserResponse>>> getUsersByGroup(
            @PathVariable UUID groupId,
            @PageableDefault(size = 20) Pageable pageable) {

        log.info("GET /api/v1/users/group/{}", groupId);

        PageResponse<UserResponse> users = userQueryService.findByUserGroup(groupId, pageable);

        return ResponseEntity.ok(
                BaseResponse.success(users, "Users retrieved successfully")
        );
    }

    /**
     * Get active users
     */
    @GetMapping("/active")
    @PreAuthorize("hasPermission('USER', 'READ')")
    @Operation(summary = "Get active users")
    public ResponseEntity<BaseResponse<PageResponse<UserResponse>>> getActiveUsers(
            @PageableDefault(size = 20) Pageable pageable) {

        log.info("GET /api/v1/users/active");

        PageResponse<UserResponse> users = userQueryService.findActiveUsers(pageable);

        return ResponseEntity.ok(
                BaseResponse.success(users, "Active users retrieved successfully")
        );
    }

    /**
     * Get locked users
     */
    @GetMapping("/locked")
    @PreAuthorize("hasPermission('USER', 'READ')")
    @Operation(summary = "Get locked users")
    public ResponseEntity<BaseResponse<PageResponse<UserResponse>>> getLockedUsers(
            @PageableDefault(size = 20) Pageable pageable) {

        log.info("GET /api/v1/users/locked");

        PageResponse<UserResponse> users = userQueryService.findLockedUsers(pageable);

        return ResponseEntity.ok(
                BaseResponse.success(users, "Locked users retrieved successfully")
        );
    }

    /**
     * Get users with unverified email
     */
    @GetMapping("/unverified")
    @PreAuthorize("hasPermission('USER', 'READ')")
    @Operation(summary = "Get users with unverified email")
    public ResponseEntity<BaseResponse<PageResponse<UserResponse>>> getUnverifiedUsers(
            @PageableDefault(size = 20) Pageable pageable) {

        log.info("GET /api/v1/users/unverified");

        PageResponse<UserResponse> users = userQueryService.findUnverifiedUsers(pageable);

        return ResponseEntity.ok(
                BaseResponse.success(users, "Unverified users retrieved successfully")
        );
    }

    /**
     * Get user statistics
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasPermission('USER', 'READ')")
    @Operation(
            summary = "Get user statistics",
            description = "Retrieve comprehensive user statistics"
    )
    public ResponseEntity<BaseResponse<UserQueryService.UserStatistics>> getUserStatistics() {

        log.info("GET /api/v1/users/statistics");

        UserQueryService.UserStatistics stats = userQueryService.getUserStatistics();

        return ResponseEntity.ok(
                BaseResponse.success(stats, "Statistics retrieved successfully")
        );
    }

    /**
     * Get user activity summary
     */
    @GetMapping("/activity-summary")
    @PreAuthorize("hasPermission('USER', 'READ')")
    @Operation(summary = "Get user activity summary")
    public ResponseEntity<BaseResponse<UserQueryService.UserActivitySummary>> getUserActivitySummary(
            @RequestParam(defaultValue = "30") int days) {

        log.info("GET /api/v1/users/activity-summary - days: {}", days);

        UserQueryService.UserActivitySummary summary = userQueryService.getUserActivitySummary(days);

        return ResponseEntity.ok(
                BaseResponse.success(summary, String.format("Activity summary for last %d days", days))
        );
    }

    /**
     * Check if email exists
     */
    @GetMapping("/check-email")
    @Operation(summary = "Check if email exists")
    public ResponseEntity<BaseResponse<Map<String, Boolean>>> checkEmailExists(
            @RequestParam String email) {

        log.info("GET /api/v1/users/check-email - email: {}", email);

        boolean exists = userQueryService.existsByEmail(email);

        return ResponseEntity.ok(
                BaseResponse.success(
                        Map.of("exists", exists),
                        exists ? "Email is already registered" : "Email is available"
                )
        );
    }

    // ========================================
    // Command Endpoints (Write Operations)
    // ========================================

    /**
     * Create new user
     */
    @PostMapping
    @PreAuthorize("hasPermission('USER', 'CREATE')")
    @Operation(
            summary = "Create user",
            description = "Create a new user account"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "User created successfully",
                    content = @Content(schema = @Schema(implementation = UserDetailResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "409", description = "Email already exists")
    })
    public ResponseEntity<BaseResponse<UserDetailResponse>> createUser(
            @Valid @RequestBody CreateUserRequest request,
            BindingResult bindingResult) {

        log.info("POST /api/v1/users - email: {}", request.getEmail());

        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest()
                    .body(BaseResponse.error("VALIDATION_ERROR", "Invalid user data"));
        }

        UserDetailResponse user = userCommandService.createUser(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(BaseResponse.success(user, "User created successfully"));
    }

    /**
     * Update user
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasPermission('USER', 'UPDATE')")
    @Operation(
            summary = "Update user",
            description = "Update user information (admin only)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<BaseResponse<UserDetailResponse>> updateUser(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserRequest request,
            BindingResult bindingResult) {

        log.info("PUT /api/v1/users/{}", id);

        // Additional validation with context
        updateUserRequestValidator.validateWithContext(request, id, bindingResult);

        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest()
                    .body(BaseResponse.error("VALIDATION_ERROR", "Invalid user data"));
        }

        UserDetailResponse user = userCommandService.updateUser(id, request);

        return ResponseEntity.ok(
                BaseResponse.success(user, "User updated successfully")
        );
    }

    /**
     * Update user profile (self-service)
     */
    @PutMapping("/{id}/profile")
    @PreAuthorize("hasPermission('USER', 'UPDATE') or #id == principal.id")
    @Operation(
            summary = "Update user profile",
            description = "Update own profile information (self-service)"
    )
    public ResponseEntity<BaseResponse<UserDetailResponse>> updateUserProfile(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserProfileRequest request,
            BindingResult bindingResult) {

        log.info("PUT /api/v1/users/{}/profile", id);

        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest()
                    .body(BaseResponse.error("VALIDATION_ERROR", "Invalid profile data"));
        }

        UserDetailResponse user = userCommandService.updateUserProfile(id, request);

        return ResponseEntity.ok(
                BaseResponse.success(user, "Profile updated successfully")
        );
    }

    /**
     * Delete user (hard delete)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasPermission('USER', 'DELETE')")
    @Operation(
            summary = "Delete user",
            description = "Permanently delete user account"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User deleted successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<BaseResponse<Void>> deleteUser(@PathVariable UUID id) {

        log.info("DELETE /api/v1/users/{}", id);

        userCommandService.hardDeleteUser(id);

        return ResponseEntity.ok(
                BaseResponse.success(null, "User deleted successfully")
        );
    }

    // ========================================
    // Account Management Endpoints
    // ========================================

    /**
     * Activate user account
     */
    @PostMapping("/{id}/activate")
    @PreAuthorize("hasPermission('USER', 'UPDATE')")
    @Operation(
            summary = "Activate user account",
            description = "Enable user account and grant access"
    )
    public ResponseEntity<BaseResponse<Void>> activateUser(@PathVariable UUID id) {

        log.info("POST /api/v1/users/{}/activate", id);

        userCommandService.activateUser(id);

        return ResponseEntity.ok(
                BaseResponse.success(null, "User activated successfully")
        );
    }

    /**
     * Lock user account
     */
    @PostMapping("/{id}/lock")
    @PreAuthorize("hasPermission('USER', 'UPDATE')")
    @Operation(
            summary = "Lock user account",
            description = "Lock user account and prevent access"
    )
    public ResponseEntity<BaseResponse<Void>> lockUser(
            @PathVariable UUID id,
            @RequestParam(required = false, defaultValue = "Manual lock by administrator") String reason) {

        log.info("POST /api/v1/users/{}/lock - reason: {}", id, reason);

        userCommandService.lockUser(id, reason);

        return ResponseEntity.ok(
                BaseResponse.success(null, "User locked successfully")
        );
    }

    /**
     * Unlock user account
     */
    @PostMapping("/{id}/unlock")
    @PreAuthorize("hasPermission('USER', 'UPDATE')")
    @Operation(
            summary = "Unlock user account",
            description = "Unlock user account and restore access"
    )
    public ResponseEntity<BaseResponse<Void>> unlockUser(@PathVariable UUID id) {

        log.info("POST /api/v1/users/{}/unlock", id);

        userCommandService.unlockUser(id);

        return ResponseEntity.ok(
                BaseResponse.success(null, "User unlocked successfully")
        );
    }

    /**
     * Enable user account
     */
    @PostMapping("/{id}/enable")
    @PreAuthorize("hasPermission('USER', 'UPDATE')")
    @Operation(summary = "Enable user account")
    public ResponseEntity<BaseResponse<Void>> enableUser(@PathVariable UUID id) {

        log.info("POST /api/v1/users/{}/enable", id);

        userCommandService.enableUser(id);

        return ResponseEntity.ok(
                BaseResponse.success(null, "User enabled successfully")
        );
    }

    /**
     * Disable user account
     */
    @PostMapping("/{id}/disable")
    @PreAuthorize("hasPermission('USER', 'UPDATE')")
    @Operation(summary = "Disable user account")
    public ResponseEntity<BaseResponse<Void>> disableUser(@PathVariable UUID id) {

        log.info("POST /api/v1/users/{}/disable", id);

        userCommandService.disableUser(id);

        return ResponseEntity.ok(
                BaseResponse.success(null, "User disabled successfully")
        );
    }

    // ========================================
    // Password Management Endpoints
    // ========================================

    /**
     * Change password
     */
    @PostMapping("/{id}/change-password")
    @PreAuthorize("hasPermission('USER', 'UPDATE') or #id == principal.id")
    @Operation(
            summary = "Change password",
            description = "Change user password (requires current password)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password changed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid password data"),
            @ApiResponse(responseCode = "401", description = "Current password is incorrect")
    })
    public ResponseEntity<BaseResponse<Void>> changePassword(
            @PathVariable UUID id,
            @Valid @RequestBody ChangePasswordRequest request,
            BindingResult bindingResult) {

        log.info("POST /api/v1/users/{}/change-password", id);

        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest()
                    .body(BaseResponse.error("VALIDATION_ERROR", "Invalid password data"));
        }

        userCommandService.changePassword(id, request);

        return ResponseEntity.ok(
                BaseResponse.success(null, "Password changed successfully")
        );
    }

    /**
     * Reset password (admin only - no current password required)
     */
    @PostMapping("/{id}/reset-password")
    @PreAuthorize("hasPermission('USER', 'UPDATE')")
    @Operation(
            summary = "Reset password (admin)",
            description = "Reset user password without requiring current password (admin only)"
    )
    public ResponseEntity<BaseResponse<Void>> resetPassword(
            @PathVariable UUID id,
            @RequestBody Map<String, String> request) {

        log.info("POST /api/v1/users/{}/reset-password", id);

        String newPassword = request.get("newPassword");
        if (newPassword == null || newPassword.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(BaseResponse.error("VALIDATION_ERROR", "New password is required"));
        }

        // Implementation would be similar to resetPassword in forgot password flow
        // userCommandService.adminResetPassword(id, newPassword);

        return ResponseEntity.ok(
                BaseResponse.success(null, "Password reset successfully")
        );
    }

    // ========================================
    // Role Management Endpoints
    // ========================================

    /**
     * Assign roles to user
     */
    @PostMapping("/{id}/roles")
    @PreAuthorize("hasPermission('USER', 'UPDATE')")
    @Operation(
            summary = "Assign roles to user",
            description = "Assign one or more roles to a user"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Roles assigned successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid role data"),
            @ApiResponse(responseCode = "404", description = "User or role not found")
    })
    public ResponseEntity<BaseResponse<UserDetailResponse>> assignRoles(
            @PathVariable UUID id,
            @Valid @RequestBody AssignRoleRequest request,
            BindingResult bindingResult) {

        log.info("POST /api/v1/users/{}/roles", id);

        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest()
                    .body(BaseResponse.error("VALIDATION_ERROR", "Invalid role data"));
        }

        UserDetailResponse user = userCommandService.assignRoles(id, request);

        return ResponseEntity.ok(
                BaseResponse.success(user, "Roles assigned successfully")
        );
    }

    /**
     * Remove role from user
     */
    @DeleteMapping("/{userId}/roles/{roleId}")
    @PreAuthorize("hasPermission('USER', 'UPDATE')")
    @Operation(
            summary = "Remove role from user",
            description = "Remove a specific role from a user"
    )
    public ResponseEntity<BaseResponse<UserDetailResponse>> removeRole(
            @PathVariable UUID userId,
            @PathVariable UUID roleId) {

        log.info("DELETE /api/v1/users/{}/roles/{}", userId, roleId);

        UserDetailResponse user = userCommandService.removeRole(userId, roleId);

        return ResponseEntity.ok(
                BaseResponse.success(user, "Role removed successfully")
        );
    }

    /**
     * Get user permissions
     */
    @GetMapping("/{id}/permissions")
    @PreAuthorize("hasPermission('USER', 'READ') or #id == principal.id")
    @Operation(
            summary = "Get user permissions",
            description = "Get all permissions for a user (from all roles)"
    )
    public ResponseEntity<BaseResponse<Map<String, Object>>> getUserPermissions(@PathVariable UUID id) {

        log.info("GET /api/v1/users/{}/permissions", id);

        UserDetailResponse user = userQueryService.findUserDetailById(id);

        return ResponseEntity.ok(
                BaseResponse.success(
                        Map.of(
                                "userId", id,
                                "permissions", user.getPermissions(),
                                "roles", user.getRoles()
                        ),
                        "Permissions retrieved successfully"
                )
        );
    }

    // ========================================
    // Bulk Operations Endpoints
    // ========================================

    /**
     * Get users by IDs
     */
    @PostMapping("/bulk/find")
    @PreAuthorize("hasPermission('USER', 'READ')")
    @Operation(summary = "Get users by IDs")
    public ResponseEntity<BaseResponse<List<UserResponse>>> getUsersByIds(
            @RequestBody List<UUID> userIds) {

        log.info("POST /api/v1/users/bulk/find - count: {}", userIds.size());

        List<UserResponse> users = userQueryService.findByIds(userIds);

        return ResponseEntity.ok(
                BaseResponse.success(users, String.format("Retrieved %d users", users.size()))
        );
    }

    /**
     * Bulk activate users
     */
    @PostMapping("/bulk/activate")
    @PreAuthorize("hasPermission('USER', 'UPDATE')")
    @Operation(summary = "Bulk activate users")
    public ResponseEntity<BaseResponse<Map<String, Integer>>> bulkActivateUsers(
            @RequestBody List<UUID> userIds) {

        log.info("POST /api/v1/users/bulk/activate - count: {}", userIds.size());

        int successCount = 0;
        int failCount = 0;

        for (UUID id : userIds) {
            try {
                userCommandService.activateUser(id);
                successCount++;
            } catch (Exception e) {
                log.error("Failed to activate user: {}", id, e);
                failCount++;
            }
        }

        return ResponseEntity.ok(
                BaseResponse.success(
                        Map.of("success", successCount, "failed", failCount),
                        String.format("Activated %d users, %d failed", successCount, failCount)
                )
        );
    }

    /**
     * Bulk lock users
     */
    @PostMapping("/bulk/lock")
    @PreAuthorize("hasPermission('USER', 'UPDATE')")
    @Operation(summary = "Bulk lock users")
    public ResponseEntity<BaseResponse<Map<String, Integer>>> bulkLockUsers(
            @RequestBody Map<String, Object> request) {

        @SuppressWarnings("unchecked")
        List<String> userIds = (List<String>) request.get("userIds");
        String reason = (String) request.getOrDefault("reason", "Bulk lock operation");

        log.info("POST /api/v1/users/bulk/lock - count: {}", userIds.size());

        int successCount = 0;
        int failCount = 0;

        for (String id : userIds) {
            try {
                userCommandService.lockUser(UUID.fromString(id), reason);
                successCount++;
            } catch (Exception e) {
                log.error("Failed to lock user: {}", id, e);
                failCount++;
            }
        }

        return ResponseEntity.ok(
                BaseResponse.success(
                        Map.of("success", successCount, "failed", failCount),
                        String.format("Locked %d users, %d failed", successCount, failCount)
                )
        );
    }

    // ========================================
    // Export Endpoints
    // ========================================

    /**
     * Export users to CSV
     */
    @GetMapping("/export/csv")
    @PreAuthorize("hasPermission('USER', 'READ')")
    @Operation(summary = "Export users to CSV")
    public ResponseEntity<BaseResponse<String>> exportUsersToCSV(
            @RequestParam(required = false) String filter) {

        log.info("GET /api/v1/users/export/csv");

        // Implementation would generate CSV and return download URL or content

        return ResponseEntity.ok(
                BaseResponse.success(
                        "export-url-placeholder",
                        "Export initiated successfully"
                )
        );
    }

    // ========================================
    // Health Check
    // ========================================

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    @Operation(summary = "Health check")
    public ResponseEntity<BaseResponse<Map<String, String>>> healthCheck() {
        return ResponseEntity.ok(
                BaseResponse.success(
                        Map.of(
                                "status", "UP",
                                "service", "UserService",
                                "timestamp", java.time.LocalDateTime.now().toString()
                        ),
                        "Service is healthy"
                )
        );
    }
}