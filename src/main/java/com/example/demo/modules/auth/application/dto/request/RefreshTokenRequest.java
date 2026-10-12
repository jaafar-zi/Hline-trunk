package com.example.demo.modules.auth.application.dto.request;

import com.example.demo.common.base.dto.BaseRequest;
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
@Schema(description = "Refresh token request")
public class RefreshTokenRequest extends BaseRequest {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "Refresh token is required")
    @Schema(description = "JWT refresh token", required = true)
    private String refreshToken;
}