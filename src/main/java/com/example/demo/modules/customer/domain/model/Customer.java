package com.example.demo.modules.customer.domain.model;

import com.example.demo.common.base.entity.SoftDeletableAggregateRoot;
import com.example.demo.common.event.DomainEvent;
import com.example.demo.modules.customer.domain.event.*;
import com.example.demo.modules.customer.domain.model.vo.AddressType;
import com.example.demo.modules.customer.domain.model.vo.CustomerName;
import com.example.demo.modules.customer.domain.model.vo.CustomerPreferences;
import com.example.demo.modules.customer.domain.model.vo.CustomerStatus;
import com.example.demo.modules.customer.domain.model.vo.Email;
import com.example.demo.modules.customer.domain.model.vo.Phone;
import com.example.demo.modules.order.domain.model.vo.Address;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Customer Aggregate Root
 * 
 * Responsibilities:
 * - Manage customer profile and credentials
 * - Handle customer lifecycle (activate, deactivate, suspend, block)
 * - Manage customer addresses (add, update, remove, set preferred)
 * - Maintain customer preferences
 * - Enforce business rules and invariants
 * 
 * Business Rules:
 * - Email must be unique across all customers
 * - Customer must have at least one address after first address is added
 * - Only ACTIVE customers can place orders
 * - Soft delete preserves customer history
 * 
 * @author Customer Domain Team
 */
@Entity
@Table(
    name = "customers",
    indexes = {
        @Index(name = "idx_customer_email", columnList = "email"),
        @Index(name = "idx_customer_status", columnList = "status"),
        @Index(name = "idx_customer_deleted", columnList = "deleted")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer extends SoftDeletableAggregateRoot {
    
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "firstName", column = @Column(name = "first_name", nullable = false)),
        @AttributeOverride(name = "lastName", column = @Column(name = "last_name", nullable = false))
    })
    private CustomerName name;
    
    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "email", unique = true, nullable = false, length = 255))
    private Email email;
    
    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "phone", nullable = false, length = 20))
    private Phone phone;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private CustomerStatus status = CustomerStatus.ACTIVE;
    
    @OneToMany(
        mappedBy = "customer",
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    @OrderBy("id ASC")
    @Builder.Default
    private List<CustomerAddress> addresses = new ArrayList<>();
    
    @Column(name = "preferred_address_id")
    private Long preferredAddressId;
    
    @Embedded
    private CustomerPreferences preferences;

    /**
     * Constructor for new customer registration
     */
    public Customer(CustomerName name, Email email, Phone phone) {
        validateCustomerCreation(name, email, phone);
        
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.status = CustomerStatus.ACTIVE;
        this.addresses = new ArrayList<>();
        this.preferences = CustomerPreferences.defaultPreferences();
        
        // Register domain event
        registerEvent(new CustomerRegisteredEvent(
            this.getId(),
            this.getVersion(),
            email.getValue(),
            name.getFirstName(),
            name.getLastName()
        ));
    }
    
    // ========== Business Methods ==========

    /**
     * Update customer profile information
     */
    public void updateProfile(CustomerName name, Phone phone) {
        validateNotDeleted();
        validateProfileUpdate(name, phone);
        
        this.name = name;
        this.phone = phone;
        
        registerEvent(new CustomerProfileUpdatedEvent(
            this.getId(),
            this.getVersion(),
            name.getFullName(),
            phone.getValue()
        ));
    }

    /**
     * Activate customer account
     * 
     * @throws IllegalStateException if already active or deleted
     */
    public void activate() {
        validateNotDeleted();
        
        if (this.status == CustomerStatus.ACTIVE) {
            throw new IllegalStateException(
                "Customer is already active: " + this.getId()
            );
        }
        
        this.status = CustomerStatus.ACTIVE;
        
        registerEvent(new CustomerActivatedEvent(
            this.getId(),
            this.getVersion()
        ));
    }

    /**
     * Deactivate customer account (customer-initiated)
     * 
     * @throws IllegalStateException if already inactive or deleted
     */
    public void deactivate() {
        validateNotDeleted();
        
        if (this.status == CustomerStatus.INACTIVE) {
            throw new IllegalStateException(
                "Customer is already inactive: " + this.getId()
            );
        }
        
        this.status = CustomerStatus.INACTIVE;
        
        registerEvent(new CustomerDeactivatedEvent(
            this.getId(),
            this.getVersion()
        ));
    }

    /**
     * Suspend customer account (admin-initiated, temporary)
     * 
     * @param reason reason for suspension
     * @throws IllegalStateException if already suspended or deleted
     */
    public void suspend(String reason) {
        validateNotDeleted();
        
        if (this.status == CustomerStatus.SUSPENDED) {
            throw new IllegalStateException(
                "Customer is already suspended: " + this.getId()
            );
        }
        
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Suspension reason is required");
        }
        
        this.status = CustomerStatus.SUSPENDED;
        
        registerEvent(new CustomerSuspendedEvent(
            this.getId(),
            this.getVersion(),
            reason
        ));
    }

    /**
     * Block customer account (admin-initiated, severe)
     * 
     * @param reason reason for blocking
     * @throws IllegalStateException if already blocked or deleted
     */
    public void block(String reason) {
        validateNotDeleted();
        
        if (this.status == CustomerStatus.BLOCKED) {
            throw new IllegalStateException(
                "Customer is already blocked: " + this.getId()
            );
        }
        
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Block reason is required");
        }
        
        this.status = CustomerStatus.BLOCKED;
        
        registerEvent(new CustomerBlockedEvent(
            this.getId(),
            this.getVersion(),
            reason
        ));
    }
    
    /**
     * Add a new address to customer
     * 
     * @param address the address to add
     * @param type the type of address
     * @return the created CustomerAddress
     */
    public CustomerAddress addAddress(Address address, AddressType type) {
        validateNotDeleted();
        validateAddress(address, type);
        
        boolean isFirstAddress = addresses.isEmpty();
        
        CustomerAddress customerAddress = new CustomerAddress(
            address,
            type,
            isFirstAddress // First address is automatically preferred
        );
        
        customerAddress.setCustomer(this); // Set the relationship
        addresses.add(customerAddress);
        
        // Note: preferredAddressId will be set after persistence when ID is generated
        // You may need to handle this in your service layer after saving
        
        registerEvent(new AddressAddedEvent(
            this.getId(),
            this.getVersion(),
            null, // ID not yet available, will be set after save
            type
        ));
        
        return customerAddress;
    }

    /**
     * Update an existing address
     * 
     * @param addressId ID of address to update
     * @param address new address details
     * @throws IllegalArgumentException if address not found
     */
    public void updateAddress(Long addressId, Address address) {
        validateNotDeleted();
        
        CustomerAddress customerAddress = findAddressById(addressId)
            .orElseThrow(() -> new IllegalArgumentException(
                "Address not found: " + addressId
            ));
        
        customerAddress.updateAddress(address);
        
        registerEvent(new AddressUpdatedEvent(
            this.getId(),
            this.getVersion(),
            addressId
        ));
    }

    /**
     * Remove an address from customer
     * 
     * @param addressId ID of address to remove
     * @throws IllegalStateException if trying to remove only address
     * @throws IllegalArgumentException if address not found
     */
    public void removeAddress(Long addressId) {
        validateNotDeleted();
        
        if (addresses.size() <= 1) {
            throw new IllegalStateException(
                "Cannot remove the only address. Customer must have at least one address."
            );
        }
        
        CustomerAddress customerAddress = findAddressById(addressId)
            .orElseThrow(() -> new IllegalArgumentException(
                "Address not found: " + addressId
            ));
        
        addresses.remove(customerAddress);
        
        // If removed address was preferred, set first remaining as preferred
        if (addressId.equals(preferredAddressId)) {
            this.preferredAddressId = addresses.get(0).getId();
            addresses.get(0).markAsPreferred();
        }
        
        registerEvent(new AddressRemovedEvent(
            this.getId(),
            this.getVersion(),
            addressId
        ));
    }

    /**
     * Set preferred address for customer
     * 
     * @param addressId ID of address to set as preferred
     * @throws IllegalArgumentException if address not found
     */
    public void setPreferredAddress(Long addressId) {
        validateNotDeleted();
        
        CustomerAddress newPreferred = findAddressById(addressId)
            .orElseThrow(() -> new IllegalArgumentException(
                "Address not found: " + addressId
            ));
        
        // Unmark previous preferred
        if (preferredAddressId != null) {
            findAddressById(preferredAddressId)
                .ifPresent(CustomerAddress::unmarkAsPreferred);
        }
        
        // Mark new preferred
        newPreferred.markAsPreferred();
        this.preferredAddressId = addressId;
        
        registerEvent(new PreferredAddressChangedEvent(
            this.getId(),
            this.getVersion(),
            addressId
        ));
    }

    /**
     * Update customer preferences
     * 
     * @param preferences new preferences
     */
    public void updatePreferences(CustomerPreferences preferences) {
        validateNotDeleted();
        
        if (preferences == null) {
            throw new IllegalArgumentException("Preferences cannot be null");
        }
        
        this.preferences = preferences;
        
        registerEvent(new CustomerProfileUpdatedEvent(
            this.getId(),
            this.getVersion(),
            this.name.getFullName(),
            this.phone.getValue()
        ));
    }

    // ========== Query Methods ==========

    /**
     * Get preferred address
     */
    @Transient
    public Optional<CustomerAddress> getPreferredAddress() {
        return findAddressById(preferredAddressId);
    }

    /**
     * Get all shipping addresses
     */
    @Transient
    public List<CustomerAddress> getShippingAddresses() {
        return addresses.stream()
            .filter(CustomerAddress::canUseForShipping)
            .toList();
    }

    /**
     * Get all billing addresses
     */
    @Transient
    public List<CustomerAddress> getBillingAddresses() {
        return addresses.stream()
            .filter(CustomerAddress::canUseForBilling)
            .toList();
    }

    /**
     * Get all addresses (unmodifiable)
     */
    @Transient
    public List<CustomerAddress> getAllAddresses() {
        return Collections.unmodifiableList(addresses);
    }

    /**
     * Check if customer is active
     */
    @Transient
    public boolean isActive() {
        return this.status == CustomerStatus.ACTIVE && !isDeleted();
    }

    /**
     * Check if customer can place orders
     */
    @Transient
    public boolean canPlaceOrder() {
        return isActive() && !addresses.isEmpty();
    }

    /**
     * Check if customer can login
     */
    @Transient
    public boolean canLogin() {
        return this.status.canLogin() && !isDeleted();
    }

    // ========== Private Helper Methods ==========

    private Optional<CustomerAddress> findAddressById(Long addressId) {
        if (addressId == null) {
            return Optional.empty();
        }
        return addresses.stream()
            .filter(addr -> addr.getId().equals(addressId))
            .findFirst();
    }

    private void validateNotDeleted() {
        if (isDeleted()) {
            throw new IllegalStateException(
                "Cannot perform operation on deleted customer: " + this.getId()
            );
        }
    }

    private void validateCustomerCreation(CustomerName name, Email email, Phone phone) {
        if (name == null) {
            throw new IllegalArgumentException("Customer name is required");
        }
        if (email == null) {
            throw new IllegalArgumentException("Customer email is required");
        }
        if (phone == null) {
            throw new IllegalArgumentException("Customer phone is required");
        }
    }

    private void validateProfileUpdate(CustomerName name, Phone phone) {
        if (name == null) {
            throw new IllegalArgumentException("Customer name is required");
        }
        if (phone == null) {
            throw new IllegalArgumentException("Customer phone is required");
        }
    }

    private void validateAddress(Address address, AddressType type) {
        if (address == null) {
            throw new IllegalArgumentException("Address is required");
        }
        if (type == null) {
            throw new IllegalArgumentException("Address type is required");
        }
    }

    // ========== Soft Delete Event Methods ==========

    @Override
    protected CustomerEvent createDeletedEvent(String deletedBy) {
        return new CustomerDeletedEvent(
            this.getId(),
            this.getVersion(),
            this.email.getValue(),
            deletedBy
        );
    }

    @Override
    protected DomainEvent createRestoredEvent(String restoredBy, String previousDeletedBy) {
        return new CustomerRestoredEvent(
            this.getId(),
            this.getVersion(),
            this.email.getValue(),
            restoredBy,
            previousDeletedBy
        );
    }

    @Override
    public String getAggregateType() {
        return "Customer";
    }

    @Override
    public String toString() {
        return String.format(
            "Customer[id=%s, name=%s, email=%s, status=%s, addresses=%d, deleted=%s]",
            getId(), name, email, status, addresses.size(), isDeleted()
        );
    }
}