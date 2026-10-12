package com.example.demo.modules.user.application.dto.request;

import com.example.demo.common.base.dto.BaseRequest;
import com.example.demo.common.validator.constraints.ValidUUID;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.UUID;

/**
 * Request DTO for updating a role
 *
 * @author Architecture Team
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Update role request")
public class UpdateRoleRequest extends BaseRequest {

    private static final long serialVersionUID = 1L;

    @Size(max = 255, message = "Display name must not exceed 255 characters")
    @Schema(description = "Display name", example = "Manager")
    private String displayName;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    @Schema(description = "Role description")
    private String description;

    @Schema(description = "Role priority", example = "50")
    private Integer priority;

    @Schema(description = "Permission IDs to assign")
    private Set<UUID> permissionIds;
}