// CustomerPreferencesResponse.java
package com.example.demo.modules.customer.application.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Customer preferences response DTO
 *
 * @author Customer Domain Team
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Customer preferences response")
public class CustomerPreferencesResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "Preferred language (ISO 639-1)", example = "en")
    private String language;

    @Schema(description = "Preferred currency (ISO 4217)", example = "USD")
    private String currency;

    @Schema(description = "Email notifications enabled", example = "true")
    private boolean emailNotifications;

    @Schema(description = "SMS notifications enabled", example = "false")
    private boolean smsNotifications;

    @Schema(description = "Marketing emails enabled", example = "false")
    private boolean marketingEmails;
}