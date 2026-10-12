package com.example.demo.modules.user.application.dto.request;

import com.example.demo.common.base.dto.BaseRequest;
import com.example.demo.common.validator.constraints.NotFutureDate;
import com.example.demo.common.validator.constraints.ValidPhone;
import com.example.demo.common.validator.constraints.ValidTimezone;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Request DTO for updating user profile (self-service)
 *
 * @author Architecture Team
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Update user profile request (self-service)")
public class UpdateUserProfileRequest extends BaseRequest {

    private static final long serialVersionUID = 1L;

    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z\\s'-]+$", message = "First name contains invalid characters")
    @Schema(description = "User's first name", example = "John")
    private String firstName;

    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z\\s'-]+$", message = "Last name contains invalid characters")
    @Schema(description = "User's last name", example = "Doe")
    private String lastName;

    @Size(max = 100, message = "Display name must not exceed 100 characters")
    @Schema(description = "Display name", example = "Johnny")
    private String displayName;

    @NotFutureDate(message = "Date of birth cannot be in the future", allowToday = false, allowNull = true)
    @Schema(description = "Date of birth", example = "1990-01-15")
    private LocalDate dateOfBirth;

    @ValidPhone(
            allowNull = true,
            requireCountryCode = false,
            allowExtension = true
    )
    @Schema(description = "Phone number", example = "+1234567890")
    private String phoneNumber;

    @Size(max = 1000, message = "Bio must not exceed 1000 characters")
    @Schema(description = "Bio/About me", example = "Software developer passionate about clean code")
    private String bio;

    @Size(max = 500, message = "Avatar URL must not exceed 500 characters")
    @Pattern(regexp = "^(https?://)?([\\da-z.-]+)\\.([a-z.]{2,6})([/\\w .-]*)*/?$", message = "Invalid URL format")
    @Schema(description = "Avatar/Profile picture URL", example = "https://example.com/avatar.jpg")
    private String avatarUrl;

    @ValidTimezone
    @Schema(description = "Timezone", example = "America/New_York")
    private String timezone;

    @Pattern(regexp = "^[a-z]{2}$", message = "Language must be a valid ISO 639-1 code")
    @Schema(description = "Preferred language (ISO 639-1 code)", example = "en")
    private String language;

    @Pattern(regexp = "^[A-Z]{2}$", message = "Country must be a valid ISO 3166-1 alpha-2 code")
    @Schema(description = "Country (ISO 3166-1 alpha-2 code)", example = "US")
    private String country;

    @Size(max = 500, message = "Website URL must not exceed 500 characters")
    @Pattern(regexp = "^(https?://)?([\\da-z.-]+)\\.([a-z.]{2,6})([/\\w .-]*)*/?$", message = "Invalid URL format")
    @Schema(description = "Website URL", example = "https://johndoe.com")
    private String websiteUrl;

    @Size(max = 500, message = "LinkedIn URL must not exceed 500 characters")
    @Pattern(regexp = "^(https?://)?(www\\.)?linkedin\\.com/.*$", message = "Invalid LinkedIn URL")
    @Schema(description = "LinkedIn profile URL", example = "https://linkedin.com/in/johndoe")
    private String linkedinUrl;

    @Size(max = 500, message = "GitHub URL must not exceed 500 characters")
    @Pattern(regexp = "^(https?://)?(www\\.)?github\\.com/.*$", message = "Invalid GitHub URL")
    @Schema(description = "GitHub profile URL", example = "https://github.com/johndoe")
    private String githubUrl;

    @Size(max = 50, message = "Twitter handle must not exceed 50 characters")
    @Pattern(regexp = "^@?[A-Za-z0-9_]{1,15}$", message = "Twitter handle is invalid")
    @Schema(description = "Twitter handle", example = "@johndoe")
    private String twitterHandle;

    @Schema(description = "Email notification preferences")
    private NotificationPreferences notificationPreferences;

    @Schema(description = "Privacy settings")
    private PrivacySettings privacySettings;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Notification preferences")
    public static class NotificationPreferences {
        @Builder.Default
        private Boolean emailNotifications = true;
        @Builder.Default
        private Boolean pushNotifications = true;
        @Builder.Default
        private Boolean smsNotifications = false;
        @Builder.Default
        private Boolean newsletter = true;
        @Builder.Default
        private Boolean productUpdates = true;
        @Builder.Default
        private Boolean securityAlerts = true;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Privacy settings")
    public static class PrivacySettings {
        @Builder.Default
        private Boolean publicProfile = true;
        @Builder.Default
        private Boolean showEmail = false;
        @Builder.Default
        private Boolean showPhone = false;
        @Builder.Default
        private Boolean searchable = true;
        @Builder.Default
        private Boolean showOnlineStatus = true;
    }
}