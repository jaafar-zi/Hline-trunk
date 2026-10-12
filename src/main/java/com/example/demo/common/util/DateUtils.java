package com.example.demo.common.util;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.Date;
import java.util.TimeZone;

/**
 * Utility class for date and time operations
 *
 * Features:
 * - Date conversion and formatting
 * - Date calculations
 * - Time zone handling
 * - Date range operations
 * - Business day calculations
 *
 * @author Architecture Team
 * @version 1.0
 */
@Slf4j
@UtilityClass
public class DateUtils {

    // Common date formatters
    public static final DateTimeFormatter ISO_DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    public static final DateTimeFormatter ISO_DATETIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    public static final DateTimeFormatter DEFAULT_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter DEFAULT_DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final DateTimeFormatter DISPLAY_DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy");
    public static final DateTimeFormatter DISPLAY_DATETIME_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
    public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    // ========== Conversion Methods ==========

    /**
     * Convert LocalDate to Date
     */
    public static Date toDate(LocalDate localDate) {
        if (localDate == null) {
            return null;
        }
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    /**
     * Convert LocalDateTime to Date
     */
    public static Date toDate(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * Convert Date to LocalDate
     */
    public static LocalDate toLocalDate(Date date) {
        if (date == null) {
            return null;
        }
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    /**
     * Convert Date to LocalDateTime
     */
    public static LocalDateTime toLocalDateTime(Date date) {
        if (date == null) {
            return null;
        }
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    /**
     * Convert timestamp (milliseconds) to LocalDateTime
     */
    public static LocalDateTime toLocalDateTime(long timestamp) {
        return Instant.ofEpochMilli(timestamp)
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }

    /**
     * Convert LocalDateTime to timestamp (milliseconds)
     */
    public static long toTimestamp(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return 0;
        }
        return localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    // ========== Parsing Methods ==========

    /**
     * Parse string to LocalDate with default format (yyyy-MM-dd)
     */
    public static LocalDate parseDate(String dateString) {
        return parseDate(dateString, DEFAULT_DATE_FORMATTER);
    }

    /**
     * Parse string to LocalDate with custom format
     */
    public static LocalDate parseDate(String dateString, DateTimeFormatter formatter) {
        if (dateString == null || dateString.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(dateString.trim(), formatter);
        } catch (DateTimeParseException e) {
            log.error("Failed to parse date: {}", dateString, e);
            return null;
        }
    }

    /**
     * Parse string to LocalDateTime with default format
     */
    public static LocalDateTime parseDateTime(String dateTimeString) {
        return parseDateTime(dateTimeString, DEFAULT_DATETIME_FORMATTER);
    }

    /**
     * Parse string to LocalDateTime with custom format
     */
    public static LocalDateTime parseDateTime(String dateTimeString, DateTimeFormatter formatter) {
        if (dateTimeString == null || dateTimeString.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDateTime.parse(dateTimeString.trim(), formatter);
        } catch (DateTimeParseException e) {
            log.error("Failed to parse datetime: {}", dateTimeString, e);
            return null;
        }
    }

    /**
     * Parse ISO 8601 date string
     */
    public static LocalDateTime parseISO8601(String isoString) {
        if (isoString == null || isoString.trim().isEmpty()) {
            return null;
        }
        try {
            return ZonedDateTime.parse(isoString).toLocalDateTime();
        } catch (DateTimeParseException e) {
            log.error("Failed to parse ISO8601 datetime: {}", isoString, e);
            return null;
        }
    }

    // ========== Formatting Methods ==========

    /**
     * Format LocalDate to string with default format
     */
    public static String formatDate(LocalDate date) {
        return formatDate(date, DEFAULT_DATE_FORMATTER);
    }

    /**
     * Format LocalDate to string with custom format
     */
    public static String formatDate(LocalDate date, DateTimeFormatter formatter) {
        if (date == null) {
            return null;
        }
        return date.format(formatter);
    }

    /**
     * Format LocalDateTime to string with default format
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        return formatDateTime(dateTime, DEFAULT_DATETIME_FORMATTER);
    }

    /**
     * Format LocalDateTime to string with custom format
     */
    public static String formatDateTime(LocalDateTime dateTime, DateTimeFormatter formatter) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(formatter);
    }

    /**
     * Format LocalDateTime to ISO 8601 string
     */
    public static String formatISO8601(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    /**
     * Format for display (human-readable)
     */
    public static String formatForDisplay(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(DISPLAY_DATETIME_FORMATTER);
    }

    // ========== Date Calculations ==========

    /**
     * Add days to date
     */
    public static LocalDate addDays(LocalDate date, long days) {
        return date != null ? date.plusDays(days) : null;
    }

    /**
     * Add months to date
     */
    public static LocalDate addMonths(LocalDate date, long months) {
        return date != null ? date.plusMonths(months) : null;
    }

    /**
     * Add years to date
     */
    public static LocalDate addYears(LocalDate date, long years) {
        return date != null ? date.plusYears(years) : null;
    }

    /**
     * Calculate days between two dates
     */
    public static long daysBetween(LocalDate start, LocalDate end) {
        if (start == null || end == null) {
            return 0;
        }
        return ChronoUnit.DAYS.between(start, end);
    }

    /**
     * Calculate hours between two datetimes
     */
    public static long hoursBetween(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            return 0;
        }
        return ChronoUnit.HOURS.between(start, end);
    }

    /**
     * Calculate minutes between two datetimes
     */
    public static long minutesBetween(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            return 0;
        }
        return ChronoUnit.MINUTES.between(start, end);
    }

    /**
     * Calculate age from birth date
     */
    public static int calculateAge(LocalDate birthDate) {
        if (birthDate == null) {
            return 0;
        }
        return Period.between(birthDate, LocalDate.now()).getYears();
    }

    // ========== Date Checking Methods ==========

    /**
     * Check if date is in the past
     */
    public static boolean isPast(LocalDate date) {
        return date != null && date.isBefore(LocalDate.now());
    }

    /**
     * Check if date is in the future
     */
    public static boolean isFuture(LocalDate date) {
        return date != null && date.isAfter(LocalDate.now());
    }

    /**
     * Check if date is today
     */
    public static boolean isToday(LocalDate date) {
        return date != null && date.isEqual(LocalDate.now());
    }

    /**
     * Check if datetime is within last N hours
     */
    public static boolean isWithinLastHours(LocalDateTime dateTime, long hours) {
        if (dateTime == null) {
            return false;
        }
        LocalDateTime threshold = LocalDateTime.now().minusHours(hours);
        return dateTime.isAfter(threshold);
    }

    /**
     * Check if date is weekend
     */
    public static boolean isWeekend(LocalDate date) {
        if (date == null) {
            return false;
        }
        DayOfWeek day = date.getDayOfWeek();
        return day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY;
    }

    /**
     * Check if date is business day (Monday-Friday)
     */
    public static boolean isBusinessDay(LocalDate date) {
        return !isWeekend(date);
    }

    /**
     * Check if date is in range
     */
    public static boolean isInRange(LocalDate date, LocalDate start, LocalDate end) {
        if (date == null) {
            return false;
        }
        boolean afterStart = start == null || !date.isBefore(start);
        boolean beforeEnd = end == null || !date.isAfter(end);
        return afterStart && beforeEnd;
    }

    // ========== Date Range Operations ==========

    /**
     * Get start of day
     */
    public static LocalDateTime startOfDay(LocalDate date) {
        return date != null ? date.atStartOfDay() : null;
    }

    /**
     * Get end of day
     */
    public static LocalDateTime endOfDay(LocalDate date) {
        return date != null ? date.atTime(23, 59, 59, 999_999_999) : null;
    }

    /**
     * Get start of month
     */
    public static LocalDate startOfMonth(LocalDate date) {
        return date != null ? date.with(TemporalAdjusters.firstDayOfMonth()) : null;
    }

    /**
     * Get end of month
     */
    public static LocalDate endOfMonth(LocalDate date) {
        return date != null ? date.with(TemporalAdjusters.lastDayOfMonth()) : null;
    }

    /**
     * Get start of year
     */
    public static LocalDate startOfYear(LocalDate date) {
        return date != null ? date.with(TemporalAdjusters.firstDayOfYear()) : null;
    }

    /**
     * Get end of year
     */
    public static LocalDate endOfYear(LocalDate date) {
        return date != null ? date.with(TemporalAdjusters.lastDayOfYear()) : null;
    }

    // ========== Business Day Calculations ==========

    /**
     * Add business days (skipping weekends)
     */
    public static LocalDate addBusinessDays(LocalDate date, int days) {
        if (date == null) {
            return null;
        }

        LocalDate result = date;
        int addedDays = 0;

        while (addedDays < days) {
            result = result.plusDays(1);
            if (isBusinessDay(result)) {
                addedDays++;
            }
        }

        return result;
    }

    /**
     * Calculate business days between two dates
     */
    public static long businessDaysBetween(LocalDate start, LocalDate end) {
        if (start == null || end == null) {
            return 0;
        }

        long businessDays = 0;
        LocalDate current = start;

        while (!current.isAfter(end)) {
            if (isBusinessDay(current)) {
                businessDays++;
            }
            current = current.plusDays(1);
        }

        return businessDays;
    }

    // ========== Time Zone Operations ==========

    /**
     * Convert datetime to different timezone
     */
    public static ZonedDateTime convertTimeZone(LocalDateTime dateTime,
                                                ZoneId fromZone,
                                                ZoneId toZone) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.atZone(fromZone).withZoneSameInstant(toZone);
    }

    /**
     * Get current datetime in specific timezone
     */
    public static LocalDateTime nowInTimeZone(String timeZoneId) {
        return ZonedDateTime.now(ZoneId.of(timeZoneId)).toLocalDateTime();
    }

    /**
     * Get UTC datetime
     */
    public static LocalDateTime nowUTC() {
        return LocalDateTime.now(ZoneOffset.UTC);
    }

    // ========== Relative Time ==========

    /**
     * Get human-readable relative time (e.g., "2 hours ago")
     */
    public static String getRelativeTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }

        LocalDateTime now = LocalDateTime.now();
        long minutes = ChronoUnit.MINUTES.between(dateTime, now);
        long hours = ChronoUnit.HOURS.between(dateTime, now);
        long days = ChronoUnit.DAYS.between(dateTime, now);

        if (minutes < 1) {
            return "just now";
        } else if (minutes < 60) {
            return minutes + " minute" + (minutes > 1 ? "s" : "") + " ago";
        } else if (hours < 24) {
            return hours + " hour" + (hours > 1 ? "s" : "") + " ago";
        } else if (days < 7) {
            return days + " day" + (days > 1 ? "s" : "") + " ago";
        } else if (days < 30) {
            long weeks = days / 7;
            return weeks + " week" + (weeks > 1 ? "s" : "") + " ago";
        } else if (days < 365) {
            long months = days / 30;
            return months + " month" + (months > 1 ? "s" : "") + " ago";
        } else {
            long years = days / 365;
            return years + " year" + (years > 1 ? "s" : "") + " ago";
        }
    }

    // ========== Validation ==========

    /**
     * Validate date string format
     */
    public static boolean isValidDateFormat(String dateString, String pattern) {
        try {
            LocalDate.parse(dateString, DateTimeFormatter.ofPattern(pattern));
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    /**
     * Get current timestamp in seconds
     */
    public static long currentTimeSeconds() {
        return Instant.now().getEpochSecond();
    }

    /**
     * Get current timestamp in milliseconds
     */
    public static long currentTimeMillis() {
        return System.currentTimeMillis();
    }
}