package com.example.demo.modules.user.application.dto.request;

import com.example.demo.common.base.dto.BaseRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Request DTO for updating user
 *
 * @author Architecture Team
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Update user request")
public class UpdateUserRequest extends BaseRequest {

    private static final long serialVersionUID = 1L;

    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z\\s'-]+$", message = "First name contains invalid characters")
    @Schema(description = "User's first name", example = "John")
    private String firstName;

    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z\\s'-]+$", message = "Last name contains invalid characters")
    @Schema(description = "User's last name", example = "Doe")
    private String lastName;

    @Email(message = "Email must be valid")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    @Schema(description = "User's email address", example = "john.doe@example.com")
    private String email;

    @Past(message = "Date of birth must be in the past")
    @Schema(description = "User's date of birth", example = "1990-01-15")
    private LocalDate dateOfBirth;

    @Schema(description = "Phone number", example = "+1234567890")
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Phone number is invalid")
    private String phoneNumber;

    @Schema(description = "Display name", example = "Johnny")
    @Size(max = 100, message = "Display name must not exceed 100 characters")
    private String displayName;

    @Schema(description = "Bio/description", example = "Software developer")
    @Size(max = 1000, message = "Bio must not exceed 1000 characters")
    private String bio;

    @Schema(description = "Timezone", example = "America/New_York")
    private String timezone;

    @Schema(description = "Preferred language", example = "en")
    @Pattern(regexp = "^[a-z]{2}$", message = "Language must be a valid ISO 639-1 code")
    private String language;

    @Schema(description = "Country code", example = "US")
    @Pattern(regexp = "^[A-Z]{2}$", message = "Country must be a valid ISO 3166-1 alpha-2 code")
    private String country;

    @Schema(description = "Avatar URL")
    @Size(max = 500, message = "Avatar URL must not exceed 500 characters")
    private String avatarUrl;

    @Schema(description = "Enable/disable user account")
    private Boolean enabled;

    @Schema(description = "User group ID")
    private String userGroupId;
}