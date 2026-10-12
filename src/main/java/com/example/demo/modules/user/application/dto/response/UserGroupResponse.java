package com.example.demo.modules.user.application.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

/**
 * User group response DTO
 *
 * @author Architecture Team
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "User group response")
public class UserGroupResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "Group ID")
    private UUID id;

    @Schema(description = "Group name")
    private String name;

    @Schema(description = "Group code")
    private String code;

    @Schema(description = "Description")
    private String description;

    @Schema(description = "Is active")
    private Boolean active;

    @Schema(description = "Priority")
    private Integer priority;

    @Schema(description = "Number of users in group")
    private Integer userCount;

    @Schema(description = "Number of active users in group")
    private Long activeUserCount;

    @Schema(description = "Number of roles assigned to this group")
    private Integer roleCount;

    @Schema(description = "Number of child groups")
    private Integer childGroupCount;

    @Schema(description = "Whether this group has a parent group")
    private Boolean hasParent;

    @Schema(description = "Parent group ID")
    private UUID parentGroupId;

    @Schema(description = "Parent group name")
    private String parentGroupName;

    @Schema(description = "Depth in group hierarchy (0 for root groups)")
    private Integer depth;
}