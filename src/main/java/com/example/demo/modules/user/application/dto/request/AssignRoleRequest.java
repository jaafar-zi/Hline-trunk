package com.example.demo.modules.user.application.dto.request;

import com.example.demo.common.base.dto.BaseRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * Request DTO for assigning/removing roles
 *
 * @author Architecture Team
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Assign roles to user request")
public class AssignRoleRequest extends BaseRequest {

    private static final long serialVersionUID = 1L;

    @NotEmpty(message = "Role IDs are required")
    @Schema(description = "Role IDs to assign", required = true)
    private Set<String> roleIds;

    @Schema(description = "Replace existing roles (true) or add to existing (false)", example = "false")
    @Builder.Default
    private Boolean replaceExisting = false;
}