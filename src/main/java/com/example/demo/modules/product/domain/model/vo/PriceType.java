package com.example.demo.modules.product.domain.model.vo;

/**
 * Price Type Enumeration
 *
 * Defines different types of prices for products
 */
public enum PriceType {
    /**
     * Standard/regular price
     */
    BASE,

    /**
     * Promotional/sale price
     */
    PROMOTIONAL,

    /**
     * Tiered pricing based on customer group
     */
    TIERED,

    /**
     * Wholesale price
     */
    WHOLESALE,

    /**
     * Retail price (MSRP)
     */
    RETAIL,

    /**
     * Member/subscription price
     */
    MEMBER,

    /**
     * Clearance price
     */
    CLEARANCE,

    /**
     * Bundle price
     */
    BUNDLE;

    public boolean requiresValidityPeriod() {
        return this == PROMOTIONAL || this == CLEARANCE;
    }

    public boolean supportsDiscount() {
        return this == PROMOTIONAL || this == CLEARANCE || this == MEMBER;
    }
}