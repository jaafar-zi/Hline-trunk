package com.example.demo.modules.auth.application.dto.request;

import com.example.demo.common.base.dto.BaseRequest;
import com.example.demo.common.validator.constraints.ValidPassword;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Change password request (authenticated user)")
public class ChangePasswordRequest extends BaseRequest {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "Current password is required")
    @Schema(description = "Current password", required = true)
    private String currentPassword;

    @NotBlank(message = "New password is required")
    @ValidPassword(
            minLength = 8, maxLength = 100,
            requireUppercase = true, requireLowercase = true,
            requireDigit = true, requireSpecialChar = true,
            disallowCommon = true,
            strength = ValidPassword.StrengthLevel.MEDIUM
    )
    @Schema(description = "New password", example = "NewPassword123!", required = true)
    private String newPassword;

    @NotBlank(message = "Confirm password is required")
    @Schema(description = "Confirm new password", example = "NewPassword123!", required = true)
    private String confirmPassword;

    @Override
    public void validate() {
        if (newPassword != null && !newPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException("Passwords do not match");
        }
        if (currentPassword != null && currentPassword.equals(newPassword)) {
            throw new IllegalArgumentException(
                    "New password must be different from current password");
        }
    }
}