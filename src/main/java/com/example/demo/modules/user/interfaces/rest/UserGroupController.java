package com.example.demo.modules.user.interfaces.rest;

import com.example.demo.common.base.dto.BaseResponse;
import com.example.demo.common.base.dto.PageResponse;
import com.example.demo.modules.user.application.command.UserGroupCommandService;
import com.example.demo.modules.user.application.dto.request.AssignRolesRequest;
import com.example.demo.modules.user.application.dto.request.CreateUserGroupRequest;
import com.example.demo.modules.user.application.dto.request.UpdateUserGroupRequest;
import com.example.demo.modules.user.application.dto.response.UserGroupResponse;
import com.example.demo.modules.user.application.query.UserGroupQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * UserGroup REST Controller
 *
 * @author Architecture Team
 * @version 1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/user-groups")
@RequiredArgsConstructor
@Tag(name = "User Groups", description = "User group management APIs")
public class UserGroupController {

    private final UserGroupCommandService userGroupCommandService;
    private final UserGroupQueryService userGroupQueryService;

    @GetMapping
    @PreAuthorize("hasPermission('USER_GROUP', 'READ')")
    @Operation(summary = "Get all user groups")
    public ResponseEntity<BaseResponse<PageResponse<UserGroupResponse>>> getAllUserGroups(
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {

        log.info("GET /api/v1/user-groups");

        PageResponse<UserGroupResponse> groups = userGroupQueryService.getAllUserGroups(pageable);

        return ResponseEntity.ok(
                BaseResponse.success(groups, "User groups retrieved successfully")
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasPermission('USER_GROUP', 'READ')")
    @Operation(summary = "Get user group by ID")
    public ResponseEntity<BaseResponse<UserGroupResponse>> getUserGroupById(@PathVariable UUID id) {

        log.info("GET /api/v1/user-groups/{}", id);

        UserGroupResponse group = userGroupQueryService.findUserGroupById(id);

        return ResponseEntity.ok(
                BaseResponse.success(group, "User group retrieved successfully")
        );
    }

    @GetMapping("/code/{code}")
    @PreAuthorize("hasPermission('USER_GROUP', 'READ')")
    @Operation(summary = "Get user group by code")
    public ResponseEntity<BaseResponse<UserGroupResponse>> getUserGroupByCode(@PathVariable String code) {

        log.info("GET /api/v1/user-groups/code/{}", code);

        UserGroupResponse group = userGroupQueryService.findByCode(code);

        return ResponseEntity.ok(
                BaseResponse.success(group, "User group retrieved successfully")
        );
    }

    @GetMapping("/active")
    @PreAuthorize("hasPermission('USER_GROUP', 'READ')")
    @Operation(summary = "Get all active user groups")
    public ResponseEntity<BaseResponse<List<UserGroupResponse>>> getActiveUserGroups() {

        log.info("GET /api/v1/user-groups/active");

        List<UserGroupResponse> groups = userGroupQueryService.getActiveUserGroups();

        return ResponseEntity.ok(
                BaseResponse.success(groups, "Active user groups retrieved successfully")
        );
    }

    @GetMapping("/root")
    @PreAuthorize("hasPermission('USER_GROUP', 'READ')")
    @Operation(summary = "Get root user groups")
    public ResponseEntity<BaseResponse<List<UserGroupResponse>>> getRootUserGroups() {

        log.info("GET /api/v1/user-groups/root");

        List<UserGroupResponse> groups = userGroupQueryService.getRootUserGroups();

        return ResponseEntity.ok(
                BaseResponse.success(groups, "Root user groups retrieved successfully")
        );
    }

    @GetMapping("/hierarchy")
    @PreAuthorize("hasPermission('USER_GROUP', 'READ')")
    @Operation(summary = "Get user group hierarchy")
    public ResponseEntity<BaseResponse<List<UserGroupQueryService.UserGroupHierarchyResponse>>> getUserGroupHierarchy() {

        log.info("GET /api/v1/user-groups/hierarchy");

        List<UserGroupQueryService.UserGroupHierarchyResponse> hierarchy = userGroupQueryService.getUserGroupHierarchy();

        return ResponseEntity.ok(
                BaseResponse.success(hierarchy, "User group hierarchy retrieved successfully")
        );
    }

    @GetMapping("/{id}/children")
    @PreAuthorize("hasPermission('USER_GROUP', 'READ')")
    @Operation(summary = "Get child groups")
    public ResponseEntity<BaseResponse<List<UserGroupResponse>>> getChildGroups(@PathVariable UUID id) {

        log.info("GET /api/v1/user-groups/{}/children", id);

        List<UserGroupResponse> groups = userGroupQueryService.getChildGroups(id);

        return ResponseEntity.ok(
                BaseResponse.success(groups, "Child groups retrieved successfully")
        );
    }

    @GetMapping("/{id}/user-count")
    @PreAuthorize("hasPermission('USER_GROUP', 'READ')")
    @Operation(summary = "Get user count in group")
    public ResponseEntity<BaseResponse<Map<String, Long>>> getUserCount(@PathVariable UUID id) {

        log.info("GET /api/v1/user-groups/{}/user-count", id);

        long totalUsers = userGroupQueryService.countUsersInGroup(id);
        long activeUsers = userGroupQueryService.countActiveUsersInGroup(id);

        Map<String, Long> counts = new HashMap<>();
        counts.put("totalUsers", totalUsers);
        counts.put("activeUsers", activeUsers);

        return ResponseEntity.ok(
                BaseResponse.success(counts, "User counts retrieved successfully")
        );
    }

    @PostMapping
    @PreAuthorize("hasPermission('USER_GROUP', 'CREATE')")
    @Operation(summary = "Create user group")
    public ResponseEntity<BaseResponse<UserGroupResponse>> createUserGroup(
            @Valid @RequestBody CreateUserGroupRequest request) {

        log.info("POST /api/v1/user-groups - name: {}", request.getName());

        UserGroupResponse group = userGroupCommandService.createUserGroup(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(BaseResponse.success(group, "User group created successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasPermission('USER_GROUP', 'UPDATE')")
    @Operation(summary = "Update user group")
    public ResponseEntity<BaseResponse<UserGroupResponse>> updateUserGroup(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserGroupRequest request) {

        log.info("PUT /api/v1/user-groups/{}", id);

        UserGroupResponse group = userGroupCommandService.updateUserGroup(id, request);

        return ResponseEntity.ok(
                BaseResponse.success(group, "User group updated successfully")
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasPermission('USER_GROUP', 'DELETE')")
    @Operation(summary = "Delete user group")
    public ResponseEntity<BaseResponse<Void>> deleteUserGroup(@PathVariable UUID id) {

        log.info("DELETE /api/v1/user-groups/{}", id);

        userGroupCommandService.deleteUserGroup(id);

        return ResponseEntity.ok(
                BaseResponse.success(null, "User group deleted successfully")
        );
    }

    @PostMapping("/{id}/activate")
    @PreAuthorize("hasPermission('USER_GROUP', 'UPDATE')")
    @Operation(summary = "Activate user group")
    public ResponseEntity<BaseResponse<UserGroupResponse>> activateUserGroup(@PathVariable UUID id) {

        log.info("POST /api/v1/user-groups/{}/activate", id);

        UserGroupResponse group = userGroupCommandService.activateUserGroup(id);

        return ResponseEntity.ok(
                BaseResponse.success(group, "User group activated successfully")
        );
    }

    @PostMapping("/{id}/deactivate")
    @PreAuthorize("hasPermission('USER_GROUP', 'UPDATE')")
    @Operation(summary = "Deactivate user group")
    public ResponseEntity<BaseResponse<UserGroupResponse>> deactivateUserGroup(@PathVariable UUID id) {

        log.info("POST /api/v1/user-groups/{}/deactivate", id);

        UserGroupResponse group = userGroupCommandService.deactivateUserGroup(id);

        return ResponseEntity.ok(
                BaseResponse.success(group, "User group deactivated successfully")
        );
    }

    @PostMapping("/{id}/roles")
    @PreAuthorize("hasPermission('USER_GROUP', 'UPDATE')")
    @Operation(summary = "Assign roles to user group")
    public ResponseEntity<BaseResponse<UserGroupResponse>> assignRoles(
            @PathVariable UUID id,
            @Valid @RequestBody AssignRolesRequest request) {

        log.info("POST /api/v1/user-groups/{}/roles", id);

        UserGroupResponse group = userGroupCommandService.assignRoles(id, request.getRoleIds());

        return ResponseEntity.ok(
                BaseResponse.success(group, "Roles assigned successfully")
        );
    }

    @DeleteMapping("/{groupId}/roles/{roleId}")
    @PreAuthorize("hasPermission('USER_GROUP', 'UPDATE')")
    @Operation(summary = "Remove role from user group")
    public ResponseEntity<BaseResponse<UserGroupResponse>> removeRole(
            @PathVariable UUID groupId,
            @PathVariable UUID roleId) {

        log.info("DELETE /api/v1/user-groups/{}/roles/{}", groupId, roleId);

        UserGroupResponse group = userGroupCommandService.removeRole(groupId, roleId);

        return ResponseEntity.ok(
                BaseResponse.success(group, "Role removed successfully")
        );
    }
}