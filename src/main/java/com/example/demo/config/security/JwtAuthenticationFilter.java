package com.example.demo.config.security;

import com.example.demo.modules.auth.application.service.AuthPrincipalService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT authentication filter – extended to support both USER and CUSTOMER principals.
 *
 * Flow:
 * 1. Extract Bearer token from Authorization header
 * 2. Extract email + subjectType from token claims
 * 3. Load principal (User or Customer) via AuthPrincipalService
 * 4. Validate token against principal
 * 5. Set authentication in SecurityContext
 *
 * @author Architecture Team
 * @version 1.0
 */
@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider     jwtTokenProvider;
    private final AuthPrincipalService authPrincipalService;
    private final JwtConfig            jwtConfig;

    /**
     * @Lazy on AuthPrincipalService breaks the circular dependency:
     * SecurityConfig → JwtAuthenticationFilter → AuthPrincipalService
     *                                           → CustomerRepository (fine)
     *                                           → CustomUserDetailsService (fine)
     */
    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider,
                                    @Lazy AuthPrincipalService authPrincipalService,
                                    JwtConfig jwtConfig) {
        this.jwtTokenProvider     = jwtTokenProvider;
        this.authPrincipalService = authPrincipalService;
        this.jwtConfig            = jwtConfig;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest  request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain         filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader(jwtConfig.getHeaderName());

        if (authHeader == null || !authHeader.startsWith(jwtConfig.getTokenPrefix())) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            final String jwt         = authHeader.substring(
                    jwtConfig.getTokenPrefix().length());
            final String username    = jwtTokenProvider.extractUsername(jwt);
            final String subjectType = extractSubjectType(jwt);

            if (username != null
                    && SecurityContextHolder.getContext().getAuthentication() == null) {

                // Load principal based on subject type (USER or CUSTOMER)
                UserDetails principal = authPrincipalService
                        .loadPrincipal(username, subjectType);

                if (jwtTokenProvider.isTokenValid(jwt, principal)) {

                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    principal, null, principal.getAuthorities());

                    authToken.setDetails(
                            new WebAuthenticationDetailsSource()
                                    .buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    log.debug("JWT authenticated: email={}, type={}",
                            username, subjectType);
                } else {
                    log.warn("Invalid JWT token for: {}", username);
                }
            }
        } catch (Exception e) {
            log.error("JWT authentication error: {}", e.getMessage());
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extract subjectType claim from token.
     * Falls back to "USER" if claim is missing (backwards compatible).
     */
    private String extractSubjectType(String token) {
        try {
            Object subjectType = jwtTokenProvider
                    .extractClaim(token, claims -> claims.get("subjectType"));
            return subjectType != null ? subjectType.toString() : "USER";
        } catch (Exception e) {
            return "USER";
        }
    }
}