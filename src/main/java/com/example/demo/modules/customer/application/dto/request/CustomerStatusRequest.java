package com.example.demo.modules.customer.application.dto.request;

import com.example.demo.common.base.dto.BaseRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for changing customer status (suspend/block)
 * Carries the reason for the status change.
 *
 * @author Customer Domain Team
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Customer status change request")
public class CustomerStatusRequest extends BaseRequest {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "Reason is required")
    @Size(min = 5, max = 500, message = "Reason must be between 5 and 500 characters")
    @Schema(description = "Reason for the status change", example = "Policy violation", required = true)
    private String reason;

    @Override
    public void normalize() {
        super.normalize();
        if (reason != null) reason = reason.trim();
    }
}