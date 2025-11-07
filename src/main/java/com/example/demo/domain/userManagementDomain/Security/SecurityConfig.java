package com.example.demo.domain.userManagementDomain.Security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfig {

    private final JwtFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(req -> {
                    req.requestMatchers(
                                    // Public business endpoints
                                    "/products/**",
                                    "/api/v1/products/**",
                                    "/entityProfiles/**",
                                    "/api/v1/entityProfiles/**",
                                    "/roles/**",
                                    "/api/v1/roles/**",
                                    "/orders/**",
                                    "/product-attributes/**",
                                    "/api/v1/product-attributes/**",
                                    "/api/v1/orders/**",
                                    "/categories/**",
                                    "/api/v1/categories/**",
                                    "/shipments/**",
                                    "/api/v1/shipments/**",
                                    "/addresses/**",
                                    "/api/v1/addresses/**",
                                    "/product-attributes/product/**",
                                    "api/v1/product-attributes/product/**",
                                    "/pricings/product/**",
                                    "/api/v1/pricings/**",
                                    "/pricings/**",
                                    "/pricings/product/*/currency/*",
                                    "/api/v1/pricings/product/*/currency/*",
                                    "/addresses/user/**",
                                    "/api/v1/addresses/user/**",
                                    "/roles/permissions/**",
                                    "/api/v1/roles/permissions/**",
                                    "/roles/permissions/init/**",
                                    "/api/v1/roles/permissions/init/**",
                                    "/auth/**",
                                    "users/favorites/**",
                                    "/api/v1/users/favorites/**",
                                    "/reviews/**",
                                    "/assets/**",
                                    "/user-groups/**",
                                    "/users/**",

                                    // Swagger/OpenAPI - root level
                                    "/swagger-ui/**",
                                    "/swagger-ui.html",
                                    "/v2/api-docs",
                                    "/v3/api-docs",
                                    "/v3/api-docs/**",
                                    "/swagger-resources",
                                    "/swagger-resources/**",
                                    "/configuration/ui",
                                    "/configuration/security",
                                    "/webjars/**",

                                    // Swagger/OpenAPI - with context-path
                                    "/api/v1/swagger-ui/**",
                                    "/api/v1/swagger-ui.html",
                                    "/api/v1/v2/api-docs",
                                    "/api/v1/v3/api-docs",
                                    "/api/v1/v3/api-docs/**",
                                    "/api/v1/swagger-resources",
                                    "/api/v1/swagger-resources/**",
                                    "/api/v1/configuration/ui",
                                    "/api/v1/configuration/security",
                                    "/api/v1/webjars/**",

                                    // Error page
                                    "/error"
                            )
                            .permitAll()
                            .anyRequest().authenticated();
                })
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

}
