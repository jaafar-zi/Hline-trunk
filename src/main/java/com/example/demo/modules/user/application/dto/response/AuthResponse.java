package com.example.demo.modules.user.application.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

/**
 * Authentication response DTO
 * Returned after successful login
 *
 * @author Architecture Team
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Authentication response")
public class AuthResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "Access token (JWT)", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String accessToken;

    @Schema(description = "Refresh token", example = "550e8400-e29b-41d4-a716-446655440000")
    private String refreshToken;

    @Schema(description = "Token type", example = "Bearer")
    @Builder.Default
    private String tokenType = "Bearer";

    @Schema(description = "Token expiration time in seconds", example = "3600")
    private Long expiresIn;

    @Schema(description = "Token expiration timestamp")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime expiresAt;

    @Schema(description = "Refresh token expiration time in seconds", example = "604800")
    private Long refreshExpiresIn;

    @Schema(description = "Refresh token expiration timestamp")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime refreshExpiresAt;

    @Schema(description = "User information")
    private UserInfo user;

    @Schema(description = "Session ID")
    private String sessionId;

    @Schema(description = "Two-factor authentication required", example = "false")
    @Builder.Default
    private Boolean requiresTwoFactor = false;

    @Schema(description = "Two-factor authentication methods available")
    private Set<String> twoFactorMethods;

    @Schema(description = "Additional metadata")
    private AuthMetadata metadata;

    /**
     * Nested DTO for user info in auth response
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "User information in auth response")
    public static class UserInfo implements Serializable {

        private static final long serialVersionUID = 1L;

        @Schema(description = "User ID")
        private UUID id;

        @Schema(description = "Email address")
        private String email;

        @Schema(description = "First name")
        private String firstName;

        @Schema(description = "Last name")
        private String lastName;

        @Schema(description = "Full name")
        private String fullName;

        @Schema(description = "Display name")
        private String displayName;

        @Schema(description = "Avatar URL")
        private String avatarUrl;

        @Schema(description = "User roles")
        private Set<String> roles;

        @Schema(description = "User permissions")
        private Set<String> permissions;

        @Schema(description = "Email verified status")
        private Boolean emailVerified;

        @Schema(description = "Account enabled status")
        private Boolean enabled;

        @Schema(description = "Last login timestamp")
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime lastLoginAt;
    }

    /**
     * Nested DTO for auth metadata
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Authentication metadata")
    public static class AuthMetadata implements Serializable {

        private static final long serialVersionUID = 1L;

        @Schema(description = "Login timestamp")
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime loginAt;

        @Schema(description = "Client IP address")
        private String ipAddress;

        @Schema(description = "User agent")
        private String userAgent;

        @Schema(description = "Device ID")
        private String deviceId;

        @Schema(description = "Device name")
        private String deviceName;

        @Schema(description = "Login method", example = "password")
        private String loginMethod;

        @Schema(description = "Location (city, country)")
        private String location;

        @Schema(description = "Is this a new device")
        private Boolean newDevice;

        @Schema(description = "Previous login timestamp")
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime previousLoginAt;
    }

    /**
     * Create successful auth response
     */
    public static AuthResponse success(String accessToken, String refreshToken,
                                       Long expiresIn, UserInfo user) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(expiresIn)
                .expiresAt(LocalDateTime.now().plusSeconds(expiresIn))
                .user(user)
                .requiresTwoFactor(false)
                .build();
    }

    /**
     * Create two-factor required response
     */
    public static AuthResponse requiresTwoFactor(String sessionId, Set<String> methods) {
        return AuthResponse.builder()
                .sessionId(sessionId)
                .requiresTwoFactor(true)
                .twoFactorMethods(methods)
                .build();
    }
}