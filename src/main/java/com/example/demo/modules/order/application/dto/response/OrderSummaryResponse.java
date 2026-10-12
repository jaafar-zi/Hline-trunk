package com.example.demo.modules.order.application.dto.response;

import com.example.demo.common.base.dto.BaseDTO;
import com.example.demo.modules.order.domain.model.vo.OrderStatus;
import com.example.demo.modules.order.domain.model.vo.PaymentStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Order Summary Response DTO
 * 
 * Lightweight version for list views
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
@Schema(description = "Order summary (lightweight)")
public class OrderSummaryResponse extends BaseDTO {

    @Schema(description = "Order number", example = "ORD-20240115-000001")
    private String orderNumber;

    @Schema(description = "Customer ID")
    private UUID customerId;

    @Schema(description = "Customer full name")
    private String customerFullName;

    @Schema(description = "Customer email")
    private String customerEmail;

    @Schema(description = "Order status")
    private OrderStatus status;

    @Schema(description = "Payment status")
    private PaymentStatus paymentStatus;

    @Schema(description = "Number of items")
    private Integer itemCount;

    @Schema(description = "Total amount")
    private BigDecimal totalAmount;

    @Schema(description = "Currency code", example = "USD")
    private String currency;

    @Schema(description = "Confirmed at")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime confirmedAt;

    @Schema(description = "Delivered at")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime deliveredAt;
}