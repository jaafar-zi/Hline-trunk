package com.example.demo.domain.userManagementDomain.Security;

import com.example.demo.domain.userManagementDomain.user.entity.TokenType;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    private final String secretKey = "bG9uZy1yYW5kb20tbGFyZ2UtZW5vdWdoLXNlY3JldC1rZXktZm9yLUpXVA==";
    private final long accessTokenExpiration = 1000 * 60 * 15;
    private final long refreshTokenExpiration = 1000 * 60 * 60 * 24 * 7;
    private final long resetTokenExpiration = 1000 * 60 * 30;

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractTokenType(String token) {
        return extractClaim(token, claims -> (String) claims.get("type"));
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails, TokenType.AUTHENTICATION.toString());
    }

    public String generateForgetPasswordToken(UserDetails userDetails, TokenType type) {
        return generateToken(new HashMap<>(), userDetails, type.toString());
    }

    public String generateRefreshToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails, TokenType.REFRESH.toString());
    }

    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails, String tokenType) {
        long expiration;
        switch (TokenType.valueOf(tokenType)) {
            case AUTHENTICATION -> expiration = accessTokenExpiration;
            case REFRESH -> expiration = refreshTokenExpiration;
            case PASSWORD_RESET -> expiration = resetTokenExpiration;
            default -> throw new IllegalArgumentException("Unsupported token type: " + tokenType);
        }
        return buildToken(extraClaims, userDetails, expiration, tokenType);
    }

    private String buildToken(Map<String, Object> extraClaims, UserDetails userDetails, long expiration, String type) {
        var authorities = userDetails.getAuthorities()
                .stream().map(GrantedAuthority::getAuthority)
                .toList();

        extraClaims.put("type", type);

        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .claim("authorities", authorities)
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    public boolean isRefreshTokenValid(String token, UserDetails userDetails) {
        return isTokenValid(token, userDetails) &&
                TokenType.REFRESH.name().equals(extractTokenType(token));
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}

