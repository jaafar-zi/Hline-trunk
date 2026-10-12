package com.example.demo.modules.user.application.validator;

import com.example.demo.common.validator.CustomValidators;
import com.example.demo.common.validator.DateValidator;
import com.example.demo.modules.user.application.dto.request.UpdateUserProfileRequest;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.regex.Pattern;

/**
 * Spring Validator for UpdateUserProfileRequest
 * Validates self-service profile updates
 *
 * @author Architecture Team
 * @version 1.0
 */
@Component
public class UpdateUserProfileRequestValidator implements Validator {

    private static final Pattern TWITTER_HANDLE_PATTERN = Pattern.compile("^@?[A-Za-z0-9_]{1,15}$");
    private static final Pattern URL_PATTERN = Pattern.compile(
            "^(https?://)?([\\w\\-]+\\.)+[\\w\\-]+(/[\\w\\-._~:/?#\\[\\]@!$&'()*+,;=]*)?$",
            Pattern.CASE_INSENSITIVE
    );
    private static final int MAX_BIO_LENGTH = 1000;
    private static final int MAX_URL_LENGTH = 500;

    @Override
    public boolean supports(Class<?> clazz) {
        return UpdateUserProfileRequest.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        UpdateUserProfileRequest request = (UpdateUserProfileRequest) target;

        // Date of birth validation
        if (request.getDateOfBirth() != null) {
            validateDateOfBirth(request.getDateOfBirth(), errors);
        }

        // Timezone validation
        if (request.getTimezone() != null && !request.getTimezone().isBlank()) {
            validateTimezone(request.getTimezone(), errors);
        }

        // Country code validation
        if (request.getCountry() != null && !request.getCountry().isBlank()) {
            validateCountryCode(request.getCountry(), errors);
        }

        // Language code validation
        if (request.getLanguage() != null && !request.getLanguage().isBlank()) {
            validateLanguageCode(request.getLanguage(), errors);
        }

        // Phone number validation
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().isBlank()) {
            validatePhoneNumber(request.getPhoneNumber(), errors);
        }

        // Bio validation
        if (request.getBio() != null) {
            validateBio(request.getBio(), errors);
        }

        // URL validations
        validateUrls(request, errors);

        // Twitter handle validation
        if (request.getTwitterHandle() != null && !request.getTwitterHandle().isBlank()) {
            validateTwitterHandle(request.getTwitterHandle(), errors);
        }

        // SQL injection check
        validateNoSqlInjection(request, errors);

        // Notification preferences validation
        if (request.getNotificationPreferences() != null) {
            validateNotificationPreferences(request.getNotificationPreferences(), errors);
        }

        // Privacy settings validation
        if (request.getPrivacySettings() != null) {
            validatePrivacySettings(request.getPrivacySettings(), errors);
        }
    }

    // ====================================
    // Private Validation Methods
    // ====================================

    private void validateDateOfBirth(java.time.LocalDate dateOfBirth, Errors errors) {
        if (DateValidator.isFuture(dateOfBirth)) {
            errors.rejectValue("dateOfBirth", "dateOfBirth.future",
                    "Date of birth cannot be in the future");
            return;
        }

        if (!DateValidator.isMinimumAge(dateOfBirth, 13)) {
            errors.rejectValue("dateOfBirth", "age.tooYoung",
                    "You must be at least 13 years old");
            return;
        }

        if (!DateValidator.isAgeInRange(dateOfBirth, 13, 120)) {
            errors.rejectValue("dateOfBirth", "age.invalid",
                    "Invalid date of birth");
        }
    }

    private void validateTimezone(String timezone, Errors errors) {
        try {
            java.time.ZoneId.of(timezone);
        } catch (java.time.DateTimeException e) {
            errors.rejectValue("timezone", "timezone.invalid",
                    String.format("Invalid timezone: %s", timezone));
        }
    }

    private void validateCountryCode(String country, Errors errors) {
        if (!java.util.Arrays.asList(java.util.Locale.getISOCountries()).contains(country.toUpperCase())) {
            errors.rejectValue("country", "country.invalid",
                    "Invalid ISO 3166-1 alpha-2 country code");
        }
    }

    private void validateLanguageCode(String language, Errors errors) {
        if (!java.util.Arrays.asList(java.util.Locale.getISOLanguages()).contains(language.toLowerCase())) {
            errors.rejectValue("language", "language.invalid",
                    "Invalid ISO 639-1 language code");
        }
    }

    private void validatePhoneNumber(String phoneNumber, Errors errors) {
        String cleanPhone = phoneNumber.replaceAll("[\\s()\\-.]", "");

        if (!cleanPhone.matches("^\\+?[1-9]\\d{6,14}$")) {
            errors.rejectValue("phoneNumber", "phoneNumber.invalid",
                    "Invalid phone number format");
            return;
        }

        // Check for sequential numbers (e.g., 1234567890)
        if (cleanPhone.replaceAll("\\+", "").matches(".*(012|123|234|345|456|567|678|789|890).*")) {
            errors.rejectValue("phoneNumber", "phoneNumber.sequential",
                    "Phone number contains suspicious sequential pattern");
        }
    }

    private void validateBio(String bio, Errors errors) {
        if (bio.length() > MAX_BIO_LENGTH) {
            errors.rejectValue("bio", "bio.tooLong",
                    String.format("Bio must not exceed %d characters", MAX_BIO_LENGTH));
            return;
        }

        // Check for excessive special characters
        long specialCharCount = bio.chars().filter(ch -> !Character.isLetterOrDigit(ch) && !Character.isWhitespace(ch)).count();
        double specialCharRatio = (double) specialCharCount / bio.length();

        if (specialCharRatio > 0.3) {
            errors.rejectValue("bio", "bio.tooManySpecialChars",
                    "Bio contains too many special characters");
        }
    }

    private void validateUrls(UpdateUserProfileRequest request, Errors errors) {
        // Avatar URL
        if (request.getAvatarUrl() != null && !request.getAvatarUrl().isBlank()) {
            validateUrl(request.getAvatarUrl(), "avatarUrl", "Avatar URL", errors);
            validateImageUrl(request.getAvatarUrl(), "avatarUrl", errors);
        }

        // Website URL
        if (request.getWebsiteUrl() != null && !request.getWebsiteUrl().isBlank()) {
            validateUrl(request.getWebsiteUrl(), "websiteUrl", "Website URL", errors);
        }

        // LinkedIn URL
        if (request.getLinkedinUrl() != null && !request.getLinkedinUrl().isBlank()) {
            validateUrl(request.getLinkedinUrl(), "linkedinUrl", "LinkedIn URL", errors);
            if (!request.getLinkedinUrl().toLowerCase().contains("linkedin.com")) {
                errors.rejectValue("linkedinUrl", "linkedinUrl.invalid",
                        "Must be a valid LinkedIn URL (linkedin.com)");
            }
        }

        // GitHub URL
        if (request.getGithubUrl() != null && !request.getGithubUrl().isBlank()) {
            validateUrl(request.getGithubUrl(), "githubUrl", "GitHub URL", errors);
            if (!request.getGithubUrl().toLowerCase().contains("github.com")) {
                errors.rejectValue("githubUrl", "githubUrl.invalid",
                        "Must be a valid GitHub URL (github.com)");
            }
        }
    }

    private void validateUrl(String url, String fieldName, String fieldDisplayName, Errors errors) {
        if (url.length() > MAX_URL_LENGTH) {
            errors.rejectValue(fieldName, fieldName + ".tooLong",
                    String.format("%s must not exceed %d characters", fieldDisplayName, MAX_URL_LENGTH));
            return;
        }

        if (!URL_PATTERN.matcher(url).matches()) {
            errors.rejectValue(fieldName, fieldName + ".invalid",
                    String.format("Invalid %s format", fieldDisplayName));
            return;
        }

        // Check for malicious patterns
        String lowerUrl = url.toLowerCase();
        if (lowerUrl.contains("javascript:") || lowerUrl.contains("data:") || lowerUrl.contains("vbscript:")) {
            errors.rejectValue(fieldName, fieldName + ".malicious",
                    String.format("%s contains potentially malicious content", fieldDisplayName));
        }
    }

    private void validateImageUrl(String url, String fieldName, Errors errors) {
        String lowerUrl = url.toLowerCase();
        boolean isImageUrl = lowerUrl.matches(".*\\.(jpg|jpeg|png|gif|webp|svg)(\\?.*)?$") ||
                lowerUrl.contains("/image/") ||
                lowerUrl.contains("avatar") ||
                lowerUrl.contains("profile");

        if (!isImageUrl) {
            errors.rejectValue(fieldName, fieldName + ".notImage",
                    "Avatar URL should point to an image file");
        }
    }

    private void validateTwitterHandle(String handle, Errors errors) {
        if (!TWITTER_HANDLE_PATTERN.matcher(handle).matches()) {
            errors.rejectValue("twitterHandle", "twitterHandle.invalid",
                    "Twitter handle must be 1-15 characters, alphanumeric and underscores only");
            return;
        }

        String cleanHandle = handle.startsWith("@") ? handle.substring(1) : handle;

        if (cleanHandle.length() > 15) {
            errors.rejectValue("twitterHandle", "twitterHandle.tooLong",
                    "Twitter handle cannot exceed 15 characters");
        }

        // Reserved handles
        String[] reserved = {"admin", "twitter", "support", "help", "official"};
        for (String reservedHandle : reserved) {
            if (cleanHandle.equalsIgnoreCase(reservedHandle)) {
                errors.rejectValue("twitterHandle", "twitterHandle.reserved",
                        "This Twitter handle is reserved");
                break;
            }
        }
    }

    private void validateNotificationPreferences(
            UpdateUserProfileRequest.NotificationPreferences prefs, Errors errors) {

        // Security alerts should always be enabled (warning only)
        if (prefs.getSecurityAlerts() != null && !prefs.getSecurityAlerts()) {
            errors.rejectValue("notificationPreferences.securityAlerts",
                    "securityAlerts.disabled",
                    "Warning: Disabling security alerts is not recommended");
        }

        // If SMS enabled, phone number should be provided
        if (Boolean.TRUE.equals(prefs.getSmsNotifications())) {
            // This would need context from the full request
            // Just a warning for now
            errors.rejectValue("notificationPreferences.smsNotifications",
                    "smsNotifications.noPhone",
                    "SMS notifications require a valid phone number");
        }
    }

    private void validatePrivacySettings(
            UpdateUserProfileRequest.PrivacySettings settings, Errors errors) {

        // If showing email/phone publicly, warn user
        if (Boolean.TRUE.equals(settings.getShowEmail())) {
            errors.rejectValue("privacySettings.showEmail",
                    "showEmail.warning",
                    "Warning: Your email will be visible to everyone");
        }

        if (Boolean.TRUE.equals(settings.getShowPhone())) {
            errors.rejectValue("privacySettings.showPhone",
                    "showPhone.warning",
                    "Warning: Your phone number will be visible to everyone");
        }

        // Validate logical consistency
        if (Boolean.FALSE.equals(settings.getPublicProfile()) &&
                Boolean.TRUE.equals(settings.getSearchable())) {
            errors.rejectValue("privacySettings.searchable",
                    "searchable.inconsistent",
                    "Cannot be searchable with a private profile");
        }
    }

    private void validateNoSqlInjection(UpdateUserProfileRequest request, Errors errors) {
        if (request.getFirstName() != null && !CustomValidators.isSqlInjectionFree(request.getFirstName())) {
            errors.rejectValue("firstName", "firstName.sqlInjection",
                    "First name contains invalid characters");
        }

        if (request.getLastName() != null && !CustomValidators.isSqlInjectionFree(request.getLastName())) {
            errors.rejectValue("lastName", "lastName.sqlInjection",
                    "Last name contains invalid characters");
        }

        if (request.getDisplayName() != null && !CustomValidators.isSqlInjectionFree(request.getDisplayName())) {
            errors.rejectValue("displayName", "displayName.sqlInjection",
                    "Display name contains invalid characters");
        }

        if (request.getBio() != null && !CustomValidators.isSqlInjectionFree(request.getBio())) {
            errors.rejectValue("bio", "bio.sqlInjection",
                    "Bio contains invalid characters");
        }
    }
}