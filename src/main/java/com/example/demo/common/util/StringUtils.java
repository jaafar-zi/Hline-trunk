package com.example.demo.common.util;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.Normalizer;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Utility class for string operations
 *
 * Features:
 * - String validation and checking
 * - String transformation
 * - String sanitization
 * - Text processing
 * - Encoding/Decoding
 * - Masking and security
 *
 * @author Architecture Team
 * @version 1.0
 */
@Slf4j
@UtilityClass
public class StringUtils {

    // ========== Constants ==========

    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+");
    private static final Pattern ALPHANUMERIC_PATTERN = Pattern.compile("^[a-zA-Z0-9]+$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,}$"
    );
    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^[+]?[(]?[0-9]{1,4}[)]?[-\\s.]?[(]?[0-9]{1,4}[)]?[-\\s.]?[0-9]{1,9}$"
    );
    private static final Pattern URL_PATTERN = Pattern.compile(
            "^(https?|ftp)://[^\\s/$.?#].[^\\s]*$",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern UUID_PATTERN = Pattern.compile(
            "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$"
    );
    private static final Pattern IPV4_PATTERN = Pattern.compile(
            "^((25[0-5]|(2[0-4]|1\\d|[1-9]|)\\d)\\.?\\b){4}$"
    );

    private static final String EMPTY = "";
    private static final String SPACE = " ";
    private static final String ELLIPSIS = "...";
    private static final char MASK_CHAR = '*';

    // ========== Null/Empty Checking ==========

    /**
     * Check if string is null or empty
     */
    public static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }

    /**
     * Check if string is null, empty, or whitespace
     */
    public static boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * Check if string is not null and not empty
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    /**
     * Check if string is not blank
     */
    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }

    /**
     * Check if all strings are blank
     */
    public static boolean isAllBlank(String... strings) {
        if (strings == null || strings.length == 0) {
            return true;
        }
        return Arrays.stream(strings).allMatch(StringUtils::isBlank);
    }

    /**
     * Check if any string is blank
     */
    public static boolean isAnyBlank(String... strings) {
        if (strings == null || strings.length == 0) {
            return true;
        }
        return Arrays.stream(strings).anyMatch(StringUtils::isBlank);
    }

    /**
     * Check if all strings are not blank
     */
    public static boolean isAllNotBlank(String... strings) {
        if (strings == null || strings.length == 0) {
            return false;
        }
        return Arrays.stream(strings).allMatch(StringUtils::isNotBlank);
    }

    /**
     * Check if any string is not blank
     */
    public static boolean isAnyNotBlank(String... strings) {
        if (strings == null || strings.length == 0) {
            return false;
        }
        return Arrays.stream(strings).anyMatch(StringUtils::isNotBlank);
    }

    // ========== Default Values ==========

    /**
     * Return default value if string is null or empty
     */
    public static String defaultIfEmpty(String str, String defaultValue) {
        return isEmpty(str) ? defaultValue : str;
    }

    /**
     * Return default value if string is blank
     */
    public static String defaultIfBlank(String str, String defaultValue) {
        return isBlank(str) ? defaultValue : str;
    }

    /**
     * Return empty string if null
     */
    public static String defaultString(String str) {
        return str == null ? EMPTY : str;
    }

    /**
     * Return first non-empty string
     */
    public static String firstNonEmpty(String... strings) {
        if (strings == null) {
            return null;
        }
        return Arrays.stream(strings)
                .filter(StringUtils::isNotEmpty)
                .findFirst()
                .orElse(null);
    }

    /**
     * Return first non-blank string
     */
    public static String firstNonBlank(String... strings) {
        if (strings == null) {
            return null;
        }
        return Arrays.stream(strings)
                .filter(StringUtils::isNotBlank)
                .findFirst()
                .orElse(null);
    }

    // ========== Trimming & Cleaning ==========

    /**
     * Trim string, return null if result is empty
     */
    public static String trimToNull(String str) {
        String trimmed = str == null ? null : str.trim();
        return isEmpty(trimmed) ? null : trimmed;
    }

    /**
     * Trim string, return empty if null
     */
    public static String trimToEmpty(String str) {
        return str == null ? EMPTY : str.trim();
    }

    /**
     * Remove all whitespace from string
     */
    public static String removeWhitespace(String str) {
        if (isEmpty(str)) {
            return str;
        }
        return str.replaceAll("\\s+", EMPTY);
    }

    /**
     * Normalize whitespace (replace multiple spaces with single space)
     */
    public static String normalizeWhitespace(String str) {
        if (isEmpty(str)) {
            return str;
        }
        return WHITESPACE_PATTERN.matcher(str.trim()).replaceAll(SPACE);
    }

    /**
     * Remove accents/diacritics from string
     */
    public static String removeAccents(String str) {
        if (isEmpty(str)) {
            return str;
        }
        String normalized = Normalizer.normalize(str, Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{M}", EMPTY);
    }

    /**
     * Strip start characters
     */
    public static String stripStart(String str, String stripChars) {
        if (isEmpty(str) || stripChars == null) {
            return str;
        }

        int start = 0;
        while (start < str.length() && stripChars.indexOf(str.charAt(start)) != -1) {
            start++;
        }
        return str.substring(start);
    }

    /**
     * Strip end characters
     */
    public static String stripEnd(String str, String stripChars) {
        if (isEmpty(str) || stripChars == null) {
            return str;
        }

        int end = str.length();
        while (end > 0 && stripChars.indexOf(str.charAt(end - 1)) != -1) {
            end--;
        }
        return str.substring(0, end);
    }

    /**
     * Strip both start and end characters
     */
    public static String strip(String str, String stripChars) {
        return stripEnd(stripStart(str, stripChars), stripChars);
    }

    // ========== Case Conversion ==========

    /**
     * Convert to camelCase
     */
    public static String toCamelCase(String str) {
        if (isBlank(str)) {
            return str;
        }

        String[] words = str.toLowerCase().split("[\\s_-]+");
        StringBuilder result = new StringBuilder(words[0]);

        for (int i = 1; i < words.length; i++) {
            if (isNotBlank(words[i])) {
                result.append(capitalize(words[i]));
            }
        }

        return result.toString();
    }

    /**
     * Convert to PascalCase
     */
    public static String toPascalCase(String str) {
        if (isBlank(str)) {
            return str;
        }

        return Arrays.stream(str.toLowerCase().split("[\\s_-]+"))
                .filter(StringUtils::isNotBlank)
                .map(StringUtils::capitalize)
                .collect(Collectors.joining());
    }

    /**
     * Convert to snake_case
     */
    public static String toSnakeCase(String str) {
        if (isBlank(str)) {
            return str;
        }

        return str.replaceAll("([a-z])([A-Z])", "$1_$2")
                .replaceAll("[\\s-]+", "_")
                .toLowerCase();
    }

    /**
     * Convert to kebab-case
     */
    public static String toKebabCase(String str) {
        if (isBlank(str)) {
            return str;
        }

        return str.replaceAll("([a-z])([A-Z])", "$1-$2")
                .replaceAll("[\\s_]+", "-")
                .toLowerCase();
    }

    /**
     * Convert to SCREAMING_SNAKE_CASE
     */
    public static String toScreamingSnakeCase(String str) {
        if (isBlank(str)) {
            return str;
        }
        return toSnakeCase(str).toUpperCase();
    }

    /**
     * Capitalize first letter
     */
    public static String capitalize(String str) {
        if (isBlank(str)) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }

    /**
     * Uncapitalize first letter
     */
    public static String uncapitalize(String str) {
        if (isBlank(str)) {
            return str;
        }
        return str.substring(0, 1).toLowerCase() + str.substring(1);
    }

    /**
     * Capitalize each word
     */
    public static String capitalizeWords(String str) {
        if (isBlank(str)) {
            return str;
        }

        return Arrays.stream(str.split("\\s+"))
                .filter(StringUtils::isNotBlank)
                .map(StringUtils::capitalize)
                .collect(Collectors.joining(SPACE));
    }

    /**
     * Swap case of each character
     */
    public static String swapCase(String str) {
        if (isEmpty(str)) {
            return str;
        }

        StringBuilder result = new StringBuilder(str.length());
        for (char c : str.toCharArray()) {
            if (Character.isUpperCase(c)) {
                result.append(Character.toLowerCase(c));
            } else if (Character.isLowerCase(c)) {
                result.append(Character.toUpperCase(c));
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }

    // ========== Substring & Truncation ==========

    /**
     * Safely get substring
     */
    public static String substring(String str, int start, int end) {
        if (str == null) {
            return null;
        }

        int length = str.length();
        start = Math.max(0, start);
        end = Math.min(length, end);

        if (start > end) {
            return EMPTY;
        }

        return str.substring(start, end);
    }

    /**
     * Get substring from start index
     */
    public static String substring(String str, int start) {
        if (str == null) {
            return null;
        }
        return substring(str, start, str.length());
    }

    /**
     * Truncate string to max length
     */
    public static String truncate(String str, int maxLength) {
        if (isEmpty(str) || str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength);
    }

    /**
     * Truncate with ellipsis
     */
    public static String truncateWithEllipsis(String str, int maxLength) {
        if (isEmpty(str) || str.length() <= maxLength) {
            return str;
        }

        if (maxLength < 3) {
            return truncate(str, maxLength);
        }

        return str.substring(0, maxLength - 3) + ELLIPSIS;
    }

    /**
     * Truncate at word boundary
     */
    public static String truncateAtWord(String str, int maxLength) {
        if (isEmpty(str) || str.length() <= maxLength) {
            return str;
        }

        String truncated = str.substring(0, maxLength);
        int lastSpace = truncated.lastIndexOf(' ');

        if (lastSpace > 0) {
            return truncated.substring(0, lastSpace) + ELLIPSIS;
        }

        return truncated + ELLIPSIS;
    }

    /**
     * Get first N characters
     */
    public static String left(String str, int length) {
        if (str == null) {
            return null;
        }
        return str.substring(0, Math.min(str.length(), length));
    }

    /**
     * Get last N characters
     */
    public static String right(String str, int length) {
        if (str == null) {
            return null;
        }
        int start = Math.max(0, str.length() - length);
        return str.substring(start);
    }

    /**
     * Get middle portion of string
     */
    public static String mid(String str, int start, int length) {
        if (str == null) {
            return null;
        }

        if (start < 0) {
            start = 0;
        }

        if (start >= str.length()) {
            return EMPTY;
        }

        int end = Math.min(start + length, str.length());
        return str.substring(start, end);
    }

    // ========== Padding ==========

    /**
     * Pad string on left
     */
    public static String padLeft(String str, int length, char padChar) {
        if (str == null) {
            str = EMPTY;
        }

        int padCount = length - str.length();
        if (padCount <= 0) {
            return str;
        }

        return String.valueOf(padChar).repeat(padCount) + str;
    }

    /**
     * Pad string on right
     */
    public static String padRight(String str, int length, char padChar) {
        if (str == null) {
            str = EMPTY;
        }

        int padCount = length - str.length();
        if (padCount <= 0) {
            return str;
        }

        return str + String.valueOf(padChar).repeat(padCount);
    }

    /**
     * Center string with padding
     */
    public static String center(String str, int length, char padChar) {
        if (str == null) {
            str = EMPTY;
        }

        int strLen = str.length();
        if (strLen >= length) {
            return str;
        }

        int leftPad = (length - strLen) / 2;
        int rightPad = length - strLen - leftPad;

        return String.valueOf(padChar).repeat(leftPad) + str +
                String.valueOf(padChar).repeat(rightPad);
    }

    /**
     * Pad left with zeros
     */
    public static String leftPadZero(String str, int length) {
        return padLeft(str, length, '0');
    }

    /**
     * Pad left with spaces
     */
    public static String leftPadSpace(String str, int length) {
        return padLeft(str, length, ' ');
    }

    // ========== Validation ==========

    /**
     * Check if string is alphanumeric
     */
    public static boolean isAlphanumeric(String str) {
        if (isBlank(str)) {
            return false;
        }
        return ALPHANUMERIC_PATTERN.matcher(str).matches();
    }

    /**
     * Check if string is numeric
     */
    public static boolean isNumeric(String str) {
        if (isBlank(str)) {
            return false;
        }
        return str.matches("\\d+");
    }

    /**
     * Check if string is decimal number
     */
    public static boolean isDecimal(String str) {
        if (isBlank(str)) {
            return false;
        }
        return str.matches("-?\\d+(\\.\\d+)?");
    }

    /**
     * Check if string is alphabetic only
     */
    public static boolean isAlpha(String str) {
        if (isBlank(str)) {
            return false;
        }
        return str.matches("[a-zA-Z]+");
    }

    /**
     * Check if string is valid email
     */
    public static boolean isValidEmail(String email) {
        if (isBlank(email)) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * Check if string is valid phone number
     */
    public static boolean isValidPhone(String phone) {
        if (isBlank(phone)) {
            return false;
        }
        return PHONE_PATTERN.matcher(phone).matches();
    }

    /**
     * Check if string is valid URL
     */
    public static boolean isValidUrl(String url) {
        if (isBlank(url)) {
            return false;
        }
        return URL_PATTERN.matcher(url).matches();
    }

    /**
     * Check if string is valid UUID
     */
    public static boolean isValidUuid(String uuid) {
        if (isBlank(uuid)) {
            return false;
        }
        return UUID_PATTERN.matcher(uuid).matches();
    }

    /**
     * Check if string is valid IPv4 address
     */
    public static boolean isValidIpv4(String ip) {
        if (isBlank(ip)) {
            return false;
        }
        return IPV4_PATTERN.matcher(ip).matches();
    }

    /**
     * Check if string contains only lowercase
     */
    public static boolean isLowerCase(String str) {
        if (isEmpty(str)) {
            return false;
        }
        return str.equals(str.toLowerCase()) && !str.equals(str.toUpperCase());
    }

    /**
     * Check if string contains only uppercase
     */
    public static boolean isUpperCase(String str) {
        if (isEmpty(str)) {
            return false;
        }
        return str.equals(str.toUpperCase()) && !str.equals(str.toLowerCase());
    }

    // ========== Comparison ==========

    /**
     * Check if strings are equal
     */
    public static boolean equals(String str1, String str2) {
        return Objects.equals(str1, str2);
    }

    /**
     * Check if strings are equal (case-insensitive)
     */
    public static boolean equalsIgnoreCase(String str1, String str2) {
        if (str1 == null && str2 == null) {
            return true;
        }
        if (str1 == null || str2 == null) {
            return false;
        }
        return str1.equalsIgnoreCase(str2);
    }

    /**
     * Check if string contains substring
     */
    public static boolean contains(String str, String substring) {
        if (str == null || substring == null) {
            return false;
        }
        return str.contains(substring);
    }

    /**
     * Check if string contains substring (case-insensitive)
     */
    public static boolean containsIgnoreCase(String str, String substring) {
        if (str == null || substring == null) {
            return false;
        }
        return str.toLowerCase().contains(substring.toLowerCase());
    }

    /**
     * Check if string contains any of the substrings
     */
    public static boolean containsAny(String str, String... substrings) {
        if (isEmpty(str) || substrings == null || substrings.length == 0) {
            return false;
        }
        return Arrays.stream(substrings)
                .anyMatch(sub -> str.contains(sub));
    }

    /**
     * Check if string contains all of the substrings
     */
    public static boolean containsAll(String str, String... substrings) {
        if (isEmpty(str) || substrings == null || substrings.length == 0) {
            return false;
        }
        return Arrays.stream(substrings)
                .allMatch(sub -> str.contains(sub));
    }

    /**
     * Check if string starts with prefix
     */
    public static boolean startsWith(String str, String prefix) {
        if (str == null || prefix == null) {
            return false;
        }
        return str.startsWith(prefix);
    }

    /**
     * Check if string starts with prefix (case-insensitive)
     */
    public static boolean startsWithIgnoreCase(String str, String prefix) {
        if (str == null || prefix == null) {
            return false;
        }
        return str.toLowerCase().startsWith(prefix.toLowerCase());
    }

    /**
     * Check if string ends with suffix
     */
    public static boolean endsWith(String str, String suffix) {
        if (str == null || suffix == null) {
            return false;
        }
        return str.endsWith(suffix);
    }

    /**
     * Check if string ends with suffix (case-insensitive)
     */
    public static boolean endsWithIgnoreCase(String str, String suffix) {
        if (str == null || suffix == null) {
            return false;
        }
        return str.toLowerCase().endsWith(suffix.toLowerCase());
    }

    // ========== Counting & Occurrence ==========

    /**
     * Count occurrences of substring
     */
    public static int countOccurrences(String str, String substring) {
        if (isEmpty(str) || isEmpty(substring)) {
            return 0;
        }

        int count = 0;
        int index = 0;

        while ((index = str.indexOf(substring, index)) != -1) {
            count++;
            index += substring.length();
        }

        return count;
    }

    /**
     * Count occurrences of character
     */
    public static int countOccurrences(String str, char ch) {
        if (isEmpty(str)) {
            return 0;
        }

        int count = 0;
        for (char c : str.toCharArray()) {
            if (c == ch) {
                count++;
            }
        }
        return count;
    }

    /**
     * Count matches by regex pattern
     */
    public static int countMatches(String str, String regex) {
        if (isEmpty(str) || isEmpty(regex)) {
            return 0;
        }

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(str);

        int count = 0;
        while (matcher.find()) {
            count++;
        }
        return count;
    }

    // ========== Replacement & Removal ==========

    /**
     * Replace first occurrence
     */
    public static String replaceFirst(String str, String target, String replacement) {
        if (isEmpty(str) || isEmpty(target)) {
            return str;
        }
        return str.replaceFirst(Pattern.quote(target),
                Matcher.quoteReplacement(defaultString(replacement)));
    }

    /**
     * Replace last occurrence
     */
    public static String replaceLast(String str, String target, String replacement) {
        if (isEmpty(str) || isEmpty(target)) {
            return str;
        }

        int lastIndex = str.lastIndexOf(target);
        if (lastIndex == -1) {
            return str;
        }

        return str.substring(0, lastIndex) +
                defaultString(replacement) +
                str.substring(lastIndex + target.length());
    }

    /**
     * Replace all occurrences
     */
    public static String replaceAll(String str, String target, String replacement) {
        if (isEmpty(str) || isEmpty(target)) {
            return str;
        }
        return str.replace(target, defaultString(replacement));
    }

    /**
     * Replace all (case-insensitive)
     */
    public static String replaceAllIgnoreCase(String str, String target, String replacement) {
        if (isEmpty(str) || isEmpty(target)) {
            return str;
        }
        return str.replaceAll("(?i)" + Pattern.quote(target),
                Matcher.quoteReplacement(defaultString(replacement)));
    }

    /**
     * Remove all occurrences
     */
    public static String remove(String str, String target) {
        return replaceAll(str, target, EMPTY);
    }

    /**
     * Remove first occurrence
     */
    public static String removeFirst(String str, String target) {
        return replaceFirst(str, target, EMPTY);
    }

    /**
     * Remove last occurrence
     */
    public static String removeLast(String str, String target) {
        return replaceLast(str, target, EMPTY);
    }

    /**
     * Remove start substring if present
     */
    public static String removeStart(String str, String prefix) {
        if (isEmpty(str) || isEmpty(prefix)) {
            return str;
        }
        if (str.startsWith(prefix)) {
            return str.substring(prefix.length());
        }
        return str;
    }

    /**
     * Remove end substring if present
     */
    public static String removeEnd(String str, String suffix) {
        if (isEmpty(str) || isEmpty(suffix)) {
            return str;
        }
        if (str.endsWith(suffix)) {
            return str.substring(0, str.length() - suffix.length());
        }
        return str;
    }

    // ========== Joining & Splitting ==========

    /**
     * Join strings with delimiter
     */
    public static String join(String delimiter, String... strings) {
        if (strings == null || strings.length == 0) {
            return EMPTY;
        }
        return String.join(defaultString(delimiter), strings);
    }

    /**
     * Join collection with delimiter
     */
    public static String join(String delimiter, Collection<String> strings) {
        if (strings == null || strings.isEmpty()) {
            return EMPTY;
        }
        return String.join(defaultString(delimiter), strings);
    }

    /**
     * Join with delimiter, filtering null/empty
     */
    public static String joinNonEmpty(String delimiter, String... strings) {
        if (strings == null || strings.length == 0) {
            return EMPTY;
        }
        return Arrays.stream(strings)
                .filter(StringUtils::isNotEmpty)
                .collect(Collectors.joining(defaultString(delimiter)));
    }

    /**
     * Join with delimiter, filtering blank
     */
    public static String joinNonBlank(String delimiter, String... strings) {
        if (strings == null || strings.length == 0) {
            return EMPTY;
        }
        return Arrays.stream(strings)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.joining(defaultString(delimiter)));
    }

    /**
     * Split string by delimiter
     */
    public static String[] split(String str, String delimiter) {
        if (isEmpty(str)) {
            return new String[0];
        }
        return str.split(Pattern.quote(delimiter));
    }

    /**
     * Split string by delimiter with limit
     */
    public static String[] split(String str, String delimiter, int limit) {
        if (isEmpty(str)) {
            return new String[0];
        }
        return str.split(Pattern.quote(delimiter), limit);
    }

    /**
     * Split into list
     */
    public static List<String> splitToList(String str, String delimiter) {
        return Arrays.asList(split(str, delimiter));
    }

    /**
     * Split and trim each element
     */
    public static List<String> splitAndTrim(String str, String delimiter) {
        if (isEmpty(str)) {
            return Collections.emptyList();
        }
        return Arrays.stream(str.split(Pattern.quote(delimiter)))
                .map(String::trim)
                .filter(StringUtils::isNotEmpty)
                .collect(Collectors.toList());
    }

    // ========== Sanitization ==========

    /**
     * Remove HTML tags
     */
    public static String stripHtml(String html) {
        if (isEmpty(html)) {
            return html;
        }
        return html.replaceAll("<[^>]*>", EMPTY);
    }

    /**
     * Sanitize for SQL (basic - use PreparedStatement instead!)
     */
    public static String sanitizeForSql(String input) {
        if (isEmpty(input)) {
            return input;
        }
        return input.replace("'", "''")
                .replace("\\", "\\\\")
                .replace("\0", EMPTY)
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    /**
     * Sanitize for XSS prevention
     */
    public static String sanitizeXss(String input) {
        if (isEmpty(input)) {
            return input;
        }

        return input.replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#x27;")
                .replace("/", "&#x2F;")
                .replace("&", "&amp;");
    }

    /**
     * Encode HTML entities
     */
    public static String encodeHtml(String input) {
        return sanitizeXss(input);
    }

    /**
     * Decode HTML entities (basic)
     */
    public static String decodeHtml(String input) {
        if (isEmpty(input)) {
            return input;
        }

        return input.replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&quot;", "\"")
                .replace("&#x27;", "'")
                .replace("&#x2F;", "/")
                .replace("&amp;", "&");
    }

    /**
     * Remove special characters
     */
    public static String removeSpecialCharacters(String str) {
        if (isEmpty(str)) {
            return str;
        }
        return str.replaceAll("[^a-zA-Z0-9\\s]", EMPTY);
    }

    /**
     * Remove non-alphanumeric characters
     */
    public static String removeNonAlphanumeric(String str) {
        if (isEmpty(str)) {
            return str;
        }
        return str.replaceAll("[^a-zA-Z0-9]", EMPTY);
    }

    /**
     * Remove control characters
     */
    public static String removeControlCharacters(String str) {
        if (isEmpty(str)) {
            return str;
        }
        return str.replaceAll("\\p{Cntrl}", EMPTY);
    }

    /**
     * Escape regex special characters
     */
    public static String escapeRegex(String str) {
        if (isEmpty(str)) {
            return str;
        }
        return Pattern.quote(str);
    }

    /**
     * Escape Java string
     */
    public static String escapeJava(String str) {
        if (isEmpty(str)) {
            return str;
        }

        StringBuilder sb = new StringBuilder();
        for (char c : str.toCharArray()) {
            switch (c) {
                case '\b': sb.append("\\b"); break;
                case '\t': sb.append("\\t"); break;
                case '\n': sb.append("\\n"); break;
                case '\f': sb.append("\\f"); break;
                case '\r': sb.append("\\r"); break;
                case '\"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                default: sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * Unescape Java string
     */
    public static String unescapeJava(String str) {
        if (isEmpty(str)) {
            return str;
        }

        return str.replace("\\b", "\b")
                .replace("\\t", "\t")
                .replace("\\n", "\n")
                .replace("\\f", "\f")
                .replace("\\r", "\r")
                .replace("\\\"", "\"")
                .replace("\\\\", "\\");
    }

    // ========== Masking ==========

    /**
     * Mask email (keep first 2 chars and domain)
     */
    public static String maskEmail(String email) {
        if (isEmpty(email) || !email.contains("@")) {
            return email;
        }

        String[] parts = email.split("@");
        String local = parts[0];
        String domain = parts[1];

        if (local.length() <= 2) {
            return repeat(MASK_CHAR, 2) + "@" + domain;
        }

        return local.substring(0, 2) + repeat(MASK_CHAR, 3) + "@" + domain;
    }

    /**
     * Mask phone number (show last 4 digits)
     */
    public static String maskPhone(String phone) {
        if (isEmpty(phone)) {
            return phone;
        }

        String digits = removeNonAlphanumeric(phone);

        if (digits.length() <= 4) {
            return repeat(MASK_CHAR, digits.length());
        }

        return repeat(MASK_CHAR, digits.length() - 4) +
                digits.substring(digits.length() - 4);
    }

    /**
     * Mask credit card (show last 4 digits)
     */
    public static String maskCreditCard(String creditCard) {
        if (isEmpty(creditCard)) {
            return creditCard;
        }

        String digits = removeNonAlphanumeric(creditCard);

        if (digits.length() <= 4) {
            return repeat(MASK_CHAR, digits.length());
        }

        return repeat(MASK_CHAR, digits.length() - 4) +
                digits.substring(digits.length() - 4);
    }

    /**
     * Mask string (keep first and last N chars)
     */
    public static String mask(String str, int keepStart, int keepEnd) {
        if (isEmpty(str)) {
            return str;
        }

        int length = str.length();

        if (keepStart + keepEnd >= length) {
            return str;
        }

        return str.substring(0, keepStart) +
                repeat(MASK_CHAR, length - keepStart - keepEnd) +
                str.substring(length - keepEnd);
    }

    /**
     * Mask middle portion of string
     */
    public static String maskMiddle(String str) {
        if (isEmpty(str)) {
            return str;
        }

        int length = str.length();
        if (length <= 4) {
            return repeat(MASK_CHAR, length);
        }

        return mask(str, 2, 2);
    }

    // ========== Encoding & Hashing ==========

    /**
     * Encode to Base64
     */
    public static String encodeBase64(String str) {
        if (isEmpty(str)) {
            return str;
        }
        return Base64.getEncoder().encodeToString(str.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Decode from Base64
     */
    public static String decodeBase64(String str) {
        if (isEmpty(str)) {
            return str;
        }
        try {
            byte[] decoded = Base64.getDecoder().decode(str);
            return new String(decoded, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            log.error("Failed to decode Base64 string", e);
            return null;
        }
    }

    /**
     * Encode URL
     */
    public static String encodeUrl(String str) {
        if (isEmpty(str)) {
            return str;
        }
        try {
            return java.net.URLEncoder.encode(str, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("Failed to encode URL", e);
            return str;
        }
    }

    /**
     * Decode URL
     */
    public static String decodeUrl(String str) {
        if (isEmpty(str)) {
            return str;
        }
        try {
            return java.net.URLDecoder.decode(str, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("Failed to decode URL", e);
            return str;
        }
    }

    /**
     * Generate MD5 hash
     */
    public static String md5(String str) {
        return hash(str, "MD5");
    }

    /**
     * Generate SHA-256 hash
     */
    public static String sha256(String str) {
        return hash(str, "SHA-256");
    }

    /**
     * Generate SHA-512 hash
     */
    public static String sha512(String str) {
        return hash(str, "SHA-512");
    }

    /**
     * Generate hash using specified algorithm
     */
    private static String hash(String str, String algorithm) {
        if (isEmpty(str)) {
            return str;
        }

        try {
            MessageDigest digest = MessageDigest.getInstance(algorithm);
            byte[] hashBytes = digest.digest(str.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            log.error("Hash algorithm not found: {}", algorithm, e);
            return null;
        }
    }

    // ========== String Analysis ==========

    /**
     * Calculate Levenshtein distance
     */
    public static int levenshteinDistance(String s1, String s2) {
        if (s1 == null || s2 == null) {
            throw new IllegalArgumentException("Strings must not be null");
        }

        int[][] dp = new int[s1.length() + 1][s2.length() + 1];

        for (int i = 0; i <= s1.length(); i++) {
            dp[i][0] = i;
        }

        for (int j = 0; j <= s2.length(); j++) {
            dp[0][j] = j;
        }

        for (int i = 1; i <= s1.length(); i++) {
            for (int j = 1; j <= s2.length(); j++) {
                int cost = (s1.charAt(i - 1) == s2.charAt(j - 1)) ? 0 : 1;
                dp[i][j] = Math.min(Math.min(
                                dp[i - 1][j] + 1,      // deletion
                                dp[i][j - 1] + 1),     // insertion
                        dp[i - 1][j - 1] + cost // substitution
                );
            }
        }

        return dp[s1.length()][s2.length()];
    }

    /**
     * Calculate similarity percentage (0-100)
     */
    public static double similarity(String s1, String s2) {
        if (s1 == null || s2 == null) {
            return 0.0;
        }

        if (s1.equals(s2)) {
            return 100.0;
        }

        int maxLength = Math.max(s1.length(), s2.length());
        if (maxLength == 0) {
            return 100.0;
        }

        int distance = levenshteinDistance(s1, s2);
        return (1.0 - (double) distance / maxLength) * 100.0;
    }

    /**
     * Check if strings are similar within threshold
     */
    public static boolean isSimilar(String s1, String s2, double threshold) {
        return similarity(s1, s2) >= threshold;
    }

    // ========== Miscellaneous ==========

    /**
     * Repeat string N times
     */
    public static String repeat(String str, int count) {
        if (isEmpty(str) || count <= 0) {
            return EMPTY;
        }
        return str.repeat(count);
    }

    /**
     * Repeat character N times
     */
    public static String repeat(char ch, int count) {
        if (count <= 0) {
            return EMPTY;
        }
        return String.valueOf(ch).repeat(count);
    }

    /**
     * Reverse string
     */
    public static String reverse(String str) {
        if (isEmpty(str)) {
            return str;
        }
        return new StringBuilder(str).reverse().toString();
    }

    /**
     * Check if string is palindrome
     */
    public static boolean isPalindrome(String str) {
        if (isEmpty(str)) {
            return false;
        }
        return str.equals(reverse(str));
    }

    /**
     * Abbreviate string
     */
    public static String abbreviate(String str, int maxLength) {
        if (isEmpty(str) || maxLength >= str.length()) {
            return str;
        }

        if (maxLength < 4) {
            return truncate(str, maxLength);
        }

        return str.substring(0, maxLength - 3) + ELLIPSIS;
    }

    /**
     * Abbreviate middle
     */
    public static String abbreviateMiddle(String str, int maxLength) {
        if (isEmpty(str) || maxLength >= str.length()) {
            return str;
        }

        if (maxLength < 4) {
            return truncate(str, maxLength);
        }

        int keepLength = (maxLength - 3) / 2;
        return str.substring(0, keepLength) + ELLIPSIS +
                str.substring(str.length() - keepLength);
    }

    /**
     * Wrap text to specified width
     */
    public static String wrap(String str, int wrapLength) {
        if (isEmpty(str) || wrapLength <= 0) {
            return str;
        }

        StringBuilder result = new StringBuilder();
        int index = 0;

        while (index < str.length()) {
            if (index + wrapLength >= str.length()) {
                result.append(str.substring(index));
                break;
            }

            int wrapAt = str.lastIndexOf(' ', index + wrapLength);

            if (wrapAt <= index) {
                wrapAt = index + wrapLength;
            }

            result.append(str, index, wrapAt).append("\n");
            index = wrapAt + 1;
        }

        return result.toString();
    }

    /**
     * Generate random string
     */
    public static String randomAlphanumeric(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }

        return sb.toString();
    }

    /**
     * Generate random alphabetic string
     */
    public static String randomAlphabetic(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        Random random = new Random();
        StringBuilder sb = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }

        return sb.toString();
    }

    /**
     * Generate random numeric string
     */
    public static String randomNumeric(int length) {
        String chars = "0123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }

        return sb.toString();
    }

    /**
     * Convert to safe filename
     */
    public static String toSafeFilename(String filename) {
        if (isEmpty(filename)) {
            return filename;
        }

        return filename.replaceAll("[^a-zA-Z0-9.-]", "_")
                .replaceAll("_{2,}", "_")
                .trim();
    }

    /**
     * Convert bytes to human-readable size
     */
    public static String formatBytes(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        }

        int exp = (int) (Math.log(bytes) / Math.log(1024));
        char pre = "KMGTPE".charAt(exp - 1);

        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }

    /**
     * Convert collection to comma-separated string
     */
    public static String toCommaSeparated(Collection<?> collection) {
        if (collection == null || collection.isEmpty()) {
            return EMPTY;
        }
        return collection.stream()
                .map(Object::toString)
                .collect(Collectors.joining(", "));
    }

    /**
     * Convert array to comma-separated string
     */
    public static String toCommaSeparated(Object[] array) {
        if (array == null || array.length == 0) {
            return EMPTY;
        }
        return Arrays.stream(array)
                .map(Object::toString)
                .collect(Collectors.joining(", "));
    }

    /**
     * Ensure string ends with suffix
     */
    public static String ensureEndsWith(String str, String suffix) {
        if (isEmpty(str) || isEmpty(suffix)) {
            return str;
        }
        return str.endsWith(suffix) ? str : str + suffix;
    }

    /**
     * Ensure string starts with prefix
     */
    public static String ensureStartsWith(String str, String prefix) {
        if (isEmpty(str) || isEmpty(prefix)) {
            return str;
        }
        return str.startsWith(prefix) ? str : prefix + str;
    }

    /**
     * Quote string
     */
    public static String quote(String str) {
        return "\"" + defaultString(str) + "\"";
    }

    /**
     * Unquote string
     */
    public static String unquote(String str) {
        if (isEmpty(str) || str.length() < 2) {
            return str;
        }

        if ((str.startsWith("\"") && str.endsWith("\"")) ||
                (str.startsWith("'") && str.endsWith("'"))) {
            return str.substring(1, str.length() - 1);
        }

        return str;
    }

    /**
     * Get length safely (null-safe)
     */
    public static int length(String str) {
        return str == null ? 0 : str.length();
    }

    /**
     * Get char at index safely
     */
    public static Character charAt(String str, int index) {
        if (str == null || index < 0 || index >= str.length()) {
            return null;
        }
        return str.charAt(index);
    }
}