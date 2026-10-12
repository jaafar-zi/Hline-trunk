package com.example.demo.modules.auth.interfaces;

import com.example.demo.common.base.dto.BaseResponse;
import com.example.demo.config.security.JwtTokenProvider;
import com.example.demo.modules.auth.application.dto.request.*;
import com.example.demo.modules.auth.application.dto.response.AuthResponse;
import com.example.demo.modules.auth.application.dto.response.TokenValidationResponse;
import com.example.demo.modules.auth.application.service.AuthService;
import com.example.demo.modules.auth.application.validator.ChangePasswordRequestValidator;
import com.example.demo.modules.auth.application.validator.LoginRequestValidator;
import com.example.demo.modules.auth.application.validator.ResetPasswordRequestValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

/**
 * REST Controller for Auth operations.
 *
 * Public endpoints (no token required):
 *   POST /api/v1/auth/login
 *   POST /api/v1/auth/refresh
 *   POST /api/v1/auth/forgot-password
 *   POST /api/v1/auth/reset-password
 *
 * Authenticated endpoints:
 *   POST /api/v1/auth/logout
 *   POST /api/v1/auth/change-password
 *   GET  /api/v1/auth/validate
 *   GET  /api/v1/auth/me
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Auth endpoints for User and Customer")
public class AuthController {

    private final AuthService                    authService;
    private final JwtTokenProvider               jwtTokenProvider;
    private final LoginRequestValidator          loginValidator;
    private final ResetPasswordRequestValidator  resetPasswordValidator;
    private final ChangePasswordRequestValidator changePasswordValidator;

    // ====================================
    // Public Endpoints
    // ====================================

    @PostMapping("/login")
    @Operation(summary = "Login – works for both USER and CUSTOMER subject types")
    public ResponseEntity<BaseResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {

        log.info("POST /api/v1/auth/login - {}", request.getEmail());

        runValidator(loginValidator, request, "login");

        AuthResponse response = authService.login(request, extractIp(httpRequest));
        return ResponseEntity.ok(BaseResponse.success(response, "Login successful"));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token using a refresh token")
    public ResponseEntity<BaseResponse<AuthResponse>> refresh(
            @Valid @RequestBody RefreshTokenRequest request,
            HttpServletRequest httpRequest) {

        log.info("POST /api/v1/auth/refresh");
        AuthResponse response = authService.refresh(request, extractIp(httpRequest));
        return ResponseEntity.ok(BaseResponse.success(response, "Token refreshed"));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Request a password reset email")
    public ResponseEntity<BaseResponse<Void>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {

        log.info("POST /api/v1/auth/forgot-password");
        authService.forgotPassword(request);
        // Always return success to prevent email enumeration
        return ResponseEntity.ok(BaseResponse.success(null,
                "If the email exists, a reset link has been sent"));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset password using a reset token")
    public ResponseEntity<BaseResponse<Void>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {

        log.info("POST /api/v1/auth/reset-password");
        runValidator(resetPasswordValidator, request, "resetPassword");

        authService.resetPassword(request);
        return ResponseEntity.ok(BaseResponse.success(null, "Password reset successfully"));
    }

    // ====================================
    // Authenticated Endpoints
    // ====================================

    @PostMapping("/logout")
    @Operation(summary = "Logout and invalidate the current token")
    public ResponseEntity<BaseResponse<Void>> logout(
            HttpServletRequest httpRequest) {

        log.info("POST /api/v1/auth/logout");
        String token = extractBearerToken(httpRequest);
        authService.logout(token, extractIp(httpRequest));
        return ResponseEntity.ok(BaseResponse.success(null, "Logged out successfully"));
    }

    @PostMapping("/change-password")
    @Operation(summary = "Change password (authenticated user)")
    public ResponseEntity<BaseResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            @AuthenticationPrincipal UserDetails currentUser,
            HttpServletRequest httpRequest) {

        log.info("POST /api/v1/auth/change-password");
        runValidator(changePasswordValidator, request, "changePassword");

        // Extract subject type from token
        String token       = extractBearerToken(httpRequest);
        String subjectType = jwtTokenProvider.extractSubjectType(token);

        authService.changePassword(
                currentUser.getUsername(), subjectType, request);

        return ResponseEntity.ok(BaseResponse.success(null,
                "Password changed successfully"));
    }

    @GetMapping("/validate")
    @Operation(summary = "Validate a JWT token")
    public ResponseEntity<BaseResponse<TokenValidationResponse>> validate(
            HttpServletRequest httpRequest) {

        String token = extractBearerToken(httpRequest);
        TokenValidationResponse response = authService.validate(token);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @GetMapping("/me")
    @Operation(summary = "Get current authenticated principal info")
    public ResponseEntity<BaseResponse<TokenValidationResponse>> me(
            @AuthenticationPrincipal UserDetails currentUser,
            HttpServletRequest httpRequest) {

        String token = extractBearerToken(httpRequest);
        TokenValidationResponse response = authService.validate(token);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    // ====================================
    // Private Helpers
    // ====================================

    private <T> void runValidator(org.springframework.validation.Validator validator,
                                   T target,
                                   String objectName) {
        Errors errors = new BeanPropertyBindingResult(target, objectName);
        validator.validate(target, errors);

        if (errors.hasErrors()) {
            String message = errors.getAllErrors().stream()
                    .map(e -> e.getDefaultMessage())
                    .collect(Collectors.joining(", "));
            throw new com.example.demo.common.exception
                    .BusinessException(message);
        }
    }

    private String extractBearerToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }

    private String extractIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank()) ip = request.getHeader("X-Real-IP");
        if (ip == null || ip.isBlank()) ip = request.getRemoteAddr();
        if (ip != null && ip.contains(",")) ip = ip.split(",")[0].trim();
        return ip != null ? ip : "unknown";
    }
}