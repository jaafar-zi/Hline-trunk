package com.example.demo.modules.user.domain.model.vo;

import com.example.demo.common.base.entity.BaseValueObject;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.ZoneId;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * UserProfile Value Object
 *
 * Contains additional user profile information
 * Immutable and self-validating
 * Embedded in User entity
 *
 * @author Architecture Team
 * @version 1.0
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // For JPA only
public class UserProfile extends BaseValueObject {

    private static final long serialVersionUID = 1L;

    // Validation constants
    private static final int MAX_DISPLAY_NAME_LENGTH = 100;
    private static final int MAX_PHONE_LENGTH = 20;
    private static final int MAX_AVATAR_URL_LENGTH = 500;
    private static final int MAX_BIO_LENGTH = 1000;
    private static final int MAX_TIMEZONE_LENGTH = 50;
    private static final int MAX_LANGUAGE_LENGTH = 10;
    private static final int COUNTRY_CODE_LENGTH = 2;

    // Phone validation pattern (international format)
    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^\\+?[1-9]\\d{1,14}$|^\\d{10,15}$"
    );

    // URL validation pattern (simplified)
    private static final Pattern URL_PATTERN = Pattern.compile(
            "^(https?://)?([\\da-z.-]+)\\.([a-z.]{2,6})([/\\w .-]*)*/?$"
    );

    /**
     * Display name (can be different from first+last name)
     */
    @Column(name = "display_name", length = MAX_DISPLAY_NAME_LENGTH)
    private String displayName;

    /**
     * Phone number (international format preferred)
     */
    @Column(name = "phone_number", length = MAX_PHONE_LENGTH)
    private String phoneNumber;

    /**
     * Profile photo URL
     */
    @Column(name = "avatar_url", length = MAX_AVATAR_URL_LENGTH)
    private String avatarUrl;

    /**
     * Bio/description
     */
    @Column(name = "bio", length = MAX_BIO_LENGTH)
    private String bio;

    /**
     * Time zone (e.g., "America/New_York", "Europe/Paris")
     */
    @Column(name = "timezone", length = MAX_TIMEZONE_LENGTH)
    private String timezone;

    /**
     * Preferred language (ISO 639-1 code, e.g., "en", "fr")
     */
    @Column(name = "language", length = MAX_LANGUAGE_LENGTH)
    private String language;

    /**
     * Country code (ISO 3166-1 alpha-2, e.g., "US", "FR")
     */
    @Column(name = "country", length = COUNTRY_CODE_LENGTH)
    private String country;

    /**
     * Private constructor - use builder or factory methods
     */
    private UserProfile(String displayName, String phoneNumber, String avatarUrl,
                        String bio, String timezone, String language, String country) {
        this.displayName = displayName;
        this.phoneNumber = phoneNumber;
        this.avatarUrl = avatarUrl;
        this.bio = bio;
        this.timezone = timezone;
        this.language = language;
        this.country = country;
        validate();
    }

    // ====================================
    // Factory Methods
    // ====================================

    /**
     * Create default profile with UTC timezone and English language
     */
    public static UserProfile createDefault() {
        return new UserProfile(null, null, null, null, "UTC", "en", null);
    }

    /**
     * Create profile with basic info
     */
    public static UserProfile create(String displayName, String phoneNumber) {
        return new UserProfile(displayName, phoneNumber, null, null, "UTC", "en", null);
    }

    /**
     * Create profile with all fields
     */
    public static UserProfile of(String displayName, String phoneNumber, String avatarUrl,
                                 String bio, String timezone, String language, String country) {
        return new UserProfile(displayName, phoneNumber, avatarUrl, bio, timezone, language, country);
    }

    /**
     * Builder for fluent construction
     */
    public static Builder builder() {
        return new Builder();
    }

    // ====================================
    // Validation
    // ====================================

    @Override
    protected void validate() {
        validateDisplayName();
        validatePhoneNumber();
        validateAvatarUrl();
        validateBio();
        validateTimezone();
        validateLanguage();
        validateCountry();
    }

    private void validateDisplayName() {
        if (displayName != null) {
            if (displayName.isBlank()) {
                throw new IllegalArgumentException("Display name cannot be blank");
            }
            if (displayName.length() > MAX_DISPLAY_NAME_LENGTH) {
                throw new IllegalArgumentException(
                        String.format("Display name exceeds maximum length of %d characters",
                                MAX_DISPLAY_NAME_LENGTH)
                );
            }
        }
    }

    private void validatePhoneNumber() {
        if (phoneNumber != null && !phoneNumber.isBlank()) {
            // Remove common formatting characters for validation
            String cleanPhone = phoneNumber.replaceAll("[\\s()-]", "");

            if (!PHONE_PATTERN.matcher(cleanPhone).matches()) {
                throw new IllegalArgumentException("Invalid phone number format: " + phoneNumber);
            }

            if (cleanPhone.length() > MAX_PHONE_LENGTH) {
                throw new IllegalArgumentException(
                        String.format("Phone number exceeds maximum length of %d", MAX_PHONE_LENGTH)
                );
            }
        }
    }

    private void validateAvatarUrl() {
        if (avatarUrl != null && !avatarUrl.isBlank()) {
            if (avatarUrl.length() > MAX_AVATAR_URL_LENGTH) {
                throw new IllegalArgumentException(
                        String.format("Avatar URL exceeds maximum length of %d", MAX_AVATAR_URL_LENGTH)
                );
            }

            if (!URL_PATTERN.matcher(avatarUrl.toLowerCase()).matches()) {
                throw new IllegalArgumentException("Invalid avatar URL format");
            }
        }
    }

    private void validateBio() {
        if (bio != null && bio.length() > MAX_BIO_LENGTH) {
            throw new IllegalArgumentException(
                    String.format("Bio exceeds maximum length of %d characters", MAX_BIO_LENGTH)
            );
        }
    }

    private void validateTimezone() {
        if (timezone != null && !timezone.isBlank()) {
            try {
                ZoneId.of(timezone);
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid timezone: " + timezone);
            }
        }
    }

    private void validateLanguage() {
        if (language != null && !language.isBlank()) {
            if (language.length() > MAX_LANGUAGE_LENGTH) {
                throw new IllegalArgumentException("Invalid language code length");
            }

            // Validate against ISO 639-1 codes
            Set<String> validLanguages = Set.of(Locale.getISOLanguages());
            if (!validLanguages.contains(language.toLowerCase())) {
                throw new IllegalArgumentException("Invalid ISO 639-1 language code: " + language);
            }
        }
    }

    private void validateCountry() {
        if (country != null && !country.isBlank()) {
            if (country.length() != COUNTRY_CODE_LENGTH) {
                throw new IllegalArgumentException(
                        "Country code must be exactly 2 characters (ISO 3166-1 alpha-2)"
                );
            }

            // Validate against ISO 3166-1 alpha-2 codes
            Set<String> validCountries = Set.of(Locale.getISOCountries());
            if (!validCountries.contains(country.toUpperCase())) {
                throw new IllegalArgumentException("Invalid ISO 3166-1 alpha-2 country code: " + country);
            }
        }
    }

    // ====================================
    // Business Methods (Immutable Updates)
    // ====================================

    /**
     * Update profile (returns new instance)
     */
    public UserProfile update(String displayName, String phoneNumber, String bio,
                              String timezone, String language) {
        return new UserProfile(
                displayName,
                phoneNumber,
                this.avatarUrl, // Keep existing avatar
                bio,
                timezone != null ? timezone : this.timezone,
                language != null ? language : this.language,
                this.country // Keep existing country
        );
    }

    /**
     * Update display name (returns new instance)
     */
    public UserProfile withDisplayName(String newDisplayName) {
        return new UserProfile(newDisplayName, this.phoneNumber, this.avatarUrl,
                this.bio, this.timezone, this.language, this.country);
    }

    /**
     * Update phone number (returns new instance)
     */
    public UserProfile withPhoneNumber(String newPhoneNumber) {
        return new UserProfile(this.displayName, newPhoneNumber, this.avatarUrl,
                this.bio, this.timezone, this.language, this.country);
    }

    /**
     * Update avatar URL (returns new instance)
     */
    public UserProfile withAvatarUrl(String newAvatarUrl) {
        return new UserProfile(this.displayName, this.phoneNumber, newAvatarUrl,
                this.bio, this.timezone, this.language, this.country);
    }

    /**
     * Update bio (returns new instance)
     */
    public UserProfile withBio(String newBio) {
        return new UserProfile(this.displayName, this.phoneNumber, this.avatarUrl,
                newBio, this.timezone, this.language, this.country);
    }

    /**
     * Update timezone (returns new instance)
     */
    public UserProfile withTimezone(String newTimezone) {
        return new UserProfile(this.displayName, this.phoneNumber, this.avatarUrl,
                this.bio, newTimezone, this.language, this.country);
    }

    /**
     * Update language (returns new instance)
     */
    public UserProfile withLanguage(String newLanguage) {
        return new UserProfile(this.displayName, this.phoneNumber, this.avatarUrl,
                this.bio, this.timezone, newLanguage, this.country);
    }

    /**
     * Update country (returns new instance)
     */
    public UserProfile withCountry(String newCountry) {
        return new UserProfile(this.displayName, this.phoneNumber, this.avatarUrl,
                this.bio, this.timezone, this.language, newCountry);
    }

    // ====================================
    // Query Methods
    // ====================================

    /**
     * Check if profile is complete (all fields filled)
     */
    public boolean isComplete() {
        return displayName != null && !displayName.isBlank()
                && phoneNumber != null && !phoneNumber.isBlank()
                && avatarUrl != null && !avatarUrl.isBlank()
                && bio != null && !bio.isBlank()
                && timezone != null
                && language != null
                && country != null;
    }

    /**
     * Check if profile has basic info
     */
    public boolean hasBasicInfo() {
        return displayName != null && !displayName.isBlank()
                && phoneNumber != null && !phoneNumber.isBlank();
    }

    /**
     * Check if profile has avatar
     */
    public boolean hasAvatar() {
        return avatarUrl != null && !avatarUrl.isBlank();
    }

    /**
     * Get profile completion percentage
     */
    public int getCompletionPercentage() {
        int totalFields = 7;
        int filledFields = 0;

        if (displayName != null && !displayName.isBlank()) filledFields++;
        if (phoneNumber != null && !phoneNumber.isBlank()) filledFields++;
        if (avatarUrl != null && !avatarUrl.isBlank()) filledFields++;
        if (bio != null && !bio.isBlank()) filledFields++;
        if (timezone != null && !timezone.equals("UTC")) filledFields++;
        if (language != null && !language.equals("en")) filledFields++;
        if (country != null && !country.isBlank()) filledFields++;

        return (filledFields * 100) / totalFields;
    }

    /**
     * Get formatted phone number (display-friendly)
     */
    public String getFormattedPhoneNumber() {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            return null;
        }

        // Simple formatting - can be enhanced based on country
        String clean = phoneNumber.replaceAll("[^0-9+]", "");

        if (clean.startsWith("+")) {
            // International format
            return clean.replaceFirst("^(\\+\\d{1,3})(\\d{3})(\\d{3})(\\d+)$", "$1 $2 $3 $4");
        } else if (clean.length() == 10) {
            // US format
            return clean.replaceFirst("^(\\d{3})(\\d{3})(\\d{4})$", "($1) $2-$3");
        }

        return phoneNumber;
    }

    /**
     * Get timezone display name
     */
    public String getTimezoneDisplayName() {
        if (timezone == null || timezone.isBlank()) {
            return "UTC";
        }

        try {
            ZoneId zoneId = ZoneId.of(timezone);
            return zoneId.getDisplayName(java.time.format.TextStyle.FULL, Locale.ENGLISH);
        } catch (Exception e) {
            return timezone;
        }
    }

    /**
     * Get language display name
     */
    public String getLanguageDisplayName() {
        if (language == null || language.isBlank()) {
            return "English";
        }

        Locale locale = new Locale(language);
        return locale.getDisplayLanguage(Locale.ENGLISH);
    }

    /**
     * Get country display name
     */
    public String getCountryDisplayName() {
        if (country == null || country.isBlank()) {
            return null;
        }

        Locale locale = new Locale("", country);
        return locale.getDisplayCountry(Locale.ENGLISH);
    }

    // ====================================
    // Value Object Contract
    // ====================================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserProfile that = (UserProfile) o;
        return Objects.equals(displayName, that.displayName) &&
                Objects.equals(phoneNumber, that.phoneNumber) &&
                Objects.equals(avatarUrl, that.avatarUrl) &&
                Objects.equals(bio, that.bio) &&
                Objects.equals(timezone, that.timezone) &&
                Objects.equals(language, that.language) &&
                Objects.equals(country, that.country);
    }

    @Override
    public int hashCode() {
        return Objects.hash(displayName, phoneNumber, avatarUrl, bio, timezone, language, country);
    }

    @Override
    public String toString() {
        return String.format("UserProfile[displayName=%s, phone=%s, timezone=%s, language=%s, completion=%d%%]",
                displayName,
                phoneNumber != null ? maskPhoneNumber() : "null",
                timezone,
                language,
                getCompletionPercentage());
    }

    /**
     * Mask phone number for display
     */
    private String maskPhoneNumber() {
        if (phoneNumber == null || phoneNumber.length() < 4) {
            return "****";
        }
        return "****" + phoneNumber.substring(phoneNumber.length() - 4);
    }

    // ====================================
    // Builder
    // ====================================

    public static class Builder {
        private String displayName;
        private String phoneNumber;
        private String avatarUrl;
        private String bio;
        private String timezone = "UTC";
        private String language = "en";
        private String country;

        private Builder() {}

        public Builder displayName(String displayName) {
            this.displayName = displayName;
            return this;
        }

        public Builder phoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
            return this;
        }

        public Builder avatarUrl(String avatarUrl) {
            this.avatarUrl = avatarUrl;
            return this;
        }

        public Builder bio(String bio) {
            this.bio = bio;
            return this;
        }

        public Builder timezone(String timezone) {
            this.timezone = timezone;
            return this;
        }

        public Builder language(String language) {
            this.language = language;
            return this;
        }

        public Builder country(String country) {
            this.country = country;
            return this;
        }

        public UserProfile build() {
            return new UserProfile(displayName, phoneNumber, avatarUrl, bio,
                    timezone, language, country);
        }
    }
}