package com.example.demo.modules.product.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.example.demo.common.base.entity.SoftDeletableAggregateRoot;
import com.example.demo.common.event.DomainEvent;
import com.example.demo.common.event.EntityUpdatedEvent;
import com.example.demo.common.util.ChangeTracker;
import com.example.demo.modules.product.domain.event.PriceActivatedEvent;
import com.example.demo.modules.product.domain.event.PriceChangedEvent;
import com.example.demo.modules.product.domain.event.PriceDeactivatedEvent;
import com.example.demo.modules.product.domain.model.vo.Money;
import com.example.demo.modules.product.domain.model.vo.PriceType;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Product Price Entity
 *
 * Represents pricing information for products
 * Part of Product aggregate
 *
 * Business Rules:
 * - Price amount must be positive
 * - Base price is required for product to be sellable
 * - Promotional prices must have validity period
 * - Promotional price must be less than base price
 * - Multiple currencies supported
 * - Price priority determines which price to use when multiple are active
 *
 * Events Published:
 * - EntityUpdatedEvent<ProductPrice> (with change tracking)
 * - PriceChangedEvent (when price amount changes)
 * - PriceActivatedEvent
 * - PriceDeactivatedEvent
 *
 * @author PIM Team
 * @version 1.0
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "product_prices",
        indexes = {
                @Index(name = "idx_product_price_product", columnList = "product_id"),
                @Index(name = "idx_product_price_type", columnList = "product_id, price_type"),
                @Index(name = "idx_product_price_currency", columnList = "currency"),
                @Index(name = "idx_product_price_active", columnList = "active"),
                @Index(name = "idx_product_price_priority", columnList = "priority DESC"),
                @Index(name = "idx_product_price_dates", columnList = "valid_from, valid_to")
        }
)
public class ProductPrice extends SoftDeletableAggregateRoot {

    // ====================================
    // Core Information
    // ====================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "amount", nullable = false, precision = 19, scale = 4)),
            @AttributeOverride(name = "currency", column = @Column(name = "currency", nullable = false, length = 3))
    })
    private Money price;

    @Enumerated(EnumType.STRING)
    @Column(name = "price_type", nullable = false, length = 50)
    @Builder.Default
    private PriceType priceType = PriceType.BASE;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Column(name = "priority")
    @Builder.Default
    private Integer priority = 0;

    // ====================================
    // Validity Period
    // ====================================

    @Column(name = "valid_from")
    private LocalDateTime validFrom;

    @Column(name = "valid_to")
    private LocalDateTime validTo;

    // ====================================
    // Discount Information (for promotional prices)
    // ====================================

    @Column(name = "discount_percentage", precision = 5, scale = 2)
    private BigDecimal discountPercentage;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "discount_amount", precision = 19, scale = 4)),
            @AttributeOverride(name = "currency", column = @Column(name = "discount_currency", length = 3))
    })
    private Money discountAmount;

    @Column(name = "original_price", precision = 19, scale = 4)
    private BigDecimal originalPrice;

    // ====================================
    // Tax Information
    // ====================================

    @Column(name = "tax_inclusive", nullable = false)
    @Builder.Default
    private Boolean taxInclusive = false;

    @Column(name = "tax_rate", precision = 5, scale = 2)
    private BigDecimal taxRate;

    @Column(name = "tax_class", length = 50)
    private String taxClass;

    // ====================================
    // Pricing Metadata
    // ====================================

    @Column(name = "price_list_id")
    private java.util.UUID priceListId; // Reference to external price list

    @Column(name = "customer_group", length = 100)
    private String customerGroup; // e.g., "WHOLESALE", "RETAIL", "VIP"

    @Column(name = "market", length = 50)
    private String market; // e.g., "US", "EU", "ASIA"

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    // ====================================
    // Factory Methods
    // ====================================

    /**
     * Create base price
     *
     * Events Published:
     * - None (event published by Product aggregate when price is added)
     */
    public static ProductPrice createBase(Money price) {
        validatePrice(price);

        return ProductPrice.builder()
                .price(price)
                .priceType(PriceType.BASE)
                .active(true)
                .priority(0)
                .taxInclusive(false)
                .build();
    }

    /**
     * Create promotional price
     */
    public static ProductPrice createPromotional(
            Money price,
            LocalDateTime validFrom,
            LocalDateTime validTo
    ) {
        validatePrice(price);
        validatePeriod(validFrom, validTo);

        return ProductPrice.builder()
                .price(price)
                .priceType(PriceType.PROMOTIONAL)
                .validFrom(validFrom)
                .validTo(validTo)
                .active(true)
                .priority(10) // Higher priority than base
                .taxInclusive(false)
                .build();
    }

    /**
     * Create promotional price with discount
     */
    public static ProductPrice createPromotionalWithDiscount(
            Money originalPrice,
            BigDecimal discountPercentage,
            LocalDateTime validFrom,
            LocalDateTime validTo
    ) {
        validatePrice(originalPrice);
        validateDiscount(discountPercentage);
        validatePeriod(validFrom, validTo);

        Money discountedPrice = originalPrice.multiplyBy(
                BigDecimal.ONE.subtract(discountPercentage.divide(BigDecimal.valueOf(100)))
        );

        return ProductPrice.builder()
                .price(discountedPrice)
                .originalPrice(originalPrice.getAmount())
                .discountPercentage(discountPercentage)
                .discountAmount(originalPrice.subtract(discountedPrice))
                .priceType(PriceType.PROMOTIONAL)
                .validFrom(validFrom)
                .validTo(validTo)
                .active(true)
                .priority(10)
                .taxInclusive(false)
                .build();
    }

    /**
     * Create tiered price
     */
    public static ProductPrice createTiered(Money price, String customerGroup) {
        validatePrice(price);

        return ProductPrice.builder()
                .price(price)
                .priceType(PriceType.TIERED)
                .customerGroup(customerGroup)
                .active(true)
                .priority(5)
                .taxInclusive(false)
                .build();
    }

    // ====================================
    // Domain Logic - Price Updates
    // ====================================

    /**
     * Update price amount
     *
     * Events Published:
     * - PriceChangedEvent (domain-specific)
     */
    public void updatePrice(Money newPrice) {
        validatePrice(newPrice);

        if (!this.price.getCurrency().equals(newPrice.getCurrency())) {
            throw new IllegalArgumentException(
                    String.format("Cannot change currency from %s to %s",
                            this.price.getCurrency(), newPrice.getCurrency()));
        }

        if (!this.price.equals(newPrice)) {
            Money oldPrice = this.price;
            this.price = newPrice;

            // If this is a promotional price with percentage discount, recalculate
            if (this.priceType == PriceType.PROMOTIONAL && this.originalPrice != null) {
                recalculateDiscount();
            }

            registerEvent(PriceChangedEvent.builder()
                    .priceId(this.getId())
                    .productId(this.product != null ? this.product.getId() : null)
                    .priceType(this.priceType)
                    .oldPrice(oldPrice)
                    .newPrice(newPrice)
                    .currency(newPrice.getCurrency())
                    .occurredAt(LocalDateTime.now())
                    .build());
        }
    }

    /**
     * Update price type
     */
    public void updatePriceType(PriceType newType) {
        if (newType == null) {
            throw new IllegalArgumentException("Price type cannot be null");
        }

        if (this.priceType != newType) {
            ChangeTracker tracker = new ChangeTracker();
            tracker.track("priceType", this.priceType, newType);

            this.priceType = newType;

            // Adjust priority based on type
            if (newType == PriceType.PROMOTIONAL) {
                this.priority = 10;
            } else if (newType == PriceType.TIERED) {
                this.priority = 5;
            } else {
                this.priority = 0;
            }

            registerEvent(new EntityUpdatedEvent<>(this, tracker.getChanges()));
        }
    }

    /**
     * Update validity period
     */
    public void updateValidityPeriod(LocalDateTime validFrom, LocalDateTime validTo) {
        validatePeriod(validFrom, validTo);

        ChangeTracker tracker = new ChangeTracker();

        if (!java.util.Objects.equals(this.validFrom, validFrom)) {
            tracker.track("validFrom", this.validFrom, validFrom);
            this.validFrom = validFrom;
        }

        if (!java.util.Objects.equals(this.validTo, validTo)) {
            tracker.track("validTo", this.validTo, validTo);
            this.validTo = validTo;
        }

        if (tracker.hasChanges()) {
            registerEvent(new EntityUpdatedEvent<>(this, tracker.getChanges()));
        }
    }

    /**
     * Update discount
     */
    public void updateDiscount(BigDecimal discountPercentage, Money originalPrice) {
        if (this.priceType != PriceType.PROMOTIONAL) {
            throw new IllegalStateException("Discount can only be set on promotional prices");
        }

        validateDiscount(discountPercentage);
        validatePrice(originalPrice);

        ChangeTracker tracker = new ChangeTracker();

        tracker.track("discountPercentage", this.discountPercentage, discountPercentage);
        tracker.track("originalPrice", this.originalPrice, originalPrice.getAmount());

        this.discountPercentage = discountPercentage;
        this.originalPrice = originalPrice.getAmount();

        // Recalculate price and discount amount
        Money discountedPrice = originalPrice.multiplyBy(
                BigDecimal.ONE.subtract(discountPercentage.divide(BigDecimal.valueOf(100)))
        );

        this.price = discountedPrice;
        this.discountAmount = originalPrice.subtract(discountedPrice);

        tracker.track("price", null, discountedPrice);
        tracker.track("discountAmount", null, this.discountAmount);

        registerEvent(new EntityUpdatedEvent<>(this, tracker.getChanges()));
    }

    /**
     * Update tax information
     */
    public void updateTaxInfo(Boolean taxInclusive, BigDecimal taxRate, String taxClass) {
        ChangeTracker tracker = new ChangeTracker();

        if (taxInclusive != null && !taxInclusive.equals(this.taxInclusive)) {
            tracker.track("taxInclusive", this.taxInclusive, taxInclusive);
            this.taxInclusive = taxInclusive;
        }

        if (taxRate != null && !taxRate.equals(this.taxRate)) {
            if (taxRate.compareTo(BigDecimal.ZERO) < 0 || taxRate.compareTo(BigDecimal.valueOf(100)) > 0) {
                throw new IllegalArgumentException("Tax rate must be between 0 and 100");
            }
            tracker.track("taxRate", this.taxRate, taxRate);
            this.taxRate = taxRate;
        }

        if (taxClass != null && !taxClass.equals(this.taxClass)) {
            tracker.track("taxClass", this.taxClass, taxClass);
            this.taxClass = taxClass;
        }

        if (tracker.hasChanges()) {
            registerEvent(new EntityUpdatedEvent<>(this, tracker.getChanges()));
        }
    }

    /**
     * Update priority
     */
    public void updatePriority(Integer priority) {
        if (priority != null && !priority.equals(this.priority)) {
            ChangeTracker tracker = new ChangeTracker();
            tracker.track("priority", this.priority, priority);

            this.priority = priority;

            registerEvent(new EntityUpdatedEvent<>(this, tracker.getChanges()));
        }
    }

    /**
     * Update customer group and market
     */
    public void updateSegmentation(String customerGroup, String market) {
        ChangeTracker tracker = new ChangeTracker();

        if (customerGroup != null && !customerGroup.equals(this.customerGroup)) {
            tracker.track("customerGroup", this.customerGroup, customerGroup);
            this.customerGroup = customerGroup;
        }

        if (market != null && !market.equals(this.market)) {
            tracker.track("market", this.market, market);
            this.market = market;
        }

        if (tracker.hasChanges()) {
            registerEvent(new EntityUpdatedEvent<>(this, tracker.getChanges()));
        }
    }

    // ====================================
    // Domain Logic - Activation
    // ====================================

    /**
     * Activate price
     *
     * Events Published:
     * - PriceActivatedEvent
     */
    public void activate() {
        if (Boolean.TRUE.equals(this.active)) {
            throw new IllegalStateException("Price is already active");
        }

        this.active = true;

        registerEvent(PriceActivatedEvent.builder()
                .priceId(this.getId())
                .productId(this.product != null ? this.product.getId() : null)
                .priceType(this.priceType)
                .price(this.price)
                .occurredAt(LocalDateTime.now())
                .build());
    }

    /**
     * Deactivate price
     *
     * Events Published:
     * - PriceDeactivatedEvent
     */
    public void deactivate(String reason) {
        if (Boolean.FALSE.equals(this.active)) {
            throw new IllegalStateException("Price is already inactive");
        }

        if (this.priceType == PriceType.BASE && this.product != null) {
            // Check if there are other active base prices
            long activeBasePrices = this.product.getPrices().stream()
                    .filter(p -> p.getPriceType() == PriceType.BASE)
                    .filter(p -> Boolean.TRUE.equals(p.getActive()))
                    .filter(p -> !p.equals(this))
                    .count();

            if (activeBasePrices == 0) {
                throw new IllegalStateException(
                        "Cannot deactivate the only active base price. Product must have at least one active base price.");
            }
        }

        this.active = false;

        registerEvent(PriceDeactivatedEvent.builder()
                .priceId(this.getId())
                .productId(this.product != null ? this.product.getId() : null)
                .priceType(this.priceType)
                .price(this.price)
                .reason(reason)
                .occurredAt(LocalDateTime.now())
                .build());
    }

    // ====================================
    // Query Methods
    // ====================================

    @Override
    protected DomainEvent createDeletedEvent(String deletedBy) {
        return null;
    }

    @Override
    protected DomainEvent createRestoredEvent(String restoredBy, String previousDeletedBy) {
        return null;
    }

    /**
     * Check if price is currently active based on validity period
     */
    @Transient
    public boolean isActive() {
        if (Boolean.FALSE.equals(active)) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();

        if (validFrom != null && now.isBefore(validFrom)) {
            return false;
        }

        if (validTo != null && now.isAfter(validTo)) {
            return false;
        }

        return true;
    }

    /**
     * Check if price is scheduled (future)
     */
    @Transient
    public boolean isScheduled() {
        if (validFrom == null) {
            return false;
        }
        return LocalDateTime.now().isBefore(validFrom);
    }

    /**
     * Check if price is expired
     */
    @Transient
    public boolean isExpired() {
        if (validTo == null) {
            return false;
        }
        return LocalDateTime.now().isAfter(validTo);
    }

    /**
     * Get price including tax
     */
    @Transient
    public Money getPriceIncludingTax() {
        if (Boolean.TRUE.equals(taxInclusive)) {
            return price;
        }

        if (taxRate != null) {
            BigDecimal taxMultiplier = BigDecimal.ONE.add(
                    taxRate.divide(BigDecimal.valueOf(100))
            );
            return price.multiplyBy(taxMultiplier);
        }

        return price;
    }

    /**
     * Get price excluding tax
     */
    @Transient
    public Money getPriceExcludingTax() {
        if (Boolean.FALSE.equals(taxInclusive)) {
            return price;
        }

        if (taxRate != null) {
            BigDecimal taxDivisor = BigDecimal.ONE.add(
                    taxRate.divide(BigDecimal.valueOf(100))
            );
            return price.divideBy(taxDivisor);
        }

        return price;
    }

    /**
     * Get tax amount
     */
    @Transient
    public Money getTaxAmount() {
        if (taxRate == null) {
            return Money.zero(price.getCurrency());
        }

        Money basePrice = getPriceExcludingTax();
        Money priceWithTax = getPriceIncludingTax();

        return priceWithTax.subtract(basePrice);
    }

    /**
     * Get savings amount (for promotional prices)
     */
    @Transient
    public Money getSavings() {
        if (discountAmount != null) {
            return discountAmount;
        }

        if (originalPrice != null) {
            Money original = Money.of(originalPrice, price.getCurrency());
            return original.subtract(price);
        }

        return Money.zero(price.getCurrency());
    }

    /**
     * Get savings percentage
     */
    @Transient
    public BigDecimal getSavingsPercentage() {
        if (discountPercentage != null) {
            return discountPercentage;
        }

        if (originalPrice != null && originalPrice.compareTo(BigDecimal.ZERO) > 0) {
            Money original = Money.of(originalPrice, price.getCurrency());
            Money savings = getSavings();
            return savings.getAmount()
                    .divide(original.getAmount(), 4, java.math.RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }

        return BigDecimal.ZERO;
    }

    // ====================================
    // Helper Methods
    // ====================================

    /**
     * Recalculate discount based on current price and original price
     */
    private void recalculateDiscount() {
        if (originalPrice != null) {
            Money original = Money.of(originalPrice, price.getCurrency());
            this.discountAmount = original.subtract(price);

            if (originalPrice.compareTo(BigDecimal.ZERO) > 0) {
                this.discountPercentage = discountAmount.getAmount()
                        .divide(original.getAmount(), 4, java.math.RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));
            }
        }
    }

    // ====================================
    // Validation
    // ====================================

    private static void validatePrice(Money price) {
        if (price == null) {
            throw new IllegalArgumentException("Price cannot be null");
        }
        if (price.getAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Price amount cannot be negative");
        }
    }

    private static void validateDiscount(BigDecimal discountPercentage) {
        if (discountPercentage == null) {
            throw new IllegalArgumentException("Discount percentage cannot be null");
        }
        if (discountPercentage.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Discount percentage cannot be negative");
        }
        if (discountPercentage.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new IllegalArgumentException("Discount percentage cannot exceed 100%");
        }
    }

    private static void validatePeriod(LocalDateTime validFrom, LocalDateTime validTo) {
        if (validFrom != null && validTo != null && validFrom.isAfter(validTo)) {
            throw new IllegalArgumentException("Valid from date must be before valid to date");
        }
    }

    /**
     * Validate price is applicable for promotional type
     */
    public void validatePromotionalPrice(Money basePrice) {
        if (this.priceType != PriceType.PROMOTIONAL) {
            return;
        }

        if (basePrice != null && this.price.getAmount().compareTo(basePrice.getAmount()) >= 0) {
            throw new IllegalArgumentException(
                    "Promotional price must be less than base price");
        }

        if (validFrom == null || validTo == null) {
            throw new IllegalArgumentException(
                    "Promotional price must have validity period");
        }
    }

    /**
     * Clone price for variant or duplication
     */
    public ProductPrice clone() {
        return ProductPrice.builder()
                .price(this.price)
                .priceType(this.priceType)
                .active(this.active)
                .priority(this.priority)
                .validFrom(this.validFrom)
                .validTo(this.validTo)
                .discountPercentage(this.discountPercentage)
                .discountAmount(this.discountAmount)
                .originalPrice(this.originalPrice)
                .taxInclusive(this.taxInclusive)
                .taxRate(this.taxRate)
                .taxClass(this.taxClass)
                .customerGroup(this.customerGroup)
                .market(this.market)
                .notes(this.notes)
                .build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProductPrice)) return false;
        ProductPrice that = (ProductPrice) o;
        return getId() != null && getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return String.format("ProductPrice[id=%s, type=%s, price=%s, active=%s]",
                getId(), priceType, price, active);
    }
}