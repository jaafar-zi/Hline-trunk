package com.example.demo.modules.user.application.dto.request;

import com.example.demo.common.base.dto.BaseRequest;
import com.example.demo.common.validator.constraints.ValidUUID;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * Request DTO for creating a role
 *
 * @author Architecture Team
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Create role request")
public class CreateRoleRequest extends BaseRequest {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "Role name is required")
    @Size(max = 100, message = "Role name must not exceed 100 characters")
    @Schema(description = "Role name (e.g., ROLE_ADMIN)", example = "ROLE_MANAGER", required = true)
    private String name;

    @Size(max = 255, message = "Display name must not exceed 255 characters")
    @Schema(description = "Display name", example = "Manager")
    private String displayName;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    @Schema(description = "Role description")
    private String description;

    @Schema(description = "Role priority (higher = more privileged)", example = "50")
    private Integer priority;

    @Schema(description = "Permission IDs to assign")
    private Set<String> permissionIds;
}