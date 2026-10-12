package com.example.demo.modules.user.application.dto.request;

import com.example.demo.common.base.dto.BaseRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating a permission
 *
 * @author Architecture Team
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Update permission request")
public class UpdatePermissionRequest extends BaseRequest {

    private static final long serialVersionUID = 1L;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    @Schema(description = "Permission description")
    private String description;

    @Size(max = 100, message = "Category must not exceed 100 characters")
    @Schema(description = "Category for grouping")
    private String category;

    @Schema(description = "Display order")
    private Integer displayOrder;
}