package com.example.demo.modules.user.application.validator;

import com.example.demo.common.validator.CustomValidators;
import com.example.demo.common.validator.DateValidator;
import com.example.demo.modules.user.application.dto.request.UpdateUserRequest;
import com.example.demo.modules.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.UUID;

/**
 * Spring Validator for UpdateUserRequest
 * Handles business logic validation using core validators
 *
 * @author Architecture Team
 * @version 1.0
 */
@Component
@RequiredArgsConstructor
public class UpdateUserRequestValidator implements Validator {

    private final UserRepository userRepository;

    @Override
    public boolean supports(Class<?> clazz) {
        return UpdateUserRequest.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        UpdateUserRequest request = (UpdateUserRequest) target;

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

        // Avatar URL validation
        if (request.getAvatarUrl() != null && !request.getAvatarUrl().isBlank()) {
            validateAvatarUrl(request.getAvatarUrl(), errors);
        }

        // UserGroup ID validation
        if (request.getUserGroupId() != null && !request.getUserGroupId().isBlank()) {
            validateUserGroupId(request.getUserGroupId(), errors);
        }

        // SQL injection check on text fields
        validateNoSqlInjection(request, errors);
    }

    /**
     * Validate with user ID context (for email uniqueness check)
     * This method should be called from the controller with the user ID from the path
     */
    public void validateWithContext(UpdateUserRequest request, UUID userId, Errors errors) {
        // First perform standard validation
        validate(request, errors);

        // Email uniqueness (excluding current user)
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            validateEmailUniqueness(request.getEmail(), userId, errors);
        }
    }

    // ====================================
    // Private Validation Methods
    // ====================================

    private void validateDateOfBirth(java.time.LocalDate dateOfBirth, Errors errors) {
        // Check if date is in the future
        if (DateValidator.isFuture(dateOfBirth)) {
            errors.rejectValue("dateOfBirth", "dateOfBirth.future",
                    "Date of birth cannot be in the future");
            return;
        }

        // Check minimum age (13 years - COPPA compliance)
        if (!DateValidator.isMinimumAge(dateOfBirth, 13)) {
            errors.rejectValue("dateOfBirth", "age.tooYoung",
                    "User must be at least 13 years old (COPPA compliance)");
            return;
        }

        // Check reasonable age (not older than 120 years)
        if (!DateValidator.isAgeInRange(dateOfBirth, 13, 120)) {
            errors.rejectValue("dateOfBirth", "age.invalid",
                    "Invalid date of birth. Age must be between 13 and 120 years");
        }
    }

    private void validateTimezone(String timezone, Errors errors) {
        try {
            java.time.ZoneId.of(timezone);
        } catch (java.time.DateTimeException e) {
            errors.rejectValue("timezone", "timezone.invalid",
                    String.format("Invalid timezone: %s. Use IANA timezone format (e.g., America/New_York)", timezone));
        }
    }

    private void validateCountryCode(String country, Errors errors) {
        String upperCountry = country.toUpperCase();

        if (!java.util.Arrays.asList(java.util.Locale.getISOCountries()).contains(upperCountry)) {
            errors.rejectValue("country", "country.invalid",
                    String.format("Invalid country code: %s. Must be ISO 3166-1 alpha-2 code (e.g., US, FR, DE)", country));
        }
    }

    private void validateLanguageCode(String language, Errors errors) {
        String lowerLanguage = language.toLowerCase();

        if (!java.util.Arrays.asList(java.util.Locale.getISOLanguages()).contains(lowerLanguage)) {
            errors.rejectValue("language", "language.invalid",
                    String.format("Invalid language code: %s. Must be ISO 639-1 code (e.g., en, fr, de)", language));
        }
    }

    private void validatePhoneNumber(String phoneNumber, Errors errors) {
        // Remove common formatting characters
        String cleanPhone = phoneNumber.replaceAll("[\\s()\\-.]", "");

        // Check format: optional + followed by 7-15 digits
        if (!cleanPhone.matches("^\\+?[1-9]\\d{6,14}$")) {
            errors.rejectValue("phoneNumber", "phoneNumber.invalid",
                    "Invalid phone number format. Use international format (e.g., +1234567890)");
            return;
        }

        // Check if it's not all the same digit (e.g., 1111111111)
        if (cleanPhone.replaceAll("\\+", "").matches("^(\\d)\\1+$")) {
            errors.rejectValue("phoneNumber", "phoneNumber.pattern",
                    "Phone number cannot be all the same digit");
        }
    }

    private void validateAvatarUrl(String avatarUrl, Errors errors) {
        if (!CustomValidators.isValidUrl(avatarUrl)) {
            errors.rejectValue("avatarUrl", "avatarUrl.invalid",
                    "Invalid avatar URL format. Must be a valid HTTP/HTTPS URL");
            return;
        }

        // Check URL length
        if (avatarUrl.length() > 500) {
            errors.rejectValue("avatarUrl", "avatarUrl.tooLong",
                    "Avatar URL must not exceed 500 characters");
        }

        // Check if URL points to image (optional - based on extension)
        String lowerUrl = avatarUrl.toLowerCase();
        boolean isImageUrl = lowerUrl.endsWith(".jpg") || lowerUrl.endsWith(".jpeg") ||
                lowerUrl.endsWith(".png") || lowerUrl.endsWith(".gif") ||
                lowerUrl.endsWith(".webp") || lowerUrl.contains("image");

        if (!isImageUrl) {
            // Warning only - not an error
            errors.rejectValue("avatarUrl", "avatarUrl.notImage",
                    "Avatar URL should point to an image file (jpg, png, gif, webp)");
        }
    }

    private void validateUserGroupId(String userGroupId, Errors errors) {
        if (!CustomValidators.isValidUuid(userGroupId)) {
            errors.rejectValue("userGroupId", "userGroupId.invalidFormat",
                    "Invalid UUID format for user group ID");
        }
    }

    private void validateEmailUniqueness(String email, UUID userId, Errors errors) {
        if (userRepository.existsByEmailAndIdNot(email, userId)) {
            errors.rejectValue("email", "email.exists",
                    String.format("Email '%s' is already in use by another user", email));
        }
    }

    private void validateNoSqlInjection(UpdateUserRequest request, Errors errors) {
        // First name
        if (request.getFirstName() != null && !CustomValidators.isSqlInjectionFree(request.getFirstName())) {
            errors.rejectValue("firstName", "firstName.sqlInjection",
                    "First name contains invalid or potentially dangerous characters");
        }

        // Last name
        if (request.getLastName() != null && !CustomValidators.isSqlInjectionFree(request.getLastName())) {
            errors.rejectValue("lastName", "lastName.sqlInjection",
                    "Last name contains invalid or potentially dangerous characters");
        }

        // Display name
        if (request.getDisplayName() != null && !CustomValidators.isSqlInjectionFree(request.getDisplayName())) {
            errors.rejectValue("displayName", "displayName.sqlInjection",
                    "Display name contains invalid or potentially dangerous characters");
        }

        // Bio
        if (request.getBio() != null && !CustomValidators.isSqlInjectionFree(request.getBio())) {
            errors.rejectValue("bio", "bio.sqlInjection",
                    "Bio contains invalid or potentially dangerous characters");
        }
    }
}