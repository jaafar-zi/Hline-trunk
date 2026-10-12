package com.example.demo.modules.user.application.dto.response;

import com.example.demo.modules.user.domain.model.vo.UserStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

/**
 * User detail response DTO (full view)
 *
 * @author Architecture Team
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "User detailed response")
public class UserDetailResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "User ID")
    private UUID id;

    @Schema(description = "First name")
    private String firstName;

    @Schema(description = "Last name")
    private String lastName;

    @Schema(description = "Full name")
    private String fullName;

    @Schema(description = "Email address")
    private String email;

    @Schema(description = "Display name")
    private String displayName;

    @Schema(description = "Avatar URL")
    private String avatarUrl;

    @Schema(description = "Bio/description")
    private String bio;

    @Schema(description = "Date of birth")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateOfBirth;

    @Schema(description = "Phone number")
    private String phoneNumber;

    @Schema(description = "Timezone")
    private String timezone;

    @Schema(description = "Language")
    private String language;

    @Schema(description = "Country")
    private String country;

    // Account Status
    @Schema(description = "Is account enabled")
    private Boolean enabled;

    @Schema(description = "Is account locked")
    private Boolean accountLocked;

    @Schema(description = "Is email verified")
    private Boolean emailVerified;

    @Schema(description = "User status")
    private UserStatus status;

    @Schema(description = "Account expires at")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime accountExpiresAt;

    @Schema(description = "Password expires at")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime passwordExpiresAt;

    @Schema(description = "Locked until (for temporary locks)")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime lockedUntil;

    @Schema(description = "Failed login attempts")
    private Integer failedLoginAttempts;

    @Schema(description = "Last login timestamp")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime lastLoginAt;

    // Roles & Permissions
    @Schema(description = "User roles")
    private Set<RoleResponse> roles;

    @Schema(description = "All permissions (from all roles)")
    private Set<String> permissions;

    @Schema(description = "User group")
    private UserGroupResponse userGroup;

    // Audit
    @Schema(description = "Created timestamp")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @Schema(description = "Created by")
    private String createdBy;

    @Schema(description = "Updated timestamp")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    @Schema(description = "Updated by")
    private String updatedBy;

    @Schema(description = "Entity version (for optimistic locking)")
    private Long version;

    // Nested DTOs
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Role response")
    public static class RoleResponse implements Serializable {
        private UUID id;
        private String name;
        private String displayName;
        private String description;
        private Integer priority;
        private Boolean systemRole;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "User group response")
    public static class UserGroupResponse implements Serializable {
        private UUID id;
        private String name;
        private String code;
        private String description;
        private Boolean active;
    }
}