package com.example.demo.modules.user.application.dto.request;

import com.example.demo.common.base.dto.BaseRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating a permission
 *
 * @author Architecture Team
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Create permission request")
public class CreatePermissionRequest extends BaseRequest {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "Resource is required")
    @Size(max = 50, message = "Resource must not exceed 50 characters")
    @Pattern(regexp = "^[A-Z_]+$", message = "Resource must be uppercase letters and underscores")
    @Schema(description = "Resource (e.g., USER, PRODUCT)", example = "USER", required = true)
    private String resource;

    @NotBlank(message = "Action is required")
    @Size(max = 50, message = "Action must not exceed 50 characters")
    @Pattern(regexp = "^[A-Z_]+$", message = "Action must be uppercase letters and underscores")
    @Schema(description = "Action (e.g., CREATE, READ, UPDATE, DELETE)", example = "READ", required = true)
    private String action;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    @Schema(description = "Permission description")
    private String description;

    @Size(max = 100, message = "Category must not exceed 100 characters")
    @Schema(description = "Category for grouping", example = "User Management")
    private String category;

    @Schema(description = "Display order", example = "10")
    private Integer displayOrder;
}