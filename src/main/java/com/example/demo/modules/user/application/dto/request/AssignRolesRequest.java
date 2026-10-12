package com.example.demo.modules.user.application.dto.request;

import com.example.demo.common.base.dto.BaseRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.UUID;

/**
 * Request DTO for assigning roles
 *
 * @author Architecture Team
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Assign roles request")
public class AssignRolesRequest extends BaseRequest {

    private static final long serialVersionUID = 1L;

    @NotEmpty(message = "Role IDs are required")
    @Schema(description = "Role IDs to assign", required = true)
    private Set<UUID> roleIds;
}