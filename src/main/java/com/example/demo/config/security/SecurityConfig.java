package com.example.demo.config.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Central security configuration.
 *
 * Two independent filter chains:
 *
 *  Chain 1 — @Order(1) — /actuator/**  (observability — PUBLIC)
 *  ┌─────────────────────────────────┬──────────────────────────────────────┐
 *  │ Endpoint                        │ Access                               │
 *  ├─────────────────────────────────┼──────────────────────────────────────┤
 *  │ /actuator/health                │ Public (no auth)                     │
 *  │ /actuator/info                  │ Public (no auth)                     │
 *  │ /actuator/prometheus            │ Public (no auth)                     │
 *  │ /actuator/metrics               │ Public (no auth)                     │
 *  │ /actuator/loggers               │ Public (no auth)                     │
 *  │ /actuator/**                    │ Public (no auth)                     │
 *  └─────────────────────────────────┴──────────────────────────────────────┘
 *
 *  ⚠️  PRODUCTION WARNING:
 *  This configuration exposes ALL actuator endpoints without authentication.
 *  Acceptable for:
 *    - Local development
 *    - Internal instances behind a firewall / private network
 *    - Kubernetes clusters where the port is not exposed externally
 *  NOT acceptable for:
 *    - Publicly reachable instances (metrics leak JVM internals,
 *      loggers endpoint allows runtime log-level manipulation)
 *  For production, reinstate IP allowlisting or basic auth on
 *  /actuator/prometheus and restrict the rest to ADMIN.
 *
 *  Chain 2 — @Order(2) — everything else (JWT protected)
 *  ┌─────────────────────────────────────────┬───────────────────────┐
 *  │ Endpoint                                │ Access                │
 *  ├─────────────────────────────────────────┼───────────────────────┤
 *  │ POST /api/v1/auth/login                 │ Public                │
 *  │ POST /api/v1/auth/refresh               │ Public                │
 *  │ POST /api/v1/auth/forgot-password       │ Public                │
 *  │ POST /api/v1/auth/reset-password        │ Public                │
 *  │ POST /api/v1/customers (register)       │ Public                │
 *  ├─────────────────────────────────────────┼───────────────────────┤
 *  │ POST /api/v1/users (create user)        │ ADMIN only            │
 *  │ GET  /api/v1/users/**                   │ USER, ADMIN           │
 *  │ PUT  /api/v1/users/**                   │ USER, ADMIN           │
 *  │ DELETE /api/v1/users/**                 │ ADMIN only            │
 *  ├─────────────────────────────────────────┼───────────────────────┤
 *  │ GET  /api/v1/customers/**               │ Authenticated         │
 *  │ PUT/DELETE /api/v1/customers/**         │ @PreAuthorize         │
 *  └─────────────────────────────────────────┴───────────────────────┘
 *
 * @author Architecture Team
 * @version 2.1
 */
@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserDetailsService      userDetailsService;

    // =========================================================================
    // Whitelist Constants
    // =========================================================================

    // ── Swagger ───────────────────────────────────────────────────────────────
    private static final String[] SWAGGER_WHITELIST = {
            "/v3/api-docs",
            "/v3/api-docs/**",
            "/v3/api-docs.yaml",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/swagger-resources",
            "/swagger-resources/**",
            "/webjars/**",
            "/configuration/ui",
            "/configuration/security"
    };

    // ── System ────────────────────────────────────────────────────────────────
    private static final String[] SYSTEM_WHITELIST = {
            "/error",
            "/error/**",
            "/favicon.ico",
            "/api/v1/public/**"
    };

    // ── Auth public ───────────────────────────────────────────────────────────
    // NOTE: /api/v1/auth/logout and /api/v1/auth/change-password
    //       are NOT listed here — they require authentication.
    private static final String[] AUTH_PUBLIC_WHITELIST = {
            "/api/v1/auth/login",
            "/api/v1/auth/refresh",
            "/api/v1/auth/forgot-password",
            "/api/v1/auth/reset-password"
    };

    // ── Kitchen (temporary – testing only) ───────────────────────────────────
    private static final String[] KITCHEN_PUBLIC_WHITELIST = {
            "/api/v1/kitchen-colours",
            "/api/v1/kitchen-colours/**",
            "/api/v1/kitchen-ranges",
            "/api/v1/kitchen-ranges/**",
            "/api/v1/kitchens/**",
            "/api/v1/kitchen-ranges/*/collections",
            "/api/v1/kitchen-ranges/*/collections/**",
            "/api/v1/kitchen-products/**",
            "/api/v1/kitchen-configurations/**",
            "/api/v1/admin/kitchen-collections/**",
            "/api/v1/admin/kitchen-products/**"
    };

    // =========================================================================
    // Chain 1 — Actuator / Observability Security (@Order(1))
    // Evaluated FIRST. Completely isolated from the JWT filter.
    // =========================================================================

    /**
     * Dedicated security filter chain for all /actuator/** endpoints.
     *
     * ALL actuator endpoints are PUBLIC on this instance — no authentication
     * or authorization required. This allows:
     *  - Prometheus to scrape /actuator/prometheus without credentials
     *  - Kubernetes liveness/readiness probes to hit /actuator/health freely
     *  - Grafana / monitoring tooling to query /actuator/metrics directly
     *
     * The chain still exists as a separate filter chain (rather than just
     * permitAll in the main chain) because it:
     *  - Keeps the JwtAuthenticationFilter completely out of the actuator
     *    request path (no JWT parsing overhead on every 15s scrape)
     *  - Isolates observability traffic from application auth logic
     *  - Makes it trivial to re-secure later by editing ONE chain
     *
     * Stateless session: no session is ever created for actuator calls.
     */
    @Bean
    @Order(1)
    public SecurityFilterChain actuatorSecurityFilterChain(
            HttpSecurity http) throws Exception {

        log.info("Configuring Actuator Security Filter Chain " +
                 "(PUBLIC — no auth required on this instance)");

        http
            // Only this chain handles /actuator/**
            .securityMatcher("/actuator/**")
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // ── All actuator endpoints public on this instance ────────
                .anyRequest().permitAll()
            );

        // NOTE: no .httpBasic(), no JWT filter, no entry points —
        // nothing here can trigger a 401/403 challenge.

        log.info("Actuator Security Filter Chain configured " +
                 "— all /actuator/** endpoints are PUBLIC");
        return http.build();
    }

    // =========================================================================
    // Chain 2 — Main Application Security (@Order(2))
    // Evaluated SECOND. Handles all non-actuator endpoints.
    // =========================================================================

    @Bean
    @Order(2)
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        log.info("Configuring Main Application Security Filter Chain");

        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .authorizeHttpRequests(auth -> auth

                // ── Infrastructure ──────────────────────────────────────────
                .requestMatchers(SWAGGER_WHITELIST).permitAll()
                .requestMatchers(SYSTEM_WHITELIST).permitAll()
                .requestMatchers(KITCHEN_PUBLIC_WHITELIST).permitAll()

                // NOTE: actuator paths are NOT listed here at all.
                // Chain 1 (Order 1) claims every /actuator/** request
                // before this chain is ever consulted.

                // ── Auth (public routes only) ───────────────────────────────
                .requestMatchers(AUTH_PUBLIC_WHITELIST).permitAll()

                // logout, change-password, validate, me → must be authenticated
                .requestMatchers(
                        "/api/v1/auth/logout",
                        "/api/v1/auth/change-password",
                        "/api/v1/auth/validate",
                        "/api/v1/auth/me"
                ).authenticated()

                // ── Customer ────────────────────────────────────────────────
                // Registration is public (anyone can sign up)
                .requestMatchers(HttpMethod.POST, "/api/v1/customers").permitAll()

                // All other customer endpoints require authentication.
                // Fine-grained control via @PreAuthorize in CustomerController.
                .requestMatchers("/api/v1/customers/**").authenticated()

                // ── User management ─────────────────────────────────────────
                // Creating a user requires ADMIN
                // (self-registration goes through /api/v1/customers)
                .requestMatchers(HttpMethod.POST,
                        "/api/v1/users",
                        "/api/v1/users/**"
                ).hasRole("ADMIN")

                .requestMatchers(HttpMethod.GET,
                        "/api/v1/users/**"
                ).hasAnyRole("USER", "ADMIN")

                .requestMatchers(HttpMethod.PUT,
                        "/api/v1/users/**"
                ).hasAnyRole("USER", "ADMIN")

                .requestMatchers(HttpMethod.DELETE,
                        "/api/v1/users/**"
                ).hasRole("ADMIN")

                // ── Products (public reads) ─────────────────────────────────
                .requestMatchers(HttpMethod.GET,
                        "/api/v1/products",
                        "/api/v1/products/search",
                        "/api/v1/products/published",
                        "/api/v1/products/{id}",
                        "/api/v1/products/sku/{sku}",
                        "/api/v1/products/category/{categoryId}",
                        "/api/v1/products/brand/{brand}"
                ).permitAll()

                // ── Products (admin reads) ──────────────────────────────────
                .requestMatchers(HttpMethod.GET,
                        "/api/v1/products/statistics",
                        "/api/v1/products/ready-to-publish",
                        "/api/v1/products/incomplete"
                ).hasRole("ADMIN")

                .requestMatchers(HttpMethod.GET,
                        "/api/v1/products/low-stock"
                ).hasAnyRole("ADMIN", "INVENTORY_MANAGER")

                // ── Products (writes) ───────────────────────────────────────
                .requestMatchers(HttpMethod.POST,
                        "/api/v1/products/**"
                ).hasAnyRole("ADMIN", "PRODUCT_MANAGER")

                .requestMatchers(HttpMethod.PUT,
                        "/api/v1/products/**"
                ).hasAnyRole("ADMIN", "PRODUCT_MANAGER")

                .requestMatchers(HttpMethod.DELETE,
                        "/api/v1/products/**"
                ).hasRole("ADMIN")

                // ── Admin ────────────────────────────────────────────────────
                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")

                // ── Everything else requires authentication ──────────────────
                .anyRequest().authenticated()
            )
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(
                jwtAuthFilter,
                UsernamePasswordAuthenticationFilter.class)
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint((request, response, ex) -> {
                    log.error("Unauthorized [{}]: {}",
                            request.getRequestURI(),
                            ex.getMessage());
                    response.sendError(401, "Unauthorized");
                })
                .accessDeniedHandler((request, response, ex) -> {
                    log.error("Access Denied [{}]: {}",
                            request.getRequestURI(),
                            ex.getMessage());
                    response.sendError(403, "Forbidden");
                })
            );

        log.info("Main Application Security Filter Chain configured");
        return http.build();
    }

    // =========================================================================
    // Shared Beans
    // =========================================================================

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOriginPattern("*");
        config.addAllowedMethod("*");
        config.addAllowedHeader("*");
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}