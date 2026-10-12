package com.example.demo.modules.order.application.dto.request;

import com.example.demo.common.base.dto.BaseRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * Refund Order Request DTO
 * 
 * @author Order Team
 * @version 1.0
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request to refund an order")
public class RefundOrderRequest extends BaseRequest {

    @NotBlank(message = "Refund reason is required")
    @Size(max = 500, message = "Reason must not exceed 500 characters")
    @Schema(description = "Refund reason", required = true)
    private String reason;

    @Override
    public void normalize() {
        super.normalize();
        
        if (reason != null) {
            reason = reason.trim();
        }
    }
}