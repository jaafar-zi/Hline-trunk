package com.example.demo.modules.customer.interfaces;

import com.example.demo.common.base.dto.BaseResponse;
import com.example.demo.common.base.dto.PageResponse;
import com.example.demo.modules.customer.application.command.CustomerCommandService;
import com.example.demo.modules.customer.application.dto.request.*;
import com.example.demo.modules.customer.application.dto.response.*;
import com.example.demo.modules.customer.application.query.CustomerQueryService;
import com.example.demo.modules.customer.domain.model.vo.CustomerStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST Controller for Customer operations
 *
 * Endpoints:
 * - POST   /api/v1/customers                          → register
 * - GET    /api/v1/customers                          → search/list (paginated)
 * - GET    /api/v1/customers/{id}                     → find by ID
 * - PUT    /api/v1/customers/{id}/profile             → update profile
 * - PUT    /api/v1/customers/{id}/preferences         → update preferences
 * - DELETE /api/v1/customers/{id}                     → soft delete
 * - POST   /api/v1/customers/{id}/restore             → restore
 * - POST   /api/v1/customers/{id}/activate            → activate
 * - POST   /api/v1/customers/{id}/deactivate          → deactivate
 * - POST   /api/v1/customers/{id}/suspend             → suspend
 * - POST   /api/v1/customers/{id}/block               → block
 * - GET    /api/v1/customers/{id}/addresses           → list addresses
 * - POST   /api/v1/customers/{id}/addresses           → add address
 * - PUT    /api/v1/customers/{id}/addresses/{addrId}  → update address
 * - DELETE /api/v1/customers/{id}/addresses/{addrId}  → remove address
 * - PUT    /api/v1/customers/{id}/addresses/{addrId}/preferred → set preferred
 *
 * @author Customer Domain Team
 * @version 1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
@Tag(name = "Customer", description = "Customer management endpoints")
public class CustomerController {

    private final CustomerCommandService commandService;
    private final CustomerQueryService   queryService;

    // ====================================
    // Registration
    // ====================================

    @PostMapping
    @Operation(summary = "Register a new customer")
    public ResponseEntity<BaseResponse<CustomerResponse>> register(
            @Valid @RequestBody RegisterCustomerRequest request) {

        log.info("POST /api/v1/customers - register customer");
        CustomerResponse response = commandService.register(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(BaseResponse.success(response, "Customer registered successfully"));
    }

    // ====================================
    // Queries
    // ====================================

    @GetMapping
    @Operation(summary = "Search customers with filters and pagination")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPPORT')")
    public ResponseEntity<BaseResponse<PageResponse<CustomerSummaryResponse>>> findAll(
            @Valid CustomerFilterRequest filter) {

        log.info("GET /api/v1/customers - search customers");
        PageResponse<CustomerSummaryResponse> page = queryService.findAll(filter);
        return ResponseEntity.ok(BaseResponse.success(page));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Find customer by ID")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPPORT') or @customerSecurity.isOwner(#id, principal)")
    public ResponseEntity<BaseResponse<CustomerResponse>> findById(
            @Parameter(description = "Customer ID") @PathVariable UUID id) {

        log.info("GET /api/v1/customers/{}", id);
        CustomerResponse response = queryService.findById(id);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @GetMapping("/by-status/{status}")
    @Operation(summary = "Find customers by status")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPPORT')")
    public ResponseEntity<BaseResponse<List<CustomerSummaryResponse>>> findByStatus(
            @PathVariable CustomerStatus status) {

        log.info("GET /api/v1/customers/by-status/{}", status);
        List<CustomerSummaryResponse> customers = queryService.findByStatus(status);
        return ResponseEntity.ok(BaseResponse.success(customers));
    }

    // ====================================
    // Profile & Preferences
    // ====================================

    @PutMapping("/{id}/profile")
    @Operation(summary = "Update customer profile")
    @PreAuthorize("hasRole('ADMIN') or @customerSecurity.isOwner(#id, principal)")
    public ResponseEntity<BaseResponse<CustomerResponse>> updateProfile(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCustomerProfileRequest request) {

        log.info("PUT /api/v1/customers/{}/profile", id);
        CustomerResponse response = commandService.updateProfile(id, request);
        return ResponseEntity.ok(BaseResponse.success(response, "Profile updated successfully"));
    }

    @PutMapping("/{id}/preferences")
    @Operation(summary = "Update customer preferences")
    @PreAuthorize("hasRole('ADMIN') or @customerSecurity.isOwner(#id, principal)")
    public ResponseEntity<BaseResponse<CustomerResponse>> updatePreferences(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCustomerPreferencesRequest request) {

        log.info("PUT /api/v1/customers/{}/preferences", id);
        CustomerResponse response = commandService.updatePreferences(id, request);
        return ResponseEntity.ok(BaseResponse.success(response, "Preferences updated successfully"));
    }

    // ====================================
    // Status Lifecycle
    // ====================================

    @PostMapping("/{id}/activate")
    @Operation(summary = "Activate customer account")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<CustomerResponse>> activate(
            @PathVariable UUID id) {

        log.info("POST /api/v1/customers/{}/activate", id);
        CustomerResponse response = commandService.activate(id);
        return ResponseEntity.ok(BaseResponse.success(response, "Customer activated successfully"));
    }

    @PostMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate customer account")
    @PreAuthorize("hasRole('ADMIN') or @customerSecurity.isOwner(#id, principal)")
    public ResponseEntity<BaseResponse<CustomerResponse>> deactivate(
            @PathVariable UUID id) {

        log.info("POST /api/v1/customers/{}/deactivate", id);
        CustomerResponse response = commandService.deactivate(id);
        return ResponseEntity.ok(BaseResponse.success(response, "Customer deactivated successfully"));
    }

    @PostMapping("/{id}/suspend")
    @Operation(summary = "Suspend customer account (admin)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<CustomerResponse>> suspend(
            @PathVariable UUID id,
            @Valid @RequestBody CustomerStatusRequest request) {

        log.info("POST /api/v1/customers/{}/suspend", id);
        CustomerResponse response = commandService.suspend(id, request);
        return ResponseEntity.ok(BaseResponse.success(response, "Customer suspended successfully"));
    }

    @PostMapping("/{id}/block")
    @Operation(summary = "Block customer account (admin)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<CustomerResponse>> block(
            @PathVariable UUID id,
            @Valid @RequestBody CustomerStatusRequest request) {

        log.info("POST /api/v1/customers/{}/block", id);
        CustomerResponse response = commandService.block(id, request);
        return ResponseEntity.ok(BaseResponse.success(response, "Customer blocked successfully"));
    }

    // ====================================
    // Soft Delete / Restore
    // ====================================

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft-delete a customer")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<Void>> delete(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails currentUser) {

        log.info("DELETE /api/v1/customers/{}", id);
        commandService.delete(id, currentUser.getUsername());
        return ResponseEntity.ok(BaseResponse.success(null, "Customer deleted successfully"));
    }

    @PostMapping("/{id}/restore")
    @Operation(summary = "Restore a soft-deleted customer")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<CustomerResponse>> restore(
            @PathVariable UUID id) {

        log.info("POST /api/v1/customers/{}/restore", id);
        CustomerResponse response = commandService.restore(id);
        return ResponseEntity.ok(BaseResponse.success(response, "Customer restored successfully"));
    }

    // ====================================
    // Addresses
    // ====================================

    @GetMapping("/{id}/addresses")
    @Operation(summary = "Get all addresses for a customer")
    @PreAuthorize("hasRole('ADMIN') or @customerSecurity.isOwner(#id, principal)")
    public ResponseEntity<BaseResponse<List<CustomerAddressResponse>>> findAddresses(
            @PathVariable UUID id) {

        log.info("GET /api/v1/customers/{}/addresses", id);
        List<CustomerAddressResponse> addresses = queryService.findAddresses(id);
        return ResponseEntity.ok(BaseResponse.success(addresses));
    }

    @GetMapping("/{id}/addresses/preferred")
    @Operation(summary = "Get preferred address for a customer")
    @PreAuthorize("hasRole('ADMIN') or @customerSecurity.isOwner(#id, principal)")
    public ResponseEntity<BaseResponse<CustomerAddressResponse>> findPreferredAddress(
            @PathVariable UUID id) {

        log.info("GET /api/v1/customers/{}/addresses/preferred", id);
        CustomerAddressResponse address = queryService.findPreferredAddress(id);
        return ResponseEntity.ok(BaseResponse.success(address));
    }

    @GetMapping("/{id}/addresses/shipping")
    @Operation(summary = "Get shipping addresses for a customer")
    @PreAuthorize("hasRole('ADMIN') or @customerSecurity.isOwner(#id, principal)")
    public ResponseEntity<BaseResponse<List<CustomerAddressResponse>>> findShippingAddresses(
            @PathVariable UUID id) {

        log.info("GET /api/v1/customers/{}/addresses/shipping", id);
        List<CustomerAddressResponse> addresses = queryService.findShippingAddresses(id);
        return ResponseEntity.ok(BaseResponse.success(addresses));
    }

    @GetMapping("/{id}/addresses/billing")
    @Operation(summary = "Get billing addresses for a customer")
    @PreAuthorize("hasRole('ADMIN') or @customerSecurity.isOwner(#id, principal)")
    public ResponseEntity<BaseResponse<List<CustomerAddressResponse>>> findBillingAddresses(
            @PathVariable UUID id) {

        log.info("GET /api/v1/customers/{}/addresses/billing", id);
        List<CustomerAddressResponse> addresses = queryService.findBillingAddresses(id);
        return ResponseEntity.ok(BaseResponse.success(addresses));
    }

    @PostMapping("/{id}/addresses")
    @Operation(summary = "Add a new address to customer")
    @PreAuthorize("hasRole('ADMIN') or @customerSecurity.isOwner(#id, principal)")
    public ResponseEntity<BaseResponse<CustomerAddressResponse>> addAddress(
            @PathVariable UUID id,
            @Valid @RequestBody AddressRequest request) {

        log.info("POST /api/v1/customers/{}/addresses", id);
        CustomerAddressResponse address = commandService.addAddress(id, request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(BaseResponse.success(address, "Address added successfully"));
    }

    @PutMapping("/{id}/addresses/{addressId}")
    @Operation(summary = "Update an existing address")
    @PreAuthorize("hasRole('ADMIN') or @customerSecurity.isOwner(#id, principal)")
    public ResponseEntity<BaseResponse<CustomerAddressResponse>> updateAddress(
            @PathVariable UUID id,
            @PathVariable Long addressId,
            @Valid @RequestBody AddressRequest request) {

        log.info("PUT /api/v1/customers/{}/addresses/{}", id, addressId);
        CustomerAddressResponse address = commandService.updateAddress(id, addressId, request);
        return ResponseEntity.ok(BaseResponse.success(address, "Address updated successfully"));
    }

    @DeleteMapping("/{id}/addresses/{addressId}")
    @Operation(summary = "Remove an address from customer")
    @PreAuthorize("hasRole('ADMIN') or @customerSecurity.isOwner(#id, principal)")
    public ResponseEntity<BaseResponse<Void>> removeAddress(
            @PathVariable UUID id,
            @PathVariable Long addressId) {

        log.info("DELETE /api/v1/customers/{}/addresses/{}", id, addressId);
        commandService.removeAddress(id, addressId);
        return ResponseEntity.ok(BaseResponse.success(null, "Address removed successfully"));
    }

    @PutMapping("/{id}/addresses/{addressId}/preferred")
    @Operation(summary = "Set preferred address for customer")
    @PreAuthorize("hasRole('ADMIN') or @customerSecurity.isOwner(#id, principal)")
    public ResponseEntity<BaseResponse<CustomerResponse>> setPreferredAddress(
            @PathVariable UUID id,
            @PathVariable Long addressId) {

        log.info("PUT /api/v1/customers/{}/addresses/{}/preferred", id, addressId);
        CustomerResponse response = commandService.setPreferredAddress(id, addressId);
        return ResponseEntity.ok(BaseResponse.success(response, "Preferred address updated successfully"));
    }
}