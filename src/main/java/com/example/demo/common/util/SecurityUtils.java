package com.example.demo.common.util;

import com.example.demo.common.validator.PasswordValidator;
import jakarta.servlet.http.HttpServletRequest;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.security.SecureRandom;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Utility class for Security operations
 *
 * Features:
 * - Authentication and Authorization
 * - Password validation and encoding (delegates to PasswordValidator)
 * - Token generation
 * - Security context management
 * - IP address handling
 * - Input sanitization
 * - Session management
 * - Security headers
 * - CSRF protection
 *
 * @author Architecture Team
 * @version 1.0
 */
@Slf4j
@UtilityClass
public class SecurityUtils {

    // ========== Constants ==========

    private static final PasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder(12);
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private static final int DEFAULT_TOKEN_LENGTH = 32;
    private static final int DEFAULT_OTP_LENGTH = 6;
    private static final int SESSION_TIMEOUT_MINUTES = 30;

    // SQL injection patterns
    private static final Pattern[] SQL_INJECTION_PATTERNS = {
            Pattern.compile("('.+--)|(--)|(;)|(\\|\\|)|(\\*)"),
            Pattern.compile("\\b(ALTER|CREATE|DELETE|DROP|EXEC(UTE)?|INSERT( +INTO)?|MERGE|SELECT|UPDATE|UNION( +ALL)?)\\b", Pattern.CASE_INSENSITIVE)
    };

    // XSS patterns
    private static final Pattern[] XSS_PATTERNS = {
            Pattern.compile("<script>(.*?)</script>", Pattern.CASE_INSENSITIVE),
            Pattern.compile("src[\r\n]*=[\r\n]*\\\'(.*?)\\\'", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
            Pattern.compile("</script>", Pattern.CASE_INSENSITIVE),
            Pattern.compile("<script(.*?)>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
            Pattern.compile("eval\\((.*?)\\)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
            Pattern.compile("expression\\((.*?)\\)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
            Pattern.compile("javascript:", Pattern.CASE_INSENSITIVE),
            Pattern.compile("vbscript:", Pattern.CASE_INSENSITIVE),
            Pattern.compile("onload(.*?)=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL)
    };

    // ========== Authentication & Authorization ==========

    /**
     * Get current authentication
     */
    public static Optional<Authentication> getAuthentication() {
        SecurityContext context = SecurityContextHolder.getContext();
        return Optional.ofNullable(context.getAuthentication());
    }

    /**
     * Get current username
     */
    public static Optional<String> getCurrentUsername() {
        return getAuthentication()
                .map(Authentication::getName);
    }

    /**
     * Get current user details
     */
    public static Optional<UserDetails> getCurrentUserDetails() {
        return getAuthentication()
                .map(Authentication::getPrincipal)
                .filter(principal -> principal instanceof UserDetails)
                .map(principal -> (UserDetails) principal);
    }

    /**
     * Get current user ID (assuming principal has getId method)
     */
    public static Optional<Long> getCurrentUserId() {
        return getAuthentication()
                .map(Authentication::getPrincipal)
                .filter(principal -> principal instanceof UserPrincipal)
                .map(principal -> ((UserPrincipal) principal).getId());
    }

    /**
     * Check if user is authenticated
     */
    public static boolean isAuthenticated() {
        return getAuthentication()
                .map(Authentication::isAuthenticated)
                .orElse(false);
    }

    /**
     * Check if user is anonymous
     */
    public static boolean isAnonymous() {
        return !isAuthenticated();
    }

    /**
     * Get user authorities/roles
     */
    public static Set<String> getCurrentAuthorities() {
        return getAuthentication()
                .map(Authentication::getAuthorities)
                .map(authorities -> authorities.stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toSet()))
                .orElse(Collections.emptySet());
    }

    /**
     * Check if user has role
     */
    public static boolean hasRole(String role) {
        if (StringUtils.isBlank(role)) {
            return false;
        }

        String roleToCheck = role.startsWith("ROLE_") ? role : "ROLE_" + role;
        return getCurrentAuthorities().contains(roleToCheck);
    }

    /**
     * Check if user has any of the roles
     */
    public static boolean hasAnyRole(String... roles) {
        if (roles == null || roles.length == 0) {
            return false;
        }

        Set<String> authorities = getCurrentAuthorities();
        return Arrays.stream(roles)
                .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
                .anyMatch(authorities::contains);
    }

    /**
     * Check if user has all roles
     */
    public static boolean hasAllRoles(String... roles) {
        if (roles == null || roles.length == 0) {
            return false;
        }

        Set<String> authorities = getCurrentAuthorities();
        return Arrays.stream(roles)
                .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
                .allMatch(authorities::contains);
    }

    /**
     * Check if user has authority
     */
    public static boolean hasAuthority(String authority) {
        if (StringUtils.isBlank(authority)) {
            return false;
        }
        return getCurrentAuthorities().contains(authority);
    }

    /**
     * Check if user has any authority
     */
    public static boolean hasAnyAuthority(String... authorities) {
        if (authorities == null || authorities.length == 0) {
            return false;
        }

        Set<String> userAuthorities = getCurrentAuthorities();
        return Arrays.stream(authorities)
                .anyMatch(userAuthorities::contains);
    }

    /**
     * Check if user is admin
     */
    public static boolean isAdmin() {
        return hasAnyRole("ADMIN", "SUPER_ADMIN", "SYSTEM_ADMIN");
    }

    /**
     * Clear security context
     */
    public static void clearContext() {
        SecurityContextHolder.clearContext();
    }

    // ========== Password Operations (Delegating to PasswordValidator) ==========

    /**
     * Encode password using BCrypt
     */
    public static String encodePassword(String rawPassword) {
        if (StringUtils.isBlank(rawPassword)) {
            throw new SecurityException("Password must not be blank");
        }
        return PASSWORD_ENCODER.encode(rawPassword);
    }

    /**
     * Verify password matches encoded password
     */
    public static boolean matches(String rawPassword, String encodedPassword) {
        if (StringUtils.isBlank(rawPassword) || StringUtils.isBlank(encodedPassword)) {
            return false;
        }
        return PASSWORD_ENCODER.matches(rawPassword, encodedPassword);
    }

    /**
     * Validate password strength using PasswordValidator
     */
    public static PasswordValidator.PasswordStrength validatePassword(String password) {
        return PasswordValidator.validateStrength(password);
    }

    /**
     * Check if password is strong (delegates to PasswordValidator)
     */
    public static boolean isPasswordStrong(String password) {
        PasswordValidator.PasswordStrength strength = PasswordValidator.validateStrength(password);
        return strength.isValid() && strength.getScore() >= 60;
    }

    /**
     * Validate password with custom requirements
     */
    public static boolean validatePasswordWithRequirements(
            String password,
            int minLength,
            boolean requireUppercase,
            boolean requireLowercase,
            boolean requireDigit,
            boolean requireSpecial) {

        return PasswordValidator.meetsMinimumRequirements(
                password, minLength, requireUppercase,
                requireLowercase, requireDigit, requireSpecial
        );
    }

    /**
     * Get password validation suggestions
     */
    public static List<String> getPasswordSuggestions(String password) {
        return PasswordValidator.generateSuggestions(password);
    }

    /**
     * Check if password is common/weak
     */
    public static boolean isCommonPassword(String password) {
        return PasswordValidator.isCommonPassword(password);
    }

    /**
     * Calculate password entropy
     */
    public static double calculatePasswordEntropy(String password) {
        return PasswordValidator.calculateEntropy(password);
    }

    /**
     * Generate random secure password
     */
    public static String generateRandomPassword(int length) {
        if (length < 8) {
            length = 12;
        }

        String lowercase = "abcdefghijklmnopqrstuvwxyz";
        String uppercase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String digits = "0123456789";
        String special = "!@#$%^&*()_+-=[]{}|;:,.<>?";
        String allChars = lowercase + uppercase + digits + special;

        StringBuilder password = new StringBuilder();

        // Ensure at least one of each type
        password.append(lowercase.charAt(SECURE_RANDOM.nextInt(lowercase.length())));
        password.append(uppercase.charAt(SECURE_RANDOM.nextInt(uppercase.length())));
        password.append(digits.charAt(SECURE_RANDOM.nextInt(digits.length())));
        password.append(special.charAt(SECURE_RANDOM.nextInt(special.length())));

        // Fill the rest randomly
        for (int i = 4; i < length; i++) {
            password.append(allChars.charAt(SECURE_RANDOM.nextInt(allChars.length())));
        }

        // Shuffle the password
        return shuffleString(password.toString());
    }

    /**
     * Generate random password with default length
     */
    public static String generateRandomPassword() {
        return generateRandomPassword(12);
    }

    /**
     * Shuffle string characters
     */
    private static String shuffleString(String input) {
        List<Character> characters = input.chars()
                .mapToObj(c -> (char) c)
                .collect(Collectors.toList());
        Collections.shuffle(characters, SECURE_RANDOM);
        return characters.stream()
                .map(String::valueOf)
                .collect(Collectors.joining());
    }

    // ========== Token Generation ==========

    /**
     * Generate secure random token
     */
    public static String generateToken() {
        return generateToken(DEFAULT_TOKEN_LENGTH);
    }

    /**
     * Generate secure random token with specific length
     */
    public static String generateToken(int length) {
        byte[] randomBytes = new byte[length];
        SECURE_RANDOM.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    /**
     * Generate UUID token
     */
    public static String generateUuidToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * Generate numeric OTP
     */
    public static String generateOtp() {
        return generateOtp(DEFAULT_OTP_LENGTH);
    }

    /**
     * Generate numeric OTP with specific length
     */
    public static String generateOtp(int length) {
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < length; i++) {
            otp.append(SECURE_RANDOM.nextInt(10));
        }
        return otp.toString();
    }

    /**
     * Generate alphanumeric code
     */
    public static String generateAlphanumericCode(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < length; i++) {
            code.append(chars.charAt(SECURE_RANDOM.nextInt(chars.length())));
        }
        return code.toString();
    }

    /**
     * Generate API key
     */
    public static String generateApiKey() {
        return "sk_" + generateToken(32);
    }

    /**
     * Generate session ID
     */
    public static String generateSessionId() {
        return generateUuidToken();
    }

    // ========== Input Sanitization ==========

    /**
     * Sanitize input to prevent XSS
     */
    public static String sanitizeXss(String input) {
        if (StringUtils.isBlank(input)) {
            return input;
        }

        String sanitized = input;

        // Remove script tags and their content
        for (Pattern pattern : XSS_PATTERNS) {
            sanitized = pattern.matcher(sanitized).replaceAll("");
        }

        // HTML encode special characters
        sanitized = sanitized.replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#x27;")
                .replace("/", "&#x2F;");

        return sanitized;
    }

    /**
     * Check if input contains XSS
     */
    public static boolean containsXss(String input) {
        if (StringUtils.isBlank(input)) {
            return false;
        }

        for (Pattern pattern : XSS_PATTERNS) {
            if (pattern.matcher(input).find()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Sanitize SQL input
     */
    public static String sanitizeSql(String input) {
        if (StringUtils.isBlank(input)) {
            return input;
        }

        // Escape single quotes
        return input.replace("'", "''")
                .replace("\\", "\\\\")
                .replace("\0", "");
    }

    /**
     * Check if input contains SQL injection
     */
    public static boolean containsSqlInjection(String input) {
        if (StringUtils.isBlank(input)) {
            return false;
        }

        for (Pattern pattern : SQL_INJECTION_PATTERNS) {
            if (pattern.matcher(input).find()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Sanitize filename
     */
    public static String sanitizeFilename(String filename) {
        if (StringUtils.isBlank(filename)) {
            return filename;
        }

        // Remove path traversal characters
        String sanitized = filename.replaceAll("[.]{2,}", "")
                .replaceAll("[/\\\\]", "")
                .replaceAll("[^a-zA-Z0-9._-]", "_");

        // Limit length
        if (sanitized.length() > 255) {
            String extension = FileUtils.getExtension(sanitized);
            String baseName = FileUtils.getBaseName(sanitized);
            sanitized = baseName.substring(0, 250 - extension.length()) + "." + extension;
        }

        return sanitized;
    }

    /**
     * Validate and sanitize email
     */
    public static String sanitizeEmail(String email) {
        if (StringUtils.isBlank(email)) {
            return email;
        }

        email = email.trim().toLowerCase();

        if (!StringUtils.isValidEmail(email)) {
            throw new SecurityException("Invalid email format");
        }

        return email;
    }

    /**
     * Sanitize URL
     */
    public static String sanitizeUrl(String url) {
        if (StringUtils.isBlank(url)) {
            return url;
        }

        // Check for allowed protocols
        if (!url.matches("^(https?|ftp)://.*")) {
            throw new SecurityException("URL must use HTTP, HTTPS, or FTP protocol");
        }

        // Remove dangerous characters
        return url.replaceAll("[<>\"']", "");
    }

    // ========== IP Address Operations ==========

    /**
     * Get client IP address from request
     */
    public static String getClientIp(HttpServletRequest request) {
        if (request == null) {
            return null;
        }

        String ip = request.getHeader("X-Forwarded-For");
        if (isValidIp(ip)) {
            return ip.split(",")[0].trim();
        }

        ip = request.getHeader("X-Real-IP");
        if (isValidIp(ip)) {
            return ip;
        }

        ip = request.getHeader("Proxy-Client-IP");
        if (isValidIp(ip)) {
            return ip;
        }

        ip = request.getHeader("WL-Proxy-Client-IP");
        if (isValidIp(ip)) {
            return ip;
        }

        ip = request.getHeader("HTTP_CLIENT_IP");
        if (isValidIp(ip)) {
            return ip;
        }

        ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        if (isValidIp(ip)) {
            return ip;
        }

        return request.getRemoteAddr();
    }

    /**
     * Validate IP address
     */
    private static boolean isValidIp(String ip) {
        return StringUtils.isNotBlank(ip)
                && !"unknown".equalsIgnoreCase(ip)
                && StringUtils.isValidIpv4(ip);
    }

    /**
     * Check if IP is in whitelist
     */
    public static boolean isIpWhitelisted(String ip, Set<String> whitelist) {
        if (StringUtils.isBlank(ip) || CollectionUtils.isEmpty(whitelist)) {
            return false;
        }
        return whitelist.contains(ip);
    }

    /**
     * Check if IP is in blacklist
     */
    public static boolean isIpBlacklisted(String ip, Set<String> blacklist) {
        if (StringUtils.isBlank(ip) || CollectionUtils.isEmpty(blacklist)) {
            return false;
        }
        return blacklist.contains(ip);
    }

    /**
     * Check if IP is from localhost
     */
    public static boolean isLocalhost(String ip) {
        return "127.0.0.1".equals(ip)
                || "0:0:0:0:0:0:0:1".equals(ip)
                || "localhost".equalsIgnoreCase(ip);
    }

    // ========== CSRF Protection ==========

    /**
     * Generate CSRF token
     */
    public static String generateCsrfToken() {
        return generateToken(32);
    }

    /**
     * Validate CSRF token
     */
    public static boolean validateCsrfToken(String sessionToken, String requestToken) {
        if (StringUtils.isBlank(sessionToken) || StringUtils.isBlank(requestToken)) {
            return false;
        }
        return sessionToken.equals(requestToken);
    }

    // ========== Security Headers ==========

    /**
     * Get recommended security headers
     */
    public static Map<String, String> getSecurityHeaders() {
        Map<String, String> headers = new HashMap<>();

        headers.put("X-Frame-Options", "DENY");
        headers.put("X-XSS-Protection", "1; mode=block");
        headers.put("X-Content-Type-Options", "nosniff");
        headers.put("Content-Security-Policy",
                "default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline'");
        headers.put("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
        headers.put("Referrer-Policy", "no-referrer-when-downgrade");
        headers.put("Permissions-Policy", "geolocation=(), microphone=(), camera=()");

        return headers;
    }

    // ========== Rate Limiting ==========

    private static final Map<String, RateLimitInfo> RATE_LIMIT_TRACKER = new HashMap<>();

    /**
     * Check if request is rate limited
     */
    public static boolean isRateLimited(String identifier, int maxAttempts, long windowSeconds) {
        if (StringUtils.isBlank(identifier)) {
            return false;
        }

        long now = System.currentTimeMillis();
        RateLimitInfo info = RATE_LIMIT_TRACKER.get(identifier);

        if (info == null) {
            info = new RateLimitInfo(1, now);
            RATE_LIMIT_TRACKER.put(identifier, info);
            return false;
        }

        long windowMillis = windowSeconds * 1000;
        if (now - info.getFirstAttempt() > windowMillis) {
            info.setAttempts(1);
            info.setFirstAttempt(now);
            return false;
        }

        if (info.getAttempts() >= maxAttempts) {
            return true;
        }

        info.incrementAttempts();
        return false;
    }

    /**
     * Clear rate limit for identifier
     */
    public static void clearRateLimit(String identifier) {
        if (StringUtils.isNotBlank(identifier)) {
            RATE_LIMIT_TRACKER.remove(identifier);
        }
    }

    // ========== Session Management ==========

    /**
     * Check if session is valid
     */
    public static boolean isSessionValid(Date sessionCreated, Date lastAccess) {
        if (sessionCreated == null || lastAccess == null) {
            return false;
        }

        long now = System.currentTimeMillis();
        long sessionAge = now - sessionCreated.getTime();
        long idleTime = now - lastAccess.getTime();

        long maxSessionAge = 24 * 60 * 60 * 1000;
        long maxIdleTime = SESSION_TIMEOUT_MINUTES * 60 * 1000;

        return sessionAge < maxSessionAge && idleTime < maxIdleTime;
    }

    /**
     * Calculate session timeout
     */
    public static Date calculateSessionTimeout(Date lastAccess) {
        if (lastAccess == null) {
            return null;
        }
        return new Date(lastAccess.getTime() + (SESSION_TIMEOUT_MINUTES * 60 * 1000));
    }

    // ========== Encryption Helpers ==========

    /**
     * Encrypt sensitive data
     */
    public static String encryptSensitiveData(String data, String key) {
        if (StringUtils.isBlank(data)) {
            return data;
        }
        return EncryptionUtils.encrypt(data, key);
    }

    /**
     * Decrypt sensitive data
     */
    public static String decryptSensitiveData(String encryptedData, String key) {
        if (StringUtils.isBlank(encryptedData)) {
            return encryptedData;
        }
        return EncryptionUtils.decrypt(encryptedData, key);
    }

    // ========== Audit Logging ==========

    /**
     * Create audit log entry
     */
    public static AuditLogEntry createAuditLog(String action, String details) {
        return AuditLogEntry.builder()
                .username(getCurrentUsername().orElse("anonymous"))
                .action(action)
                .details(details)
                .timestamp(new Date())
                .ipAddress(null)
                .build();
    }

    /**
     * Create audit log with IP
     */
    public static AuditLogEntry createAuditLog(String action, String details, String ipAddress) {
        return AuditLogEntry.builder()
                .username(getCurrentUsername().orElse("anonymous"))
                .action(action)
                .details(details)
                .timestamp(new Date())
                .ipAddress(ipAddress)
                .build();
    }

    // ========== Utility Classes ==========

    /**
     * Rate limit information
     */
    private static class RateLimitInfo {
        private int attempts;
        private long firstAttempt;

        public RateLimitInfo(int attempts, long firstAttempt) {
            this.attempts = attempts;
            this.firstAttempt = firstAttempt;
        }

        public int getAttempts() {
            return attempts;
        }

        public void setAttempts(int attempts) {
            this.attempts = attempts;
        }

        public void incrementAttempts() {
            this.attempts++;
        }

        public long getFirstAttempt() {
            return firstAttempt;
        }

        public void setFirstAttempt(long firstAttempt) {
            this.firstAttempt = firstAttempt;
        }
    }

    /**
     * User principal interface
     */
    public interface UserPrincipal {
        Long getId();
        String getUsername();
        String getEmail();
    }

    /**
     * Audit log entry
     */
    @lombok.Builder
    @lombok.Data
    public static class AuditLogEntry {
        private String username;
        private String action;
        private String details;
        private Date timestamp;
        private String ipAddress;
    }

    /**
     * Custom security exception
     */
    public static class SecurityException extends RuntimeException {
        public SecurityException(String message) {
            super(message);
        }

        public SecurityException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}