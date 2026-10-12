package com.example.demo.common.handler;

import com.example.demo.common.exception.ErrorCode;
import com.example.demo.common.exception.ForbiddenException;
import com.example.demo.common.exception.UnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Dedicated handler for security-related exceptions
 *
 * Handles:
 * - Authentication failures
 * - Authorization failures
 * - Token-related errors
 *
 * @author Architecture Team
 * @version 1.0
 */
@Slf4j
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
@RequiredArgsConstructor
public class SecurityExceptionHandler {

    private final ErrorResponseBuilder errorResponseBuilder;

    /**
     * Handle UnauthorizedException
     */
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiErrorResponse> handleUnauthorized(
            UnauthorizedException ex,
            HttpServletRequest request) {

        log.warn("Unauthorized access attempt - Path: {}", request.getRequestURI());

        ApiErrorResponse response = errorResponseBuilder.build(
                HttpStatus.UNAUTHORIZED.value(),
                ErrorCode.UNAUTHORIZED,
                ex.getMessage(),
                ex.getDetails(),
                request
        );

        response.addSuggestion("Ensure you are logged in");
        response.addSuggestion("Check if your session has expired");

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(response);
    }

    /**
     * Handle ForbiddenException
     */
    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ApiErrorResponse> handleForbidden(
            ForbiddenException ex,
            HttpServletRequest request) {

        log.warn("Forbidden access attempt - Path: {} - User: {}",
                request.getRequestURI(),
                request.getUserPrincipal() != null ?
                        request.getUserPrincipal().getName() : "anonymous");

        ApiErrorResponse response = errorResponseBuilder.build(
                HttpStatus.FORBIDDEN.value(),
                ErrorCode.FORBIDDEN,
                ex.getMessage(),
                ex.getDetails(),
                request
        );

        response.addSuggestion("Verify you have the required permissions");
        response.addSuggestion("Contact your administrator for access");

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(response);
    }

    /**
     * Handle Spring Security AuthenticationException
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiErrorResponse> handleAuthenticationException(
            AuthenticationException ex,
            HttpServletRequest request) {

        log.warn("Authentication failed: {} - Path: {}",
                ex.getMessage(), request.getRequestURI());

        String message = "Authentication failed";
        String details = "Invalid credentials or authentication token";

        if (ex instanceof BadCredentialsException) {
            message = "Invalid credentials";
            details = "The username or password you entered is incorrect";
        }

        ApiErrorResponse response = errorResponseBuilder.build(
                HttpStatus.UNAUTHORIZED.value(),
                ErrorCode.UNAUTHORIZED,
                message,
                details,
                request
        );

        response.addSuggestion("Check your username and password");
        response.addSuggestion("Ensure your account is active");

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(response);
    }

    /**
     * Handle Spring Security AccessDeniedException
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDenied(
            AccessDeniedException ex,
            HttpServletRequest request) {

        log.warn("Access denied: {} - Path: {} - User: {}",
                ex.getMessage(),
                request.getRequestURI(),
                request.getUserPrincipal() != null ?
                        request.getUserPrincipal().getName() : "anonymous");

        ApiErrorResponse response = errorResponseBuilder.build(
                HttpStatus.FORBIDDEN.value(),
                ErrorCode.FORBIDDEN,
                "Access denied",
                "You don't have permission to access this resource",
                request
        );

        response.addSuggestion("Verify you have the required role or permission");
        response.addSuggestion("Contact your administrator if you believe this is an error");

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(response);
    }
}