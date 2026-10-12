package com.example.demo.common.validator;

import com.example.demo.common.validator.constraints.NotFutureDate;
import com.example.demo.common.validator.constraints.NotPastDate;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * Date validation utilities and validators
 *
 * @author Architecture Team
 * @version 1.0
 */
@Slf4j
@UtilityClass
public class DateValidator {

    private static final DateTimeFormatter ISO_DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    /**
     * Validator for @NotPastDate annotation
     */
    public static class NotPastDateValidator implements ConstraintValidator<NotPastDate, Object> {

        private boolean allowToday;
        private boolean allowNull;

        @Override
        public void initialize(NotPastDate constraintAnnotation) {
            this.allowToday = constraintAnnotation.allowToday();
            this.allowNull = constraintAnnotation.allowNull();
        }

        @Override
        public boolean isValid(Object value, ConstraintValidatorContext context) {
            if (value == null) {
                return allowNull;
            }

            LocalDate dateToCheck = convertToLocalDate(value);
            if (dateToCheck == null) {
                buildConstraintViolation(context, "Invalid date format");
                return false;
            }

            LocalDate today = LocalDate.now();

            if (allowToday) {
                if (dateToCheck.isBefore(today)) {
                    buildConstraintViolation(context,
                            String.format("Date must not be before %s", today));
                    return false;
                }
            } else {
                if (!dateToCheck.isAfter(today)) {
                    buildConstraintViolation(context,
                            String.format("Date must be after %s", today));
                    return false;
                }
            }

            return true;
        }

        private void buildConstraintViolation(ConstraintValidatorContext context, String message) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(message)
                    .addConstraintViolation();
        }
    }

    /**
     * Validator for @NotFutureDate annotation
     */
    public static class NotFutureDateValidator implements ConstraintValidator<NotFutureDate, Object> {

        private boolean allowToday;
        private boolean allowNull;

        @Override
        public void initialize(NotFutureDate constraintAnnotation) {
            this.allowToday = constraintAnnotation.allowToday();
            this.allowNull = constraintAnnotation.allowNull();
        }

        @Override
        public boolean isValid(Object value, ConstraintValidatorContext context) {
            if (value == null) {
                return allowNull;
            }

            LocalDate dateToCheck = convertToLocalDate(value);
            if (dateToCheck == null) {
                buildConstraintViolation(context, "Invalid date format");
                return false;
            }

            LocalDate today = LocalDate.now();

            if (allowToday) {
                if (dateToCheck.isAfter(today)) {
                    buildConstraintViolation(context,
                            String.format("Date must not be after %s", today));
                    return false;
                }
            } else {
                if (!dateToCheck.isBefore(today)) {
                    buildConstraintViolation(context,
                            String.format("Date must be before %s", today));
                    return false;
                }
            }

            return true;
        }

        private void buildConstraintViolation(ConstraintValidatorContext context, String message) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(message)
                    .addConstraintViolation();
        }
    }

    /**
     * Convert various date types to LocalDate
     */
    private static LocalDate convertToLocalDate(Object value) {
        if (value instanceof LocalDate) {
            return (LocalDate) value;
        } else if (value instanceof LocalDateTime) {
            return ((LocalDateTime) value).toLocalDate();
        } else if (value instanceof java.util.Date) {
            return ((java.util.Date) value).toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
        } else if (value instanceof String) {
            try {
                return LocalDate.parse((String) value, ISO_DATE_FORMATTER);
            } catch (Exception e) {
                log.warn("Failed to parse date string: {}", value);
                return null;
            }
        }
        return null;
    }

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
     * Check if date is within range
     */
    public static boolean isInRange(LocalDate date, LocalDate start, LocalDate end) {
        if (date == null) {
            return false;
        }

        boolean afterStart = start == null || !date.isBefore(start);
        boolean beforeEnd = end == null || !date.isAfter(end);

        return afterStart && beforeEnd;
    }

    /**
     * Calculate age from birth date
     */
    public static int calculateAge(LocalDate birthDate) {
        if (birthDate == null) {
            return 0;
        }
        return (int) ChronoUnit.YEARS.between(birthDate, LocalDate.now());
    }

    /**
     * Check if age is at least minimum
     */
    public static boolean isMinimumAge(LocalDate birthDate, int minimumAge) {
        return calculateAge(birthDate) >= minimumAge;
    }

    /**
     * Check if age is within range
     */
    public static boolean isAgeInRange(LocalDate birthDate, int minAge, int maxAge) {
        int age = calculateAge(birthDate);
        return age >= minAge && age <= maxAge;
    }

    /**
     * Calculate days between dates
     */
    public static long daysBetween(LocalDate start, LocalDate end) {
        if (start == null || end == null) {
            return 0;
        }
        return ChronoUnit.DAYS.between(start, end);
    }

    /**
     * Check if dates are within X days of each other
     */
    public static boolean isWithinDays(LocalDate date1, LocalDate date2, int days) {
        return Math.abs(daysBetween(date1, date2)) <= days;
    }

    /**
     * Validate business day (Monday-Friday)
     */
    public static boolean isBusinessDay(LocalDate date) {
        if (date == null) {
            return false;
        }
        DayOfWeek day = date.getDayOfWeek();
        return day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY;
    }

    /**
     * Validate weekend (Saturday-Sunday)
     */
    public static boolean isWeekend(LocalDate date) {
        if (date == null) {
            return false;
        }
        DayOfWeek day = date.getDayOfWeek();
        return day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY;
    }
}