package com.example.demo.modules.order.application.dto.request;

import com.example.demo.common.base.dto.BaseRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * Address Request DTO
 * 
 * @author Order Team
 * @version 1.0
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Address details")
public class AddressRequest extends BaseRequest {

    @NotBlank(message = "Street address is required")
    @Size(max = 255, message = "Street address must not exceed 255 characters")
    @Schema(description = "Street address", required = true)
    private String streetAddress;

    @Size(max = 255, message = "Street address line 2 must not exceed 255 characters")
    @Schema(description = "Street address line 2")
    private String streetAddress2;

    @NotBlank(message = "City is required")
    @Size(max = 100, message = "City must not exceed 100 characters")
    @Schema(description = "City", required = true)
    private String city;

    @Size(max = 100, message = "State/Province must not exceed 100 characters")
    @Schema(description = "State or province")
    private String stateProvince;

    @NotBlank(message = "Postal code is required")
    @Size(max = 20, message = "Postal code must not exceed 20 characters")
    @Schema(description = "Postal code", required = true)
    private String postalCode;

    @NotBlank(message = "Country is required")
    @Size(min = 2, max = 2, message = "Country must be 2-letter ISO code")
    @Pattern(regexp = "[A-Z]{2}", message = "Country must be 2-letter uppercase ISO code")
    @Schema(description = "Country (ISO 3166-1 alpha-2)", required = true, example = "US")
    private String country;

    @Size(max = 20, message = "Phone must not exceed 20 characters")
    @Schema(description = "Phone number")
    private String phone;

    @Override
    public void normalize() {
        super.normalize();
        
        if (streetAddress != null) {
            streetAddress = streetAddress.trim();
        }
        
        if (streetAddress2 != null) {
            streetAddress2 = streetAddress2.trim();
            if (streetAddress2.isEmpty()) {
                streetAddress2 = null;
            }
        }
        
        if (city != null) {
            city = city.trim();
        }
        
        if (stateProvince != null) {
            stateProvince = stateProvince.trim();
            if (stateProvince.isEmpty()) {
                stateProvince = null;
            }
        }
        
        if (postalCode != null) {
            postalCode = postalCode.trim().toUpperCase();
        }
        
        if (country != null) {
            country = country.trim().toUpperCase();
        }
        
        if (phone != null) {
            phone = phone.trim();
            if (phone.isEmpty()) {
                phone = null;
            }
        }
    }
}