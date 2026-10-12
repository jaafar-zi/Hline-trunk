package com.example.demo.common.exception;

/**
 * Standard error codes for the application
 * Used for client-side error handling
 *
 * @author Architecture Team
 * @version 1.0
 */
public final class ErrorCode {

    // General Errors (1000-1999)
    public static final String INTERNAL_ERROR = "ERR_1000";
    public static final String BUSINESS_ERROR = "ERR_1001";
    public static final String UNKNOWN_ERROR = "ERR_1099";

    // Validation Errors (2000-2999)
    public static final String VALIDATION_ERROR = "ERR_2000";
    public static final String BAD_REQUEST = "ERR_2001";
    public static final String MISSING_PARAMETER = "ERR_2002";
    public static final String INVALID_FORMAT = "ERR_2003";
    public static final String CONSTRAINT_VIOLATION = "ERR_2004";

    // Authentication & Authorization Errors (3000-3999)
    public static final String UNAUTHORIZED = "ERR_3000";
    public static final String FORBIDDEN = "ERR_3001";
    public static final String INVALID_CREDENTIALS = "ERR_3002";
    public static final String TOKEN_EXPIRED = "ERR_3003";
    public static final String INVALID_TOKEN = "ERR_3004";
    public static final String INSUFFICIENT_PERMISSIONS = "ERR_3005";

    // Resource Errors (4000-4999)
    public static final String ENTITY_NOT_FOUND = "ERR_4000";
    public static final String RESOURCE_NOT_FOUND = "ERR_4001";
    public static final String DUPLICATE_ENTITY = "ERR_4002";
    public static final String RESOURCE_ALREADY_EXISTS = "ERR_4003";

    // State & Operation Errors (5000-5999)
    public static final String INVALID_OPERATION = "ERR_5000";
    public static final String CONCURRENT_MODIFICATION = "ERR_5001";
    public static final String OPERATION_NOT_ALLOWED = "ERR_5002";
    public static final String INVALID_STATE = "ERR_5003";

    // External Service Errors (6000-6999)
    public static final String EXTERNAL_SERVICE_ERROR = "ERR_6000";
    public static final String SERVICE_UNAVAILABLE = "ERR_6001";
    public static final String SERVICE_TIMEOUT = "ERR_6002";
    public static final String INVALID_RESPONSE = "ERR_6003";

    // Database Errors (7000-7999)
    public static final String DATABASE_ERROR = "ERR_7000";
    public static final String DATA_INTEGRITY_VIOLATION = "ERR_7001";
    public static final String OPTIMISTIC_LOCK_ERROR = "ERR_7002";

    // File & Upload Errors (8000-8999)
    public static final String FILE_NOT_FOUND = "ERR_8000";
    public static final String FILE_UPLOAD_ERROR = "ERR_8001";
    public static final String INVALID_FILE_TYPE = "ERR_8002";
    public static final String FILE_TOO_LARGE = "ERR_8003";

    // Rate Limiting & Throttling (9000-9999)
    public static final String RATE_LIMIT_EXCEEDED = "ERR_9000";
    public static final String TOO_MANY_REQUESTS = "ERR_9001";

    private ErrorCode() {
        // Prevent instantiation
    }

    /**
     * Check if error code is client error (4xx)
     */
    public static boolean isClientError(String errorCode) {
        if (errorCode == null) return false;
        String numericPart = errorCode.replaceAll("[^0-9]", "");
        if (numericPart.isEmpty()) return false;
        int code = Integer.parseInt(numericPart);
        return code >= 2000 && code < 6000;
    }

    /**
     * Check if error code is server error (5xx)
     */
    public static boolean isServerError(String errorCode) {
        if (errorCode == null) return false;
        String numericPart = errorCode.replaceAll("[^0-9]", "");
        if (numericPart.isEmpty()) return false;
        int code = Integer.parseInt(numericPart);
        return code >= 1000 && code < 2000 || code >= 6000;
    }
}