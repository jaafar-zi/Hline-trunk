package com.example.demo.modules.product.domain.model.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;

/**
 * Money Value Object
 * 
 * Immutable value object representing monetary amounts
 * 
 * Business Rules:
 * - Amount cannot be null
 * - Currency must be valid ISO 4217 code
 * - Scale is fixed at 4 decimal places for precision
 * - All arithmetic operations return new instances (immutability)
 * - Currency must match for arithmetic operations
 * 
 * Design Patterns:
 * - Value Object pattern
 * - Money pattern (Fowler)
 * 
 * @author PIM Team
 * @version 1.0
 */
@Embeddable
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED) // For JPA
public class Money implements Serializable, Comparable<Money> {

    private static final long serialVersionUID = 1L;
    private static final int SCALE = 4;
    private static final RoundingMode DEFAULT_ROUNDING = RoundingMode.HALF_UP;

    @Column(name = "amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    /**
     * Private constructor - use factory methods
     */
    private Money(BigDecimal amount, String currency) {
        this.amount = normalize(amount);
        this.currency = validateAndNormalizeCurrency(currency);
    }

    // ====================================
    // Factory Methods
    // ====================================

    /**
     * Create Money from amount and currency
     */
    public static Money of(BigDecimal amount, String currency) {
        if (amount == null) {
            throw new IllegalArgumentException("Amount cannot be null");
        }
        if (currency == null || currency.isBlank()) {
            throw new IllegalArgumentException("Currency cannot be null or empty");
        }
        return new Money(amount, currency);
    }

    /**
     * Create Money from double amount
     */
    public static Money of(double amount, String currency) {
        return of(BigDecimal.valueOf(amount), currency);
    }

    /**
     * Create Money from long amount (in cents/minor units)
     */
    public static Money ofMinor(long amountInMinorUnits, String currency) {
        BigDecimal amount = BigDecimal.valueOf(amountInMinorUnits)
                .divide(BigDecimal.valueOf(100), SCALE, DEFAULT_ROUNDING);
        return new Money(amount, currency);
    }

    /**
     * Create zero money
     */
    public static Money zero(String currency) {
        return new Money(BigDecimal.ZERO, currency);
    }

    /**
     * Create money in USD
     */
    public static Money usd(BigDecimal amount) {
        return of(amount, "USD");
    }

    public static Money usd(double amount) {
        return of(amount, "USD");
    }

    /**
     * Create money in EUR
     */
    public static Money eur(BigDecimal amount) {
        return of(amount, "EUR");
    }

    public static Money eur(double amount) {
        return of(amount, "EUR");
    }

    /**
     * Create money in GBP
     */
    public static Money gbp(BigDecimal amount) {
        return of(amount, "GBP");
    }

    public static Money gbp(double amount) {
        return of(amount, "GBP");
    }

    // ====================================
    // Arithmetic Operations
    // ====================================

    /**
     * Add money (same currency required)
     */
    public Money add(Money other) {
        assertSameCurrency(other);
        return new Money(this.amount.add(other.amount), this.currency);
    }

    /**
     * Subtract money (same currency required)
     */
    public Money subtract(Money other) {
        assertSameCurrency(other);
        return new Money(this.amount.subtract(other.amount), this.currency);
    }

    /**
     * Multiply by a factor
     */
    public Money multiplyBy(BigDecimal factor) {
        if (factor == null) {
            throw new IllegalArgumentException("Multiplication factor cannot be null");
        }
        return new Money(this.amount.multiply(factor), this.currency);
    }

    /**
     * Multiply by a double factor
     */
    public Money multiplyBy(double factor) {
        return multiplyBy(BigDecimal.valueOf(factor));
    }

    /**
     * Multiply by an integer factor
     */
    public Money multiplyBy(int factor) {
        return multiplyBy(BigDecimal.valueOf(factor));
    }

    /**
     * Divide by a divisor
     */
    public Money divideBy(BigDecimal divisor) {
        if (divisor == null) {
            throw new IllegalArgumentException("Divisor cannot be null");
        }
        if (divisor.compareTo(BigDecimal.ZERO) == 0) {
            throw new ArithmeticException("Cannot divide by zero");
        }
        return new Money(this.amount.divide(divisor, SCALE, DEFAULT_ROUNDING), this.currency);
    }

    /**
     * Divide by a double divisor
     */
    public Money divideBy(double divisor) {
        return divideBy(BigDecimal.valueOf(divisor));
    }

    /**
     * Divide by an integer divisor
     */
    public Money divideBy(int divisor) {
        return divideBy(BigDecimal.valueOf(divisor));
    }

    /**
     * Calculate percentage of this amount
     * 
     * @param percentage Percentage value (e.g., 20 for 20%)
     * @return Money representing the percentage amount
     */
    public Money percentage(BigDecimal percentage) {
        if (percentage == null) {
            throw new IllegalArgumentException("Percentage cannot be null");
        }
        BigDecimal factor = percentage.divide(BigDecimal.valueOf(100), SCALE, DEFAULT_ROUNDING);
        return multiplyBy(factor);
    }

    /**
     * Calculate percentage of this amount
     */
    public Money percentage(double percentage) {
        return percentage(BigDecimal.valueOf(percentage));
    }

    /**
     * Negate the amount
     */
    public Money negate() {
        return new Money(this.amount.negate(), this.currency);
    }

    /**
     * Get absolute value
     */
    public Money abs() {
        return new Money(this.amount.abs(), this.currency);
    }

    // ====================================
    // Allocation/Distribution
    // ====================================

    /**
     * Allocate money into N equal parts
     * Handles remainder by distributing to first items
     * 
     * Example: $10 / 3 = [$3.34, $3.33, $3.33]
     */
    public Money[] allocate(int parts) {
        if (parts <= 0) {
            throw new IllegalArgumentException("Number of parts must be positive");
        }

        Money[] results = new Money[parts];
        Money lowResult = divideBy(parts);
        Money highResult = lowResult.add(Money.of(
                BigDecimal.ONE.divide(BigDecimal.valueOf(10000), SCALE, DEFAULT_ROUNDING),
                currency
        ));

        BigDecimal total = lowResult.amount.multiply(BigDecimal.valueOf(parts));
        BigDecimal remainder = this.amount.subtract(total);
        int remainderPennies = remainder.multiply(BigDecimal.valueOf(10000))
                .setScale(0, RoundingMode.HALF_UP)
                .intValue();

        for (int i = 0; i < parts; i++) {
            results[i] = (i < remainderPennies) ? highResult : lowResult;
        }

        return results;
    }

    /**
     * Allocate money by ratios
     * 
     * Example: $100 with ratios [1, 1, 3] = [$20, $20, $60]
     */
    public Money[] allocate(int[] ratios) {
        if (ratios == null || ratios.length == 0) {
            throw new IllegalArgumentException("Ratios cannot be null or empty");
        }

        int total = 0;
        for (int ratio : ratios) {
            if (ratio < 0) {
                throw new IllegalArgumentException("Ratios cannot be negative");
            }
            total += ratio;
        }

        if (total == 0) {
            throw new IllegalArgumentException("Total of ratios cannot be zero");
        }

        Money[] results = new Money[ratios.length];
        BigDecimal totalAllocated = BigDecimal.ZERO;

        for (int i = 0; i < ratios.length; i++) {
            if (i == ratios.length - 1) {
                // Last item gets the remainder to avoid rounding issues
                results[i] = new Money(this.amount.subtract(totalAllocated), this.currency);
            } else {
                BigDecimal share = this.amount
                        .multiply(BigDecimal.valueOf(ratios[i]))
                        .divide(BigDecimal.valueOf(total), SCALE, DEFAULT_ROUNDING);
                results[i] = new Money(share, this.currency);
                totalAllocated = totalAllocated.add(share);
            }
        }

        return results;
    }

    // ====================================
    // Comparison Operations
    // ====================================

    /**
     * Check if this money is greater than other
     */
    public boolean isGreaterThan(Money other) {
        assertSameCurrency(other);
        return this.amount.compareTo(other.amount) > 0;
    }

    /**
     * Check if this money is greater than or equal to other
     */
    public boolean isGreaterThanOrEqualTo(Money other) {
        assertSameCurrency(other);
        return this.amount.compareTo(other.amount) >= 0;
    }

    /**
     * Check if this money is less than other
     */
    public boolean isLessThan(Money other) {
        assertSameCurrency(other);
        return this.amount.compareTo(other.amount) < 0;
    }

    /**
     * Check if this money is less than or equal to other
     */
    public boolean isLessThanOrEqualTo(Money other) {
        assertSameCurrency(other);
        return this.amount.compareTo(other.amount) <= 0;
    }

    /**
     * Check if amount is zero
     */
    public boolean isZero() {
        return this.amount.compareTo(BigDecimal.ZERO) == 0;
    }

    /**
     * Check if amount is positive
     */
    public boolean isPositive() {
        return this.amount.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * Check if amount is negative
     */
    public boolean isNegative() {
        return this.amount.compareTo(BigDecimal.ZERO) < 0;
    }

    /**
     * Check if amount is positive or zero
     */
    public boolean isPositiveOrZero() {
        return this.amount.compareTo(BigDecimal.ZERO) >= 0;
    }

    @Override
    public int compareTo(Money other) {
        assertSameCurrency(other);
        return this.amount.compareTo(other.amount);
    }

    // ====================================
    // Currency Operations
    // ====================================

    /**
     * Check if same currency as other money
     */
    public boolean hasSameCurrency(Money other) {
        if (other == null) {
            return false;
        }
        return this.currency.equals(other.currency);
    }

    /**
     * Get Currency object
     */
    public Currency getCurrencyInstance() {
        return Currency.getInstance(currency);
    }

    /**
     * Get currency symbol (e.g., $, €, £)
     */
    public String getCurrencySymbol() {
        return getCurrencyInstance().getSymbol();
    }

    /**
     * Get amount in minor units (cents)
     */
    public long getAmountInMinorUnits() {
        return amount.multiply(BigDecimal.valueOf(100))
                .setScale(0, DEFAULT_ROUNDING)
                .longValue();
    }

    // ====================================
    // Formatting
    // ====================================

    /**
     * Format as currency string
     * Example: $1,234.56
     */
    public String format() {
        java.text.NumberFormat formatter = java.text.NumberFormat.getCurrencyInstance();
        formatter.setCurrency(getCurrencyInstance());
        return formatter.format(amount);
    }

    /**
     * Format with specific locale
     */
    public String format(java.util.Locale locale) {
        java.text.NumberFormat formatter = java.text.NumberFormat.getCurrencyInstance(locale);
        formatter.setCurrency(getCurrencyInstance());
        return formatter.format(amount);
    }

    /**
     * Simple format without symbol
     * Example: 1234.56
     */
    public String formatWithoutSymbol() {
        return String.format("%." + SCALE + "f", amount);
    }

    /**
     * Format with symbol but without thousands separator
     * Example: USD 1234.56
     */
    public String formatSimple() {
        return String.format("%s %." + SCALE + "f", currency, amount);
    }

    // ====================================
    // Helper Methods
    // ====================================

    /**
     * Normalize amount to standard scale
     */
    private BigDecimal normalize(BigDecimal amount) {
        if (amount == null) {
            throw new IllegalArgumentException("Amount cannot be null");
        }
        return amount.setScale(SCALE, DEFAULT_ROUNDING);
    }

    /**
     * Validate and normalize currency code
     */
    private String validateAndNormalizeCurrency(String currency) {
        if (currency == null || currency.isBlank()) {
            throw new IllegalArgumentException("Currency cannot be null or empty");
        }

        String normalized = currency.trim().toUpperCase();

        // Validate ISO 4217 currency code
        try {
            Currency.getInstance(normalized);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Invalid currency code: " + currency + ". Must be a valid ISO 4217 code.", e
            );
        }

        return normalized;
    }

    /**
     * Assert that currencies match for operations
     */
    private void assertSameCurrency(Money other) {
        if (other == null) {
            throw new IllegalArgumentException("Money object cannot be null");
        }
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException(
                    String.format("Currency mismatch: %s vs %s", this.currency, other.currency)
            );
        }
    }

    // ====================================
    // Min/Max Operations
    // ====================================

    /**
     * Get minimum of two money values
     */
    public static Money min(Money money1, Money money2) {
        if (money1 == null) return money2;
        if (money2 == null) return money1;
        return money1.isLessThan(money2) ? money1 : money2;
    }

    /**
     * Get maximum of two money values
     */
    public static Money max(Money money1, Money money2) {
        if (money1 == null) return money2;
        if (money2 == null) return money1;
        return money1.isGreaterThan(money2) ? money1 : money2;
    }

    /**
     * Sum array of money values
     */
    public static Money sum(Money... moneys) {
        if (moneys == null || moneys.length == 0) {
            throw new IllegalArgumentException("At least one money value required");
        }

        Money result = moneys[0];
        for (int i = 1; i < moneys.length; i++) {
            result = result.add(moneys[i]);
        }
        return result;
    }

    // ====================================
    // Rounding Operations
    // ====================================

    /**
     * Round to nearest currency unit (e.g., nearest cent)
     */
    public Money roundToCurrencyUnit() {
        int fractionDigits = getCurrencyInstance().getDefaultFractionDigits();
        return new Money(
                amount.setScale(fractionDigits, DEFAULT_ROUNDING),
                currency
        );
    }

    /**
     * Round to specific scale
     */
    public Money round(int scale, RoundingMode roundingMode) {
        if (scale < 0) {
            throw new IllegalArgumentException("Scale cannot be negative");
        }
        if (roundingMode == null) {
            roundingMode = DEFAULT_ROUNDING;
        }
        return new Money(amount.setScale(scale, roundingMode), currency);
    }

    // ====================================
    // Object Overrides
    // ====================================

    @Override
    public String toString() {
        return String.format("%s %s", currency, amount.toPlainString());
    }

    /**
     * Create a copy of this money object
     */
    public Money copy() {
        return new Money(this.amount, this.currency);
    }

    /**
     * Parse money from string
     * Formats supported: "USD 100.50", "100.50 USD", "100.50"
     */
    public static Money parse(String moneyString, String defaultCurrency) {
        if (moneyString == null || moneyString.isBlank()) {
            throw new IllegalArgumentException("Money string cannot be null or empty");
        }

        String trimmed = moneyString.trim();
        String[] parts = trimmed.split("\\s+");

        if (parts.length == 1) {
            // Only amount provided
            return Money.of(new BigDecimal(parts[0]), defaultCurrency);
        } else if (parts.length == 2) {
            // Currency and amount
            try {
                // Try "CURRENCY AMOUNT" format
                Currency.getInstance(parts[0]);
                return Money.of(new BigDecimal(parts[1]), parts[0]);
            } catch (IllegalArgumentException e) {
                // Try "AMOUNT CURRENCY" format
                return Money.of(new BigDecimal(parts[0]), parts[1]);
            }
        }

        throw new IllegalArgumentException("Invalid money format: " + moneyString);
    }
}