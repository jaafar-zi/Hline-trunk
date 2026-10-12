package com.example.demo.modules.customer.domain.model;

import com.example.demo.modules.customer.domain.model.vo.AddressType;
import com.example.demo.modules.order.domain.model.vo.Address;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entity representing a customer's address within the Customer aggregate
 * 
 * Not an aggregate root - lifecycle managed by Customer
 * 
 * @author Customer Domain Team
 */
@Entity
@Table(
    name = "customer_addresses",
    indexes = {
        @Index(name = "idx_customer_address_customer", columnList = "customer_id"),
        @Index(name = "idx_customer_address_type", columnList = "address_type"),
        @Index(name = "idx_customer_address_preferred", columnList = "is_preferred")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class CustomerAddress {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;
    
    @Embedded
    private Address address;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "address_type", nullable = false, length = 20)
    private AddressType type;
    
    @Column(name = "is_preferred", nullable = false)
    @Builder.Default
    private boolean preferred = false;

    /**
     * Constructor for creating new address (without ID - JPA will generate it)
     */
    public CustomerAddress(Address address, AddressType type, boolean preferred) {
        if (address == null) {
            throw new IllegalArgumentException("Address cannot be null");
        }
        if (type == null) {
            throw new IllegalArgumentException("Address type cannot be null");
        }
        this.address = address;
        this.type = type;
        this.preferred = preferred;
    }

    /**
     * Update the address details
     */
    public void updateAddress(Address address) {
        if (address == null) {
            throw new IllegalArgumentException("Address cannot be null");
        }
        this.address = address;
    }

    /**
     * Change the address type
     */
    public void changeType(AddressType type) {
        if (type == null) {
            throw new IllegalArgumentException("Address type cannot be null");
        }
        this.type = type;
    }

    /**
     * Mark as preferred address
     */
    public void markAsPreferred() {
        this.preferred = true;
    }

    /**
     * Unmark as preferred address
     */
    public void unmarkAsPreferred() {
        this.preferred = false;
    }
    
    /**
     * Check if address can be used for billing
     */
    @Transient
    public boolean canUseForBilling() {
        return type.canUseForBilling();
    }
    
    /**
     * Check if address can be used for shipping
     */
    @Transient
    public boolean canUseForShipping() {
        return type.canUseForShipping();
    }

    @Override
    public String toString() {
        return String.format("CustomerAddress[id=%d, type=%s, preferred=%s, %s]",
            id, type, preferred, address);
    }
}