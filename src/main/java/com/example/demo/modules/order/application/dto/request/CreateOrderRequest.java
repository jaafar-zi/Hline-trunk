package com.example.demo.modules.order.application.dto.request;

import com.example.demo.common.base.dto.BaseRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;
import java.util.UUID;

/**
 * Create Order Request DTO
 * 
 * @author Order Team
 * @version 1.0
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request to create a new order")
public class CreateOrderRequest extends BaseRequest {

    @NotNull(message = "Customer ID is required")
    @Schema(description = "Customer ID", required = true)
    private UUID customerId;

    @NotBlank(message = "Customer email is required")
    @Email(message = "Invalid email format")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    @Schema(description = "Customer email", required = true, example = "customer@example.com")
    private String customerEmail;

    @NotBlank(message = "First name is required")
    @Size(max = 100, message = "First name must not exceed 100 characters")
    @Schema(description = "Customer first name", required = true)
    private String customerFirstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 100, message = "Last name must not exceed 100 characters")
    @Schema(description = "Customer last name", required = true)
    private String customerLastName;

    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    @Schema(description = "Customer phone number")
    private String customerPhone;

    @NotNull(message = "Shipping address is required")
    @Valid
    @Schema(description = "Shipping address", required = true)
    private AddressRequest shippingAddress;

    @NotNull(message = "Billing address is required")
    @Valid
    @Schema(description = "Billing address", required = true)
    private AddressRequest billingAddress;

    @NotEmpty(message = "Order must contain at least one item")
    @Valid
    @Schema(description = "Order items", required = true)
    private List<OrderItemRequest> items;

    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be 3 characters (ISO 4217)")
    @Schema(description = "Currency code (ISO 4217)", required = true, example = "USD")
    private String currency;

    @Size(max = 1000, message = "Customer notes must not exceed 1000 characters")
    @Schema(description = "Customer notes")
    private String customerNotes;

    @Size(max = 50, message = "Discount code must not exceed 50 characters")
    @Schema(description = "Discount code")
    private String discountCode;

    @Size(max = 45, message = "IP address must not exceed 45 characters")
    @Schema(description = "Customer IP address")
    private String ipAddress;

    @Size(max = 500, message = "User agent must not exceed 500 characters")
    @Schema(description = "Customer user agent")
    private String userAgent;

    @Override
    public void normalize() {
        super.normalize();
        
        if (customerEmail != null) {
            customerEmail = customerEmail.trim().toLowerCase();
        }
        
        if (customerFirstName != null) {
            customerFirstName = customerFirstName.trim();
        }
        
        if (customerLastName != null) {
            customerLastName = customerLastName.trim();
        }
        
        if (customerPhone != null) {
            customerPhone = customerPhone.trim();
        }
        
        if (currency != null) {
            currency = currency.trim().toUpperCase();
        }
        
        if (discountCode != null) {
            discountCode = discountCode.trim().toUpperCase();
        }
        
        // Normalize nested objects
        if (shippingAddress != null) {
            shippingAddress.normalize();
        }
        
        if (billingAddress != null) {
            billingAddress.normalize();
        }
        
        if (items != null) {
            items.forEach(BaseRequest::normalize);
        }
    }
}