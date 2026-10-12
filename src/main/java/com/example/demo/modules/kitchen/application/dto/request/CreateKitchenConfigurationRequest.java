package com.example.demo.modules.kitchen.application.dto.request;

import com.example.demo.common.base.dto.BaseRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.UUID;

/**
 * Create Kitchen Configuration Request
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Create kitchen configuration request")
public class CreateKitchenConfigurationRequest extends BaseRequest {

    @NotNull(message = "Collection ID is required")
    @Schema(description = "Collection ID", required = true)
    private UUID collectionId;

    @Size(max = 200)
    @Schema(description = "Configuration name", example = "My L-Shaped Kitchen")
    private String name;

    @Schema(description = "Customer ID (for logged-in users)")
    private UUID customerId;

    @Schema(description = "Session ID (for guest users)")
    private String sessionId;

    @Override
    public void normalize() {
        super.normalize();
        if (name != null) name = name.trim();
        if (sessionId != null) sessionId = sessionId.trim();
        if (name == null || name.isBlank()) name = "My Kitchen";
    }
}