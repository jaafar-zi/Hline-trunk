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
 * Event published when product price is deactivated
 * 
 * This event is used for:
 * - Promotional campaign end
 * - Price list deactivation
 * - Product listing updates
 * - Revert to base/regular price
 * - Marketing automation (end of sale)
 * - Analytics (campaign performance)
 * 
 * @author PIM Team
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceDeactivatedEvent implements DomainEvent {

    private static final long serialVersionUID = 1L;

    @Builder.Default
    private String eventId = UUID.randomUUID().toString();

    private UUID priceId;
    private UUID productId;
    private String sku;
    private String productName;
    private UUID categoryId;
    private String categoryName;

    /**
     * Deactivated price details
     */
    private PriceType priceType;
    private Money price;
    private String currency;
    private Integer priority;

    /**
     * Validity period
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime validFrom;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime validTo;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime activatedAt;       // When this price was activated

    private Long activeDurationHours;        // How long was it active?

    /**
     * Deactivation details
     */
    private String reason;                   // EXPIRED, MANUAL, CAMPAIGN_END, REPLACED, etc.
    private String deactivationType;         // SCHEDULED, MANUAL, AUTOMATIC, FORCED
    private Boolean wasScheduled;            // Was this a scheduled deactivation?
    private Boolean wasExpired;              // Did it expire naturally?

    /**
     * Replacement price
     */
    private UUID replacementPriceId;
    private PriceType replacementPriceType;
    private Money replacementPrice;
    private Boolean hasReplacement;          // Is there a new active price?

    /**
     * Price change impact
     */
    private Money priceDifference;           // Difference between old and new price
    private BigDecimal priceChangePercentage;
    private String priceChangeDirection;     // INCREASE, DECREASE, NO_CHANGE

    /**
     * Discount information (for promotional prices)
     */
    private BigDecimal discountPercentage;
    private Money discountAmount;
    private Money originalPrice;

    /**
     * Campaign information
     */
    private UUID campaignId;
    private String campaignName;
    private String campaignType;

    /**
     * Performance metrics (for promotional prices)
     */
    private Integer totalOrders;             // Orders placed during active period
    private BigDecimal totalRevenue;         // Revenue generated
    private Integer uniqueCustomers;         // Unique customers who purchased
    private Double conversionRate;           // Conversion rate during campaign

    /**
     * Segmentation
     */
    private String customerGroup;
    private String market;
    private UUID priceListId;

    /**
     * Business actions
     */
    private Boolean revertToBasePrice;       // Should product revert to base price?
    private Boolean updateProductListings;   // Update on website/catalog?
    private Boolean notifyCustomers;         // Notify customers of price change?
    private Boolean archivePrice;            // Archive for historical reference?

    /**
     * Metadata
     */
    private String deactivatedBy;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    @Builder.Default
    private LocalDateTime occurredAt = LocalDateTime.now();

    private String correlationId;
    private String causationId;
    private String source;
    private EventMetadata metadata;

    /**
     * Audit trail
     */
    private String notes;
    private Boolean requiresApproval;
    private String approvedBy;

    public PriceDeactivatedEvent(
        UUID priceId,
        UUID productId,
        PriceType priceType,
        Money price,
        String reason
    ) {
        this.eventId = UUID.randomUUID().toString();
        this.priceId = priceId;
        this.productId = productId;
        this.priceType = priceType;
        this.price = price;
        this.currency = price != null ? price.getCurrency() : null;
        this.reason = reason;
        this.occurredAt = LocalDateTime.now();
        this.source = "product-price-service";
        
        determineDeactivationType();
    }

    @Override
    public String getEventType() {
        return "product.price.deactivated";
    }

    @Override
    public String getTriggeredBy() {
        return deactivatedBy != null ? deactivatedBy : "system";
    }

    @Override
    public String getSource() {
        return source != null ? source : "product-price-service";
    }

    @Override
    public boolean isAsync() {
        return true; // Async for downstream systems
    }

    /**
     * Determine deactivation type based on reason
     */
    private void determineDeactivationType() {
        if ("EXPIRED".equals(reason)) {
            this.deactivationType = "AUTOMATIC";
            this.wasExpired = true;
            this.wasScheduled = true;
        } else if ("CAMPAIGN_END".equals(reason)) {
            this.deactivationType = "SCHEDULED";
            this.wasScheduled = true;
        } else if ("MANUAL".equals(reason) || "REPLACED".equals(reason)) {
            this.deactivationType = "MANUAL";
            this.wasScheduled = false;
        }
    }

    /**
     * Calculate active duration if activation time is known
     */
    public void calculateActiveDuration() {
        if (activatedAt != null) {
            this.activeDurationHours = java.time.temporal.ChronoUnit.HOURS.between(
                activatedAt,
                occurredAt
            );
        }
    }

    /**
     * Calculate price change if replacement exists
     */
    public void calculatePriceChange() {
        if (price == null || replacementPrice == null) {
            this.priceChangeDirection = "NO_CHANGE";
            return;
        }

        this.priceDifference = replacementPrice.subtract(price);
        
        if (price.getAmount().compareTo(BigDecimal.ZERO) != 0) {
            this.priceChangePercentage = priceDifference.getAmount()
                    .divide(price.getAmount(), 4, java.math.RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }

        int comparison = priceDifference.getAmount().compareTo(BigDecimal.ZERO);
        if (comparison > 0) {
            this.priceChangeDirection = "INCREASE";
        } else if (comparison < 0) {
            this.priceChangeDirection = "DECREASE";
        } else {
            this.priceChangeDirection = "NO_CHANGE";
        }
    }

    /**
     * Check if this was a promotional price
     */
    public boolean wasPromotionalPrice() {
        return priceType == PriceType.PROMOTIONAL;
    }

    /**
     * Check if price is increasing after deactivation
     */
    public boolean isPriceIncreasing() {
        return "INCREASE".equals(priceChangeDirection);
    }

    /**
     * Check if price is decreasing after deactivation
     */
    public boolean isPriceDecreasing() {
        return "DECREASE".equals(priceChangeDirection);
    }

    /**
     * Check if deactivation was successful (had replacement)
     */
    public boolean hasSuccessfulTransition() {
        return Boolean.TRUE.equals(hasReplacement) && replacementPrice != null;
    }

    /**
     * Get formatted price change message
     */
    public String getPriceChangeMessage() {
        if (priceDifference == null || priceChangePercentage == null) {
            return "No price change information available";
        }

        String direction = isPriceIncreasing() ? "increased" : "decreased";
        return String.format(
            "Price %s by %s (%.2f%%)",
            direction,
            priceDifference.abs().format(),
            priceChangePercentage.abs()
        );
    }

    /**
     * Get formatted duration
     */
    public String getFormattedDuration() {
        if (activeDurationHours == null) {
            return "Unknown";
        }

        long days = activeDurationHours / 24;
        long hours = activeDurationHours % 24;

        if (days > 0) {
            return String.format("%d days, %d hours", days, hours);
        } else {
            return String.format("%d hours", hours);
        }
    }

    /**
     * Check if had good performance (for promotional prices)
     */
    public boolean hadGoodPerformance(int minOrders, BigDecimal minRevenue) {
        if (!wasPromotionalPrice()) {
            return false;
        }

        return (totalOrders != null && totalOrders >= minOrders) &&
               (totalRevenue != null && totalRevenue.compareTo(minRevenue) >= 0);
    }

    @Override
    public String toString() {
        return String.format(
            "PriceDeactivatedEvent{priceId=%s, productId=%s, type=%s, price=%s, reason=%s, replacement=%s}",
            priceId, productId, priceType, price, reason, 
            hasReplacement != null && hasReplacement ? replacementPrice : "None"
        );
    }
}