package com.example.demo.domain.orderManagementDomain.entity;


import lombok.*;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemRequest {
    private Long productId;
    private Integer quantity;
}
