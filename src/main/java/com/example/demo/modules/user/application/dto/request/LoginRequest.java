package com.example.demo.modules.user.application.dto.request;

import com.example.demo.common.base.dto.BaseRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for user login
 *
 * @author Architecture Team
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "User login request")
public class LoginRequest extends BaseRequest {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Schema(description = "User's email address", example = "john.doe@example.com", required = true)
    private String email;

    @NotBlank(message = "Password is required")
    @Schema(description = "User's password", example = "Password123!", required = true)
    private String password;

    @Schema(description = "Remember me (extends session duration)", example = "false")
    @Builder.Default
    private Boolean rememberMe = false;

    @Schema(description = "Two-factor authentication code (if enabled)", example = "123456")
    private String twoFactorCode;

    @Schema(description = "Device identifier for tracking", example = "mobile-app-android")
    private String deviceId;

    @Schema(description = "Device name/type", example = "Samsung Galaxy S21")
    private String deviceName;

    @Schema(description = "Client IP address (auto-filled by server)")
    private String ipAddress;

    @Schema(description = "User agent (auto-filled by server)")
    private String userAgent;
}