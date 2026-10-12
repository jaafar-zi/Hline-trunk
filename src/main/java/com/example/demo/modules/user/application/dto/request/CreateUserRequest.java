package com.example.demo.modules.user.application.dto.request;

import com.example.demo.common.base.dto.BaseRequest;
import com.example.demo.common.validator.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Set;

/**
 * Request DTO for creating a new user
 *
 * @author Architecture Team
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Create user request")
public class CreateUserRequest extends BaseRequest {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z\\s'-]+$", message = "First name contains invalid characters")
    @Schema(description = "User's first name", example = "John", required = true)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z\\s'-]+$", message = "Last name contains invalid characters")
    @Schema(description = "User's last name", example = "Doe", required = true)
    private String lastName;

    @NotBlank(message = "Email is required")
    @ValidEmail(
            message = "Invalid email format",
            maxLength = 255,
            allowPlusAddressing = true
    )
    @UniqueField(
            entity = com.example.demo.modules.user.domain.model.User.class,
            field = "email",
            message = "Email already exists"
    )
    @Schema(description = "User's email address", example = "john.doe@example.com", required = true)
    private String email;

    @NotBlank(message = "Password is required")
    @ValidPassword(
            minLength = 8,
            maxLength = 100,
            requireUppercase = true,
            requireLowercase = true,
            requireDigit = true,
            requireSpecialChar = true,
            disallowCommon = true,
            strength = com.example.demo.common.validator.constraints.ValidPassword.StrengthLevel.MEDIUM
    )
    @Schema(description = "User's password (min 8 chars, must include uppercase, lowercase, number, special char)",
            example = "Password123!", required = true)
    private String password;

    @Schema(description = "Confirm password (must match password)", example = "Password123!")
    private String confirmPassword;

    @NotFutureDate(message = "Date of birth cannot be in the future", allowToday = false)
    @ValidDateRange(
            useOffset = true,
            minDaysOffset = -43800, // Approx. 120 years ago
            maxDaysOffset = -4745,   // Approx. 13 years ago (COPPA compliance)
            message = "User must be between 13 and 120 years old"
    )
    @Schema(description = "User's date of birth", example = "1990-01-15")
    private LocalDate dateOfBirth;

    @ValidPhone(
            allowNull = true,
            requireCountryCode = false,
            allowExtension = true,
            minDigits = 7,
            maxDigits = 15
    )
    @Schema(description = "Phone number", example = "+1234567890")
    private String phoneNumber;

    @Schema(description = "Role IDs to assign to user")
    private Set<String> roleIds;

    @ValidUUID(allowNull = true)
    @Schema(description = "User group ID")
    private String userGroupId;

    @Size(max = 100, message = "Display name must not exceed 100 characters")
    @Schema(description = "Display name", example = "Johnny")
    private String displayName;

    @Size(max = 1000, message = "Bio must not exceed 1000 characters")
    @Schema(description = "Bio/description", example = "Software developer")
    private String bio;

    @ValidTimezone
    @Schema(description = "Timezone", example = "America/New_York")
    private String timezone;

    @Pattern(regexp = "^[a-z]{2}$", message = "Language must be a valid ISO 639-1 code")
    @Schema(description = "Preferred language", example = "en")
    private String language;

    @Pattern(regexp = "^[A-Z]{2}$", message = "Country must be a valid ISO 3166-1 alpha-2 code")
    @Schema(description = "Country code", example = "US")
    private String country;

    @Override
    public void validate() {
        super.validate();

        if (confirmPassword != null && !confirmPassword.equals(password)) {
            throw new IllegalArgumentException("Passwords do not match");
        }
    }
}