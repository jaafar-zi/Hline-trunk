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
 * Event published when product price is activated
 * 
 * This event is used for:
 * - Product listing updates (show new price on website)
 * - Promotional campaign activation
 * - Price list activation for customer groups
 * - Inventory systems synchronization
 * - Marketing automation triggers
 * - Price comparison updates
 * 
 * @author PIM Team
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceActivatedEvent implements DomainEvent {

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
     * Price details
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

    private Boolean isScheduled;          // Was this a scheduled activation?
    private Boolean isIndefinite;         // No end date

    /**
     * Discount information (for promotional prices)
     */
    private BigDecimal discountPercentage;
    private Money discountAmount;
    private Money originalPrice;
    private Money savingsAmount;

    /**
     * Segmentation
     */
    private String customerGroup;         // RETAIL, WHOLESALE, VIP
    private String market;                // US, EU, ASIA
    private UUID priceListId;
    private String priceListName;

    /**
     * Campaign information
     */
    private UUID campaignId;
    private String campaignName;
    private String campaignType;          // FLASH_SALE, SEASONAL, CLEARANCE, etc.

    /**
     * Previous state
     */
    private PriceType previousActivePriceType;
    private Money previousActivePrice;
    private Boolean replacedAnotherPrice; // Did this replace another active price?
    private UUID replacedPriceId;

    /**
     * Business impact
     */
    private Boolean isPromotionalPrice;
    private Boolean requiresCustomerNotification;
    private Boolean updateProductListings;
    private Boolean triggerMarketingCampaign;

    /**
     * Tax information
     */
    private Boolean taxInclusive;
    private BigDecimal taxRate;

    /**
     * Metadata
     */
    private String activatedBy;
    private String activationReason;      // SCHEDULED, MANUAL, CAMPAIGN_START, etc.
    private String activationMethod;      // AUTO, MANUAL, API, BULK_IMPORT

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    @Builder.Default
    private LocalDateTime occurredAt = LocalDateTime.now();

    private String correlationId;
    private String causationId;
    private String source;
    private EventMetadata metadata;

    /**
     * Notifications
     */
    private Integer subscriberCount;      // Customers waiting for price drop
    private Boolean notifySubscribers;

    public PriceActivatedEvent(
        UUID priceId,
        UUID productId,
        PriceType priceType,
        Money price
    ) {
        this.eventId = UUID.randomUUID().toString();
        this.priceId = priceId;
        this.productId = productId;
        this.priceType = priceType;
        this.price = price;
        this.currency = price != null ? price.getCurrency() : null;
        this.occurredAt = LocalDateTime.now();
        this.source = "product-price-service";
        this.isPromotionalPrice = (priceType == PriceType.PROMOTIONAL);
    }

    @Override
    public String getEventType() {
        return "product.price.activated";
    }

    @Override
    public String getTriggeredBy() {
        return activatedBy != null ? activatedBy : "system";
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
     * Check if this is a promotional activation
     */
    public boolean isPromotion() {
        return Boolean.TRUE.equals(isPromotionalPrice) || priceType == PriceType.PROMOTIONAL;
    }

    /**
     * Check if activation is immediate
     */
    public boolean isImmediateActivation() {
        if (validFrom == null) {
            return true;
        }
        return !validFrom.isAfter(LocalDateTime.now());
    }

    /**
     * Check if this is a time-limited offer
     */
    public boolean isTimeLimited() {
        return validTo != null;
    }

    /**
     * Get duration in days (if time-limited)
     */
    public Long getDurationInDays() {
        if (validFrom == null || validTo == null) {
            return null;
        }
        return java.time.temporal.ChronoUnit.DAYS.between(validFrom, validTo);
    }

    /**
     * Get remaining time in hours
     */
    public Long getRemainingHours() {
        if (validTo == null) {
            return null;
        }
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(validTo)) {
            return 0L;
        }
        return java.time.temporal.ChronoUnit.HOURS.between(now, validTo);
    }

    /**
     * Check if significant discount (e.g., > 20%)
     */
    public boolean isSignificantDiscount(BigDecimal threshold) {
        if (discountPercentage == null) {
            return false;
        }
        return discountPercentage.compareTo(threshold) >= 0;
    }

    /**
     * Get formatted savings message
     */
    public String getSavingsMessage() {
        if (savingsAmount == null || discountPercentage == null) {
            return null;
        }
        return String.format("Save %s (%.0f%% off)", 
            savingsAmount.format(), 
            discountPercentage
        );
    }

    @Override
    public String toString() {
        return String.format(
            "PriceActivatedEvent{priceId=%s, productId=%s, type=%s, price=%s, discount=%s%%}",
            priceId, productId, priceType, price, 
            discountPercentage != null ? discountPercentage : "N/A"
        );
    }
}