package com.example.demo.modules.user.application.validator;

import com.example.demo.common.validator.CustomValidators;
import com.example.demo.common.validator.DateValidator;
import com.example.demo.modules.user.application.dto.request.CreateUserRequest;
import com.example.demo.modules.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.time.LocalDate;

/**
 * Spring Validator for CreateUserRequest
 * Handles business logic validation using core validators
 *
 * @author Architecture Team
 * @version 1.0
 */
@Component
@RequiredArgsConstructor
public class CreateUserRequestValidator implements Validator {

    private final UserRepository userRepository;

    @Override
    public boolean supports(Class<?> clazz) {
        return CreateUserRequest.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        CreateUserRequest request = (CreateUserRequest) target;

        // Email uniqueness check
        validateEmailUniqueness(request.getEmail(), errors);

        // Password confirmation
        validatePasswordConfirmation(request.getPassword(), request.getConfirmPassword(), errors);

        // Age validation
        validateAge(request.getDateOfBirth(), errors);

        // Timezone validation
        validateTimezone(request.getTimezone(), errors);

        // Country code validation
        validateCountryCode(request.getCountry(), errors);

        // Language code validation
        validateLanguageCode(request.getLanguage(), errors);

        // SQL injection check
        validateNoSqlInjection(request, errors);

        // Phone number validation (if provided)
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().isBlank()) {
            validatePhoneNumber(request.getPhoneNumber(), errors);
        }
    }

    private void validateEmailUniqueness(String email, Errors errors) {
        if (email != null && !email.isBlank()) {
            if (userRepository.existsByEmail(email)) {
                errors.rejectValue("email", "email.exists",
                        "Email address is already registered");
            }
        }
    }

    private void validatePasswordConfirmation(String password, String confirmPassword, Errors errors) {
        if (password != null && confirmPassword != null && !password.equals(confirmPassword)) {
            errors.rejectValue("confirmPassword", "password.mismatch",
                    "Password confirmation does not match");
        }
    }

    private void validateAge(LocalDate dateOfBirth, Errors errors) {
        if (dateOfBirth != null) {
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
                        "Invalid date of birth");
            }
        }
    }

    private void validateTimezone(String timezone, Errors errors) {
        if (timezone != null && !timezone.isBlank()) {
            try {
                java.time.ZoneId.of(timezone);
            } catch (Exception e) {
                errors.rejectValue("timezone", "timezone.invalid",
                        "Invalid timezone: " + timezone);
            }
        }
    }

    private void validateCountryCode(String country, Errors errors) {
        if (country != null && !country.isBlank()) {
            if (!java.util.Arrays.asList(java.util.Locale.getISOCountries()).contains(country.toUpperCase())) {
                errors.rejectValue("country", "country.invalid",
                        "Invalid ISO 3166-1 alpha-2 country code");
            }
        }
    }

    private void validateLanguageCode(String language, Errors errors) {
        if (language != null && !language.isBlank()) {
            if (!java.util.Arrays.asList(java.util.Locale.getISOLanguages()).contains(language.toLowerCase())) {
                errors.rejectValue("language", "language.invalid",
                        "Invalid ISO 639-1 language code");
            }
        }
    }

    private void validatePhoneNumber(String phoneNumber, Errors errors) {
        // Remove formatting for validation
        String cleanPhone = phoneNumber.replaceAll("[\\s()-]", "");

        if (!cleanPhone.matches("^\\+?[1-9]\\d{1,14}$")) {
            errors.rejectValue("phoneNumber", "phoneNumber.invalid",
                    "Invalid phone number format. Use international format (e.g., +1234567890)");
        }
    }

    private void validateNoSqlInjection(CreateUserRequest request, Errors errors) {
        // Check all string fields for SQL injection patterns
        if (!CustomValidators.isSqlInjectionFree(request.getFirstName())) {
            errors.rejectValue("firstName", "firstName.sqlInjection",
                    "Invalid characters detected in first name");
        }

        if (!CustomValidators.isSqlInjectionFree(request.getLastName())) {
            errors.rejectValue("lastName", "lastName.sqlInjection",
                    "Invalid characters detected in last name");
        }

        if (!CustomValidators.isSqlInjectionFree(request.getDisplayName())) {
            errors.rejectValue("displayName", "displayName.sqlInjection",
                    "Invalid characters detected in display name");
        }

        if (!CustomValidators.isSqlInjectionFree(request.getBio())) {
            errors.rejectValue("bio", "bio.sqlInjection",
                    "Invalid characters detected in bio");
        }
    }
}
