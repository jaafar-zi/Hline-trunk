package com.example.demo.common.interceptor;

import com.example.demo.common.annotation.PublicEndpoint;
import com.example.demo.common.annotation.RequireRole;
import com.example.demo.common.constant.SecurityConstants;
import com.example.demo.common.util.JsonUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;

import javax.crypto.SecretKey;

import java.nio.charset.StandardCharsets;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthenticationInterceptor implements HandlerInterceptor {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.enabled:true}")
    private boolean jwtEnabled;

    @Value("${api.key.enabled:false}")
    private boolean apiKeyEnabled;

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final Set<String> PUBLIC_PATHS = new HashSet<>(Arrays.asList(
            "/api/v1/auth/**",
            "/api/v1/public/**",

            "/api/v1/products",
            "/api/v1/products/search",
            "/api/v1/products/published",

            "/actuator/health",
            "/actuator/health/**",
            "/actuator/info",

            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/v3/api-docs",
            "/webjars/**",

            "/error",
            "/error/**",
            "/favicon.ico",

            // ✅ ADD — Kitchen endpoints (remove when auth module is ready)
            "/api/v1/kitchen-colours",
            "/api/v1/kitchen-colours/**",
            "/api/v1/kitchen-ranges",
            "/api/v1/kitchen-ranges/**",
            "/api/v1/kitchen-configurations/**",
            "/api/v1/kitchens/**",
            "/api/v1/kitchen-products/**",
            "/api/v1/admin/kitchen-collections/**",
            "/api/v1/admin/kitchen-products/**",
            "/api/v1/admin/kitchen-variants/**"
    ));

    private static final Set<String> PUBLIC_PATH_PREFIXES = new HashSet<>(Arrays.asList(
            "/api/v1/products/sku/",
            "/api/v1/products/category/",
            "/api/v1/products/brand/"
    ));

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;

        if (isPublicEndpoint(request, handlerMethod)) {
            log.debug("Public endpoint accessed: {}", request.getRequestURI());
            return true;
        }

        if (SecurityContextHolder.getContext().getAuthentication() != null
                && SecurityContextHolder.getContext().getAuthentication().isAuthenticated()
                && !(SecurityContextHolder.getContext().getAuthentication().getPrincipal()
                        instanceof String)) {
            log.debug("Already authenticated via Spring Security: {}",
                    request.getRequestURI());
            return true;
        }

        AuthenticationResult authResult = authenticate(request);

        if (!authResult.isAuthenticated()) {
            handleAuthenticationFailure(response, authResult, request);
            return false;
        }

        setupSecurityContext(authResult);

        if (!authorize(handlerMethod, authResult)) {
            handleAuthorizationFailure(response, authResult, request);
            return false;
        }

        request.setAttribute("userId", authResult.getUserId());
        request.setAttribute("username", authResult.getUsername());
        request.setAttribute("roles", authResult.getRoles());

        log.debug("Authentication successful for user: {} on path: {}",
                authResult.getUsername(), request.getRequestURI());

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request,
                           HttpServletResponse response,
                           Object handler,
                           ModelAndView modelAndView) throws Exception {
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler,
                                Exception ex) throws Exception {
        SecurityContextHolder.clearContext();
    }

    private boolean isPublicEndpoint(HttpServletRequest request, HandlerMethod handlerMethod) {
        if (handlerMethod.hasMethodAnnotation(PublicEndpoint.class)) {
            return true;
        }

        if (handlerMethod.getBeanType().isAnnotationPresent(PublicEndpoint.class)) {
            return true;
        }

        String path = request.getRequestURI();
        String method = request.getMethod();

        if ("GET".equalsIgnoreCase(method)) {
            boolean isPublicPrefix = PUBLIC_PATH_PREFIXES.stream()
                    .anyMatch(path::startsWith);
            if (isPublicPrefix) {
                return true;
            }

            if (path.matches("/api/v1/products/[^/]+$")) {
                return true;
            }
        }

        return PUBLIC_PATHS.stream().anyMatch(pattern -> matchPath(path, pattern));
    }

    private boolean matchPath(String path, String pattern) {
        if (pattern.endsWith("/**")) {
            String prefix = pattern.substring(0, pattern.length() - 3);
            return path.startsWith(prefix);
        }
        return path.equals(pattern);
    }

    private AuthenticationResult authenticate(HttpServletRequest request) {
        if (jwtEnabled) {
            AuthenticationResult jwtResult = authenticateWithJWT(request);
            if (jwtResult.isAuthenticated()) {
                return jwtResult;
            }
        }

        if (apiKeyEnabled) {
            AuthenticationResult apiKeyResult = authenticateWithApiKey(request);
            if (apiKeyResult.isAuthenticated()) {
                return apiKeyResult;
            }
        }

        return AuthenticationResult.failed("No valid authentication credentials provided");
    }

    private AuthenticationResult authenticateWithJWT(HttpServletRequest request) {
        String token = extractJwtToken(request);

        if (token == null || token.isEmpty()) {
            return AuthenticationResult.failed("JWT token not found");
        }

        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            String tokenType = claims.get(SecurityConstants.JWT_CLAIM_TOKEN_TYPE, String.class);
            if (!SecurityConstants.TOKEN_TYPE_ACCESS.equals(tokenType)) {
                return AuthenticationResult.failed("Invalid token type");
            }

            Long userId = claims.get(SecurityConstants.JWT_CLAIM_USER_ID, Long.class);
            String username = claims.get(SecurityConstants.JWT_CLAIM_USERNAME, String.class);
            String email = claims.get(SecurityConstants.JWT_CLAIM_EMAIL, String.class);

            @SuppressWarnings("unchecked")
            List<String> roles = claims.get(SecurityConstants.JWT_CLAIM_ROLES, List.class);
            if (roles == null) roles = new ArrayList<>();

            @SuppressWarnings("unchecked")
            List<String> authorities =
                    claims.get(SecurityConstants.JWT_CLAIM_AUTHORITIES, List.class);
            if (authorities == null) authorities = new ArrayList<>();

            return AuthenticationResult.success(userId, username, email,
                    roles, authorities, "JWT");

        } catch (ExpiredJwtException e) {
            log.warn("JWT token expired: {}", e.getMessage());
            return AuthenticationResult.failed("Token expired");
        } catch (SignatureException e) {
            log.warn("JWT signature validation failed: {}", e.getMessage());
            return AuthenticationResult.failed("Invalid token signature");
        } catch (MalformedJwtException e) {
            log.warn("Malformed JWT token: {}", e.getMessage());
            return AuthenticationResult.failed("Malformed token");
        } catch (Exception e) {
            log.error("JWT authentication failed", e);
            return AuthenticationResult.failed("Authentication failed: " + e.getMessage());
        }
    }

    private String extractJwtToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(SecurityConstants.JWT_HEADER_NAME);
        if (bearerToken != null
                && bearerToken.startsWith(SecurityConstants.JWT_HEADER_PREFIX)) {
            return bearerToken.substring(SecurityConstants.JWT_HEADER_PREFIX.length());
        }

        String customToken = request.getHeader("X-Auth-Token");
        if (customToken != null && !customToken.isEmpty()) {
            return customToken;
        }

        String queryToken = request.getParameter("token");
        if (queryToken != null && !queryToken.isEmpty()) {
            log.warn("Token passed as query parameter - not recommended");
            return queryToken;
        }

        return null;
    }

    private AuthenticationResult authenticateWithApiKey(HttpServletRequest request) {
        String apiKey = request.getHeader(SecurityConstants.API_KEY_HEADER);
        String apiSecret = request.getHeader(SecurityConstants.API_SECRET_HEADER);

        if (apiKey == null || apiKey.isEmpty()) {
            return AuthenticationResult.failed("API key not found");
        }

        boolean isValid = validateApiKey(apiKey, apiSecret);
        if (!isValid) {
            return AuthenticationResult.failed("Invalid API key");
        }

        ApiKeyDetails details = getApiKeyDetails(apiKey);
        if (details == null) {
            return AuthenticationResult.failed("API key not found");
        }

        if (!details.isActive()) {
            return AuthenticationResult.failed("API key is inactive");
        }

        return AuthenticationResult.success(
                details.getUserId(), details.getUsername(), details.getEmail(),
                details.getRoles(), details.getAuthorities(), "API_KEY"
        );
    }

    private void setupSecurityContext(AuthenticationResult authResult) {
        List<GrantedAuthority> authorities = new ArrayList<>();

        authResult.getRoles().forEach(role ->
                authorities.add(new SimpleGrantedAuthority("ROLE_" + role))
        );

        authResult.getAuthorities().forEach(authority ->
                authorities.add(new SimpleGrantedAuthority(authority))
        );

        UserPrincipal principal = new UserPrincipal(
                authResult.getUserId(),
                authResult.getUsername(),
                authResult.getEmail(),
                authorities
        );

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(principal, null, authorities);

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private boolean authorize(HandlerMethod handlerMethod, AuthenticationResult authResult) {
        RequireRole methodAnnotation = handlerMethod.getMethodAnnotation(RequireRole.class);
        if (methodAnnotation != null) {
            return hasRequiredRoles(authResult, methodAnnotation.value());
        }

        RequireRole classAnnotation =
                handlerMethod.getBeanType().getAnnotation(RequireRole.class);
        if (classAnnotation != null) {
            return hasRequiredRoles(authResult, classAnnotation.value());
        }

        return true;
    }

    private boolean hasRequiredRoles(AuthenticationResult authResult, String[] requiredRoles) {
        if (requiredRoles == null || requiredRoles.length == 0) {
            return true;
        }
        Set<String> userRoles = new HashSet<>(authResult.getRoles());
        return Arrays.stream(requiredRoles).anyMatch(userRoles::contains);
    }

    private void handleAuthenticationFailure(HttpServletResponse response,
                                             AuthenticationResult authResult,
                                             HttpServletRequest request) throws Exception {
        log.warn("Authentication failed [{}]: {}",
                request.getRequestURI(), authResult.getErrorMessage());

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        Map<String, Object> errorResponse = new LinkedHashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now().format(FORMATTER));
        errorResponse.put("status", HttpStatus.UNAUTHORIZED.value());
        errorResponse.put("error", "Unauthorized");
        errorResponse.put("message", authResult.getErrorMessage());
        errorResponse.put("path", request.getRequestURI());

        response.getWriter().write(JsonUtils.toJson(errorResponse));
        response.getWriter().flush();
    }

    private void handleAuthorizationFailure(HttpServletResponse response,
                                            AuthenticationResult authResult,
                                            HttpServletRequest request) throws Exception {
        log.warn("Authorization failed [{}] for user: {}",
                request.getRequestURI(), authResult.getUsername());

        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        Map<String, Object> errorResponse = new LinkedHashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now().format(FORMATTER));
        errorResponse.put("status", HttpStatus.FORBIDDEN.value());
        errorResponse.put("error", "Forbidden");
        errorResponse.put("message", "Access denied. Insufficient permissions.");
        errorResponse.put("path", request.getRequestURI());

        response.getWriter().write(JsonUtils.toJson(errorResponse));
        response.getWriter().flush();
    }

    private boolean validateApiKey(String apiKey, String apiSecret) {
        return false;
    }

    private ApiKeyDetails getApiKeyDetails(String apiKey) {
        return null;
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    private static class AuthenticationResult {
        private boolean authenticated;
        private Long userId;
        private String username;
        private String email;
        private List<String> roles;
        private List<String> authorities;
        private String authType;
        private String errorMessage;

        public static AuthenticationResult success(Long userId, String username, String email,
                                                   List<String> roles, List<String> authorities,
                                                   String authType) {
            return new AuthenticationResult(true, userId, username, email,
                    roles, authorities, authType, null);
        }

        public static AuthenticationResult failed(String errorMessage) {
            return new AuthenticationResult(false, null, null, null,
                    new ArrayList<>(), new ArrayList<>(), null, errorMessage);
        }
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    private static class UserPrincipal {
        private Long id;
        private String username;
        private String email;
        private Collection<? extends GrantedAuthority> authorities;
    }

    @lombok.Data
    private static class ApiKeyDetails {
        private Long userId;
        private String username;
        private String email;
        private List<String> roles;
        private List<String> authorities;
        private boolean active;
    }
}