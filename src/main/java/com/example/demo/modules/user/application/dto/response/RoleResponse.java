package com.example.demo.modules.user.application.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

/**
 * Role response DTO
 *
 * @author Architecture Team
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Role response")
public class RoleResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "Role ID")
    private UUID id;

    @Schema(description = "Role name", example = "ROLE_ADMIN")
    private String name;

    @Schema(description = "Display name", example = "Administrator")
    private String displayName;

    @Schema(description = "Description")
    private String description;

    @Schema(description = "Is system role (cannot be deleted)")
    private Boolean systemRole;

    @Schema(description = "Priority level")
    private Integer priority;

    @Schema(description = "Permission names")
    private Set<String> permissions;

    @Schema(description = "Number of permissions")
    private Integer permissionCount;

    @Schema(description = "Number of users with this role")
    private Integer userCount;

    @Schema(description = "Created timestamp")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @Schema(description = "Updated timestamp")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
}