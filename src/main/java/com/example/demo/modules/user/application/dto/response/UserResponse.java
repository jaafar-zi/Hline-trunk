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
 * User response DTO (summary view)
 *
 * @author Architecture Team
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "User summary response")
public class UserResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "User ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @Schema(description = "First name", example = "John")
    private String firstName;

    @Schema(description = "Last name", example = "Doe")
    private String lastName;

    @Schema(description = "Full name", example = "John Doe")
    private String fullName;

    @Schema(description = "Email address", example = "john.doe@example.com")
    private String email;

    @Schema(description = "Display name", example = "Johnny")
    private String displayName;

    @Schema(description = "Avatar URL")
    private String avatarUrl;

    @Schema(description = "Date of birth", example = "1990-01-15")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateOfBirth;

    @Schema(description = "Phone number", example = "+1234567890")
    private String phoneNumber;

    @Schema(description = "Is account enabled", example = "true")
    private Boolean enabled;

    @Schema(description = "Is account locked", example = "false")
    private Boolean accountLocked;

    @Schema(description = "Is email verified", example = "true")
    private Boolean emailVerified;

    @Schema(description = "User status")
    private UserStatus status;

    @Schema(description = "User roles")
    private Set<String> roles;

    @Schema(description = "User group name")
    private String userGroupName;

    @Schema(description = "Last login timestamp")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime lastLoginAt;

    @Schema(description = "Created timestamp")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @Schema(description = "Last updated timestamp")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
}