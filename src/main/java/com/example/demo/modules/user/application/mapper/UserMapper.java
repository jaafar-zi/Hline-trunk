package com.example.demo.modules.user.application.mapper;

import com.example.demo.common.mapper.BaseMapper;
import com.example.demo.modules.user.application.dto.request.CreateUserRequest;
import com.example.demo.modules.user.application.dto.request.UpdateUserRequest;
import com.example.demo.modules.user.application.dto.request.UpdateUserProfileRequest;
import com.example.demo.modules.user.application.dto.response.UserDetailResponse;
import com.example.demo.modules.user.application.dto.response.UserResponse;
import com.example.demo.modules.user.domain.model.Permission;
import com.example.demo.modules.user.domain.model.Role;
import com.example.demo.modules.user.domain.model.User;
import com.example.demo.modules.user.domain.model.UserGroup;
import com.example.demo.modules.user.domain.model.vo.Email;
import com.example.demo.modules.user.domain.model.vo.UserProfile;
import org.mapstruct.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * MapStruct mapper for User entity <-> DTOs
 *
 * @author Architecture Team
 * @version 1.0
 */
@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
public abstract class UserMapper implements BaseMapper<User, UserResponse> {

    // ====================================
    // Entity to Response Mappings
    // ====================================

    /**
     * Map User entity to UserResponse (summary view)
     * Implementation of BaseMapper.toDto()
     */
    @Override
    @Mapping(target = "fullName", expression = "java(user.getFullName())")
    @Mapping(target = "email", expression = "java(extractEmailValue(user.getEmail()))")
    @Mapping(target = "displayName", source = "profile.displayName")
    @Mapping(target = "avatarUrl", source = "profile.avatarUrl")
    @Mapping(target = "phoneNumber", source = "profile.phoneNumber")
    @Mapping(target = "status", expression = "java(user.getStatus())")
    @Mapping(target = "roles", expression = "java(extractRoleNames(user.getRoles()))")
    @Mapping(target = "userGroupName", expression = "java(extractUserGroupName(user.getUserGroup()))")
    public abstract UserResponse toDto(User user);

    /**
     * Alias for toDto() - for better readability in services
     * Maps User entity to UserResponse (summary view)
     */
    public UserResponse toResponse(User user) {
        return toDto(user);
    }

    /**
     * Map list of User entities to list of UserResponses
     * Convenience method for query services
     */
    public List<UserResponse> toResponseList(List<User> users) {
        return toDtoList(users);
    }

    /**
     * Map User entity to UserDetailResponse (full view)
     */
    @Mapping(target = "fullName", expression = "java(user.getFullName())")
    @Mapping(target = "email", expression = "java(extractEmailValue(user.getEmail()))")
    @Mapping(target = "displayName", source = "profile.displayName")
    @Mapping(target = "avatarUrl", source = "profile.avatarUrl")
    @Mapping(target = "bio", source = "profile.bio")
    @Mapping(target = "phoneNumber", source = "profile.phoneNumber")
    @Mapping(target = "timezone", source = "profile.timezone")
    @Mapping(target = "language", source = "profile.language")
    @Mapping(target = "country", source = "profile.country")
    @Mapping(target = "status", expression = "java(user.getStatus())")
    @Mapping(target = "roles", expression = "java(mapRolesToDetailResponse(user.getRoles()))")
    @Mapping(target = "permissions", expression = "java(extractPermissionNames(user.getAllPermissions()))")
    @Mapping(target = "userGroup", expression = "java(mapUserGroupToResponse(user.getUserGroup()))")
    public abstract UserDetailResponse toDetailResponse(User user);

    /**
     * Map list of User entities to list of UserDetailResponses
     */
    public List<UserDetailResponse> toDetailResponseList(List<User> users) {
        if (users == null || users.isEmpty()) {
            return Collections.emptyList();
        }
        return users.stream()
                .map(this::toDetailResponse)
                .collect(Collectors.toList());
    }

    // ====================================
    // Request to Entity Mappings
    // ====================================

    /**
     * Map CreateUserRequest to User entity
     */
    @Mapping(target = "email", expression = "java(createEmail(request.getEmail()))")
    @Mapping(target = "profile", expression = "java(createProfile(request))")
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "userGroup", ignore = true)
    @Mapping(target = "enabled", constant = "false")
    @Mapping(target = "emailVerified", constant = "false")
    @Mapping(target = "accountLocked", constant = "false")
    @Mapping(target = "failedLoginAttempts", constant = "0")
    @Mapping(target = "lastLoginAt", ignore = true)
    @Mapping(target = "passwordExpiresAt", ignore = true)
    @Mapping(target = "accountExpiresAt", ignore = true)
    @Mapping(target = "lockedUntil", ignore = true)
    @Mapping(target = "emailVerificationToken", ignore = true)
    public abstract User fromCreateRequest(CreateUserRequest request);

    /**
     * Implementation of BaseMapper.toEntity(UserResponse)
     * This method is not supported - cannot create entity from response DTO
     */
    @Override
    public User toEntity(UserResponse dto) {
        throw new UnsupportedOperationException("Cannot create entity from response DTO. Use fromCreateRequest() instead.");
    }

    // ====================================
    // Update Mappings
    // ====================================

    /**
     * Update existing User entity from UpdateUserRequest (PATCH)
     * Implementation of BaseMapper.updateEntityFromDto
     */
    @BeanMapping(
            nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
            nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS
    )
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "email", expression = "java(updateEmail(dto.getEmail(), entity.getEmail()))")
    @Mapping(target = "profile", expression = "java(updateUserProfile(dto, entity.getProfile()))")
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "userGroup", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "lastLoginAt", ignore = true)
    @Mapping(target = "emailVerificationToken", ignore = true)
    @Mapping(target = "emailVerified", ignore = true)
    @Mapping(target = "accountLocked", ignore = true)
    @Mapping(target = "failedLoginAttempts", ignore = true)
    @Mapping(target = "passwordExpiresAt", ignore = true)
    @Mapping(target = "accountExpiresAt", ignore = true)
    @Mapping(target = "lockedUntil", ignore = true)
    public abstract User updateEntityFromDto(UpdateUserRequest dto, @MappingTarget User entity);

    /**
     * Update user profile (self-service)
     */
    @BeanMapping(
            nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
            nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS
    )
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "profile", expression = "java(updateProfileOnly(request, user.getProfile()))")
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "userGroup", ignore = true)
    @Mapping(target = "enabled", ignore = true)
    @Mapping(target = "accountLocked", ignore = true)
    @Mapping(target = "emailVerified", ignore = true)
    @Mapping(target = "failedLoginAttempts", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "lastLoginAt", ignore = true)
    @Mapping(target = "passwordExpiresAt", ignore = true)
    @Mapping(target = "accountExpiresAt", ignore = true)
    @Mapping(target = "lockedUntil", ignore = true)
    @Mapping(target = "emailVerificationToken", ignore = true)
    public abstract User updateProfileFromRequest(UpdateUserProfileRequest request, @MappingTarget User user);

    // ====================================
    // Public Profile Mapping Methods
    // ====================================

    /**
     * Map CreateUserRequest to UserProfile
     */
    public UserProfile toUserProfile(CreateUserRequest request) {
        if (request == null) {
            return UserProfile.createDefault();
        }

        return UserProfile.builder()
                .displayName(request.getDisplayName())
                .phoneNumber(request.getPhoneNumber())
                .bio(request.getBio())
                .timezone(Optional.ofNullable(request.getTimezone()).orElse("UTC"))
                .language(Optional.ofNullable(request.getLanguage()).orElse("en"))
                .country(request.getCountry())
                .build();
    }

    /**
     * Map UpdateUserProfileRequest to UserProfile
     */
    public UserProfile toUserProfile(UpdateUserProfileRequest request) {
        if (request == null) {
            return UserProfile.createDefault();
        }

        return UserProfile.builder()
                .displayName(request.getDisplayName())
                .phoneNumber(request.getPhoneNumber())
                .bio(request.getBio())
                .avatarUrl(request.getAvatarUrl())
                .timezone(request.getTimezone())
                .language(request.getLanguage())
                .country(request.getCountry())
                .build();
    }

    // ====================================
    // Email Mapping Helpers
    // ====================================

    protected String extractEmailValue(Email email) {
        return email != null ? email.getValue() : null;
    }

    protected Email createEmail(String emailStr) {
        return emailStr != null && !emailStr.isBlank() ? Email.of(emailStr) : null;
    }

    protected Email updateEmail(String emailStr, Email currentEmail) {
        return (emailStr != null && !emailStr.isBlank())
                ? Email.of(emailStr)
                : currentEmail;
    }

    // ====================================
    // Profile Mapping Helpers
    // ====================================

    protected UserProfile createProfile(CreateUserRequest request) {
        if (request == null) {
            return UserProfile.createDefault();
        }

        return UserProfile.builder()
                .displayName(request.getDisplayName())
                .phoneNumber(request.getPhoneNumber())
                .bio(request.getBio())
                .timezone(Optional.ofNullable(request.getTimezone()).orElse("UTC"))
                .language(Optional.ofNullable(request.getLanguage()).orElse("en"))
                .country(request.getCountry())
                .build();
    }

    protected UserProfile updateUserProfile(UpdateUserRequest request, UserProfile existing) {
        if (request == null) {
            return existing;
        }

        UserProfile base = Optional.ofNullable(existing).orElseGet(UserProfile::createDefault);

        return UserProfile.builder()
                .displayName(Optional.ofNullable(request.getDisplayName()).orElse(base.getDisplayName()))
                .phoneNumber(Optional.ofNullable(request.getPhoneNumber()).orElse(base.getPhoneNumber()))
                .avatarUrl(Optional.ofNullable(request.getAvatarUrl()).orElse(base.getAvatarUrl()))
                .bio(Optional.ofNullable(request.getBio()).orElse(base.getBio()))
                .timezone(Optional.ofNullable(request.getTimezone()).orElse(base.getTimezone()))
                .language(Optional.ofNullable(request.getLanguage()).orElse(base.getLanguage()))
                .country(Optional.ofNullable(request.getCountry()).orElse(base.getCountry()))
                .build();
    }

    protected UserProfile updateProfileOnly(UpdateUserProfileRequest request, UserProfile existing) {
        if (request == null) {
            return existing;
        }

        UserProfile base = Optional.ofNullable(existing).orElseGet(UserProfile::createDefault);

        return UserProfile.builder()
                .displayName(Optional.ofNullable(request.getDisplayName()).orElse(base.getDisplayName()))
                .phoneNumber(Optional.ofNullable(request.getPhoneNumber()).orElse(base.getPhoneNumber()))
                .avatarUrl(Optional.ofNullable(request.getAvatarUrl()).orElse(base.getAvatarUrl()))
                .bio(Optional.ofNullable(request.getBio()).orElse(base.getBio()))
                .timezone(Optional.ofNullable(request.getTimezone()).orElse(base.getTimezone()))
                .language(Optional.ofNullable(request.getLanguage()).orElse(base.getLanguage()))
                .country(Optional.ofNullable(request.getCountry()).orElse(base.getCountry()))
                .build();
    }

    // ====================================
    // Role Mapping Helpers
    // ====================================

    protected Set<String> extractRoleNames(Set<Role> roles) {
        if (roles == null || roles.isEmpty()) {
            return Collections.emptySet();
        }

        return roles.stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
    }

    protected Set<UserDetailResponse.RoleResponse> mapRolesToDetailResponse(Set<Role> roles) {
        if (roles == null || roles.isEmpty()) {
            return Collections.emptySet();
        }

        return roles.stream()
                .map(this::toRoleResponse)
                .collect(Collectors.toSet());
    }

    private UserDetailResponse.RoleResponse toRoleResponse(Role role) {
        if (role == null) {
            return null;
        }

        return UserDetailResponse.RoleResponse.builder()
                .id(role.getId())
                .name(role.getName())
                .displayName(role.getDisplayName())
                .description(role.getDescription())
                .priority(role.getPriority())
                .systemRole(role.getSystemRole())
                .build();
    }

    // ====================================
    // Permission Mapping Helpers
    // ====================================

    protected Set<String> extractPermissionNames(Set<Permission> permissions) {
        if (permissions == null || permissions.isEmpty()) {
            return Collections.emptySet();
        }

        return permissions.stream()
                .map(Permission::getName)
                .collect(Collectors.toSet());
    }

    // ====================================
    // UserGroup Mapping Helpers
    // ====================================

    protected String extractUserGroupName(UserGroup userGroup) {
        return userGroup != null ? userGroup.getName() : null;
    }

    protected UserDetailResponse.UserGroupResponse mapUserGroupToResponse(UserGroup userGroup) {
        if (userGroup == null) {
            return null;
        }

        return UserDetailResponse.UserGroupResponse.builder()
                .id(userGroup.getId())
                .name(userGroup.getName())
                .code(userGroup.getCode())
                .description(userGroup.getDescription())
                .active(userGroup.getActive())
                .build();
    }
}