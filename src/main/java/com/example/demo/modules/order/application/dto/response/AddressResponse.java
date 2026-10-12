package com.example.demo.modules.order.application.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.io.Serializable;

/**
 * Address Response DTO
 * 
 * Value object - does not extend BaseDTO
 * 
 * @author Order Team
 * @version 1.0
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Address details")
public class AddressResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "Street address")
    private String streetAddress;

    @Schema(description = "Street address line 2")
    private String streetAddress2;

    @Schema(description = "City")
    private String city;

    @Schema(description = "State or province")
    private String stateProvince;

    @Schema(description = "Postal code")
    private String postalCode;

    @Schema(description = "Country (ISO 3166-1 alpha-2)", example = "US")
    private String country;

    @Schema(description = "Phone number")
    private String phone;

    @Schema(description = "Full formatted address")
    private String fullAddress;
}