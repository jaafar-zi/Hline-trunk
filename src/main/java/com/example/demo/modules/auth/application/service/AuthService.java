package com.example.demo.modules.auth.application.service;

import com.example.demo.common.exception.BusinessException;
import com.example.demo.common.exception.EntityNotFoundException;
import com.example.demo.config.security.CustomUserDetailsService;
import com.example.demo.config.security.JwtTokenProvider;
import com.example.demo.modules.auth.application.dto.request.*;
import com.example.demo.modules.auth.application.dto.response.AuthResponse;
import com.example.demo.modules.auth.application.dto.response.TokenValidationResponse;
import com.example.demo.modules.auth.domain.event.*;
import com.example.demo.modules.customer.domain.model.Customer;
import com.example.demo.modules.customer.domain.repository.CustomerRepository;
import com.example.demo.modules.user.domain.event.*;
import com.example.demo.modules.user.domain.model.User;
import com.example.demo.modules.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Core Auth Service
 *
 * Handles login, logout, token refresh, password reset
 * and password change for both User and Customer principals.
 *
 * Uses existing JwtTokenProvider and CustomUserDetailsService.
 *
 * @author Auth Domain Team
 * @version 1.0
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AuthService {

    private static final int MAX_FAILED_ATTEMPTS = 5;

    private final UserRepository           userRepository;
    private final CustomerRepository       customerRepository;
    private final PasswordEncoder          passwordEncoder;
    private final JwtTokenProvider         jwtTokenProvider;
    private final CustomUserDetailsService userDetailsService;
    private final AuthPrincipalService     principalService;
    private final ApplicationEventPublisher eventPublisher;

    // ====================================
    // Login
    // ====================================

    public AuthResponse login(LoginRequest request, String ipAddress) {
        request.normalize();

        return switch (request.getSubjectType()) {
            case "CUSTOMER" -> loginCustomer(
                    request.getEmail(), request.getPassword(), ipAddress);
            default         -> loginUser(
                    request.getEmail(), request.getPassword(), ipAddress);
        };
    }

    // ── User Login ────────────────────────────────────────────────────────────

    private AuthResponse loginUser(String email,
                                    String password,
                                    String ipAddress) {
        log.info("User login attempt: {}", email);

        User user = userRepository.findByEmailWithRoles(email)
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        validateUserCanLogin(user);

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            user.recordFailedLogin(MAX_FAILED_ATTEMPTS);
            userRepository.save(user);
            publishDomainEvents(user);
            throw new BadCredentialsException("Invalid credentials");
        }

        user.recordSuccessfulLogin();
        userRepository.save(user);
        publishDomainEvents(user);

        String subjectId    = user.getId().toString();
        String accessToken  = jwtTokenProvider.generateToken(user, subjectId, "USER");
        String refreshToken = jwtTokenProvider.generateRefreshToken(user, subjectId, "USER");

        publishAuthEvent(subjectId, "USER", email, ipAddress, "LOGIN");

        log.info("User logged in: {}", email);
        return buildAuthResponse(user, subjectId, "USER", accessToken, refreshToken);
    }

    // ── Customer Login ────────────────────────────────────────────────────────

    private AuthResponse loginCustomer(String email,
                                        String password,
                                        String ipAddress) {
        log.info("Customer login attempt: {}", email);

        // Customers authenticate via their linked User record (same email)
        User user = userRepository.findByEmailWithRoles(email)
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        // Ensure the User has ROLE_CUSTOMER
        boolean isCustomer = user.getAllRoles().stream()
                .anyMatch(r -> r.getName().contains("CUSTOMER"));

        if (!isCustomer) {
            throw new BadCredentialsException("Invalid credentials");
        }

        validateUserCanLogin(user);

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            user.recordFailedLogin(MAX_FAILED_ATTEMPTS);
            userRepository.save(user);
            publishDomainEvents(user);
            throw new BadCredentialsException("Invalid credentials");
        }

        // Load Customer aggregate for subject ID
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        if (!customer.canLogin()) {
            throw new BusinessException("Customer account cannot login");
        }

        user.recordSuccessfulLogin();
        userRepository.save(user);
        publishDomainEvents(user);

        String subjectId    = customer.getId().toString();
        String accessToken  = jwtTokenProvider.generateToken(
                user, subjectId, "CUSTOMER");
        String refreshToken = jwtTokenProvider.generateRefreshToken(
                user, subjectId, "CUSTOMER");

        publishAuthEvent(subjectId, "CUSTOMER", email, ipAddress, "LOGIN");

        log.info("Customer logged in: {}", email);
        return buildAuthResponse(user, subjectId, "CUSTOMER", accessToken, refreshToken);
    }

    // ====================================
    // Token Refresh
    // ====================================

    public AuthResponse refresh(RefreshTokenRequest request, String ipAddress) {
        String refreshToken = request.getRefreshToken();

        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new BusinessException("Invalid or expired refresh token");
        }

        String email       = jwtTokenProvider.extractUsername(refreshToken);
        String subjectType = jwtTokenProvider.extractSubjectType(refreshToken);
        String subjectId   = jwtTokenProvider.extractSubjectId(refreshToken);

        UserDetails principal = principalService.loadPrincipal(email, subjectType);

        String newAccessToken  = jwtTokenProvider.generateToken(
                principal, subjectId, subjectType);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(
                principal, subjectId, subjectType);

        publishAuthEvent(subjectId, subjectType, email, ipAddress, "REFRESH");

        log.info("Token refreshed for: {} ({})", email, subjectType);
        return buildRefreshResponse(
                email, subjectId, subjectType,
                principal, newAccessToken, newRefreshToken);
    }

    // ====================================
    // Logout
    // ====================================

    public void logout(String token, String ipAddress) {
        if (token == null || !jwtTokenProvider.validateToken(token)) {
            return;
        }

        String email       = jwtTokenProvider.extractUsername(token);
        String subjectId   = jwtTokenProvider.extractSubjectId(token);
        String subjectType = jwtTokenProvider.extractSubjectType(token);

        publishAuthEvent(subjectId, subjectType, email, ipAddress, "LOGOUT");

        userRepository.findByEmail(email).ifPresent(user ->
                eventPublisher.publishEvent(UserLogoutEvent.builder()
                        .userId(user.getId())
                        .email(email)
                        .logoutType("explicit")
                        .forced(false)
                        .terminateAllSessions(false)
                        .source("demo-app")
                        .build()));

        log.info("Logout for: {} ({})", email, subjectType);
    }

    // ====================================
    // Token Validation
    // ====================================

    @Transactional(readOnly = true)
    public TokenValidationResponse validate(String token) {
        if (token == null || !jwtTokenProvider.validateToken(token)) {
            return TokenValidationResponse.builder()
                    .valid(false)
                    .reason(jwtTokenProvider.getValidationFailureReason(token))
                    .build();
        }

        String email       = jwtTokenProvider.extractUsername(token);
        String subjectType = jwtTokenProvider.extractSubjectType(token);
        String subjectId   = jwtTokenProvider.extractSubjectId(token);

        Set<String> authorities = jwtTokenProvider
                .extractClaim(token, claims -> {
                    Object roles = claims.get("roles");
                    if (roles instanceof java.util.List<?> list) {
                        return list.stream()
                                .map(Object::toString)
                                .collect(Collectors.toSet());
                    }
                    return Set.<String>of();
                });

        return TokenValidationResponse.builder()
                .valid(true)
                .subjectId(subjectId)
                .subjectType(subjectType)
                .email(email)
                .authorities(authorities)
                .build();
    }

    // ====================================
    // Forgot Password
    // ====================================

    public void forgotPassword(ForgotPasswordRequest request) {
        request.normalize();

        userRepository.findByEmail(request.getEmail()).ifPresent(user -> {
            String token = UUID.randomUUID().toString();
            user.setEmailVerificationToken(token);
            userRepository.save(user);

            // Notification service listens to this event and sends email
            eventPublisher.publishEvent(UserPasswordChangedEvent.builder()
                    .userId(user.getId())
                    .email(request.getEmail())
                    .changedBy("system")
                    .selfService(true)
                    .source("demo-app")
                    .build());

            log.info("Password reset token generated for: {}", request.getEmail());
        });

        // Always return success (prevents email enumeration)
    }

    // ====================================
    // Reset Password
    // ====================================

    public void resetPassword(ResetPasswordRequest request) {
        request.validate();

        User user = userRepository.findByEmailVerificationToken(request.getToken())
                .orElseThrow(() -> new BusinessException(
                        "Invalid or expired reset token"));

        user.changePassword(passwordEncoder.encode(request.getNewPassword()));
        user.setEmailVerificationToken(null);

        userRepository.save(user);
        publishDomainEvents(user);

        log.info("Password reset completed for user: {}", user.getId());
    }

    // ====================================
    // Change Password (authenticated)
    // ====================================

    public void changePassword(String email,
                                String subjectType,
                                ChangePasswordRequest request) {
        request.validate();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException(
                        "User not found with email: " + email));

        if (!passwordEncoder.matches(
                request.getCurrentPassword(), user.getPasswordHash())) {
            throw new BusinessException("Current password is incorrect");
        }

        user.changePassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        publishDomainEvents(user);

        // Force logout all sessions
        eventPublisher.publishEvent(UserLogoutEvent.builder()
                .userId(user.getId())
                .email(email)
                .logoutType("password_changed")
                .forced(true)
                .terminateAllSessions(true)
                .source("demo-app")
                .build());

        log.info("Password changed for: {} ({})", email, subjectType);
    }

    // ====================================
    // Private Helpers
    // ====================================

    private void validateUserCanLogin(User user) {
        if (!user.isEnabled()) {
            throw new BusinessException("Account is not enabled");
        }
        if (!user.isAccountNonLocked()) {
            throw new BusinessException("Account is locked");
        }
        if (!user.isAccountNonExpired()) {
            throw new BusinessException("Account has expired");
        }
        if (!user.isCredentialsNonExpired()) {
            throw new BusinessException(
                    "Credentials expired. Please reset your password");
        }
    }

    private AuthResponse buildAuthResponse(User user,
                                            String subjectId,
                                            String subjectType,
                                            String accessToken,
                                            String refreshToken) {
        Set<String> authorities = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getAccessTokenExpirationSeconds())
                .expiresAt(LocalDateTime.now().plusSeconds(
                        jwtTokenProvider.getAccessTokenExpirationSeconds()))
                .subjectId(subjectId)
                .subjectType(subjectType)
                .email(user.getEmail().getValue())
                .fullName(user.getFullName())
                .authorities(authorities)
                .build();
    }

    private AuthResponse buildRefreshResponse(String email,
                                               String subjectId,
                                               String subjectType,
                                               UserDetails principal,
                                               String accessToken,
                                               String refreshToken) {
        Set<String> authorities = principal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getAccessTokenExpirationSeconds())
                .expiresAt(LocalDateTime.now().plusSeconds(
                        jwtTokenProvider.getAccessTokenExpirationSeconds()))
                .subjectId(subjectId)
                .subjectType(subjectType)
                .email(email)
                .authorities(authorities)
                .build();
    }

    private void publishDomainEvents(User user) {
        user.getDomainEvents().forEach(eventPublisher::publishEvent);
        user.clearDomainEvents();
    }

    private void publishAuthEvent(String subjectId,
                                   String subjectType,
                                   String email,
                                   String ipAddress,
                                   String type) {
        switch (type) {
            case "LOGIN" -> eventPublisher.publishEvent(
                    AuthTokenIssuedEvent.builder()
                            .subjectId(subjectId).subjectType(subjectType)
                            .email(email).ipAddress(ipAddress)
                            .tokenType("ACCESS").build());
            case "REFRESH" -> eventPublisher.publishEvent(
                    AuthTokenRefreshedEvent.builder()
                            .subjectId(subjectId).subjectType(subjectType)
                            .email(email).ipAddress(ipAddress).build());
            case "LOGOUT" -> eventPublisher.publishEvent(
                    AuthLogoutEvent.builder()
                            .subjectId(subjectId).subjectType(subjectType)
                            .email(email).ipAddress(ipAddress)
                            .forced(false).build());
        }
    }
}