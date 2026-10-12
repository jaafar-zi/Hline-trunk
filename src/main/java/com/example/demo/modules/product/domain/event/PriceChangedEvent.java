package com.example.demo.modules.product.domain.event;

import com.example.demo.common.event.DomainEvent;
import com.example.demo.common.event.EventMetadata;
import com.example.demo.modules.product.domain.model.vo.Money;
import com.example.demo.modules.product.domain.model.vo.PriceType;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Event published when product price changes
 * 
 * This is a business-critical event used for:
 * - Price history tracking
 * - Competitive price monitoring
 * - Customer price change notifications
 * - Revenue analytics
 * - Pricing strategy optimization
 * - Integration with pricing engines
 * 
 * @author PIM Team
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceChangedEvent implements DomainEvent {

    private static final long serialVersionUID = 1L;

    @Builder.Default
    private String eventId = UUID.randomUUID().toString();

    private UUID priceId;
    private UUID productId;
    private String sku;
    private String productName;

    /**
     * Price details
     */
    private PriceType priceType;
    private Money oldPrice;
    private Money newPrice;
    private String currency;

    /**
     * Change details
     */
    private BigDecimal changeAmount;      // Absolute change
    private BigDecimal changePercentage;  // Percentage change
    private String changeDirection;       // INCREASE, DECREASE, NO_CHANGE

    /**
     * Context
     */
    private String reason;                // COST_CHANGE, PROMOTION, COMPETITIVE, SEASONAL, etc.
    private String changeType;            // MANUAL, AUTOMATIC, SCHEDULED, BULK_UPDATE
    private UUID campaignId;              // If part of a pricing campaign
    private String campaignName;

    /**
     * Impact assessment
     */
    private Boolean affectsActiveOrders;  // Whether active orders are affected
    private Integer affectedOrderCount;   // Number of orders that might be impacted
    private Boolean requiresCustomerNotification;

    /**
     * Price validity
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime effectiveFrom;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime effectiveTo;

    /**
     * Metadata
     */
    private String changedBy;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    @Builder.Default
    private LocalDateTime occurredAt = LocalDateTime.now();

    private String correlationId;
    private String causationId;
    private String source;
    private EventMetadata metadata;

    /**
     * Market and segment
     */
    private String market;                // US, EU, ASIA
    private String customerGroup;         // RETAIL, WHOLESALE, VIP

    /**
     * Approval workflow
     */
    private Boolean requiresApproval;
    private String approvedBy;
    private LocalDateTime approvedAt;

    public PriceChangedEvent(
        UUID priceId,
        UUID productId,
        PriceType priceType,
        Money oldPrice,
        Money newPrice,
        String currency
    ) {
        this.eventId = UUID.randomUUID().toString();
        this.priceId = priceId;
        this.productId = productId;
        this.priceType = priceType;
        this.oldPrice = oldPrice;
        this.newPrice = newPrice;
        this.currency = currency;
        this.occurredAt = LocalDateTime.now();
        this.source = "product-price-service";
        
        calculateChange();
    }

    @Override
    public String getEventType() {
        return "product.price.changed";
    }

    @Override
    public String getTriggeredBy() {
        return changedBy != null ? changedBy : "system";
    }

    @Override
    public String getSource() {
        return source != null ? source : "product-price-service";
    }

    @Override
    public boolean isAsync() {
        return false; // Sync - critical for pricing consistency
    }

    /**
     * Calculate change amount and percentage
     */
    private void calculateChange() {
        if (oldPrice == null || newPrice == null) {
            this.changeDirection = "UNKNOWN";
            return;
        }

        // Calculate absolute change
        Money change = newPrice.subtract(oldPrice);
        this.changeAmount = change.getAmount();

        // Calculate percentage change
        if (oldPrice.getAmount().compareTo(BigDecimal.ZERO) != 0) {
            this.changePercentage = changeAmount
                    .divide(oldPrice.getAmount(), 4, java.math.RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        } else {
            this.changePercentage = BigDecimal.ZERO;
        }

        // Determine direction
        int comparison = changeAmount.compareTo(BigDecimal.ZERO);
        if (comparison > 0) {
            this.changeDirection = "INCREASE";
        } else if (comparison < 0) {
            this.changeDirection = "DECREASE";
        } else {
            this.changeDirection = "NO_CHANGE";
        }
    }

    /**
     * Check if price increased
     */
    public boolean isPriceIncrease() {
        return "INCREASE".equals(changeDirection);
    }

    /**
     * Check if price decreased
     */
    public boolean isPriceDecrease() {
        return "DECREASE".equals(changeDirection);
    }

    /**
     * Check if significant change (e.g., > 10%)
     */
    public boolean isSignificantChange(BigDecimal threshold) {
        if (changePercentage == null) {
            return false;
        }
        return changePercentage.abs().compareTo(threshold) > 0;
    }

    /**
     * Get absolute change amount
     */
    public BigDecimal getAbsoluteChange() {
        return changeAmount != null ? changeAmount.abs() : BigDecimal.ZERO;
    }

    /**
     * Get formatted change
     */
    public String getFormattedChange() {
        if (changeAmount == null) {
            return "N/A";
        }
        String sign = changeAmount.compareTo(BigDecimal.ZERO) > 0 ? "+" : "";
        return String.format("%s%s %s (%.2f%%)", 
            sign, 
            changeAmount, 
            currency,
            changePercentage != null ? changePercentage : BigDecimal.ZERO
        );
    }

    @Override
    public String toString() {
        return String.format(
            "PriceChangedEvent{priceId=%s, productId=%s, type=%s, oldPrice=%s, newPrice=%s, change=%s}",
            priceId, productId, priceType, oldPrice, newPrice, getFormattedChange()
        );
    }
}
