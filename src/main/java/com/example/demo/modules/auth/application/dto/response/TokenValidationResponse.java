package com.example.demo.modules.auth.application.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Token validation response")
public class TokenValidationResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "Is the token valid", example = "true")
    private boolean valid;

    @Schema(description = "Principal ID")
    private String subjectId;

    @Schema(description = "Principal type", example = "USER")
    private String subjectType;

    @Schema(description = "Email address")
    private String email;

    @Schema(description = "Granted authorities")
    private Set<String> authorities;

    @Schema(description = "Reason token is invalid (if applicable)")
    private String reason;
}