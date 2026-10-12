package com.example.demo.modules.customer.application.dto.response;

import com.example.demo.modules.customer.domain.model.vo.CustomerStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Lightweight Customer response for lists/pages
 *
 * @author Customer Domain Team
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Customer summary (lightweight)")
public class CustomerSummaryResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "Customer ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @Schema(description = "Full name", example = "John Doe")
    private String fullName;

    @Schema(description = "Email address", example = "john.doe@example.com")
    private String email;

    @Schema(description = "Phone number", example = "+1234567890")
    private String phone;

    @Schema(description = "Account status", example = "ACTIVE")
    private CustomerStatus status;

    @Schema(description = "Number of addresses", example = "2")
    private int addressCount;

    @Schema(description = "Registration date", example = "2024-01-15T10:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
}