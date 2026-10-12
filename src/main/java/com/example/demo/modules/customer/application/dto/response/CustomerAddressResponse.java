// CustomerAddressResponse.java
package com.example.demo.modules.customer.application.dto.response;

import com.example.demo.modules.customer.domain.model.vo.AddressType;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Customer address response DTO
 *
 * @author Customer Domain Team
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Customer address response")
public class CustomerAddressResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "Address ID", example = "1")
    private Long id;

    @Schema(description = "Street address line 1", example = "123 Main St")
    private String streetAddress;

    @Schema(description = "Street address line 2", example = "Apt 4B")
    private String streetAddress2;

    @Schema(description = "City", example = "New York")
    private String city;

    @Schema(description = "State or Province", example = "NY")
    private String stateProvince;

    @Schema(description = "Postal/ZIP code", example = "10001")
    private String postalCode;

    @Schema(description = "Country ISO code", example = "US")
    private String country;

    @Schema(description = "Address phone number", example = "+1234567890")
    private String phone;

    @Schema(description = "Address type", example = "BOTH")
    private AddressType type;

    @Schema(description = "Whether this is the preferred address", example = "true")
    private boolean preferred;

    @Schema(description = "Full formatted address",
            example = "123 Main St, Apt 4B, New York, NY 10001, US")
    private String fullAddress;
}