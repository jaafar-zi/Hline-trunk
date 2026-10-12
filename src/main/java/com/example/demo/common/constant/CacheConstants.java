package com.example.demo.common.constant;

/**
 * Cache-related constants
 *
 * Defines cache names, TTLs, and cache configuration constants
 * Used with Spring Cache, Redis, or other caching mechanisms
 *
 * @author Architecture Team
 * @version 1.0
 */
public final class CacheConstants {

    private CacheConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    // ========== Cache Names ==========

    /**
     * User-related caches
     */
    public static final String CACHE_USER = "users";
    public static final String CACHE_USER_DETAILS = "userDetails";
    public static final String CACHE_USER_PROFILE = "userProfiles";
    public static final String CACHE_USER_PERMISSIONS = "userPermissions";
    public static final String CACHE_USER_ROLES = "userRoles";
    public static final String CACHE_USER_PREFERENCES = "userPreferences";

    /**
     * Authentication and session caches
     */
    public static final String CACHE_AUTH_TOKEN = "authTokens";
    public static final String CACHE_REFRESH_TOKEN = "refreshTokens";
    public static final String CACHE_SESSION = "sessions";
    public static final String CACHE_LOGIN_ATTEMPTS = "loginAttempts";
    public static final String CACHE_OTP = "otpCodes";
    public static final String CACHE_PASSWORD_RESET = "passwordResets";

    /**
     * Configuration caches
     */
    public static final String CACHE_CONFIG = "configurations";
    public static final String CACHE_SETTINGS = "settings";
    public static final String CACHE_FEATURE_FLAGS = "featureFlags";
    public static final String CACHE_API_KEYS = "apiKeys";

    /**
     * Business entity caches
     */
    public static final String CACHE_PRODUCT = "products";
    public static final String CACHE_CATEGORY = "categories";
    public static final String CACHE_DEPARTMENT = "departments";
    public static final String CACHE_ORGANIZATION = "organizations";
    public static final String CACHE_CUSTOMER = "customers";
    public static final String CACHE_ORDER = "orders";

    /**
     * Lookup and reference data caches
     */
    public static final String CACHE_LOOKUP = "lookups";
    public static final String CACHE_COUNTRY = "countries";
    public static final String CACHE_CURRENCY = "currencies";
    public static final String CACHE_TIMEZONE = "timezones";
    public static final String CACHE_LANGUAGE = "languages";

    /**
     * Report and analytics caches
     */
    public static final String CACHE_REPORT = "reports";
    public static final String CACHE_DASHBOARD = "dashboards";
    public static final String CACHE_METRICS = "metrics";
    public static final String CACHE_STATISTICS = "statistics";

    /**
     * API and external service caches
     */
    public static final String CACHE_API_RESPONSE = "apiResponses";
    public static final String CACHE_EXTERNAL_DATA = "externalData";
    public static final String CACHE_RATE_LIMIT = "rateLimits";

    // ========== Cache TTL (Time To Live) in seconds ==========

    /**
     * Short-term cache (1-5 minutes)
     */
    public static final long TTL_ONE_MINUTE = 60L;
    public static final long TTL_TWO_MINUTES = 120L;
    public static final long TTL_FIVE_MINUTES = 300L;

    /**
     * Medium-term cache (10-30 minutes)
     */
    public static final long TTL_TEN_MINUTES = 600L;
    public static final long TTL_FIFTEEN_MINUTES = 900L;
    public static final long TTL_THIRTY_MINUTES = 1800L;

    /**
     * Long-term cache (1-24 hours)
     */
    public static final long TTL_ONE_HOUR = 3600L;
    public static final long TTL_TWO_HOURS = 7200L;
    public static final long TTL_SIX_HOURS = 21600L;
    public static final long TTL_TWELVE_HOURS = 43200L;
    public static final long TTL_ONE_DAY = 86400L;

    /**
     * Extended cache (multiple days)
     */
    public static final long TTL_ONE_WEEK = 604800L;
    public static final long TTL_ONE_MONTH = 2592000L;

    /**
     * Specific entity TTLs
     */
    public static final long TTL_USER = TTL_FIFTEEN_MINUTES;
    public static final long TTL_AUTH_TOKEN = TTL_ONE_HOUR;
    public static final long TTL_REFRESH_TOKEN = TTL_ONE_DAY;
    public static final long TTL_SESSION = TTL_THIRTY_MINUTES;
    public static final long TTL_OTP = TTL_FIVE_MINUTES;
    public static final long TTL_CONFIG = TTL_ONE_HOUR;
    public static final long TTL_LOOKUP = TTL_ONE_DAY;
    public static final long TTL_API_RESPONSE = TTL_FIVE_MINUTES;
    public static final long TTL_RATE_LIMIT = TTL_ONE_MINUTE;

    // ========== Cache Sizes ==========

    /**
     * Maximum cache sizes (number of entries)
     */
    public static final int MAX_SIZE_SMALL = 100;
    public static final int MAX_SIZE_MEDIUM = 1000;
    public static final int MAX_SIZE_LARGE = 10000;
    public static final int MAX_SIZE_XLARGE = 100000;

    /**
     * Specific cache sizes
     */
    public static final int MAX_SIZE_USER = MAX_SIZE_LARGE;
    public static final int MAX_SIZE_AUTH_TOKEN = MAX_SIZE_XLARGE;
    public static final int MAX_SIZE_SESSION = MAX_SIZE_LARGE;
    public static final int MAX_SIZE_CONFIG = MAX_SIZE_SMALL;
    public static final int MAX_SIZE_LOOKUP = MAX_SIZE_MEDIUM;

    // ========== Cache Key Prefixes ==========

    public static final String KEY_PREFIX_USER = "user:";
    public static final String KEY_PREFIX_AUTH = "auth:";
    public static final String KEY_PREFIX_SESSION = "session:";
    public static final String KEY_PREFIX_OTP = "otp:";
    public static final String KEY_PREFIX_CONFIG = "config:";
    public static final String KEY_PREFIX_RATE_LIMIT = "ratelimit:";
    public static final String KEY_PREFIX_LOCK = "lock:";

    // ========== Cache Key Separators ==========

    public static final String KEY_SEPARATOR = ":";
    public static final String KEY_WILDCARD = "*";

    // ========== Cache Flags ==========

    /**
     * Cache control flags
     */
    public static final boolean CACHE_NULL_VALUES = false;
    public static final boolean CACHE_STATISTICS_ENABLED = true;
    public static final boolean CACHE_ALLOW_COMPRESSION = true;

    // ========== Redis-specific Constants ==========

    /**
     * Redis database indexes
     */
    public static final int REDIS_DB_CACHE = 0;
    public static final int REDIS_DB_SESSION = 1;
    public static final int REDIS_DB_RATE_LIMIT = 2;
    public static final int REDIS_DB_LOCK = 3;

    /**
     * Redis connection pool settings
     */
    public static final int REDIS_POOL_MAX_TOTAL = 128;
    public static final int REDIS_POOL_MAX_IDLE = 64;
    public static final int REDIS_POOL_MIN_IDLE = 16;
    public static final long REDIS_POOL_MAX_WAIT_MILLIS = 3000L;

    /**
     * Redis timeout settings (milliseconds)
     */
    public static final int REDIS_CONNECT_TIMEOUT = 5000;
    public static final int REDIS_READ_TIMEOUT = 3000;

    // ========== Caffeine Cache Settings ==========

    /**
     * Local cache settings (when using Caffeine)
     */
    public static final int CAFFEINE_INITIAL_CAPACITY = 100;
    public static final int CAFFEINE_MAX_SIZE = 10000;
    public static final long CAFFEINE_EXPIRE_AFTER_WRITE = TTL_FIVE_MINUTES;
    public static final long CAFFEINE_EXPIRE_AFTER_ACCESS = TTL_TEN_MINUTES;

    // ========== Cache Patterns ==========

    /**
     * Common cache key patterns
     */
    public static final String PATTERN_USER_BY_ID = KEY_PREFIX_USER + "id:%d";
    public static final String PATTERN_USER_BY_USERNAME = KEY_PREFIX_USER + "username:%s";
    public static final String PATTERN_USER_BY_EMAIL = KEY_PREFIX_USER + "email:%s";
    public static final String PATTERN_SESSION_BY_ID = KEY_PREFIX_SESSION + "%s";
    public static final String PATTERN_OTP_BY_USER = KEY_PREFIX_OTP + "user:%d";
    public static final String PATTERN_RATE_LIMIT = KEY_PREFIX_RATE_LIMIT + "%s:%s";

    // ========== Helper Methods ==========

    /**
     * Generate user cache key by ID
     */
    public static String userKeyById(Long userId) {
        return String.format(PATTERN_USER_BY_ID, userId);
    }

    /**
     * Generate user cache key by username
     */
    public static String userKeyByUsername(String username) {
        return String.format(PATTERN_USER_BY_USERNAME, username);
    }

    /**
     * Generate user cache key by email
     */
    public static String userKeyByEmail(String email) {
        return String.format(PATTERN_USER_BY_EMAIL, email);
    }

    /**
     * Generate session cache key
     */
    public static String sessionKey(String sessionId) {
        return String.format(PATTERN_SESSION_BY_ID, sessionId);
    }

    /**
     * Generate OTP cache key
     */
    public static String otpKey(Long userId) {
        return String.format(PATTERN_OTP_BY_USER, userId);
    }

    /**
     * Generate rate limit cache key
     */
    public static String rateLimitKey(String identifier, String action) {
        return String.format(PATTERN_RATE_LIMIT, identifier, action);
    }

    /**
     * Build composite cache key
     */
    public static String buildKey(String... parts) {
        return String.join(KEY_SEPARATOR, parts);
    }
}