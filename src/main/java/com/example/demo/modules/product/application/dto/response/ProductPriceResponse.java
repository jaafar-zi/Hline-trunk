package com.example.demo.modules.product.application.dto.response;

import com.example.demo.common.base.dto.BaseDTO;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Lean Product Price Response - Performance optimized
 *
 * @author PIM Team
 * @version 1.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Product price response - optimized")
public class ProductPriceResponse extends BaseDTO {

    private static final long serialVersionUID = 1L;

    // ====================================
    // Reference
    // ====================================

    @Schema(description = "Product ID reference")
    private UUID productId;

    @Schema(description = "Variant ID reference (if variant-specific)")
    private UUID variantId;

    // ====================================
    // Core Price Data
    // ====================================

    @Schema(description = "Price amount", example = "99.99", required = true)
    private BigDecimal amount;

    @Schema(description = "Currency", example = "USD", required = true)
    private String currency;

    @Schema(description = "Price type", example = "BASE")
    private String priceType;

    @Schema(description = "Is active", example = "true")
    private Boolean active;

    @Schema(description = "Priority", example = "10")
    private Integer priority;

    // ====================================
    // Validity
    // ====================================

    @Schema(description = "Valid from")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime validFrom;

    @Schema(description = "Valid to")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime validTo;

    // ====================================
    // Discount Info (if applicable)
    // ====================================

    @Schema(description = "Discount percentage", example = "10.0")
    private BigDecimal discountPercentage;

    @Schema(description = "Original price", example = "109.99")
    private BigDecimal originalPrice;

    // ====================================
    // Segmentation
    // ====================================

    @Schema(description = "Customer group", example = "RETAIL")
    private String customerGroup;

    @Schema(description = "Market", example = "US")
    private String market;

    // ====================================
    // Tax (basic)
    // ====================================

    @Schema(description = "Tax inclusive", example = "false")
    private Boolean taxInclusive;

    @Schema(description = "Tax rate", example = "10.0")
    private BigDecimal taxRate;

    // ====================================
    // Helper Methods
    // ====================================

    public boolean isCurrentlyValid() {
        if (!Boolean.TRUE.equals(active)) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now();
        boolean afterStart = validFrom == null || !now.isBefore(validFrom);
        boolean beforeEnd = validTo == null || !now.isAfter(validTo);
        return afterStart && beforeEnd && isActive();
    }

    public boolean hasDiscount() {
        return discountPercentage != null && discountPercentage.compareTo(BigDecimal.ZERO) > 0;
    }

    @Override
    public String toString() {
        return String.format("ProductPriceResponse[id=%s, amount=%s %s, type=%s]", 
            id, amount, currency, priceType);
    }
}