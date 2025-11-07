package com.example.demo.domain.userManagementDomain.Security;

import com.google.common.net.HttpHeaders;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;


    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        String uri = request.getRequestURI();

        if (uri.contains("/api/v1/auth/register") ||
                uri.contains("/api/v1/products") ||
                uri.contains("/api/v1/products/erp-table") ||
                uri.contains("/api/v1/roles/permissions") ||
                uri.contains("/api/v1/roles") ||
                uri.contains("/api/v1/entityProfiles") ||
                uri.contains("/api/v1/addresses") ||
                uri.contains("/api/v1/addresses/user") ||
                uri.contains("/api/v1/orders") ||
                uri.contains("/api/v1/roles/permissions/init") ||
                uri.contains("/api/v1/pricings/product/\\d+/currency/[a-zA-Z]+") ||
                uri.contains("/api/v1/pricings") ||
                uri.contains("api/v1/product-attributes/product") ||
                uri.contains("/api/v1/product-attributes")||
                uri.contains("/api/v1/categories")||
                uri.contains("/api/v1/reviews")||
                uri.contains("/api/v1/reviews/product//\\d+")||
                uri.contains("/api/v1/assets")||
                uri.contains("/api/v1/users/favorites/")||
                uri.contains("/api/v1/auth") ||
                uri.contains("/api/v1/shipments") ||
                uri.contains("/api/v1/user-groups") ||
                uri.contains("/api/v1/users") ||

                // Add Swagger-related paths
                uri.contains("/api/v1/swagger-ui") ||
                uri.contains("/api/v1/v3/api-docs") ||
                uri.contains("/api/v1/swagger-resources") ||
                uri.contains("/api/v1/webjars") ||
                uri.contains("/api/v1/configuration") ||
                uri.contains("/api/v1/error")
        ) {
            filterChain.doFilter(request, response);
            return;
        }
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        final String jwt;
        final String userEmail;
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        jwt = authHeader.substring(7);
        userEmail = jwtService.extractUsername(jwt);
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);
            if (jwtService.isTokenValid(jwt, userDetails)) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities());
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        filterChain.doFilter(request, response);
    }

}
