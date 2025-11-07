package com.example.demo.domain.orderManagementDomain.entity;

import lombok.*;

import java.util.List;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaceOrderRequest {
    private List<ItemRequest> items;
    private Long shippingAddressId;
    private PaymentMethod paymentMethod;
    private String customerNote;
}

