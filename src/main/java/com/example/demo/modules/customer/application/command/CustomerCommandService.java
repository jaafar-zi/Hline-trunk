package com.example.demo.modules.customer.application.command;

import com.example.demo.common.exception.EntityNotFoundException;
import com.example.demo.common.exception.ValidationException;
import com.example.demo.modules.customer.application.dto.request.*;
import com.example.demo.modules.customer.application.dto.response.*;
import com.example.demo.modules.customer.application.mapper.CustomerMapper;
import com.example.demo.modules.customer.application.validator.*;
import com.example.demo.modules.customer.domain.model.Customer;
import com.example.demo.modules.customer.domain.model.CustomerAddress;
import com.example.demo.modules.customer.domain.model.vo.CustomerName;
import com.example.demo.modules.customer.domain.model.vo.CustomerPreferences;
import com.example.demo.modules.customer.domain.model.vo.Phone;
import com.example.demo.modules.customer.domain.repository.CustomerRepository;
import com.example.demo.modules.order.domain.model.vo.Address;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Command Service – all state-changing operations on Customer.
 *
 * Orchestration flow:
 *   normalize → validate (declarative + Spring Validator) →
 *   load aggregate → mutate (domain method) → save → publish events
 *
 * Bean Validation annotations (@ValidEmail, @UniqueField, @ValidPhone, etc.)
 * run automatically at the controller layer. Spring Validators here provide
 * an additional layer for cross-field and semantic rules.
 *
 * @author Customer Domain Team
 * @version 1.0
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class CustomerCommandService {

    private final CustomerRepository                         customerRepository;
    private final CustomerMapper                             customerMapper;
    private final ApplicationEventPublisher                  eventPublisher;

    // ── Spring Validators ────────────────────────────────────────────────────
    private final RegisterCustomerRequestValidator           registerValidator;
    private final UpdateCustomerProfileRequestValidator      profileValidator;
    private final AddressRequestValidator                    addressValidator;
    private final UpdateCustomerPreferencesRequestValidator  preferencesValidator;

    // ====================================
    // Registration
    // ====================================

    /**
     * Register a new customer.
     *
     * Email uniqueness is enforced by @UniqueField on the DTO,
     * which runs at the controller layer before this method is called.
     * The Spring Validator here handles any remaining cross-field rules.
     */
    public CustomerResponse register(RegisterCustomerRequest request) {
        log.info("Registering new customer with email: {}", request.getEmail());

        request.normalize();
        runValidator(registerValidator, request, "registerCustomer");

        Customer customer = customerMapper.fromRegisterRequest(request);
        Customer saved    = customerRepository.save(customer);

        publishDomainEvents(saved);

        log.info("Customer registered with ID: {}", saved.getId());
        return customerMapper.toResponse(saved);
    }

    // ====================================
    // Profile
    // ====================================

    public CustomerResponse updateProfile(UUID customerId,
                                          UpdateCustomerProfileRequest request) {
        log.info("Updating profile for customer: {}", customerId);

        request.normalize();
        runValidator(profileValidator, request, "updateProfile");

        Customer customer = findActiveCustomer(customerId);

        customer.updateProfile(
                new CustomerName(request.getFirstName(), request.getLastName()),
                new Phone(request.getPhone())
        );

        Customer saved = customerRepository.save(customer);
        publishDomainEvents(saved);

        log.info("Profile updated for customer: {}", customerId);
        return customerMapper.toResponse(saved);
    }

    // ====================================
    // Status Lifecycle
    // ====================================

    public CustomerResponse activate(UUID customerId) {
        log.info("Activating customer: {}", customerId);

        Customer customer = findCustomerById(customerId);
        customer.activate();

        Customer saved = customerRepository.save(customer);
        publishDomainEvents(saved);

        return customerMapper.toResponse(saved);
    }

    public CustomerResponse deactivate(UUID customerId) {
        log.info("Deactivating customer: {}", customerId);

        Customer customer = findActiveCustomer(customerId);
        customer.deactivate();

        Customer saved = customerRepository.save(customer);
        publishDomainEvents(saved);

        return customerMapper.toResponse(saved);
    }

    public CustomerResponse suspend(UUID customerId,
                                    CustomerStatusRequest request) {
        log.info("Suspending customer: {}", customerId);

        request.normalize();

        Customer customer = findActiveCustomer(customerId);
        customer.suspend(request.getReason());

        Customer saved = customerRepository.save(customer);
        publishDomainEvents(saved);

        return customerMapper.toResponse(saved);
    }

    public CustomerResponse block(UUID customerId,
                                  CustomerStatusRequest request) {
        log.info("Blocking customer: {}", customerId);

        request.normalize();

        Customer customer = findCustomerById(customerId);
        customer.block(request.getReason());

        Customer saved = customerRepository.save(customer);
        publishDomainEvents(saved);

        return customerMapper.toResponse(saved);
    }

    // ====================================
    // Soft Delete / Restore
    // ====================================

    public void delete(UUID customerId, String deletedBy) {
        log.info("Soft-deleting customer: {} by: {}", customerId, deletedBy);

        Customer customer = findActiveCustomer(customerId);
        customer.delete(deletedBy);

        customerRepository.save(customer);
        publishDomainEvents(customer);

        log.info("Customer soft-deleted: {}", customerId);
    }

    public CustomerResponse restore(UUID customerId) {
        log.info("Restoring customer: {}", customerId);

        Customer customer = customerRepository.findByIdIncludingDeleted(customerId)
                .orElseThrow(() -> new EntityNotFoundException(Customer.class, customerId));

        customer.restore();

        Customer saved = customerRepository.save(customer);
        publishDomainEvents(saved);

        log.info("Customer restored: {}", customerId);
        return customerMapper.toResponse(saved);
    }

    // ====================================
    // Addresses
    // ====================================

    public CustomerAddressResponse addAddress(UUID customerId,
                                              AddressRequest request) {
        log.info("Adding address to customer: {}", customerId);

        request.normalize();
        runValidator(addressValidator, request, "addAddress");

        Customer customer = findActiveCustomer(customerId);
        Address  address  = customerMapper.toAddress(request);

        CustomerAddress added = customer.addAddress(address, request.getType());
        Customer saved        = customerRepository.save(customer);
        publishDomainEvents(saved);

        // Retrieve persisted address (now has DB-generated ID)
        CustomerAddress persisted = saved.getAllAddresses().stream()
                .filter(a -> a.getAddress().equals(address))
                .findFirst()
                .orElse(added);

        log.info("Address added to customer: {}", customerId);
        return customerMapper.toAddressResponse(persisted);
    }

    public CustomerAddressResponse updateAddress(UUID customerId,
                                                  Long addressId,
                                                  AddressRequest request) {
        log.info("Updating address {} for customer: {}", addressId, customerId);

        request.normalize();
        runValidator(addressValidator, request, "updateAddress");

        Customer customer = findActiveCustomer(customerId);
        Address  address  = customerMapper.toAddress(request);

        customer.updateAddress(addressId, address);

        Customer saved = customerRepository.save(customer);
        publishDomainEvents(saved);

        CustomerAddress updated = saved.getAllAddresses().stream()
                .filter(a -> a.getId().equals(addressId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Address not found after update: " + addressId));

        log.info("Address {} updated for customer: {}", addressId, customerId);
        return customerMapper.toAddressResponse(updated);
    }

    public void removeAddress(UUID customerId, Long addressId) {
        log.info("Removing address {} from customer: {}", addressId, customerId);

        Customer customer = findActiveCustomer(customerId);
        customer.removeAddress(addressId);

        Customer saved = customerRepository.save(customer);
        publishDomainEvents(saved);

        log.info("Address {} removed from customer: {}", addressId, customerId);
    }

    public CustomerResponse setPreferredAddress(UUID customerId, Long addressId) {
        log.info("Setting preferred address {} for customer: {}", addressId, customerId);

        Customer customer = findActiveCustomer(customerId);
        customer.setPreferredAddress(addressId);

        Customer saved = customerRepository.save(customer);
        publishDomainEvents(saved);

        return customerMapper.toResponse(saved);
    }

    // ====================================
    // Preferences
    // ====================================

    public CustomerResponse updatePreferences(UUID customerId,
                                              UpdateCustomerPreferencesRequest request) {
        log.info("Updating preferences for customer: {}", customerId);

        request.normalize();
        runValidator(preferencesValidator, request, "updatePreferences");

        Customer            customer    = findActiveCustomer(customerId);
        CustomerPreferences preferences = customerMapper.toPreferences(request);

        customer.updatePreferences(preferences);

        Customer saved = customerRepository.save(customer);
        publishDomainEvents(saved);

        log.info("Preferences updated for customer: {}", customerId);
        return customerMapper.toResponse(saved);
    }

    // ====================================
    // Private Helpers
    // ====================================

    private Customer findActiveCustomer(UUID customerId) {
        Customer customer = findCustomerById(customerId);
        if (customer.isDeleted()) {
            throw new IllegalStateException("Customer is deleted: " + customerId);
        }
        return customer;
    }

    private Customer findCustomerById(UUID customerId) {
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new EntityNotFoundException(Customer.class, customerId));
    }

    private void publishDomainEvents(Customer customer) {
        customer.getDomainEvents().forEach(eventPublisher::publishEvent);
        customer.clearDomainEvents();
    }

    /**
     * Run a Spring Validator and throw CustomerValidationException if errors found.
     */
    private <T> void runValidator(org.springframework.validation.Validator validator,
                                   T target,
                                   String objectName) {
        Errors errors = new BeanPropertyBindingResult(target, objectName);
        validator.validate(target, errors);

        if (errors.hasErrors()) {
            String message = errors.getAllErrors().stream()
                    .map(e -> e.getDefaultMessage())
                    .collect(Collectors.joining(", "));
            throw new ValidationException( message);
        }
    }
}