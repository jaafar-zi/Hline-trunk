package com.example.demo.common.util;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Utility class for validation operations
 *
 * Features:
 * - Data type validation
 * - Format validation (email, phone, URL, UUID, etc.)
 * - Range validation
 * - Business rule validation
 * - Bean validation
 * - Custom validators
 * - Bulk validation
 *
 * @author Architecture Team
 * @version 1.0
 */
@Slf4j
@UtilityClass
public class ValidationUtils {

    // ========== Constants ==========

    private static final ValidatorFactory VALIDATOR_FACTORY = Validation.buildDefaultValidatorFactory();
    private static final Validator VALIDATOR = VALIDATOR_FACTORY.getValidator();

    // Regex Patterns
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );

    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^[+]?[(]?[0-9]{1,4}[)]?[-\\s.]?[(]?[0-9]{1,4}[)]?[-\\s.]?[0-9]{1,9}$"
    );

    private static final Pattern UUID_PATTERN = Pattern.compile(
            "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$"
    );

    private static final Pattern IPV4_PATTERN = Pattern.compile(
            "^((25[0-5]|(2[0-4]|1\\d|[1-9]|)\\d)\\.?\\b){4}$"
    );

    private static final Pattern IPV6_PATTERN = Pattern.compile(
            "^(([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,7}:|" +
                    "([0-9a-fA-F]{1,4}:){1,6}:[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,5}" +
                    "(:[0-9a-fA-F]{1,4}){1,2}|([0-9a-fA-F]{1,4}:){1,4}(:[0-9a-fA-F]{1,4}){1,3}|" +
                    "([0-9a-fA-F]{1,4}:){1,3}(:[0-9a-fA-F]{1,4}){1,4}|([0-9a-fA-F]{1,4}:){1,2}" +
                    "(:[0-9a-fA-F]{1,4}){1,5}|[0-9a-fA-F]{1,4}:((:[0-9a-fA-F]{1,4}){1,6})|" +
                    ":((:[0-9a-fA-F]{1,4}){1,7}|:)|fe80:(:[0-9a-fA-F]{0,4}){0,4}%[0-9a-zA-Z]{1,}|" +
                    "::(ffff(:0{1,4}){0,1}:){0,1}((25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])\\.){3}" +
                    "(25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])|([0-9a-fA-F]{1,4}:){1,4}:" +
                    "((25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])\\.){3}(25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9]))$"
    );

    private static final Pattern ALPHANUMERIC_PATTERN = Pattern.compile("^[a-zA-Z0-9]+$");
    private static final Pattern ALPHABETIC_PATTERN = Pattern.compile("^[a-zA-Z]+$");
    private static final Pattern NUMERIC_PATTERN = Pattern.compile("^[0-9]+$");
    private static final Pattern DECIMAL_PATTERN = Pattern.compile("^-?\\d*\\.?\\d+$");
    private static final Pattern HEX_PATTERN = Pattern.compile("^[0-9a-fA-F]+$");

    private static final Pattern CREDIT_CARD_PATTERN = Pattern.compile(
            "^(?:4[0-9]{12}(?:[0-9]{3})?|5[1-5][0-9]{14}|3[47][0-9]{13}|3(?:0[0-5]|[68][0-9])[0-9]{11}|" +
                    "6(?:011|5[0-9]{2})[0-9]{12}|(?:2131|1800|35\\d{3})\\d{11})$"
    );

    private static final Pattern SSN_PATTERN = Pattern.compile("^\\d{3}-\\d{2}-\\d{4}$");
    private static final Pattern ZIP_CODE_PATTERN = Pattern.compile("^\\d{5}(-\\d{4})?$");
    private static final Pattern SLUG_PATTERN = Pattern.compile("^[a-z0-9]+(?:-[a-z0-9]+)*$");

    // Country codes
    private static final Set<String> ISO_COUNTRY_CODES = new HashSet<>(Arrays.asList(Locale.getISOCountries()));

    // Currency codes
    private static final Set<String> ISO_CURRENCY_CODES = Currency.getAvailableCurrencies().stream()
            .map(Currency::getCurrencyCode)
            .collect(Collectors.toSet());

    // ========== Basic Validation ==========

    /**
     * Check if value is null
     */
    public static boolean isNull(Object value) {
        return value == null;
    }

    /**
     * Check if value is not null
     */
    public static boolean isNotNull(Object value) {
        return value != null;
    }

    /**
     * Require non-null value
     */
    public static <T> T requireNonNull(T value, String message) {
        if (value == null) {
            throw new ValidationException(message);
        }
        return value;
    }

    /**
     * Check if all values are non-null
     */
    public static boolean allNonNull(Object... values) {
        if (values == null) {
            return false;
        }
        return Arrays.stream(values).allMatch(Objects::nonNull);
    }

    /**
     * Check if any value is null
     */
    public static boolean anyNull(Object... values) {
        if (values == null) {
            return true;
        }
        return Arrays.stream(values).anyMatch(Objects::isNull);
    }

    // ========== String Validation ==========

    /**
     * Validate string is not blank
     */
    public static boolean isNotBlank(String value) {
        return StringUtils.isNotBlank(value);
    }

    /**
     * Validate string length
     */
    public static boolean hasLength(String value, int min, int max) {
        if (StringUtils.isBlank(value)) {
            return false;
        }
        int length = value.length();
        return length >= min && length <= max;
    }

    /**
     * Validate minimum length
     */
    public static boolean hasMinLength(String value, int minLength) {
        return StringUtils.isNotBlank(value) && value.length() >= minLength;
    }

    /**
     * Validate maximum length
     */
    public static boolean hasMaxLength(String value, int maxLength) {
        return StringUtils.isBlank(value) || value.length() <= maxLength;
    }

    /**
     * Validate exact length
     */
    public static boolean hasExactLength(String value, int length) {
        return StringUtils.isNotBlank(value) && value.length() == length;
    }

    /**
     * Validate string matches pattern
     */
    public static boolean matches(String value, Pattern pattern) {
        if (StringUtils.isBlank(value) || pattern == null) {
            return false;
        }
        return pattern.matcher(value).matches();
    }

    /**
     * Validate string matches regex
     */
    public static boolean matches(String value, String regex) {
        if (StringUtils.isBlank(value) || StringUtils.isBlank(regex)) {
            return false;
        }
        return value.matches(regex);
    }

    // ========== Format Validation ==========

    /**
     * Validate email format
     */
    public static boolean isValidEmail(String email) {
        if (StringUtils.isBlank(email)) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * Validate email with length check
     */
    public static boolean isValidEmail(String email, int maxLength) {
        return isValidEmail(email) && email.length() <= maxLength;
    }

    /**
     * Validate phone number
     */
    public static boolean isValidPhone(String phone) {
        if (StringUtils.isBlank(phone)) {
            return false;
        }
        String normalized = phone.replaceAll("[\\s()-]", "");
        return PHONE_PATTERN.matcher(normalized).matches();
    }

    /**
     * Validate US phone number format
     */
    public static boolean isValidUsPhone(String phone) {
        if (StringUtils.isBlank(phone)) {
            return false;
        }
        String normalized = phone.replaceAll("[\\s()-]", "");
        return normalized.matches("^\\d{10}$") || normalized.matches("^1\\d{10}$");
    }

    /**
     * Validate URL format
     */
    public static boolean isValidUrl(String url) {
        if (StringUtils.isBlank(url)) {
            return false;
        }
        try {
            new URL(url);
            return true;
        } catch (MalformedURLException e) {
            return false;
        }
    }

    /**
     * Validate URL with allowed protocols
     */
    public static boolean isValidUrl(String url, String... allowedProtocols) {
        if (!isValidUrl(url)) {
            return false;
        }
        try {
            URL urlObj = new URL(url);
            String protocol = urlObj.getProtocol().toLowerCase();
            return Arrays.asList(allowedProtocols).contains(protocol);
        } catch (MalformedURLException e) {
            return false;
        }
    }

    /**
     * Validate UUID format
     */
    public static boolean isValidUuid(String uuid) {
        if (StringUtils.isBlank(uuid)) {
            return false;
        }
        return UUID_PATTERN.matcher(uuid).matches();
    }

    /**
     * Validate IPv4 address
     */
    public static boolean isValidIpv4(String ip) {
        if (StringUtils.isBlank(ip)) {
            return false;
        }
        return IPV4_PATTERN.matcher(ip).matches();
    }

    /**
     * Validate IPv6 address
     */
    public static boolean isValidIpv6(String ip) {
        if (StringUtils.isBlank(ip)) {
            return false;
        }
        return IPV6_PATTERN.matcher(ip).matches();
    }

    /**
     * Validate IP address (IPv4 or IPv6)
     */
    public static boolean isValidIpAddress(String ip) {
        return isValidIpv4(ip) || isValidIpv6(ip);
    }

    /**
     * Validate credit card number (Luhn algorithm)
     */
    public static boolean isValidCreditCard(String cardNumber) {
        if (StringUtils.isBlank(cardNumber)) {
            return false;
        }

        String normalized = cardNumber.replaceAll("\\s|-", "");

        if (!CREDIT_CARD_PATTERN.matcher(normalized).matches()) {
            return false;
        }

        return isValidLuhn(normalized);
    }

    /**
     * Validate using Luhn algorithm
     */
    private static boolean isValidLuhn(String number) {
        int sum = 0;
        boolean alternate = false;

        for (int i = number.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(number.charAt(i));

            if (alternate) {
                digit *= 2;
                if (digit > 9) {
                    digit -= 9;
                }
            }

            sum += digit;
            alternate = !alternate;
        }

        return sum % 10 == 0;
    }

    /**
     * Validate SSN format
     */
    public static boolean isValidSsn(String ssn) {
        if (StringUtils.isBlank(ssn)) {
            return false;
        }
        return SSN_PATTERN.matcher(ssn).matches();
    }

    /**
     * Validate ZIP code
     */
    public static boolean isValidZipCode(String zipCode) {
        if (StringUtils.isBlank(zipCode)) {
            return false;
        }
        return ZIP_CODE_PATTERN.matcher(zipCode).matches();
    }

    /**
     * Validate slug format (URL-friendly string)
     */
    public static boolean isValidSlug(String slug) {
        if (StringUtils.isBlank(slug)) {
            return false;
        }
        return SLUG_PATTERN.matcher(slug).matches();
    }

    // ========== Type Validation ==========

    /**
     * Check if string is alphanumeric
     */
    public static boolean isAlphanumeric(String value) {
        if (StringUtils.isBlank(value)) {
            return false;
        }
        return ALPHANUMERIC_PATTERN.matcher(value).matches();
    }

    /**
     * Check if string is alphabetic
     */
    public static boolean isAlphabetic(String value) {
        if (StringUtils.isBlank(value)) {
            return false;
        }
        return ALPHABETIC_PATTERN.matcher(value).matches();
    }

    /**
     * Check if string is numeric
     */
    public static boolean isNumeric(String value) {
        if (StringUtils.isBlank(value)) {
            return false;
        }
        return NUMERIC_PATTERN.matcher(value).matches();
    }

    /**
     * Check if string is decimal number
     */
    public static boolean isDecimal(String value) {
        if (StringUtils.isBlank(value)) {
            return false;
        }
        return DECIMAL_PATTERN.matcher(value).matches();
    }

    /**
     * Check if string is hexadecimal
     */
    public static boolean isHexadecimal(String value) {
        if (StringUtils.isBlank(value)) {
            return false;
        }
        return HEX_PATTERN.matcher(value).matches();
    }

    /**
     * Check if string is valid integer
     */
    public static boolean isInteger(String value) {
        if (StringUtils.isBlank(value)) {
            return false;
        }
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Check if string is valid long
     */
    public static boolean isLong(String value) {
        if (StringUtils.isBlank(value)) {
            return false;
        }
        try {
            Long.parseLong(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Check if string is valid double
     */
    public static boolean isDouble(String value) {
        if (StringUtils.isBlank(value)) {
            return false;
        }
        try {
            Double.parseDouble(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Check if string is valid boolean
     */
    public static boolean isBoolean(String value) {
        if (StringUtils.isBlank(value)) {
            return false;
        }
        return "true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value);
    }

    // ========== Numeric Range Validation ==========

    /**
     * Validate integer is in range
     */
    public static boolean isInRange(int value, int min, int max) {
        return value >= min && value <= max;
    }

    /**
     * Validate long is in range
     */
    public static boolean isInRange(long value, long min, long max) {
        return value >= min && value <= max;
    }

    /**
     * Validate double is in range
     */
    public static boolean isInRange(double value, double min, double max) {
        return value >= min && value <= max;
    }

    /**
     * Validate BigDecimal is in range
     */
    public static boolean isInRange(BigDecimal value, BigDecimal min, BigDecimal max) {
        if (value == null || min == null || max == null) {
            return false;
        }
        return value.compareTo(min) >= 0 && value.compareTo(max) <= 0;
    }

    /**
     * Validate number is positive
     */
    public static boolean isPositive(Number value) {
        if (value == null) {
            return false;
        }
        return value.doubleValue() > 0;
    }

    /**
     * Validate number is non-negative
     */
    public static boolean isNonNegative(Number value) {
        if (value == null) {
            return false;
        }
        return value.doubleValue() >= 0;
    }

    /**
     * Validate number is negative
     */
    public static boolean isNegative(Number value) {
        if (value == null) {
            return false;
        }
        return value.doubleValue() < 0;
    }

    /**
     * Validate minimum value
     */
    public static boolean isMinValue(Number value, Number min) {
        if (value == null || min == null) {
            return false;
        }
        return value.doubleValue() >= min.doubleValue();
    }

    /**
     * Validate maximum value
     */
    public static boolean isMaxValue(Number value, Number max) {
        if (value == null || max == null) {
            return false;
        }
        return value.doubleValue() <= max.doubleValue();
    }

    // ========== Date/Time Validation ==========

    /**
     * Validate date string format
     */
    public static boolean isValidDate(String dateStr, String pattern) {
        if (StringUtils.isBlank(dateStr) || StringUtils.isBlank(pattern)) {
            return false;
        }
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
            LocalDate.parse(dateStr, formatter);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    /**
     * Validate ISO date format (yyyy-MM-dd)
     */
    public static boolean isValidIsoDate(String dateStr) {
        return isValidDate(dateStr, "yyyy-MM-dd");
    }

    /**
     * Validate date is in the past
     */
    public static boolean isPastDate(LocalDate date) {
        if (date == null) {
            return false;
        }
        return date.isBefore(LocalDate.now());
    }

    /**
     * Validate date is in the future
     */
    public static boolean isFutureDate(LocalDate date) {
        if (date == null) {
            return false;
        }
        return date.isAfter(LocalDate.now());
    }

    /**
     * Validate date is today
     */
    public static boolean isToday(LocalDate date) {
        if (date == null) {
            return false;
        }
        return date.isEqual(LocalDate.now());
    }

    /**
     * Validate date is between range
     */
    public static boolean isDateBetween(LocalDate date, LocalDate start, LocalDate end) {
        if (date == null || start == null || end == null) {
            return false;
        }
        return (date.isEqual(start) || date.isAfter(start)) &&
                (date.isEqual(end) || date.isBefore(end));
    }

    /**
     * Validate datetime is between range
     */
    public static boolean isDateTimeBetween(LocalDateTime dateTime, LocalDateTime start, LocalDateTime end) {
        if (dateTime == null || start == null || end == null) {
            return false;
        }
        return (dateTime.isEqual(start) || dateTime.isAfter(start)) &&
                (dateTime.isEqual(end) || dateTime.isBefore(end));
    }

    /**
     * Validate age is valid
     */
    public static boolean isValidAge(LocalDate birthDate, int minAge, int maxAge) {
        if (birthDate == null) {
            return false;
        }
        int age = LocalDate.now().getYear() - birthDate.getYear();
        return age >= minAge && age <= maxAge;
    }

    // ========== Collection Validation ==========

    /**
     * Validate collection is not empty
     */
    public static boolean isNotEmpty(Collection<?> collection) {
        return CollectionUtils.isNotEmpty(collection);
    }

    /**
     * Validate collection size
     */
    public static boolean hasSize(Collection<?> collection, int min, int max) {
        if (CollectionUtils.isEmpty(collection)) {
            return min == 0;
        }
        int size = collection.size();
        return size >= min && size <= max;
    }

    /**
     * Validate minimum collection size
     */
    public static boolean hasMinSize(Collection<?> collection, int minSize) {
        if (CollectionUtils.isEmpty(collection)) {
            return minSize == 0;
        }
        return collection.size() >= minSize;
    }

    /**
     * Validate maximum collection size
     */
    public static boolean hasMaxSize(Collection<?> collection, int maxSize) {
        if (CollectionUtils.isEmpty(collection)) {
            return true;
        }
        return collection.size() <= maxSize;
    }

    /**
     * Validate all elements are non-null
     */
    public static boolean allElementsNonNull(Collection<?> collection) {
        if (CollectionUtils.isEmpty(collection)) {
            return true;
        }
        return collection.stream().allMatch(Objects::nonNull);
    }

    /**
     * Validate no duplicate elements
     */
    public static boolean hasNoDuplicates(Collection<?> collection) {
        if (CollectionUtils.isEmpty(collection)) {
            return true;
        }
        return collection.size() == new HashSet<>(collection).size();
    }

    // ========== Map Validation ==========

    /**
     * Validate map is not empty
     */
    public static boolean isNotEmpty(Map<?, ?> map) {
        return CollectionUtils.isNotEmpty(map);
    }

    /**
     * Validate map contains key
     */
    public static boolean containsKey(Map<?, ?> map, Object key) {
        return map != null && map.containsKey(key);
    }

    /**
     * Validate all keys are non-null
     */
    public static boolean allKeysNonNull(Map<?, ?> map) {
        if (CollectionUtils.isEmpty(map)) {
            return true;
        }
        return map.keySet().stream().allMatch(Objects::nonNull);
    }

    /**
     * Validate all values are non-null
     */
    public static boolean allValuesNonNull(Map<?, ?> map) {
        if (CollectionUtils.isEmpty(map)) {
            return true;
        }
        return map.values().stream().allMatch(Objects::nonNull);
    }

    // ========== Business Rule Validation ==========

    /**
     * Validate country code (ISO 3166)
     */
    public static boolean isValidCountryCode(String countryCode) {
        if (StringUtils.isBlank(countryCode)) {
            return false;
        }
        return ISO_COUNTRY_CODES.contains(countryCode.toUpperCase());
    }

    /**
     * Validate currency code (ISO 4217)
     */
    public static boolean isValidCurrencyCode(String currencyCode) {
        if (StringUtils.isBlank(currencyCode)) {
            return false;
        }
        return ISO_CURRENCY_CODES.contains(currencyCode.toUpperCase());
    }

    /**
     * Validate username format
     */
    public static boolean isValidUsername(String username) {
        if (StringUtils.isBlank(username)) {
            return false;
        }
        // 3-20 characters, alphanumeric, underscore, hyphen
        return username.matches("^[a-zA-Z0-9_-]{3,20}$");
    }

    /**
     * Validate file extension
     */
    public static boolean hasValidExtension(String filename, String... allowedExtensions) {
        if (StringUtils.isBlank(filename) || allowedExtensions == null || allowedExtensions.length == 0) {
            return false;
        }

        String extension = FileUtils.getExtension(filename).toLowerCase();
        return Arrays.stream(allowedExtensions)
                .map(String::toLowerCase)
                .anyMatch(ext -> ext.equals(extension));
    }

    /**
     * Validate MIME type
     */
    public static boolean isValidMimeType(String mimeType, String... allowedTypes) {
        if (StringUtils.isBlank(mimeType) || allowedTypes == null || allowedTypes.length == 0) {
            return false;
        }

        String normalized = mimeType.toLowerCase();
        return Arrays.stream(allowedTypes)
                .map(String::toLowerCase)
                .anyMatch(normalized::equals);
    }

    /**
     * Validate JSON string
     */
    public static boolean isValidJson(String json) {
        return JsonUtils.isValidJson(json);
    }

    /**
     * Validate Base64 string
     */
    public static boolean isValidBase64(String base64) {
        if (StringUtils.isBlank(base64)) {
            return false;
        }
        try {
            Base64.getDecoder().decode(base64);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    // ========== Bean Validation ==========

    /**
     * Validate bean using JSR-303 annotations
     */
    public static <T> ValidationResult validate(T bean) {
        if (bean == null) {
            return ValidationResult.invalid("Bean must not be null");
        }

        Set<ConstraintViolation<T>> violations = VALIDATOR.validate(bean);

        if (violations.isEmpty()) {
            return ValidationResult.valid();
        }

        Map<String, String> errors = violations.stream()
                .collect(Collectors.toMap(
                        v -> v.getPropertyPath().toString(),
                        ConstraintViolation::getMessage,
                        (existing, replacement) -> existing + "; " + replacement
                ));

        return ValidationResult.invalid(errors);
    }

    /**
     * Validate bean property
     */
    public static <T> ValidationResult validateProperty(T bean, String propertyName) {
        if (bean == null) {
            return ValidationResult.invalid("Bean must not be null");
        }

        if (StringUtils.isBlank(propertyName)) {
            return ValidationResult.invalid("Property name must not be blank");
        }

        Set<ConstraintViolation<T>> violations = VALIDATOR.validateProperty(bean, propertyName);

        if (violations.isEmpty()) {
            return ValidationResult.valid();
        }

        Map<String, String> errors = violations.stream()
                .collect(Collectors.toMap(
                        v -> v.getPropertyPath().toString(),
                        ConstraintViolation::getMessage
                ));

        return ValidationResult.invalid(errors);
    }

    /**
     * Validate value against constraints
     */
    public static <T> ValidationResult validateValue(Class<T> beanType, String propertyName, Object value) {
        if (beanType == null || StringUtils.isBlank(propertyName)) {
            return ValidationResult.invalid("Bean type and property name must not be null");
        }

        Set<ConstraintViolation<T>> violations = VALIDATOR.validateValue(beanType, propertyName, value);

        if (violations.isEmpty()) {
            return ValidationResult.valid();
        }

        Map<String, String> errors = violations.stream()
                .collect(Collectors.toMap(
                        v -> v.getPropertyPath().toString(),
                        ConstraintViolation::getMessage
                ));

        return ValidationResult.invalid(errors);
    }

    // ========== Bulk Validation ==========

    /**
     * Validate multiple beans
     */
    public static <T> Map<Integer, ValidationResult> validateAll(Collection<T> beans) {
        if (CollectionUtils.isEmpty(beans)) {
            return Collections.emptyMap();
        }

        Map<Integer, ValidationResult> results = new HashMap<>();
        int index = 0;

        for (T bean : beans) {
            results.put(index++, validate(bean));
        }

        return results;
    }

    /**
     * Check if all beans are valid
     */
    public static <T> boolean areAllValid(Collection<T> beans) {
        if (CollectionUtils.isEmpty(beans)) {
            return false;
        }

        return beans.stream()
                .map(ValidationUtils::validate)
                .allMatch(ValidationResult::isValid);
    }

    // ========== Custom Validators ==========

    /**
     * Validate using custom predicate
     */
    public static <T> boolean isValid(T value, Predicate<T> validator) {
        if (validator == null) {
            return false;
        }
        return validator.test(value);
    }

    /**
     * Validate using custom predicate with message
     */
    public static <T> ValidationResult validateWith(T value, Predicate<T> validator, String errorMessage) {
        if (validator == null) {
            return ValidationResult.invalid("Validator must not be null");
        }

        if (validator.test(value)) {
            return ValidationResult.valid();
        }

        return ValidationResult.invalid(errorMessage);
    }

    /**
     * Chain multiple validators
     */
    @SafeVarargs
    public static <T> ValidationResult validateChain(T value, Predicate<T>... validators) {
        if (validators == null || validators.length == 0) {
            return ValidationResult.invalid("No validators provided");
        }

        for (int i = 0; i < validators.length; i++) {
            if (!validators[i].test(value)) {
                return ValidationResult.invalid("Validation failed at step " + (i + 1));
            }
        }

        return ValidationResult.valid();
    }

    // ========== Utility Classes ==========

    /**
     * Validation result
     */
    public static class ValidationResult {
        private final boolean valid;
        private final Map<String, String> errors;
        private final String message;

        private ValidationResult(boolean valid, Map<String, String> errors, String message) {
            this.valid = valid;
            this.errors = errors != null ? errors : new HashMap<>();
            this.message = message;
        }

        public static ValidationResult valid() {
            return new ValidationResult(true, null, null);
        }

        public static ValidationResult invalid(String message) {
            return new ValidationResult(false, null, message);
        }

        public static ValidationResult invalid(Map<String, String> errors) {
            return new ValidationResult(false, errors, null);
        }

        public boolean isValid() {
            return valid;
        }

        public boolean isInvalid() {
            return !valid;
        }

        public Map<String, String> getErrors() {
            return new HashMap<>(errors);
        }

        public String getMessage() {
            if (message != null) {
                return message;
            }
            if (!errors.isEmpty()) {
                return String.join("; ", errors.values());
            }
            return null;
        }

        public List<String> getErrorList() {
            if (!errors.isEmpty()) {
                return new ArrayList<>(errors.values());
            }
            if (message != null) {
                return Collections.singletonList(message);
            }
            return Collections.emptyList();
        }

        public boolean hasErrors() {
            return !valid;
        }

        public int getErrorCount() {
            if (message != null) {
                return 1;
            }
            return errors.size();
        }

        @Override
        public String toString() {
            return "ValidationResult{" +
                    "valid=" + valid +
                    ", errors=" + errors +
                    ", message='" + message + '\'' +
                    '}';
        }
    }

    /**
     * Validation builder for complex validations
     */
    public static class ValidationBuilder<T> {
        private final T value;
        private final List<String> errors = new ArrayList<>();

        private ValidationBuilder(T value) {
            this.value = value;
        }

        public static <T> ValidationBuilder<T> of(T value) {
            return new ValidationBuilder<>(value);
        }

        public ValidationBuilder<T> notNull(String errorMessage) {
            if (value == null) {
                errors.add(errorMessage);
            }
            return this;
        }

        public ValidationBuilder<T> validate(Predicate<T> validator, String errorMessage) {
            if (value != null && !validator.test(value)) {
                errors.add(errorMessage);
            }
            return this;
        }

        public ValidationBuilder<T> validateString(Function<T, String> extractor,
                                                   Predicate<String> validator,
                                                   String errorMessage) {
            if (value != null) {
                String str = extractor.apply(value);
                if (!validator.test(str)) {
                    errors.add(errorMessage);
                }
            }
            return this;
        }

        public ValidationResult build() {
            if (errors.isEmpty()) {
                return ValidationResult.valid();
            }

            Map<String, String> errorMap = new HashMap<>();
            for (int i = 0; i < errors.size(); i++) {
                errorMap.put("error_" + i, errors.get(i));
            }

            return ValidationResult.invalid(errorMap);
        }

        public T getValueOrThrow() {
            ValidationResult result = build();
            if (result.isInvalid()) {
                throw new ValidationException(result.getMessage());
            }
            return value;
        }
    }

    /**
     * Custom validation exception
     */
    public static class ValidationException extends RuntimeException {
        private final Map<String, String> errors;

        public ValidationException(String message) {
            super(message);
            this.errors = new HashMap<>();
        }

        public ValidationException(Map<String, String> errors) {
            super(errors.values().stream().collect(Collectors.joining("; ")));
            this.errors = errors;
        }

        public Map<String, String> getErrors() {
            return new HashMap<>(errors);
        }

        public boolean hasErrors() {
            return !errors.isEmpty();
        }
    }
}