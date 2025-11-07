package com.example.demo.domain.orderManagementDomain.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;

@SuperBuilder
@Getter
@Setter
@ToString
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class OrderShipmentDto {

    @NotNull(message = "Shipment ID cannot be null")
    private Long shipmentId;

    @NotNull(message = "Order ID cannot be null")
    private Long orderId;

    @NotBlank(message = "Courier name cannot be blank")
    @Size(max = 100, message = "Courier name cannot exceed 100 characters")
    private String courierName;

    @NotBlank(message = "Tracking number cannot be blank")
    @Size(max = 50, message = "Tracking number cannot exceed 50 characters")
    private String trackingNumber;

    @NotBlank(message = "Origin cannot be blank")
    @Size(max = 255, message = "Origin cannot exceed 255 characters")
    private String origin;

    @NotBlank(message = "Destination cannot be blank")
    @Size(max = 255, message = "Destination cannot exceed 255 characters")
    private String destination;

    @NotBlank(message = "Shipment status cannot be blank")
    @Size(max = 50, message = "Shipment status cannot exceed 50 characters")
    private String shipmentStatus;

    @FutureOrPresent(message = "Estimated delivery date cannot be in the past")
    private LocalDate estimatedDeliveryDate;

    @NotNull(message = "Delivery duration cannot be null")
    @Min(value = 1, message = "Delivery duration must be at least 1 day")
    @Max(value = 365, message = "Delivery duration cannot exceed 365 days")
    private Integer deliveryDuration;

    @NotNull(message = "Shipment cost cannot be null")
    @DecimalMin(value = "0.00", message = "Shipment cost must be at least 0")
    @Digits(integer = 8, fraction = 2, message = "Shipment cost is invalid")
    private BigDecimal shipmentCost;
}

