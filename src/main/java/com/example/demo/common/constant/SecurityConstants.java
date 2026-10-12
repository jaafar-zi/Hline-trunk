package com.example.demo.common.constant;

/**
 * Security-related constants
 *
 * Defines security configuration, JWT settings, encryption parameters,
 * and authentication/authorization constants
 *
 * @author Architecture Team
 * @version 1.0
 */
public final class SecurityConstants {

    private SecurityConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    // ========== JWT Token Settings ==========

    /**
     * JWT header names
     */
    public static final String JWT_HEADER_NAME = "Authorization";
    public static final String JWT_HEADER_PREFIX = "Bearer ";
    public static final String JWT_REFRESH_HEADER = "X-Refresh-Token";

    /**
     * JWT claim names
     */
    public static final String JWT_CLAIM_USER_ID = "userId";
    public static final String JWT_CLAIM_USERNAME = "username";
    public static final String JWT_CLAIM_EMAIL = "email";
    public static final String JWT_CLAIM_ROLES = "roles";
    public static final String JWT_CLAIM_AUTHORITIES = "authorities";
    public static final String JWT_CLAIM_TOKEN_TYPE = "tokenType";
    public static final String JWT_CLAIM_TENANT_ID = "tenantId";
    public static final String JWT_CLAIM_SESSION_ID = "sessionId";
    public static final String JWT_CLAIM_IP_ADDRESS = "ipAddress";
    public static final String JWT_CLAIM_DEVICE_ID = "deviceId";

    /**
     * JWT token types
     */
    public static final String TOKEN_TYPE_ACCESS = "ACCESS";
    public static final String TOKEN_TYPE_REFRESH = "REFRESH";
    public static final String TOKEN_TYPE_RESET = "RESET";
    public static final String TOKEN_TYPE_VERIFICATION = "VERIFICATION";

    /**
     * JWT expiration times (in seconds)
     */
    public static final long JWT_ACCESS_TOKEN_VALIDITY = 3600L; // 1 hour
    public static final long JWT_REFRESH_TOKEN_VALIDITY = 86400L * 7; // 7 days
    public static final long JWT_RESET_TOKEN_VALIDITY = 3600L; // 1 hour
    public static final long JWT_VERIFICATION_TOKEN_VALIDITY = 86400L; // 24 hours

    /**
     * JWT signing algorithm
     */
    public static final String JWT_ALGORITHM = "HS512";

    // ========== Password Settings ==========

    /**
     * Password requirements
     */
    public static final int PASSWORD_MIN_LENGTH = 8;
    public static final int PASSWORD_MAX_LENGTH = 128;
    public static final boolean PASSWORD_REQUIRE_UPPERCASE = true;
    public static final boolean PASSWORD_REQUIRE_LOWERCASE = true;
    public static final boolean PASSWORD_REQUIRE_DIGIT = true;
    public static final boolean PASSWORD_REQUIRE_SPECIAL_CHAR = true;

    /**
     * Password encoding
     */
    public static final int BCRYPT_STRENGTH = 12;
    public static final int PBKDF2_ITERATIONS = 65536;
    public static final int PBKDF2_KEY_LENGTH = 256;

    /**
     * Password history and expiration
     */
    public static final int PASSWORD_HISTORY_COUNT = 5;
    public static final int PASSWORD_EXPIRY_DAYS = 90;
    public static final int PASSWORD_RESET_LINK_EXPIRY_HOURS = 24;

    // ========== Authentication Settings ==========

    /**
     * Login attempt limits
     */
    public static final int MAX_LOGIN_ATTEMPTS = 5;
    public static final long LOGIN_ATTEMPT_RESET_TIME = 900L; // 15 minutes
    public static final long ACCOUNT_LOCK_DURATION = 1800L; // 30 minutes

    /**
     * Session settings
     */
    public static final int SESSION_TIMEOUT_MINUTES = 30;
    public static final int MAX_CONCURRENT_SESSIONS = 3;
    public static final boolean SESSION_INVALIDATE_ON_LOGOUT = true;

    /**
     * OTP settings
     */
    public static final int OTP_LENGTH = 6;
    public static final long OTP_VALIDITY_SECONDS = 300L; // 5 minutes
    public static final int OTP_MAX_ATTEMPTS = 3;

    /**
     * Two-Factor Authentication (2FA)
     */
    public static final boolean TWO_FA_ENABLED = false;
    public static final int TWO_FA_CODE_LENGTH = 6;
    public static final long TWO_FA_VALIDITY_SECONDS = 300L; // 5 minutes

    // ========== Authorization Settings ==========

    /**
     * Role names
     */
    public static final String ROLE_PREFIX = "ROLE_";
    public static final String ROLE_USER = "ROLE_USER";
    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    public static final String ROLE_SUPER_ADMIN = "ROLE_SUPER_ADMIN";
    public static final String ROLE_MODERATOR = "ROLE_MODERATOR";
    public static final String ROLE_GUEST = "ROLE_GUEST";
    public static final String ROLE_SYSTEM = "ROLE_SYSTEM";
    public static final String ROLE_API = "ROLE_API";

    /**
     * Authority/Permission names
     */
    public static final String AUTHORITY_PREFIX = "PERM_";
    public static final String AUTHORITY_READ = "PERM_READ";
    public static final String AUTHORITY_WRITE = "PERM_WRITE";
    public static final String AUTHORITY_UPDATE = "PERM_UPDATE";
    public static final String AUTHORITY_DELETE = "PERM_DELETE";
    public static final String AUTHORITY_ADMIN = "PERM_ADMIN";

    // ========== Encryption Settings ==========

    /**
     * Encryption algorithms
     */
    public static final String ENCRYPTION_ALGORITHM_AES = "AES";
    public static final String ENCRYPTION_ALGORITHM_RSA = "RSA";
    public static final String ENCRYPTION_MODE_GCM = "AES/GCM/NoPadding";
    public static final String ENCRYPTION_MODE_CBC = "AES/CBC/PKCS5Padding";

    /**
     * Key sizes
     */
    public static final int AES_KEY_SIZE = 256;
    public static final int RSA_KEY_SIZE = 2048;
    public static final int IV_SIZE = 16;
    public static final int SALT_SIZE = 16;

    /**
     * Hash algorithms
     */
    public static final String HASH_ALGORITHM_MD5 = "MD5";
    public static final String HASH_ALGORITHM_SHA256 = "SHA-256";
    public static final String HASH_ALGORITHM_SHA512 = "SHA-512";

    // ========== API Security ==========

    /**
     * API key settings
     */
    public static final String API_KEY_HEADER = "X-API-Key";
    public static final String API_SECRET_HEADER = "X-API-Secret";
    public static final int API_KEY_LENGTH = 32;
    public static final String API_KEY_PREFIX = "sk_";

    /**
     * Rate limiting
     */
    public static final int RATE_LIMIT_REQUESTS = 100;
    public static final long RATE_LIMIT_WINDOW_SECONDS = 60L;
    public static final int RATE_LIMIT_REQUESTS_ANONYMOUS = 20;
    public static final int RATE_LIMIT_REQUESTS_AUTHENTICATED = 100;
    public static final int RATE_LIMIT_REQUESTS_PREMIUM = 1000;

    /**
     * CORS settings
     */
    public static final String[] CORS_ALLOWED_ORIGINS = {"*"};
    public static final String[] CORS_ALLOWED_METHODS = {"GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"};
    public static final String[] CORS_ALLOWED_HEADERS = {"*"};
    public static final String[] CORS_EXPOSED_HEADERS = {"Authorization", "X-Total-Count", "X-Page-Number"};
    public static final long CORS_MAX_AGE = 3600L;

    // ========== Security Headers ==========

    /**
     * HTTP Security headers
     */
    public static final String HEADER_X_FRAME_OPTIONS = "X-Frame-Options";
    public static final String HEADER_X_CONTENT_TYPE_OPTIONS = "X-Content-Type-Options";
    public static final String HEADER_X_XSS_PROTECTION = "X-XSS-Protection";
    public static final String HEADER_STRICT_TRANSPORT_SECURITY = "Strict-Transport-Security";
    public static final String HEADER_CONTENT_SECURITY_POLICY = "Content-Security-Policy";
    public static final String HEADER_REFERRER_POLICY = "Referrer-Policy";

    /**
     * Security header values
     */
    public static final String X_FRAME_OPTIONS_VALUE = "DENY";
    public static final String X_CONTENT_TYPE_OPTIONS_VALUE = "nosniff";
    public static final String X_XSS_PROTECTION_VALUE = "1; mode=block";
    public static final String HSTS_VALUE = "max-age=31536000; includeSubDomains";
    public static final String CSP_VALUE = "default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline'";
    public static final String REFERRER_POLICY_VALUE = "no-referrer-when-downgrade";

    // ========== CSRF Protection ==========

    /**
     * CSRF settings
     */
    public static final String CSRF_TOKEN_HEADER = "X-CSRF-TOKEN";
    public static final String CSRF_TOKEN_PARAMETER = "_csrf";
    public static final String CSRF_COOKIE_NAME = "XSRF-TOKEN";
    public static final int CSRF_TOKEN_LENGTH = 32;

    // ========== IP Security ==========

    /**
     * IP whitelist/blacklist
     */
    public static final boolean IP_WHITELIST_ENABLED = false;
    public static final boolean IP_BLACKLIST_ENABLED = true;
    public static final String[] TRUSTED_IP_RANGES = {"127.0.0.1", "10.0.0.0/8", "172.16.0.0/12", "192.168.0.0/16"};

    // ========== Audit & Logging ==========

    /**
     * Audit event types
     */
    public static final String AUDIT_LOGIN_SUCCESS = "LOGIN_SUCCESS";
    public static final String AUDIT_LOGIN_FAILURE = "LOGIN_FAILURE";
    public static final String AUDIT_LOGOUT = "LOGOUT";
    public static final String AUDIT_PASSWORD_CHANGE = "PASSWORD_CHANGE";
    public static final String AUDIT_PASSWORD_RESET = "PASSWORD_RESET";
    public static final String AUDIT_ACCOUNT_CREATED = "ACCOUNT_CREATED";
    public static final String AUDIT_ACCOUNT_UPDATED = "ACCOUNT_UPDATED";
    public static final String AUDIT_ACCOUNT_DELETED = "ACCOUNT_DELETED";
    public static final String AUDIT_ACCOUNT_LOCKED = "ACCOUNT_LOCKED";
    public static final String AUDIT_ACCOUNT_UNLOCKED = "ACCOUNT_UNLOCKED";
    public static final String AUDIT_PERMISSION_GRANTED = "PERMISSION_GRANTED";
    public static final String AUDIT_PERMISSION_REVOKED = "PERMISSION_REVOKED";
    public static final String AUDIT_UNAUTHORIZED_ACCESS = "UNAUTHORIZED_ACCESS";

    /**
     * Security log levels
     */
    public static final String LOG_LEVEL_SECURITY = "SECURITY";
    public static final String LOG_LEVEL_AUTHENTICATION = "AUTHENTICATION";
    public static final String LOG_LEVEL_AUTHORIZATION = "AUTHORIZATION";

    // ========== OAuth2 Settings ==========

    /**
     * OAuth2 grant types
     */
    public static final String GRANT_TYPE_PASSWORD = "password";
    public static final String GRANT_TYPE_REFRESH_TOKEN = "refresh_token";
    public static final String GRANT_TYPE_AUTHORIZATION_CODE = "authorization_code";
    public static final String GRANT_TYPE_CLIENT_CREDENTIALS = "client_credentials";

    /**
     * OAuth2 token types
     */
    public static final String OAUTH2_TOKEN_TYPE_BEARER = "Bearer";

    /**
     * OAuth2 scopes
     */
    public static final String SCOPE_READ = "read";
    public static final String SCOPE_WRITE = "write";
    public static final String SCOPE_ADMIN = "admin";
    public static final String SCOPE_TRUST = "trust";

    // ========== Request/Response Security ==========

    /**
     * Request size limits
     */
    public static final long MAX_REQUEST_SIZE = 10 * 1024 * 1024; // 10MB
    public static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    public static final int MAX_FILENAME_LENGTH = 255;

    /**
     * Allowed file types
     */
    public static final String[] ALLOWED_IMAGE_EXTENSIONS = {"jpg", "jpeg", "png", "gif", "webp"};
    public static final String[] ALLOWED_DOCUMENT_EXTENSIONS = {"pdf", "doc", "docx", "xls", "xlsx", "txt"};
    public static final String[] ALLOWED_MIME_TYPES = {
            "image/jpeg", "image/png", "image/gif",
            "application/pdf", "text/plain"
    };

    // ========== Timeout Settings ==========

    /**
     * Connection timeouts (milliseconds)
     */
    public static final int CONNECT_TIMEOUT = 5000;
    public static final int READ_TIMEOUT = 10000;
    public static final int WRITE_TIMEOUT = 10000;

    // ========== Paths & URLs ==========

    /**
     * Public endpoints (no authentication required)
     */
    public static final String[] PUBLIC_ENDPOINTS = {
            "/api/public/**",
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/forgot-password",
            "/api/auth/reset-password",
            "/actuator/health",
            "/swagger-ui/**",
            "/v3/api-docs/**"
    };

    /**
     * Admin-only endpoints
     */
    public static final String[] ADMIN_ENDPOINTS = {
            "/api/admin/**",
            "/api/users/*/delete",
            "/api/system/**"
    };

    // ========== Default Values ==========

    /**
     * Default user settings
     */
    public static final boolean DEFAULT_USER_ENABLED = true;
    public static final boolean DEFAULT_USER_LOCKED = false;
    public static final boolean DEFAULT_CREDENTIALS_EXPIRED = false;

    /**
     * Anonymous user
     */
    public static final String ANONYMOUS_USER = "anonymousUser";
    public static final String SYSTEM_USER = "system";
}