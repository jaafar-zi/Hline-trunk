package com.example.demo.modules.kitchen.application.dto.request;

import com.example.demo.common.base.dto.BaseRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

/**
 * Change Global Cabinet Colour Request
 * The key mechanic - changes colour for all non-overridden items
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Change global cabinet colour request")
public class ChangeGlobalColourRequest extends BaseRequest {

    @NotNull(message = "Colour ID is required")
    @Schema(description = "New cabinet colour ID", required = true)
    private UUID colourId;
}