package com.example.demo.modules.order.application.dto.request;

import com.example.demo.common.base.dto.BaseRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * Update Order Request DTO
 * 
 * @author Order Team
 * @version 1.0
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request to update order details")
public class UpdateOrderRequest extends BaseRequest {

    @Valid
    @Schema(description = "Updated shipping address")
    private AddressRequest shippingAddress;

    @Valid
    @Schema(description = "Updated billing address")
    private AddressRequest billingAddress;

    @Size(max = 1000, message = "Customer notes must not exceed 1000 characters")
    @Schema(description = "Updated customer notes")
    private String customerNotes;

    @Size(max = 1000, message = "Internal notes must not exceed 1000 characters")
    @Schema(description = "Updated internal notes")
    private String internalNotes;

    @Override
    public void normalize() {
        super.normalize();
        
        if (customerNotes != null) {
            customerNotes = customerNotes.trim();
            if (customerNotes.isEmpty()) {
                customerNotes = null;
            }
        }
        
        if (internalNotes != null) {
            internalNotes = internalNotes.trim();
            if (internalNotes.isEmpty()) {
                internalNotes = null;
            }
        }
        
        if (shippingAddress != null) {
            shippingAddress.normalize();
        }
        
        if (billingAddress != null) {
            billingAddress.normalize();
        }
    }
}