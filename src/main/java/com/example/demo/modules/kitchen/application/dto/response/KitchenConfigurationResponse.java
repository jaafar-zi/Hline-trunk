package com.example.demo.modules.kitchen.application.dto.response;

import com.example.demo.common.base.dto.BaseDTO;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Kitchen Configuration Response DTO
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Kitchen configuration response")
public class KitchenConfigurationResponse extends BaseDTO {

    @Schema(description = "Configuration name", example = "My L-Shaped Kitchen")
    private String name;

    @Schema(description = "Status", example = "DRAFT")
    private String status;

    @Schema(description = "Customer ID")
    private UUID customerId;

    @Schema(description = "Collection ID")
    private UUID collectionId;

    @Schema(description = "Collection name")
    private String collectionName;

    @Schema(description = "Collection slug")
    private String collectionSlug;

    @Schema(description = "Range name")
    private String rangeName;

    @Schema(description = "Selected cabinet colour")
    private KitchenColourResponse selectedCabinetColour;

    @Schema(description = "Available cabinet colours")
    private List<KitchenColourResponse> availableCabinetColours;

    @Schema(description = "Configuration items")
    private List<KitchenConfigurationItemResponse> items;

    @Schema(description = "Total number of unique line items")
    private Integer totalItems;

    @Schema(description = "Total unit count (sum of quantities)")
    private Long totalUnits;

    @Schema(description = "Subtotal amount")
    private BigDecimal subtotal;

    @Schema(description = "Currency")
    private String currency;
}