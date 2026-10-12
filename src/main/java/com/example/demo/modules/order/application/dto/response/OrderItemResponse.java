package com.example.demo.modules.order.application.dto.response;

import com.example.demo.common.base.dto.BaseDTO;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Order Item Response DTO
 * 
 * @author Order Team
 * @version 1.0
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Order item details")
public class OrderItemResponse extends BaseDTO {

    @Schema(description = "Product ID")
    private UUID productId;

    @Schema(description = "Product SKU")
    private String productSku;

    @Schema(description = "Product name")
    private String productName;

    @Schema(description = "Product variant ID")
    private UUID productVariantId;

    @Schema(description = "Product variant SKU")
    private String productVariantSku;

    @Schema(description = "Unit price")
    private BigDecimal unitPrice;

    @Schema(description = "Quantity")
    private Integer quantity;

    @Schema(description = "Subtotal (unit price × quantity)")
    private BigDecimal subtotal;

    @Schema(description = "Tax rate")
    private BigDecimal taxRate;

    @Schema(description = "Tax amount")
    private BigDecimal taxAmount;

    @Schema(description = "Discount amount")
    private BigDecimal discountAmount;

    @Schema(description = "Total amount")
    private BigDecimal total;

    @Schema(description = "Currency code", example = "USD")
    private String currency;

    @Schema(description = "Notes")
    private String notes;
}