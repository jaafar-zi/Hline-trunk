package com.example.demo.modules.customer.application.query;

import com.example.demo.common.base.dto.PageResponse;
import com.example.demo.common.exception.EntityNotFoundException;
import com.example.demo.common.exception.ValidationException;
import com.example.demo.modules.customer.application.dto.request.CustomerFilterRequest;
import com.example.demo.modules.customer.application.dto.response.*;
import com.example.demo.modules.customer.application.mapper.CustomerMapper;
import com.example.demo.modules.customer.application.validator.CustomerFilterRequestValidator;
import com.example.demo.modules.customer.domain.model.Customer;
import com.example.demo.modules.customer.domain.model.vo.CustomerStatus;
import com.example.demo.modules.customer.domain.repository.CustomerRepository;
import com.example.demo.modules.customer.domain.specification.CustomerSpecifications;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Query Service – all read operations for Customer.
 *
 * Read-only transactions; no state mutations.
 *
 * @author Customer Domain Team
 * @version 1.0
 */
@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CustomerQueryService {

    private final CustomerRepository            customerRepository;
    private final CustomerMapper                customerMapper;
    private final CustomerFilterRequestValidator filterValidator;

    // ====================================
    // Single Lookups
    // ====================================

    public CustomerResponse findById(UUID customerId) {
        log.debug("Finding customer by ID: {}", customerId);
        return customerMapper.toResponse(getCustomerById(customerId));
    }

    public CustomerResponse findByEmail(String email) {
        log.debug("Finding customer by email: {}", email);
        Customer customer = customerRepository
                .findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Customer not found with email: " + email));
        return customerMapper.toResponse(customer);
    }

    public boolean existsByEmail(String email) {
        return customerRepository.existsByEmail(email.trim().toLowerCase());
    }

    // ====================================
    // Paginated / Filtered Search
    // ====================================

    public PageResponse<CustomerSummaryResponse> findAll(CustomerFilterRequest filter) {
        log.debug("Searching customers with filter");

        filter.normalize();
        runFilterValidator(filter);

        Specification<Customer> spec     = buildSpecification(filter);
        Pageable                pageable = buildPageable(filter);

        Page<Customer> page = customerRepository.findAll(spec, pageable);

        return PageResponse.from(page,
                page.getContent().stream()
                    .map(customerMapper::toSummaryResponse)
                    .collect(Collectors.toList()));
    }

    // ====================================
    // Status-Based Queries
    // ====================================

    public List<CustomerSummaryResponse> findByStatus(CustomerStatus status) {
        log.debug("Finding customers by status: {}", status);
        return customerMapper.toSummaryResponseList(
                customerRepository.findByStatus(status));
    }

    public List<CustomerSummaryResponse> findActiveCustomers() {
        return customerMapper.toSummaryResponseList(
                customerRepository.findAllActive());
    }

    public List<CustomerSummaryResponse> findSuspendedCustomers() {
        return customerMapper.toSummaryResponseList(
                customerRepository.findAllSuspended());
    }

    public List<CustomerSummaryResponse> findBlockedCustomers() {
        return customerMapper.toSummaryResponseList(
                customerRepository.findAllBlocked());
    }

    // ====================================
    // Address Queries
    // ====================================

    public List<CustomerAddressResponse> findAddresses(UUID customerId) {
        log.debug("Finding addresses for customer: {}", customerId);
        return customerMapper.toAddressResponseList(
                getCustomerById(customerId).getAllAddresses());
    }

    public CustomerAddressResponse findPreferredAddress(UUID customerId) {
        log.debug("Finding preferred address for customer: {}", customerId);
        Customer customer = getCustomerById(customerId);
        return customer.getPreferredAddress()
                .map(customerMapper::toAddressResponse)
                .orElseThrow(() -> new IllegalStateException(
                        "Customer has no preferred address: " + customerId));
    }

    public List<CustomerAddressResponse> findShippingAddresses(UUID customerId) {
        return customerMapper.toAddressResponseList(
                getCustomerById(customerId).getShippingAddresses());
    }

    public List<CustomerAddressResponse> findBillingAddresses(UUID customerId) {
        return customerMapper.toAddressResponseList(
                getCustomerById(customerId).getBillingAddresses());
    }

    // ====================================
    // Statistics
    // ====================================

    public long countAll() {
        return customerRepository.countActive();
    }

    public long countByStatus(CustomerStatus status) {
        return customerRepository.countByStatus(status);
    }

    // ====================================
    // Private Helpers
    // ====================================

    private Customer getCustomerById(UUID customerId) {
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new EntityNotFoundException(
                        Customer.class, customerId));
    }

    private void runFilterValidator(CustomerFilterRequest filter) {
        Errors errors = new BeanPropertyBindingResult(filter, "customerFilter");
        filterValidator.validate(filter, errors);

        if (errors.hasErrors()) {
            String message = errors.getAllErrors().stream()
                    .map(e -> e.getDefaultMessage())
                    .collect(Collectors.joining(", "));
            throw new ValidationException( message);
        }
    }

    private Specification<Customer> buildSpecification(CustomerFilterRequest filter) {

        Specification<Customer> spec = Boolean.TRUE.equals(filter.getOnlyDeleted())
                ? CustomerSpecifications.isDeleted()
                : CustomerSpecifications.isNotDeleted();

        if (filter.getName() != null && !filter.getName().isBlank()) {
            spec = spec.and(CustomerSpecifications.fullNameContains(filter.getName()));
        }
        if (filter.getEmail() != null && !filter.getEmail().isBlank()) {
            spec = spec.and(CustomerSpecifications.emailContains(filter.getEmail()));
        }
        if (filter.getPhone() != null && !filter.getPhone().isBlank()) {
            spec = spec.and(CustomerSpecifications.phoneContains(filter.getPhone()));
        }
        if (filter.getStatus() != null) {
            spec = spec.and(CustomerSpecifications.hasStatus(filter.getStatus()));
        }
        if (filter.getLanguage() != null && !filter.getLanguage().isBlank()) {
            spec = spec.and(CustomerSpecifications.hasLanguage(filter.getLanguage()));
        }
        if (filter.getCurrency() != null && !filter.getCurrency().isBlank()) {
            spec = spec.and(CustomerSpecifications.hasCurrency(filter.getCurrency()));
        }
        if (Boolean.TRUE.equals(filter.getMarketingOptIn())) {
            spec = spec.and(CustomerSpecifications.hasMarketingEmailsEnabled());
        }
        if (Boolean.TRUE.equals(filter.getEmailNotifications())) {
            spec = spec.and(CustomerSpecifications.hasEmailNotificationsEnabled());
        }
        if (filter.getCreatedAfter() != null) {
            spec = spec.and(CustomerSpecifications.createdAfter(filter.getCreatedAfter()));
        }
        if (filter.getCreatedBefore() != null) {
            spec = spec.and(CustomerSpecifications.createdBefore(filter.getCreatedBefore()));
        }

        return spec;
    }

    private Pageable buildPageable(CustomerFilterRequest filter) {
        return PageRequest.of(
                filter.getEffectivePage(),
                filter.getEffectiveSize(),
                buildSort(filter.getSortBy(), filter.getSortDirection()));
    }

    private Sort buildSort(String sortBy, String direction) {
        if (sortBy == null || sortBy.isBlank()) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }

        Sort.Direction dir = "ASC".equalsIgnoreCase(direction)
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        return switch (sortBy) {
            case "firstName"  -> Sort.by(dir, "name.firstName");
            case "lastName"   -> Sort.by(dir, "name.lastName");
            case "email"      -> Sort.by(dir, "email.value");
            case "status"     -> Sort.by(dir, "status");
            case "createdAt"  -> Sort.by(dir, "createdAt");
            case "updatedAt"  -> Sort.by(dir, "updatedAt");
            default           -> Sort.by(Sort.Direction.DESC, "createdAt");
        };
    }
}