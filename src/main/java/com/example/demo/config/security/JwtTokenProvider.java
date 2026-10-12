package com.example.demo.config.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * JWT token provider
 *
 * Extended to support subjectId and subjectType claims
 * for multi-principal authentication (User + Customer).
 *
 * Existing API (generateToken, isTokenValid, extractUsername) unchanged.
 *
 * @author Architecture Team
 * @version 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final JwtConfig jwtConfig;

    // ====================================
    // Existing API (unchanged)
    // ====================================

    /**
     * Generate token from UserDetails (USER subject type by default)
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));
        return generateToken(claims, userDetails.getUsername());
    }

    /**
     * Generate token with custom claims
     */
    public String generateToken(Map<String, Object> extraClaims, String username) {
        long expirationMillis = jwtConfig.getExpiration().toMillis();
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(username)
                .setIssuer(jwtConfig.getIssuer())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expirationMillis))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Generate refresh token
     */
    public String generateRefreshToken(UserDetails userDetails) {
        long refreshExpirationMillis = jwtConfig.getRefreshExpiration().toMillis();
        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .setIssuer(jwtConfig.getIssuer())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(
                        System.currentTimeMillis() + refreshExpirationMillis))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Validate token against UserDetails
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        boolean isValid = username.equals(userDetails.getUsername())
                && !isTokenExpired(token);
        if (!isValid) {
            log.warn("Token validation failed for user: {}", username);
        }
        return isValid;
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        return claimsResolver.apply(extractAllClaims(token));
    }

    // ====================================
    // Extended API (auth module additions)
    // ====================================

    /**
     * Generate access token with subjectId + subjectType claims.
     * Used by AuthService for multi-principal support.
     */
    public String generateToken(UserDetails userDetails,
                                 String subjectId,
                                 String subjectType) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));
        claims.put("subjectId",   subjectId);
        claims.put("subjectType", subjectType);
        return generateToken(claims, userDetails.getUsername());
    }

    /**
     * Generate refresh token with subjectId + subjectType claims.
     */
    public String generateRefreshToken(UserDetails userDetails,
                                        String subjectId,
                                        String subjectType) {
        long refreshExpirationMillis = jwtConfig.getRefreshExpiration().toMillis();
        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .setIssuer(jwtConfig.getIssuer())
                .claim("subjectId",   subjectId)
                .claim("subjectType", subjectType)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(
                        System.currentTimeMillis() + refreshExpirationMillis))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Extract subjectId claim (UUID as String)
     */
    public String extractSubjectId(String token) {
        return extractClaim(token, claims -> claims.get("subjectId", String.class));
    }

    /**
     * Extract subjectType claim ("USER" or "CUSTOMER")
     */
    public String extractSubjectType(String token) {
        Object type = extractClaim(token, claims -> claims.get("subjectType"));
        return type != null ? type.toString() : "USER";
    }

    /**
     * Validate token format/signature only (no UserDetails needed)
     */
    public boolean validateToken(String token) {
        try {
            extractAllClaims(token);
            return !isTokenExpired(token);
        } catch (Exception e) {
            log.warn("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Get access token expiration in seconds
     */
    public long getAccessTokenExpirationSeconds() {
        return jwtConfig.getExpiration().getSeconds();
    }

    /**
     * Get human-readable failure reason for invalid tokens
     */
    public String getValidationFailureReason(String token) {
        try {
            extractAllClaims(token);
            if (isTokenExpired(token)) return "Token has expired";
            return null;
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            return "Token has expired";
        } catch (io.jsonwebtoken.MalformedJwtException e) {
            return "Token is malformed";
        } catch (io.jsonwebtoken.security.SecurityException e) {
            return "Token signature is invalid";
        } catch (Exception e) {
            return "Token is invalid";
        }
    }

    // ====================================
    // Private Helpers
    // ====================================

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtConfig.getSecret()
                .getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}