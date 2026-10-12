package com.example.demo.modules.auth.application.dto.request;

import com.example.demo.common.base.dto.BaseRequest;
import com.example.demo.common.validator.constraints.ValidEmail;
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
@Schema(description = "Forgot password request")
public class ForgotPasswordRequest extends BaseRequest {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "Email is required")
    @ValidEmail(message = "Invalid email format", maxLength = 255, allowPlusAddressing = true)
    @Schema(description = "Email address to send reset link to",
            example = "john.doe@example.com", required = true)
    private String email;

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