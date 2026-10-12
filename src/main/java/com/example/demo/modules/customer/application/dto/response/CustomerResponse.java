package com.example.demo.modules.customer.application.dto.response;

import com.example.demo.common.base.dto.BaseDTO;
import com.example.demo.modules.customer.domain.model.vo.CustomerStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * Full Customer response DTO
 *
 * @author Customer Domain Team
 * @version 1.0
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Full customer response")
public class CustomerResponse extends BaseDTO {

    private static final long serialVersionUID = 1L;

    @Schema(description = "First name", example = "John")
    private String firstName;

    @Schema(description = "Last name", example = "Doe")
    private String lastName;

    @Schema(description = "Full name", example = "John Doe")
    private String fullName;

    @Schema(description = "Email address", example = "john.doe@example.com")
    private String email;

    @Schema(description = "Phone number", example = "+1234567890")
    private String phone;

    @Schema(description = "Account status", example = "ACTIVE")
    private CustomerStatus status;

    @Schema(description = "Customer addresses")
    private List<CustomerAddressResponse> addresses;

    @Schema(description = "Preferred address ID", example = "1")
    private Long preferredAddressId;

    @Schema(description = "Customer preferences")
    private CustomerPreferencesResponse preferences;

    @Schema(description = "Whether customer can place orders", example = "true")
    private boolean canPlaceOrder;

    @Schema(description = "Whether customer can login", example = "true")
    private boolean canLogin;
}