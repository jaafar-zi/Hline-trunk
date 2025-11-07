package com.example.demo.domain.productManagementDomain.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
public class ProductPricingDTO {

    private Long id;
    private List<Long> productIds;
    private String currency;
    private Double oldPrice;
    private Double price;
    private boolean promoFlag;

    // Constructor to match custom JPQL or manual mapping
    public ProductPricingDTO(
            Long id,
            List<Long> productIds,
            String currency,
            Double oldPrice,
            Double price,
            boolean promoFlag
    ) {
        this.id = id;
        this.productIds = productIds;
        this.currency = currency;
        this.oldPrice = oldPrice;
        this.price = price;
        this.promoFlag = promoFlag;
    }
}
