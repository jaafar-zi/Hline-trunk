package com.example.demo.productCatalog.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
public class ProductPricingDTO {
    private Long id;
    private Long productId;
    private String currency;
    private Double oldPrice;
    private Double price;
    private boolean promoFlag;

    // Constructor to match the JPQL query
    public ProductPricingDTO(
            Long id,
            Long productId,
            String currency,
            Double oldPrice,
            Double price,
            boolean promoFlag
    ) {
        this.id = id;
        this.productId = productId;
        this.currency = currency;
        this.oldPrice = oldPrice;
        this.price = price;
        this.promoFlag = promoFlag;
    }
}

