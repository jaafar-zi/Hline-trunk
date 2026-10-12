// LoginRequest.java
package com.example.demo.modules.auth.application.dto.request;

import com.example.demo.common.base.dto.BaseRequest;
import com.example.demo.common.validator.constraints.ValidEmail;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Login request – shared by User and Customer auth flows.
 *
 * The `subjectType` field tells the auth service which aggregate to look up.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Login request")
public class LoginRequest extends BaseRequest {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "Email is required")
    @ValidEmail(message = "Invalid email format", maxLength = 255, allowPlusAddressing = true)
    @Schema(description = "Email address", example = "john.doe@example.com", required = true)
    private String email;

    @NotBlank(message = "Password is required")
    @Schema(description = "Password", example = "Password123!", required = true)
    private String password;

    /**
     * Identifies which principal type to authenticate.
     * Defaults to USER if not provided.
     */
    @Schema(description = "Subject type: USER or CUSTOMER", example = "USER")
    @Builder.Default
    private String subjectType = "USER";

    @Override
    public void normalize() {
        super.normalize();
        if (email       != null) email       = email.trim().toLowerCase();
        if (subjectType != null) subjectType = subjectType.trim().toUpperCase();
    }
}