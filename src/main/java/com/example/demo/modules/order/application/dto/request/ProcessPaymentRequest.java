package com.example.demo.modules.order.application.dto.request;

import com.example.demo.common.base.dto.BaseRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * Process Payment Request DTO
 * 
 * @author Order Team
 * @version 1.0
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request to process payment for an order")
public class ProcessPaymentRequest extends BaseRequest {

    @NotBlank(message = "Payment method is required")
    @Size(max = 50, message = "Payment method must not exceed 50 characters")
    @Schema(description = "Payment method", required = true, example = "CREDIT_CARD")
    private String paymentMethod;

    @NotBlank(message = "Transaction ID is required")
    @Size(max = 255, message = "Transaction ID must not exceed 255 characters")
    @Schema(description = "Payment transaction ID", required = true)
    private String transactionId;

    @Override
    public void normalize() {
        super.normalize();
        
        if (paymentMethod != null) {
            paymentMethod = paymentMethod.trim().toUpperCase();
        }
        
        if (transactionId != null) {
            transactionId = transactionId.trim();
        }
    }
}