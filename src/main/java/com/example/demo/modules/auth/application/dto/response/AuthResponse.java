package com.example.demo.modules.auth.application.dto.response;

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

/**
 * Authentication response carrying both tokens and principal info.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Authentication response")
public class AuthResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    // ── Tokens ───────────────────────────────────────────────
    @Schema(description = "JWT access token")
    private String accessToken;

    @Schema(description = "JWT refresh token")
    private String refreshToken;

    @Schema(description = "Token type", example = "Bearer")
    @Builder.Default
    private String tokenType = "Bearer";

    @Schema(description = "Access token expiry in seconds", example = "3600")
    private Long expiresIn;

    @Schema(description = "Access token expiry timestamp")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime expiresAt;

    // ── Principal info ───────────────────────────────────────
    @Schema(description = "Principal ID (UUID)")
    private String subjectId;

    @Schema(description = "Principal type: USER or CUSTOMER", example = "USER")
    private String subjectType;

    @Schema(description = "Email address", example = "john.doe@example.com")
    private String email;

    @Schema(description = "Full name", example = "John Doe")
    private String fullName;

    @Schema(description = "Granted authorities / roles")
    private Set<String> authorities;
}