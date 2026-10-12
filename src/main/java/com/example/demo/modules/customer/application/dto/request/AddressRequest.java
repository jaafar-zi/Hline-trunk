package com.example.demo.modules.customer.application.dto.request;

import com.example.demo.common.validator.constraints.ValidPhone;
import com.example.demo.modules.customer.domain.model.vo.AddressType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Request DTO for creating/updating a customer address
 *
 * @author Customer Domain Team
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Address request payload")
public class AddressRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "Street address is required")
    @Size(max = 255, message = "Street address must not exceed 255 characters")
    @Schema(description = "Street address line 1", example = "123 Main St", required = true)
    private String streetAddress;

    @Size(max = 255, message = "Street address 2 must not exceed 255 characters")
    @Schema(description = "Street address line 2", example = "Apt 4B")
    private String streetAddress2;

    @NotBlank(message = "City is required")
    @Size(max = 100, message = "City must not exceed 100 characters")
    @Schema(description = "City", example = "New York", required = true)
    private String city;

    @Size(max = 100, message = "State/Province must not exceed 100 characters")
    @Schema(description = "State or Province", example = "NY")
    private String stateProvince;

    @NotBlank(message = "Postal code is required")
    @Size(max = 20, message = "Postal code must not exceed 20 characters")
    @Schema(description = "Postal/ZIP code", example = "10001", required = true)
    private String postalCode;

    @NotBlank(message = "Country is required")
    @Pattern(regexp = "^[A-Z]{2}$", message = "Country must be a valid ISO 3166-1 alpha-2 code")
    @Schema(description = "Country ISO 3166-1 alpha-2 code", example = "US", required = true)
    private String country;

    @ValidPhone(
            allowNull = true,
            requireCountryCode = false,
            allowExtension = true,
            minDigits = 7,
            maxDigits = 15
    )
    @Schema(description = "Address contact phone number", example = "+1234567890")
    private String phone;

    @NotNull(message = "Address type is required")
    @Schema(description = "Address type", example = "BOTH", required = true)
    private AddressType type;

    public void normalize() {
        if (streetAddress  != null) streetAddress  = streetAddress.trim();
        if (streetAddress2 != null) streetAddress2 = streetAddress2.trim();
        if (city           != null) city           = city.trim();
        if (stateProvince  != null) stateProvince  = stateProvince.trim();
        if (postalCode     != null) postalCode     = postalCode.trim();
        if (country        != null) country        = country.trim().toUpperCase();
        if (phone          != null) phone          = phone.trim();
    }
}