package com.example.demo.modules.user.application.dto.request;

import com.example.demo.common.base.dto.BaseRequest;
import com.example.demo.common.validator.constraints.ValidUUID;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.UUID;

/**
 * Request DTO for creating a user group
 *
 * @author Architecture Team
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Create user group request")
public class CreateUserGroupRequest extends BaseRequest {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "Group name is required")
    @Size(max = 100, message = "Group name must not exceed 100 characters")
    @Schema(description = "Group name", example = "Marketing Team", required = true)
    private String name;

    @NotBlank(message = "Group code is required")
    @Size(max = 50, message = "Group code must not exceed 50 characters")
    @Pattern(regexp = "^[A-Z0-9_]+$", message = "Group code must be uppercase letters, numbers, and underscores")
    @Schema(description = "Group code (unique identifier)", example = "MARKETING_TEAM", required = true)
    private String code;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    @Schema(description = "Group description")
    private String description;

    @Schema(description = "Group priority", example = "10")
    private Integer priority;

    @ValidUUID(allowNull = true)
    @Schema(description = "Parent group ID")
    private UUID parentGroupId;

    @Schema(description = "Role IDs to assign")
    private Set<UUID> roleIds;
}