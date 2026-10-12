package com.example.demo.modules.customer.domain.model.vo;

/**
 * Type of customer address
 * 
 * @author Customer Domain Team
 */
public enum AddressType {
    /**
     * Billing address only
     */
    BILLING,
    
    /**
     * Shipping address only
     */
    SHIPPING,
    
    /**
     * Can be used for both billing and shipping
     */
    BOTH;
    
    public boolean canUseForBilling() {
        return this == BILLING || this == BOTH;
    }
    
    public boolean canUseForShipping() {
        return this == SHIPPING || this == BOTH;
    }
}