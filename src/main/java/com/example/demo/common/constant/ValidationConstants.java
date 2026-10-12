package com.example.demo.common.constant;

/**
 * Validation-related constants
 *
 * Defines validation rules, error messages, regex patterns,
 * and validation constraints used across the application
 *
 * @author Architecture Team
 * @version 1.0
 */
public final class ValidationConstants {

    private ValidationConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    // ========== Length Constraints ==========

    /**
     * General string length limits
     */
    public static final int MIN_STRING_LENGTH = 1;
    public static final int MAX_STRING_LENGTH = 255;
    public static final int MAX_TEXT_LENGTH = 5000;
    public static final int MAX_LONG_TEXT_LENGTH = 65535;

    /**
     * Name field lengths
     */
    public static final int MIN_NAME_LENGTH = 2;
    public static final int MAX_NAME_LENGTH = 50;
    public static final int MAX_FULL_NAME_LENGTH = 100;

    /**
     * Username constraints
     */
    public static final int MIN_USERNAME_LENGTH = 3;
    public static final int MAX_USERNAME_LENGTH = 20;

    /**
     * Email constraints
     */
    public static final int MAX_EMAIL_LENGTH = 100;

    /**
     * Password constraints
     */
    public static final int MIN_PASSWORD_LENGTH = 8;
    public static final int MAX_PASSWORD_LENGTH = 128;

    /**
     * Phone constraints
     */
    public static final int MIN_PHONE_LENGTH = 10;
    public static final int MAX_PHONE_LENGTH = 15;

    /**
     * Address field lengths
     */
    public static final int MAX_ADDRESS_LENGTH = 200;
    public static final int MAX_CITY_LENGTH = 50;
    public static final int MAX_STATE_LENGTH = 50;
    public static final int MAX_COUNTRY_LENGTH = 50;
    public static final int MAX_ZIP_CODE_LENGTH = 10;

    /**
     * Description field lengths
     */
    public static final int MAX_SHORT_DESCRIPTION_LENGTH = 500;
    public static final int MAX_DESCRIPTION_LENGTH = 2000;

    /**
     * Title field lengths
     */
    public static final int MIN_TITLE_LENGTH = 3;
    public static final int MAX_TITLE_LENGTH = 100;

    /**
     * Code/ID field lengths
     */
    public static final int MAX_CODE_LENGTH = 20;
    public static final int MAX_UUID_LENGTH = 36;

    // ========== Numeric Constraints ==========

    /**
     * ID constraints
     */
    public static final long MIN_ID = 1L;
    public static final long MAX_ID = Long.MAX_VALUE;

    /**
     * Age constraints
     */
    public static final int MIN_AGE = 0;
    public static final int MAX_AGE = 150;
    public static final int MIN_ADULT_AGE = 18;

    /**
     * Quantity constraints
     */
    public static final int MIN_QUANTITY = 0;
    public static final int MAX_QUANTITY = 999999;

    /**
     * Price constraints
     */
    public static final double MIN_PRICE = 0.0;
    public static final double MAX_PRICE = 999999999.99;

    /**
     * Percentage constraints
     */
    public static final double MIN_PERCENTAGE = 0.0;
    public static final double MAX_PERCENTAGE = 100.0;

    /**
     * Rating constraints
     */
    public static final int MIN_RATING = 1;
    public static final int MAX_RATING = 5;

    /**
     * Priority constraints
     */
    public static final int MIN_PRIORITY = 1;
    public static final int MAX_PRIORITY = 10;

    // ========== Collection Constraints ==========

    /**
     * List size limits
     */
    public static final int MIN_LIST_SIZE = 0;
    public static final int MAX_LIST_SIZE = 1000;

    /**
     * Pagination limits
     */
    public static final int MIN_PAGE_SIZE = 1;
    public static final int MAX_PAGE_SIZE = 100;
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MIN_PAGE_NUMBER = 0;

    /**
     * Batch size limits
     */
    public static final int MIN_BATCH_SIZE = 1;
    public static final int MAX_BATCH_SIZE = 1000;

    // ========== Regex Patterns ==========

    /**
     * Username pattern: alphanumeric, underscore, hyphen
     */
    public static final String REGEX_USERNAME = "^[a-zA-Z0-9_-]{3,20}$";

    /**
     * Email pattern (RFC 5322 simplified)
     */
    public static final String REGEX_EMAIL = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";

    /**
     * Password pattern: min 8 chars, 1 upper, 1 lower, 1 digit, 1 special
     */
    public static final String REGEX_PASSWORD = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";

    /**
     * Phone patterns
     */
    public static final String REGEX_PHONE = "^[+]?[(]?[0-9]{1,4}[)]?[-\\s.]?[(]?[0-9]{1,4}[)]?[-\\s.]?[0-9]{1,9}$";
    public static final String REGEX_US_PHONE = "^\\d{10}$|^\\d{3}-\\d{3}-\\d{4}$|^\\(\\d{3}\\)\\s?\\d{3}-\\d{4}$";

    /**
     * Alphanumeric patterns
     */
    public static final String REGEX_ALPHANUMERIC = "^[a-zA-Z0-9]+$";
    public static final String REGEX_ALPHABETIC = "^[a-zA-Z]+$";
    public static final String REGEX_NUMERIC = "^[0-9]+$";

    /**
     * URL pattern
     */
    public static final String REGEX_URL = "^(https?|ftp)://[^\\s/$.?#].[^\\s]*$";

    /**
     * UUID pattern
     */
    public static final String REGEX_UUID = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";

    /**
     * IPv4 pattern
     */
    public static final String REGEX_IPV4 = "^((25[0-5]|(2[0-4]|1\\d|[1-9]|)\\d)\\.?\\b){4}$";

    /**
     * Slug pattern (URL-friendly)
     */
    public static final String REGEX_SLUG = "^[a-z0-9]+(?:-[a-z0-9]+)*$";

    /**
     * Hex color pattern
     */
    public static final String REGEX_HEX_COLOR = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$";

    /**
     * Credit card pattern (basic)
     */
    public static final String REGEX_CREDIT_CARD = "^(?:4[0-9]{12}(?:[0-9]{3})?|5[1-5][0-9]{14}|3[47][0-9]{13})$";

    /**
     * SSN pattern
     */
    public static final String REGEX_SSN = "^\\d{3}-\\d{2}-\\d{4}$";

    /**
     * ZIP code patterns
     */
    public static final String REGEX_ZIP_CODE = "^\\d{5}(-\\d{4})?$";
    public static final String REGEX_POSTAL_CODE = "^[A-Z0-9]{3,10}$";

    /**
     * Date patterns
     */
    public static final String REGEX_DATE_ISO = "^\\d{4}-\\d{2}-\\d{2}$";
    public static final String REGEX_DATE_US = "^\\d{2}/\\d{2}/\\d{4}$";
    public static final String REGEX_DATETIME_ISO = "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}$";

    /**
     * File extension pattern
     */
    public static final String REGEX_FILE_EXTENSION = "^[a-zA-Z0-9]+$";

    // ========== Validation Messages ==========

    /**
     * Required field messages
     */
    public static final String MSG_REQUIRED = "This field is required";
    public static final String MSG_NOT_NULL = "Value must not be null";
    public static final String MSG_NOT_EMPTY = "Value must not be empty";
    public static final String MSG_NOT_BLANK = "Value must not be blank";

    /**
     * Length validation messages
     */
    public static final String MSG_MIN_LENGTH = "Minimum length is {min} characters";
    public static final String MSG_MAX_LENGTH = "Maximum length is {max} characters";
    public static final String MSG_LENGTH_RANGE = "Length must be between {min} and {max} characters";

    /**
     * Format validation messages
     */
    public static final String MSG_INVALID_FORMAT = "Invalid format";
    public static final String MSG_INVALID_EMAIL = "Invalid email address";
    public static final String MSG_INVALID_PHONE = "Invalid phone number";
    public static final String MSG_INVALID_URL = "Invalid URL";
    public static final String MSG_INVALID_UUID = "Invalid UUID format";
    public static final String MSG_INVALID_DATE = "Invalid date format";

    /**
     * Numeric validation messages
     */
    public static final String MSG_MIN_VALUE = "Minimum value is {min}";
    public static final String MSG_MAX_VALUE = "Maximum value is {max}";
    public static final String MSG_VALUE_RANGE = "Value must be between {min} and {max}";
    public static final String MSG_POSITIVE = "Value must be positive";
    public static final String MSG_NEGATIVE = "Value must be negative";
    public static final String MSG_NON_NEGATIVE = "Value must be non-negative";

    /**
     * Type validation messages
     */
    public static final String MSG_MUST_BE_NUMERIC = "Value must be numeric";
    public static final String MSG_MUST_BE_ALPHANUMERIC = "Value must be alphanumeric";
    public static final String MSG_MUST_BE_ALPHABETIC = "Value must contain only letters";

    /**
     * Business rule messages
     */
    public static final String MSG_ALREADY_EXISTS = "Record already exists";
    public static final String MSG_NOT_FOUND = "Record not found";
    public static final String MSG_DUPLICATE_ENTRY = "Duplicate entry";
    public static final String MSG_INVALID_CREDENTIALS = "Invalid credentials";
    public static final String MSG_UNAUTHORIZED = "Unauthorized access";
    public static final String MSG_FORBIDDEN = "Access forbidden";

    /**
     * Password validation messages
     */
    public static final String MSG_PASSWORD_TOO_SHORT = "Password must be at least 8 characters";
    public static final String MSG_PASSWORD_TOO_LONG = "Password must not exceed 128 characters";
    public static final String MSG_PASSWORD_WEAK = "Password is too weak";
    public static final String MSG_PASSWORD_MISMATCH = "Passwords do not match";
    public static final String MSG_PASSWORD_INVALID = "Password must contain uppercase, lowercase, digit, and special character";

    /**
     * Collection validation messages
     */
    public static final String MSG_LIST_EMPTY = "List must not be empty";
    public static final String MSG_LIST_TOO_LARGE = "List exceeds maximum size of {max}";
    public static final String MSG_DUPLICATE_VALUES = "List contains duplicate values";

    /**
     * Date/Time validation messages
     */
    public static final String MSG_PAST_DATE = "Date must be in the past";
    public static final String MSG_FUTURE_DATE = "Date must be in the future";
    public static final String MSG_DATE_RANGE = "Date must be between {start} and {end}";
    public static final String MSG_INVALID_AGE = "Invalid age";
    public static final String MSG_UNDERAGE = "Must be at least 18 years old";

    /**
     * File validation messages
     */
    public static final String MSG_FILE_TOO_LARGE = "File size exceeds maximum of {max}";
    public static final String MSG_INVALID_FILE_TYPE = "Invalid file type";
    public static final String MSG_FILE_UPLOAD_FAILED = "File upload failed";

    // ========== Field Names (for error mapping) ==========

    public static final String FIELD_ID = "id";
    public static final String FIELD_NAME = "name";
    public static final String FIELD_EMAIL = "email";
    public static final String FIELD_USERNAME = "username";
    public static final String FIELD_PASSWORD = "password";
    public static final String FIELD_PHONE = "phone";
    public static final String FIELD_ADDRESS = "address";
    public static final String FIELD_DATE = "date";
    public static final String FIELD_CREATED_AT = "createdAt";
    public static final String FIELD_UPDATED_AT = "updatedAt";

    // ========== Status Values ==========

    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_INACTIVE = "INACTIVE";
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_APPROVED = "APPROVED";
    public static final String STATUS_REJECTED = "REJECTED";
    public static final String STATUS_DELETED = "DELETED";

    // ========== Validation Groups ==========

    /**
     * Marker interfaces for validation groups
     */
    public interface Create {}
    public interface Update {}
    public interface Delete {}
    public interface Search {}
    public interface Import {}
    public interface Export {}
}