package com.example.demo.modules.user.application.dto.request;

import com.example.demo.common.base.dto.BaseRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for email verification
 *
 * @author Architecture Team
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Email verification request")
public class VerifyEmailRequest extends BaseRequest {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "Verification token is required")
    @Schema(description = "Email verification token from email", required = true)
    private String token;
}