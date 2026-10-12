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
 * Permission response DTO
 *
 * @author Architecture Team
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Permission response")
public class PermissionResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "Permission ID")
    private UUID id;

    @Schema(description = "Permission name", example = "USER:READ")
    private String name;

    @Schema(description = "Resource", example = "USER")
    private String resource;

    @Schema(description = "Action", example = "READ")
    private String action;

    @Schema(description = "Description")
    private String description;

    @Schema(description = "Category")
    private String category;

    @Schema(description = "Is system permission")
    private Boolean systemPermission;

    @Schema(description = "Display order")
    private Integer displayOrder;

    @Schema(description = "Number of roles with this permission")
    private Integer roleCount;
}