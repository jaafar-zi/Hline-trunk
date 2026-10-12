package com.example.demo.modules.order.application.dto.response;

import com.example.demo.common.base.dto.BaseDTO;
import com.example.demo.modules.order.domain.model.vo.OrderStatus;
import com.example.demo.modules.order.domain.model.vo.PaymentStatus;
import com.example.demo.modules.order.domain.model.vo.ShippingStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Order Response DTO
 * 
 * Full order details response
 * 
 * @author Order Team
 * @version 1.0
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Order details")
public class OrderResponse extends BaseDTO {

    @Schema(description = "Order number", example = "ORD-20240115-000001")
    private String orderNumber;

    @Schema(description = "Customer ID")
    private UUID customerId;

    @Schema(description = "Customer email")
    private String customerEmail;

    @Schema(description = "Customer first name")
    private String customerFirstName;

    @Schema(description = "Customer last name")
    private String customerLastName;

    @Schema(description = "Customer full name")
    private String customerFullName;

    @Schema(description = "Customer phone")
    private String customerPhone;

    @Schema(description = "Order status")
    private OrderStatus status;

    @Schema(description = "Payment status")
    private PaymentStatus paymentStatus;

    @Schema(description = "Shipping status")
    private ShippingStatus shippingStatus;

    @Schema(description = "Shipping address")
    private AddressResponse shippingAddress;

    @Schema(description = "Billing address")
    private AddressResponse billingAddress;

    @Schema(description = "Order items")
    private List<OrderItemResponse> items;

    @Schema(description = "Number of items")
    private Integer itemCount;

    @Schema(description = "Total quantity of all items")
    private Integer totalQuantity;

    @Schema(description = "Subtotal amount")
    private BigDecimal subtotalAmount;

    @Schema(description = "Tax amount")
    private BigDecimal taxAmount;

    @Schema(description = "Shipping cost")
    private BigDecimal shippingCost;

    @Schema(description = "Discount amount")
    private BigDecimal discountAmount;

    @Schema(description = "Total amount")
    private BigDecimal totalAmount;

    @Schema(description = "Currency code", example = "USD")
    private String currency;

    @Schema(description = "Payment method")
    private String paymentMethod;

    @Schema(description = "Payment transaction ID")
    private String paymentTransactionId;

    @Schema(description = "Shipping method")
    private String shippingMethod;

    @Schema(description = "Tracking number")
    private String trackingNumber;

    @Schema(description = "Carrier")
    private String carrier;

    @Schema(description = "Customer notes")
    private String customerNotes;

    @Schema(description = "Internal notes")
    private String internalNotes;

    @Schema(description = "Discount code")
    private String discountCode;

    @Schema(description = "IP address")
    private String ipAddress;

    @Schema(description = "User agent")
    private String userAgent;

    @Schema(description = "Confirmed at")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime confirmedAt;

    @Schema(description = "Payment authorized at")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime paymentAuthorizedAt;

    @Schema(description = "Payment captured at")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime paymentCapturedAt;

    @Schema(description = "Shipped at")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime shippedAt;

    @Schema(description = "Estimated delivery date")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime estimatedDeliveryDate;

    @Schema(description = "Delivered at")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime deliveredAt;

    @Schema(description = "Cancelled at")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime cancelledAt;

    @Schema(description = "Cancellation reason")
    private String cancellationReason;

    @Schema(description = "Refunded at")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime refundedAt;

    @Schema(description = "Refund reason")
    private String refundReason;
}