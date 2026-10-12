// UpdateCustomerPreferencesRequest.java
package com.example.demo.modules.customer.application.dto.request;

import com.example.demo.common.base.dto.BaseRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating customer preferences
 *
 * @author Customer Domain Team
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Update customer preferences request")
public class UpdateCustomerPreferencesRequest extends BaseRequest {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "Language is required")
    @Pattern(regexp = "^[a-z]{2}$", message = "Language must be a valid ISO 639-1 code")
    @Schema(description = "Preferred language (ISO 639-1)", example = "en", required = true)
    private String language;

    @NotBlank(message = "Currency is required")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be a valid ISO 4217 code")
    @Schema(description = "Preferred currency (ISO 4217)", example = "USD", required = true)
    private String currency;

    @Schema(description = "Enable email notifications", example = "true")
    private boolean emailNotifications;

    @Schema(description = "Enable SMS notifications", example = "false")
    private boolean smsNotifications;

    @Schema(description = "Enable marketing emails", example = "false")
    private boolean marketingEmails;

    @Override
    public void normalize() {
        super.normalize();
        if (language != null) language = language.trim().toLowerCase();
        if (currency != null) currency = currency.trim().toUpperCase();
    }
}