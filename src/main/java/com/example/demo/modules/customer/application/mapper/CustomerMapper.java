package com.example.demo.modules.customer.application.mapper;

import com.example.demo.common.mapper.BaseMapper;
import com.example.demo.modules.customer.application.dto.request.AddressRequest;
import com.example.demo.modules.customer.application.dto.request.RegisterCustomerRequest;
import com.example.demo.modules.customer.application.dto.request.UpdateCustomerPreferencesRequest;
import com.example.demo.modules.customer.application.dto.response.*;
import com.example.demo.modules.customer.domain.model.Customer;
import com.example.demo.modules.customer.domain.model.CustomerAddress;
import com.example.demo.modules.customer.domain.model.vo.*;
import com.example.demo.modules.order.domain.model.vo.Address;
import org.mapstruct.*;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * MapStruct mapper for Customer entity <-> DTOs
 *
 * Follows the same conventions as UserMapper:
 * - abstract class implementing BaseMapper
 * - MapStruct for generation
 * - Manual helpers for VO construction
 *
 * @author Customer Domain Team
 */
@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
public abstract class CustomerMapper implements BaseMapper<Customer, CustomerResponse> {

    // ====================================
    // Customer → Response (BaseMapper.toDto)
    // ====================================

    /**
     * Map Customer to full CustomerResponse
     * Implementation of BaseMapper.toDto()
     */
    @Override
    @Mapping(target = "firstName",        expression = "java(customer.getName().getFirstName())")
    @Mapping(target = "lastName",         expression = "java(customer.getName().getLastName())")
    @Mapping(target = "fullName",         expression = "java(customer.getName().getFullName())")
    @Mapping(target = "email",            expression = "java(customer.getEmail().getValue())")
    @Mapping(target = "phone",            expression = "java(customer.getPhone().getValue())")
    @Mapping(target = "status",           expression = "java(customer.getStatus())")
    @Mapping(target = "addresses",        expression = "java(toAddressResponseList(customer.getAllAddresses()))")
    @Mapping(target = "preferredAddressId", source = "preferredAddressId")
    @Mapping(target = "preferences",      expression = "java(toPreferencesResponse(customer.getPreferences()))")
    @Mapping(target = "canPlaceOrder",    expression = "java(customer.canPlaceOrder())")
    @Mapping(target = "canLogin",         expression = "java(customer.canLogin())")
    @Mapping(target = "createdAt",        source = "createdAt")
    @Mapping(target = "updatedAt",        source = "updatedAt")
    @Mapping(target = "createdBy",        source = "createdBy")
    @Mapping(target = "updatedBy",        source = "updatedBy")
    @Mapping(target = "version",          source = "version")
    @Mapping(target = "deleted",          expression = "java(customer.isDeleted())")
    @Mapping(target = "deletedAt",        source = "deletedAt")
    @Mapping(target = "deletedBy",        source = "deletedBy")
    public abstract CustomerResponse toDto(Customer customer);

    /**
     * Alias for readability in services
     */
    public CustomerResponse toResponse(Customer customer) {
        return toDto(customer);
    }

    /**
     * Not supported – cannot create a Customer aggregate from a response DTO
     */
    @Override
    public Customer toEntity(CustomerResponse dto) {
        throw new UnsupportedOperationException(
                "Cannot create Customer from response DTO. Use fromRegisterRequest() instead.");
    }

    // ====================================
    // Customer → Summary Response
    // ====================================

    @Mapping(target = "id",           source = "id")
    @Mapping(target = "fullName",     expression = "java(customer.getName().getFullName())")
    @Mapping(target = "email",        expression = "java(customer.getEmail().getValue())")
    @Mapping(target = "phone",        expression = "java(customer.getPhone().getValue())")
    @Mapping(target = "status",       source = "status")
    @Mapping(target = "addressCount", expression = "java(customer.getAllAddresses().size())")
    @Mapping(target = "createdAt",    source = "createdAt")
    public abstract CustomerSummaryResponse toSummaryResponse(Customer customer);

    public List<CustomerSummaryResponse> toSummaryResponseList(List<Customer> customers) {
        if (customers == null || customers.isEmpty()) {
            return Collections.emptyList();
        }
        return customers.stream()
                .map(this::toSummaryResponse)
                .collect(Collectors.toList());
    }

    // ====================================
    // CustomerAddress → Response
    // ====================================

    @Mapping(target = "id",            source = "id")
    @Mapping(target = "streetAddress", source = "address.streetAddress")
    @Mapping(target = "streetAddress2",source = "address.streetAddress2")
    @Mapping(target = "city",          source = "address.city")
    @Mapping(target = "stateProvince", source = "address.stateProvince")
    @Mapping(target = "postalCode",    source = "address.postalCode")
    @Mapping(target = "country",       source = "address.country")
    @Mapping(target = "phone",         source = "address.phone")
    @Mapping(target = "type",          source = "type")
    @Mapping(target = "preferred",     source = "preferred")
    @Mapping(target = "fullAddress",   expression = "java(customerAddress.getAddress().getFullAddress())")
    public abstract CustomerAddressResponse toAddressResponse(CustomerAddress customerAddress);

    public List<CustomerAddressResponse> toAddressResponseList(List<CustomerAddress> addresses) {
        if (addresses == null || addresses.isEmpty()) {
            return Collections.emptyList();
        }
        return addresses.stream()
                .map(this::toAddressResponse)
                .collect(Collectors.toList());
    }

    // ====================================
    // CustomerPreferences → Response
    // ====================================

    @Mapping(target = "language",           source = "language")
    @Mapping(target = "currency",           source = "currency")
    @Mapping(target = "emailNotifications", source = "emailNotifications")
    @Mapping(target = "smsNotifications",   source = "smsNotifications")
    @Mapping(target = "marketingEmails",    source = "marketingEmails")
    public abstract CustomerPreferencesResponse toPreferencesResponse(CustomerPreferences preferences);

    // ====================================
    // Request → Domain VO Helpers
    // ====================================

    /**
     * Build a new Customer aggregate from a registration request.
     * Called by the command service – VO validation happens inside constructors.
     */
    public Customer fromRegisterRequest(RegisterCustomerRequest request) {
        return new Customer(
                buildCustomerName(request.getFirstName(), request.getLastName()),
                buildEmail(request.getEmail()),
                buildPhone(request.getPhone())
        );
    }

    /**
     * Build an Address VO from an AddressRequest
     */
    public Address toAddress(AddressRequest request) {
        if (request == null) return null;
        return Address.builder()
                .streetAddress(request.getStreetAddress())
                .streetAddress2(request.getStreetAddress2())
                .city(request.getCity())
                .stateProvince(request.getStateProvince())
                .postalCode(request.getPostalCode())
                .country(request.getCountry())
                .phone(request.getPhone())
                .build();
    }

    /**
     * Build a CustomerPreferences VO from a preferences request
     */
    public CustomerPreferences toPreferences(UpdateCustomerPreferencesRequest request) {
        if (request == null) return CustomerPreferences.defaultPreferences();
        return CustomerPreferences.of(
                request.getLanguage(),
                request.getCurrency(),
                request.isEmailNotifications(),
                request.isSmsNotifications(),
                request.isMarketingEmails()
        );
    }

    // ====================================
    // Private VO Construction Helpers
    // ====================================

    protected CustomerName buildCustomerName(String firstName, String lastName) {
        return new CustomerName(firstName, lastName);
    }

    protected Email buildEmail(String email) {
        return Email.of(email);
    }

    protected Phone buildPhone(String phone) {
        return new Phone(phone);
    }
}