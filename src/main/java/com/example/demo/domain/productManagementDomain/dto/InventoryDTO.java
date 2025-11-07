package com.example.demo.domain.productManagementDomain.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@SuperBuilder
@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class InventoryDTO {

    private Long id;
    private Long productId;
    private int quantityInStock;
    private int reservedQuantity;
    private int reorderThreshold;
    private int reorderQuantity;
    private LocalDateTime lastUpdated;
    private LocalDateTime lastRestocked;
}

