package com.example.demo.modules.customer.application.dto.request;

import com.example.demo.common.base.dto.BaseRequest;
import com.example.demo.common.validator.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for registering a new customer
 *
 * @author Customer Domain Team
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Register customer request")
public class RegisterCustomerRequest extends BaseRequest {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "First name is required")
    @Size(min = 1, max = 50, message = "First name must be between 1 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z\\s'-]+$", message = "First name contains invalid characters")
    @Schema(description = "Customer's first name", example = "John", required = true)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 1, max = 50, message = "Last name must be between 1 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z\\s'-]+$", message = "Last name contains invalid characters")
    @Schema(description = "Customer's last name", example = "Doe", required = true)
    private String lastName;

    @NotBlank(message = "Email is required")
    @ValidEmail(
            message = "Invalid email format",
            maxLength = 255,
            allowPlusAddressing = true
    )
    @UniqueField(
            entity = com.example.demo.modules.customer.domain.model.Customer.class,
            field = "email.value",
            message = "Email is already registered"
    )
    @Schema(description = "Customer's email address", example = "john.doe@example.com", required = true)
    private String email;

    @NotBlank(message = "Phone is required")
    @ValidPhone(
            allowNull = false,
            requireCountryCode = true,
            allowExtension = false,
            minDigits = 7,
            maxDigits = 15
    )
    @Schema(description = "Customer's phone number (E.164 format)", example = "+1234567890", required = true)
    private String phone;

    @Override
    public void normalize() {
        super.normalize();
        if (firstName != null) firstName = firstName.trim();
        if (lastName  != null) lastName  = lastName.trim();
        if (email     != null) email     = email.trim().toLowerCase();
        if (phone     != null) phone     = phone.trim();
    }
}