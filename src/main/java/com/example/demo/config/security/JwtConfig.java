package com.example.demo.config.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * JWT configuration properties
 * Externalized for environment-specific settings
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "app.security.jwt")
public class JwtConfig {

    /**
     * JWT secret key (should be 256+ bits for HS256)
     * Must be changed in production and stored securely
     */
    private String secret = "your-256-bit-secret-key-here-change-in-production-minimum-32-characters";

    /**
     * JWT token expiration time
     * Default: 24 hours
     */
    private Duration expiration = Duration.ofHours(24);

    /**
     * JWT refresh token expiration time
     * Default: 7 days
     */
    private Duration refreshExpiration = Duration.ofDays(7);

    /**
     * JWT issuer
     */
    private String issuer = "demo-app";

    /**
     * JWT token prefix in Authorization header
     */
    private String tokenPrefix = "Bearer ";

    /**
     * Header name for JWT token
     */
    private String headerName = "Authorization";
}