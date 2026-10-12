package com.example.demo.modules.order.application.dto.request;

import com.example.demo.common.base.dto.BaseRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Ship Order Request DTO
 * 
 * @author Order Team
 * @version 1.0
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request to ship an order")
public class ShipOrderRequest extends BaseRequest {

    @NotBlank(message = "Tracking number is required")
    @Size(max = 255, message = "Tracking number must not exceed 255 characters")
    @Schema(description = "Tracking number", required = true)
    private String trackingNumber;

    @NotBlank(message = "Carrier is required")
    @Size(max = 100, message = "Carrier must not exceed 100 characters")
    @Schema(description = "Shipping carrier", required = true, example = "FedEx")
    private String carrier;

    @Future(message = "Estimated delivery date must be in the future")
    @Schema(description = "Estimated delivery date")
    private LocalDateTime estimatedDeliveryDate;

    @Override
    public void normalize() {
        super.normalize();
        
        if (trackingNumber != null) {
            trackingNumber = trackingNumber.trim().toUpperCase();
        }
        
        if (carrier != null) {
            carrier = carrier.trim();
        }
    }
}