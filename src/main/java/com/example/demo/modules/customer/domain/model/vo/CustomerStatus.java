package com.example.demo.modules.customer.domain.model.vo;

/**
 * Customer account status
 * 
 * Lifecycle:
 * ACTIVE -> INACTIVE (customer deactivates account)
 * ACTIVE -> SUSPENDED (admin suspends for policy violation)
 * ACTIVE/INACTIVE/SUSPENDED -> BLOCKED (admin blocks for severe violation)
 * 
 * @author Customer Domain Team
 */
public enum CustomerStatus {
    /**
     * Customer account is active and can place orders
     */
    ACTIVE,
    
    /**
     * Customer voluntarily deactivated account
     * Can be reactivated by customer
     */
    INACTIVE,
    
    /**
     * Account suspended by admin (temporary)
     * Requires admin review to reactivate
     */
    SUSPENDED,
    
    /**
     * Account blocked by admin (severe)
     * Requires admin approval to unblock
     */
    BLOCKED;
    
    public boolean canPlaceOrder() {
        return this == ACTIVE;
    }
    
    public boolean canLogin() {
        return this == ACTIVE || this == INACTIVE;
    }
}