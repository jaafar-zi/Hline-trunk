package com.example.demo.modules.user.application.dto.request;

import com.example.demo.common.base.dto.BaseRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.UUID;

/**
 * Request DTO for updating a user group
 *
 * @author Architecture Team
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Update user group request")
public class UpdateUserGroupRequest extends BaseRequest {

    private static final long serialVersionUID = 1L;

    @Size(max = 100, message = "Group name must not exceed 100 characters")
    @Schema(description = "Group name", example = "Marketing Team")
    private String name;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    @Schema(description = "Group description")
    private String description;

    @Schema(description = "Group priority")
    private Integer priority;

    @Schema(description = "Active status")
    private Boolean active;

    @Schema(description = "Parent group ID")
    private UUID parentGroupId;

    @Schema(description = "Role IDs to assign")
    private Set<UUID> roleIds;
}